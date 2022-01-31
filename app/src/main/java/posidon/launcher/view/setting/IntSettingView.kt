package posidon.launcher.view.setting

import android.content.Context
import android.util.AttributeSet
import posidon.launcher.R

abstract class IntSettingView : SettingView {

    var default = 0

    constructor(c: Context, a: AttributeSet? = null, sa: Int = 0, sr: Int = 0) : super(c, a, sa, sr)

    constructor(c: Context, key: String, default: Int, labelId: Int, iconId: Int) : super(c, key, labelId, iconId) {
        this.default = default
    }

    override fun init(attrs: AttributeSet?, defStyle: Int, defStyleRes: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SettingView, defStyle, defStyleRes)
        default = a.getInt(R.styleable.SettingView_def, 0)
        super.init(attrs, defStyle, defStyleRes)
        a.recycle()
    }
}
