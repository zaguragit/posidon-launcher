package posidon.launcher.items

import android.app.ActivityOptions
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.os.Process
import android.view.View
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools

class PinnedShortcut : LauncherItem {

    val packageName: String
    val id: String

    override val icon: Drawable?
    override val label: String?

    constructor(shortcut: ShortcutInfo, context: Context) {
        label = shortcut.shortLabel.toString()
        packageName = shortcut.`package`
        id = shortcut.id
        icon = (context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps).getShortcutBadgedIconDrawable(shortcut, 1)
        println("UAN__ $label $this")
    }

    constructor(string: String) {
        packageName = string.substring(9).substringBefore('/')
        id = string.substring(10 + packageName.length)
        val launcherApps = Tools.appContext!!.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val shortcuts = launcherApps.getShortcuts(LauncherApps.ShortcutQuery().apply {
            setPackage(packageName)
            //setShortcutIds(listOf(id))
            setQueryFlags(
                LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED or
                LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                LauncherApps.ShortcutQuery.FLAG_GET_KEY_FIELDS_ONLY)
        }, Process.myUserHandle())
        val shortcut = shortcuts?.getOrNull(0)
        if (shortcut != null) {
            icon = launcherApps.getShortcutBadgedIconDrawable(shortcut, 1)
            label = shortcut.shortLabel?.toString()
        } else {
            icon = null
            label = null
        }
        for (s in shortcuts!!) {
            println("\tTRII___ ${shortcut?.id}")
        }
        println("TUU__ $label $this")
    }

    override fun toString() = "shortcut:$packageName/$id"

    override fun open(context: Context, view: View, dockI: Int) {
        try {
            (context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps).getShortcuts(LauncherApps.ShortcutQuery()
                .setQueryFlags(
                    LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                    LauncherApps.ShortcutQuery.FLAG_GET_KEY_FIELDS_ONLY), Process.myUserHandle()).also {
                println("ANASTASALANEFASA" + it?.size + "aaa" + it?.joinToString { "\n" + it.`package` + " / " + it.id })
            }
            (context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps).startShortcut(packageName, id, view.clipBounds, when (Settings["anim:app_open", "posidon"]) {
                "scale_up" -> ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.measuredWidth, view.measuredHeight)
                "clip_reveal" -> ActivityOptions.makeClipRevealAnimation(view, 0, 0, view.measuredWidth, view.measuredHeight)
                else -> ActivityOptions.makeCustomAnimation(context, R.anim.appopen, R.anim.home_exit)
            }.toBundle(), Process.myUserHandle())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inline fun isInstalled(packageManager: PackageManager) = Tools.isInstalled(packageName, packageManager)

    companion object {
        private val pinnedShortcuts = HashMap<String, PinnedShortcut>().apply {
            Settings.getStrings("pinned_shortcuts")?.forEach {
                put(it, PinnedShortcut(it))
            }
        }

        private val data get() = Settings.getStrings("pinned_shortcuts")

        fun pin(context: Context, shortcut: PinnedShortcut) {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            pinnedShortcuts.putIfAbsent(shortcut.toString(), shortcut)
            data?.add(shortcut.toString())
            Settings.apply()
            launcherApps.pinShortcuts(
                shortcut.packageName,
                pinnedShortcuts.filter { it.value.packageName == shortcut.packageName }.map { it.value.id },
                Process.myUserHandle()
            )
        }

        fun unpin(context: Context, shortcut: PinnedShortcut): Boolean {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            if (pinnedShortcuts.remove(shortcut.toString(), shortcut)) {
                data?.remove(shortcut.toString())
                Settings.apply()
                launcherApps.pinShortcuts(
                    shortcut.packageName,
                    pinnedShortcuts.filter { it.value.packageName == shortcut.packageName }.map { it.value.id },
                    Process.myUserHandle()
                )
                return true
            }
            return false
        }
    }
}