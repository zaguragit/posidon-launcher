package posidon.launcher.tools

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.launcherutils.Launcher
import posidon.android.conveniencelib.Device
import posidon.android.conveniencelib.dp
import posidon.android.conveniencelib.getNavigationBarHeight
import posidon.android.conveniencelib.toBitmap
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.storage.Settings
import posidon.launcher.view.recycler.GridLayoutManager
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

object Tools {

    inline fun isInstalled(packageName: String, packageManager: PackageManager): Boolean {
        var found = true
        try { packageManager.getPackageInfo(packageName, 0) }
        catch (e: Exception) { found = false }
        return found
    }

    var appContextReference = WeakReference<Context>(null)
    inline val appContext get() = appContextReference.get()

    inline val canBlurDrawer get() = Settings["drawer:blur", true] && ContextCompat.checkSelfPermission(appContext!!, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
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
        }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Device.screenHeight(context) - appContext!!.dp(300).toInt()))
        window!!.setBackgroundDrawableResource(R.drawable.card)
        show()
    }

    inline fun isAClick(startX: Float, endX: Float, startY: Float, endY: Float): Boolean {
        val threshold = appContext!!.dp(16)
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
            location[1] + view.measuredHeight + appContext!!.dp(4).toInt()
        } else {
            gravity = gravity or Gravity.BOTTOM
            screenHeight - location[1] + appContext!!.dp(4).toInt() + navbarHeight
        }

        return Triple(x, y, gravity)
    }
}

inline fun Context.getStatusBarHeight(): Int {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
}

inline fun Context.vibrate() {
    val duration = Settings["hapticfeedback", 14]
    if (duration != 0) {
        getSystemService(Vibrator::class.java).vibrate(
            VibrationEffect.createOneShot(duration.toLong(), VibrationEffect.DEFAULT_AMPLITUDE),
            AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        )
    }
}

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