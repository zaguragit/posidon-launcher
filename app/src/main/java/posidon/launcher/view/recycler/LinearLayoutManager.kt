package posidon.launcher.view.recycler

import android.content.Context
import androidx.recyclerview.widget.RecyclerView

/**
 * This is to stop the "IndexOutOfBoundsException: Inconsistency detected" error from happening
 */
class LinearLayoutManager : androidx.recyclerview.widget.LinearLayoutManager {

    constructor(context: Context) : super(context)
    constructor(context: Context, orientation: Int, reverseLayout: Boolean) : super(context, orientation, reverseLayout)

    override fun supportsPredictiveItemAnimations() = false

    companion object {
        const val HORIZONTAL = RecyclerView.HORIZONTAL
        const val VERTICAL = RecyclerView.VERTICAL
    }
}