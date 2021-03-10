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
    val key: String,
    val progress: Float
) {
    fun open() {
        try { contentIntent?.send() }
        catch (e: Exception) {}
    }

    fun cancel() {
        NotificationService.instance.cancelNotification(key)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as posidon.launcher.feed.notifications.Notification
        if (title != other.title) return false
        if (isSummary != other.isSummary) return false
        if (key != other.key) return false
        return true
    }

    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + isSummary.hashCode()
        result = 31 * result + (actions?.contentHashCode() ?: 0)
        result = 31 * result + (contentIntent?.hashCode() ?: 0)
        result = 31 * result + key.hashCode()
        result = 31 * result + progress.toInt()
        return result
    }
}