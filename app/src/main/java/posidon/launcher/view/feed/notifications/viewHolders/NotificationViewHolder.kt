package posidon.launcher.view.feed.notifications.viewHolders

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import posidon.launcher.view.SwipeableLayout

class NotificationViewHolder(
    val view: ViewGroup,
    val card: SwipeableLayout,
    val linearLayout: LinearLayout
) : RecyclerView.ViewHolder(view)