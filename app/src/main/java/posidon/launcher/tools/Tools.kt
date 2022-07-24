package posidon.launcher.tools

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.conveniencelib.Device
import io.posidon.android.conveniencelib.getNavigationBarHeight
import io.posidon.android.conveniencelib.units.dp
import io.posidon.android.conveniencelib.units.toPixels
import io.posidon.android.conveniencelib.vibrate
import io.posidon.android.launcherutils.Launcher
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.storage.Settings
import posidon.launcher.view.recycler.GridLayoutManager
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object Tools {

    inline fun isInstalled(packageName: String, packageManager: PackageManager): Boolean {
        var found = true
        try { packageManager.getPackageInfo(packageName, 0) }
        catch (e: Exception) { found = false }
        return found
    }

    var appContextReference = WeakReference<Context>(null)
    inline val appContext get() = appContextReference.get()

    inline val canBlurDrawer get() = Settings["drawer:blur:enabled", true] && ContextCompat.checkSelfPermission(appContext!!, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    inline val canBlurSearch get() = Settings["search:blur", true] && ContextCompat.checkSelfPermission(appContext!!, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

	var navbarHeight = 0

	fun updateNavbarHeight(activity: Activity) {
        if (Settings["ignore_navbar", false]) {
            navbarHeight = 0
            return
        }
        navbarHeight = activity.getNavigationBarHeight()
    }

    inline val isDefaultLauncher: Boolean get() {
        return Launcher.getDefaultLauncher(appContext!!.packageManager) == "posidon.launcher"
    }

    fun searchOptimize(s: String) = s.lowercase(Locale.getDefault())
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

    private class AppSelectionAdapter(
        val apps: List<App>,
        val onClick: (app: App) -> Unit
    ) : RecyclerView.Adapter<AppSelectionAdapter.ViewHolder>() {

        class ViewHolder(
            val view: View,
            val icon: ImageView,
            val iconFrame: FrameLayout,
            val text: TextView
        ) : RecyclerView.ViewHolder(view)

        override fun getItemCount() = apps.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(appContext!!).inflate(R.layout.drawer_item, parent, false)
            val iconFrame = view.findViewById<FrameLayout>(R.id.iconFrame)
            val icon = iconFrame.findViewById<ImageView>(R.id.iconimg)
            val text = view.findViewById<TextView>(R.id.icontxt)
            return ViewHolder(view, icon, iconFrame, text)
        }

        override fun onBindViewHolder(holder: ViewHolder, i: Int) {
            val app = apps[i]
            holder.icon.setImageDrawable(app.icon)
            holder.text.text = app.label
            holder.view.setOnClickListener {
                onClick(app)
            }
        }
    }

    fun selectApp(context: Context, includeHidden: Boolean, out: (app: App) -> Unit) = Dialog(context, R.style.longpressmenusheet).run {
        setContentView(RecyclerView(context).apply {
            val apps = if (includeHidden) ArrayList<App>().apply {
                addAll(Global.apps)
                addAll(App.hidden)
                if (Settings["drawer:sorting", 0] == 1) sortWith { o1, o2 ->
                    val iHsv = floatArrayOf(0f, 0f, 0f)
                    val jHsv = floatArrayOf(0f, 0f, 0f)
                    Color.colorToHSV(Palette.from(o1.icon!!.toBitmap()).generate().getVibrantColor(0xff252627.toInt()), iHsv)
                    Color.colorToHSV(Palette.from(o2.icon!!.toBitmap()).generate().getVibrantColor(0xff252627.toInt()), jHsv)
                    (iHsv[0] - jHsv[0]).toInt()
                }
                else sortWith { o1, o2 ->
                    o1.label.compareTo(o2.label, ignoreCase = true)
                }
            } else Global.apps
            layoutManager = GridLayoutManager(context, 4)
            adapter = AppSelectionAdapter(apps) {
                out(it)
                dismiss()
            }
        }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Device.screenHeight(context) - 300.dp.toPixels(context)))
        window!!.setBackgroundDrawableResource(R.drawable.card)
        show()
    }

    inline fun isAClick(startX: Float, endX: Float, startY: Float, endY: Float): Boolean {
        val threshold = 16.dp.toPixels(appContext!!)
        return abs(startX - endX) < threshold && abs(startY - endY) < threshold
    }

    /**
     * @return Triple(x, y, gravity)
     */
    inline fun getPopupLocationFromView(view: View): Triple<Int, Int, Int> {

        val location = IntArray(2).also {
            view.getLocationOnScreen(it)
        }

        var gravity: Int

        val screenWidth = Device.screenWidth(view.context)
        val screenHeight = Device.screenHeight(view.context)

        val x = if (location[0] > screenWidth / 2) {
            gravity = Gravity.END
            screenWidth - location[0] - view.measuredWidth
        } else {
            gravity = Gravity.START
            location[0]
        }

        val y = if (location[1] < screenHeight / 2) {
            gravity = gravity or Gravity.TOP
            location[1] + view.measuredHeight + 4.dp.toPixels(appContext!!)
        } else {
            gravity = gravity or Gravity.BOTTOM
            screenHeight - location[1] + 4.dp.toPixels(appContext!!) + navbarHeight
        }

        return Triple(x, y, gravity)
    }
}

inline fun Context.vibrate() = vibrate(Settings["hapticfeedback", 14])

inline fun Context.open(action: String, b: Bundle? = null, block: Intent.() -> Unit) = startActivity(Intent(action)
    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK).also(block), b)
inline fun Context.open(c: Class<*>, b: Bundle? = null, block: Intent.() -> Unit) = startActivity(Intent(this, c)
    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK).also(block), b)
inline fun Context.open(action: String, b: Bundle? = null) = startActivity(Intent(action)
    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK), b)
inline fun Context.open(c: Class<*>, b: Bundle? = null) = startActivity(Intent(this, c)
    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK), b)


inline fun Drawable.clone() = constantState?.newDrawable()?.mutate()

inline fun Drawable.toBitmapDrawable(duplicateIfBitmapDrawable: Boolean = false) = if (this is BitmapDrawable && !duplicateIfBitmapDrawable) this else {
    BitmapDrawable(Tools.appContext!!.resources, toBitmap())
}

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
    var yw: Int = yi
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