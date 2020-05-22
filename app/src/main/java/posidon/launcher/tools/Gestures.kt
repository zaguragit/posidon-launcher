package posidon.launcher.tools

import android.content.Intent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import posidon.launcher.LauncherMenu
import posidon.launcher.Main
import posidon.launcher.search.SearchActivity

object Gestures {

    const val PULL_DOWN_NOTIFICATIONS = "notif"
    const val OPEN_APP_DRAWER = "drawer"
    const val OPEN_SEARCH = "search"
    const val OPEN_OVERVIEW = "overview"

    fun performTrigger(key: String) {
        when (key) {
            PULL_DOWN_NOTIFICATIONS -> Tools.publicContext!!.pullStatusbar()
            OPEN_APP_DRAWER -> Main.instance.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            OPEN_SEARCH -> Tools.publicContext!!.startActivity(Intent(Tools.publicContext, SearchActivity::class.java))
            OPEN_OVERVIEW -> LauncherMenu.openOverview()
        }
    }
}