package posidon.launcher.customizations

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Home
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.applyFontSetting
import posidon.launcher.view.Spinner

class CustomNotifications : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_notifications)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)

        findViewById<Spinner>(R.id.notificationGrouping).data = resources.getStringArray(R.array.notificationGrouping)
        findViewById<Spinner>(R.id.notificationGrouping).selectionI = when (Settings["notifications:groupingType", "os"]) { "os" -> 0; "byApp" -> 1; else -> 2 }

        Home.customized = true
    }

    fun openHideApps(v: View) = startActivity(Intent(this, CustomHiddenAppNotifications::class.java))

    override fun onPause() {
        Home.customized = true
        Settings.apply {
            putNotSave("notifications:groupingType", when (findViewById<Spinner>(R.id.notificationGrouping).selectionI) { 0 -> "os"; 1 -> "byApp"; else -> "none" })
            apply()
        }
        super.onPause()
    }
}