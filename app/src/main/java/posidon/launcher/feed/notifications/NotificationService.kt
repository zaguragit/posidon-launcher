package posidon.launcher.feed.notifications

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.UserHandle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.palette.graphics.Palette
import posidon.android.conveniencelib.Graphics
import posidon.android.conveniencelib.toBitmap
import posidon.launcher.Global
import posidon.launcher.Home
import posidon.launcher.items.App
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

class NotificationService : NotificationListenerService() {

    init {
        update = {
            if (!Settings["search:asHome", false]) {
                try {
                    loadNotifications(activeNotifications)
                } catch (e: Exception) {
                    loadNotifications(null)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Settings.init(applicationContext)
        instance = this
        if (Tools.appContext == null) {
            Tools.appContextReference = WeakReference(applicationContext)
        }
        update()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onNotificationPosted(s: StatusBarNotification) = update()
    override fun onNotificationPosted(s: StatusBarNotification?, rm: RankingMap?) = update()
    override fun onNotificationRemoved(s: StatusBarNotification) = update()
    override fun onNotificationRemoved(s: StatusBarNotification?, rm: RankingMap?) = update()
    override fun onNotificationRemoved(s: StatusBarNotification, rm: RankingMap, reason: Int) = update()
    override fun onNotificationRankingUpdate(rm: RankingMap) = update()
    override fun onNotificationChannelModified(pkg: String, u: UserHandle, c: NotificationChannel, modifType: Int) = update()
    override fun onNotificationChannelGroupModified(pkg: String, u: UserHandle, g: NotificationChannelGroup, modifType: Int) = update()

    private fun loadNotifications(notifications: Array<StatusBarNotification>?) = thread (isDaemon = true) {

        Settings.init(applicationContext)

        fun showNotificationBadgeOnPackage(packageName: String) {
            App.getFromPackage(packageName)
                ?.forEach { it.notificationCount++ }
        }

        var hasMusic = false
        val groups = ArrayList<ArrayList<NotificationItem>>()
        var i = 0
        var notificationsAmount2 = 0
        lock.withLock {
            try {
                for (app in Global.apps) {
                    app.notificationCount = 0
                }
                if (notifications != null) {
                    val aggregator = when (Settings["notifications:groupingType", "os"]) {
                        "byApp" -> ({ notification: StatusBarNotification ->
                            val group = ArrayList<NotificationItem>()
                            val packageName = notification.packageName
                            var last: Bundle? = null
                            var extras: Bundle
                            while (
                                i < notifications.size &&
                                notifications[i].packageName == packageName
                            ) {
                                extras = notifications[i].notification.extras
                                if (last == null ||
                                    extras.getCharSequence(android.app.Notification.EXTRA_TITLE) != last.getCharSequence(android.app.Notification.EXTRA_TITLE) ||
                                    extras.getCharSequence(android.app.Notification.EXTRA_TEXT) != last.getCharSequence(android.app.Notification.EXTRA_TEXT) ||
                                    extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT) != last.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT) ||
                                    notifications[i].notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY == 0
                                ) {
                                    showNotificationBadgeOnPackage(notifications[i].packageName)
                                    group.add(
                                        NotificationCreator.create(
                                            applicationContext,
                                            notifications[i]
                                        )
                                    )
                                    if (notifications[i].notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY == 0)
                                        notificationsAmount2++
                                }
                                last = extras
                                i++
                            }
                            groups.add(group)
                        })
                        "none" -> ({ notification: StatusBarNotification ->
                            showNotificationBadgeOnPackage(notification.packageName)
                            val group = ArrayList<NotificationItem>()
                            group.add(NotificationCreator.create(applicationContext, notification))
                            groups.add(group)
                            notificationsAmount2++
                            i++
                        })
                        else -> ({ notification: StatusBarNotification ->
                            val group = ArrayList<NotificationItem>()
                            if (notification.notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY != 0) {
                                val key = notification.groupKey
                                var last: Bundle? = null
                                var extras: Bundle
                                while (
                                    i < notifications.size && notifications[i].isGroup &&
                                    notifications[i].groupKey == key && notifications[i].groupKey != null
                                ) {
                                    extras = notifications[i].notification.extras
                                    if (last == null ||
                                        extras.getCharSequence(android.app.Notification.EXTRA_TITLE) != last!!.getCharSequence(android.app.Notification.EXTRA_TITLE) ||
                                        extras.getCharSequence(android.app.Notification.EXTRA_TEXT) != last!!.getCharSequence(android.app.Notification.EXTRA_TEXT) ||
                                        extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT) != last!!.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT) ||
                                        notifications[i].notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY == 0
                                    ) {
                                        showNotificationBadgeOnPackage(notifications[i].packageName)
                                        group.add(
                                            NotificationCreator.create(
                                                applicationContext,
                                                notifications[i]
                                            )
                                        )
                                        if (notifications[i].notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY == 0)
                                            notificationsAmount2++
                                    }
                                    last = extras
                                    i++
                                }
                            } else {
                                showNotificationBadgeOnPackage(notification.packageName)
                                group.add(NotificationCreator.create(applicationContext, notification))
                                notificationsAmount2++
                                i++
                            }
                            groups.add(group)
                        })
                    }
                    while (i < notifications.size) {
                        val notification = notifications[i]

                        if (!notification.isClearable && Settings["notif:hide_persistent", false]) {
                            i++
                            continue
                        }

                        if (
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                            && notification.notification.bubbleMetadata?.isNotificationSuppressed == true
                        ) {
                            i++
                            continue
                        }

                        if (Settings["notif:ex:${notification.packageName}", false]) {
                            i++
                            continue
                        }

                        if (!hasMusic && Home.instance.feed.musicCard != null && notification.notification.extras.getCharSequence(
                                android.app.Notification.EXTRA_TEMPLATE
                            )?.let { it.subSequence(25, it.length) == "MediaStyle" } == true
                        ) {
                            handleMusicNotification(applicationContext, notification)
                            hasMusic = true
                            i++
                            continue
                        }

                        aggregator(notification)
                    }
                    if (!hasMusic) Home.instance.runOnUiThread {
                        Home.instance.feed.musicCard?.visibility = View.GONE
                    }
                } else Home.instance.runOnUiThread {
                    Home.instance.feed.musicCard?.visibility = View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } catch (e: OutOfMemoryError) {
                groups.clear()
                notificationGroups.clear()
                notificationsAmount2 = 0
                System.gc()
            }
            notificationGroups = groups
            notificationsAmount = notificationsAmount2
            onUpdate()
        }
    }

    companion object {

        lateinit var instance: NotificationService private set

        var notificationGroups = ArrayList<ArrayList<NotificationItem>>()
            private set

        var onUpdate = {}

		var notificationsAmount = 0
        private val lock = ReentrantLock()

        var update = {}
            private set

        private fun handleMusicNotification(context: Context, notification: StatusBarNotification) {
            val icon = getIcon(context, notification)
            val extras = notification.notification.extras

            //println(extras.keySet().joinToString("\n") { "$it -> " + extras[it].toString() })

            var title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)
            if (title == null || title.toString().replace(" ", "").isEmpty()) {
                try { title = context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(notification.packageName, 0)) }
                catch (e: Exception) { e.printStackTrace() }
            }

            var subtitle = extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT)
            if (subtitle == null) subtitle = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)

            Palette.from(icon!!.toBitmap()).generate {
                val def = Settings["notificationbgcolor", -0x1]
                val color = it?.getDominantColor(def) ?: def
                Home.instance.feed.musicCard?.visibility = View.VISIBLE
                Home.instance.feed.musicCard?.updateTrack(color, title, subtitle, icon, notification.notification.contentIntent)
            }
        }

        private inline fun getIcon(context: Context, n: StatusBarNotification): Drawable? {
            try {
                return n.notification.getLargeIcon().loadDrawable(context)
            } catch (e: Exception) {}
            try {
                return ResourcesCompat.getDrawable(context.createPackageContext(n.packageName, 0).resources, n.notification.icon, null)?.also {
                    Graphics.tryAnimate(Home.instance, it)
                    val colorList = ColorStateList.valueOf(if (n.notification.color == Settings["notificationbgcolor", -0x1] || n.notification.color == 0) Settings["notificationtitlecolor", -0xeeeded] else n.notification.color)
                    it.setTintList(colorList)
                }
            } catch (e: Exception) { e.printStackTrace() }
            return null
        }
    }
}