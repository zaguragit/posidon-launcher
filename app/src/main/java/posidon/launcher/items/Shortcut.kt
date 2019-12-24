package posidon.launcher.items

import android.content.pm.ShortcutInfo
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
class Shortcut : LauncherItem {

    val packageName: String
    val name: String
    val extraString: String

    constructor(shortcut: ShortcutInfo) {
        label = shortcut.shortLabel.toString()
        packageName = shortcut.activity!!.packageName
        name = shortcut.activity!!.className
        extraString = shortcut.id
    }

    constructor(string: String) {
        val s = string.split('/')
        packageName = s[0]
        name = s[1]
        extraString = string.substring(packageName.length + name.length + 2)
    }

    override fun toString(): String {
        return "$packageName/$name/$extraString"
    }
}