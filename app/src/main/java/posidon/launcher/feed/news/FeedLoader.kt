package posidon.launcher.feed.news

import android.os.AsyncTask
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import posidon.launcher.feed.news.chooser.FeedChooser
import posidon.launcher.feed.news.chooser.Source
import posidon.launcher.storage.Settings
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import kotlin.math.abs

class FeedLoader(
    private val onFinished: (success: Boolean, items: ArrayList<FeedItem>?) -> Unit
) : AsyncTask<Unit, Unit, Boolean>() {

    private val feedItems = ArrayList<FeedItem>()
    private val deleted = Settings.getStrings("feed:deleted_articles")
    private val pullParserFactory = XmlPullParserFactory.newInstance()

    companion object {
        private val endStrings = arrayOf("",
            "/feed",
            "/rss",
            "/feed.xml",
            "/rss.xml",
            "/atom",
            "/atom.xml")
    }

    override fun doInBackground(vararg Units: Unit): Boolean? {
        val today = Calendar.getInstance()[Calendar.DAY_OF_YEAR]
        val deletedIter = deleted.iterator()
        for (article in deletedIter) {
            val day = article.substringBefore(':').toDouble()
            if (abs(day - today) > 4) {
                deletedIter.remove()
            }
        }
        Settings.apply()
        val threads = LinkedList<Thread>()
        for (u in Settings["feedUrls", FeedChooser.defaultSources].split("|")) {
            if (u.isNotEmpty()) {
                threads.add(thread (isDaemon = true) {
                    var (url, domain, name) = if (!u.startsWith("http://") && !u.startsWith("https://")) {
                        val slashI = u.indexOf('/')
                        val domain = if (slashI != -1) u.substring(0, slashI) else u
                        Triple("https://$u", "https://$domain", if (domain.startsWith("www.")) {
                            domain.substring(4)
                        } else domain)
                    } else {
                        val slashI = u.indexOf('/', 8)
                        val domain = if (slashI != -1) u.substring(0, slashI) else u
                        Triple(u, domain, if (domain.startsWith("www.")) {
                            domain.substring(4)
                        } else domain)
                    }
                    if (url.endsWith("/")) {
                        url = url.substring(0, url.length - 1)
                    }

                    var i = 0
                    while (i < endStrings.size) {
                        try {
                            val newUrl = url + endStrings[i]
                            val connection = URL(newUrl).openConnection()
                            connection.connectTimeout = 15 * 1000
                            connection.readTimeout = 15 * 1000
                            connection.connect()
                            parseFeed(connection.getInputStream(), Source(name, newUrl, domain))
                            break
                        } catch (e: Exception) {}
                        i++
                    }
                })
            }
        }

        var i = 0
        var j: Int
        var temp: FeedItem

        for (thread in threads) {
            kotlin.runCatching {
                thread.join(60000)
            }
        }

        while (i < feedItems.size - 1) {
            j = i + 1
            while (j < feedItems.size) {
                if (feedItems[i].isBefore(feedItems[j])) {
                    temp = feedItems[i]
                    feedItems[i] = feedItems[j]
                    feedItems[j] = temp
                }
                j++
            }
            i++
        }

        return feedItems.size != 0
    }

    override fun onPostExecute(success: Boolean) = onFinished(success, feedItems)

    private val lock = ReentrantLock()

    @Throws(XmlPullParserException::class, IOException::class)
    private fun parseFeed(inputStream: InputStream, source: Source) {
        var title: String? = null
        var link: String? = null
        var img: String? = null
        var time: Date? = null
        var isItem = 0
        inputStream.use {
            val parser: XmlPullParser = pullParserFactory.newPullParser()
            parser.setInput(inputStream, null)
            parser.nextTag()
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                val name = parser.name ?: continue
                when (parser.eventType) {
                    XmlPullParser.END_TAG -> when {
                        name.equals("item", ignoreCase = true) ||
                        name.equals("entry", ignoreCase = true) -> {
                            isItem = 0
                            if (title != null && link != null) {
                                if (Settings["feed:delete_articles", false]) {
                                    var show = true
                                    for (string in deleted) {
                                        if (string.substringAfter(':') == "$link:$title") {
                                            show = false; break
                                        }
                                    }
                                    if (show) {
                                        lock.lock()
                                        feedItems.add(FeedItem(title!!, link!!, img, time!!, source))
                                        lock.unlock()
                                    }
                                } else {
                                    lock.lock()
                                    feedItems.add(FeedItem(title!!, link!!, img, time!!, source))
                                    lock.unlock()
                                }
                            }
                            title = null
                            link = null
                            img = null
                            time = null
                        }
                    }
                    XmlPullParser.START_TAG -> when {
                        name.equals("item", ignoreCase = true) -> isItem = 1
                        name.equals("entry", ignoreCase = true) -> isItem = 2
                        isItem == 1 -> when { //RSS
                            name.equals("title", ignoreCase = true) -> title = getText(parser)
                            name.equals("link", ignoreCase = true) -> link = getText(parser)
                            name.equals("pubDate", ignoreCase = true) -> {
                                val text = getText(parser).trim()
                                val format = SimpleDateFormat("ccc, dd MMM yyyy HH:mm:ss Z", Locale.ROOT)
                                time = try { format.parse(text.replace("GMT", "+0000"))!! } catch (e: Exception) { Date(0) }
                            }
                            img == null -> when (name) {
                                "description", "content:encoded" -> {
                                    val result = getText(parser)
                                    if (result.contains("src=\"")) {
                                        val start = result.indexOf("src=\"", result.indexOf("img")) + 5
                                        val end = result.indexOf("\"", start)
                                        img = result.substring(start, end)
                                    }
                                }
                                "image" -> img = getText(parser)
                                "media:content" -> {
                                    val medium = parser.getAttributeValue(null, "medium")
                                    val url = parser.getAttributeValue(null, "url")
                                    if (medium == "image" ||
                                        url.endsWith(".jpg") ||
                                        url.endsWith(".png") ||
                                        url.endsWith(".svg") ||
                                        url.endsWith(".jpeg")) {
                                        img = url
                                    }
                                }
                                "media:thumbnail", "enclosure" -> img = parser.getAttributeValue(null, "url")
                                "itunes:image" -> img = parser.getAttributeValue(null, "href")
                            }
                        }
                        isItem == 2 -> when { //Atom
                            name.equals("title", ignoreCase = true) -> title = getText(parser)
                            name.equals("id", ignoreCase = true) -> link = getText(parser)
                            name.equals("published", ignoreCase = true) ||
                            name.equals("updated", ignoreCase = true)-> {
                                val text = getText(parser).trim()
                                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT)
                                time = try { format.parse(text)!! } catch (e: Exception) { Date(0) }
                            }
                            (name.equals("isSummary", ignoreCase = true) || name.equals("content", ignoreCase = true)) && img == null -> {
                                val result = getText(parser)
                                if (result.contains("src=\"")) {
                                    val start = result.indexOf("src=\"", result.indexOf("img")) + 5
                                    val end = result.indexOf("\"", start)
                                    img = result.substring(start, end)
                                }
                            }
                        }
                        name.equals("title", ignoreCase = true) -> {
                            val new = getText(parser)
                            if (new.isNotBlank()) {
                                source.name = new
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getText(parser: XmlPullParser): String {
        return if (parser.next() == XmlPullParser.TEXT) {
            parser.text.also { parser.nextTag() }
        } else ""
    }
}