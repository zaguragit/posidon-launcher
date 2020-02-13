package posidon.launcher.feed.notifications

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class SwipeToDeleteCallback : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        swipeListener?.onSwipe(viewHolder, direction)
    }

    interface SwipeListener {
        fun onSwipe(viewHolder: RecyclerView.ViewHolder?, direction: Int)
    }

    companion object {
        var swipeListener: SwipeListener? = null
    }

    private var lastViewHolderToBeDragged: RecyclerView.ViewHolder? = null
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        lastViewHolderToBeDragged?.itemView?.animate()?.alpha(1f)?.duration = 150L
        viewHolder?.itemView?.animate()?.alpha(0.9f)?.duration = 150L
        lastViewHolderToBeDragged = viewHolder
    }
}