package posidon.launcher

import posidon.launcher.external.kustom.Kustom
import posidon.launcher.items.App

object Global {

    var appSections = ArrayList<ArrayList<App>>()
    var apps = ArrayList<App>()

    var shouldSetApps = true
    var customized = false

    var accentColor = -0xeeaa01
        set(value) {
            field = value
            Kustom["accent"] = value.toUInt().toString(16)
        }
}