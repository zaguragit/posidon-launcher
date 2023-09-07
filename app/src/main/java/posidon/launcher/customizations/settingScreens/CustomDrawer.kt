package posidon.launcher.customizations.settingScreens

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.view.setting.*

class CustomDrawer : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings.init(applicationContext)
        configureWindowForSettings()
        setSettingsContentView(R.string.settings_title_drawer) {
            card {
                color(
                    labelId = R.string.background,
                    iconId = R.drawable.ic_color,
                    key = "drawer:background_color",
                    default = 0xa4171717.toInt(),
                )
                numberSeekBar(
                    labelId = R.string.columns,
                    key = "drawer:columns",
                    default = 5,
                    max = 7,
                    startsWith1 = true,
                )
                numberSeekBar(
                    labelId = R.string.iconSize,
                    key = "drawer:icons:size",
                    default = 64,
                    max = 96,
                    startsWith1 = true,
                )
                numberSeekBar(
                    labelId = R.string.spacing,
                    key = "verticalspacing",
                    default = 12,
                    max = 48,
                )
                spinner(
                    labelId = R.string.sorting,
                    iconId = R.drawable.ic_sorting,
                    key = "drawer:sorting",
                    default = 0,
                    array = R.array.sortingAlgorithms
                )
                switch(
                    labelId = R.string.slide_up,
                    iconId = R.drawable.ic_arrow_up,
                    key = "drawer:slide_up",
                    default = true,
                )
                clickable(
                    labelId = R.string.setting_title_hide_apps,
                    iconId = R.drawable.ic_visible,
                ) {
                    it.context.startActivity(Intent(it.context, CustomHiddenApps::class.java))
                }
            }
            labelSettings("drawer:labels", true, 0x70ffffff, 12)
            card {
                switchTitle(
                    labelId = R.string.scrollbar,
                    key = "drawer:scrollbar:enabled",
                    default = false,
                )
                switch(
                    labelId = R.string.show_outside,
                    iconId = R.drawable.ic_home,
                    key = "drawer:scrollbar:show_outside",
                    default = false,
                )
                color(
                    labelId = R.string.text_color,
                    iconId = R.drawable.ic_color,
                    key = "drawer:scrollbar:text_color",
                    default = 0xaaeeeeee.toInt(),
                )
                color(
                    labelId = R.string.accent_color,
                    iconId = R.drawable.ic_color,
                    key = "drawer:scrollbar:highlight_color",
                    default = 0xffffffff.toInt(),
                )
                color(
                    labelId = R.string.floating_color,
                    iconId = R.drawable.ic_color,
                    key = "drawer:scrollbar:floating_color",
                    default = 0xffffffff.toInt(),
                )
                color(
                    labelId = R.string.background,
                    iconId = R.drawable.ic_color,
                    key = "drawer:scrollbar:bg_color",
                    default = 0x00000000,
                )
                numberSeekBar(
                    labelId = R.string.width,
                    key = "drawer:scrollbar:width",
                    default = 24,
                    max = 64,
                )
                switch(
                    labelId = R.string.reserve_space,
                    iconId = R.drawable.ic_sections,
                    key = "drawer:scrollbar:reserve_space",
                    default = true,
                )
                spinner(
                    labelId = R.string.position,
                    iconId = R.drawable.ic_apps,
                    key = "drawer:scrollbar:position",
                    default = 1,
                    array = R.array.scrollBarPosition,
                )
            }
            card {
                switchTitle(
                    labelId = R.string.sections,
                    key = "drawer:sections:enabled",
                    default = false,
                )
                spinner(
                    labelId = R.string.name_position,
                    iconId = R.drawable.ic_label,
                    key = "drawer:sections:name:position",
                    default = 0,
                    array = R.array.namePositions,
                )
            }
        }
        Global.shouldSetApps = true
        Global.customized = true
    }
}