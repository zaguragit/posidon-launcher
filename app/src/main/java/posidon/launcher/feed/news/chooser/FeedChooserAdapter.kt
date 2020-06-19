package posidon.launcher.feed.news.chooser

import android.content.Context
import android.content.res.ColorStateList
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.vibrate

class FeedChooserAdapter(internal val context: Context, private val feedUrls: ArrayList<String>) : RecyclerView.Adapter<FeedChooserAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var card: LinearLayout = itemView.findViewById(R.id.card)
        var text: TextView = itemView.findViewById(R.id.txt)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.text.text = feedUrls[position]

        /*val usedUrls: ArrayList<String> = ArrayList()
        usedUrls.addAll(Settings.getString("feedUrls", "androidpolice.com")
                .replace("|http://", "|")
                .replace("|https://", "|")
                .replace("|www.", "|")
                .split("|"))

        val chosenColor = ColorTools.blendColors(Main.accentColor, context.resources.getColor(R.color.settingscardbg), 0.2f)

        if (usedUrls.contains(simplifyUrl(sources[position].url!!))) holder.card.backgroundTintList = ColorStateList.valueOf(chosenColor)
        else holder.card.backgroundTintList = ColorStateList.valueOf(context.resources.getColor(R.color.settingscardbg))
*/
        holder.card.backgroundTintList = ColorStateList.valueOf(Main.accentColor and 0x00ffffff or 0x33000000)
        holder.text.setOnLongClickListener {
            context.vibrate()
            val dialog = BottomSheetDialog(context, R.style.bottomsheet)
            dialog.setContentView(R.layout.feed_chooser_option_edit_dialog)
            dialog.window!!.findViewById<View>(R.id.design_bottom_sheet).setBackgroundResource(R.drawable.bottom_sheet)
            dialog.findViewById<EditText>(R.id.title)!!.text = Editable.Factory().newEditable(feedUrls[position])
            //dialog.findViewById<TextView>(R.id.title)!!.backgroundTintList = ColorStateList.valueOf(Main.accentColor and 0x00ffffff or 0x33000000)
            dialog.findViewById<TextView>(R.id.done)!!.setTextColor(Main.accentColor)
            dialog.findViewById<TextView>(R.id.done)!!.backgroundTintList = ColorStateList.valueOf(Main.accentColor and 0x00ffffff or 0x33000000)
            dialog.findViewById<TextView>(R.id.done)!!.setOnClickListener {
                dialog.dismiss()
                feedUrls[position] = dialog.findViewById<EditText>(R.id.title)!!.text.toString().replace('|', ' ')
                notifyDataSetChanged()
                Settings.putNotSave("feedUrls", feedUrls.joinToString("|"))
                Settings.apply()
            }
            dialog.findViewById<TextView>(R.id.remove)!!.setOnClickListener {
                dialog.dismiss()
                feedUrls.removeAt(position)
                notifyDataSetChanged()
                Settings.putNotSave("feedUrls", feedUrls.joinToString("|"))
                Settings.apply()
            }
            dialog.show()
            true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder((context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.feed_chooser_option, parent, false))
    }

    override fun getItemCount(): Int {
        return feedUrls.size
    }

    override fun getItemId(i: Int): Long {
        return 0
    }
}
