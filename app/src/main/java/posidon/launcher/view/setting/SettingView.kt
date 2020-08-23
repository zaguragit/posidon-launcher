package posidon.launcher.view.setting

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import posidon.launcher.R
import posidon.launcher.tools.dp

abstract class SettingView : LinearLayout {

    protected lateinit var labelView: TextView
    protected open val doSpecialIcon = false

    protected lateinit var key: String

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int, defStyleRes: Int) : super(context, attrs, defStyle, defStyleRes) {
        init(attrs, defStyle)
    }

    protected open fun init(attrs: AttributeSet?, defStyle: Int) {

        orientation = HORIZONTAL

        val a = context.obtainStyledAttributes(attrs, R.styleable.SettingView, defStyle, 0)
        key = a.getString(R.styleable.SettingView_key)!!

        if (doSpecialIcon) {
            populateIcon(a)
        }
        labelView = TextView(context).apply {
            if (!doSpecialIcon) {
                setCompoundDrawablesRelativeWithIntrinsicBounds(a.getResourceId(R.styleable.SettingView_drawable, 0), 0, 0, 0)
            }
            text = a.getString(R.styleable.SettingView_label)
            textSize = 17f
            includeFontPadding = false
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            compoundDrawablePadding = 15.dp.toInt()
            val h = 8.dp.toInt()
            setPadding(h, 0, h, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                setTextColor(context.getColor(R.color.cardtxt))
            } else {
                setTextColor(context.resources.getColor(R.color.cardtxt))
            }
            layoutParams = LayoutParams(0, 60.dp.toInt(), 1f)
        }
        addView(labelView)
        populate(attrs, defStyle)
    }

    open fun populateIcon(a: TypedArray) {}
    abstract fun populate(attrs: AttributeSet?, defStyle: Int)
}
