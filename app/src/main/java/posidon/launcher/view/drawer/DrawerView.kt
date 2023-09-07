package posidon.launcher.view.drawer

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import android.widget.GridView.STRETCH_COLUMN_WIDTH
import androidx.core.graphics.ColorUtils
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.posidon.android.conveniencelib.Device
import io.posidon.android.conveniencelib.drawable.FastBitmapDrawable
import io.posidon.android.conveniencelib.getStatusBarHeight
import io.posidon.android.conveniencelib.units.dp
import io.posidon.android.conveniencelib.units.toFloatPixels
import io.posidon.android.conveniencelib.units.toPixels
import posidon.launcher.Global
import posidon.launcher.Home
import posidon.launcher.R
import posidon.launcher.drawable.NonDrawable
import posidon.launcher.items.App
import posidon.launcher.items.Folder
import posidon.launcher.items.users.DrawerAdapter
import posidon.launcher.items.users.ItemLongPress
import posidon.launcher.items.users.SectionedDrawerAdapter
import posidon.launcher.search.SearchActivity
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Dock
import posidon.launcher.external.kustom.Kustom
import posidon.launcher.items.users.AppLoader
import posidon.launcher.items.users.IconConfig
import posidon.launcher.tools.Tools
import posidon.launcher.tools.theme.Wallpaper
import posidon.launcher.view.GridView
import posidon.launcher.view.feed.Feed
import kotlin.concurrent.thread
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

class DrawerView : FrameLayout {

    val scrollBar by lazy { AlphabetScrollbarWrapper(drawerGrid, AlphabetScrollbar.VERTICAL) }

