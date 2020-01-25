package posidon.launcher.feed.news

import posidon.launcher.feed.news.chooser.Source
import java.text.SimpleDateFormat
import java.util.*

class FeedItem internal constructor(val title: String?, val link: String?, val img: String?, timeString: String?, val source: Source) {
    val time: Date
    init {
        val format = try {
            SimpleDateFormat("ccc, dd MMM yyyy HH:mm:ss Z")
        } catch (e: Exception) {
            SimpleDateFormat("ccc, dd MMM yyyy HH:mm:ss Z")
        }
        time = try { format.parse(timeString!!)!! } catch (e: Exception) { e.printStackTrace(); Date(0) }
        //Log.d("aaaaaaa", format.format(Calendar.getInstance().time))
    }
}