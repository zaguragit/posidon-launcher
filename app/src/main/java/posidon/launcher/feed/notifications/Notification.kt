package posidon.launcher.feed.notifications

import android.app.Notification
import android.app.PendingIntent
import android.graphics.drawable.Drawable

class Notification(
        val title: CharSequence?,
        val text: CharSequence?,
        val isSummary: Boolean,
        val bigPic: Drawable?,
        val icon: Drawable?,
        val actions: Array<Notification.Action>?,
        private val contentIntent: PendingIntent?,
        val key: String
) {
    fun open() {
        try { contentIntent?.send() }
        catch (ignore: Exception) {}
    }
}