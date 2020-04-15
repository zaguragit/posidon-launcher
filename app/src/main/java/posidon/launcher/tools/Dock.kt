package posidon.launcher.tools

import android.os.Build
import android.text.TextUtils
import posidon.launcher.Main
import posidon.launcher.items.App
import posidon.launcher.items.Folder
import posidon.launcher.items.LauncherItem
import posidon.launcher.items.Shortcut
import posidon.launcher.storage.Settings

object Dock {

    fun add(item: LauncherItem, i: Int) {
        var data: Array<String?> = Settings["dock", ""].split("\n").toTypedArray()
        if (data.size <= i) data = data.copyOf(i + 1)
        if (data[i] == null || data[i] == "" || data[i] == "null") data[i] = item.toString()
        else if (item is App || item is Shortcut) {
            if (data[i]!!.startsWith("folder(") && data[i]!!.endsWith(")"))
                data[i] = "folder(" + data[i]!!.substring(7, data[i]!!.length - 1) + "¬" + item.toString() + ")"
            else data[i] = "folder(" + "folder¬" + data[i] + "¬" + item.toString() + ")"
        } else if (item is Folder) {
            var folderContent = item.toString().substring(7, item.toString().length - 1)
            if (data[i]!!.startsWith("folder(") && data[i]!!.endsWith(")")) {
                folderContent = folderContent.substring(folderContent.indexOf('¬') + 1)
                data[i] = "folder(" + data[i]!!.substring(7, data[i]!!.length - 1) + "¬" + folderContent + ")"
            } else data[i] = "folder(" + folderContent + "¬" + data[i] + ")"
        }
        Settings["dock"] = TextUtils.join("\n", data)
        Main.setDock()
    }

    operator fun get(i: Int): LauncherItem? {
        val string = Settings["dock", ""].split("\n")[i]
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && string.startsWith("shortcut:") -> Shortcut(string)
            string.startsWith("folder(") && string.endsWith(')') -> Folder(Tools.publicContext, string)
            else -> App[string]
        }
    }

    operator fun iterator(): Iterator<LauncherItem> {
        return object : Iterator<LauncherItem> {

            val data = Settings["dock", ""].split("\n")
            var i = 0

            override fun hasNext() = i < data.lastIndex

            override fun next(): LauncherItem {
                val string = data[++i]
                return when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && string.startsWith("shortcut:") -> Shortcut(string)
                    string.startsWith("folder(") && string.endsWith(')') -> Folder(Tools.publicContext, string)
                    else -> App[string]!!
                }
            }
        }
    }

    operator fun set(i: Int, item: LauncherItem) {
        var data: Array<String?> = Settings["dock", ""].split("\n").toTypedArray()
        if (data.size <= i) data = data.copyOf(i + 1)
        data[i] = item.toString()
        Settings["dock"] = TextUtils.join("\n", data)
        Main.setDock()
    }
}