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
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.vibrate

class FeedChooserAdapter(
    internal val context: Context,
    private val feedUrls: ArrayList<String>
) : RecyclerView.Adapter<FeedChooserAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var card: LinearLayout = itemView.findViewById(R.id.card)
        var text: TextView = itemView.findViewById(R.id.txt)
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        val url = feedUrls[i]
        holder.text.text = url

        holder.card.backgroundTintList = ColorStateList.valueOf(Global.accentColor and 0x00ffffff or 0x33000000)
        holder.text.setOnLongClickListener {
            context.vibrate()
            val dialog = BottomSheetDialog(context, R.style.bottomsheet)
            dialog.setContentView(R.layout.feed_chooser_option_edit_dialog)
            dialog.window!!.findViewById<View>(R.id.design_bottom_sheet).setBackgroundResource(R.drawable.bottom_sheet)
            val title = dialog.findViewById<EditText>(R.id.title)!!
            val done = dialog.findViewById<TextView>(R.id.done)!!
            title.text = Editable.Factory().newEditable(url)
            done.setTextColor(Global.accentColor)
            done.backgroundTintList = ColorStateList.valueOf(Global.accentColor and 0x00ffffff or 0x33000000)
            done.setOnClickListener {
                dialog.dismiss()
                feedUrls[i] = title.text.toString().replace('|', ' ')
                notifyDataSetChanged()
                Settings.putNotSave("feedUrls", feedUrls.joinToString("|"))
                Settings.apply()
            }
            dialog.findViewById<TextView>(R.id.remove)!!.setOnClickListener {
                dialog.dismiss()
                feedUrls.remove(url)
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

    override fun getItemCount() = feedUrls.size

    override fun getItemId(i: Int) = 0L
}
