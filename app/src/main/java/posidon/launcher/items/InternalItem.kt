package posidon.launcher.items

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View

inline fun InternalItem(
    label: String,
    icon: Drawable?,
    crossinline openFn: (context: Context, view: View, dockI: Int) -> Unit
) = object : LauncherItem() {
    init {
        this.label = label
        this.icon = icon
    }
    override fun open(context: Context, view: View, dockI: Int) = openFn(context, view, dockI)
}