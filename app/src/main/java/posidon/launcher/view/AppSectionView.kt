package posidon.launcher.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.palette.graphics.Palette
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.items.ItemLongPress
import posidon.launcher.storage.Settings
import posidon.launcher.tools.ColorTools
import posidon.launcher.tools.dp
import posidon.launcher.tools.toBitmap

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

    private val appSize = when (Settings["icsize", 1]) {
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
            findViewById<ImageView>(R.id.iconimg).setImageDrawable(app.icon)
            findViewById<View>(R.id.iconFrame).apply {
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
            findViewById<TextView>(R.id.notificationBadge).apply {
                if (Settings["notif:badges", true] && app.notificationCount != 0) {
                    visibility = View.VISIBLE
                    text = app.notificationCount.toString()
                    Palette.from(app.icon!!.toBitmap()).generate {
                        val color = it?.getDominantColor(0xff111213.toInt()) ?: 0xff111213.toInt()
                        background = ColorTools.notificationBadge(color)
                        setTextColor(if (ColorTools.useDarkText(color)) 0xff111213.toInt() else 0xffffffff.toInt())
                    }
                } else { visibility = View.GONE }
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