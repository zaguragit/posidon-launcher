package posidon.launcher.feed.news

import posidon.launcher.feed.news.chooser.Source
import java.text.SimpleDateFormat
import java.util.*

class FeedItem(val title: String, val link: String, val img: String?, timeString: String?, val source: Source) {

    val time: Date

    init {
        val format = SimpleDateFormat("ccc, dd MMM yyyy HH:mm:ss Z", Locale.ROOT)
        time = try { format.parse(timeString!!.trim().replace("GMT", "+0000"))!! } catch (e: Exception) { Date(0) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FeedItem
        if (title != other.title) return false
        if (link != other.link) return false
        return true
    }

    override fun hashCode() = 31 * title.hashCode() + link.hashCode()
}