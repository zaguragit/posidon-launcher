package posidon.launcher.view.recycler

import android.content.Context

/**
 * This is to stop the "IndexOutOfBoundsException: Inconsistency detected" error from happening
 */
class GridLayoutManager(context: Context, columns: Int) : androidx.recyclerview.widget.GridLayoutManager(context, columns) {

    override fun supportsPredictiveItemAnimations() = false
}