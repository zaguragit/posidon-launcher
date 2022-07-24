package posidon.launcher.customizations.settingScreens

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.view.setting.*

class CustomNotifications : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings.init(applicationContext)
        configureWindowForSettings()
        setSettingsContentView(R.string.notifications) {
            card {
                title(R.string.cards)
                color(
                    R.string.background,
                    R.drawable.ic_color,
                    "notificationbgcolor",
                    0xFFFFFFFF.toInt(),
                )
                switch(
                    R.string.collapse_notifications,
                    R.drawable.ic_arrow_up,
                    "collapseNotifications",
                    default = false,
                )
                spinner(
                    R.string.grouping,
                    R.drawable.ic_apps,
                    "notifications:groupingType",
                    0,
                    R.array.notificationGrouping,
                )
                color(
                    R.string.swipe_bg_color,
                    R.drawable.ic_color,
                    "notif:card_swipe_bg_color",
                    0x880d0e0f.toInt(),
                )
            }
            card {
                title(R.string.text)
                color(
                    R.string.title_color,
                    R.drawable.ic_color,
                    "notificationtitlecolor",
                    0xFF111213.toInt(),
                )
                color(
                    R.string.text_color,
                    R.drawable.ic_color,
                    "notificationtxtcolor",
                    0xFF252627.toInt(),
                )
                numberSeekBar(
                    labelId = R.string.max_lines,
                    key = "notif:text:max_lines",
                    default = 3,
                    max = 24,
                    startsWith1 = true,
                )
            }
            card {
                switchTitle(R.string.action_buttons, "notificationActionsEnabled", false)
                numberSeekBar(
                    labelId = R.string.radius,
                    key = "notif:actions:radius",
                    default = 24,
                    max = 50,
                )
                color(
                    R.string.background,
                    R.drawable.ic_color,
                    "notificationActionBGColor",
                    0x88e0e0e0.toInt(),
                )
                color(
                    R.string.text_color,
                    R.drawable.ic_color,
                    "notificationActionTextColor",
                    0xFF252627.toInt(),
                )
            }
            card {
                switchTitle(R.string.notification_badges, "notif:badges", true)
                switch(
                    R.string.show_number,
                    R.drawable.ic_label,
                    "notif:badges:show_num",
                    default = true,
                )
                spinner(
                    R.string.background_type,
                    R.drawable.ic_color_dropper,
                    "notif:badges:bg_type",
                    0,
                    R.array.notifBadgesBGs,
                )
                color(
                    R.string.background,
                    R.drawable.ic_color,
                    "notif:badges:bg_color",
                    0xffff5555.toInt(),
                )
            }
            card {
                clickable(R.string.hidden_apps, R.drawable.ic_visible, ::openHideApps)
                switch(
                    R.string.hide_persistent_notifications,
                    R.drawable.ic_visible,
                    "notif:hide_persistent",
                    default = false,
                )
            }
        }
        Global.customized = true
    }

    fun openHideApps(v: View) = startActivity(Intent(this, CustomHiddenAppNotifications::class.java))
}