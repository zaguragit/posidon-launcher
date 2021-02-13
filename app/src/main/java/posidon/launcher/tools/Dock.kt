package posidon.launcher.tools

import posidon.launcher.items.App
import posidon.launcher.items.Folder
import posidon.launcher.items.LauncherItem
import posidon.launcher.items.PinnedShortcut
import posidon.launcher.storage.Settings

object Dock {

    inline fun add(item: LauncherItem, i: Int) {
        val data = Settings.getString("dock:icon:$i") ?: return set(i, item)
        val currentItem = LauncherItem(data) ?: return set(i, item)
        val k = "dock:icon:$i"
        when (item) {
            is App, is PinnedShortcut -> {
                if (currentItem is Folder)
                    Settings[k] = "folder:" + data.substring(7, data.length) + "\t" + item.toString()
                else Settings[k] = "folder:${Tools.generateFolderUid()}\t$data\t$item"
            }
            is Folder -> {
                var folderContent = item.toString().substring(7, item.toString().length)
                if (currentItem is Folder) {
                    folderContent = folderContent.substring(folderContent.indexOf('\t') + 1)
                    Settings[k] = "folder:" + data.substring(7, data.length) + "\t" + folderContent
                } else Settings[k] = "folder:$folderContent\t$data"
            }
        }
    }

    inline operator fun get(i: Int) = Settings.getString("dock:icon:$i")?.let { LauncherItem(it) }

    inline operator fun set(i: Int, item: LauncherItem?) {
        Settings["dock:icon:$i"] = item?.toString()
    }
}