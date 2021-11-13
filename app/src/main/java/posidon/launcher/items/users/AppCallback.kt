package posidon.launcher.items.users

import android.content.pm.LauncherApps
import android.os.Bundle
import android.os.UserHandle

class AppCallback(
    val loadApps: () -> Unit
) : LauncherApps.Callback() {
    override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle?, replacing: Boolean) = loadApps()
    override fun onPackageChanged(packageName: String, user: UserHandle?) = loadApps()
    override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle?, replacing: Boolean) = loadApps()
    override fun onPackageAdded(packageName: String, user: UserHandle?) = loadApps()
    override fun onPackageRemoved(packageName: String, user: UserHandle?) = loadApps()
    override fun onPackagesSuspended(packageNames: Array<out String>?, user: UserHandle?) = loadApps()
    override fun onPackagesUnsuspended(packageNames: Array<out String>?, user: UserHandle?) = loadApps()
    override fun onPackagesSuspended(
        packageNames: Array<out String>?,
        user: UserHandle?,
        launcherExtras: Bundle?
    ) = loadApps()
}