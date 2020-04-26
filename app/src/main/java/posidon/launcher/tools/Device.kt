package posidon.launcher.tools

import android.content.Context

object Device {

    inline val displayWidth get() = Tools.publicContext!!.resources.displayMetrics.widthPixels
    inline val displayHeight get() = Tools.publicContext!!.resources.displayMetrics.heightPixels
}