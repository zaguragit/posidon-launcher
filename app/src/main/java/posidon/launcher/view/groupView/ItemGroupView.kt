package posidon.launcher.view.groupView

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewParent
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import posidon.android.conveniencelib.dp
import posidon.launcher.items.LauncherItem

abstract class ItemGroupView(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    protected val items = ArrayList<LauncherItem>()

    val gridLayout = GridLayout(context)

    val textView = TextView(context).apply {
        textSize = 18f
        setPaddingRelative(dp(28).toInt(), 0, 0, 0)
    }

    fun setItems(list: Iterable<LauncherItem>, parent: ViewParent) {
        clear()
        list.forEach { add(it, parent) }
    }

    fun add(item: LauncherItem, parent: ViewParent) {
        items.add(item)
        gridLayout.addView(getItemView(item, parent).apply {
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

    abstract fun getItemView (item: LauncherItem, parent: ViewParent): View

    fun clear() {
        items.clear()
        gridLayout.removeAllViews()
    }

    init {
        orientation = VERTICAL
        addView(textView)
        addView(gridLayout, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
    }

    inline var title: CharSequence
        get() = textView.text
        set(value) { textView.text = value }
}