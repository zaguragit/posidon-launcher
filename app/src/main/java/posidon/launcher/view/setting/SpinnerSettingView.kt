package posidon.launcher.view.setting

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.dp
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
            textSize = 17f
            setTextColor(context.resources.getColor(R.color.cardtxt))
            gravity = Gravity.START or Gravity.CENTER_VERTICAL

            val h = 8.dp.toInt()
            setPadding(h, 0, h, 0)

            layoutParams = LayoutParams(WRAP_CONTENT, 60.dp.toInt())

            setSelectionChangedListener {
                Settings[key] = it.selectionI
            }
        }
        addView(spinner)
    }
}