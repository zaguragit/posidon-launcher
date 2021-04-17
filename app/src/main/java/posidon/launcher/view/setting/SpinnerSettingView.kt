package posidon.launcher.view.setting

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import posidon.android.conveniencelib.dp
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.view.Spinner

class SpinnerSettingView : IntSettingView {

    private lateinit var spinner: Spinner

    constructor(c: Context) : super(c)
    constructor(c: Context, a: AttributeSet) : super(c, a)
    constructor(c: Context, a: AttributeSet, sa: Int) : super(c, a, sa)
    constructor(c: Context, a: AttributeSet, sa: Int, sr: Int) : super(c, a, sa, sr)

    override fun populate(attrs: AttributeSet?, defStyle: Int, defStyleRes: Int) {

        val a = context.obtainStyledAttributes(attrs, R.styleable.SpinnerSettingView, defStyle, defStyleRes)
        val array = a.getTextArray(R.styleable.SpinnerSettingView_array)

        spinner = Spinner(context).apply {

            data = array
            selectionI = Settings[key, default]

            includeFontPadding = false
            textSize = 15f
            setTextColor(context.resources.getColor(R.color.cardspinnertxt))
            gravity = Gravity.START or Gravity.CENTER_VERTICAL

            val h = dp(8).toInt()
            setPadding(h, 0, h, 0)

            setSelectionChangedListener {
                Settings[key] = it.selectionI
            }
        }
        addView(spinner, LayoutParams(WRAP_CONTENT, dp(60).toInt()))
        a.recycle()
    }
}