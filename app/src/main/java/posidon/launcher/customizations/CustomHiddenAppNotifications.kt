package posidon.launcher.customizations

import android.os.Build
import posidon.launcher.items.App
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Sort
import posidon.launcher.tools.theme.Icons

class CustomHiddenAppNotifications : AppTickingActivity() {

    override fun getApps(): List<App> {
        val apps = ArrayList<App>()

        val pacslist = packageManager.getInstalledPackages(0)
        for (i in pacslist.indices) {
            apps.add(App(pacslist[i].packageName, pacslist[i].activities.firstOrNull()?.name ?: "", label = pacslist[i].applicationInfo.loadLabel(packageManager).toString()).apply {
                icon = pacslist[i].applicationInfo.loadIcon(packageManager)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    icon = Icons.generateAdaptiveIcon(icon!!)
                }
                icon = Icons.badgeMaybe(icon!!, false)
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