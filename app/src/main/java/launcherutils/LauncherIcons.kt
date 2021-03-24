package launcherutils

import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import androidx.annotation.RequiresApi
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.min

object LauncherIcons {

    class IconPackInfo {
        var scaleFactor = 1f
        val iconResourceNames = HashMap<String, String>()
        var iconBack: String? = null
        var iconMask: String? = null
        var iconFront: String? = null
    }

    fun getIconPackInfo(res: Resources, iconPackPackageName: String): IconPackInfo {
        val info = IconPackInfo()
        try {
            val n = res.getIdentifier("appfilter", "xml", iconPackPackageName)
            if (n != 0) {
                val xrp = res.getXml(n)
                while (xrp.eventType != XmlResourceParser.END_DOCUMENT) {
                    if (xrp.eventType == 2) {
                        try {
                            when (xrp.name) {
                                "scale" -> {
                                    info.scaleFactor = xrp.getAttributeValue(0).toFloat()
                                }
                                "item" -> {
                                    info.iconResourceNames[xrp.getAttributeValue(0)] = xrp.getAttributeValue(1)
                                }
                                "iconback" -> info.iconBack = xrp.getAttributeValue(0)
                                "iconmask" -> info.iconMask = xrp.getAttributeValue(0)
                                "iconupon" -> info.iconFront = xrp.getAttributeValue(0)
                            }
                        } catch (e: Exception) {}
                    }
                    xrp.next()
                }
            } else {
                val factory = XmlPullParserFactory.newInstance()
                factory.isValidating = false
                val xpp = factory.newPullParser()
                val raw = res.assets.open("appfilter.xml")
                xpp.setInput(raw, null)
                while (xpp.eventType != XmlPullParser.END_DOCUMENT) {
                    if (xpp.eventType == 2) {
                        try {
                            when (xpp.name) {
                                "scale" -> {
                                    info.scaleFactor = xpp.getAttributeValue(0).toFloat()
                                }
                                "item" -> {
                                    info.iconResourceNames[xpp.getAttributeValue(0)] = xpp.getAttributeValue(1)
                                }
                                "iconback" -> info.iconBack = xpp.getAttributeValue(0)
                                "iconmask" -> info.iconMask = xpp.getAttributeValue(0)
                                "iconupon" -> info.iconFront = xpp.getAttributeValue(0)
                            }
                        } catch (e: Exception) {}
                    }
                    xpp.next()
                }
            }
        } catch (ignore: Exception) {}
        return info
    }

    fun getResourceNames(res: Resources, iconPack: String?): ArrayList<String> {
        val strings = ArrayList<String>()
        try {
            val n = res.getIdentifier("drawable", "xml", iconPack)
            if (n != 0) {
                val xrp = res.getXml(n)
                while (xrp.eventType != XmlResourceParser.END_DOCUMENT) {
                    try {
                        if (xrp.eventType == 2 && !strings.contains(xrp.getAttributeValue(0))) {
                            if (xrp.name == "item") {
                                strings.add(xrp.getAttributeValue(0))
                            }
                        }
                    } catch (ignore: Exception) {}
                    xrp.next()
                }
            } else {
                val factory = XmlPullParserFactory.newInstance()
                factory.isValidating = false
                val xpp = factory.newPullParser()
                val raw = res.assets.open("drawable.xml")
                xpp.setInput(raw, null)
                while (xpp!!.eventType != XmlPullParser.END_DOCUMENT) {
                    try {
                        if (xpp.eventType == 2 && !strings.contains(xpp.getAttributeValue(0))) {
                            if (xpp.name == "item") {
                                strings.add(xpp.getAttributeValue(0))
                            }
                        }
                    } catch (ignore: Exception) {}
                    xpp.next()
                }
            }
        } catch (ignore: Exception) {}
        return strings
    }

    @RequiresApi(Build.VERSION_CODES.O)
    inline fun getSystemAdaptiveIconPath(width: Int, height: Int): Path {
        val minSize = min(width, height)
        val path = AdaptiveIconDrawable(null, null).iconMask
        val rect = RectF()
        path.computeBounds(rect, true)
        val matrix = Matrix()
        matrix.setScale(minSize / rect.right, minSize / rect.bottom)
        path.transform(matrix)
        path.fillType = Path.FillType.INVERSE_EVEN_ODD
        return path
    }
}