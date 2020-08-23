package posidon.launcher.customizations

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.applyFontSetting
import posidon.launcher.view.Spinner
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
                Main.setDrawerScrollbarEnabled(it)
            }
        }

        findViewById<Spinner>(R.id.sortingOptions).data = resources.getStringArray(R.array.sortingAlgorithms)
        findViewById<Spinner>(R.id.sortingOptions).selectionI = Settings["drawer:sorting", 0]

        findViewById<Switch>(R.id.blurswitch).isChecked = Settings["drawer:blur", true]

        findViewById<Spinner>(R.id.sectionLetter).data = resources.getStringArray(R.array.namePositions)
        findViewById<Spinner>(R.id.sectionLetter).selectionI = Settings["drawer:sec_name_pos", 0]

        findViewById<Switch>(R.id.sectionsEnabled).isChecked = Settings["drawer:sections_enabled", false]

    }

    override fun onPause() {
        Settings.apply {
            putNotSave("icsize", icsize!!.progress)
            if (get("drawer:sections_enabled", false) != findViewById<Switch>(R.id.sectionsEnabled).isChecked) {
                putNotSave("drawer:sections_enabled", findViewById<Switch>(R.id.sectionsEnabled).isChecked)
                Main.shouldSetApps = true
                Main.customized = true
            }
            if (get("drawer:sorting", 0) != findViewById<Spinner>(R.id.sortingOptions).selectionI) {
                putNotSave("drawer:sorting", findViewById<Spinner>(R.id.sortingOptions).selectionI)
                Main.shouldSetApps = true
                Main.customized = true
            }
            if (get("drawer:sec_name_pos", 0) != findViewById<Spinner>(R.id.sectionLetter).selectionI) {
                putNotSave("drawer:sec_name_pos", findViewById<Spinner>(R.id.sectionLetter).selectionI)
                Main.shouldSetApps = true
                Main.customized = true
            }
            putNotSave("drawer:blur", findViewById<Switch>(R.id.blurswitch).isChecked)
            apply()
        }
        super.onPause()
    }
}