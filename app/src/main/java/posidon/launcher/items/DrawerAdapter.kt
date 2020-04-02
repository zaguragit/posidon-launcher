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
import posidon.launcher.tools.Settings

class DrawerAdapter(private val context: Context) : BaseAdapter(), SectionIndexer {

    override fun getCount(): Int = Main.apps.size
    override fun getItem(i: Int) = Main.apps[i]
    override fun getItemId(i: Int): Long = 0

    var appSize = 0

    init {
        when (Settings["icsize", 1]) {
            0 -> appSize = (context.resources.displayMetrics.density * 64).toInt()
            1 -> appSize = (context.resources.displayMetrics.density * 74).toInt()
            2 -> appSize = (context.resources.displayMetrics.density * 84).toInt()
        }
    }

    internal class ViewHolder {
        var icon: ImageView? = null
        var text: TextView? = null
    }

    override fun getView(i: Int, cv: View?, parent: ViewGroup): View? {
        var convertView = cv
        val viewHolder: ViewHolder
        val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (convertView == null) {
            if (Settings["drawer:columns", 4] > 2) convertView = li.inflate(R.layout.drawer_item, null) else {
                convertView = li.inflate(R.layout.list_item, parent, false)
                if (Settings["drawer:columns", 4] == 2) (convertView.findViewById<View>(R.id.icontxt) as TextView).textSize = 18f
            }
            viewHolder = ViewHolder()
            viewHolder.icon = convertView.findViewById(R.id.iconimg)
            viewHolder.text = convertView.findViewById(R.id.icontxt)
            convertView.tag = viewHolder
        } else viewHolder = convertView.tag as ViewHolder
        //viewHolder.icon!!.setOnClickListener { Main.apps[i]!!.open(context, it) }
        //viewHolder.icon!!.setOnLongClickListener(ItemLongPress.drawer(context, i))
        viewHolder.icon!!.setImageDrawable(Main.apps[i]!!.icon)
        if (Settings["labelsenabled", false]) {
            viewHolder.text!!.text = Main.apps[i]!!.label
            viewHolder.text!!.visibility = View.VISIBLE
            viewHolder.text!!.setTextColor(Settings["labelColor", -0x11111112])
        } else viewHolder.text!!.visibility = View.INVISIBLE
        viewHolder.icon!!.layoutParams.height = appSize
        viewHolder.icon!!.layoutParams.width = appSize
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