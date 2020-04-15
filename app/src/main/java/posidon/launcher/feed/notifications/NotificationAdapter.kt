package posidon.launcher.feed.notifications

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import posidon.launcher.LauncherMenu
import posidon.launcher.R
import posidon.launcher.storage.Settings

class NotificationAdapter(private val context: Context, private val window: Window) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(internal val view: ViewGroup, internal val card: CardView, internal val linearLayout: LinearLayout) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): NotificationViewHolder {
        val view = RelativeLayout(context)
        val hMargin = (Settings["feed:card_margin_x", 16] * context.resources.displayMetrics.density).toInt()
        val vMargin = (9 * context.resources.displayMetrics.density).toInt()
        view.setPadding(hMargin, vMargin, hMargin, vMargin)

        val card = CardView(context)
        card.preventCornerOverlap = true
        card.elevation = 0f
        card.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        card.radius = context.resources.displayMetrics.density * Settings["feed:card_radius", 15]
        card.setCardBackgroundColor(Settings["notificationbgcolor", -0x1])
        view.addView(card)

        val ll = LinearLayout(context)
        ll.orientation = LinearLayout.VERTICAL
        ll.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val padding = (8 * context.resources.displayMetrics.density).toInt()
        ll.setPadding(padding, padding, padding, padding)
        card.addView(ll)
        return NotificationViewHolder(view, card, ll)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val groups = NotificationService.groups()
        if (groups.size != 0) for (notification in groups[position]) {
            val view: View = when {
                notification.isSummary -> LayoutInflater.from(context).inflate(R.layout.notification_normal_summary, null)
                else -> {
                    val v1: View
                    if (notification.bigPic == null) {
                        v1 = LayoutInflater.from(context).inflate(R.layout.notification_normal, null)
                    } else {
                        v1 = LayoutInflater.from(context).inflate(R.layout.notification_big_pic, null)
                        v1.findViewById<ImageView>(R.id.bigPic).setImageDrawable(notification.bigPic)
                    }
                    if (notification.actions != null && Settings["notificationActionsEnabled", false]) {
                        v1.findViewById<LinearLayout>(R.id.action_list).visibility = View.VISIBLE
                        for (action in notification.actions) {
                            val a = TextView(context)
                            a.text = action.title
                            a.textSize = 14f
                            a.setTextColor(Settings["notificationActionTextColor", -0xdad9d9])
                            val r = 24 * context.resources.displayMetrics.density
                            val out = ShapeDrawable(RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null))
                            out.paint.color = Settings["notificationActionBGColor", 0x88e0e0e0.toInt()]
                            a.background = out
                            val vPadding = (10 * context.resources.displayMetrics.density).toInt()
                            val hPadding = (15 * context.resources.displayMetrics.density).toInt()
                            a.setPadding(hPadding, vPadding, hPadding, vPadding)
                            v1.findViewById<LinearLayout>(R.id.action_list).addView(a)
                            (a.layoutParams as LinearLayout.LayoutParams).leftMargin = (6 * context.resources.displayMetrics.density).toInt()
                            (a.layoutParams as LinearLayout.LayoutParams).rightMargin = (6 * context.resources.displayMetrics.density).toInt()
                            a.setOnClickListener {
                                try { action.actionIntent.send() }
                                catch (e: Exception) { e.printStackTrace() }
                            }
                        }
                    }
                    v1
                }
            }

            view.findViewById<TextView>(R.id.title).text = notification.title
            view.findViewById<TextView>(R.id.txt).text = notification.text

            view.findViewById<ImageView>(R.id.iconimg).setImageDrawable(notification.icon)
            view.findViewById<TextView>(R.id.title).setTextColor(Settings["notificationtitlecolor", -0xeeeded])
            view.findViewById<TextView>(R.id.txt).setTextColor(Settings["notificationtxtcolor", -0xdad9d9])

            view.setOnClickListener { notification.open() }
            view.setOnLongClickListener(LauncherMenu(context, window))
            holder.linearLayout.addView(view)
        }
    }

    override fun getItemCount(): Int { return NotificationService.groups().size }
}
