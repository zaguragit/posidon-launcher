package posidon.launcher.customizations.settingScreens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.view.setting.*

class CustomFolders : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings.init(applicationContext)
        configureWindowForSettings()
        setSettingsContentView(R.string.settings_title_folders) {
            card {
                color(
                    labelId = R.string.background,
                    iconId = R.drawable.ic_color,
                    key = "folder:background_color",
                    default = 0xDD111213.toInt(),
                )
                numberSeekBar(
                    labelId = R.string.columns,
                    key = "folder:columns",
                    default = 3,
                    max = 7,
                    startsWith1 = true,
                )
            }
            card {
                title(R.string.window)
                color(
                    labelId = R.string.background,
                    iconId = R.drawable.ic_color,
                    key = "folder:window:background_color",
                    default = 0xDD111213.toInt(),
                )
                switch(
                    labelId = R.string.show_name,
                    iconId = R.drawable.ic_visible,
                    key = "folder:show_title",
                    default = true,
                )
                color(
                    labelId = R.string.name_color,
                    iconId = R.drawable.ic_color,
                    key = "folder:title_color",
                    default = 0xFFFFFFFF.toInt(),
                )
                numberSeekBar(
                    labelId = R.string.radius,
                    key = "folder:radius",
                    default = 18,
                    max = 30,
                )
            }
            labelSettings("folder:labels", 0xddfffffff.toInt(), 12)
        }
        Global.customized = true
    }
}