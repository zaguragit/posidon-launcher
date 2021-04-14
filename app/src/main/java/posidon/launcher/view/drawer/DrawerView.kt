package posidon.launcher.view.drawer

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.graphics.drawable.*
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import android.widget.GridView.STRETCH_COLUMN_WIDTH
import posidon.android.conveniencelib.Colors
import posidon.android.conveniencelib.Device
import posidon.android.conveniencelib.dp
import posidon.android.conveniencelib.drawable.FastBitmapDrawable
import posidon.launcher.Global
import posidon.launcher.Home
import posidon.launcher.R
import posidon.launcher.external.kustom.Kustom
import posidon.launcher.items.Folder
import posidon.launcher.items.users.AppLoader
import posidon.launcher.items.users.DrawerAdapter
import posidon.launcher.items.users.ItemLongPress
import posidon.launcher.items.users.SectionedDrawerAdapter
import posidon.launcher.search.SearchActivity
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Dock
import posidon.launcher.tools.Tools
import posidon.launcher.tools.getStatusBarHeight
import posidon.launcher.tools.theme.Wallpaper
import posidon.launcher.view.GridView
import kotlin.concurrent.thread
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

class DrawerView : LinearLayout {

    val scrollBar by lazy { AlphabetScrollbarWrapper(drawerGrid, AlphabetScrollbar.VERTICAL) }

    val blurBg = LayerDrawable(arrayOf<Drawable>(
        ColorDrawable(0),
        ColorDrawable(0),
        ColorDrawable(0),
        ColorDrawable(0)
    ))

