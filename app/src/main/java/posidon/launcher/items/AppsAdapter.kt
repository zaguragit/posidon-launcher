package posidon.launcher.items

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.SectionIndexer
import android.widget.TextView
import androidx.palette.graphics.Palette
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.ColorTools
import posidon.launcher.tools.dp
import posidon.launcher.tools.toBitmap

class AppsAdapter(
    private val context: Context,
    private val apps: Array<App>
) : BaseAdapter(), SectionIndexer {

    override fun getCount(): Int = apps.size
    override fun getItem(position: Int) = null
    override fun getItemId(position: Int): Long = 0

    class ViewHolder(
        var icon: ImageView,
        var iconFrame: View,
        var text: TextView)

    override fun getView(position: Int, cv: View?, parent: ViewGroup): View? {
        var convertView = cv
        val viewHolder: ViewHolder
        if (convertView == null) {
            val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            if (Settings["drawer:columns", 4] > 2) convertView = li.inflate(R.layout.drawer_item, null) else {
                convertView = li.inflate(R.layout.list_item, null)
                if (Settings["drawer:columns", 4] == 2) convertView.findViewById<TextView>(R.id.icontxt).textSize = 18f
            }
            viewHolder = ViewHolder(
                convertView.findViewById(R.id.iconimg),
                convertView.findViewById(R.id.iconFrame),
                convertView.findViewById(R.id.icontxt))
            convertView.tag = viewHolder
        } else viewHolder = convertView.tag as ViewHolder

        val app = apps[position]
        viewHolder.icon.setImageDrawable(app.icon)
        if (Settings["labelsenabled", false]) {
            viewHolder.text.text = app.label
            viewHolder.text.visibility = View.VISIBLE
            viewHolder.text.setTextColor(Settings["labelColor", -0x11111112])
        } else viewHolder.text.visibility = View.INVISIBLE

        var appSize = 0
        when (Settings["icsize", 1]) {
            0 -> appSize = 64.dp.toInt()
            1 -> appSize = 74.dp.toInt()
            2 -> appSize = 84.dp.toInt()
        }
        viewHolder.iconFrame.layoutParams.height = appSize
        viewHolder.iconFrame.layoutParams.width = appSize
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