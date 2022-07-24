package posidon.launcher.tools.theme

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.RECTANGLE
import android.text.TextUtils
import android.widget.TextView
import io.posidon.android.conveniencelib.units.dp
import io.posidon.android.conveniencelib.units.toFloatPixels
import io.posidon.android.conveniencelib.units.toPixels
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools

object Customizer {

    fun styleLabel(namespace: String, view: TextView, defaultColor: Int, defaultTextSize: Int) {
        view.setTextColor(Settings["$namespace:color", defaultColor])
        val maxLines = Settings["$namespace:max_lines", 1]
        view.isSingleLine = maxLines == 1
        view.maxLines = maxLines
        view.ellipsize = TextUtils.TruncateAt.END
        val textSize = Settings["$namespace:text_size", defaultTextSize]
        view.textSize = textSize.toFloat()
        view.layoutParams = view.layoutParams.also {
            it.height = (textSize.dp.toFloatPixels(view) + 4.dp.toFloatPixels(view)).toInt()
        }
    }

    fun genBG(context: Context, namespace: String, onlyCurveTop: Boolean, defRadius: Int, defColor: Int): Drawable {
        val strokeWidth = Settings["$namespace:stroke:width", 0]
        val radius = Settings["$namespace:radius", defRadius]
        val backgroundColor = Settings["$namespace:color", defColor]
        val strokeColor = Settings["$namespace:stroke:color", 0]
        return GradientDrawable().apply {
            val r = radius.dp.toFloatPixels(context)
            shape = RECTANGLE
            cornerRadii = if (onlyCurveTop) floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f) else floatArrayOf(r, r, r, r, r, r, r, r)
            setColor(backgroundColor)
            setStroke(strokeWidth.dp.toPixels(context), strokeColor)
        }
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
}