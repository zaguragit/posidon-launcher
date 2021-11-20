package posidon.launcher.customizations.settingScreens

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.drawable.FastColorDrawable
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.theme.applyFontSetting
import posidon.launcher.view.Spinner

class CustomNotifications : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_notifications)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) window.setDecorFitsSystemWindows(false)
        else window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)
        window.setBackgroundDrawable(FastColorDrawable(Global.getBlackAccent()))

        TextViewCompat.setCompoundDrawableTintList(findViewById(R.id.grouping_label), ColorStateList.valueOf(Global.getPastelAccent()))

        findViewById<Spinner>(R.id.notificationGrouping).run {
            data = resources.getStringArray(R.array.notificationGrouping)
            selectionI = when (Settings["notifications:groupingType", "os"]) {
                "os" -> 0; "byApp" -> 1; else -> 2
            }
        }

        Global.customized = true
    }

    fun openHideApps(v: View) = startActivity(Intent(this, CustomHiddenAppNotifications::class.java))

    override fun onPause() {
        Global.customized = true
        Settings.apply {
            putNotSave("notifications:groupingType", when (findViewById<Spinner>(R.id.notificationGrouping).selectionI) { 0 -> "os"; 1 -> "byApp"; else -> "none" })
            apply()
        }
        super.onPause()
    }
}