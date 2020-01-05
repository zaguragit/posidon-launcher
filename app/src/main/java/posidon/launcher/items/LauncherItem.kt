package posidon.launcher.items

import android.graphics.drawable.Drawable

open class LauncherItem {
    @JvmField
    var icon: Drawable? = null
    @JvmField
    var label: String? = null
}