package posidon.launcher.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import posidon.android.conveniencelib.dp
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.items.LauncherItem
import posidon.launcher.storage.Settings
import posidon.launcher.tools.theme.Icons

internal class SearchAdapter(
    private val context: Context,
    private val results: List<LauncherItem>
) : BaseAdapter() {

    override fun getCount(): Int = results.size
    override fun getItem(i: Int): Any = results[i]
    override fun getItemId(position: Int): Long = 0

    class ViewHolder(
        var icon: ImageView,
        var iconFrame: View,
        var text: TextView,
        var notificationBadge: TextView)

    override fun getView(position: Int, cv: View?, parent: ViewGroup): View? {
        var convertView = cv
        val holder: ViewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
            holder = ViewHolder(
                convertView.findViewById(R.id.iconimg),
                convertView.findViewById(R.id.iconFrame),
                convertView.findViewById(R.id.icontxt),
                convertView.findViewById(R.id.notificationBadge))
            convertView.tag = holder
        } else holder = convertView.tag as ViewHolder

        val icon = holder.icon
        val iconFrame = holder.iconFrame
        val text = holder.text
        val item = results[position]
        icon.setImageDrawable(item.icon)
        text.text = item.label
        text.setTextColor(Settings["searchtxtcolor", -0x1])
        if (item is App && Settings["notif:badges", true] && item.notificationCount != 0) {
            val badge = holder.notificationBadge
            badge.visibility = View.VISIBLE
            badge.text = if (Settings["notif:badges:show_num", true]) item.notificationCount.toString() else ""
            Icons.generateNotificationBadgeBGnFG(item.icon!!) { bg, fg ->
                badge.background = bg
                badge.setTextColor(fg)
            }
        } else {
            holder.notificationBadge.visibility = View.GONE
        }
        val appSize = context.dp(Settings["search:icons:size", 56]).toInt()
        iconFrame.layoutParams.height = appSize
        iconFrame.layoutParams.width = appSize
        return convertView
    }

}