package posidon.launcher.items

import android.app.ActivityOptions
import android.content.Context
import android.content.pm.ShortcutInfo
import android.os.Build
import android.os.Process
import android.view.View
import androidx.annotation.RequiresApi
import posidon.launcher.Main.launcherApps
import posidon.launcher.R
import posidon.launcher.tools.Settings

@RequiresApi(Build.VERSION_CODES.O)
class Shortcut : LauncherItem {

    val packageName: String
    val id: String

    constructor(shortcut: ShortcutInfo) {
        label = shortcut.shortLabel.toString()
        packageName = shortcut.`package`
        id = shortcut.id
        icon = launcherApps.getShortcutBadgedIconDrawable(shortcut, 1)
    }

    constructor(string: String) {
        val s = string.substring(9).split('/')
        packageName = s[0]
        id = string.substring(packageName.length + 1)
        icon = null
    }

    override fun toString(): String {
        return "shortcut:$packageName/$id"
    }

    public fun open(context: Context, view: View) {
        try {
            launcherApps.startShortcut(packageName, id, view.clipBounds, when (Settings.getString("anim:app_open", "posidon")) {
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
}