    fun updateTheme(feed: Feed) {
        dock.updateTheme(this)
        dock.updateDimensions(this, feed, feed.desktopContent)
        if (Global.shouldSetApps) {
            loadApps()
        } else onAppLoaderEnd()
        if (Settings["drawer:sections:enabled", false]) {
            drawerGrid.numColumns = 1
            drawerGrid.verticalSpacing = 0
        } else {
            drawerGrid.numColumns = Settings["drawer:columns", 5]
            drawerGrid.verticalSpacing = Settings["verticalspacing", 12].dp.toPixels(context)
        }
        val searchBarEnabled = Settings["drawersearchbarenabled", true]
        run {
            val searchBarHeight = if (searchBarEnabled) 56.dp.toPixels(context) else 0
            val scrollbarWidth = if (
                Settings["drawer:scrollbar:enabled", false] && // isEnabled
                Settings["drawer:scrollbar:position", 1] == 2 // isHorizontal
            ) Settings["drawer:scrollbar:width", 24].dp.toPixels(context) else 0
            searchBarVBox.setPadding(0, 0, 0, Tools.navbarHeight + if (Settings["drawer:scrollbar:show_outside", false]) scrollbarWidth else 0)
            drawerGrid.setPadding(0, context.getStatusBarHeight(), 0, Tools.navbarHeight + searchBarHeight + scrollbarWidth + 12.dp.toPixels(context))
        }
        if (!searchBarEnabled) {
            searchBar.isVisible = false
            return
        }
        searchBar.isVisible = true
        searchTxt.setTextColor(Settings["searchtxtcolor", -0x1])
        searchIcon.imageTintList = ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(Settings["searchhintcolor", -0x1]))
        searchBarVBox.background = ShapeDrawable().apply {
            val tr = Settings["searchradius", 0].dp.toFloatPixels(context)
            shape = RoundRectShape(floatArrayOf(tr, tr, tr, tr, 0f, 0f, 0f, 0f), null, null)
            paint.color = Settings["searchcolor", 0x33000000]
        }
    }

    inline fun loadAppsIfShould() {
        if (Global.shouldSetApps) {
            loadApps()
        }
    }

    fun loadApps() {
        AppLoader(context.applicationContext) {
            Home.instance.runOnUiThread {
                onAppLoaderEnd()
            }
        }.execute()
    }

    fun onAppLoaderEnd() {
        Dock.clearCache()
        val home = Home.instance
        val s = drawerGrid.scrollY
        if (Settings["drawer:sections:enabled", false]) {
            drawerGrid.adapter = SectionedDrawerAdapter(this)
            drawerGrid.onItemClickListener = null
            drawerGrid.onItemLongClickListener = null
        } else {
            drawerGrid.adapter = DrawerAdapter()
            drawerGrid.onItemClickListener = AdapterView.OnItemClickListener { _, v, i, _ -> Global.apps[i].open(context, v) }
            drawerGrid.setOnItemLongClickListener { _, view, position, _ ->
                val app = Global.apps[position]
                ItemLongPress.onItemLongPress(context, view, app, null, {
                    app.setHidden()
                    loadApps()
                }, removeFunction = ItemLongPress.HIDE)
                true
            }
        }
        drawerGrid.scrollY = s
        scrollBar.updateAdapter()
        dock.loadAppsAndUpdateHome(this, home.feed, home.feed.desktopContent)
        home.feed.onAppsLoaded()
    }

    fun setKustomVars() {
        if (Settings["kustom:variables:enable", false]) {
            if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                Kustom[context, "posidon", "screen"] = "drawer"
            } else {
                Kustom[context, "posidon", "screen"] = "home"
            }
        }
    }

    inline fun init(home: Home) {
        behavior = BottomSheetBehavior.from(this).apply {
            isHideable = false
            peekHeight = 0
            setState(BottomSheetBehavior.STATE_COLLAPSED)
        }

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            val things = IntArray(4)
            val radii = FloatArray(8)
            val colors = IntArray(1)
            val floats = FloatArray(1)

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        if (home.hasWindowFocus() && Settings["kustom:variables:enable", false]) {
                            Kustom[context, "posidon", "screen"] = "home"
                        }
                        drawerGrid.smoothScrollToPositionFromTop(0, 0, 0)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Settings["gesture:back", ""] == "") {
                            home.window.decorView.findViewById<View>(android.R.id.content).systemGestureExclusionRects = listOf(Rect(0, 0, Device.screenWidth(context), Device.screenHeight(context)))
                        }
                        floats[0] = Settings["dock:radius", 0].dp.toFloatPixels(context)
                        drawerContent.isInvisible = true
                        dock.isInvisible = false
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        if (Settings["kustom:variables:enable", false]) {
                            Kustom[context, "posidon", "screen"] = "drawer"
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            home.window.decorView.findViewById<View>(android.R.id.content).systemGestureExclusionRects = listOf()
                        }
                        drawerContent.isInvisible = false
                        dock.isInvisible = true
                    }
                    else -> {
                        drawerContent.isInvisible = false
                        dock.isInvisible = false
                    }
                }
                ItemLongPress.currentPopup?.dismiss()
                things[1] = Settings["dock:background_color", 0xff242424.toInt()]
                things[2] = Settings["dock:background_type", 0]
                things[3] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) min(
                    rootWindowInsets.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)?.radius ?: 0,
                    rootWindowInsets.getRoundedCorner(RoundedCorner.POSITION_TOP_RIGHT)?.radius ?: 0,
                ) else 0
                colors[0] = Settings["drawer:background_color", 0xa4171717.toInt()]
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val inverseOffset = 1 - slideOffset
                if (!Settings["drawer:scrollbar:show_outside", false]) scrollBar.alpha = slideOffset
                scrollBar.floatingFactor = inverseOffset
                home.feed.alpha = inverseOffset.pow(1.2f)
                if (slideOffset >= 0) {
                    drawerGrid.alpha = slideOffset.pow(0.3f)
                    try {
                        val bg = background
                        when (things[2]) {
                            0 -> {
                                val r = things[3].toFloat() * slideOffset + floats[0] * inverseOffset
                                radii.fill(r, 0, 4)
                                bg as ShapeDrawable
                                bg.paint.color = ColorUtils.blendARGB(colors[1], 0, slideOffset)
                                bg.shape = RoundRectShape(radii, null, null)
                            }
                            1 -> {
                                val r = things[3].toFloat() * slideOffset
                                radii.fill(r, 0, 4)
                                bg as LayerDrawable
                                val midColor =
                                    ColorUtils.blendARGB(colors[1], 0, slideOffset)
                                (bg.getDrawable(0) as GradientDrawable).also {
                                    val topColor =
                                        midColor and 0x00ffffff or ((0xff * slideOffset).toInt() shl 24)
                                    it.cornerRadii = radii
                                    it.colors = intArrayOf(topColor, midColor)
                                }
                                (bg.getDrawable(1) as GradientDrawable).colors =
                                    intArrayOf(midColor, colors[0])
                            }
                            2 -> {
                                val r = things[3].toFloat() * slideOffset + floats[0] * inverseOffset
                                radii.fill(r, 0, 4)
                                bg as ShapeDrawable
                                bg.paint.color = colors[0] and 0xffffff or (((colors[0] ushr 24).toFloat() * slideOffset).toInt() shl 24)
                                bg.shape = RoundRectShape(radii, null, null)
                            }
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                } else {
                    drawerGrid.alpha = 0f
                    val scrollbarPosition = Settings["drawer:scrollbar:position", 1]
                    if (scrollbarPosition == 2) scrollBar.translationY = scrollBar.height.toFloat() * -slideOffset
                    if (!Settings["feed:show_behind_dock", false]) {
                        (home.feed.layoutParams as MarginLayoutParams).bottomMargin = ((1 + slideOffset) * (dock.dockHeight + Tools.navbarHeight + (Settings["dockbottompadding", 10] - 18).dp.toPixels(context))).toInt()
                        home.feed.requestLayout()
                    }
                }
                dock.alpha = inverseOffset
            }
        })
    }

    constructor(c: Context) : super(c)
    constructor(c: Context, a: AttributeSet?) : super(c, a)
    constructor(c: Context, a: AttributeSet?, sa: Int) : super(c, a, sa)

    val dock = DockView(context).apply {
        onItemClick = { context, view, i, item ->
            item.open(context, view, i)
        }
        onItemLongClick = { context, view, i, item ->
            ItemLongPress.onItemLongPress(context, view, item, onRemove = {
                Dock[i] = null
                loadApps()
            }, onEdit = if (item is Folder) {
                { item.edit(it, i) }
            } else null, dockI = i)
            true
        }
    }

    val drawerGrid = GridView(context).apply {
        gravity = Gravity.CENTER_HORIZONTAL
        stretchMode = STRETCH_COLUMN_WIDTH
        selector = NonDrawable()
        isVerticalScrollBarEnabled = false
        isVerticalFadingEdgeEnabled = true
        setFadingEdgeLength(72.dp.toPixels(context))
        clipToPadding = false
        alpha = 0f
        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && canScrollVertically(-1))
                requestDisallowInterceptTouchEvent(true)
            false
        }
    }

    val searchIcon = ImageView(context).apply {
        run {
            val p = 12.dp.toPixels(context)
            setPadding(p, p, p, p)
        }
        setImageResource(R.drawable.ic_search)
        imageTintList = ColorStateList.valueOf(0xffffffff.toInt())
    }

    val searchTxt = TextView(context).apply {
        run {
            val p = 12.dp.toPixels(context)
            setPadding(p, p, p, p)
        }
        gravity = Gravity.CENTER_VERTICAL
        textSize = 16f
    }

    val searchBar = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setOnClickListener {
            SearchActivity.open(context)
        }
        val height = 56.dp.toPixels(context)
        addView(searchIcon, LayoutParams(height, height).apply {
            marginStart = 8.dp.toPixels(context)
        })
        addView(searchTxt, LayoutParams(MATCH_PARENT, height).apply {
            marginStart = -16.dp.toPixels(context)
        })
    }

    val searchBarVBox = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        addView(searchBar, ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
    }

    val drawerContent = FrameLayout(context).apply {
        addView(drawerGrid, LayoutParams(MATCH_PARENT, MATCH_PARENT))
        addView(searchBarVBox, LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            this.gravity = Gravity.BOTTOM
        })
    }

    lateinit var behavior: BottomSheetBehavior<DrawerView>

    inline var locked: Boolean
        get() = !behavior.isDraggable
        set(value) { behavior.isDraggable = !value }

    inline var state: Int
        get() = behavior.state
        set(value) = behavior.setState(value)

    inline var isHideable: Boolean
        get() = behavior.isHideable
        set(value) { behavior.isHideable = value }

    inline var peekHeight: Int
        get() = behavior.peekHeight
        set(value) = behavior.setPeekHeight(value)

    init {
        addView(drawerContent, LayoutParams(MATCH_PARENT, MATCH_PARENT))
        addView(dock, LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            gravity = Gravity.TOP
        })
    }
}