package posidon.launcher.view.feed

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import posidon.launcher.R
import posidon.launcher.items.ContactItem
import posidon.launcher.items.LauncherItem
import posidon.launcher.storage.Settings
import posidon.launcher.tools.dp
import posidon.launcher.view.groupView.ItemGroupView

class ContactCardView(context: Context, attrs: AttributeSet? = null) : ItemGroupView(context, attrs), FeedSection {

    var columns
        get() = gridLayout.columnCount
        set(value) {
            gridLayout.columnCount = value
        }

    init {
        columns = 5
        title = context.getString(R.string.starred_contacts)
        textView.run {
            val p = 10.dp.toInt()
            setPaddingRelative(p, 16.dp.toInt(), 0, p)
        }
        setPadding(8.dp.toInt(), 0, 8.dp.toInt(), 0)
    }

    override fun getItemView(item: LauncherItem): View {
        item as ContactItem
        return (LayoutInflater.from(context).inflate(R.layout.drawer_item, gridLayout, false)).apply {
            layoutParams.width = (gridLayout.measuredWidth) / columns
            findViewById<ImageView>(R.id.iconimg).setImageDrawable(item.icon)
            findViewById<View>(R.id.iconFrame).run {
                layoutParams.height = 74.dp.toInt()
                layoutParams.width = 74.dp.toInt()
            }
            findViewById<TextView>(R.id.icontxt).text = item.label
            findViewById<TextView>(R.id.notificationBadge).visibility = View.GONE
            setOnClickListener { item.open() }
            (layoutParams as GridLayout.LayoutParams).bottomMargin = Settings["verticalspacing", 12].dp.toInt()
        }
    }

    override fun updateTheme(activity: Activity) {
        val marginX = Settings["feed:card_margin_x", 16].dp.toInt()
        val marginY = Settings["feed:card_margin_y", 9].dp.toInt()
        background = ShapeDrawable().apply {
            val r = Settings["feed:card_radius", 15].dp
            shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
            paint.color = Settings["notificationbgcolor", -0x1]
        }
        columns = Settings["contacts_card:columns", 5]
        (layoutParams as MarginLayoutParams).run {
            leftMargin = marginX
            rightMargin = marginX
            topMargin = marginY
            bottomMargin = marginY
        }
    }

    fun update(activity: Activity) {
        ContactItem.getList(true).also {
            activity.runOnUiThread {
                setItems(it)
            }
        }
    }
}