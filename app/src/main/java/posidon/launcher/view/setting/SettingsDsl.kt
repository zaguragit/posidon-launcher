package posidon.launcher.view.setting

import android.app.Activity
import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ArrayRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.setMargins
import androidx.core.widget.NestedScrollView
import posidon.android.conveniencelib.dp
import posidon.android.conveniencelib.getStatusBarHeight
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools

@JvmInline
value class SettingsViewScope(val viewGroup: ViewGroup)

inline fun Activity.setSettingsContentView(@StringRes titleId: Int, builder: SettingsViewScope.() -> Unit) {
    setContentView(NestedScrollView(this).apply {
        setPadding(0, getStatusBarHeight(), 0, Tools.navbarHeight)
        isVerticalFadingEdgeEnabled = true
        setFadingEdgeLength(dp(64).toInt())
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(context).apply {
                setText(titleId)
                gravity = Gravity.CENTER
                setTextColor(Global.getForeground())
                textSize = 32f
            }, ViewGroup.LayoutParams(MATCH_PARENT, dp(256).toInt()))
            builder(SettingsViewScope(this))
        }, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
    })
}

inline fun SettingsViewScope.card(builder: SettingsViewScope.() -> Unit) {
    viewGroup.addView(LinearLayout(viewGroup.context).apply {
        orientation = LinearLayout.VERTICAL
        background = context.getDrawable(R.drawable.settings_card)
        val vp = dp(12).toInt()
        setPadding(0, vp, 0, vp)
        clipToPadding = false
        builder(SettingsViewScope(this))
    }, ViewGroup.MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
        setMargins(viewGroup.dp(8).toInt())
    })
}

fun SettingsViewScope.numberSeekBar(
    @StringRes
    labelId: Int,
    key: String,
    default: Int,
    max: Int,
    startsWith1: Boolean = false,
) {
    viewGroup.addView(NumberBarSettingView(viewGroup.context, key, default, labelId).apply {
        this.startsWith1 = startsWith1
        this.value = if (isFloat) Settings[key, default.toFloat()].toInt() else Settings[key, default]
        this.max = max
    }, settingsEntryLayoutParams())
}

fun SettingsViewScope.switch(
    @StringRes
    labelId: Int,
    @DrawableRes
    iconId: Int,
    key: String,
    default: Boolean,
) = viewGroup.addView(
    SwitchSettingView(viewGroup.context, key, default, labelId, iconId),
    settingsEntryLayoutParams()
)

fun SettingsViewScope.color(
    @StringRes
    labelId: Int,
    @DrawableRes
    iconId: Int = R.drawable.ic_color,
    key: String,
    @ColorInt
    default: Int,
) = viewGroup.addView(
    ColorSettingView(viewGroup.context, key, default, labelId, iconId),
    settingsEntryLayoutParams()
)

fun SettingsViewScope.spinner(
    @StringRes
    labelId: Int,
    @DrawableRes
    iconId: Int,
    key: String,
    default: Int,
    @ArrayRes
    array: Int
) {
    viewGroup.addView(SpinnerSettingView(viewGroup.context, key, default, labelId, iconId).apply {
        this.array = resources.getStringArray(array)
        selectionI = Settings[key, default]
    }, settingsEntryLayoutParams())
}

fun SettingsViewScope.switchTitle(
    @StringRes
    labelId: Int,
    key: String,
    default: Boolean,
) {
    viewGroup.addView(HeaderSwitchSettingView(viewGroup.context).apply {
        this.label = context.getString(labelId)
        this.value = Settings[key, default]
        this.key = key
    }, settingsTitleLayoutParams())
}

fun SettingsViewScope.title(
    @StringRes
    labelId: Int,
) {
    viewGroup.addView(HeaderSettingView(viewGroup.context).apply {
        this.label = context.getString(labelId)
    }, settingsTitleLayoutParams())
}

private fun SettingsViewScope.settingsEntryLayoutParams() = ViewGroup.MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
    leftMargin = viewGroup.dp(12).toInt()
    rightMargin = viewGroup.dp(12).toInt()
}

private fun SettingsViewScope.settingsTitleLayoutParams() = ViewGroup.MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
    topMargin = -viewGroup.dp(12).toInt()
    bottomMargin = viewGroup.dp(12).toInt()
}