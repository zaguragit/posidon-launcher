package posidon.launcher.items

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.SectionIndexer
import android.widget.TextView
import posidon.launcher.R
import posidon.launcher.storage.Settings

class AppsAdapter(private val context: Context, private val apps: Array<App>) : BaseAdapter(), SectionIndexer {

    override fun getCount(): Int = apps.size
    override fun getItem(position: Int) = null
    override fun getItemId(position: Int): Long = 0

    internal class ViewHolder {
        var icon: ImageView? = null
        var text: TextView? = null
    }

    override fun getView(position: Int, cv: View?, parent: ViewGroup): View? {
        var convertView = cv
        val viewHolder: ViewHolder
        val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (convertView == null) {
            if (Settings["drawer:columns", 4] > 2) convertView = li.inflate(R.layout.drawer_item, null) else {
                convertView = li.inflate(R.layout.list_item, null)
                if (Settings["drawer:columns", 4] == 2) convertView.findViewById<TextView>(R.id.icontxt).textSize = 18f
            }
            viewHolder = ViewHolder()
            viewHolder.icon = convertView.findViewById(R.id.iconimg)
            viewHolder.text = convertView.findViewById(R.id.icontxt)
            convertView.tag = viewHolder
        } else viewHolder = convertView.tag as ViewHolder
        viewHolder.icon!!.setImageDrawable(apps[position].icon)
        if (Settings["labelsenabled", false]) {
            viewHolder.text!!.text = apps[position].label
            viewHolder.text!!.visibility = View.VISIBLE
            viewHolder.text!!.setTextColor(Settings["labelColor", -0x11111112])
        } else viewHolder.text!!.visibility = View.INVISIBLE
        var appSize = 0
        when (Settings["icsize", 1]) {
            0 -> appSize = (context.resources.displayMetrics.density * 64).toInt()
            1 -> appSize = (context.resources.displayMetrics.density * 74).toInt()
            2 -> appSize = (context.resources.displayMetrics.density * 84).toInt()
        }
        viewHolder.icon!!.layoutParams.height = appSize
        viewHolder.icon!!.layoutParams.width = appSize
        return convertView
    }

    private val savedSections = ArrayList<Char>().apply {
        for (i in apps.indices) {
            val char = apps[i].label!![0].toUpperCase()
            if (i == 0 || apps[i - 1].label!![0].toUpperCase() != char) add(char)
        }
    }.toTypedArray()

    override fun getSections(): Array<Char> = savedSections

    override fun getSectionForPosition(i: Int): Int =
            savedSections.indexOf(apps[i].label!![0].toUpperCase())

    override fun getPositionForSection(i: Int): Int {
        val char = savedSections[i]
        for (j in apps.indices) if (apps[j].label!![0].toUpperCase() == char) return j
        return 0
    }

}