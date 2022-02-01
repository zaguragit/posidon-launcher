package posidon.launcher.customizations.settingScreens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.view.setting.*

class CustomDock : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings.init(applicationContext)
        configureWindowForSettings()
        setSettingsContentView(R.string.settings_title_dock) {
            card {
                numberSeekBar(
                    labelId = R.string.columns,
                    key = "dock:columns",
                    default = 5,
                    max = 7,
                    startsWith1 = true,
                )
                numberSeekBar(
                    labelId = R.string.settings_rows,
                    key = "dock:rows",
                    default = 1,
                    max = 5,
                    startsWith1 = true,
                )
                numberSeekBar(
                    labelId = R.string.iconSize,
                    key = "dock:icons:size",
                    default = 74,
                    max = 96,
                    startsWith1 = true,
                )
            }
            card {
                color(
                    labelId = R.string.background,
                    iconId = R.drawable.ic_color,
                    key = "dock:background_color",
                    default = 0xbe080808.toInt(),
                )
                spinner(
                    labelId = R.string.background_type,
                    iconId = R.drawable.ic_shapes,
                    key = "dock:background_type",
                    default = 1,
                    array = R.array.bgModes
                )
                numberSeekBar(
                    labelId = R.string.radius,
                    key = "dock:radius",
                    default = 30,
                    max = 50,
                )
                numberSeekBar(
                    labelId = R.string.bottom_padding,
                    key = "dock:bottom_padding",
                    default = 10,
                    max = 30,
                )
                numberSeekBar(
                    labelId = R.string.horizontal_margin,
                    key = "dock:margin_x",
                    default = 16,
                    max = 32,
                )
            }
            labelSettings("dock:labels", false, 0xEEEEEEEE.toInt(), 12)
        }
        Global.customized = true
    }
}