    fun updateTheme() {
        dock.updateTheme(this)
        if (Global.shouldSetApps) {
            AppLoader(context, ::onAppLoaderEnd).execute()
        } else onAppLoaderEnd()
        if (Settings["drawer:sections_enabled", false]) {
            drawerGrid.numColumns = 1
            drawerGrid.verticalSpacing = 0
        } else {
            drawerGrid.numColumns = Settings["drawer:columns", 4]
            drawerGrid.verticalSpacing = context.dp(Settings["verticalspacing", 12]).toInt()
        }
        val searchBarEnabled = Settings["drawersearchbarenabled", true]
        run {
            val searchBarHeight = if (searchBarEnabled) context.dp(56).toInt() else 0
            val scrollbarWidth = if (
                Settings["drawer:scrollbar:enabled", false] && // isEnabled
                Settings["drawer:scrollbar:position", 1] == 2 // isHorizontal
            ) context.dp(Settings["drawer:scrollbar:width", 24]).toInt() else 0
            searchBarVBox.setPadding(0, 0, 0, Tools.navbarHeight + if (Settings["drawer:scrollbar:show_outside", false]) scrollbarWidth else 0)
            drawerGrid.setPadding(0, context.getStatusBarHeight(), 0, Tools.navbarHeight + searchBarHeight + scrollbarWidth + context.dp(12).toInt())
        }
        if (!searchBarEnabled) {
            searchBar.visibility = GONE
            return
        }
        searchBar.visibility = VISIBLE
        searchTxt.setTextColor(Settings["searchtxtcolor", -0x1])
        searchIcon.imageTintList = ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(Settings["searchhintcolor", -0x1]))
        searchBarVBox.background = ShapeDrawable().apply {
            val tr = context.dp(Settings["searchradius", 0])
            shape = RoundRectShape(floatArrayOf(tr, tr, tr, tr, 0f, 0f, 0f, 0f), null, null)
            paint.color = Settings["searchcolor", 0x33000000]
        }
    }

    inline fun loadAppsIfShould() {
        if (Global.shouldSetApps) {
            loadApps()
        }
    }

    inline fun loadApps() {
        AppLoader(context, ::onAppLoaderEnd).execute()
    }

    fun onAppLoaderEnd() {
        val home = Home.instance
        val s = drawerGrid.scrollY
        if (Settings["drawer:sections_enabled", false]) {
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
                }, isRemoveFnActuallyHide = true)
                true
            }
        }
        drawerGrid.scrollY = s
        scrollBar.updateAdapter()
        dock.loadAppsAndUpdateHome(this, home.feed, home.feed.desktopContent, home)
    }

    fun setKustomVars() {
        if (Settings["kustom:variables:enable", false]) {
            if (behavior.state == BottomDrawerBehavior.STATE_EXPANDED) {
                Kustom["screen"] = "drawer"
            } else {
                Kustom["screen"] = "home"
            }
        }
    }

    fun tryBlur() {
        if (Tools.canBlurDrawer) {
            val shouldHide = behavior.state == BottomDrawerBehavior.STATE_COLLAPSED || behavior.state == BottomDrawerBehavior.STATE_HIDDEN
            thread(isDaemon = true) {
                val blurLayers = Settings["blurLayers", 1]
                val radius = Settings["drawer:blur:rad", 15f]
                for (i in 0 until blurLayers) {
                    val bmp = Wallpaper.blurredWall(radius / blurLayers * (i + 1))
                    val bd = FastBitmapDrawable(bmp)
                    if (shouldHide) bd.alpha = 0
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) blurBg.setDrawable(i, bd)
                    else {
                        blurBg.setId(i, i)
                        blurBg.setDrawableByLayerId(i, bd)
                    }
                }
            }
        }
    }

    inline fun init(home: Home) {
        behavior = LockableBottomDrawerBehavior.from(this).apply {
            isHideable = false
            peekHeight = 0
            setState(BottomDrawerBehavior.STATE_COLLAPSED)
        }

        behavior.addBottomSheetCallback(object : BottomDrawerBehavior.BottomSheetCallback() {

            val things = IntArray(3)
            val radii = FloatArray(8)
            val colors = IntArray(3)
            val floats = FloatArray(1)

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomDrawerBehavior.STATE_COLLAPSED -> {
                        if (home.hasWindowFocus() && Settings["kustom:variables:enable", false]) {
                            Kustom["screen"] = "home"
                        }
                        drawerGrid.smoothScrollToPositionFromTop(0, 0, 0)
                        colors[0] = Settings["dock:background_color", -0x78000000] and 0x00ffffff
                        colors[1] = Settings["dock:background_color", -0x78000000]

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Settings["gesture:back", ""] == "") {
                            home.window.decorView.findViewById<View>(android.R.id.content).systemGestureExclusionRects = listOf(Rect(0, 0, Device.screenWidth(context), Device.screenHeight(context)))
                        }
                        val tr = context.dp(Settings["dockradius", 30])
                        radii[0] = tr
                        radii[1] = tr
                        radii[2] = tr
                        radii[3] = tr
                    }
                    BottomDrawerBehavior.STATE_EXPANDED -> {
                        if (Settings["kustom:variables:enable", false]) {
                            Kustom["screen"] = "drawer"
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            home.window.decorView.findViewById<View>(android.R.id.content).systemGestureExclusionRects = listOf()
                        }
                    }
                }
                ItemLongPress.currentPopup?.dismiss()
                floats[0] = dock.dockHeight.toFloat() / (Device.screenHeight(context) + dock.dockHeight)
                things[0] = if (Tools.canBlurDrawer) Settings["blurLayers", 1] else 0
                things[1] = Settings["dock:background_color", -0x78000000]
                things[2] = Settings["dock:background_type", 0]
                colors[2] = Settings["drawer:background_color", -0x78000000]
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val inverseOffset = 1 - slideOffset
                drawerGrid.alpha = slideOffset
                if (!Settings["drawer:scrollbar:show_outside", false]) scrollBar.alpha = slideOffset
                scrollBar.floatingFactor = inverseOffset
                home.feed.alpha = inverseOffset.pow(1.2f)
                if (slideOffset >= 0) {
                    try {
                        val bg = background
                        when (things[2]) {
                            0 -> {
                                bg as ShapeDrawable
                                bg.paint.color = Colors.blend(colors[2], things[1], slideOffset)
                                bg.shape = RoundRectShape(radii, null, null)
                            }
                            1 -> {
                                bg as LayerDrawable
                                colors[1] = Colors.blend(colors[2], things[1], slideOffset)
                                (bg.getDrawable(0) as GradientDrawable).colors = intArrayOf(colors[0], colors[1])
                                (bg.getDrawable(1) as GradientDrawable).colors = intArrayOf(colors[1], colors[2])
                            }
                            2, 3 -> {
                                bg as ShapeDrawable
                                bg.paint.color = colors[2] and 0xffffff or (((colors[2] ushr 24).toFloat() * slideOffset).toInt() shl 24)
                                bg.shape = RoundRectShape(radii, null, null)
                            }
                        }
                        val blurLayers = things[0]
                        if (blurLayers != 0) {
                            val repetitive = (slideOffset * 255).roundToInt() * blurLayers
                            for (i in 0 until blurLayers) {
                                blurBg.getDrawable(i).alpha = max(min(repetitive - (i shl 8) + i, 255), 0)
                            }
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                } else {
                    val scrollbarPosition = Settings["drawer:scrollbar:position", 1]
                    if (scrollbarPosition == 2) scrollBar.translationY = scrollBar.height.toFloat() * -slideOffset
                    if (!Settings["feed:show_behind_dock", false]) {
                        (home.feed.layoutParams as MarginLayoutParams).bottomMargin = ((1 + slideOffset) * (dock.dockHeight + Tools.navbarHeight + context.dp((Settings["dockbottompadding", 10] - 18)))).toInt()
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
        selector = ColorDrawable(0)
        isVerticalScrollBarEnabled = false
        isVerticalFadingEdgeEnabled = true
        setFadingEdgeLength(context.dp(72).toInt())
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
            val p = context.dp(12).toInt()
            setPadding(p, p, p, p)
        }
        setImageResource(R.drawable.ic_search)
        imageTintList = ColorStateList.valueOf(0xffffffff.toInt())
    }

    val searchTxt = TextView(context).apply {
        run {
            val p = context.dp(12).toInt()
            setPadding(p, p, p, p)
        }
        gravity = Gravity.CENTER_VERTICAL
        textSize = 16f
    }

    val searchBar = LinearLayout(context).apply {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setOnClickListener {
            SearchActivity.open(context)
        }
        val height = context.dp(56).toInt()
        addView(searchIcon, LayoutParams(height, height).apply {
            marginStart = context.dp(8).toInt()
        })
        addView(searchTxt, LayoutParams(MATCH_PARENT, height).apply {
            marginStart = -context.dp(16).toInt()
        })
    }

    val searchBarVBox = LinearLayout(context).apply {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        addView(searchBar, ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
    }

    val drawerContent = FrameLayout(context).apply {
        addView(drawerGrid, LayoutParams(MATCH_PARENT, MATCH_PARENT))
        addView(searchBarVBox, FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            this.gravity = Gravity.BOTTOM
        })
    }

    lateinit var behavior: LockableBottomDrawerBehavior<DrawerView>

    inline var locked: Boolean
        get() = behavior.locked
        set(value) { behavior.locked = value }

    inline var state: Int
        get() = behavior.getState()
        set(value) = behavior.setState(value)

    inline var peekHeight: Int
        get() = behavior.peekHeight
        set(value) = behavior.setPeekHeight(value)

    init {
        orientation = VERTICAL
        addView(dock, LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM
        })
        addView(drawerContent, LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }
}