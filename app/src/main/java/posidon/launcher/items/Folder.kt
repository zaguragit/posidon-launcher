package posidon.launcher.items

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import posidon.launcher.items.App.Companion.get
import posidon.launcher.storage.Settings
import posidon.launcher.tools.toBitmap
import java.util.*
import kotlin.math.min

class Folder(context: Context, string: String) : LauncherItem() {

    val apps: MutableList<App?> = ArrayList()

    override fun toString(): String {
        val sb = StringBuilder()
        for (app in apps) if (app != null) sb.append("¬").append(app.packageName).append("/").append(app.name)
        return "folder($label$sb)"
    }

    private fun icon(context: Context): Bitmap? {
        try {
            val previewApps = min(apps.size, 4)
            val drr = arrayOfNulls<Drawable>(previewApps + 1)
            drr[0] = ColorDrawable(Settings["folderBG", -0x22eeeded])
            for (i in 0 until previewApps) {
                drr[i + 1] = BitmapDrawable(context.resources, apps[i]!!.icon!!.toBitmap())
            }
            val layerDrawable = LayerDrawable(drr)
            val width = layerDrawable.intrinsicWidth
            val height = layerDrawable.intrinsicHeight
            val paddingNear = width / 6
            val paddingFar = width / 12 * 7
            val paddingMedium = (paddingFar + paddingNear) / 2
            when (previewApps) {
                1 -> layerDrawable.setLayerInset(1, paddingMedium, paddingMedium, paddingMedium, paddingMedium)
                2 -> {
                    layerDrawable.setLayerInset(1, paddingNear, paddingMedium, paddingFar, paddingMedium)
                    layerDrawable.setLayerInset(2, paddingFar, paddingMedium, paddingNear, paddingMedium)
                }
                3 -> {
                    layerDrawable.setLayerInset(1, paddingNear, paddingNear, paddingFar, paddingFar)
                    layerDrawable.setLayerInset(2, paddingFar, paddingNear, paddingNear, paddingFar)
                    layerDrawable.setLayerInset(3, paddingMedium, paddingFar, paddingMedium, paddingNear)
                }
                else -> {
                    layerDrawable.setLayerInset(1, paddingNear, paddingNear, paddingFar, paddingFar)
                    layerDrawable.setLayerInset(2, paddingFar, paddingNear, paddingNear, paddingFar)
                    layerDrawable.setLayerInset(3, paddingNear, paddingFar, paddingFar, paddingNear)
                    layerDrawable.setLayerInset(4, paddingFar, paddingFar, paddingNear, paddingNear)
                }
            }
            var bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            var canvas = Canvas(bitmap!!)
            layerDrawable.setBounds(0, 0, width, height)
            layerDrawable.draw(canvas)
            val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            canvas = Canvas(outputBitmap)
            canvas.drawBitmap(bitmap, 0f, 0f, Paint().apply { isAntiAlias = true })
            if (Settings["icshape", 4] != 3) {
                val path = Path()
                when (Settings["icshape", 4]) {
                    1 -> path.addCircle(width.toFloat() / 2f + 1, height.toFloat() / 2f + 1, min(width.toFloat(), height.toFloat() / 2f) - 2, Path.Direction.CCW)
                    2 -> path.addRoundRect(2f, 2f, width - 2.toFloat(), height - 2.toFloat(), min(width, height).toFloat() / 4f, min(width, height).toFloat() / 4f, Path.Direction.CCW)
                    0, 4 -> {
                        //Formula: (|x|)^3 + (|y|)^3 = radius^3
                        val xx = 2
                        val yy = 2
                        val radius = min(width, height) / 2 - 2
                        val radiusToPow = radius * radius * radius.toDouble()
                        path.moveTo(-radius.toFloat(), 0f)
                        run {
                            var x = -radius
                            while (x <= radius) {
                                path.lineTo(x.toFloat(), Math.cbrt(radiusToPow - Math.abs(x * x * x)).toFloat())
                                x++
                            }
                        }
                        var x = radius
                        while (x >= -radius) {
                            path.lineTo(x.toFloat(), (-Math.cbrt(radiusToPow - Math.abs(x * x * x))).toFloat())
                            x--
                        }
                        path.close()
                        val matrix = Matrix()
                        matrix.postTranslate(xx + radius.toFloat(), yy + radius.toFloat())
                        path.transform(matrix)
                    }
                }
                path.fillType = Path.FillType.INVERSE_EVEN_ODD
                canvas.drawPath(path, Paint().apply {
                    isAntiAlias = true
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                })
            }
            bitmap = outputBitmap
            return bitmap
        } catch (e: Exception) { e.printStackTrace() }
        return null
    }

    fun clear() {
        apps.clear()
    }

    init {
        val a = string.substring(7, string.length - 1).split("¬").toTypedArray()
        label = a[0]
        for (i in 1 until a.size) {
            val app = get(a[i])
            if (app != null) apps.add(app)
        }
        icon = BitmapDrawable(context.resources, icon(context))
    }

    companion object {
        var currentlyOpen = false
    }
}