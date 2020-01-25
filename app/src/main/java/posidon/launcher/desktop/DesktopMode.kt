package posidon.launcher.desktop

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.tools.Settings.getBool
import posidon.launcher.tools.Settings.getInt
import posidon.launcher.tools.Settings.getString
import posidon.launcher.tools.Sort.colorSort
import posidon.launcher.tools.Sort.labelSort
import posidon.launcher.tools.ThemeTools
import posidon.launcher.tools.Tools
import java.util.*

class DesktopMode : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.desktop)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        val menuBtn = findViewById<ImageView>(R.id.menuBtn)
        Tools.animate(menuBtn.drawable)
        setApps()
    }

    fun showMenu(view: View?) = startActivity(Intent(this, AppList::class.java))

    private fun setApps() {
        var skippedapps = 0
        val pacslist = packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
        Main.apps = arrayOfNulls(pacslist.size)
        val ICONSIZE = Tools.numtodp(65, this)
        var themeRes: Resources? = null
        val iconpackName = getString("iconpack", "system")
        var iconResource: String?
        var intres: Int
        var intresiconback = 0
        var intresiconfront = 0
        var intresiconmask = 0
        val scaleFactor: Float
        val p = Paint(Paint.FILTER_BITMAP_FLAG)
        p.isAntiAlias = true
        val origP = Paint(Paint.FILTER_BITMAP_FLAG)
        origP.isAntiAlias = true
        val maskp = Paint(Paint.FILTER_BITMAP_FLAG)
        maskp.isAntiAlias = true
        maskp.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
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
        var origCanv: Canvas
        var canvas: Canvas
        scaleFactor = ThemeTools.getScaleFactor(themeRes, iconpackName)
        var back: Bitmap? = null
        var mask: Bitmap? = null
        var front: Bitmap? = null
        var scaledBitmap: Bitmap?
        var scaledOrig: Bitmap
        var orig: Bitmap
        if (iconpackName.compareTo("") != 0 && themeRes != null) {
            if (intresiconback != 0) back = BitmapFactory.decodeResource(themeRes, intresiconback, uniformOptions)
            if (intresiconmask != 0) mask = BitmapFactory.decodeResource(themeRes, intresiconmask, uniformOptions)
            if (intresiconfront != 0) front = BitmapFactory.decodeResource(themeRes, intresiconfront, uniformOptions)
        }
        for (i in pacslist.indices) {
            if (getBool(pacslist[i].activityInfo.packageName + "/" + pacslist[i].activityInfo.name + "?hidden", false)) skippedapps++ else {
                Main.apps[i - skippedapps] = App()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        Main.apps[i - skippedapps].icon = Tools.adaptic(this@DesktopMode, packageManager.getActivityIcon(ComponentName(pacslist[i].activityInfo.packageName, pacslist[i].activityInfo.name)))
                    } catch (e: Exception) { e.printStackTrace() }
                } else Main.apps[i - skippedapps].icon = pacslist[i].loadIcon(packageManager)
                Main.apps[i - skippedapps].packageName = pacslist[i].activityInfo.packageName
                Main.apps[i - skippedapps].name = pacslist[i].activityInfo.name
                Main.apps[i - skippedapps].label = getString(Main.apps[i - skippedapps].packageName + "/" + Main.apps[i - skippedapps].name + "?label", pacslist[i].loadLabel(packageManager).toString())
                intres = 0
                iconResource = ThemeTools.getResourceName(themeRes, iconpackName, "ComponentInfo{" + Main.apps[i - skippedapps].packageName + "/" + Main.apps[i - skippedapps].name + "}")
                if (iconResource != null) intres = themeRes!!.getIdentifier(iconResource, "drawable", iconpackName)
                if (intres != 0) try { //Do NOT add the theme parameter to getDrawable()
                    Main.apps[i - skippedapps].icon = themeRes!!.getDrawable(intres)
                    try {
                        if (!(getSystemService(Context.POWER_SERVICE) as PowerManager).isPowerSaveMode && getBool("animatedicons", true)) Tools.animate(Main.apps[i - skippedapps].icon)
                    } catch (ignore: Exception) {}
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Main.apps[i - skippedapps].icon = Tools.adaptic(this@DesktopMode, Main.apps[i - skippedapps].icon)
                } catch (e: Exception) { e.printStackTrace() } else {
                    orig = Bitmap.createBitmap(Main.apps[i - skippedapps].icon!!.intrinsicWidth, Main.apps[i - skippedapps].icon!!.intrinsicHeight, Bitmap.Config.ARGB_8888)
                    Main.apps[i - skippedapps].icon!!.setBounds(0, 0, Main.apps[i - skippedapps].icon!!.intrinsicWidth, Main.apps[i - skippedapps].icon!!.intrinsicHeight)
                    Main.apps[i - skippedapps].icon!!.draw(Canvas(orig))
                    scaledOrig = Bitmap.createBitmap(ICONSIZE, ICONSIZE, Bitmap.Config.ARGB_8888)
                    scaledBitmap = Bitmap.createBitmap(ICONSIZE, ICONSIZE, Bitmap.Config.ARGB_8888)
                    canvas = Canvas(scaledBitmap)
                    if (back != null) canvas.drawBitmap(back, Tools.getResizedMatrix(back, ICONSIZE, ICONSIZE), p)
                    origCanv = Canvas(scaledOrig)
                    orig = Tools.getResizedBitmap(orig, (ICONSIZE * scaleFactor).toInt(), (ICONSIZE * scaleFactor).toInt())
                    origCanv.drawBitmap(orig, scaledOrig.width - orig.width / 2f - scaledOrig.width / 2f, scaledOrig.width - orig.width / 2f - scaledOrig.width / 2f, origP)
                    if (mask != null) origCanv.drawBitmap(mask, Tools.getResizedMatrix(mask, ICONSIZE, ICONSIZE), maskp)
                    if (back != null) canvas.drawBitmap(Tools.getResizedBitmap(scaledOrig, ICONSIZE, ICONSIZE), 0f, 0f, p) else canvas.drawBitmap(Tools.getResizedBitmap(scaledOrig, ICONSIZE, ICONSIZE), 0f, 0f, p)
                    if (front != null) canvas.drawBitmap(front, Tools.getResizedMatrix(front, ICONSIZE, ICONSIZE), p)
                    Main.apps[i - skippedapps].icon = BitmapDrawable(resources, scaledBitmap)
                }
            }
        }
        Main.apps = Arrays.copyOf(Main.apps, Main.apps.size - skippedapps)
        if (getInt("sortAlgorithm", 1) == 1) colorSort(Main.apps) else labelSort(Main.apps)
    }
}