package posidon.launcher.items.users.customAppIcon

import android.content.res.Resources
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.dp
import posidon.launcher.tools.theme.Graphics
import posidon.launcher.tools.theme.Icons

internal class IconsAdapter(
    private val iconPack: String,
    private val icons: ArrayList<String>,
    private val themeRes: Resources,
    private val onSelectIcon: (String) -> Unit
) : RecyclerView.Adapter<IconsAdapter.ViewHolder>() {

    private val searchResults = ArrayList<String>()

    init {
        searchResults.addAll(icons)
    }

    fun search(term: String) {
        searchResults.clear()
        val searchOptimizedTerm = Tools.searchOptimize(term)
        for (string in icons) {
            if (Tools.searchOptimize(string).startsWith(searchOptimizedTerm)) {
                searchResults.add(string)
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = searchResults.size

    inner class ViewHolder(v: View, val icon: ImageView) : RecyclerView.ViewHolder(v)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val convertView = LayoutInflater.from(parent.context).inflate(R.layout.drawer_item, null)
        val viewHolder = ViewHolder(convertView, convertView.findViewById(R.id.iconimg))
        convertView.findViewById<View>(R.id.icontxt).visibility = View.GONE
        convertView.findViewById<View>(R.id.iconFrame).layoutParams.run {
            val appSize = when (Settings["icsize", 1]) {
                0 -> 64.dp.toInt()
                2 -> 84.dp.toInt()
                else -> 74.dp.toInt()
            }
            width = appSize
            height = appSize
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        val intRes = themeRes.getIdentifier(searchResults[i], "drawable", iconPack)
        if (intRes == 0) {
            holder.icon.setImageDrawable(null)
            holder.icon.setOnClickListener(null)
        } else {
            holder.icon.setImageDrawable(Graphics.tryAnimate(Icons.badgeMaybe(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Icons.generateAdaptiveIcon(themeRes.getDrawable(intRes))
            } else {
                themeRes.getDrawable(intRes)
            }, false)))
            holder.icon.setOnClickListener {
                onSelectIcon("ref:$iconPack|${searchResults[i]}")
            }
        }
    }
}