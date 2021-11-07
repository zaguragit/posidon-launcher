package posidon.launcher

import io.posidon.android.launcherutils.liveWallpaper.Kustom
import posidon.launcher.items.App
import posidon.launcher.tools.Tools

object Global {

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