package posidon.launcher.view.setting

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import posidon.launcher.R
import posidon.launcher.tools.dp

abstract class IntSettingView : SettingView {

    protected var default = 0

    constructor(c: Context) : super(c)
    constructor(c: Context, a: AttributeSet) : super(c, a)
    constructor(c: Context, a: AttributeSet, sa: Int) : super(c, a, sa)
    constructor(c: Context, a: AttributeSet, sa: Int, sr: Int) : super(c, a, sa, sr)

    override fun init(attrs: AttributeSet?, defStyle: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SettingView, defStyle, 0)
        default = a.getInt(R.styleable.SettingView_def, 0)
        super.init(attrs, defStyle)
    }
}
