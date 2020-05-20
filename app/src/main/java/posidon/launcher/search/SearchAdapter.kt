package posidon.launcher.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.storage.Settings
import posidon.launcher.tools.dp

internal class SearchAdapter(private val context: Context, private val results: ArrayList<App>) : BaseAdapter() {

    override fun getCount(): Int = results.size
    override fun getItem(position: Int): Any? = null
    override fun getItemId(position: Int): Long = 0

    override fun getView(position: Int, cv: View?, parent: ViewGroup): View? {
        var convertView = cv
        if (convertView == null) convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        val icon = convertView!!.findViewById<ImageView>(R.id.iconimg)
        val text = convertView.findViewById<TextView>(R.id.icontxt)
        icon.setImageDrawable(results[position].icon)
        text.text = results[position].label
        text.setTextColor(Settings["searchtxtcolor", -0x1])
        var appSize = 0
        when (Settings["icsize", 1]) {
            0 -> appSize = 64.dp.toInt()
            1 -> appSize = 72.dp.toInt()
            2 -> appSize = 96.dp.toInt()
        }
        icon.layoutParams.height = appSize
        icon.layoutParams.width = appSize
        return convertView
    }

}