package posidon.launcher.view.setting

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Gestures
import posidon.launcher.tools.Tools
import posidon.launcher.tools.dp
import posidon.launcher.view.Spinner

class ActionSelectorSettingView : IntSettingView {

    private lateinit var spinner: Spinner

    constructor(c: Context) : super(c)
    constructor(c: Context, a: AttributeSet) : super(c, a)
    constructor(c: Context, a: AttributeSet, sa: Int) : super(c, a, sa)
    constructor(c: Context, a: AttributeSet, sa: Int, sr: Int) : super(c, a, sa, sr)

    override fun populate(attrs: AttributeSet?, defStyle: Int, defStyleRes: Int) {

        val array = context.resources.getStringArray(R.array.actions)
        val openAppI = Gestures.getIndex(Gestures.OPEN_APP)

        spinner = Spinner(context).apply {

            data = array
            val action = Settings[key, Gestures.getKey(default)]
            selectionI = Gestures.getIndex(action)

            if (selectionI == openAppI) {
                val string = action.substring(Gestures.OPEN_APP.length + 1)
                App[string]?.run {
                    text = array[openAppI] + " ($label)"
                }
            }

            includeFontPadding = false
            textSize = 17f
            setTextColor(context.resources.getColor(R.color.cardtxt))
            gravity = Gravity.START or Gravity.CENTER_VERTICAL

            val h = 8.dp.toInt()
            setPadding(h, 0, h, 0)

            layoutParams = LayoutParams(WRAP_CONTENT, 60.dp.toInt())

            setSelectionChangedListener {
                if (it.selectionI == openAppI) {
                    if (Gestures.getIndex(action) == openAppI) {
                        val string = action.substring(Gestures.OPEN_APP.length + 1)
                        App[string]?.run {
                            text = array[openAppI] + " ($label)"
                        }
                    }
                    Tools.selectApp(context, includeHidden = true) { app ->
                        Settings[key] = Gestures.getKey(it.selectionI) + ":$app"
                        text = array[openAppI] + " (${app.label})"
                    }
                } else {
                    Settings[key] = Gestures.getKey(it.selectionI)
                }
            }
        }
        addView(spinner)
    }
}