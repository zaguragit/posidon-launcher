package posidon.launcher.items

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.palette.graphics.Palette
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.ColorTools
import posidon.launcher.tools.Tools
import posidon.launcher.tools.dp
import posidon.launcher.tools.toBitmap

class DrawerAdapter : BaseAdapter(), SectionIndexer {

    override fun getCount(): Int = Main.apps.size
    override fun getItem(i: Int) = Main.apps[i]
    override fun getItemId(i: Int): Long = 0

    var appSize = when (Settings["icsize", 1]) {
        0 -> 64.dp.toInt()
        2 -> 84.dp.toInt()
        else -> 74.dp.toInt()
    }

    internal class ViewHolder(
        val icon: ImageView,
        val iconFrame: FrameLayout,
        val text: TextView,
        val notificationBadge: TextView)

    override fun getView(i: Int, cv: View?, parent: ViewGroup): View? {
        var convertView = cv
        val holder: ViewHolder
        val li = Tools.publicContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (convertView == null) {
            convertView = if (Settings["drawer:columns", 4] > 2) li.inflate(R.layout.drawer_item, parent, false)
            else li.inflate(R.layout.list_item, parent, false).apply {
                if (Settings["drawer:columns", 4] == 2)
                    findViewById<TextView>(R.id.icontxt).textSize = 18f
            }
            holder = ViewHolder(
                convertView.findViewById(R.id.iconimg),
                convertView.findViewById(R.id.iconFrame),
                convertView.findViewById(R.id.icontxt),
                convertView.findViewById(R.id.notificationBadge))
            holder.iconFrame.layoutParams.height = appSize
            holder.iconFrame.layoutParams.width = appSize
            convertView.tag = holder
        } else holder = convertView.tag as ViewHolder
        val app = Main.apps[i]
        holder.icon.setImageDrawable(app.icon)
        if (Settings["labelsenabled", false]) {
            holder.text.text = app.label
            holder.text.visibility = View.VISIBLE
            holder.text.setTextColor(Settings["labelColor", -0x11111112])
        } else holder.text.visibility = View.INVISIBLE
        if (Settings["notif:badges", true] && app.notificationCount != 0) {
            val badge = holder.notificationBadge
            badge.visibility = View.VISIBLE
            badge.text = app.notificationCount.toString()
            Palette.from(app.icon!!.toBitmap()).generate {
                val color = it?.getDominantColor(0xff111213.toInt()) ?: 0xff111213.toInt()
                badge.background = ColorTools.notificationBadge(color)
                badge.setTextColor(if (ColorTools.useDarkText(color)) 0xff111213.toInt() else 0xffffffff.toInt())
            }
        } else {
            holder.notificationBadge.visibility = View.GONE
        }
        return convertView
    }

    private val savedSections = Array(Main.appSections.size) { Main.appSections[it][0].label!![0].toUpperCase() }

    override fun getSections(): Array<Char> = savedSections

    override fun getSectionForPosition(i: Int): Int {
        val label = Main.apps[i].label!!
        return savedSections.indexOf(if (label.isEmpty()) ' ' else label[0].toUpperCase())
    }

    override fun getPositionForSection(i: Int): Int {
        val char = savedSections[i]
        for (j in Main.apps.indices) {
            val label = Main.apps[j].label!!
            if (char == if (label.isEmpty()) ' ' else label[0].toUpperCase()) {
                return j
            }
        }
        return 0
    }
}