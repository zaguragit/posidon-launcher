package posidon.launcher.tools

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.*
import android.graphics.drawable.shapes.RoundRectShape
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.storage.Settings
import java.lang.ref.WeakReference
import kotlin.math.*
import kotlin.random.Random

object Tools {

	fun getResizedBitmap(bm: Bitmap, newHeight: Int, newWidth: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true)
    }

	fun getResizedMatrix(bm: Bitmap, newHeight: Int, newWidth: Int): Matrix {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return matrix
    }

	fun tryAnimate(d: Drawable): Drawable {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && d is Animatable2 -> {
                d.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable) = Main.instance.runOnUiThread { d.start() }
                })
                d.start()
            }
            d is Animatable2Compat -> {
                d.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable) = Main.instance.runOnUiThread { d.start() }
                })
                d.start()
            }
            d is AnimationDrawable -> d.start()
            d is LayerDrawable -> {
                for (i in 0 until d.numberOfLayers) {
                    tryAnimate(d.getDrawable(i))
                }
            }
            d is MaskedDrawable -> tryAnimate(d.drawable)
        }
        return d
    }

	fun clearAnimation(d: Drawable?) { when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && d is Animatable2 -> d.clearAnimationCallbacks()
        d is Animatable2Compat -> d.clearAnimationCallbacks()
        d is Animatable -> d.stop()
        d is LayerDrawable -> {
            for (i in 0 until d.numberOfLayers) {
                clearAnimation(d.getDrawable(i))
            }
        }
        d is MaskedDrawable -> clearAnimation(d.drawable)
    }}

	inline fun isInstalled(packageName: String, packageManager: PackageManager): Boolean {
        var found = true
        try { packageManager.getPackageInfo(packageName, 0) }
        catch (e: Exception) { found = false }
        return found
    }

    var publicContextReference = WeakReference<Context>(null)
    inline val publicContext get() = publicContextReference.get()

    fun fastBlur(bitmap: Bitmap, radius: Int): Bitmap {
        if (radius < 1) {
            return bitmap
        }
        val initWidth = bitmap.width
        val initHeight = bitmap.height
        val d = radius.toFloat()
        val w = (initWidth / d).roundToInt()
        val h = (initHeight / d).roundToInt()
        var bitmap = Bitmap.createScaledBitmap(bitmap, w, h, false)
        bitmap = bitmap.copy(bitmap.config, true)
        val pix = IntArray(w * h)
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)
        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1
        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int
        val vmin = IntArray(max(w, h))
        var divsum = div + 1 shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        i = 0
        while (i < 256 * divsum) {
            dv[i] = i / divsum
            i++
        }
        yi = 0
        yw = yi
        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int
        y = 0
        while (y < h) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            i = -radius
            while (i <= radius) {
                p = pix[yi + min(wm, max(i, 0))]
                sir = stack[i + radius]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 - abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                i++
            }
            stackpointer = radius
            x = 0
            while (x < w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                if (y == 0) vmin[x] = min(x + radius + 1, wm)
                p = pix[yw + vmin[x]]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                yi++
                x++
            }
            yw += w
            y++
        }
        x = 0
        while (x < w) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = max(0, yp) + x
                sir = stack[i + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]
                rbs = r1 - abs(i)
                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                if (i < hm) yp += w
                i++
            }
            yi = x
            stackpointer = radius
            y = 0
            while (y < h) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                if (x == 0) vmin[y] = min(y + r1, hm) * w
                p = x + vmin[y]
                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                yi += w
                y++
            }
            x++
        }
        bitmap.setPixels(pix, 0, w, 0, 0, w, h)
        return Bitmap.createScaledBitmap(bitmap, initWidth, initHeight, true)
    }

    inline val canBlurDrawer get() = Settings["drawer:blur", true] && ContextCompat.checkSelfPermission(publicContext!!, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    inline val canBlurSearch get() = Settings["search:blur", true] && ContextCompat.checkSelfPermission(publicContext!!, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    //inline fun canBlurOverview(context: Context?) = Settings["drawer:blur", true] && ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

	fun blurredWall(radius: Float): Bitmap? {
        try {
            @SuppressLint("MissingPermission") var bitmap: Bitmap = WallpaperManager.getInstance(publicContext).peekFastDrawable().toBitmap()
            val displayWidth = Device.displayWidth
            val displayHeight = Device.displayHeight + navbarHeight
            when {
                bitmap.height / bitmap.width.toFloat() < displayHeight / displayWidth.toFloat() -> {
                    bitmap = Bitmap.createScaledBitmap(
                        bitmap,
                        displayHeight * bitmap.width / bitmap.height,
                        displayHeight,
                        false)
                    bitmap = Bitmap.createBitmap(
                        bitmap, 0, 0,
                        displayWidth,
                        displayHeight)
                }
                bitmap.height / bitmap.width.toFloat() > displayHeight / displayWidth.toFloat() -> {
                    bitmap = Bitmap.createScaledBitmap(
                        bitmap,
                        displayWidth,
                        displayWidth * bitmap.height / bitmap.width,
                        false)
                    bitmap = Bitmap.createBitmap(
                        bitmap, 0, bitmap.height - displayHeight shr 1,
                        displayWidth,
                        displayHeight)
                }
                else -> bitmap = Bitmap.createScaledBitmap(bitmap, displayWidth, displayHeight, false)
            }
            if (radius > 0) try { bitmap = fastBlur(bitmap, radius.toInt()) }
            catch (e: Exception) { e.printStackTrace() }
            return bitmap
        } catch (e: OutOfMemoryError) {
            Toast.makeText(publicContext, "OutOfMemoryError: Couldn't blur wallpaper!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) { e.printStackTrace() }
        return null
    }

	fun centerCropWallpaper(wallpaper: Bitmap): Bitmap {
        val scaledWidth = Device.displayHeight * wallpaper.width / wallpaper.height
        var scaledWallpaper = Bitmap.createScaledBitmap(
            wallpaper,
            scaledWidth,
            Device.displayHeight,
            false)
        scaledWallpaper = Bitmap.createBitmap(
            scaledWallpaper,
            scaledWidth - Device.displayWidth shr 1,
            0,
            Device.displayWidth,
            Device.displayHeight)
        return scaledWallpaper
    }

	var navbarHeight = 0

	fun updateNavbarHeight(activity: Activity) {
        if (Settings["ignore_navbar", false]) {
            navbarHeight = 0
            return
        }
        val metrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metrics)
        val usableHeight = metrics.heightPixels
        activity.windowManager.defaultDisplay.getRealMetrics(metrics)
        val realHeight = metrics.heightPixels
        navbarHeight = if (realHeight > usableHeight) realHeight - usableHeight else 0
        val resources = activity.resources
        val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        navbarHeight = min(if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0, navbarHeight)
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
            BitmapDrawable(publicContext!!.resources, d.toBitmap())
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

    inline val isDefaultLauncher: Boolean get() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        return publicContext!!.packageManager.resolveActivity(intent, 0)?.resolvePackageName == "posidon.launcher"
    }

    inline fun springInterpolate(x: Float) = 1 + (2f.pow(-10f * x) * sin(2 * PI * (x - 0.065f)) / 0.4).toFloat()

    fun badgeMaybe(icon: Drawable, isWork: Boolean): Drawable {
        val drawable = if (isWork) {
            val badge = publicContext!!.resources.getDrawable(R.drawable.work_badge, publicContext!!.theme)
            badge.setTint(Main.accentColor)
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
            BitmapDrawable(publicContext!!.resources, drawable.toBitmap())
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
            BitmapDrawable(publicContext!!.resources, drawable.toBitmap())
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
            typeface = publicContext!!.mainFont
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
        badgeMaybe(BitmapDrawable(publicContext!!.resources, bitmap), false)
    }

    private var uidCounter = -1
    fun generateFolderUid(): String {
        if (uidCounter == -1) {
            uidCounter = Settings["folder:uids:count", 0]
        }
        val str = uidCounter++.toString(16).padStart(8, '_')
        Settings["folder:uids:count"] = uidCounter
        return str
    }

    fun searchOptimize(s: String) = s.toLowerCase()
            .replace('ñ', 'n')
            .replace('e', '3')
            .replace('a', '4')
            .replace('i', '1')
            .replace('¿', '?')
            .replace('¡', '!')
            .replace("wh", "w")
            .replace(Regex("(k|cc|ck)"), "c")
            .replace(Regex("(z|ts|sc|cs|tz)"), "s")
            .replace(Regex("([-'&/_,.:;*\"]|gh)"), "")

    fun setDockBG(drawer: View, realDock: View, dockContainerContainer: View) {
        when (Settings["dock:background_type", 0]) {
            0 -> { drawer.background = ShapeDrawable().apply {
                val tr = Settings["dockradius", 30].dp
                shape = RoundRectShape(floatArrayOf(tr, tr, tr, tr, 0f, 0f, 0f, 0f), null, null)
                paint.color = Settings["dock:background_color", -0x78000000]
            }}
            1 -> { drawer.background = LayerDrawable(arrayOf(
                    GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(
                            Settings["dock:background_color", -0x78000000] and 0x00ffffff,
                            Settings["dock:background_color", -0x78000000])),
                    GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(
                            Settings["dock:background_color", -0x78000000],
                            Settings["drawer:background_color", -0x78000000]))
            ))}
            2 -> {
                drawer.background = ShapeDrawable().apply {
                    val tr = Settings["dockradius", 30].dp
                    shape = RoundRectShape(floatArrayOf(tr, tr, tr, tr, 0f, 0f, 0f, 0f), null, null)
                    paint.color = 0
                }
                dockContainerContainer.background = null
                realDock.background = ShapeDrawable().apply {
                    val r = Settings["dockradius", 30].dp
                    shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
                    paint.color = Settings["dock:background_color", -0x78000000]
                }
            }
            3 -> {
                drawer.background = ShapeDrawable().apply {
                    val tr = Settings["dockradius", 30].dp
                    shape = RoundRectShape(floatArrayOf(tr, tr, tr, tr, 0f, 0f, 0f, 0f), null, null)
                    paint.color = 0
                }
                realDock.background = null
                dockContainerContainer.background = ShapeDrawable().apply {
                    val r = Settings["dockradius", 30].dp
                    shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
                    paint.color = Settings["dock:background_color", -0x78000000]
                }
            }
        }
    }
}

inline fun Activity.applyFontSetting() {
    when (Settings["font", "lexendDeca"]) {
        "sansserif" -> theme.applyStyle(R.style.font_sans_serif, true)
        "posidonsans" -> theme.applyStyle(R.style.font_posidon_sans, true)
        "monospace" -> theme.applyStyle(R.style.font_monospace, true)
        "ubuntu" -> theme.applyStyle(R.style.font_ubuntu, true)
        "lexendDeca" -> theme.applyStyle(R.style.font_lexend_deca, true)
        "inter" -> theme.applyStyle(R.style.font_inter, true)
        "openDyslexic" -> theme.applyStyle(R.style.font_open_dyslexic, true)
    }
}

inline fun Drawable.toBitmap(duplicateIfBitmapDrawable: Boolean = false): Bitmap {
    if (this is BitmapDrawable && bitmap != null) {
        return if (duplicateIfBitmapDrawable) {
            Bitmap.createBitmap(bitmap)
        } else bitmap
    }
    val bitmap: Bitmap = if (intrinsicWidth <= 0 || intrinsicHeight <= 0) Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    else Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    try { draw(canvas) }
    catch (e: Exception) {}
    return bitmap
}

inline fun Drawable.toBitmap(width: Int, height: Int, duplicateIfBitmapDrawable: Boolean = false): Bitmap {
    if (this is BitmapDrawable && bitmap != null) {
        return if (duplicateIfBitmapDrawable) {
            Bitmap.createBitmap(bitmap)
        } else bitmap
    }
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    try { draw(canvas) }
    catch (e: Exception) { e.printStackTrace() }
    return bitmap
}

inline fun Drawable.toBitmapDrawable(duplicateIfBitmapDrawable: Boolean = false) = if (this is BitmapDrawable && !duplicateIfBitmapDrawable) this else {
    BitmapDrawable(Tools.publicContext!!.resources, toBitmap())
}

inline fun Drawable.clone() = constantState?.newDrawable()?.mutate()

inline val Number.dp get() = Tools.publicContext!!.resources.displayMetrics.density * toFloat()
inline val Number.sp get() = Tools.publicContext!!.resources.displayMetrics.density * toFloat()

inline val Context.mainFont: Typeface
    get() = if (Settings["font", "lexendDeca"] == "sansserif" || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) Typeface.SANS_SERIF
    else {
        when (Settings["font", "lexendDeca"]) {
            "posidonsans" -> resources.getFont(R.font.posidon_sans)
            "monospace" -> resources.getFont(R.font.ubuntu_mono)
            "ubuntu" -> resources.getFont(R.font.ubuntu_medium)
            "openDyslexic" -> resources.getFont(R.font.open_dyslexic3)
            "inter" -> resources.getFont(R.font.inter)
            else -> resources.getFont(R.font.lexend_deca)
        }
    }

inline val Context.isAirplaneModeOn get() =
    android.provider.Settings.System.getInt(contentResolver, android.provider.Settings.Global.AIRPLANE_MODE_ON, 0) != 0

inline fun Context.pullStatusbar() {
    try {
        @SuppressLint("WrongConstant")
        val sbs = getSystemService("statusbar")
        Class.forName("android.app.StatusBarManager").getMethod("expandNotificationsPanel")(sbs)
    } catch (e: Exception) { e.printStackTrace() }
}



inline fun Context.getStatusBarHeight(): Int {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
}

inline fun Context.vibrate() {
    val duration = Settings["hapticfeedback", 14]
    if (duration != 0) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(
                VibrationEffect.createOneShot(duration.toLong(), VibrationEffect.DEFAULT_AMPLITUDE),
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        ) else (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(duration.toLong())
    }
}

inline fun Activity.hideKeyboard() {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var view = currentFocus
    if (view == null) {
        view = View(this)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

inline fun Activity.setWallpaperOffset(x: Float, y: Float) {
    val wallManager = WallpaperManager.getInstance(this)
    //wallManager.setWallpaperOffsets(window.attributes.token, x, y)
    wallManager.setWallpaperOffsetSteps(0f, 0f)
    wallManager.suggestDesiredDimensions(Device.displayWidth, Device.displayHeight)
}

inline fun ViewPropertyAnimator.onEnd(crossinline onEnd: (animation: Animator?) -> Unit): ViewPropertyAnimator = setListener(object : Animator.AnimatorListener {
    override fun onAnimationRepeat(animation: Animator?) {}
    override fun onAnimationCancel(animation: Animator?) {}
    override fun onAnimationStart(animation: Animator?) {}
    override fun onAnimationEnd(animation: Animator?) = onEnd(animation)
})

inline fun Animator.onEnd(crossinline onEnd: (animation: Animator?) -> Unit) = addListener(object : Animator.AnimatorListener {
    override fun onAnimationRepeat(animation: Animator?) {}
    override fun onAnimationCancel(animation: Animator?) {}
    override fun onAnimationStart(animation: Animator?) {}
    override fun onAnimationEnd(animation: Animator?) = onEnd(animation)
})