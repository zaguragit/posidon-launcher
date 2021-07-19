package posidon.launcher.tools.theme

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import posidon.android.conveniencelib.Device
import posidon.android.conveniencelib.Graphics
import posidon.android.conveniencelib.toBitmap
import posidon.launcher.Home
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

    fun blurredWall(radius: Float): Bitmap? {
        try {
            @SuppressLint("MissingPermission") var bitmap: Bitmap = WallpaperManager.getInstance(Tools.appContext).peekFastDrawable().toBitmap()
            val displayWidth = Device.screenWidth(Tools.appContext!!)
            val displayHeight = Device.screenHeight(Tools.appContext!!) + Tools.navbarHeight
            when {
                bitmap.height / bitmap.width.toFloat() < displayHeight / displayWidth.toFloat() -> {
                    bitmap = Bitmap.createScaledBitmap(
                        bitmap,
                        displayHeight * bitmap.width / bitmap.height,
                        displayHeight,
                        false)
                    bitmap = Bitmap.createBitmap(
                        bitmap, 0, 0,
                        displayWidth,
                        displayHeight)
                }
                bitmap.height / bitmap.width.toFloat() > displayHeight / displayWidth.toFloat() -> {
                    bitmap = Bitmap.createScaledBitmap(
                        bitmap,
                        displayWidth,
                        displayWidth * bitmap.height / bitmap.width,
                        false)
                    bitmap = Bitmap.createBitmap(
                        bitmap, 0, bitmap.height - displayHeight shr 1,
                        displayWidth,
                        displayHeight)
                    }
                else -> bitmap = Bitmap.createScaledBitmap(bitmap, displayWidth, displayHeight, false)
            }
            if (radius > 0)
                try { bitmap = Graphics.fastBlur(bitmap, radius.toInt()) }
                catch (e: Exception) { e.printStackTrace() }
            return bitmap
        } catch (e: OutOfMemoryError) {
            Home.instance.runOnUiThread {
                Toast.makeText(Tools.appContext, "OutOfMemoryError: Couldn't blur wallpaper!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) { e.printStackTrace() }
        return null
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