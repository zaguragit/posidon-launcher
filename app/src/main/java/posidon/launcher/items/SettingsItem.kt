package posidon.launcher.items

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.provider.Settings
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.tools.Tools
import posidon.launcher.tools.toBitmap
import posidon.launcher.tools.toBitmapDrawable

class SettingsItem private constructor(
    override var label: String?,
    override var icon: Drawable?,
    val action: String
) : LauncherItem() {

    fun open() = try {
        Tools.publicContext!!.startActivity(Intent(action))
    } catch (e: Exception) {}

    companion object {
        fun getList(): Array<SettingsItem> {
            val posidonIcon = App.getJustPackage("posidon.launcher")!![0].icon!!.toBitmapDrawable(true)
            val settingsIcon = Tools.publicContext!!.getDrawable(R.drawable.ic_settings)!!.apply { setTintList(ColorStateList.valueOf(Main.accentColor)) }
            val searchIconSize = when (posidon.launcher.storage.Settings["search:ic_size", 0]) {
                0 -> 64; 2 -> 84; else -> 74
            }
            return arrayOf(
                SettingsItem(
                    Tools.publicContext!!.getString(R.string.airplane_mode),
                    Tools.badge(Tools.publicContext!!.getDrawable(R.drawable.ic_airplane)!!, settingsIcon, searchIconSize),
                    Settings.ACTION_AIRPLANE_MODE_SETTINGS),
                SettingsItem(
                    Tools.publicContext!!.getString(R.string.power_usage),
                    Tools.badge(Tools.publicContext!!.getDrawable(R.drawable.ic_power_usage)!!, settingsIcon, searchIconSize),
                    Intent.ACTION_POWER_USAGE_SUMMARY),
                SettingsItem(
                    Tools.publicContext!!.getString(R.string.wireless_title),
                    Tools.badge(Tools.publicContext!!.getDrawable(R.drawable.ic_wifi)!!, settingsIcon, searchIconSize),
                    Settings.ACTION_WIRELESS_SETTINGS),
                SettingsItem(
                    Tools.publicContext!!.getString(R.string.device_info),
                    Tools.badge(Tools.publicContext!!.getDrawable(R.drawable.ic_device)!!, settingsIcon, searchIconSize),
                    Settings.ACTION_DEVICE_INFO_SETTINGS),
                SettingsItem(
                    Tools.publicContext!!.getString(R.string.settings_title_drawer),
                    Tools.badge(Tools.publicContext!!.getDrawable(R.drawable.custom_drawer_icon)!!, posidonIcon, searchIconSize),
                    "posidon.launcher.settings.DRAWER"),
                SettingsItem(
                    Tools.publicContext!!.getString(R.string.settings_title_feed),
                    Tools.badge(Tools.publicContext!!.getDrawable(R.drawable.custom_feed_icon)!!, posidonIcon, searchIconSize),
                    "posidon.launcher.settings.FEED"),
                SettingsItem(
                    Tools.publicContext!!.getString(R.string.notifications),
                    Tools.badge(Tools.publicContext!!.getDrawable(R.drawable.custom_notifications_icon)!!, posidonIcon, searchIconSize),
                    "posidon.launcher.settings.NOTIFICATIONS"),
                SettingsItem(
                    Tools.publicContext!!.getString(R.string.settings_title_dock),
                    Tools.badge(Tools.publicContext!!.getDrawable(R.drawable.custom_dock_icon)!!, posidonIcon, searchIconSize),
                    "posidon.launcher.settings.DOCK"),
                SettingsItem(
                    Tools.publicContext!!.getString(R.string.settings_title_folders),
                    Tools.badge(Tools.publicContext!!.getDrawable(R.drawable.custom_folders_icon)!!, posidonIcon, searchIconSize),
                    "posidon.launcher.settings.FOLDERS"),
                SettingsItem(
                    Tools.publicContext!!.getString(R.string.settings_title_search),
                    Tools.badge(Tools.publicContext!!.getDrawable(R.drawable.custom_search_icon)!!, posidonIcon, searchIconSize),
                    "posidon.launcher.settings.SEARCH"),
                SettingsItem(
                    Tools.publicContext!!.getString(R.string.settings_title_theme),
                    Tools.badge(Tools.publicContext!!.getDrawable(R.drawable.custom_theme_icon)!!, posidonIcon, searchIconSize),
                    "posidon.launcher.settings.THEME"),
                SettingsItem(
                    Tools.publicContext!!.getString(R.string.settings_title_gestures),
                    Tools.badge(Tools.publicContext!!.getDrawable(R.drawable.custom_gestures_icon)!!, posidonIcon, searchIconSize),
                    "posidon.launcher.settings.GESTURES")
            )
        }
    }
}