package launcherutils

import android.app.WallpaperManager
import android.view.View

object LiveWall {

    inline fun tap(view: View, x: Int, y: Int) {
        WallpaperManager.getInstance(view.context).sendWallpaperCommand(
            view.windowToken,
            WallpaperManager.COMMAND_TAP,
            x, y, 0, null)
    }

    inline fun drop(view: View, x: Int, y: Int) {
        WallpaperManager.getInstance(view.context).sendWallpaperCommand(
            view.windowToken,
            WallpaperManager.COMMAND_DROP,
            x, y, 0, null)
    }
}