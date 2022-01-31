package posidon.launcher.view.setting

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import posidon.android.conveniencelib.dp
import posidon.launcher.Global
import posidon.launcher.R

abstract class SettingView : LinearLayout {

    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0
    ) : super(context, attrs, defStyle, defStyleRes) {
        init(attrs, defStyle, defStyleRes)
    }

    constructor(context: Context, key: String, labelId: Int, iconId: Int) : super(context) {
        this.key = key
        if (doSpecialIcon) {
            populateIcon()
        }
        createLabelView(resources.getString(labelId), iconId)
        populate(null, 0, 0)
    }

    lateinit var key: String

    protected lateinit var labelView: TextView

    protected open val doSpecialIcon = false

    protected open fun init(attrs: AttributeSet?, defStyle: Int, defStyleRes: Int) {

        orientation = HORIZONTAL

        val a = context.obtainStyledAttributes(attrs, R.styleable.SettingView, defStyle, defStyleRes)
        a.getString(R.styleable.SettingView_key)?.let { key = it }

        if (doSpecialIcon) {
            populateIcon()
        }
        createLabelView(a.getString(R.styleable.SettingView_label), a.getResourceId(R.styleable.SettingView_drawable, 0))
        populate(attrs, defStyle, defStyleRes)
    }

    private fun createLabelView(label: String?, iconId: Int) {
        labelView = TextView(context).apply {
            if (!doSpecialIcon) {
                setCompoundDrawablesRelativeWithIntrinsicBounds(iconId, 0, 0, 0)
                TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(Global.getPastelAccent()))
            }
            text = label
            textSize = 16f
            includeFontPadding = false
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            compoundDrawablePadding = dp(15).toInt()
            val h = dp(8).toInt()
            setPadding(h, 0, h, 0)
            setTextColor(context.getColor(R.color.cardtxt))
        }
        addView(labelView, LayoutParams(0, dp(60).toInt(), 1f))
    }

    open fun populateIcon() {}
    abstract fun populate(attrs: AttributeSet?, defStyle: Int, defStyleRes: Int)

    var label: CharSequence
        get() = labelView.text
        set(value) {
            labelView.text = value
        }
}
