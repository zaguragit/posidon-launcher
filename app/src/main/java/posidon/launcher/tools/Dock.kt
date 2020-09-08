package posidon.launcher.tools

import posidon.launcher.items.App
import posidon.launcher.items.Folder
import posidon.launcher.items.LauncherItem
import posidon.launcher.items.Shortcut
import posidon.launcher.storage.Settings

object Dock {

    fun add(item: LauncherItem, i: Int) {
        val data = Settings.getString("dock:icon:$i")
        when {
            data == null || data == "" || data == "null" -> {
                Settings["dock:icon:$i"] = item.toString()
            }
            item is App || item is Shortcut -> {
                if (data.startsWith("folder:"))
                    Settings["dock:icon:$i"] = "folder:" + data.substring(7, data.length) + "\t" + item.toString()
                else Settings["dock:icon:$i"] = "folder:${Tools.generateFolderUid()}\t$data\t$item"
            }
            item is Folder -> {
                var folderContent = item.toString().substring(7, item.toString().length)
                if (data.startsWith("folder:")) {
                    folderContent = folderContent.substring(folderContent.indexOf('\t') + 1)
                    Settings["dock:icon:$i"] = "folder:" + data.substring(7, data.length) + "\t" + folderContent
                } else Settings["dock:icon:$i"] = "folder:$folderContent\t$data"
            }
        }
    }

    operator fun get(i: Int) = Settings.getString("dock:icon:$i")?.let { LauncherItem(it).apply {
        if (this is Folder && uid.length != 8) {
            val label = uid
            uid = Tools.generateFolderUid()
            Settings["folder:$uid:label"] = label
            Dock[i] = this
        }
    }}

    operator fun iterator() = object : Iterator<LauncherItem?> {

        var i = 0
        val iconCount = Settings["dock:columns", 5] * Settings["dock:rows", 1]

        override fun hasNext() = i < iconCount
        override fun next() = Settings.getString("dock:icon:${i++}")?.let { LauncherItem(it) }
    }

    operator fun set(i: Int, item: LauncherItem?) {
        Settings["dock:icon:$i"] = item?.toString()
    }
}