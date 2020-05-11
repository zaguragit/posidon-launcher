package posidon.launcher.tools

import android.content.Context
import android.content.res.Configuration

object Device {

    inline val displayWidth get() = Tools.publicContext!!.resources.displayMetrics.widthPixels
    inline val displayHeight get() = Tools.publicContext!!.resources.displayMetrics.heightPixels
}

inline val Context.isTablet get() =
    resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE

inline val Context.hasNavbar: Boolean get() {
    val id: Int = resources.getIdentifier("config_showNavigationBar", "bool", "android")
    return id != 0 && resources.getBoolean(id)
}