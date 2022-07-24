package posidon.launcher.view.setting

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import io.posidon.android.conveniencelib.units.dp
import io.posidon.android.conveniencelib.units.toPixels
import posidon.launcher.Global
import posidon.launcher.R

class ClickableSettingView : LinearLayout {

    constructor(c: Context) : this(c, null)
    constructor(c: Context, attrs: AttributeSet?) : this(c, attrs, 0)
    constructor(c: Context, attrs: AttributeSet?, sa: Int) : this(c, attrs, sa, 0)
    constructor(c: Context, attrs: AttributeSet?, sa: Int, sr: Int) : super(c, attrs, sa, sr) {
        orientation = HORIZONTAL

        val a = context.obtainStyledAttributes(attrs, R.styleable.SettingView, sa, sr)

        val tint = ColorStateList.valueOf(Global.getPastelAccent())

        val labelView = TextView(context).apply {
            setCompoundDrawablesRelativeWithIntrinsicBounds(a.getResourceId(R.styleable.SettingView_drawable, 0), 0, 0, 0)
            TextViewCompat.setCompoundDrawableTintList(this, tint)
            text = a.getString(R.styleable.SettingView_label)
            textSize = 16f
            includeFontPadding = false
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            compoundDrawablePadding = 15.dp.toPixels(context)
            val h = 8.dp.toPixels(context)
            setPadding(h, 0, h, 0)
            setTextColor(context.getColor(R.color.cardtxt))
        }
        addView(labelView, LayoutParams(0, 60.dp.toPixels(context), 1f))

        View(context).apply {
            val size = 24.dp.toPixels(context)
            background = context.getDrawable(R.drawable.ic_arrow_right)
            backgroundTintList = tint
            addView(this, LayoutParams(size, size, 0f).apply {
                val m = 18.dp.toPixels(context)
                setMargins(m, m, m, m)
            })
        }
    }

    constructor(context: Context, label: String, icon: Drawable) : super(context) {
        orientation = HORIZONTAL

        val tint = ColorStateList.valueOf(Global.getPastelAccent())

        val labelView = TextView(context).apply {
            setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
            TextViewCompat.setCompoundDrawableTintList(this, tint)
            text = label
            textSize = 16f
            includeFontPadding = false
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            compoundDrawablePadding = 15.dp.toPixels(context)
            val h = 8.dp.toPixels(context)
            setPadding(h, 0, h, 0)
            setTextColor(context.getColor(R.color.cardtxt))
        }
        addView(labelView, LayoutParams(0, 60.dp.toPixels(context), 1f))

        View(context).apply {
            val size = 24.dp.toPixels(context)
            background = context.getDrawable(R.drawable.ic_arrow_right)
            backgroundTintList = tint
            addView(this, LayoutParams(size, size, 0f).apply {
                val m = 18.dp.toPixels(context)
                setMargins(m, m, m, m)
            })
        }
    }
}
