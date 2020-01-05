package posidon.launcher.tools

import android.graphics.Color
import androidx.palette.graphics.Palette
import posidon.launcher.items.App

object Sort {
    @JvmStatic
    fun labelSort(apps: Array<App?>) {
        var i = 0
        var j: Int
        var temp: App
        while (i < apps.size - 1) {
            j = i + 1
            while (j < apps.size) {
                if (apps[i]!!.label!!.compareTo(apps[j]!!.label!!, ignoreCase = true) > 0) {
                    temp = apps[i]!!
                    apps[i] = apps[j]
                    apps[j] = temp
                }
                j++
            }
            i++
        }
    }

    @JvmStatic
    fun colorSort(apps: Array<App?>) {
        var i = 0
        var j: Int
        var temp: App
        val iHsv = floatArrayOf(0f , 0f, 0f)
        val jHsv = floatArrayOf(0f , 0f, 0f)
        while (i < apps.size - 1) {
            j = i + 1
            while (j < apps.size) {
                Color.colorToHSV(Palette.from(Tools.drawable2bitmap(apps[i]!!.icon)).generate().getVibrantColor(0xff252627.toInt()), iHsv)
                Color.colorToHSV(Palette.from(Tools.drawable2bitmap(apps[j]!!.icon)).generate().getVibrantColor(0xff252627.toInt()), jHsv)
                if (iHsv[0] < jHsv[0]) {
                    temp = apps[i]!!
                    apps[i] = apps[j]
                    apps[j] = temp
                }
                j++
            }
            i++
        }
    }
}
