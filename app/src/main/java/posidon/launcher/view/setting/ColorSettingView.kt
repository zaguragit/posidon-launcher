package posidon.launcher.view.setting

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.core.widget.TextViewCompat
import posidon.android.conveniencelib.dp
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.theme.ColorTools
import kotlin.math.max
import kotlin.math.min

class ColorSettingView : IntSettingView {

    private lateinit var colorPreview: View
    private var hasAlpha = true

    constructor(c: Context) : super(c)
    constructor(c: Context, a: AttributeSet) : this(c, a, 0)
    constructor(c: Context, a: AttributeSet, sa: Int) : this(c, a, sa, 0)
    constructor(c: Context, attrs: AttributeSet, sa: Int, sr: Int) : super(c, attrs, sa, sr) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ColorSettingView, sa, sr)
        hasAlpha = a.getBoolean(R.styleable.ColorSettingView_hasAlpha, true)
        a.recycle()
        run {
            val d = Settings[key, default]
            setPreviewColor(if (hasAlpha) d else d or -0x1000000)
        }
    }

    constructor(c: Context, key: String, default: Int, labelId: Int, iconId: Int) : super(c, key, default, labelId, iconId) {
        run {
            val d = Settings[key, default]
            setPreviewColor(if (hasAlpha) d else d or -0x1000000)
        }
    }

    fun setPreviewColor(it: Int) {
        colorPreview.background = ColorTools.colorPreview(it)
        val hsv = FloatArray(3)
        Color.colorToHSV(it, hsv)
        hsv[1] = min(hsv[1],0.5f)
        hsv[2] = min(max(0.4f, hsv[2]), 0.75f)
        val pastel = Color.HSVToColor(hsv)
        TextViewCompat.setCompoundDrawableTintList(labelView, ColorStateList.valueOf(pastel))
    }

    var onSelected: ((color: Int) -> Unit)? = null

    override fun populate(attrs: AttributeSet?, defStyle: Int, defStyleRes: Int) {

        colorPreview = View(context)
        val size = dp(36).toInt()
        addView(colorPreview, LayoutParams(size, size, 0f).apply {
            val m = dp(12).toInt()
            setMargins(m, m, m, m)
        })
        setOnClickListener {
            val c = Settings[key, default].let { if (hasAlpha) it else it and 0xffffff }
            (if (hasAlpha)
                ColorTools::pickColor
            else ColorTools::pickColorNoAlpha)(context, c) {
                val color = if (hasAlpha) it else it or -0x1000000
                setPreviewColor(color)
                Settings[key] = color
                onSelected?.invoke(color)
            }
        }
    }
}