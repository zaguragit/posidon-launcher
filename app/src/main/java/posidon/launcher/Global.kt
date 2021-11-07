package posidon.launcher

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import io.posidon.android.launcherutils.liveWallpaper.Kustom
import posidon.launcher.items.App
import posidon.launcher.tools.Tools
import kotlin.math.max
import kotlin.math.min

object Global {

    fun getPastelAccent(): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(accentColor, hsv)
        hsv[1] = min(hsv[1],0.5f)
        hsv[2] = min(max(0.4f, hsv[2]), 0.75f)
        return Color.HSVToColor(hsv)
    }

    fun getBlackAccent(): Int {
        val lab = DoubleArray(3)
        ColorUtils.colorToLAB(accentColor, lab)
        lab[0] = 0.4
        lab[1] = lab[1].times(.6).coerceAtMost(2.0).coerceAtLeast(-12.0)
        lab[2] = lab[2].times(.6).coerceAtMost(8.0).coerceAtLeast(-3.0)
        return ColorUtils.LABToColor(lab[0], lab[1], lab[2])
    }

    var appSections = ArrayList<ArrayList<App>>()
    var apps = ArrayList<App>()

    var shouldSetApps = true
    var customized = false

    var accentColor = -0xeeaa01
        set(value) {
            field = value
            Kustom[Tools.appContext!!, "posidon", "accent"] = value.toUInt().toString(16)
        }
}