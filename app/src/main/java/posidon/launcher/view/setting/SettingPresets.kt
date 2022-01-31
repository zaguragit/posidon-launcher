package posidon.launcher.view.setting

import posidon.launcher.R

fun SettingsViewScope.labelSettings(namespace: String, defaultColor: Int, defaultTextSize: Int) = card {
    switchTitle(
        labelId = R.string.app_labels,
        key = "$namespace:enabled",
        default = true,
    )
    color(
        labelId = R.string.color,
        iconId = R.drawable.ic_color,
        key = "$namespace:color",
        default = defaultColor,
    )
    numberSeekBar(
        labelId = R.string.text_size,
        key = "$namespace:text_size",
        default = defaultTextSize,
        max = 32,
        startsWith1 = true,
    )
    numberSeekBar(
        labelId = R.string.max_lines,
        key = "$namespace:max_lines",
        default = 1,
        max = 3,
        startsWith1 = true,
    )
}