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
        style: CharSequence?,
        val key: String
) {
    val style: CharSequence? = style?.subSequence(25, style.length)
    fun open() {
        try { contentIntent?.send() }
        catch (ignore: Exception) {}
    }

}