package posidon.launcher.items

import android.os.Build
import android.os.Bundle
import android.os.Process
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.palette.graphics.Palette
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
        val shortcut = Main.launcherApps.getPinItemRequest(intent).shortcutInfo
        if (shortcut != null) {
            Main.launcherApps.pinShortcuts(
                shortcut.`package`,
                listOf(shortcut.id),
                Process.myUserHandle()
            )
        }

        findViewById<View>(R.id.docksearchbar).visibility = View.GONE
        findViewById<View>(R.id.battery).visibility = View.GONE

        val container = findViewById<GridLayout>(R.id.dockContainer)
        container.removeAllViews()
        val columnCount = Settings["dock:columns", 5]
        val rowCount = Settings["dock:rows", 1]
        val showLabels = Settings["dockLabelsEnabled", false]
        val notifBadgesEnabled = Settings["notif:badges", true]
        container.columnCount = columnCount
        container.rowCount = rowCount
        val appSize = min(when (Settings["dockicsize", 1]) {
            0 -> 64.dp.toInt()
            2 -> 84.dp.toInt()
            else -> 74.dp.toInt()
        }, ((Device.displayWidth - 32.dp) / columnCount).toInt())
        var i = 0
        while (i < columnCount * rowCount) {
            val view = LayoutInflater.from(applicationContext).inflate(R.layout.drawer_item, null)
            val img = view.findViewById<ImageView>(R.id.iconimg)
            view.findViewById<View>(R.id.iconFrame).run {
                layoutParams.height = appSize
                layoutParams.width = appSize
            }
            val item = Dock[i]
            if (item is Folder) {
                img.setImageDrawable(item.icon)
                if (showLabels) {
                    view.findViewById<TextView>(R.id.icontxt).text = item.label
                    view.findViewById<TextView>(R.id.icontxt).setTextColor(Settings["dockLabelColor", -0x11111112])
                }
                if (notifBadgesEnabled) {
                    var notificationCount = 0
                    for (app in item.apps) {
                        notificationCount += app.notificationCount
                    }
                    if (notificationCount != 0) {
                        val badge = view.findViewById<TextView>(R.id.notificationBadge)
                        badge.visibility = View.VISIBLE
                        badge.text = notificationCount.toString()
                        badge.background = ColorTools.iconBadge(Main.accentColor)
                        badge.setTextColor(if (ColorTools.useDarkText(Main.accentColor)) 0xff111213.toInt() else 0xffffffff.toInt())
                    } else { view.findViewById<TextView>(R.id.notificationBadge).visibility = View.GONE }
                } else { view.findViewById<TextView>(R.id.notificationBadge).visibility = View.GONE }
            } else if (item is Shortcut) {
                if (item.isInstalled(packageManager)) {
                    if (showLabels) {
                        view.findViewById<TextView>(R.id.icontxt).text = item.label
                        view.findViewById<TextView>(R.id.icontxt).setTextColor(Settings["dockLabelColor", -0x11111112])
                    }
                    img.setImageDrawable(item.icon)
                } else {
                    Dock[i] = null
                }
            } else if (item is App) {
                if (!item.isInstalled(packageManager)) {
                    Dock[i] = null
                    continue
                }
                if (showLabels) {
                    view.findViewById<TextView>(R.id.icontxt).text = item.label
                    view.findViewById<TextView>(R.id.icontxt).setTextColor(Settings["dockLabelColor", -0x11111112])
                }
                if (notifBadgesEnabled && item.notificationCount != 0) {
                    val badge = view.findViewById<TextView>(R.id.notificationBadge)
                    badge.visibility = View.VISIBLE
                    badge.text = item.notificationCount.toString()
                    Palette.from(item.icon!!.toBitmap()).generate {
                        val color = it?.getDominantColor(0xff111213.toInt()) ?: 0xff111213.toInt()
                        badge.background = ColorTools.iconBadge(color)
                        badge.setTextColor(if (ColorTools.useDarkText(color)) 0xff111213.toInt() else 0xffffffff.toInt())
                    }
                } else { view.findViewById<TextView>(R.id.notificationBadge).visibility = View.GONE }
                img.setImageDrawable(item.icon)
            }
            view.setOnClickListener {
                Dock.add(Shortcut(shortcut!!), i)
                Main.setDock()
                finishAffinity()
            }
            container.addView(view)
            i++
        }
        container.layoutParams.height = appSize * rowCount + if (Settings["dockLabelsEnabled", false]) (18.dp * rowCount).toInt() else 0
    }
}