package posidon.launcher.feed.news

import android.os.AsyncTask
import android.text.TextUtils
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import posidon.launcher.feed.news.chooser.Source
import posidon.launcher.tools.Settings
import java.io.IOException
import java.io.InputStream
import java.net.URL

class FeedLoader(private val listener: Listener) : AsyncTask<Void, Void, Boolean>() {

    private val feedModels: ArrayList<FeedItem> = ArrayList()
    private val endStrings: Array<String> = arrayOf("", "feed", "rss", "feed.xml", "rss.xml", "atom", "atom.xml")

    override fun doInBackground(vararg voids: Void): Boolean? {
        for (u: String in Settings.getString("feedUrls", "androidpolice.com")!!.split("|")) {
            if (!TextUtils.isEmpty(u)) {
                var url = if (!u.startsWith("http://") && !u.startsWith("https://")) "https://$u" else u

                if (!url.endsWith("/")) url += "/"
                var searching = true
                var i = 0
                while (searching && i < endStrings.size) {
                    try {
                        val newUrl = url + endStrings[i]
                        parseFeed(URL(newUrl).openConnection().getInputStream(), Source(url, url))
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
        fun onFinished(feedModels: List<FeedItem>)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun parseFeed(inputStream: InputStream, source: Source) {
        var title: String? = null
        var link: String? = null
        var img: String? = null
        var date: String? = null
        var isItem = 0
        inputStream.use {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                val eventType = parser.eventType
                val name = parser.name ?: continue
                if (eventType == XmlPullParser.END_TAG) {
                    if (name.equals("item", ignoreCase = true) || name.equals("entry", ignoreCase = true)) {
                        isItem = 0
                        if (title != null && link != null)
                            feedModels.add(FeedItem(title, link, img, date, source))
                        title = null
                        link = null
                        img = null
                        date = null
                    }
                    continue
                }
                if (eventType == XmlPullParser.START_TAG) {
                    if (name.equals("item", ignoreCase = true)) {
                        isItem = 1
                        continue
                    } else if (name.equals("entry", ignoreCase = true)) {
                        isItem = 2
                        continue
                    }
                }
                var result = ""
                if (parser.next() == XmlPullParser.TEXT) {
                    result = parser.text
                    parser.nextTag()
                }
                when {
                    isItem == 1 -> when { //RSS
                        name.equals("title", ignoreCase = true) -> title = result
                        name.equals("link", ignoreCase = true) -> link = result
                        name.equals("pubDate", ignoreCase = true) -> date = result
                        img == null -> img = when (name) {
                            "description" -> {
                                if (result.contains("src=\"")) {
                                    val start = result.indexOf("src=\"", result.indexOf("img")) + 5
                                    val end = result.indexOf("\"", start)
                                    result.substring(start, end)
                                } else null
                            }
                            "media:content" -> parser.getAttributeValue(null, "url")
                            "image" -> result
                            "itunes:image" -> parser.getAttributeValue(null, "href")
                            "enclosure" -> parser.getAttributeValue(null, "url")
                            else -> null
                        }
                    }
                    isItem == 2 -> when { //Atom
                        name.equals("title", ignoreCase = true) -> title = result
                        name.equals("id", ignoreCase = true) -> link = result
                        name.equals("pubDate", ignoreCase = true) -> date = result
                        (name.equals("isSummary", ignoreCase = true) || name.equals("content", ignoreCase = true) && img == null) -> {
                            if (result.contains("src=\"")) {
                                val start = result.indexOf("src=\"", result.indexOf("img")) + 5
                                val end = result.indexOf("\"", start)
                                img = result.substring(start, end)
                            }
                        }
                    }
                    name.equals("title", ignoreCase = true) -> source.name = result
                }
            }
        }
    }
}