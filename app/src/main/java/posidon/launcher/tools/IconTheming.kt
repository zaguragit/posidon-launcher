package posidon.launcher.tools

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.*
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.min

object IconTheming {

    const val ICON_PACK_CATEGORY = "com.anddoes.launcher.THEME"

    inline fun getAvailableIconPacks(packageManager: PackageManager): MutableList<ResolveInfo> {
        return packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN)
                .addCategory(ICON_PACK_CATEGORY),
            0
        )
    }

    class IconPackInfo(
        val res: Resources,
        val iconPackPackageName: String,
    ) {
        val iconResourceNames = HashMap<String, String>()
        val calendarPrefixes = HashMap<String, String>()
        val iconModificationInfo = IconGenerationInfo(res)

        fun getDrawableResource(packageName: String, name: String): Int {
            val key = "ComponentInfo{$packageName/$name}"
            val iconResource = calendarPrefixes[key]
                ?.let { it + Calendar.getInstance()[Calendar.DAY_OF_MONTH] }
                ?: iconResourceNames[key]
                ?: return 0
            return res.getIdentifier(
                iconResource,
                "drawable",
                iconPackPackageName
            )
        }

        fun getDrawable(packageName: String, name: String, density: Int): Drawable? {
            val drawableRes = getDrawableResource(packageName, name)
            if (drawableRes == 0) return null
            return try {
                res.getDrawableForDensity(drawableRes, density, null)
            } catch (e: Resources.NotFoundException) {
                null
            }
        }
    }

    class IconGenerationInfo(
        val res: Resources,
    ) {
        var size = 0
            internal set

        var scaleFactor = 1f
            internal set

        var areUnthemedIconsChanged: Boolean = false
            internal set

        internal var back: Int = 0
        internal var mask: Int = 0
        internal var front: Int = 0

        fun getBackBitmap(
            uniformOptions: BitmapFactory.Options
        ): Bitmap? = BitmapFactory.decodeResource(res, back, uniformOptions)

        fun getMaskBitmap(
            uniformOptions: BitmapFactory.Options
        ): Bitmap? = BitmapFactory.decodeResource(res, mask, uniformOptions)

        fun getFrontBitmap(
            uniformOptions: BitmapFactory.Options
        ): Bitmap? = BitmapFactory.decodeResource(res, front, uniformOptions)
    }

    fun getIconPackInfo(
        res: Resources,
        iconPackPackageName: String,
    ): IconPackInfo {
        val info = IconPackInfo(res, iconPackPackageName)
        try {
            val n = res.getIdentifier("appfilter", "xml", iconPackPackageName)
            val x = if (n != 0) {
                res.getXml(n)
            } else {
                val factory = XmlPullParserFactory.newInstance()
                factory.isValidating = false
                val xpp = factory.newPullParser()
                val raw = res.assets.open("appfilter.xml")
                xpp.setInput(raw, null)
                xpp
            }
            while (x.eventType != XmlResourceParser.END_DOCUMENT) {
                if (x.eventType == 2) {
                    try {
                        when (x.name) {
                            "scale" -> {
                                info.iconModificationInfo.scaleFactor = x.getAttributeValue(0).toFloat()
                            }
                            "item" -> {
                                val key = x.getAttributeValue(null, "component")
                                val value = x.getAttributeValue(null, "drawable")
                                if (key != null) {
                                    if (value != null)
                                        info.iconResourceNames[key] = value
                                }
                            }
                            "calendar" -> {
                                val key = x.getAttributeValue(null, "component")
                                val value = x.getAttributeValue(null, "prefix")
                                if (key != null) {
                                    if (value != null)
                                        info.calendarPrefixes[key] = value
                                }
                            }
                            "iconback" -> info.iconModificationInfo.back = loadIconMod(
                                x.getAttributeValue(0),
                                res,
                                iconPackPackageName,
                                info
                            )
                            "iconmask" -> info.iconModificationInfo.mask = loadIconMod(
                                x.getAttributeValue(0),
                                res,
                                iconPackPackageName,
                                info
                            )
                            "iconupon" -> info.iconModificationInfo.front = loadIconMod(
                                x.getAttributeValue(0),
                                res,
                                iconPackPackageName,
                                info
                            )
                        }
                    } catch (e: Exception) {}
                }
                x.next()
            }
        } catch (e: Exception) { e.printStackTrace() }
        return info
    }

    private fun loadIconMod(name: String, res: Resources, iconPackPackageName: String, info: IconPackInfo): Int {
        val i = res.getIdentifier(
            name,
            "drawable",
            iconPackPackageName
        )
        if (i != 0) {
            info.iconModificationInfo.areUnthemedIconsChanged = true
        }
        return i
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