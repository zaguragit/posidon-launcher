package posidon.launcher.tools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import com.pixplicity.sharp.Sharp
import posidon.android.conveniencelib.toBitmap
import java.io.IOException
import java.net.URL
import kotlin.concurrent.thread

object ImageLoader {
    const val AUTO = -1

    fun loadNullableBitmap(
        url: String,
        width: Int = AUTO,
        height: Int = AUTO,
        scaleIfSmaller: Boolean = true,
        onFinished: (img: Bitmap?) -> Unit
    ) = thread {
        var w = width
        var h = height
        var img: Bitmap? = null
        try {
            val tmp = URL(url).openConnection().getInputStream().use {
                if (url.endsWith(".svg"))
                    Sharp.loadInputStream(it).drawable.toBitmap()
                else BitmapFactory.decodeStream(it) ?: return@thread
            }
            when {
                w == AUTO && h == AUTO -> img = tmp
                !scaleIfSmaller && (w > tmp.width || h > tmp.height) && w > tmp.width && h == AUTO || h > tmp.height && w == AUTO -> img = tmp
                else -> {
                    if (w == AUTO) w = h * tmp.width / tmp.height else if (h == AUTO) h = w * tmp.height / tmp.width
                    img = Bitmap.createScaledBitmap(tmp, w, h, true)
                }
            }
        }
        catch (e: IOException) {}
        catch (e: Exception) { e.printStackTrace() }
        catch (e: OutOfMemoryError) {
            img?.recycle()
            img = null
            System.gc()
        }
        onFinished(img)
    }

    inline fun loadBitmap(
        url: String,
        width: Int = AUTO,
        height: Int = AUTO,
        scaleIfSmaller: Boolean = true,
        crossinline onFinished: (img: Bitmap) -> Unit
    ) = loadNullableBitmap(url, width, height, scaleIfSmaller) {
        if (it != null) {
            onFinished(it)
        }
    }

    fun loadNullableSvg(
        url: String,
        onFinished: (img: Drawable?) -> Unit
    ) = thread {
        val img: Drawable? = try {
            URL(url).openConnection().getInputStream().use {
                Sharp.loadInputStream(it).drawable
            }
        }
        catch (e: IOException) { null }
        catch (e: Exception) {
            e.printStackTrace()
            null
        }
        catch (e: OutOfMemoryError) {
            System.gc()
            null
        }
        onFinished(img)
    }
}