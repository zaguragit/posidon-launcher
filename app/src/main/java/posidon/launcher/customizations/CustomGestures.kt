package posidon.launcher.customizations

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Gestures
import posidon.launcher.tools.Tools
import posidon.launcher.tools.applyFontSetting
import posidon.launcher.view.Spinner


class CustomGestures : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_gestures)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)

        findViewById<Spinner>(R.id.pinch).run {
            data = resources.getStringArray(R.array.gestureTriggers)
            selectionI = when (Settings["gesture:pinch", Gestures.OPEN_OVERVIEW]) {
                Gestures.PULL_DOWN_NOTIFICATIONS -> 1
                Gestures.OPEN_APP_DRAWER -> 2
                Gestures.OPEN_SEARCH -> 3
                Gestures.OPEN_OVERVIEW -> 4
                Gestures.REFRESH_FEED-> 5
                else -> 0
            }
            setSelectionChangedListener {
                Settings["gesture:pinch"] = when (selectionI) {
                    1 -> Gestures.PULL_DOWN_NOTIFICATIONS
                    2 -> Gestures.OPEN_APP_DRAWER
                    3 -> Gestures.OPEN_SEARCH
                    4 -> Gestures.OPEN_OVERVIEW
                    5 -> Gestures.REFRESH_FEED
                    else -> ""
                }
            }
        }

        findViewById<Spinner>(R.id.longPress).run {
            data = resources.getStringArray(R.array.gestureTriggers)
            selectionI = when (Settings["gesture:long_press", Gestures.OPEN_OVERVIEW]) {
                Gestures.PULL_DOWN_NOTIFICATIONS -> 1
                Gestures.OPEN_APP_DRAWER -> 2
                Gestures.OPEN_SEARCH -> 3
                Gestures.OPEN_OVERVIEW -> 4
                Gestures.REFRESH_FEED-> 5
                else -> 0
            }
            setSelectionChangedListener {
                Settings["gesture:long_press"] = when (selectionI) {
                    1 -> Gestures.PULL_DOWN_NOTIFICATIONS
                    2 -> Gestures.OPEN_APP_DRAWER
                    3 -> Gestures.OPEN_SEARCH
                    4 -> Gestures.OPEN_OVERVIEW
                    5 -> Gestures.REFRESH_FEED
                    else -> ""
                }
            }
        }

        findViewById<Spinner>(R.id.back).run {
            data = resources.getStringArray(R.array.gestureTriggers)
            selectionI = when (Settings["gesture:back", ""]) {
                Gestures.PULL_DOWN_NOTIFICATIONS -> 1
                Gestures.OPEN_APP_DRAWER -> 2
                Gestures.OPEN_SEARCH -> 3
                Gestures.OPEN_OVERVIEW -> 4
                Gestures.REFRESH_FEED-> 5
                else -> 0
            }
            setSelectionChangedListener {
                Settings["gesture:back"] = when (selectionI) {
                    1 -> Gestures.PULL_DOWN_NOTIFICATIONS
                    2 -> Gestures.OPEN_APP_DRAWER
                    3 -> Gestures.OPEN_SEARCH
                    4 -> Gestures.OPEN_OVERVIEW
                    5 -> Gestures.REFRESH_FEED
                    else -> ""
                }
            }
        }




        findViewById<Spinner>(R.id.feedTopOverscroll).run {
            data = resources.getStringArray(R.array.gestureTriggers)
            selectionI = when (Settings["gesture:feed:top_overscroll", Gestures.PULL_DOWN_NOTIFICATIONS]) {
                Gestures.PULL_DOWN_NOTIFICATIONS -> 1
                Gestures.OPEN_APP_DRAWER -> 2
                Gestures.OPEN_SEARCH -> 3
                Gestures.OPEN_OVERVIEW -> 4
                Gestures.REFRESH_FEED-> 5
                else -> 0
            }
            setSelectionChangedListener {
                Settings["gesture:feed:top_overscroll"] = when (selectionI) {
                    1 -> Gestures.PULL_DOWN_NOTIFICATIONS
                    2 -> Gestures.OPEN_APP_DRAWER
                    3 -> Gestures.OPEN_SEARCH
                    4 -> Gestures.OPEN_OVERVIEW
                    5 -> Gestures.REFRESH_FEED
                    else -> ""
                }
            }
        }

        findViewById<Spinner>(R.id.feedBottomOverscroll).run {
            data = resources.getStringArray(R.array.gestureTriggers)
            selectionI = when (Settings["gesture:feed:bottom_overscroll", Gestures.OPEN_APP_DRAWER]) {
                Gestures.PULL_DOWN_NOTIFICATIONS -> 1
                Gestures.OPEN_APP_DRAWER -> 2
                Gestures.OPEN_SEARCH -> 3
                Gestures.OPEN_OVERVIEW -> 4
                Gestures.REFRESH_FEED-> 5
                else -> 0
            }
            setSelectionChangedListener {
                Settings["gesture:feed:bottom_overscroll"] = when (selectionI) {
                    1 -> Gestures.PULL_DOWN_NOTIFICATIONS
                    2 -> Gestures.OPEN_APP_DRAWER
                    3 -> Gestures.OPEN_SEARCH
                    4 -> Gestures.OPEN_OVERVIEW
                    5 -> Gestures.REFRESH_FEED
                    else -> ""
                }
            }
        }
    }
}