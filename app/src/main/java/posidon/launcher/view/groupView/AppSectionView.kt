package posidon.launcher.view.groupView

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewParent
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import io.posidon.android.conveniencelib.units.dp
import io.posidon.android.conveniencelib.units.sp
import io.posidon.android.conveniencelib.units.toPixels
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.items.LauncherItem
import posidon.launcher.items.users.ItemLongPress
import posidon.launcher.storage.Settings
import posidon.launcher.tools.theme.Customizer
import posidon.launcher.tools.theme.Icons
import posidon.launcher.view.drawer.DrawerView
import kotlin.math.min

@SuppressLint("ViewConstructor")
class AppSectionView(val drawer: DrawerView, val themeKey: String) : ItemGroupView(drawer.context) {

    private val appSize = Settings["$themeKey:icons:size", 64].dp.toPixels(context)

    var columns = Settings["$themeKey:columns", 4]
    val labelsEnabled = Settings["$themeKey:labels:enabled", true]

    init {
        when (Settings["$themeKey:sections:name:position", 0]) {
            0 -> orientation = VERTICAL
            1 -> {
                orientation = HORIZONTAL
                textView.layoutParams.width = 28.sp.toPixels(context)
                textView.run {
                    setPadding(0, 0, 0, 0)
                    gravity = Gravity.END
                }
            }
        }

        gridLayout.run {
            columnCount = columns
            if (columns > 2) {
                setPaddingRelative(12.dp.toPixels(context), 0, 0, 0)
            }
        }
        textView.run {
            setTextColor(Settings["$themeKey:labels:color", -0x11111112])
        }
    }

    override fun getItemView (item: LauncherItem, parent: ViewParent): View {
        item as App
        val parentWidth = (parent as View).measuredWidth
        return (if (columns > 2) {
            LayoutInflater.from(context).inflate(R.layout.drawer_item, gridLayout, false).apply {
                layoutParams.width = min(if (Settings["drawer:scrollbar:enabled", false]) {
                    parentWidth - 24.dp.toPixels(context) - /* Scrollbar width -> */ 24.dp.toPixels(context)
                } else { parentWidth - 24.dp.toPixels(context) } / columns, appSize)
            }
        } else LayoutInflater.from(context).inflate(R.layout.list_item, gridLayout, false).apply {
            if (columns == 2) {
                layoutParams.width = if (Settings["drawer:scrollbar:enabled", false]) {
                    parentWidth - 24.dp.toPixels(context) - /* Scrollbar width -> */ 24.dp.toPixels(context)
                } else { parentWidth - 24.dp.toPixels(context) } / 2
            }
        }).apply {
            findViewById<ImageView>(R.id.iconimg).setImageDrawable(item.icon)
            findViewById<View>(R.id.iconFrame).run {
                layoutParams.height = appSize
                layoutParams.width = appSize
            }
            findViewById<TextView>(R.id.icontxt).run {
                if (labelsEnabled) {
                    text = item.label
                    visibility = View.VISIBLE
                    Customizer.styleLabel("$themeKey:labels", this, -0x11111112, 12)
                } else visibility = View.GONE
            }
            findViewById<TextView>(R.id.notificationBadge).run {
                if (Settings["notif:badges", true] && item.notificationCount != 0) {
                    visibility = View.VISIBLE
                    text = if (Settings["notif:badges:show_num", true]) item.notificationCount.toString() else ""
                    Icons.generateNotificationBadgeBGnFG(item.icon!!) { bg, fg ->
                        background = bg
                        setTextColor(fg)
                    }
                } else { visibility = View.GONE }
            }
            setOnClickListener { item.open(context, it) }
            setOnLongClickListener {
                ItemLongPress.onItemLongPress(context, it, item, null, {
                    item.setHidden()
                    drawer.loadApps()
                }, removeFunction = ItemLongPress.HIDE)
                true
            }
            (layoutParams as GridLayout.LayoutParams).bottomMargin = Settings["verticalspacing", 12].dp.toPixels(context)
        }
    }
}