package posidon.launcher.customizations.settingScreens

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.drawable.FastColorDrawable
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.theme.applyFontSetting

class CustomSearch : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_search)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) window.setDecorFitsSystemWindows(false)
        else window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)
        window.setBackgroundDrawable(FastColorDrawable(Global.getBlackAccent()))

        findViewById<TextView>(R.id.hinttxt).text = Settings["searchhinttxt", "Search.."]
        findViewById<SeekBar>(R.id.iconSizeSlider).progress = Settings["search:ic_size", 0]
        Global.customized = true
    }

    override fun onPause() {
        Settings["searchhinttxt"] = findViewById<TextView>(R.id.hinttxt).text.toString()
        Settings.apply {
            putNotSave("search:ic_size", findViewById<SeekBar>(R.id.iconSizeSlider).progress)
            apply()
        }
        super.onPause()
    }
}