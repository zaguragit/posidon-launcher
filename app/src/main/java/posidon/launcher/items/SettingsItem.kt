package posidon.launcher.items

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.provider.Settings
import android.view.View
import androidx.core.content.ContextCompat
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.tools.Tools
import posidon.launcher.tools.open
import posidon.launcher.tools.theme.Icons
import posidon.launcher.tools.toBitmapDrawable

class SettingsItem private constructor(
    override var label: String?,
    override var icon: Drawable?,
    val action: String
) : LauncherItem() {

    override fun open(context: Context, view: View, dockI: Int) = try {
        context.open(action)
    } catch (e: Exception) { e.printStackTrace() }

    companion object {

        private var list: Array<SettingsItem>? = null

        fun getList(): Array<SettingsItem> {
            if (list != null) {
                return list as Array<SettingsItem>
            }
            val posidonIcon = App.getFromPackage("posidon.launcher")!![0].icon!!.toBitmapDrawable(true)
            val settingsIcon = ContextCompat.getDrawable(Tools.appContext!!, R.drawable.ic_settings)!!.apply { setTintList(ColorStateList.valueOf(Global.accentColor)) }
            val searchIconSize = posidon.launcher.storage.Settings["search:icons:size", 56]
            list = arrayOf(
                SettingsItem(
                    Tools.appContext!!.getString(R.string.airplane_mode),
                    Icons.badge(ContextCompat.getDrawable(Tools.appContext!!, R.drawable.ic_airplane)!!, settingsIcon, searchIconSize),
                    Settings.ACTION_AIRPLANE_MODE_SETTINGS),
                SettingsItem(
                    Tools.appContext!!.getString(R.string.power_usage),
                    Icons.badge(ContextCompat.getDrawable(Tools.appContext!!, R.drawable.ic_power_usage)!!, settingsIcon, searchIconSize),
                    Intent.ACTION_POWER_USAGE_SUMMARY),
                SettingsItem(
                    Tools.appContext!!.getString(R.string.wireless_title),
                    Icons.badge(ContextCompat.getDrawable(Tools.appContext!!, R.drawable.ic_wifi)!!, settingsIcon, searchIconSize),
                    Settings.ACTION_WIRELESS_SETTINGS),
                SettingsItem(
                    Tools.appContext!!.getString(R.string.device_info),
                    Icons.badge(ContextCompat.getDrawable(Tools.appContext!!, R.drawable.ic_device)!!, settingsIcon, searchIconSize),
                    Settings.ACTION_DEVICE_INFO_SETTINGS),
                SettingsItem(
                    Tools.appContext!!.getString(R.string.settings_title_drawer),
                    Icons.badge(ContextCompat.getDrawable(Tools.appContext!!, R.drawable.custom_drawer_icon)!!, posidonIcon, searchIconSize),
                    "posidon.launcher.settings.DRAWER"),
                SettingsItem(
                    Tools.appContext!!.getString(R.string.settings_title_feed),
                    Icons.badge(ContextCompat.getDrawable(Tools.appContext!!, R.drawable.custom_feed_icon)!!, posidonIcon, searchIconSize),
                    "posidon.launcher.settings.FEED"),
                SettingsItem(
                    Tools.appContext!!.getString(R.string.settings_title_news),
                    Icons.badge(ContextCompat.getDrawable(Tools.appContext!!, R.drawable.custom_news_icon)!!, posidonIcon, searchIconSize),
                    "posidon.launcher.settings.NEWS"),
                SettingsItem(
                    Tools.appContext!!.getString(R.string.notifications),
                    Icons.badge(ContextCompat.getDrawable(Tools.appContext!!, R.drawable.custom_notifications_icon)!!, posidonIcon, searchIconSize),
                    "posidon.launcher.settings.NOTIFICATIONS"),
                SettingsItem(
                    Tools.appContext!!.getString(R.string.settings_title_dock),
                    Icons.badge(ContextCompat.getDrawable(Tools.appContext!!, R.drawable.custom_dock_icon)!!, posidonIcon, searchIconSize),
                    "posidon.launcher.settings.DOCK"),
                SettingsItem(
                    Tools.appContext!!.getString(R.string.settings_title_folders),
                    Icons.badge(ContextCompat.getDrawable(Tools.appContext!!, R.drawable.custom_folders_icon)!!, posidonIcon, searchIconSize),
                    "posidon.launcher.settings.FOLDERS"),
                SettingsItem(
                    Tools.appContext!!.getString(R.string.settings_title_search),
                    Icons.badge(ContextCompat.getDrawable(Tools.appContext!!, R.drawable.custom_search_icon)!!, posidonIcon, searchIconSize),
                    "posidon.launcher.settings.SEARCH"),
                SettingsItem(
                    Tools.appContext!!.getString(R.string.settings_title_theme),
                    Icons.badge(ContextCompat.getDrawable(Tools.appContext!!, R.drawable.custom_theme_icon)!!, posidonIcon, searchIconSize),
                    "posidon.launcher.settings.THEME"),
                SettingsItem(
                    Tools.appContext!!.getString(R.string.settings_title_gestures),
                    Icons.badge(ContextCompat.getDrawable(Tools.appContext!!, R.drawable.custom_gestures_icon)!!, posidonIcon, searchIconSize),
                    "posidon.launcher.settings.GESTURES")
            )
            return list!!
        }
    }

    override fun toString() = action
}