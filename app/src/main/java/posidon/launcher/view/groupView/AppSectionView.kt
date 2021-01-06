package posidon.launcher.view.groupView

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewParent
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.items.LauncherItem
import posidon.launcher.items.users.ItemLongPress
import posidon.launcher.storage.Settings
import posidon.launcher.tools.ThemeTools
import posidon.launcher.tools.dp
import posidon.launcher.tools.sp
import kotlin.math.min

class AppSectionView(context: Context) : ItemGroupView(context) {

    private val appSize = when (Settings["icsize", 1]) {
        0 -> 64.dp.toInt()
        2 -> 84.dp.toInt()
        else -> 74.dp.toInt()
    }

    init {
        when (Settings["drawer:sec_name_pos", 0]) {
            0 -> {
                orientation = VERTICAL
            }
            1 -> {
                orientation = HORIZONTAL
                textView.layoutParams.run {
                    width = 28.sp.toInt()
                }
                textView.gravity = Gravity.END
                textView.setPaddingRelative(0, 0, 0, 0)
            }
        }

        gridLayout.run {
            columnCount = Settings["drawer:columns", 4]
            if (Settings["drawer:columns", 4] > 2) {
                setPaddingRelative(12.dp.toInt(), 0, 0, 0)
            }
        }
        textView.run {
            setTextColor(Settings["labelColor", -0x11111112])
        }
    }

    override fun getItemView (item: LauncherItem, parent: ViewParent): View {
        item as App
        val columns = Settings["drawer:columns", 4]
        val parentWidth = (parent as View).measuredWidth
        return (if (columns > 2) {
            LayoutInflater.from(context).inflate(R.layout.drawer_item, gridLayout, false).apply {
                layoutParams.width = min(if (Settings["drawer:scrollbar:enabled", false]) {
                    parentWidth - 24.dp.toInt() - /* Scrollbar width -> */ 24.dp.toInt()
                } else { parentWidth - 24.dp.toInt() } / columns, appSize)
            }
        } else LayoutInflater.from(context).inflate(R.layout.list_item, gridLayout, false).apply {
            if (columns == 2) {
                layoutParams.width = if (Settings["drawer:scrollbar:enabled", false]) {
                    parentWidth - 24.dp.toInt() - /* Scrollbar width -> */ 24.dp.toInt()
                } else { parentWidth - 24.dp.toInt() } / 2
            }
        }).apply {
            findViewById<ImageView>(R.id.iconimg).setImageDrawable(item.icon)
            findViewById<View>(R.id.iconFrame).run {
                layoutParams.height = appSize
                layoutParams.width = appSize
            }
            findViewById<TextView>(R.id.icontxt).run {
                if (Settings["labelsenabled", false]) {
                    text = item.label
                    visibility = View.VISIBLE
                    setTextColor(Settings["labelColor", -0x11111112])
                } else visibility = View.GONE
            }
            findViewById<TextView>(R.id.notificationBadge).run {
                if (Settings["notif:badges", true] && item.notificationCount != 0) {
                    visibility = View.VISIBLE
                    text = if (Settings["notif:badges:show_num", true]) item.notificationCount.toString() else ""
                    ThemeTools.generateNotificationBadgeBGnFG(item.icon!!) { bg, fg ->
                        background = bg
                        setTextColor(fg)
                    }
                } else { visibility = View.GONE }
            }
            setOnClickListener { item.open(context, it) }
            setOnLongClickListener {
                ItemLongPress.showPopupWindow(context, it, item, { item.showAppEditDialog(context, it) }, null)
                true
            }
            (layoutParams as GridLayout.LayoutParams).bottomMargin = Settings["verticalspacing", 12].dp.toInt()
        }
    }
}