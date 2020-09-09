package posidon.launcher.items

import android.app.ActivityOptions
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.os.Build
import android.os.Process
import android.view.View
import androidx.annotation.RequiresApi
import posidon.launcher.Home
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools

@RequiresApi(Build.VERSION_CODES.O)
class Shortcut : LauncherItem {

    val packageName: String
    val id: String

    constructor(shortcut: ShortcutInfo) {
        label = shortcut.shortLabel.toString()
        packageName = shortcut.`package`
        id = shortcut.id
        icon = Home.launcherApps.getShortcutBadgedIconDrawable(shortcut, 1)
        println("UAN__ $label $this")
        pinnedShortcuts.putIfAbsent(this.toString(), this)
    }

    constructor(string: String) {
        packageName = string.substring(9).substringBefore('/')
        id = string.substring(10 + packageName.length)
        val shortcuts = Home.launcherApps.getShortcuts(LauncherApps.ShortcutQuery().apply {
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
            icon = Home.launcherApps.getShortcutBadgedIconDrawable(shortcut, 1)
            label = shortcut.shortLabel?.toString()
        }
        for (s in shortcuts!!) {
            println("\tTRII___ ${shortcut?.id}")
        }
        println("TUU__ $label $this")
        pinnedShortcuts.putIfAbsent(this.toString(), this)
    }

    override fun toString() = "shortcut:$packageName/$id"

    fun open(context: Context, view: View) {
        try {
            Home.launcherApps.startShortcut(packageName, id, view.clipBounds, when (Settings["anim:app_open", "posidon"]) {
                "scale_up" -> ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.measuredWidth, view.measuredHeight)
                "clip_reveal" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    ActivityOptions.makeClipRevealAnimation(view, 0, 0, view.measuredWidth, view.measuredHeight)
                else ActivityOptions.makeCustomAnimation(context, R.anim.appopen, R.anim.home_exit)
                else -> ActivityOptions.makeCustomAnimation(context, R.anim.appopen, R.anim.home_exit)
            }.toBundle(), Process.myUserHandle())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        val pinnedShortcuts = HashMap<String, Shortcut>()
    }
}

inline fun Shortcut.isInstalled(packageManager: PackageManager) = Tools.isInstalled(packageName, packageManager)