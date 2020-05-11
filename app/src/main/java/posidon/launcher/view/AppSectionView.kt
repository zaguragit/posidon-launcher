package posidon.launcher.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.items.ItemLongPress
import posidon.launcher.storage.Settings
import posidon.launcher.tools.dp

class AppSectionView(context: Context) : LinearLayout(context) {

    private val apps = ArrayList<App>()

    val gridLayout = GridLayout(context).apply {
        columnCount = Settings["drawer:columns", 4]
        if (Settings["drawer:columns", 4] > 2) {
            setPaddingRelative(12.dp.toInt(), 0, 0, 0)
        }
    }

    val textView = TextView(context).apply {
        textSize = 18f
        setTextColor(Settings["labelColor", -0x11111112])
    }



    fun setApps(list: Collection<App>) {
        clear()
        list.forEach { add(it) }
    }

    val appSize = when (Settings["icsize", 1]) {
        0 -> 64.dp.toInt()
        2 -> 84.dp.toInt()
        else -> 74.dp.toInt()
    }

    fun add(app: App) {
        apps.add(app)

        gridLayout.addView((
            if (Settings["drawer:columns", 4] > 2) LayoutInflater.from(context).inflate(R.layout.drawer_item, gridLayout, false)
            else LayoutInflater.from(context).inflate(R.layout.list_item, gridLayout, false).apply {
                if (Settings["drawer:columns", 4] == 2) findViewById<TextView>(R.id.icontxt).textSize = 18f
            }
        ).apply {
            findViewById<ImageView>(R.id.iconimg).apply {
                setImageDrawable(app.icon)
                layoutParams.height = appSize
                layoutParams.width = appSize
            }
            findViewById<TextView>(R.id.icontxt).apply {
                if (Settings["labelsenabled", false]) {
                    text = app.label
                    visibility = View.VISIBLE
                    setTextColor(Settings["labelColor", -0x11111112])
                } else visibility = View.INVISIBLE
            }
            setOnTouchListener { v, event ->
                val parentContainer = this@AppSectionView.parent as View
                if (parentContainer.canScrollVertically(-1))
                    parentContainer.parent.requestDisallowInterceptTouchEvent(true)
                gridLayout.onTouchEvent(event)
                false
            }
            setOnClickListener { app.open(context, it) }
            setOnLongClickListener(ItemLongPress.drawer(context, app))
            (layoutParams as GridLayout.LayoutParams).bottomMargin = Settings["verticalspacing", 12].dp.toInt()
        })
    }

    fun clear() {
        apps.clear()
        gridLayout.removeAllViews()
    }

    init {
        when (Settings["drawer:sec_name_pos", 0]) {
            0 -> {
                orientation = VERTICAL
                addView(textView.apply {
                    setPaddingRelative(28.dp.toInt(), 0, 0, 0)
                })
            }
            1 -> {
                orientation = HORIZONTAL
                addView(textView.apply {
                    setPaddingRelative(12.dp.toInt(), 0, 0, 0)
                })
            }
        }
        addView(gridLayout)
    }

    inline var title: CharSequence
        get() = textView.text
        set(value) { textView.text = value }
}