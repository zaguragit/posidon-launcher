package posidon.launcher.view.setting

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import posidon.android.conveniencelib.dp
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
            compoundDrawablePadding = dp(15).toInt()
            val h = dp(8).toInt()
            setPadding(h, 0, h, 0)
            setTextColor(context.getColor(R.color.cardtxt))
        }
        addView(labelView, LayoutParams(0, dp(60).toInt(), 1f))

        View(context).apply {
            val size = dp(24).toInt()
            background = context.getDrawable(R.drawable.ic_arrow_right)
            backgroundTintList = tint
            addView(this, LayoutParams(size, size, 0f).apply {
                val m = dp(18).toInt()
                setMargins(m, m, m, m)
            })
        }
    }
}
