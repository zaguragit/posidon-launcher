package posidon.launcher.tools

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.*
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import posidon.launcher.R
import posidon.launcher.storage.Settings
import kotlin.math.*

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

	inline fun animate(d: Drawable): Drawable {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && d is Animatable2 -> {
                d.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable) = d.start()
                })
                d.start()
            }
            d is Animatable2Compat -> {
                d.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable) = d.start()
                })
                d.start()
            }
            d is AnimationDrawable -> d.start()
        }
        return d
    }

	inline fun clearAnimation(d: Drawable?) { when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && d is Animatable2 -> d.clearAnimationCallbacks()
        d is Animatable2Compat -> d.clearAnimationCallbacks()
        d is Animatable -> d.stop()
    }}

	inline fun isInstalled(packageName: String, packageManager: PackageManager): Boolean {
        var found = true
        try { packageManager.getPackageInfo(packageName, 0) }
        catch (e: Exception) { found = false }
        return found
    }

    var publicContext: Context? = null

    fun fastBlur(bitmap: Bitmap, radius: Int): Bitmap? {
        if (radius < 1) return null
        var bitmap = bitmap
        val d = radius.toFloat()
        val width = (bitmap.width / d).roundToInt()
        val height = (bitmap.height / d).roundToInt()
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
        bitmap = bitmap.copy(bitmap.config, true)
        val w = bitmap.width
        val h = bitmap.height
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
        return bitmap
    }

	inline fun canBlurWall(context: Context?) = Settings["blur", true] && ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

	fun blurredWall(context: Context, radius: Float): Bitmap? {
        try {
            @SuppressLint("MissingPermission") var bitmap: Bitmap? = WallpaperManager.getInstance(context).peekFastDrawable().toBitmap()
            val displayWidth = Device.displayWidth
            val displayHeight = Device.displayHeight + navbarHeight
            if (bitmap!!.width > radius && bitmap.height > radius) {
                when {
                    bitmap.height / bitmap.width.toFloat() < displayHeight / displayWidth.toFloat() -> {
                        bitmap = Bitmap.createScaledBitmap(
                                bitmap,
                                displayHeight * bitmap.width / bitmap.height,
                                displayHeight,
                                false)
                        bitmap.config = Bitmap.Config.ARGB_8888
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
                        bitmap.config = Bitmap.Config.ARGB_8888
                        bitmap = Bitmap.createBitmap(
                                bitmap, 0, bitmap.height - displayHeight shr 1,
                                displayWidth,
                                displayHeight)
                    }
                    else -> bitmap = Bitmap.createScaledBitmap(bitmap, displayWidth, displayHeight, false)
                }
                if (radius > 0) try { bitmap = fastBlur(bitmap, radius.toInt()) }
                catch (e: Exception) { e.printStackTrace() }
            }
            return bitmap
        } catch (e: OutOfMemoryError) {
            Toast.makeText(context, "OutOfMemoryError: Couldn't blur wallpaper!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) { e.printStackTrace() }
        return null
    }

	fun centerCropWallpaper(context: Context, wallpaper: Bitmap): Bitmap {
        val scaledWidth = context.resources.displayMetrics.heightPixels * wallpaper.width / wallpaper.height
        var scaledWallpaper = Bitmap.createScaledBitmap(
                wallpaper,
                scaledWidth,
                context.resources.displayMetrics.heightPixels,
                false)
        scaledWallpaper = Bitmap.createBitmap(
                scaledWallpaper,
                scaledWidth - context.resources.displayMetrics.widthPixels shr 1,
                0,
                context.resources.displayMetrics.widthPixels,
                context.resources.displayMetrics.heightPixels)
        return scaledWallpaper
    }

	var navbarHeight = 0

	fun updateNavbarHeight(activity: Activity) {
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
    fun adaptic(context: Context, drawable: Drawable): Drawable {
        val icShape = Settings["icshape", 4]
        return if (icShape == 0) drawable else if (drawable is AdaptiveIconDrawable || Settings["reshapeicons", false]) {
            val drr = arrayOfNulls<Drawable>(2)
            if (drawable is AdaptiveIconDrawable) {
                drr[0] = drawable.background
                drr[1] = drawable.foreground
                if (Settings["icon:tint_white_bg", true]) {
                    val bg = drr[0]
                    if ((bg is ColorDrawable && bg.color == 0xffffffff.toInt()) ||
                            (Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)!!.apply {
                                val tmp = bg!!.bounds
                                bg.bounds = Rect(0, 0, 1, 1)
                                bg.draw(Canvas(this))
                                bg.bounds = tmp
                            }.getPixel(0, 0) and 0xffffff).let {
                                Color.red(it) > 0xdd &&
                                Color.green(it) > 0xdd &&
                                Color.blue(it) > 0xdd
                            }) {
                        val bgColor = Settings["icon:background", 0xff252627.toInt()]
                        drr[0] = ColorDrawable(when(Settings["icon:background_type", "custom"]) {
                            "dominant" -> Palette.from(drr[1]!!.toBitmap()).generate().getDominantColor(bgColor)
                            "lv" -> Palette.from(drr[1]!!.toBitmap()).generate().getLightVibrantColor(bgColor)
                            "dv" -> Palette.from(drr[1]!!.toBitmap()).generate().getDarkVibrantColor(bgColor)
                            "lm" -> Palette.from(drr[1]!!.toBitmap()).generate().getLightMutedColor(bgColor)
                            "dm" -> Palette.from(drr[1]!!.toBitmap()).generate().getDarkMutedColor(bgColor)
                            else -> bgColor
                        })
                    }
                }
            } else {
                val d = drawable as BitmapDrawable
                val b = Bitmap.createBitmap(d.intrinsicWidth, d.intrinsicHeight, Bitmap.Config.ARGB_8888)
                val c = Canvas(b)
                d.setBounds(c.width / 4, c.height / 4, c.width / 4 * 3, c.height / 4 * 3)
                drawable.draw(c)
                drr[1] = BitmapDrawable(context.resources, b)
                val bgColor = Settings["icon:background", 0xff252627.toInt()]
                drr[0] = ColorDrawable(when(Settings["icon:background_type", "custom"]) {
                    "dominant" -> Palette.from(b).generate().getDominantColor(bgColor)
                    "lv" -> Palette.from(b).generate().getLightVibrantColor(bgColor)
                    "dv" -> Palette.from(b).generate().getDarkVibrantColor(bgColor)
                    "lm" -> Palette.from(b).generate().getLightMutedColor(bgColor)
                    "dm" -> Palette.from(b).generate().getDarkMutedColor(bgColor)
                    else -> bgColor
                })
            }
            val layerDrawable = LayerDrawable(drr)
            val width = layerDrawable.intrinsicWidth
            val height = layerDrawable.intrinsicHeight
            var bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            var canvas = Canvas(bitmap!!)
            layerDrawable.setBounds(-canvas.width / 4, -canvas.height / 4, canvas.width / 4 * 5, canvas.height / 4 * 5)
            layerDrawable.draw(canvas)
            val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            canvas = Canvas(outputBitmap)
            canvas.drawBitmap(bitmap, 0f, 0f, Paint().apply { isAntiAlias = true })
            if (icShape != 3) {
                val path = Path()
                when (icShape) {
                    1 -> path.addCircle(width.toFloat() / 2f + 1, height.toFloat() / 2f + 1, min(width.toFloat(), height.toFloat() / 2f) - 2, Path.Direction.CCW)
                    2 -> path.addRoundRect(2f, 2f, width - 2.toFloat(), height - 2.toFloat(), min(width, height).toFloat() / 4f, min(width, height).toFloat() / 4f, Path.Direction.CCW)
                    4 -> { //Formula: (|x|)^3 + (|y|)^3 = radius^3
                        val xx = 2
                        val yy = 2
                        val radius = (min(width, height) shr 1) - 2
                        val radiusToPow = radius * radius * radius.toDouble()
                        path.moveTo(-radius.toFloat(), 0f)
                        for (x in -radius..radius) path.lineTo(x.toFloat(), Math.cbrt(radiusToPow - abs(x * x * x)).toFloat())
                        for (x in radius downTo -radius) path.lineTo(x.toFloat(), (-Math.cbrt(radiusToPow - abs(x * x * x))).toFloat())
                        path.close()
                        val matrix = Matrix()
                        matrix.postTranslate(xx + radius.toFloat(), yy + radius.toFloat())
                        path.transform(matrix)
                    }
                }
                path.fillType = Path.FillType.INVERSE_EVEN_ODD
                canvas.drawPath(path, Paint().apply {
                    isAntiAlias = true
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                })
            }
            bitmap = outputBitmap
            BitmapDrawable(context.resources, bitmap)
        } else drawable
    }

    inline val isDefaultLauncher: Boolean
        get() {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            return publicContext!!.packageManager.resolveActivity(intent, 0)?.resolvePackageName == "posidon.launcher"
        }

    inline fun springInterpolate(x: Float) = 1 + (2f.pow(-10f * x) * sin(2 * PI * (x - 0.075f))).toFloat()
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
    if (this is BitmapDrawable && bitmap != null) return if (duplicateIfBitmapDrawable) Bitmap.createBitmap(bitmap) else bitmap
    val bitmap: Bitmap = if (intrinsicWidth <= 0 || intrinsicHeight <= 0) Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    else Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    try { draw(canvas) }
    catch (e: Exception) {}
    return bitmap
}

inline fun Drawable.toBitmap(width: Int, height: Int, duplicateIfBitmapDrawable: Boolean = false): Bitmap {
    if (this is BitmapDrawable && bitmap != null) return if (duplicateIfBitmapDrawable) Bitmap.createBitmap(bitmap) else bitmap
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    try { draw(canvas) }
    catch (e: Exception) { e.printStackTrace() }
    return bitmap
}

inline val Number.dp get() = Tools.publicContext!!.resources.displayMetrics.density * toFloat()
inline val Number.sp get() = Tools.publicContext!!.resources.displayMetrics.density * toFloat()

inline val Context.mainFont get() =
    if (Settings["font", "lexendDeca"] == "sansserif" || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) Typeface.SANS_SERIF
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
    if (view == null) view = View(this)
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}