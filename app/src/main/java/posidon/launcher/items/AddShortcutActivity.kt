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
import posidon.launcher.storage.Settings
import posidon.launcher.tools.*
import kotlin.math.min

@RequiresApi(Build.VERSION_CODES.O)
class AddShortcutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.add_shortcut_activity)
        val launcherApps = getSystemService(LauncherApps::class.java)!!
        val shortcut = launcherApps.getPinItemRequest(intent).shortcutInfo
        if (shortcut != null) {
            Main.launcherApps.pinShortcuts(shortcut.`package`, ArrayList<String>().apply { add(shortcut.id) }, Process.myUserHandle())
        }

        findViewById<View>(R.id.docksearchbar).visibility = View.GONE
        findViewById<View>(R.id.battery).visibility = View.GONE

        var appSize = 0
        when (Settings["dockicsize", 1]) {
            0 -> appSize = 64.dp.toInt()
            1 -> appSize = 74.dp.toInt()
            2 -> appSize = 84.dp.toInt()
        }
        val data = Settings["dock", ""].split("\n").toTypedArray()
        val container = findViewById<GridLayout>(R.id.dockContainer)
        container.removeAllViews()
        val columnCount = Settings["dock:columns", 5]
        val rowCount = Settings["dock:rows", 1]
        val showLabels = Settings["dockLabelsEnabled", false]
        container.columnCount = columnCount
        container.rowCount = rowCount
        appSize = min(appSize, ((Tools.getDisplayWidth(this) - 32.dp) / columnCount).toInt())
        var i = 0
        while (i < data.size && i < columnCount * rowCount) {
            val string = data[i]
            val view = LayoutInflater.from(applicationContext).inflate(R.layout.drawer_item, null)
            val img = view.findViewById<ImageView>(R.id.iconimg)
            img.layoutParams.height = appSize
            img.layoutParams.width = appSize
            if (data[i].startsWith("folder(") && data[i].endsWith(")")) {
                val folder = Folder(data[i])
                img.setImageDrawable(folder.icon)
                if (showLabels) {
                    view.findViewById<TextView>(R.id.icontxt).text = folder.label
                    view.findViewById<TextView>(R.id.icontxt).setTextColor(Settings["dockLabelColor", -0x11111112])
                } else view.findViewById<View>(R.id.icontxt).visibility = View.GONE
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && data[i].startsWith("shortcut:")) {
                val shortcut = Shortcut(string)
                if (!showLabels) view.findViewById<View>(R.id.icontxt).visibility = View.GONE
                if (Tools.isInstalled(shortcut.packageName, packageManager)) {
                    if (showLabels) {
                        view.findViewById<TextView>(R.id.icontxt).text = shortcut.label
                        view.findViewById<TextView>(R.id.icontxt).setTextColor(Settings["dockLabelColor", -0x11111112])
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
                        view.findViewById<TextView>(R.id.icontxt).text = app.label
                        view.findViewById<TextView>(R.id.icontxt).setTextColor(Settings["dockLabelColor", -0x11111112])
                    }
                    img.setImageDrawable(app.icon)
                }
            }
            view.setOnClickListener {
                Dock.add(Shortcut(shortcut!!), i)
                //finish()
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