package posidon.launcher.tools

import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import com.pixplicity.sharp.Sharp
import posidon.launcher.tools.Loader.Bitmap.Companion.AUTO
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import kotlin.concurrent.thread

object Loader {

    class Text(
        private val url: String,
        private val onFinished: (string: String) -> Unit
    ) : AsyncTask<Unit?, String?, String?>() {

        override fun doInBackground(vararg params: Unit?): String? {
            try {
                val builder = StringBuilder()
                var buffer: String?
                val bufferReader = BufferedReader(InputStreamReader(URL(url).openStream()))
                while (bufferReader.readLine().also { buffer = it } != null) {
                    builder.append(buffer).append('\n')
                }
                bufferReader.close()
                return builder.toString()
            } catch (e: Exception) { e.printStackTrace() }
            return null
        }

        override fun onPostExecute(string: String?) { string?.let { onFinished(it) }}
    }

    fun threadText(
        url: String,
        onFinished: (string: String) -> Unit
    ) = thread {
        try {
            val builder = StringBuilder()
            var buffer: String?
            val bufferReader = BufferedReader(InputStreamReader(URL(url).openStream()))
            while (bufferReader.readLine().also { buffer = it } != null) {
                builder.append(buffer).append('\n')
            }
            bufferReader.close()
            onFinished(builder.toString())
        } catch (e: Exception) { e.printStackTrace() }
    }

    class Bitmap(
        private val url: String,
        private var width: Int = AUTO,
        private var height: Int = AUTO,
        private val scaleIfSmaller: Boolean = true,
        private val onFinished: (img: android.graphics.Bitmap) -> Unit
    ) : AsyncTask<Unit?, Unit?, Unit?>() {

        private var img: android.graphics.Bitmap? = null
        override fun doInBackground(vararg params: Unit?): Unit? {
            try {
                val input = URL(url).openConnection().getInputStream()
                val tmp = if (url.endsWith(".svg"))
                    Sharp.loadInputStream(input).drawable.toBitmap()
                else BitmapFactory.decodeStream(input) ?: return null
                input.close()
                when {
                    width == AUTO && height == AUTO -> img = tmp
                    !scaleIfSmaller && (width > tmp.width || height > tmp.height) && width > tmp.width && height == AUTO || height > tmp.height && width == AUTO -> img = tmp
                    else -> {
                        if (width == AUTO) width = height * tmp.width / tmp.height else if (height == AUTO) height = width * tmp.height / tmp.width
                        img = android.graphics.Bitmap.createScaledBitmap(tmp, width, height, true)
                    }
                }
            }
            catch (ignore: FileNotFoundException) {}
            catch (e: Exception) { e.printStackTrace() }
            catch (e: OutOfMemoryError) {
                img!!.recycle()
                img = null
                System.gc()
            }
            return null
        }

        override fun onPostExecute(aUnit: Unit?) { img?.let { onFinished(it) } }

        companion object {
            const val AUTO = -1
        }
    }

    class NullableBitmap(
        private val url: String,
        private var width: Int = AUTO,
        private var height: Int = AUTO,
        private val scaleIfSmaller: Boolean = true,
        private val onFinished: (img: android.graphics.Bitmap?) -> Unit
    ) : AsyncTask<Unit?, Unit?, Unit?>() {

        private var img: android.graphics.Bitmap? = null
        override fun doInBackground(vararg u: Unit?): Unit? {
            try {
                val input = URL(url).openConnection().getInputStream()
                val tmp = if (url.endsWith(".svg"))
                    Sharp.loadInputStream(input).drawable.toBitmap()
                else BitmapFactory.decodeStream(input) ?: return null
                input.close()
                when {
                    width == AUTO && height == AUTO -> img = tmp
                    !scaleIfSmaller && (width > tmp.width || height > tmp.height) && width > tmp.width && height == AUTO || height > tmp.height && width == AUTO -> img = tmp
                    else -> {
                        if (width == AUTO) width = height * tmp.width / tmp.height else if (height == AUTO) height = width * tmp.height / tmp.width
                        img = android.graphics.Bitmap.createScaledBitmap(tmp, width, height, true)
                    }
                }
            }
            catch (ignore: IOException) {}
            catch (e: Exception) { e.printStackTrace() }
            catch (e: OutOfMemoryError) {
                img!!.recycle()
                img = null
                System.gc()
            }
            return null
        }

        override fun onPostExecute(u: Unit?) = onFinished(img)
    }

    class NullableSvg(
        private val url: String,
        private val onFinished: (img: Drawable?) -> Unit
    ) : AsyncTask<Unit?, Unit?, Unit?>() {

        private var img: Drawable? = null
        override fun doInBackground(vararg u: Unit?): Unit? {
            try {
                val input = URL(url).openConnection().getInputStream()
                img = Sharp.loadInputStream(input).drawable
                input.close()
            }
            //catch (ignore: IOException) {}
            catch (e: Exception) { e.printStackTrace() }
            catch (e: OutOfMemoryError) {
                img = null
                System.gc()
            }
            return null
        }

        override fun onPostExecute(u: Unit?) = onFinished(img)
    }
}