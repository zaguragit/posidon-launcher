package posidon.launcher.tools

import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.os.UserHandle
import android.os.UserManager
import posidon.launcher.items.App
import kotlin.concurrent.thread

/**
 * Loads the apps, instead of just loading the app list,
 * the you must provide a `forEachApp` callback
 */
object AppLoader {
    inline fun loadAsync(
        context: Context,
        crossinline onEnd: () -> Unit = {},
        crossinline forEachApp: (app: App) -> Unit,
    ): Thread = thread(name = "AppLoader thread", isDaemon = true) {
        load(context, forEachApp)
        onEnd()
    }

    inline fun load(
        context: Context,
        forEachApp: (app: App) -> Unit,
    ) {
        val userManager = context.getSystemService(UserManager::class.java)
        val launcherApps = context.getSystemService(LauncherApps::class.java)

        repeat(userManager.userProfiles.size) {
            val profile = userManager.userProfiles[it]
            val appList = launcherApps.getActivityList(null, profile)
            for (i in appList.indices) forEachApp(loadApp(
                appList[i],
                profile,
            ))
        }
    }

    @PublishedApi
    internal fun loadApp(
        appListItem: LauncherActivityInfo,
        profile: UserHandle,
    ): App {
        val packageName = appListItem.applicationInfo.packageName
        val name = appListItem.name
        val label = appListItem.label.let {
            if (it.isNullOrBlank()) packageName
            else it.toString()
        }
        return App(
            packageName,
            name,
            profile,
            label,
        )
    }
}