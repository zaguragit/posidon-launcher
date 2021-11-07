package posidon.launcher.view.setting

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import posidon.android.conveniencelib.dp
import posidon.android.conveniencelib.sp
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.view.FontFitTextView
import posidon.launcher.view.Seekbar

class NumberBarSettingView : IntSettingView {

    private lateinit var seekBar: Seekbar
    private lateinit var textIcon: TextView

    constructor(c: Context) : super(c)
    constructor(c: Context, a: AttributeSet) : super(c, a)
    constructor(c: Context, a: AttributeSet, sa: Int) : super(c, a, sa)
    constructor(c: Context, a: AttributeSet, sa: Int, sr: Int) : super(c, a, sa, sr)

    override val doSpecialIcon get() = true

    override fun populateIcon(a: TypedArray) {
        textIcon = FontFitTextView(context).apply {
            layoutParams = LayoutParams(dp(48).toInt(), ViewGroup.LayoutParams.MATCH_PARENT)
            gravity = Gravity.CENTER
            defaultTextSize = sp(28f)
            val p = dp(8).toInt()
            setPadding(p, 0, p, 0)
            setTextColor(Global.getPastelAccent())
            typeface = resources.getFont(R.font.rubik_medium_caps)
        }
        addView(textIcon)
    }

    override fun populate(attrs: AttributeSet?, defStyle: Int, defStyleRes: Int) {

        val a = context.obtainStyledAttributes(attrs, R.styleable.NumberBarSettingView, defStyle, defStyleRes)

        labelView.layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(60).toInt())

        val startsWith1 = a.getBoolean(R.styleable.NumberBarSettingView_startsWith1, false)
        val isFloat = a.getBoolean(R.styleable.NumberBarSettingView_isFloat, false)

        seekBar = Seekbar(context).apply {
            layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                this.gravity = Gravity.CENTER_VERTICAL
            }
            run {
                var m = a.getInt(R.styleable.NumberBarSettingView_max, 0)
                if (startsWith1) m--
                max = m
            }
            run {
                var p = if (isFloat) Settings[key, default.toFloat()].toInt() else Settings[key, default]
                textIcon.text = p.toString()
                if (startsWith1) p--
                progress = p
            }
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStartTrackingTouch(s: SeekBar) {}
                override fun onStopTrackingTouch(s: SeekBar) {}
                override fun onProgressChanged(s: SeekBar, progress: Int, isUser: Boolean) {
                    var p = progress
                    if (startsWith1) p++
                    if (isFloat) Settings[key] = p.toFloat() else Settings[key] = p
                    textIcon.text = p.toString()
                    Global.customized = true
                }
            })
        }
        addView(seekBar)
        a.recycle()
    }
}