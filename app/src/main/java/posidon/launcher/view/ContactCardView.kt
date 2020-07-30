package posidon.launcher.view

import android.content.Context
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

class ContactCardView(context: Context, attrs: AttributeSet? = null) : ItemGroupView(context, attrs) {

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
}