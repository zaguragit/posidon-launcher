package posidon.launcher.view.setting

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import io.posidon.android.conveniencelib.units.dp
import io.posidon.android.conveniencelib.units.toPixels
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.view.Switch

class HeaderSwitchSettingView : HeaderSettingView {

    private lateinit var switch: Switch

    constructor(c: Context) : super(c)
    constructor(c: Context, a: AttributeSet?) : super(c, a)
    constructor(c: Context, a: AttributeSet?, sa: Int) : super(c, a, sa)
    constructor(c: Context, attrs: AttributeSet?, sa: Int, sr: Int) : super(c, attrs, sa, sr) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SettingView, sa, sr)
        val default = a.getBoolean(R.styleable.SettingView_def, false)
        key = a.getString(R.styleable.SettingView_key)!!
        value = Settings[key, default]
        a.recycle()
    }

    var value: Boolean
        get() = switch.isChecked
        set(value) {
            switch.isChecked = value
        }

    lateinit var key: String

    var onCheckedChange: ((Boolean) -> Unit)? = null

    override fun populate(attrs: AttributeSet?, defStyle: Int, defStyleRes: Int) {
        switch = Switch(context).apply {
            val p = 12.dp.toPixels(context)
            setPadding(p, p, p, p)
            layoutParams = LayoutParams(WRAP_CONTENT, MATCH_PARENT, Gravity.CENTER_VERTICAL or Gravity.END).apply {
                marginEnd = 12.dp.toPixels(context)
            }
            setOnCheckedChangeListener { _, checked ->
                Settings[key] = checked
                onCheckedChange?.invoke(checked)
            }
        }
        addView(switch)
    }
}