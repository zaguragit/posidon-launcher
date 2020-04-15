package posidon.launcher.items

import android.content.pm.LauncherApps
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.tools.ColorTools
import posidon.launcher.tools.Dock
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import kotlin.math.min

@RequiresApi(Build.VERSION_CODES.O)
class AddShortcutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Tools.applyFontSetting(this)
        setContentView(R.layout.add_shortcut_activity)
        val launcherApps = getSystemService(LauncherApps::class.java)!!
        val shortcut = launcherApps.getPinItemRequest(intent).shortcutInfo
        if (shortcut != null) {
            Main.launcherApps.pinShortcuts(shortcut.`package`, ArrayList<String>().apply { add(shortcut.id) }, Process.myUserHandle())
        }

        if (Settings["docksearchbarenabled", false]) {
            findViewById<View>(R.id.docksearchbar).visibility = View.VISIBLE
            findViewById<View>(R.id.battery).visibility = View.VISIBLE
            val bg = ShapeDrawable()
            val tr = Settings["docksearchradius", 30] * resources.displayMetrics.density
            bg.shape = RoundRectShape(floatArrayOf(tr, tr, tr, tr, tr, tr, tr, tr), null, null)
            bg.paint.color = Settings["docksearchcolor", -0x22000001]
            findViewById<View>(R.id.docksearchbar).background = bg
            val t = findViewById<TextView>(R.id.docksearchtxt)
            t.setTextColor(Settings["docksearchtxtcolor", -0x1000000])
            t.text = Settings["searchhinttxt", "Search.."]
            (findViewById<View>(R.id.docksearchic) as ImageView).imageTintList = ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(Settings["docksearchtxtcolor", -0x1000000]))
            (findViewById<View>(R.id.docksearchic) as ImageView).imageTintMode = PorterDuff.Mode.MULTIPLY
            (findViewById<View>(R.id.battery) as ProgressBar).progressTintList = ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(Settings["docksearchtxtcolor", -0x1000000]))
            (findViewById<View>(R.id.battery) as ProgressBar).indeterminateTintMode = PorterDuff.Mode.MULTIPLY
            (findViewById<View>(R.id.battery) as ProgressBar).progressBackgroundTintList = ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(Settings["docksearchtxtcolor", -0x1000000]))
            (findViewById<View>(R.id.battery) as ProgressBar).progressBackgroundTintMode = PorterDuff.Mode.MULTIPLY
            ((findViewById<View>(R.id.battery) as ProgressBar).progressDrawable as LayerDrawable).getDrawable(3).setTint(if (ColorTools.useDarkText(Settings["docksearchtxtcolor", -0x1000000])) -0x23000000 else -0x11000001)
        } else {
            findViewById<View>(R.id.docksearchbar).visibility = View.GONE
            findViewById<View>(R.id.battery).visibility = View.GONE
        }

        var appSize = 0
        when (Settings["dockicsize", 1]) {
            0 -> appSize = (resources.displayMetrics.density * 64).toInt()
            1 -> appSize = (resources.displayMetrics.density * 74).toInt()
            2 -> appSize = (resources.displayMetrics.density * 84).toInt()
        }
        val data = Settings["dock", ""].split("\n").toTypedArray()
        val container = findViewById<GridLayout>(R.id.dockContainer)
        container.removeAllViews()
        val columnCount = Settings["dock:columns", 5]
        val rowCount = Settings["dock:rows", 1]
        val showLabels = Settings["dockLabelsEnabled", false]
        container.columnCount = columnCount
        container.rowCount = rowCount
        appSize = min(appSize, ((Tools.getDisplayWidth(this) - 32 * resources.displayMetrics.density) / columnCount).toInt())
        var i = 0
        while (i < data.size && i < columnCount * rowCount) {
            val string = data[i]
            val view = LayoutInflater.from(applicationContext).inflate(R.layout.drawer_item, null)
            val img = view.findViewById<ImageView>(R.id.iconimg)
            img.layoutParams.height = appSize
            img.layoutParams.width = appSize
            if (data[i].startsWith("folder(") && data[i].endsWith(")")) {
                val folder = Folder(this, data[i])
                img.setImageDrawable(folder.icon)
                if (showLabels) {
                    (view.findViewById<View>(R.id.icontxt) as TextView).text = folder.label
                    (view.findViewById<View>(R.id.icontxt) as TextView).setTextColor(Settings["dockLabelColor", -0x11111112])
                } else view.findViewById<View>(R.id.icontxt).visibility = View.GONE
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && data[i].startsWith("shortcut:")) {
                val shortcut = Shortcut(string)
                if (!showLabels) view.findViewById<View>(R.id.icontxt).visibility = View.GONE
                if (Tools.isInstalled(shortcut.packageName, packageManager)) {
                    if (showLabels) {
                        (view.findViewById<View>(R.id.icontxt) as TextView).text = shortcut.label
                        (view.findViewById<View>(R.id.icontxt) as TextView).setTextColor(Settings["dockLabelColor", -0x11111112])
                    }
                    img.setImageDrawable(shortcut.icon)
                } else {
                    data[i] = ""
                    Settings["dock"] = TextUtils.join("\n", data)
                }
            } else {
                val app = App[string]
                if (!showLabels) view.findViewById<View>(R.id.icontxt).visibility = View.GONE
                if (app == null) {
                    if (!Tools.isInstalled(string.split('/')[0], packageManager)) {
                        data[i] = ""
                        Settings["dock"] = TextUtils.join("\n", data)
                    }
                } else {
                    if (showLabels) {
                        (view.findViewById<View>(R.id.icontxt) as TextView).text = app.label
                        (view.findViewById<View>(R.id.icontxt) as TextView).setTextColor(Settings["dockLabelColor", -0x11111112])
                    }
                    img.setImageDrawable(app.icon)
                }
            }
            view.setOnClickListener {
                Dock[i] = Shortcut(shortcut!!)
                finish()
            }
            container.addView(view)
            i++
        }
        while (i < columnCount * rowCount) {
            val view = LayoutInflater.from(applicationContext).inflate(R.layout.drawer_item, null)
            val img = view.findViewById<ImageView>(R.id.iconimg)
            img.layoutParams.height = appSize
            img.layoutParams.width = appSize
            if (!showLabels) view.findViewById<View>(R.id.icontxt).visibility = View.GONE
            container.addView(view)
            i++
        }
        val containerHeight = (appSize * rowCount + resources.displayMetrics.density * if (Settings["dockLabelsEnabled", false]) 18 * rowCount else 0).toInt()
        container.layoutParams.height = containerHeight
    }
}