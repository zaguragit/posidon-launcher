package posidon.launcher.feed.news

import android.os.AsyncTask
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import posidon.launcher.feed.news.chooser.FeedChooser
import posidon.launcher.feed.news.chooser.Source
import posidon.launcher.storage.Settings
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class FeedLoader(private val listener: Listener) : AsyncTask<Unit, Unit, Boolean>() {

    private val feedModels: ArrayList<FeedItem> = ArrayList()
    val deleted = Settings.getStrings("feed:deleted_articles")
    val pullParserFactory = XmlPullParserFactory.newInstance()

    companion object {
        private val endStrings = arrayOf("", "/feed", "/rss", "/feed.xml", "/rss.xml", "/atom", "/atom.xml")
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
        for (u in Settings["feedUrls", FeedChooser.defaultSources].split("|")) {
            if (u.isNotEmpty()) {
                val domain: String
                var url = if (!u.startsWith("http://") && !u.startsWith("https://")) {
                    val slashI = u.indexOf('/')
                    domain = "https://" + if (slashI != -1) u.substring(0, slashI) else u
                    "https://$u"
                } else {
                    val slashI = u.indexOf('/', 8)
                    domain = if (slashI != -1) u.substring(0, slashI) else u
                    u
                }
                if (url.endsWith("/")) url = url.substring(0, url.length - 1)
                var searching = true
                var i = 0
                while (searching && i < endStrings.size) {
                    try {
                        val newUrl = url + endStrings[i]
                        parseFeed(URL(newUrl).openConnection().getInputStream(), Source(urlToName(url), url, domain))
                        searching = false
                    }
                    catch (e: Exception) {}
                    i++
                }
            }
        }

        var i = 0
        var j: Int
        var temp: FeedItem

        while (i < feedModels.size - 1) {
            j = i + 1
            while (j < feedModels.size) {
                if (feedModels[i].time.before(feedModels[j].time)) {
                    temp = feedModels[i]
                    feedModels[i] = feedModels[j]
                    feedModels[j] = temp
                }
                j++
            }
            i++
        }

        return feedModels.size != 0
    }

    override fun onPostExecute(success: Boolean?) {
        if (success!!) listener.onFinished(feedModels)
    }

    interface Listener {
        fun onFinished(feedModels: ArrayList<FeedItem>)
    }

    fun urlToName(url: String): String {
        var out = when {
            url.startsWith("https://") -> url.substring(8)
            url.startsWith("http://") -> url.substring(7)
            else -> url
        }
        if (out.startsWith("www.")) out = out.substring(4)
        if (out.endsWith("/")) out = out.substring(0, out.length - 1)
        return out
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun parseFeed(inputStream: InputStream, source: Source) {
        var title: String? = null
        var link: String? = null
        var img: String? = null
        var date: String? = null
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
                                    for (string in deleted) if (string.substringAfter(':') == "$link:$title") {
                                        show = false; break
                                    }
                                    if (show) feedModels.add(FeedItem(title!!, link!!, img, date, source))
                                } else feedModels.add(FeedItem(title!!, link!!, img, date, source))
                            }
                            title = null
                            link = null
                            img = null
                            date = null
                        }
                    }
                    XmlPullParser.START_TAG -> when {
                        name.equals("item", ignoreCase = true) -> isItem = 1
                        name.equals("entry", ignoreCase = true) -> isItem = 2
                        isItem == 1 -> when { //RSS
                            name.equals("title", ignoreCase = true) -> title = getText(parser)
                            name.equals("link", ignoreCase = true) -> link = getText(parser)
                            name.equals("pubDate", ignoreCase = true) -> date = getText(parser)
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
                            name.equals("pubDate", ignoreCase = true) -> date = getText(parser)
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