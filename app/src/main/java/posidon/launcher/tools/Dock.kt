package posidon.launcher.tools

import android.text.TextUtils
import posidon.launcher.items.App
import posidon.launcher.items.Folder
import posidon.launcher.items.LauncherItem
import posidon.launcher.items.Shortcut

object Dock {
    fun add(item: LauncherItem, i: Int) {
        var data: Array<String?> = Settings.getString("dock", "").split("\n").toTypedArray()
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
        Settings.putString("dock", TextUtils.join("\n", data))
    }

    fun get(i: Int) {

    }

    fun set(item: LauncherItem, i: Int) {
        var data: Array<String?> = Settings.getString("dock", "").split("\n").toTypedArray()
        if (data.size <= i) data = data.copyOf(i + 1)
        data[i] = item.toString()
        Settings.putString("dock", TextUtils.join("\n", data))
    }
}