package launcherutils

import android.content.Intent
import android.content.pm.PackageManager

object Launcher {

    /**
     * @return the package name of the default launcher, null if there are no launchers
     */
    inline fun getDefaultLauncher(packageManager: PackageManager): String? {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        return packageManager.resolveActivity(intent, 0)?.resolvePackageName
    }
}