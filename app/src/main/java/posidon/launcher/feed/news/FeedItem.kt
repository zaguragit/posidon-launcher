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
}