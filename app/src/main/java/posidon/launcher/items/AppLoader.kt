package posidon.launcher.items

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.os.Build
import android.os.PowerManager
import posidon.launcher.Main
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Sort
import posidon.launcher.tools.ThemeTools
import posidon.launcher.tools.Tools
import java.lang.ref.WeakReference

class AppLoader(context: Context, private val onEnd: () -> Unit) : AsyncTask<Unit?, Unit?, Unit?>() {

    private lateinit var tmpApps: Array<App?>
    private val tmpAppSections = ArrayList<ArrayList<App>>()
    private val context: WeakReference<Context> = WeakReference(context)

    override fun doInBackground(objects: Array<Unit?>): Unit? {
        App.hidden.clear()
        val packageManager = context.get()!!.packageManager
        var skippedapps = 0
        val pacslist = packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
        val ICONSIZE = (65 * context.get()!!.resources.displayMetrics.density).toInt()
        var themeRes: Resources? = null
        val iconpackName = Settings["iconpack", "system"]
        var intresiconback = 0
        var intresiconfront = 0
        var intresiconmask = 0
        val p = Paint(Paint.FILTER_BITMAP_FLAG).apply { isAntiAlias = true }
        val maskp = Paint(Paint.FILTER_BITMAP_FLAG).apply {
            isAntiAlias = true
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        }
        if (iconpackName.compareTo("") != 0) {
            try { themeRes = packageManager.getResourcesForApplication(iconpackName) }
            catch (ignore: Exception) {}
            if (themeRes != null) {
                val backAndMaskAndFront = ThemeTools.getIconBackAndMaskResourceName(themeRes, iconpackName)
                if (backAndMaskAndFront[0] != null) intresiconback = themeRes.getIdentifier(backAndMaskAndFront[0], "drawable", iconpackName)
                if (backAndMaskAndFront[1] != null) intresiconmask = themeRes.getIdentifier(backAndMaskAndFront[1], "drawable", iconpackName)
                if (backAndMaskAndFront[2] != null) intresiconfront = themeRes.getIdentifier(backAndMaskAndFront[2], "drawable", iconpackName)
            }
        }
        val uniformOptions = BitmapFactory.Options()
        uniformOptions.inScaled = false
        val scaleFactor = ThemeTools.getScaleFactor(themeRes, iconpackName)
        var back: Bitmap? = null
        var mask: Bitmap? = null
        var front: Bitmap? = null
        if (iconpackName.compareTo("") != 0 && themeRes != null) {
            if (intresiconback != 0) back = BitmapFactory.decodeResource(themeRes, intresiconback, uniformOptions)
            if (intresiconmask != 0) mask = BitmapFactory.decodeResource(themeRes, intresiconmask, uniformOptions)
            if (intresiconfront != 0) front = BitmapFactory.decodeResource(themeRes, intresiconfront, uniformOptions)
        }
        tmpApps = arrayOfNulls(pacslist.size)
        for (i in pacslist.indices) {
            val app = App()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    app.icon = Tools.adaptic(context.get()!!, packageManager.getActivityIcon(ComponentName(
                        pacslist[i].activityInfo.packageName, pacslist[i].activityInfo.name)))
                } catch (e: Exception) {
                    app.icon = pacslist[i].loadIcon(packageManager)
                    e.printStackTrace()
                }
            } else app.icon = pacslist[i].loadIcon(packageManager)
            app.packageName = pacslist[i].activityInfo.packageName
            app.name = pacslist[i].activityInfo.name
            app.label = Settings[app.packageName + "/" + app.name + "?label", pacslist[i].loadLabel(packageManager).toString()]
            var intres = 0
            val iconResource = ThemeTools.getResourceName(themeRes, iconpackName, "ComponentInfo{" + app.packageName + "/" + app.name + "}")
            if (iconResource != null) intres = themeRes!!.getIdentifier(iconResource, "drawable", iconpackName)
            if (intres != 0) try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) app.icon = Tools.adaptic(context.get()!!, themeRes!!.getDrawable(intres)!!)
                else app.icon = themeRes!!.getDrawable(intres)
            } catch (e: Exception) { e.printStackTrace() } else try {
                var orig = Bitmap.createBitmap(app.icon!!.intrinsicWidth, app.icon!!.intrinsicHeight, Bitmap.Config.ARGB_8888)
                app.icon!!.setBounds(0, 0, app.icon!!.intrinsicWidth, app.icon!!.intrinsicHeight)
                app.icon!!.draw(Canvas(orig))
                val scaledOrig = Bitmap.createBitmap(ICONSIZE, ICONSIZE, Bitmap.Config.ARGB_8888)
                val scaledBitmap = Bitmap.createBitmap(ICONSIZE, ICONSIZE, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(scaledBitmap)
                if (back != null) canvas.drawBitmap(back, Tools.getResizedMatrix(back, ICONSIZE, ICONSIZE), p)
                val origCanv = Canvas(scaledOrig)
                orig = Tools.getResizedBitmap(orig, (ICONSIZE * scaleFactor).toInt(), (ICONSIZE * scaleFactor).toInt())
                origCanv.drawBitmap(orig, scaledOrig.width - orig.width / 2f - scaledOrig.width / 2f, scaledOrig.width - orig.width / 2f - scaledOrig.width / 2f, p)
                if (mask != null) origCanv.drawBitmap(mask, Tools.getResizedMatrix(mask, ICONSIZE, ICONSIZE), maskp)
                canvas.drawBitmap(Tools.getResizedBitmap(scaledOrig, ICONSIZE, ICONSIZE), 0f, 0f, p)
                if (front != null) canvas.drawBitmap(front, Tools.getResizedMatrix(front, ICONSIZE, ICONSIZE), p)
                app.icon = BitmapDrawable(context.get()!!.resources, scaledBitmap)
            } catch (e: Exception) { e.printStackTrace() }
            val customIcon = Settings["app:" + app.packageName + ":icon", ""]
            if (customIcon != "") try {
                val data = customIcon.split(':').toTypedArray()[1].split('|').toTypedArray()
                val t = packageManager.getResourcesForApplication(data[0])
                val intRes = t.getIdentifier(data[1], "drawable", data[0])
                app.icon = t.getDrawable(intRes)
            } catch (e: Exception) { e.printStackTrace() }
            try { if (!(context.get()!!.getSystemService(Context.POWER_SERVICE) as PowerManager).isPowerSaveMode && Settings["animatedicons", true]) Tools.animate(app.icon!!) }
            catch (ignore: Exception) {}
            App.putInSecondMap(app.packageName + "/" + app.name, app)
            if (Settings[pacslist[i].activityInfo.packageName + "/" + pacslist[i].activityInfo.name + "?hidden", false]) {
                skippedapps++
                App.hidden.add(app)
            } else tmpApps[i - skippedapps] = app
        }
        tmpApps = tmpApps.copyOf(tmpApps.size - skippedapps)
        if (Settings["drawer:sorting", 1] == 1) Sort.colorSort(tmpApps)
        else {
            var i = 0
            var j: Int
            var temp: App
            while (i < tmpApps.size - 1) {
                j = i + 1
                while (j < tmpApps.size) {
                    if (tmpApps[i]!!.label!!.compareTo(tmpApps[j]!!.label!!, ignoreCase = true) > 0) {
                        temp = tmpApps[i]!!
                        tmpApps[i] = tmpApps[j]
                        tmpApps[j] = temp
                    }
                    j++
                }
                i++
            }
        }

        if (Settings["drawer:sections_enabled", false]) {
            var currentChar = tmpApps[0]!!.label!![0].toUpperCase()
            var currentSection = ArrayList<App>().also { tmpAppSections.add(it) }
            for (app in tmpApps) {
                if (app!!.label!!.startsWith(currentChar, ignoreCase = true)) currentSection.add(app)
                else currentSection = ArrayList<App>().apply {
                    add(app)
                    tmpAppSections.add(this)
                    currentChar = app.label!![0].toUpperCase()
                }
            }
        }
        return null
    }

    override fun onPostExecute(v: Unit?) {
        Main.apps = tmpApps
        Main.appSections = tmpAppSections
        App.swapMaps()
        App.clearSecondMap()
        onEnd()
    }
}