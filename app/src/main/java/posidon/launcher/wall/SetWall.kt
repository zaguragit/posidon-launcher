package posidon.launcher.wall

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Build
import posidon.launcher.tools.Tools
import java.lang.ref.WeakReference

class SetWall(
    private val img: Bitmap,
    private val flag: Int
) : AsyncTask<Any, Unit, Unit>() {

    override fun doInBackground(vararg a: Any) {
        val wallpaperManager = WallpaperManager.getInstance(Tools.publicContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                wallpaperManager.setBitmap(img, null, true, when (flag) {
                    0 -> WallpaperManager.FLAG_SYSTEM
                    1 -> WallpaperManager.FLAG_LOCK
                    else -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                })
            } catch (e: Exception) {}
        } else {
            try { wallpaperManager.setBitmap(img) }
            catch (e: Exception) {}
        }
    }
}