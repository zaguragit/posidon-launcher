package posidon.launcher.feed.news.opml

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.Reader
import java.io.Writer
import java.util.*

object OPML {

    /**
     * Reads an Opml document and returns a list of all OPML elements it can
     * find
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Throws(XmlPullParserException::class, IOException::class)
    fun readDocument(reader: Reader): ArrayList<OpmlElement> {
        var isInOpml = false
        val elementList =  ArrayList<OpmlElement>()
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val xpp = factory.newPullParser()
        xpp.setInput(reader)
        var eventType = xpp.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (xpp.name == Symbols.OPML) {
                    isInOpml = true
                } else if (isInOpml && xpp.name == Symbols.OUTLINE) {
                    val title = xpp.getAttributeValue(null, Symbols.TITLE)
                    var text = title ?: xpp.getAttributeValue(null, Symbols.TEXT)
                    val xmlUrl = xpp.getAttributeValue(null, Symbols.XMLURL)
                    if (xmlUrl != null) {
                        if (text == null) {
                            text = xmlUrl
                        }
                        val element = OpmlElement(text, xmlUrl)
                        elementList.add(element)
                    }
                }
            }
            eventType = xpp.next()
        }
        return elementList
    }

    private const val ENCODING = "UTF-8"
    private const val OPML_VERSION = "2.0"

    /**
     * Takes a list of feeds and a writer and writes those into an OPML
     * document.
     *
     * @throws IOException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     */
    @Throws(IllegalArgumentException::class, IllegalStateException::class, IOException::class)
    fun writeDocument(feeds: List<OpmlElement>, writer: Writer) {
        val xs = Xml.newSerializer().apply {
            setOutput(writer)
            startDocument(ENCODING, false)
            startTag(null, Symbols.OPML)
            attribute(null, Symbols.VERSION, OPML_VERSION)
            startTag(null, Symbols.BODY)
        }
        for (feed in feeds) xs.run {
            startTag(null, Symbols.OUTLINE)
            attribute(null, Symbols.TEXT, feed.text)
            attribute(null, Symbols.TITLE, feed.text)
            attribute(null, Symbols.TYPE, "rss")
            attribute(null, Symbols.XMLURL, feed.xmlUrl)
            endTag(null, Symbols.OUTLINE)
        }
        xs.run {
            endTag(null, Symbols.BODY)
            endTag(null, Symbols.OPML)
            endDocument()
        }
    }

    object Symbols {
        const val OPML = "opml"
        const val BODY = "body"
        const val OUTLINE = "outline"
        const val TEXT = "text"
        const val XMLURL = "xmlUrl"
        const val TYPE = "type"
        const val VERSION = "version"
        const val HEAD = "head"
        const val TITLE = "title"
    }
}