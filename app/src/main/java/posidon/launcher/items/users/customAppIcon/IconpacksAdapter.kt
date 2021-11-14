package posidon.launcher.items.users.customAppIcon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import posidon.android.conveniencelib.dp
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.storage.Settings
import posidon.launcher.tools.theme.Customizer

class IconpacksAdapter(val iconPacks: List<App>, val onSelectIconPack: (String) -> Unit) : RecyclerView.Adapter<IconpacksAdapter.ViewHolder>() {

    class ViewHolder(
        view: View,
        val icon: ImageView,
        val text: TextView
    ) : RecyclerView.ViewHolder(view)

    override fun getItemCount(): Int = iconPacks.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val convertView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        val holder = ViewHolder(convertView,
            convertView.findViewById(R.id.iconimg),
            convertView.findViewById(R.id.icontxt))
        holder.text.visibility = View.VISIBLE
        Customizer.styleLabel("drawer:labels", holder.text, 0x70ffffff, 12f)
        convertView.findViewById<View>(R.id.iconFrame).layoutParams.run {
            val appSize = parent.dp(Settings["drawer:icons:size", 64]).toInt()
            width = appSize
            height = appSize
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        holder.icon.setImageDrawable(iconPacks[i].icon)
        holder.text.text = iconPacks[i].label
        holder.itemView.setOnClickListener {
            onSelectIconPack(iconPacks[i].packageName)
        }
    }
}
