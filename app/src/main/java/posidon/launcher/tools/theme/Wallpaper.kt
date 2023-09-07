package posidon.launcher.tools.theme

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import io.posidon.android.conveniencelib.Device
import posidon.launcher.tools.Tools
import kotlin.concurrent.thread

object Wallpaper {

    fun setWallpaper(
        img: Bitmap,
        flag: Int
    ) = thread {
        val wallpaperManager = WallpaperManager.getInstance(Tools.appContext)
        try {
            wallpaperManager.setBitmap(img, null, true, when (flag) {
                0 -> WallpaperManager.FLAG_SYSTEM
                1 -> WallpaperManager.FLAG_LOCK
                else -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            })
        } catch (e: Exception) {}
    }

    fun centerCropWallpaper(context: Context, wallpaper: Bitmap): Bitmap {
        val scaledWidth = Device.screenHeight(context) * wallpaper.width / wallpaper.height
        var scaledWallpaper = Bitmap.createScaledBitmap(
            wallpaper,
            scaledWidth,
            Device.screenHeight(context),
            false)
        scaledWallpaper = Bitmap.createBitmap(
            scaledWallpaper,
            scaledWidth - Device.screenWidth(context) shr 1,
            0,
            Device.screenWidth(context),
            Device.screenHeight(context))
        return scaledWallpaper
    }
}