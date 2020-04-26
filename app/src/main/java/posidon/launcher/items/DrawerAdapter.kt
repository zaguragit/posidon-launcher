package posidon.launcher.items

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.SectionIndexer
import android.widget.TextView
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.dp

class DrawerAdapter : BaseAdapter(), SectionIndexer {

    override fun getCount(): Int = Main.apps.size
    override fun getItem(i: Int) = Main.apps[i]
    override fun getItemId(i: Int): Long = 0

    var appSize = when (Settings["icsize", 1]) {
        0 -> 64.dp.toInt()
        2 -> 84.dp.toInt()
        else -> 74.dp.toInt()
    }

    internal class ViewHolder {
        var icon: ImageView? = null
        var text: TextView? = null
    }

    override fun getView(i: Int, cv: View?, parent: ViewGroup): View? {
        var convertView = cv
        val viewHolder: ViewHolder
        val li = Tools.publicContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (convertView == null) {
            convertView = if (Settings["drawer:columns", 4] > 2) li.inflate(R.layout.drawer_item, parent, false)
            else li.inflate(R.layout.list_item, parent, false).apply {
                if (Settings["drawer:columns", 4] == 2)
                    findViewById<TextView>(R.id.icontxt).textSize = 18f
            }
            viewHolder = ViewHolder()
            viewHolder.icon = convertView.findViewById(R.id.iconimg)
            viewHolder.text = convertView.findViewById(R.id.icontxt)
            viewHolder.icon!!.layoutParams.height = appSize
            viewHolder.icon!!.layoutParams.width = appSize
            convertView.tag = viewHolder
        } else viewHolder = convertView.tag as ViewHolder
        viewHolder.icon!!.setImageDrawable(Main.apps[i]!!.icon)
        if (Settings["labelsenabled", false]) {
            viewHolder.text!!.text = Main.apps[i]!!.label
            viewHolder.text!!.visibility = View.VISIBLE
            viewHolder.text!!.setTextColor(Settings["labelColor", -0x11111112])
        } else viewHolder.text!!.visibility = View.INVISIBLE
        return convertView
    }

    private val savedSections = ArrayList<Char>().apply {
        for (i in Main.apps.indices) {
            val char = Main.apps[i]!!.label!![0].toUpperCase()
            if (i == 0 || Main.apps[i - 1]!!.label!![0].toUpperCase() != char) add(char)
        }
    }.toTypedArray()

    override fun getSections(): Array<Char> = savedSections

    override fun getSectionForPosition(i: Int): Int =
            savedSections.indexOf(Main.apps[i]!!.label!![0].toUpperCase())

    override fun getPositionForSection(i: Int): Int {
        val char = savedSections[i]
        for (j in Main.apps.indices) if (Main.apps[j]!!.label!![0].toUpperCase() == char) return j
        return 0
    }

}