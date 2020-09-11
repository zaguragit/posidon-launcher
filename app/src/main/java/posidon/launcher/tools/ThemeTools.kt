package posidon.launcher.tools

import android.app.WallpaperManager
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.*
import android.graphics.drawable.*
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.palette.graphics.Palette
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.drawable.MaskedDrawable
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object ThemeTools {

    class IconPackInfo {
        var scaleFactor = 1f
        val iconResourceNames = HashMap<String, String>()
        var iconBack: String? = null
        var iconMask: String? = null
        var iconFront: String? = null
    }

    fun getIconPackInfo(res: Resources, iconPackName: String): IconPackInfo {
        val info = IconPackInfo()
        try {
            val n = res.getIdentifier("appfilter", "xml", iconPackName)
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

    fun setWallpaper(
        img: Bitmap,
        flag: Int
    ) = thread {
        val wallpaperManager = WallpaperManager.getInstance(Tools.publicContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                wallpaperManager.setBitmap(img, null, true, when (flag) {
                    0 -> WallpaperManager.FLAG_SYSTEM
                    1 -> WallpaperManager.FLAG_LOCK
                    else -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                })
            } catch (e: Exception) {}
        } else {
            try { wallpaperManager.setBitmap(img) }
            catch (e: Exception) {}
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    fun generateAdaptiveIcon(drawable: Drawable): Drawable {
        var containsAnimatable = drawable is Animatable
        val d: Drawable = if (drawable is AdaptiveIconDrawable || Settings["reshapeicons", false]) {
            val layerDrawable = if (drawable is AdaptiveIconDrawable) {
                val drr = arrayOf(drawable.background ?: ColorDrawable(0), drawable.foreground ?: ColorDrawable(0))
                if (drr[0] is Animatable || drr[1] is Animatable) {
                    containsAnimatable = true
                }
                if (Settings["icon:tint_white_bg", true]) {
                    val bg = drr[0]
                    if ((bg is ColorDrawable && bg.color == 0xffffffff.toInt()) ||
                            (Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply {
                                val tmp = bg.bounds
                                bg.bounds = Rect(0, 0, 1, 1)
                                bg.draw(Canvas(this))
                                bg.bounds = tmp
                            }.getPixel(0, 0) and 0xffffff).let {
                                ColorTools.red(it) > 0xdd &&
                                        ColorTools.green(it) > 0xdd &&
                                        ColorTools.blue(it) > 0xdd
                            }) {
                        val bgColor = Settings["icon:background", 0xff252627.toInt()]
                        drr[0] = when (Settings["icon:background_type", "custom"]) {
                            "dominant" -> ColorDrawable(Palette.from(drr[1].toBitmap()).generate().getDominantColor(bgColor))
                            "lv" -> ColorDrawable(Palette.from(drr[1].toBitmap()).generate().getLightVibrantColor(bgColor))
                            "dv" -> ColorDrawable(Palette.from(drr[1].toBitmap()).generate().getDarkVibrantColor(bgColor))
                            "lm" -> ColorDrawable(Palette.from(drr[1].toBitmap()).generate().getLightMutedColor(bgColor))
                            "dm" -> ColorDrawable(Palette.from(drr[1].toBitmap()).generate().getDarkMutedColor(bgColor))
                            else -> ColorDrawable(bgColor)
                        }
                    }
                }
                val tmp = LayerDrawable(drr)
                val w = tmp.intrinsicWidth
                val h = tmp.intrinsicHeight
                tmp.setLayerInset(0, -w / 6, -h / 6, -w / 6, -h / 6)
                tmp.setLayerInset(1, -w / 6, -h / 6, -w / 6, -h / 6)
                tmp
            } else {
                val w = drawable.intrinsicWidth
                val h = drawable.intrinsicHeight
                val bgColor = Settings["icon:background", 0xff252627.toInt()]
                val tmp = LayerDrawable(arrayOf(when (Settings["icon:background_type", "custom"]) {
                    "dominant" -> ColorDrawable(Palette.from(drawable.toBitmap()).generate().getDominantColor(bgColor))
                    "lv" -> ColorDrawable(Palette.from(drawable.toBitmap()).generate().getLightVibrantColor(bgColor))
                    "dv" -> ColorDrawable(Palette.from(drawable.toBitmap()).generate().getDarkVibrantColor(bgColor))
                    "lm" -> ColorDrawable(Palette.from(drawable.toBitmap()).generate().getLightMutedColor(bgColor))
                    "dm" -> ColorDrawable(Palette.from(drawable.toBitmap()).generate().getDarkMutedColor(bgColor))
                    else -> ColorDrawable(bgColor)
                }, drawable))
                tmp.setLayerInset(1, w / 4, h / 4, w / 4, h / 4)
                tmp
            }
            val width = layerDrawable.intrinsicWidth
            val height = layerDrawable.intrinsicHeight
            layerDrawable.setBounds(0, 0, width, height)

            val icShape = Settings["icshape", 4]
            if (icShape == 3) {
                layerDrawable
            } else {
                MaskedDrawable(layerDrawable, getAdaptiveIconPath(icShape, width, height))
            }
        } else drawable
        return if (containsAnimatable) d else {
            BitmapDrawable(Tools.publicContext!!.resources, d.toBitmap())
        }
    }

    fun getAdaptiveIconPath(icShape: Int, width: Int, height: Int): Path {
        val minSize = min(width, height)
        if (icShape == 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val path = AdaptiveIconDrawable(null, null).iconMask
            val rect = RectF()
            path.computeBounds(rect, true)
            val matrix = Matrix()
            matrix.setScale(minSize / rect.right, minSize / rect.bottom)
            path.transform(matrix)
            path.fillType = Path.FillType.INVERSE_EVEN_ODD
            return path
        } else {
            val path = Path()
            when (icShape) {
                1 -> path.addCircle(width / 2f, height / 2f, minSize / 2f - 2, Path.Direction.CCW)
                2 -> path.addRoundRect(2f, 2f, width - 2f, height - 2f, minSize / 4f, minSize / 4f, Path.Direction.CCW)
                4 -> { //Formula: (|x|)^3 + (|y|)^3 = radius^3
                    val xx = 2
                    val yy = 2
                    val radius = minSize / 2 - 2
                    val radiusToPow = radius * radius * radius.toDouble()
                    path.moveTo(-radius.toFloat(), 0f)
                    for (x in -radius..radius) {
                        path.lineTo(x.toFloat(), Math.cbrt(radiusToPow - abs(x * x * x)).toFloat())
                    }
                    for (x in radius downTo -radius) {
                        path.lineTo(x.toFloat(), (-Math.cbrt(radiusToPow - abs(x * x * x))).toFloat())
                    }
                    path.close()
                    val matrix = Matrix()
                    matrix.postTranslate(xx + radius.toFloat(), yy + radius.toFloat())
                    path.transform(matrix)
                }
            }
            path.fillType = Path.FillType.INVERSE_EVEN_ODD
            return path
        }
    }

    fun badgeMaybe(icon: Drawable, isWork: Boolean): Drawable {
        val drawable = if (isWork) {
            val badge = Tools.publicContext!!.resources.getDrawable(R.drawable.work_badge, Tools.publicContext!!.theme)
            badge.setTint(Global.accentColor)
            badge.setTintMode(PorterDuff.Mode.MULTIPLY)
            badge(icon, badge, when (Settings["icsize", 1]) {
                0 -> 64; 2 -> 84; else -> 74
            })
        } else LayerDrawable(arrayOf(icon)).apply {
            val diameter = max(intrinsicWidth, intrinsicHeight)
            val p = 8 * diameter / when (Settings["icsize", 1]) {
                0 -> 64; 2 -> 84; else -> 74
            }
            setLayerInset(0, p, p, p, p)
        }
        return if (icon is BitmapDrawable) {
            BitmapDrawable(Tools.publicContext!!.resources, drawable.toBitmap())
        } else drawable
    }

    fun badge(icon: Drawable, badge: Drawable, icSizeDP: Int): Drawable {
        val drawable = LayerDrawable(arrayOf(icon, badge))
        val diameter = max(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val p = 8 * diameter / icSizeDP
        drawable.setLayerInset(0, p, p, p, p)
        val o = diameter - (20.sp * diameter / icSizeDP.dp).toInt()
        drawable.setLayerInset(1, o, o, 0, 0)
        return if (icon is BitmapDrawable) {
            BitmapDrawable(Tools.publicContext!!.resources, drawable.toBitmap())
        } else drawable
    }

    private val pics = HashMap<Int, Drawable>()
    fun generateContactPicture(name: String): Drawable = pics[(name[0].toInt() shl 16) + name[1].toInt()] ?: let {
        val bitmap = Bitmap.createBitmap(108, 108, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val random = Random((name[0].toInt() shl 16) + name[1].toInt())
        canvas.drawColor(Color.HSVToColor(180, floatArrayOf(
                random.nextFloat() * 360,
                (random.nextInt(4000) + 5000) / 10000f,
                (random.nextInt(3000) + 5000) / 10000f
        )))
        val textP = Paint().apply {
            color = 0xffffffff.toInt()
            textAlign = Paint.Align.CENTER
            typeface = Tools.publicContext!!.mainFont
            textSize = 64f
            isAntiAlias = true
        }
        val x = canvas.width / 2f
        val y = (canvas.height / 2f - (textP.descent() + textP.ascent()) / 2f)
        canvas.drawText(name[0].toString(), x, y, textP)
        val icShape = Settings["icshape", 4]
        if (icShape != 3) {
            canvas.drawPath(getAdaptiveIconPath(icShape, 108, 108), Paint().apply {
                isAntiAlias = true
                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            })
        }
        badgeMaybe(BitmapDrawable(Tools.publicContext!!.resources, bitmap), false)
    }

    fun generateNotificationBadgeBGnFG(icon: Drawable? = null, onGenerated: (bg: Drawable, fg: Int) -> Unit) {
        val bgType = Settings["notif:badges:bg_type", 0]
        val customBG = Settings["notif:badges:bg_color", 0xffff5555.toInt()]
        if (icon == null || bgType == 1) {
            val bg = Global.accentColor
            onGenerated(ColorTools.iconBadge(bg), if (ColorTools.useDarkText(bg)) 0xff111213.toInt() else 0xffffffff.toInt())
        } else if (bgType == 0) {
            Palette.from(icon.toBitmap()).generate {
                val bg = it?.getDominantColor(customBG) ?: customBG
                onGenerated(ColorTools.iconBadge(bg), if (ColorTools.useDarkText(bg)) 0xff111213.toInt() else 0xffffffff.toInt())
            }
        } else {
            onGenerated(ColorTools.iconBadge(customBG), if (ColorTools.useDarkText(customBG)) 0xff111213.toInt() else 0xffffffff.toInt())
        }
    }
}