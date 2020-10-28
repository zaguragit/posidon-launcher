package posidon.launcher.items

import android.content.Context
import android.graphics.drawable.Drawable

class InternalItem(
    label: String,
    icon: Drawable?,
    val open: (context: Context) -> Unit
) : LauncherItem() {

    init {
        this.label = label
        this.icon = icon
    }
}