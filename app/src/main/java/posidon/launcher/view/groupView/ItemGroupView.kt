package posidon.launcher.view.groupView

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import posidon.launcher.items.LauncherItem
import posidon.launcher.tools.dp

abstract class ItemGroupView(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    protected val items = ArrayList<LauncherItem>()

    val gridLayout = GridLayout(context)

    val textView = TextView(context).apply {
        textSize = 18f
        setPaddingRelative(28.dp.toInt(), 0, 0, 0)
    }

    fun setItems(list: Iterable<LauncherItem>) {
        clear()
        list.forEach { add(it) }
    }

    fun add(item: LauncherItem) {
        items.add(item)
        gridLayout.addView(getItemView(item).apply {
            setOnTouchListener { _, event ->
                val parent = this@ItemGroupView.parent
                if (parent != null) {
                    val parentContainer = parent as View
                    if (parentContainer.canScrollVertically(-1)) {
                        parentContainer.parent.requestDisallowInterceptTouchEvent(true)
                    }
                }
                gridLayout.onTouchEvent(event)
                false
            }
        })
    }

    abstract fun getItemView (item: LauncherItem): View

    fun clear() {
        items.clear()
        gridLayout.removeAllViews()
    }

    init {
        orientation = VERTICAL
        addView(textView)
        addView(gridLayout)
    }

    inline var title: CharSequence
        get() = textView.text
        set(value) { textView.text = value }
}