package posidon.launcher.items

import android.graphics.drawable.Drawable
import android.os.Build

open class LauncherItem {
    var icon: Drawable? = null
    var label: String? = null

    companion object {
        operator fun invoke(string: String): LauncherItem? {
            return when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && string.startsWith("shortcut:") -> Shortcut(string)
                string.startsWith("folder:") -> Folder(string)
                else -> App[string]
            }
        }
    }
}