package posidon.launcher.customizations

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.applyFontSetting
import posidon.launcher.view.Spinner


class CustomDock : AppCompatActivity() {

    private var icsize: SeekBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_dock)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)

        findViewById<Spinner>(R.id.animationOptions).data = resources.getStringArray(R.array.bgModes)
        findViewById<Spinner>(R.id.animationOptions).selectionI = Settings["dock:background_type", 0]

        icsize = findViewById(R.id.dockiconsizeslider)
        icsize!!.progress = Settings["dockicsize", 1]

        Main.customized = true
    }

    override fun onPause() {
        Main.customized = true
        Settings.apply {
            putNotSave("dock:background_type", findViewById<Spinner>(R.id.animationOptions).selectionI)
            putNotSave("dockicsize", icsize!!.progress)
            apply()
        }
        super.onPause()
    }
}