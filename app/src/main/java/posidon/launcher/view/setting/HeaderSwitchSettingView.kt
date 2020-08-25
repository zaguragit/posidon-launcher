package posidon.launcher.view.setting

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.TextView
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.dp
import posidon.launcher.view.Switch

class HeaderSwitchSettingView : FrameLayout {

    private lateinit var switch: Switch
    private lateinit var labelView: TextView
    private lateinit var key: String

    constructor(c: Context) : this(c, null)
    constructor(c: Context, a: AttributeSet?) : this(c, a, 0)
    constructor(c: Context, a: AttributeSet?, sa: Int) : this(c, a, sa, 0)
    constructor(c: Context, a: AttributeSet?, sa: Int, sr: Int) : super(c, a, sa, sr) {
        init(a, sa, sr)
    }

    var onCheckedChange: ((Boolean) -> Unit)? = null

    fun init(attrs: AttributeSet?, defStyle: Int, defStyleRes: Int) {

        val a = context.obtainStyledAttributes(attrs, R.styleable.SettingView, defStyle, defStyleRes)
        key = a.getString(R.styleable.SettingView_key)!!

        labelView = TextView(context).apply {
            text = a.getString(R.styleable.SettingView_label)
            textSize = 20f
            gravity = Gravity.CENTER_HORIZONTAL
            includeFontPadding = false
            val p = 20.dp.toInt()
            setPadding(p, p, p, p)
            setTextColor(context.resources.getColor(R.color.cardtitle))
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL)
        }
        addView(labelView)
        val separator = View(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 2.dp.toInt(), Gravity.BOTTOM)
            setBackgroundResource(R.drawable.card_separator)
        }
        addView(separator)
        populate(attrs, defStyle, defStyleRes)
    }

    fun populate(attrs: AttributeSet?, defStyle: Int, defStyleRes: Int) {

        val a = context.obtainStyledAttributes(attrs, R.styleable.SettingView, defStyle, defStyleRes)
        val default = a.getBoolean(R.styleable.SettingView_def, false)

        switch = Switch(context).apply {
            val p = 12.dp.toInt()
            setPadding(p, p, p, p)
            layoutParams = LayoutParams(WRAP_CONTENT, MATCH_PARENT, Gravity.CENTER_VERTICAL or Gravity.END).apply {
                marginEnd = 12.dp.toInt()
            }
            isChecked = Settings[key, default]
            setOnCheckedChangeListener { _, checked ->
                Settings[key] = checked
                onCheckedChange?.invoke(checked)
            }
        }
        addView(switch)
    }
}