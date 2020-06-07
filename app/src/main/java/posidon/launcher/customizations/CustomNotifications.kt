package posidon.launcher.customizations

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.*
import posidon.launcher.view.Spinner
import posidon.launcher.view.Switch

class CustomNotifications : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_notifications)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)

        findViewById<Switch>(R.id.notificationsEnabled).isChecked = Settings["notif:enabled", true]

        findViewById<View>(R.id.notificationtitlecolorprev).background = ColorTools.colorCircle(Settings["notificationtitlecolor", -0xeeeded])
        findViewById<View>(R.id.notificationtxtcolorprev).background = ColorTools.colorCircle(Settings["notificationtxtcolor", -0xdad9d9])
        findViewById<View>(R.id.notificationbgprev).background = ColorTools.colorCircle(Settings["notificationbgcolor", -0x1])

        findViewById<Switch>(R.id.actionButtonSwitch).isChecked = Settings["notificationActionsEnabled", false]
        findViewById<Switch>(R.id.collapseNotificationSwitch).isChecked = Settings["collapseNotifications", false]
        findViewById<View>(R.id.actionBGPreview).background = ColorTools.colorCircle(Settings["notificationActionTextColor", 0x88e0e0e0.toInt()])
        findViewById<View>(R.id.actionTextColorPreview).background = ColorTools.colorCircle(Settings["notificationActionTextColor", -0xdad9d9])

        findViewById<Spinner>(R.id.notificationGrouping).data = resources.getStringArray(R.array.notificationGrouping)
        findViewById<Spinner>(R.id.notificationGrouping).selectionI = when (Settings["notifications:groupingType", "os"]) { "os" -> 0; "byApp" -> 1; else -> 2 }

        findViewById<Switch>(R.id.badgesEnabled).isChecked = Settings["notif:badges", true]

        Main.customized = true
    }

    fun picknotificationtitlecolor(v: View) = ColorTools.pickColor(this, Settings["notificationtitlecolor", -0xeeeded]) {
        v as ViewGroup
        v.getChildAt(1).background = ColorTools.colorCircle(it)
        Settings["notificationtitlecolor"] = it
    }

    fun picknotificationtxtcolor(v: View) = ColorTools.pickColor(this, Settings["notificationtxtcolor", -0xdad9d9]) {
        v as ViewGroup
        v.getChildAt(1).background = ColorTools.colorCircle(it)
        Settings["notificationtxtcolor"] = it
    }

    fun picknotificationcolor(v: View) = ColorTools.pickColor(this, Settings["notificationbgcolor", -0x1]) {
        v as ViewGroup
        v.getChildAt(1).background = ColorTools.colorCircle(it)
        Settings["notificationbgcolor"] = it
    }

    fun pickNotificationActionBGColor(v: View) = ColorTools.pickColor(this, Settings["notificationActionBGColor", 0x88e0e0e0.toInt()]) {
        v as ViewGroup
        v.getChildAt(1).background = ColorTools.colorCircle(it)
        Settings["notificationActionBGColor"] = it
    }

    fun pickNotificationActionTextColor(v: View) = ColorTools.pickColor(this, Settings["notificationActionTextColor", -0xdad9d9]) {
        v as ViewGroup
        v.getChildAt(1).background = ColorTools.colorCircle(it)
        Settings["notificationActionTextColor"] = it
    }

    fun openHideApps(v: View) = startActivity(Intent(this, CustomHiddenAppNotifications::class.java))

    override fun onPause() {
        Main.customized = true
        Settings.apply {
            putNotSave("notif:enabled", findViewById<Switch>(R.id.notificationsEnabled).isChecked)
            putNotSave("notificationActionsEnabled", findViewById<Switch>(R.id.actionButtonSwitch).isChecked)
            putNotSave("collapseNotifications", findViewById<Switch>(R.id.collapseNotificationSwitch).isChecked)
            putNotSave("notifications:groupingType", when (findViewById<Spinner>(R.id.notificationGrouping).selectionI) { 0 -> "os"; 1 -> "byApp"; else -> "none" })
            putNotSave("notif:badges", findViewById<Switch>(R.id.badgesEnabled).isChecked)
            apply()
        }
        super.onPause()
    }
}