package posidon.launcher.items

import android.graphics.drawable.Drawable

class InternalItem(
    label: String,
    icon: Drawable?,
    val open: () -> Unit
) : LauncherItem() {

    init {
        this.label = label
        this.icon = icon
    }
}