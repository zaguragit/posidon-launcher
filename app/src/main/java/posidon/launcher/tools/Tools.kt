package posidon.launcher.tools

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.media.AudioAttributes
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.DisplayMetrics
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.storage.Settings
import posidon.launcher.tools.theme.toBitmap
import posidon.launcher.view.GridLayoutManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import kotlin.math.*

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

    inline val isDefaultLauncher: Boolean get() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        return appContext!!.packageManager.resolveActivity(intent, 0)?.resolvePackageName == "posidon.launcher"
    }

    inline fun springInterpolate(x: Float) = 1 + (2f.pow(-10f * x) * sin(2 * PI * (x - 0.065f)) / 0.4).toFloat()

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
        }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Device.displayHeight - 300.dp.toInt()))
        window!!.setBackgroundDrawableResource(R.drawable.card)
        show()
    }

    inline fun isAClick(startX: Float, endX: Float, startY: Float, endY: Float): Boolean {
        val threshold = 16.dp
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

        val x = if (location[0] > Device.displayWidth / 2) {
            gravity = Gravity.END
            Device.displayWidth - location[0] - view.measuredWidth
        } else {
            gravity = Gravity.START
            location[0]
        }

        val y = if (location[1] < Device.displayHeight / 2) {
            gravity = gravity or Gravity.TOP
            location[1] + view.measuredHeight + 4.dp.toInt()
        } else {
            gravity = gravity or Gravity.BOTTOM
            Device.displayHeight - location[1] + 4.dp.toInt() + navbarHeight
        }

        return Triple(x, y, gravity)
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

inline val Number.dp get() = Tools.appContext!!.resources.displayMetrics.density * toFloat()
inline val Number.sp get() = Tools.appContext!!.resources.displayMetrics.density * toFloat()

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

inline fun Context.open(action: String, b: Bundle? = null, block: Intent.() -> Unit) = startActivity(Intent(action)
    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK).also(block), b)
inline fun Context.open(c: Class<*>, b: Bundle? = null, block: Intent.() -> Unit) = startActivity(Intent(this, c)
    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK).also(block), b)
inline fun Context.open(action: String, b: Bundle? = null) = startActivity(Intent(action)
    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK), b)
inline fun Context.open(c: Class<*>, b: Bundle? = null) = startActivity(Intent(this, c)
    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK), b)

fun <T> Context.loadRaw(id: Int, fn: (BufferedReader) -> T) =
    resources.openRawResource(id).use { stream ->
        val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
        fn(reader)
    }