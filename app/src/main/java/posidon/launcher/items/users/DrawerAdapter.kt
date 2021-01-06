package posidon.launcher.items.users

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.ThemeTools
import posidon.launcher.tools.Tools
import posidon.launcher.tools.dp
import posidon.launcher.view.HighlightAdapter

class DrawerAdapter : BaseAdapter(), SectionIndexer, HighlightAdapter {

    override fun getCount(): Int = Global.apps.size
    override fun getItem(i: Int) = Global.apps[i]
    override fun getItemId(i: Int): Long = 0

    private val appSize = when (Settings["icsize", 1]) {
        0 -> 64.dp.toInt()
        2 -> 84.dp.toInt()
        else -> 74.dp.toInt()
    }

    internal class ViewHolder(
        val icon: ImageView,
        val iconFrame: FrameLayout,
        val text: TextView,
        val notificationBadge: TextView)

    override fun getView(i: Int, cv: View?, parent: ViewGroup): View {
        var convertView = cv
        val holder: ViewHolder
        val app = Global.apps[i]
        if (convertView == null) {
            val li = Tools.appContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = if (Settings["drawer:columns", 4] > 2) li.inflate(R.layout.drawer_item, parent, false)
                else li.inflate(R.layout.list_item, parent, false)
            holder = ViewHolder(
                convertView.findViewById(R.id.iconimg),
                convertView.findViewById(R.id.iconFrame),
                convertView.findViewById(R.id.icontxt),
                convertView.findViewById(R.id.notificationBadge))
            holder.iconFrame.layoutParams.height = appSize
            holder.iconFrame.layoutParams.width = appSize
            convertView.tag = holder

            if (Settings["labelsenabled", false]) {
                holder.text.text = app.label
                holder.text.visibility = View.VISIBLE
                holder.text.setTextColor(Settings["labelColor", -0x11111112])
                val maxLines = Settings["drawer:labels:max_lines", 1]
                holder.text.isSingleLine = maxLines == 1
                holder.text.maxLines = maxLines
                holder.text.ellipsize = if (Settings["drawer:labels:marquee", true] && maxLines == 1) TextUtils.TruncateAt.MARQUEE else TextUtils.TruncateAt.END
                holder.text.isSelected = true
                holder.text.isHorizontalFadingEdgeEnabled = true
                holder.text.setFadingEdgeLength(5.dp.toInt())
            } else holder.text.visibility = View.INVISIBLE

        } else {
            holder = convertView.tag as ViewHolder
            holder.text.text = app.label
        }
        convertView!!.background = if (highlightI == i) HighlightAdapter.createHighlightDrawable() else null
        holder.icon.setImageDrawable(app.icon)
        if (Settings["notif:badges", true] && app.notificationCount != 0) {
            val badge = holder.notificationBadge
            badge.visibility = View.VISIBLE
            badge.text = if (Settings["notif:badges:show_num", true]) app.notificationCount.toString() else ""
            ThemeTools.generateNotificationBadgeBGnFG(app.icon!!) { bg, fg ->
                badge.background = bg
                badge.setTextColor(fg)
            }
        } else {
            holder.notificationBadge.visibility = View.GONE
        }
        return convertView
    }

    private val savedSections = Array(Global.appSections.size) { Global.appSections[it][0].label!![0].toUpperCase() }

    override fun getSections(): Array<Char> = savedSections

    override fun getSectionForPosition(i: Int): Int {
        val label = Global.apps[i].label!!
        return savedSections.indexOf(if (label.isEmpty()) ' ' else label[0].toUpperCase())
    }

    override fun getPositionForSection(i: Int): Int {
        val char = savedSections[i]
        for (j in Global.apps.indices) {
            val label = Global.apps[j].label!!
            if (char == if (label.isEmpty()) ' ' else label[0].toUpperCase()) {
                return j
            }
        }
        return 0
    }

    private var highlightI = -1

    override fun highlight(i: Int) {
        highlightI = i
    }

    override fun unhighlight() {
        highlightI = -1
    }
}