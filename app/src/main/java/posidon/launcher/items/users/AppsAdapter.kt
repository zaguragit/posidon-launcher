package posidon.launcher.items.users

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.SectionIndexer
import android.widget.TextView
import posidon.android.conveniencelib.dp
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.storage.Settings
import posidon.launcher.tools.theme.Customizer

class AppsAdapter(
    private val context: Context,
    private val apps: Array<App>
) : BaseAdapter(), SectionIndexer {

    override fun getCount(): Int = apps.size
    override fun getItem(position: Int) = null
    override fun getItemId(position: Int): Long = 0

    private val appSize = context.dp(Settings["drawer:icons:size", 64]).toInt()

    class ViewHolder(
        var icon: ImageView,
        var iconFrame: View,
        var text: TextView)

    override fun getView(position: Int, cv: View?, parent: ViewGroup): View? {
        var convertView = cv
        val holder: ViewHolder
        val app = apps[position]
        if (convertView == null) {
            val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = if (Settings["drawer:columns", 5] > 2) li.inflate(R.layout.drawer_item, parent, false) else {
                li.inflate(R.layout.list_item, parent, false)
            }
            holder = ViewHolder(
                convertView.findViewById(R.id.iconimg),
                convertView.findViewById(R.id.iconFrame),
                convertView.findViewById(R.id.icontxt))

            if (Settings["labelsenabled", true]) {
                holder.text.text = app.label
                holder.text.visibility = View.VISIBLE
                Customizer.styleLabel("drawer:labels", holder.text, 0x70ffffff, 12f)
            } else holder.text.visibility = View.INVISIBLE

            holder.iconFrame.layoutParams.height = appSize
            holder.iconFrame.layoutParams.width = appSize

            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
            holder.text.text = app.label
        }
        holder.icon.setImageDrawable(app.icon)
        return convertView
    }

    private val savedSections = ArrayList<Char>().apply {
        for (i in apps.indices) {
            val char = apps[i].label[0].uppercaseChar()
            if (i == 0 || apps[i - 1].label[0].uppercaseChar() != char) add(char)
        }
    }.toTypedArray()

    override fun getSections(): Array<Char> = savedSections

    override fun getSectionForPosition(i: Int): Int =
            savedSections.indexOf(apps[i].label[0].uppercaseChar())

    override fun getPositionForSection(i: Int): Int {
        val char = savedSections[i]
        for (j in apps.indices) if (apps[j].label[0].uppercaseChar() == char) return j
        return 0
    }

}