package posidon.launcher.tools

import android.graphics.Color
import androidx.palette.graphics.Palette
import posidon.android.conveniencelib.toBitmap
import posidon.launcher.items.LauncherItem

object Sort {

    fun <T : LauncherItem> labelSort(items: ArrayList<T>) {
        var i = 0
        var j: Int
        var temp: T
        while (i < items.size - 1) {
            j = i + 1
            while (j < items.size) {
                if (items[i].label!!.compareTo(items[j].label!!, ignoreCase = true) > 0) {
                    temp = items[i]
                    items[i] = items[j]
                    items[j] = temp
                }
                j++
            }
            i++
        }
    }

    fun <T : LauncherItem> colorSort(items: Array<T?>) {
        var i = 0
        var j: Int
        var temp: T
        val iHsv = floatArrayOf(0f , 0f, 0f)
        val jHsv = floatArrayOf(0f , 0f, 0f)
        while (i < items.size - 1) {
            j = i + 1
            while (j < items.size) {
                Color.colorToHSV(Palette.from(items[i]!!.icon!!.toBitmap()).generate().getVibrantColor(0xff252627.toInt()), iHsv)
                Color.colorToHSV(Palette.from(items[j]!!.icon!!.toBitmap()).generate().getVibrantColor(0xff252627.toInt()), jHsv)
                if (iHsv[0] < jHsv[0]) {
                    temp = items[i]!!
                    items[i] = items[j]
                    items[j] = temp
                }
                j++
            }
            i++
        }
    }

    fun <T : LauncherItem> colorSort(items: ArrayList<T?>) {
        var i = 0
        var j: Int
        var temp: T
        val iHsv = floatArrayOf(0f , 0f, 0f)
        val jHsv = floatArrayOf(0f , 0f, 0f)
        while (i < items.size - 1) {
            j = i + 1
            while (j < items.size) {
                Color.colorToHSV(Palette.from(items[i]!!.icon!!.toBitmap()).generate().getVibrantColor(0xff252627.toInt()), iHsv)
                Color.colorToHSV(Palette.from(items[j]!!.icon!!.toBitmap()).generate().getVibrantColor(0xff252627.toInt()), jHsv)
                if (iHsv[0] < jHsv[0]) {
                    temp = items[i]!!
                    items[i] = items[j]
                    items[j] = temp
                }
                j++
            }
            i++
        }
    }
}
