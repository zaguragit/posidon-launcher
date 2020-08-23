package posidon.launcher.customizations

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.applyFontSetting

class CustomSearch : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_search)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)

        findViewById<TextView>(R.id.hinttxt).text = Settings["searchhinttxt", "Search.."]
        findViewById<SeekBar>(R.id.iconSizeSlider).progress = Settings["search:ic_size", 0]
        findViewById<Switch>(R.id.blurswitch).isChecked = Settings["search:blur", true]
    }

    override fun onPause() {
        Main.setSearchHintText(findViewById<TextView>(R.id.hinttxt).text.toString())
        Settings.apply {
            putNotSave("search:ic_size", findViewById<SeekBar>(R.id.iconSizeSlider).progress)
            putNotSave("search:blur", findViewById<Switch>(R.id.blurswitch).isChecked)
            apply()
        }
        super.onPause()
    }
}