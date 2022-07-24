package posidon.launcher.view.setting

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import io.posidon.android.conveniencelib.units.dp
import io.posidon.android.conveniencelib.units.sp
import io.posidon.android.conveniencelib.units.toFloatPixels
import io.posidon.android.conveniencelib.units.toPixels
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.vibrate
import posidon.launcher.view.FontFitTextView
import posidon.launcher.view.Seekbar

class NumberBarSettingView : IntSettingView {

    private lateinit var seekBar: Seekbar
    private lateinit var textIcon: TextView

    private var doVibrate = false

    constructor(c: Context) : super(c)
    constructor(c: Context, a: AttributeSet) : this(c, a, 0, 0)
    constructor(c: Context, a: AttributeSet, sa: Int) : this(c, a, sa, 0)
    constructor(c: Context, attrs: AttributeSet, sa: Int, sr: Int) : super(c, attrs, sa, sr) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.NumberBarSettingView, sa, sr)
        isFloat = a.getBoolean(R.styleable.NumberBarSettingView_isFloat, false)
        startsWith1 = a.getBoolean(R.styleable.NumberBarSettingView_startsWith1, false)
        max = a.getInt(R.styleable.NumberBarSettingView_max, 0)
        value = if (isFloat) Settings[key, default.toFloat()].toInt() else Settings[key, default]
        a.recycle()
        doVibrate = true
    }

    constructor(c: Context, key: String, default: Int, labelId: Int, startsWith1: Boolean, max: Int) : super(c, key, default, labelId, 0) {
        this.startsWith1 = startsWith1
        this.value = if (isFloat) Settings[key, default.toFloat()].toInt() else Settings[key, default]
        this.max = max
        doVibrate = true
    }

    override val doSpecialIcon get() = true

    var value: Int
        get() = seekBar.progress + if (startsWith1) 1 else 0
        set(value) {
            seekBar.progress = value - if (startsWith1) 1 else 0
        }
    var max: Int
        get() = seekBar.max + if (startsWith1) 1 else 0
        set(value) {
            textIcon.text = value.toString()
            seekBar.max = value - if (startsWith1) 1 else 0
        }

    var startsWith1 = false
    var isFloat = false

    override fun populateIcon() {
        textIcon = FontFitTextView(context).apply {
            layoutParams = LayoutParams(48.dp.toPixels(context), ViewGroup.LayoutParams.MATCH_PARENT)
            gravity = Gravity.CENTER
            defaultTextSize = 28.sp.toFloatPixels(context)
            val p = 8.dp.toPixels(context)
            setPadding(p, 0, p, 0)
            setTextColor(Global.getPastelAccent())
            typeface = resources.getFont(R.font.rubik_medium_caps)
        }
        addView(textIcon)
    }

    override fun populate(attrs: AttributeSet?, defStyle: Int, defStyleRes: Int) {
        labelView.layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 60.dp.toPixels(context))
        seekBar = Seekbar(context)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(s: SeekBar) {}
            override fun onStopTrackingTouch(s: SeekBar) {}
            override fun onProgressChanged(s: SeekBar, progress: Int, isUser: Boolean) {
                if (doVibrate) context.vibrate()
                var p = progress
                if (startsWith1) p++
                if (isFloat) Settings[key] = p.toFloat() else Settings[key] = p
                textIcon.text = p.toString()
                Global.customized = true
                onValueChangeListener?.invoke(p)
            }
        })
        addView(seekBar, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            this.gravity = Gravity.CENTER_VERTICAL
        })
    }

    private var onValueChangeListener: ((Int) -> Unit)? = null

    fun setOnValueChangeListener(listener: ((Int) -> Unit)?) {
        onValueChangeListener = listener
    }
}