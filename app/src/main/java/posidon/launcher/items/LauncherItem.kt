package posidon.launcher.items

import android.graphics.drawable.Drawable
import android.os.Build

open class LauncherItem {
    open var icon: Drawable? = null
    open var label: String? = null

    open fun getColor(): Int = -0xdad9d9

    companion object {
        operator fun invoke(string: String): LauncherItem? {
            return when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && string.startsWith("shortcut:") -> Shortcut(string)
                string.startsWith("folder:") -> Folder(string).let { when (it.items.size) {
                    0 -> null
                    1 -> it.items[0]
                    else -> it
                }}
                else -> App[string]
            }
        }
    }
}