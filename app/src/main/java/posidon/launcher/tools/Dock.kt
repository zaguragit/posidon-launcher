package posidon.launcher.tools

import posidon.launcher.items.Folder
import posidon.launcher.items.LauncherItem
import posidon.launcher.storage.Settings

object Dock {

    val items = HashMap<Int, LauncherItem>()

    inline fun clearCache() = items.clear()

    inline fun add(item: LauncherItem, i: Int) {
        val currentItem = get(i) ?: return set(i, item)
        when {
            item is Folder -> {
                if (currentItem is Folder) {
                    val l = currentItem.items.size
                    currentItem.items.addAll(0, item.items)
                    set(i, currentItem)
                    if (l < 4) {
                        currentItem.updateIcon()
                    }
                } else {
                    item.items.add(0, currentItem)
                    set(i, currentItem)
                }
            }
            currentItem is Folder -> {
                val l = currentItem.items.size
                currentItem.items.add(item)
                set(i, currentItem)
                if (l < 4) {
                    currentItem.updateIcon()
                }
            }
            else -> set(i, Folder(arrayListOf(currentItem, item)))
        }
    }

    inline operator fun get(i: Int) = items[i] ?: Settings.getString("dock:icon:$i")?.let { LauncherItem(it) }.also {
        if (it == null)
            items.remove(i)
        else items[i] = it
    }

    inline operator fun set(i: Int, item: LauncherItem?) {
        if (item == null)
            items.remove(i)
        else items[i] = item
        Settings["dock:icon:$i"] = item?.toString()
    }

    inline operator fun iterator() = object : Iterator<LauncherItem?> {

        var i = 0
        val iconCount = Settings["dock:columns", 5] * Settings["dock:rows", 1]

        override fun hasNext() = i < iconCount
        override fun next() = get(i++)
    }

    inline fun indexed() = object : Iterator<Pair<LauncherItem?, Int>> {

        var i = 0
        val iconCount = Settings["dock:columns", 5] * Settings["dock:rows", 1]

        override fun hasNext() = i < iconCount
        override fun next() = get(i) to i++
    }
}