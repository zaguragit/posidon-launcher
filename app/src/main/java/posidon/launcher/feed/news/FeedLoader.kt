package posidon.launcher.feed.news

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
import kotlin.math.max

object FeedLoader {

    val pullParserFactory = XmlPullParserFactory.newInstance()
    private val endStrings = arrayOf("",
        "/feed",
        "/rss",
        "/feed.xml",
        "/rss.xml",
        "/atom",
        "/atom.xml")

    fun loadFeed(
        onFinished: (success: Boolean, items: ArrayList<FeedItem>?) -> Unit
    ) = thread (isDaemon = true) {
        val feedItems = ArrayList<FeedItem>()
        val deleted = Settings.getStrings("feed:deleted_articles")
        val lock = ReentrantLock()

        val today = Calendar.getInstance()[Calendar.DAY_OF_YEAR]
        val deletedIter = deleted.iterator()
        for (article in deletedIter) {
            val day = article.substringBefore(':').toDouble()
            if (abs(day - today) > 4) {
                deletedIter.remove()
            }
        }
        Settings["feed:deleted_articles"] = deleted
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
                            parseFeed(connection.getInputStream(), Source(name, newUrl, domain), lock, feedItems, deleted)
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

        val m = System.currentTimeMillis()
        for (thread in threads) {
            val millis = System.currentTimeMillis() - m
            kotlin.runCatching {
                thread.join(max(60000 - millis, 0))
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

        onFinished(feedItems.size != 0, feedItems)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private inline fun parseFeed(inputStream: InputStream, source: Source, lock: ReentrantLock, feedItems: ArrayList<FeedItem>, deleted: java.util.ArrayList<String>) {
        var title: String? = null
        var link: String? = null
        var img: String? = null
        var time: Date? = null
        var isItem = 0
        val items = ArrayList<FeedItem>()
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
                                        items.add(FeedItem(title!!, link!!, img, time!!, source))
                                    }
                                } else {
                                    items.add(FeedItem(title!!, link!!, img, time!!, source))
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
                                        .replace("GMT", "+0000")
                                        .replace(Regex("[-:, /]"), "")
                                time = try {
                                    SimpleDateFormat("cccddMMMyyyyHHmmssZ", Locale.ROOT).parse(text)!!
                                } catch (e: Exception) {
                                    try { SimpleDateFormat("yyyyMMdd'T'HHmmssZ", Locale.ROOT).parse(text)!! }
                                    catch (e: Exception) { Date(0) }
                                }
                            }
                            img == null -> when (name) {
                                "description", "content:encoded" -> {
                                    val result = getText(parser)
                                    val i = result.indexOf("src=\"", result.indexOf("img"))
                                    if (i != -1) {
                                        val end = result.indexOf("\"", i + 5)
                                        img = result.substring(i + 5, end)
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
                                    name.equals("updated", ignoreCase = true) -> {
                                val text = getText(parser).trim()
                                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT)
                                time = try { format.parse(text)!! } catch (e: Exception) { Date(0) }
                            }
                            (name.equals("isSummary", ignoreCase = true) || name.equals("content", ignoreCase = true)) && img == null -> {
                                val result = getText(parser)
                                val i = result.indexOf("src=\"", result.indexOf("img"))
                                if (i != -1) {
                                    val end = result.indexOf("\"", i + 5)
                                    img = result.substring(i + 5, end)
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
        lock.lock()
        feedItems.addAll(items)
        lock.unlock()
    }

    private fun getText(parser: XmlPullParser): String {
        return if (parser.next() == XmlPullParser.TEXT) {
            parser.text.also { parser.nextTag() }
        } else ""
    }
}