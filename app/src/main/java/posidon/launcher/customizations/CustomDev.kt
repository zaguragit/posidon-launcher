package posidon.launcher.customizations

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.applyFontSetting


class CustomDev : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_dev)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)
        findViewById<Switch>(R.id.showcomponent).isChecked = Settings["dev:show_app_component", false]
        findViewById<Switch>(R.id.consoleEnabled).isChecked = Settings["dev:console", false]
        Main.customized = true
    }

    override fun onPause() {
        super.onPause()
        Settings["dev:show_app_component"] = findViewById<Switch>(R.id.showcomponent).isChecked
        Settings["dev:console"] = findViewById<Switch>(R.id.consoleEnabled).isChecked
    }
}