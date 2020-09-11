package posidon.launcher

import android.content.pm.LauncherApps
import android.media.AudioManager
import android.os.PowerManager
import posidon.launcher.items.App

object Global {

    var appSections = ArrayList<ArrayList<App>>()
    var apps = ArrayList<App>()

    var shouldSetApps = true
    var customized = false

    var accentColor = -0xeeaa01

    lateinit var launcherApps: LauncherApps
    lateinit var musicService: AudioManager
    lateinit var powerManager: PowerManager
}