package posidon.launcher.customizations

import android.os.Build
import posidon.launcher.items.App
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Sort
import posidon.launcher.tools.ThemeTools

class CustomHiddenAppNotifications : AppTickingActivity() {

    override fun getApps(): List<App> {
        val apps = ArrayList<App>()

        val pacslist = packageManager.getInstalledPackages(0)
        for (i in pacslist.indices) {
            apps.add(App(pacslist[i].packageName).apply {
                icon = pacslist[i].applicationInfo.loadIcon(packageManager)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    icon = ThemeTools.generateAdaptiveIcon(icon!!)
                }
                icon = ThemeTools.badgeMaybe(icon!!, false)
                label = pacslist[i].applicationInfo.loadLabel(packageManager).toString()
            })
        }

        Sort.labelSort(apps)
        return apps
    }

    override fun isTicked(app: App): Boolean = Settings["notif:ex:${app.packageName}", false]
    override fun setTicked(app: App, isTicked: Boolean) {
        Settings["notif:ex:${app.packageName}"] = isTicked
    }
}