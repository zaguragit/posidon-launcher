package posidon.launcher.tools.theme

import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.RECTANGLE
import android.text.TextUtils
import android.widget.TextView
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.dp

object Customizer {

    fun styleLabel(namespace: String, view: TextView, defaultColor: Int) {
        view.setTextColor(Settings["$namespace:color", defaultColor])
        val maxLines = Settings["$namespace:max_lines", 1]
        view.isSingleLine = maxLines == 1
        view.maxLines = maxLines
        view.ellipsize = if (Settings["$namespace:marquee", true] && maxLines == 1) TextUtils.TruncateAt.MARQUEE else TextUtils.TruncateAt.END
        view.isSelected = true
        view.isHorizontalFadingEdgeEnabled = true
        view.setFadingEdgeLength(5.dp.toInt())

        view.freezesText = true
        view.setHorizontallyScrolling(true)
        view.isSelected = true
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        view.isEnabled = true
    }

    fun getCustomIcon(key: String): Drawable? {
        val customIcon = Settings.getString(key)
        if (!customIcon.isNullOrEmpty()) {
            try {
                val data = customIcon.split(':').toTypedArray()[1].split('|').toTypedArray()
                val t = Tools.appContext!!.packageManager.getResourcesForApplication(data[0])
                val intRes = t.getIdentifier(data[1], "drawable", data[0])
                return t.getDrawable(intRes)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    fun genBG(namespace: String, onlyCurveTop: Boolean, defRadius: Int, defColor: Int): Drawable {
        return GradientDrawable().apply {
            val r = Settings["$namespace:radius", defRadius].dp
            shape = RECTANGLE
            cornerRadii = if (onlyCurveTop) floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f) else floatArrayOf(r, r, r, r, r, r, r, r)
            setColor(Settings["$namespace:color", defColor])
            setStroke(Settings["$namespace:stroke:width", 0].dp.toInt(), Settings["$namespace:stroke:color", 0])
        }
    }
}