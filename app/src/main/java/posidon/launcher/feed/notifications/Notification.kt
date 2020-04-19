package posidon.launcher.feed.notifications

import android.app.Notification
import android.app.PendingIntent
import android.graphics.drawable.Drawable
import android.widget.Toast
import posidon.launcher.tools.Tools

class Notification(
        val title: CharSequence?,
        val text: CharSequence?,
        val isSummary: Boolean,
        val bigPic: Drawable?,
        val icon: Drawable?,
        val actions: Array<Notification.Action>?,
        private val contentIntent: PendingIntent?,
        val key: String,
        val progress: Int
) {
    fun open() {
        Toast.makeText(Tools.publicContext, "app opened", Toast.LENGTH_LONG).show()
        try { contentIntent?.send() }
        catch (ignore: Exception) {}
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as posidon.launcher.feed.notifications.Notification
        if (title != other.title) return false
        if (isSummary != other.isSummary) return false
        if (actions != null) {
            if (other.actions == null) return false
            if (!actions.contentEquals(other.actions)) return false
        } else if (other.actions != null) return false
        if (contentIntent != other.contentIntent) return false
        if (key != other.key) return false
        if (progress != other.progress) return false
        return true
    }

    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + isSummary.hashCode()
        result = 31 * result + (actions?.contentHashCode() ?: 0)
        result = 31 * result + (contentIntent?.hashCode() ?: 0)
        result = 31 * result + key.hashCode()
        result = 31 * result + progress
        return result
    }
}