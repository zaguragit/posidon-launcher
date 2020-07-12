package posidon.launcher.items

import android.content.Context
import android.graphics.*
import android.graphics.drawable.*
import android.widget.PopupWindow
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.clone
import posidon.launcher.tools.toBitmap
import kotlin.collections.ArrayList
import kotlin.math.min

class Folder(string: String) : LauncherItem() {

    val apps = ArrayList<App>()

    init {
        println(string)
        val appsList = string.substring(7, string.length).split('\t')
        label = appsList[0]
        for (i in 1 until appsList.size) {
            val app = App[appsList[i]]
            if (app != null) {
                apps.add(app)
            }
        }
        icon = icon(Tools.publicContext!!)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (app in apps) {
            sb.append("\t").append(app.toString())
        }
        return "folder:$label$sb"
    }

    private fun icon(context: Context): Drawable? {
        try {
            val previewApps = min(apps.size, 4)
            val drr = arrayOfNulls<Drawable>(previewApps + 1)
            drr[0] = ColorDrawable(Settings["folderBG", -0x22eeeded])
            for (i in 1..previewApps) {
                drr[i] = BitmapDrawable(context.resources, apps[i - 1].icon!!.toBitmap())
            }
            val layerDrawable = LayerDrawable(drr)
            val width = layerDrawable.intrinsicWidth
            val height = layerDrawable.intrinsicHeight
            val paddingNear = width / 6
            val paddingFar = width / 12 * 7
            val paddingMedium = (paddingFar + paddingNear) / 2
            when (previewApps) {
                0 -> {}
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
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap!!)
            layerDrawable.setBounds(0, 0, width, height)
            layerDrawable.draw(canvas)

            val icShape = Settings["icshape", 4]
            if (icShape != 3) {
                canvas.drawPath(Tools.getAdaptiveIconPath(icShape, width, height), Paint().apply {
                    isAntiAlias = true
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                })
            }

            return Tools.badgeMaybe(BitmapDrawable(Tools.publicContext!!.resources, bitmap), false)
        } catch (e: Exception) { e.printStackTrace() }
        return null
    }

    fun clear() {
        apps.clear()
    }

    companion object {
        var currentlyOpen: PopupWindow? = null
    }
}