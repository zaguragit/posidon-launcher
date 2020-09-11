package posidon.launcher.feed.order

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import posidon.launcher.Home
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.storage.Settings
import posidon.launcher.tools.dp
import posidon.launcher.view.SwipeableLayout

class OrderAdapter(
    val context: Context,
    val sections: ArrayList<String>
) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    class ViewHolder(
        val swipeView: SwipeableLayout,
        val icon: ImageView,
        val text: TextView
    ) : RecyclerView.ViewHolder(swipeView)

    override fun getItemCount() = sections.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val icon = ImageView(context).apply {
            run {
                val p = 30.dp.toInt()
                setPaddingRelative(p, p, 10.dp.toInt(), p)
            }
        }
        val text = TextView(context).apply {
            setTextColor(0xffffffff.toInt())
            textSize = 15f
        }
        val linearLayout = LinearLayout(context).apply {
            gravity = Gravity.CENTER_VERTICAL

            addView(icon, let {
                val s = 108.dp.toInt()
                ViewGroup.LayoutParams(s, s)
            })
            addView(text)
            setOnClickListener {}
        }
        val swipeView = SwipeableLayout(linearLayout).apply {
            layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT)
            setCardBackgroundColor(context.resources.getColor(R.color.cardbg))
            setIconColor(Home.accentColor)
            setSwipeColor(Home.accentColor and 0x11ffffff)
            useCompatPadding = true
            cardElevation = 10f
            radius = 12.dp
        }
        return ViewHolder(swipeView, icon, text)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        val section = sections[i]
        holder.swipeView.reset()
        holder.swipeView.onSwipeAway = {
            sections.remove(section)
            notifyItemRemoved(i)
            notifyItemRangeChanged(i, sections.size - i)
        }
        when (section) {
            "starred_contacts" -> {
                holder.text.text = context.getString(R.string.starred_contacts)
                holder.icon.setImageResource(R.drawable.ic_apps)
            }
            "notifications" -> {
                holder.text.text = context.getString(R.string.notifications)
                holder.icon.setImageResource(R.drawable.custom_notifications)
            }
            "news" -> {
                holder.text.text = context.getString(R.string.settings_title_news)
                holder.icon.setImageResource(R.drawable.custom_news)
            }
            else -> {
                val prefix = section.substringBefore(':')
                val value = section.substringAfter(':')
                when (prefix) {
                    "widget" -> {
                        val component = Settings["widget:$value", "posidon.launcher/posidon.launcher.external.widgets.ClockWidget"]
                        val packageName = component.substringBefore('/')
                        holder.text.text = "Widget" + " | " + (App.getJustPackage(packageName)?.get(0)?.label ?: context.packageManager.getApplicationInfo(packageName, 0).loadLabel(context.packageManager))
                        holder.icon.setImageDrawable(App.getJustPackage(packageName)?.get(0)?.icon ?: context.packageManager.getApplicationIcon(packageName))
                    }
                    else -> {
                        sections.remove(section)
                        notifyDataSetChanged()
                    }
                }
            }
        }
    }
}