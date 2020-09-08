package posidon.launcher.feed.news

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.app.ActivityOptionsCompat
import posidon.launcher.R
import posidon.launcher.feed.news.chooser.Source
import posidon.launcher.feed.news.readers.ArticleActivity
import posidon.launcher.feed.news.readers.WebViewActivity
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Loader
import java.util.*

class FeedItem(
    val title: String,
    val link: String,
    val img: String?,
    val time: Date,
    val source: Source) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FeedItem
        if (title != other.title) return false
        if (link != other.link) return false
        return true
    }

    override fun hashCode() = 31 * title.hashCode() + link.hashCode()

    inline fun isBefore(other: FeedItem) = time.before(other.time)

    fun tryLoadImage(width: Int, height: Int, onLoad: (Bitmap) -> Unit) {
        Loader.loadNullableBitmap(img!!, width, height, false) {
            if (it == null) {
                Loader.loadNullableBitmap(source.domain + '/' + img, width, height, false) {
                    if (it != null) onLoad(it)
                }
            } else onLoad(it)
        }
    }

    fun open(context: Context) = try {
        val activityOptions = ActivityOptionsCompat.makeCustomAnimation(
            context,
            R.anim.slideup,
            R.anim.home_exit
        ).toBundle()

        when (Settings["feed:openLinks", "browse"]) {
            "webView" -> context.startActivity(Intent(context, WebViewActivity::class.java).apply {
                putExtra("url", link)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            }, activityOptions)
            "app" -> context.startActivity(Intent(context, ArticleActivity::class.java).apply {
                putExtra("url", link)
                putExtra("sourceName", source.name)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            }, activityOptions)
            else -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.trim { it <= ' ' })), activityOptions)
        }
    } catch (e: Exception) { e.printStackTrace() }
}