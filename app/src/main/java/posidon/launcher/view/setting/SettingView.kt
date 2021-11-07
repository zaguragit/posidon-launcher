package posidon.launcher.view.setting

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import posidon.android.conveniencelib.dp
import posidon.launcher.Global
import posidon.launcher.R

abstract class SettingView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyle, defStyleRes) {

    protected lateinit var labelView: TextView
    protected open val doSpecialIcon = false

    lateinit var key: String

    init {
        init(attrs, defStyle, defStyleRes)
    }

    protected open fun init(attrs: AttributeSet?, defStyle: Int, defStyleRes: Int) {

        orientation = HORIZONTAL

        val a = context.obtainStyledAttributes(attrs, R.styleable.SettingView, defStyle, defStyleRes)
        key = a.getString(R.styleable.SettingView_key)!!

        if (doSpecialIcon) {
            populateIcon(a)
        }
        labelView = TextView(context).apply {
            if (!doSpecialIcon) {
                setCompoundDrawablesRelativeWithIntrinsicBounds(a.getResourceId(R.styleable.SettingView_drawable, 0), 0, 0, 0)
                TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(Global.getPastelAccent()))
            }
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
        populate(attrs, defStyle, defStyleRes)
    }

    open fun populateIcon(a: TypedArray) {}
    abstract fun populate(attrs: AttributeSet?, defStyle: Int, defStyleRes: Int)

    var label: CharSequence by labelView::text
}
