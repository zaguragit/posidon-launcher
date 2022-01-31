package posidon.launcher.customizations.settingScreens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.view.setting.card
import posidon.launcher.view.setting.configureWindowForSettings
import posidon.launcher.view.setting.setSettingsContentView
import posidon.launcher.view.setting.switch

class CustomDev : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings.init(applicationContext)
        configureWindowForSettings()
        setSettingsContentView(R.string.settings_title_dev) {
            card {
                switch(
                    labelId = R.string.setting_show_component,
                    iconId = R.drawable.ic_visible,
                    key = "dev:show_app_component",
                    default = false,
                )
                switch(
                    labelId = R.string.console,
                    iconId = R.drawable.ic_label,
                    key = "dev:console",
                    default = false,
                )
                switch(
                    labelId = R.string.hide_crash_logs,
                    iconId = R.drawable.ic_label,
                    key = "dev:hide_crash_logs",
                    default = true,
                )
            }
        }
        Global.customized = true
    }
}