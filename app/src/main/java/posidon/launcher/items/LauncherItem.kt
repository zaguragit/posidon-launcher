package posidon.launcher.items

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View

abstract class LauncherItem {

    abstract val icon: Drawable?
    abstract val label: String?

    /**
     * Color used for item long-press menus and similar stuff
     */
    open fun getColor(): Int = -0xdad9d9

    /**
     * What to do when the item is clicked
     *
     * [view]       The view that was clicked
     * [dockI]      The place in the dock that the item occupies, if it's not in the dock a -1 will be passed
     */
    abstract fun open(context: Context, view: View, dockI: Int)

    /**
     * The number to show in the notification badge
     * If value is 0, the badge won't be shown
     */
    open val notificationCount = 0

    /**
     * Text representation of the item, used to save it
     */
    abstract override fun toString(): String

    companion object {

        /**
         * Converts a string to an item
         * Used for item storage in places like the dock
         */
        operator fun invoke(string: String): LauncherItem? {
            return when {
                string.startsWith("shortcut:") -> PinnedShortcut(string)
                string.startsWith("folder:") -> Folder(string).let { when (it.items.size) {
                    0 -> null
                    1 -> it.items[0]
                    else -> it
                }}
                else -> App[string]
            }
        }

        /**
         * Creates a custom launcher item
         * Used for stuff like the DuckDuckGo search icon in the search screen
         */
        inline fun make(
            label: String,
            icon: Drawable?,
            crossinline openFn: (context: Context, view: View, dockI: Int) -> Unit
        ) = object : LauncherItem() {

            override var icon: Drawable? = icon
            override var label: String? = label

            override fun open(context: Context, view: View, dockI: Int) = openFn(context, view, dockI)
            override fun toString() = ""
        }
    }
}