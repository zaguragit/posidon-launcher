package posidon.launcher.feed.notifications

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.*
import android.os.*
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.feed.notifications.SwipeToDeleteCallback.SwipeListener
import posidon.launcher.storage.Settings
import posidon.launcher.tools.*
import java.lang.ref.WeakReference
import java.util.*

class NotificationService : NotificationListenerService() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (!Settings.isInitialized) Settings.init(baseContext)
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

    override fun onNotificationPosted(s: StatusBarNotification) = onUpdate()
    override fun onNotificationRemoved(s: StatusBarNotification) = onUpdate()
    override fun onNotificationRemoved(s: StatusBarNotification, rm: RankingMap, reason: Int) = onUpdate()
    override fun onNotificationRankingUpdate(rm: RankingMap) = onUpdate()
    override fun onNotificationChannelModified(pkg: String, u: UserHandle, c: NotificationChannel, modifType: Int) = onUpdate()
    override fun onNotificationChannelGroupModified(pkg: String, u: UserHandle, g: NotificationChannelGroup, modifType: Int) = onUpdate()

    private fun onUpdate() {
        if (!updating) try { NotificationLoader(activeNotifications).start() }
        catch (e: Exception) { NotificationLoader(null).start() }
    }

    private class NotificationLoader(private val notifications: Array<StatusBarNotification>?) : Thread() {
        override fun run() {
            var hasMusic = false
            updating = true
            val groups = ArrayList<ArrayList<Notification>>()
            var i = 0
            var notificationsAmount2 = 0
            try {
                if (notifications != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Settings["notifications:groupingType", "os"] == "os") {
                        while (i < notifications.size) {
                            if (notifications[i].notification.extras.getCharSequence(android.app.Notification.EXTRA_TEMPLATE)?.let { it.subSequence(25, it.length) == "MediaStyle" } == true) {
                                handleMusicNotification(notifications[i])
                                hasMusic = true
                                i++; continue
                            }
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
                    } else if (Settings["notifications:groupingType", "os"] == "byApp") {
                        while (i < notifications.size) {
                            if (notifications[i].notification.extras.getCharSequence(android.app.Notification.EXTRA_TEMPLATE)?.let { it.subSequence(25, it.length) == "MediaStyle" } == true) {
                                handleMusicNotification(notifications[i])
                                hasMusic = true
                                i++; continue
                            }
                            val group = ArrayList<Notification>()
                            val packageName = notifications[i].packageName
                            var last: Bundle? = null
                            var extras: Bundle
                            while (i < notifications.size && notifications[i].packageName == packageName) {
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
                            groups.add(group)
                        }
                    } else while (i < notifications.size) {
                        if (notifications[i].notification.extras.getCharSequence(android.app.Notification.EXTRA_TEMPLATE)?.let { it.subSequence(25, it.length) == "MediaStyle" } == true) {
                            handleMusicNotification(notifications[i])
                            hasMusic = true
                            i++; continue
                        }
                        val group = ArrayList<Notification>()
                        group.add(formatNotification(notifications[i]))
                        groups.add(group)
                        notificationsAmount2++
                        i++
                    }
                    if (!hasMusic) Main.instance.runOnUiThread { Main.instance.findViewById<View>(R.id.musicCard).visibility = View.GONE }
                } else Main.instance.runOnUiThread { Main.instance.findViewById<View>(R.id.musicCard).visibility = View.GONE }
            } catch (e: Exception) { e.printStackTrace() }
            notificationGroups.clear()
            notificationGroups = groups
            notificationsAmount = notificationsAmount2
            onUpdate()
            updating = false
        }

        private fun formatNotification(notification: StatusBarNotification): Notification {
            val extras = notification.notification.extras
            val isSummary = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && notification.notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY != 0
            var title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)
            if (title == null || title.toString().replace(" ", "").isEmpty()) {
                try { title = contextReference!!.get()!!.packageManager.getApplicationLabel(contextReference!!.get()!!.packageManager.getApplicationInfo(notification.packageName, 0)) }
                catch (e: Exception) { e.printStackTrace() }
            }
            var icon: Drawable? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) try {
                icon = notification.notification.getLargeIcon().loadDrawable(contextReference!!.get())
            } catch (ignore: Exception) {}
            if (icon == null) try {
                icon = contextReference!!.get()!!.createPackageContext(notification.packageName, 0).resources.getDrawable(notification.notification.icon)
                Tools.animate(icon)
                val colorList = ColorStateList.valueOf(if (notification.notification.color == Settings["notificationbgcolor", -0x1] || notification.notification.color == 0) Settings["notificationtitlecolor", -0xeeeded] else notification.notification.color)
                icon.setTintList(colorList)
            } catch (e: Exception) { e.printStackTrace() }
            var text = extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT)
            if (text == null || isSummary) text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val messages = extras.getParcelableArray(android.app.Notification.EXTRA_MESSAGES)
                if (messages != null) text = StringBuilder().apply {
                    messages.forEach {
                        val bundle = it as Bundle
                        appendln(bundle.getCharSequence("text"))
                    }
                    delete(lastIndex, length)
                }
            }

            //val progress = extras.getInt(android.app.Notification.EXTRA_PROGRESS, -1)
            //println("PROGRESSSSSSSS: --->>>>>$progress")
            //println("MAXXXX PROGRES: --->>>>>" + extras.getInt(android.app.Notification.EXTRA_PROGRESS_MAX, -1))
            //println("INTETERMINATTE: --->>>>>" + extras.getInt(android.app.Notification.EXTRA_PROGRESS_INDETERMINATE, -1))

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
                notification.key, -1
            )
        }
    }

    companion object {
        private var notificationGroups = ArrayList<ArrayList<Notification>>()
        var onUpdate = {}
		var contextReference: WeakReference<Context>? = null
		var notificationsAmount = 0
        private var updating = false
        fun groups(): ArrayList<ArrayList<Notification>> = notificationGroups

        fun handleMusicNotification(notification: StatusBarNotification) {
            println("handling music notification")
            var icon: Drawable? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) try {
                icon = notification.notification.getLargeIcon().loadDrawable(contextReference!!.get())
            } catch (ignore: Exception) {}
            if (icon == null) try {
                icon = contextReference!!.get()!!.createPackageContext(notification.packageName, 0).resources.getDrawable(notification.notification.icon)
                Tools.animate(icon)
                val colorList = ColorStateList.valueOf(if (notification.notification.color == Settings["notificationbgcolor", -0x1] || notification.notification.color == 0) Settings["notificationtitlecolor", -0xeeeded] else notification.notification.color)
                icon.setTintList(colorList)
            } catch (e: Exception) { e.printStackTrace() }
            val color = Palette.from(icon!!.toBitmap(true)).generate().getDominantColor(Settings["notificationbgcolor", -0x1])

            var title = notification.notification.extras.getCharSequence(android.app.Notification.EXTRA_TITLE)
            if (title == null || title.toString().replace(" ", "").isEmpty()) {
                try { title = contextReference!!.get()!!.packageManager.getApplicationLabel(contextReference!!.get()!!.packageManager.getApplicationInfo(notification.packageName, 0)) }
                catch (e: Exception) { e.printStackTrace() }
            }

            var subtitle = notification.notification.extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT)
            if (subtitle == null) subtitle = notification.notification.extras.getCharSequence(android.app.Notification.EXTRA_TEXT)

            Main.instance.runOnUiThread {
                Main.instance.findViewById<View>(R.id.musicCard).apply {
                    visibility = View.VISIBLE
                    setOnClickListener { notification.notification.contentIntent?.send() }
                }
                Main.instance.findViewById<ImageView>(R.id.musicCardImage).setImageDrawable(icon)
                Main.instance.findViewById<TextView>(R.id.musicCardTrackTitle).apply {
                    setTextColor(if (ColorTools.useDarkText(color)) -0xeeeded else -0x1)
                    text = title
                }
                Main.instance.findViewById<TextView>(R.id.musicCardTrackArtist).apply {
                    setTextColor(if (ColorTools.useDarkText(color)) -0xeeeded else -0x1)
                    text = subtitle
                }
                Main.instance.findViewById<View>(R.id.musicCardOverImg).background = LayerDrawable(arrayOf(
                        ColorDrawable(color),
                        GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(color, color and 0x00ffffff))
                )).apply {
                    val marginX = (Settings["feed:card_margin_x", 16] * contextReference!!.get()!!.resources.displayMetrics.density).toInt()
                    setLayerInset(0, 0, 0, 136.dp(contextReference!!.get()!!).toInt(), 0)
                    setLayerInset(1, Tools.getDisplayWidth(contextReference!!.get()!!) - 136.dp(contextReference!!.get()!!).toInt() - marginX * 2, 0, 0, 0)
                }
                Main.instance.findViewById<ImageView>(R.id.musicPrev).imageTintList = ColorStateList.valueOf(if (ColorTools.useDarkText(color)) -0xeeeded else -0x1)
                Main.instance.findViewById<ImageView>(R.id.musicPlay).imageTintList = ColorStateList.valueOf(if (ColorTools.useDarkText(color)) -0xeeeded else -0x1)
                Main.instance.findViewById<ImageView>(R.id.musicNext).imageTintList = ColorStateList.valueOf(if (ColorTools.useDarkText(color)) -0xeeeded else -0x1)
            }
        }
    }
}