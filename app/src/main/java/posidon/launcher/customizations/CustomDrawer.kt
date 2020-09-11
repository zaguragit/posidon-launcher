package posidon.launcher.customizations

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Global
import posidon.launcher.Home
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.applyFontSetting
import posidon.launcher.view.setting.SwitchSettingView

class CustomDrawer : AppCompatActivity() {

    private var icsize: SeekBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_drawer)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)

        icsize = findViewById(R.id.iconsizeslider)
        icsize!!.progress = Settings["icsize", 1]

        findViewById<SwitchSettingView>(R.id.scrollbarEnabledSetting).run {
            onCheckedChange = {
                Home.setDrawerScrollbarEnabled(it)
            }
        }
    }

    override fun onPause() {
        Settings.apply {
            putNotSave("icsize", icsize!!.progress)
            apply()
        }
        Global.shouldSetApps = true
        Global.customized = true
        super.onPause()
    }
}