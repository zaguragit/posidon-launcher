package posidon.launcher.view.drawer

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.DragEvent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import posidon.launcher.Home
import posidon.launcher.R
import posidon.launcher.items.*
import posidon.launcher.search.SearchActivity
import posidon.launcher.storage.Settings
import posidon.launcher.tools.*
import posidon.launcher.tools.theme.ColorTools
import posidon.launcher.tools.theme.Customizer
import posidon.launcher.tools.theme.Icons
import kotlin.math.abs
import kotlin.math.min

class DockView : LinearLayout {

    var onItemClick: (Context, View, dockI: Int, item: LauncherItem) -> Unit = { _, _, _, _ -> }
    var onItemLongClick: (Context, View, dockI: Int, item: LauncherItem) -> Boolean = { _, _, _, _ -> false }

    fun updateTheme(drawer: View) {
        layoutParams = (layoutParams as MarginLayoutParams).apply {
            val m = Settings["dock:margin_x", 16].dp.toInt()
            leftMargin = m
            rightMargin = m
        }
        val bgColor = Settings["dock:background_color", -0x78000000]
        when (val bgType = Settings["dock:background_type", 0]) {
            0 -> {
                background = null
                containerContainer.background = null
                drawer.background = ShapeDrawable().apply {
                    val tr = Settings["dockradius", 30].dp
                    shape = RoundRectShape(floatArrayOf(tr, tr, tr, tr, 0f, 0f, 0f, 0f), null, null)
                    paint.color = bgColor
                }
            }
            1 -> {
                background = null
                containerContainer.background = null
                drawer.background = LayerDrawable(arrayOf(
                        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(bgColor and 0x00ffffff, bgColor)),
                        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(bgColor,
                                Settings["drawer:background_color", -0x78000000]))
                ))
            }
            2 -> {
                val r = Settings["dockradius", 30].dp
                drawer.background = ShapeDrawable().apply {
                    shape = RoundRectShape(floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f), null, null)
                    paint.color = 0
                }
                containerContainer.background = null
                background = ShapeDrawable().apply {
                    shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
                    paint.color = bgColor
                }
            }
            3 -> {
                val r = Settings["dockradius", 30].dp
                drawer.background = ShapeDrawable().apply {
                    shape = RoundRectShape(floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f), null, null)
                    paint.color = 0
                }
                background = null
                containerContainer.background = ShapeDrawable().apply {
                    shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
                    paint.color = bgColor
                }
            }
            else -> {
                Settings["dock:background_type"] = 0
                Log.e("posidon launcher", "dock:background_type can't be $bgType")
            }
        }
        if (!Settings["docksearchbarenabled", false]) {
            searchBar.visibility = GONE
            battery.visibility = GONE
            return
        }
        searchBar.visibility = VISIBLE
        battery.visibility = VISIBLE
        if (Settings["dock:search:below_apps", true]) { searchBar.bringToFront() }
        else { containerContainer.bringToFront() }
        run {
            val color = Settings["docksearchtxtcolor", -0x1000000]
            searchTxt.setTextColor(color)
            searchIcon.imageTintList = ColorStateList.valueOf(color)
            battery.progressTintList = ColorStateList.valueOf(color)
            battery.indeterminateTintMode = PorterDuff.Mode.MULTIPLY
            battery.progressBackgroundTintList = ColorStateList.valueOf(color)
            battery.progressBackgroundTintMode = PorterDuff.Mode.MULTIPLY
            (battery.progressDrawable as LayerDrawable).getDrawable(3).setTint(if (ColorTools.useDarkText(color)) -0x23000000 else -0x11000001)
        }
        searchBar.background = ShapeDrawable().apply {
            val r = Settings["dock:search:radius", 30].dp
            shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
            paint.color = Settings["docksearchcolor", -0x22000001]
        }
    }

    constructor(c: Context) : super(c)
    constructor(c: Context, a: AttributeSet?) : super(c, a)
    constructor(c: Context, a: AttributeSet?, sa: Int) : super(c, a, sa)

    var dockHeight = 0

    val searchIcon = ImageView(context).apply {
        run {
            val p = 8.dp.toInt()
            setPadding(p, p, p, p)
        }
        setImageResource(R.drawable.ic_search)
        imageTintList = ColorStateList.valueOf(0xffffffff.toInt())
    }

    val searchTxt = TextView(context).apply {
        gravity = Gravity.CENTER_VERTICAL
        textSize = 16f
    }

    val battery = ProgressBar(context, null, R.style.Widget_AppCompat_ProgressBar_Horizontal).apply {
        run {
            val p = 10.dp.toInt()
            setPadding(p, p, p, p)
        }
        max = 100
        progressDrawable = context.getDrawable(R.drawable.battery_bar)
    }

    val searchBar = LinearLayout(context).apply {

        gravity = Gravity.CENTER_VERTICAL
        orientation = HORIZONTAL
        setOnClickListener {
            SearchActivity.open(context)
        }

        addView(searchIcon, LayoutParams(56.dp.toInt(), 48.dp.toInt()))
        addView(searchTxt, LayoutParams(0, 48.dp.toInt(), 1f))
        addView(battery, LayoutParams(56.dp.toInt(), 48.dp.toInt()).apply {
            marginEnd = 8.dp.toInt()
        })
    }

    val container = GridLayout(context)

    val containerContainer = FrameLayout(context).apply {
        addView(container, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            this.gravity = Gravity.CENTER_HORIZONTAL
            topMargin = 8.dp.toInt()
            bottomMargin = 8.dp.toInt()
        })
    }

    init {
        gravity = Gravity.CENTER_HORIZONTAL
        orientation = VERTICAL
        addView(searchBar, LayoutParams(MATCH_PARENT, 48.dp.toInt()).apply {
            topMargin = 10.dp.toInt()
            bottomMargin = 10.dp.toInt()
        })
        addView(containerContainer, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
    }

    fun loadAppsAndUpdateHome(drawer: DrawerView, feed: View, desktopContent: View, home: Home) {
        loadApps()
        updateDimensions(drawer, feed, desktopContent, home)
    }

    fun loadApps() {
        val columnCount = Settings["dock:columns", 5]
        val marginX = Settings["dock:margin_x", 16].dp.toInt()
        val appSize = min(when (Settings["dockicsize", 1]) {
            0 -> 64
            2 -> 84
            else -> 74
        }.dp.toInt(), (Device.displayWidth - marginX * 2) / columnCount)
        val rowCount = Settings["dock:rows", 1]
        val showLabels = Settings["dockLabelsEnabled", false]
        val notifBadgesEnabled = Settings["notif:badges", true]
        val notifBadgesShowNum = Settings["notif:badges:show_num", true]
        container.run {
            removeAllViews()
            this.columnCount = columnCount
            this.rowCount = rowCount
        }
        loop@ for (i in 0 until columnCount * rowCount) {
            val view = LayoutInflater.from(context).inflate(R.layout.drawer_item, container, false)
            view.findViewById<View>(R.id.iconFrame).run {
                layoutParams.height = appSize
                layoutParams.width = appSize
            }
            val item = Dock[i]
            if (item != null) {
                val label = view.findViewById<TextView>(R.id.icontxt)
                if (showLabels) {
                    label.text = item.label
                    Customizer.styleLabel("dock:labels", label, -0x11111112)
                } else {
                    label.visibility = GONE
                }
                when (item) {
                    is PinnedShortcut -> if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        if (!item.isInstalled(context.packageManager)) {
                            Dock[i] = null
                            continue@loop
                        }
                    }
                    is App -> {
                        if (!item.isInstalled(context.packageManager)) {
                            Dock[i] = null
                            continue@loop
                        }
                    }
                }
                val img = view.findViewById<ImageView>(R.id.iconimg)
                img.setImageDrawable(item.icon)
                val badge = view.findViewById<TextView>(R.id.notificationBadge)
                if (notifBadgesEnabled) {
                    val c = item.notificationCount
                    if (c != 0) {
                        badge.visibility = View.VISIBLE
                        badge.text = if (notifBadgesShowNum) c.toString() else ""
                        Icons.generateNotificationBadgeBGnFG { bg, fg ->
                            badge.background = bg
                            badge.setTextColor(fg)
                        }
                    } else { badge.visibility = View.GONE }
                } else { badge.visibility = View.GONE }
                view.setOnClickListener {
                    onItemClick(context, view, i, item)
                }
                view.setOnLongClickListener { view ->
                    onItemLongClick(context, view, i, item)
                }
            }
            container.addView(view)
        }
    }

    fun updateDimensions(drawer: DrawerView, feed: View, desktopContent: View, home: Home) {
        val columnCount = Settings["dock:columns", 5]
        val marginX = Settings["dock:margin_x", 16].dp.toInt()
        val appSize = min(when (Settings["dockicsize", 1]) {
            0 -> 64
            2 -> 84
            else -> 74
        }.dp.toInt(), (Device.displayWidth - marginX * 2) / columnCount)
        val rowCount = Settings["dock:rows", 1]
        val containerHeight = (appSize + if (Settings["dockLabelsEnabled", false]) 18.sp.toInt() else 0) * rowCount
        dockHeight = if (Settings["docksearchbarenabled", false] && !context.isTablet) {
            containerHeight + 84.dp.toInt()
        } else {
            containerHeight + 14.dp.toInt()
        }
        val addition = drawer.scrollBar.updateTheme(drawer, feed, this)
        dockHeight += addition
        container.layoutParams.height = containerHeight
        setPadding(0, 0, 0, addition)
        drawer.peekHeight = (dockHeight + Tools.navbarHeight + Settings["dockbottompadding", 10].dp).toInt()
        val metrics = DisplayMetrics().also {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                home.display?.getRealMetrics(it)
            } else {
                home.windowManager.defaultDisplay.getRealMetrics(it)
            }
        }
        drawer.drawerContent.layoutParams.height = metrics.heightPixels
        (home.homeView.layoutParams as FrameLayout.LayoutParams).topMargin = -dockHeight
        if (Settings["feed:show_behind_dock", false]) {
            (feed.layoutParams as MarginLayoutParams).topMargin = dockHeight
            desktopContent.setPadding(0, 12.dp.toInt(), 0, (dockHeight + Tools.navbarHeight + Settings["dockbottompadding", 10].dp).toInt())
        } else {
            (feed.layoutParams as MarginLayoutParams).run {
                topMargin = dockHeight
                bottomMargin = dockHeight + Tools.navbarHeight + (Settings["dockbottompadding", 10] - 18).dp.toInt()
            }
            desktopContent.setPadding(0, 12.dp.toInt(), 0, 24.dp.toInt())
        }
        run {
            val bg = drawer.background
            if (Settings["dock:background_type", 0] == 1 && bg is LayerDrawable) {
                bg.setLayerInset(0, 0, 0, 0, Device.displayHeight - Settings["dockbottompadding", 10].dp.toInt())
                bg.setLayerInset(1, 0, drawer.peekHeight, 0, 0)
            }
        }
        (home.findViewById<View>(R.id.blur).layoutParams as CoordinatorLayout.LayoutParams).topMargin = dockHeight
    }

    inline fun onItemDrop(event: DragEvent): Boolean {
        if (event.y > Device.displayHeight - dockHeight) {
            val item = LauncherItem(event.clipData.description.label.toString())!!
            val location = IntArray(2)
            var i = 0
            while (i < container.childCount) {
                container.getChildAt(i).getLocationOnScreen(location)
                val threshHold = min(container.getChildAt(i).height / 2.toFloat(), 100.dp)
                if (abs(location[0] - (event.x - container.getChildAt(i).height / 2f)) < threshHold && abs(location[1] - (event.y - container.getChildAt(i).height / 2f)) < threshHold) {
                    Dock.add(item, i)
                    loadApps()
                    return true
                }
                i++
            }
        }
        return false
    }

    fun getPositionOnScreen(i: Int, rectF: RectF) {
        val v = container
        val columns = container.columnCount
        val iconWidth = v.measuredWidth.toFloat() / columns
        val location = intArrayOf(0, 0)
        v.getLocationOnScreen(location)
        val left = location[0].toFloat() + iconWidth * (i % columns)
        val bottom = Device.displayHeight - location[0].toFloat() + iconWidth * (i / columns)
        val right = left + iconWidth
        val top = bottom - v.measuredHeight.toFloat()
        rectF.set(left, top, right, bottom)
    }
}