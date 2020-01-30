package posidon.launcher.feed.notifications

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.UserHandle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.recyclerview.widget.RecyclerView
import posidon.launcher.feed.notifications.SwipeToDeleteCallback.SwipeListener
import posidon.launcher.tools.Settings
import posidon.launcher.tools.Tools
import java.lang.ref.WeakReference
import java.util.*

class NotificationService : NotificationListenerService() {
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        SwipeToDeleteCallback.swipeListener = object : SwipeListener {
            override fun onSwipe(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
                try {
                    val pos = viewHolder!!.adapterPosition
                    val group = notificationGroups[pos]
                    for (notification in group) cancelNotification(notification.key)
                    group.clear()
                    notificationGroups.removeAt(pos)
                } catch (e: Exception) { e.printStackTrace() }
                onUpdate()
            }
        }
        onUpdate()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onNotificationPosted(s: StatusBarNotification) { onUpdate() }
    override fun onNotificationRemoved(s: StatusBarNotification) { onUpdate() }
    override fun onNotificationRemoved(s: StatusBarNotification, rm: RankingMap, reason: Int) { onUpdate() }
    override fun onNotificationRankingUpdate(rm: RankingMap) { onUpdate() }
    override fun onNotificationChannelModified(pkg: String, u: UserHandle, c: NotificationChannel, modifType: Int) {
        onUpdate()
    }
    override fun onNotificationChannelGroupModified(pkg: String, u: UserHandle, g: NotificationChannelGroup, modifType: Int) {
        onUpdate()
    }

    private fun onUpdate() {
        if (!updating) try { NotificationLoader(activeNotifications).start() }
        catch (e: Exception) { NotificationLoader(null).start() }
    }

    interface Listener { fun onUpdate() }

    private class NotificationLoader internal constructor(private val notifications: Array<StatusBarNotification>?) : Thread() {
        override fun run() {
            updating = true
            val groups = ArrayList<ArrayList<Notification>>()
            var i = 0
            var notificationsAmount2 = 0
            try {
                if (notifications != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        while (i < notifications.size) {
                            val group = ArrayList<Notification>()
                            if (notifications[i].notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY != 0) {
                                val key = notifications[i].groupKey
                                var last: Bundle? = null
                                var extras: Bundle
                                while (i < notifications.size && notifications[i].isGroup && notifications[i].groupKey == key && notifications[i].groupKey != null) {
                                    extras = notifications[i].notification.extras
                                    if (last == null || extras.getCharSequence(android.app.Notification.EXTRA_TITLE) !=
                                        last.getCharSequence(android.app.Notification.EXTRA_TITLE) || extras.getCharSequence(android.app.Notification.EXTRA_TEXT) !=
                                        last.getCharSequence(android.app.Notification.EXTRA_TEXT) || extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT) !=
                                        last.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT) || notifications[i].notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY == 0) {
                                        group.add(formatNotification(notifications[i]))
                                        if (notifications[i].notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY == 0) notificationsAmount2++
                                    }
                                    last = extras
                                    i++
                                }
                            } else {
                                group.add(formatNotification(notifications[i]))
                                notificationsAmount2++
                                i++
                            }
                            groups.add(group)
                        }
                    } else while (i < notifications.size) {
                        val group = ArrayList<Notification>()
                        group.add(formatNotification(notifications[i]))
                        groups.add(group)
                        notificationsAmount2++
                        i++
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
            notificationGroups.clear()
            notificationGroups = groups
            notificationsAmount = notificationsAmount2
            if (listener != null) listener!!.onUpdate()
            updating = false
        }

        private fun formatNotification(notification: StatusBarNotification): Notification {
            val extras = notification.notification.extras
            val isSummary = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && notification.notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY != 0
            var title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)
            if (title == null || title.toString().replace(" ", "").isEmpty()) {
                try {
                    title = contextReference!!.get()!!.packageManager.getApplicationLabel(contextReference!!.get()!!.packageManager.getApplicationInfo(notification.packageName, 0))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            var icon: Drawable? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) try {
                icon = notification.notification.getLargeIcon().loadDrawable(contextReference!!.get())
            } catch (ignore: Exception) {}
            if (icon == null) try {
                icon = contextReference!!.get()!!.createPackageContext(notification.packageName, 0).resources.getDrawable(notification.notification.icon)
                Tools.animate(icon)
                val colorList = ColorStateList.valueOf(if (notification.notification.color == Settings.get("notificationbgcolor", -0x1) || notification.notification.color == 0) Settings.get("notificationtitlecolor", -0xeeeded) else notification.notification.color)
                icon.setTintList(colorList)
            } catch (e: Exception) { e.printStackTrace() }
            var text = extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT)
            if (text == null || isSummary) text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)
            var bigPic: Drawable? = null
            val b = extras[android.app.Notification.EXTRA_PICTURE] as Bitmap?
            if (b != null) {
                try { bigPic = BitmapDrawable(contextReference!!.get()!!.resources, b) }
                catch (e: Exception) { e.printStackTrace() }
            }
            return Notification(
                    title, text, isSummary, bigPic, icon,
                    notification.notification.actions,
                    notification.notification.contentIntent,
                    extras.getCharSequence(android.app.Notification.EXTRA_TEMPLATE),
                    notification.key
            )
        }

    }

    companion object {
        private var notificationGroups = ArrayList<ArrayList<Notification>>()
        @JvmField
		var listener: Listener? = null
        @JvmField
		var contextReference: WeakReference<Context>? = null
        @JvmField
		var notificationsAmount = 0
        private var updating = false
        fun groups(): ArrayList<ArrayList<Notification>> = notificationGroups
    }
}