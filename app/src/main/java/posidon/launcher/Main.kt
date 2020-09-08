package posidon.launcher

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.*
import android.graphics.drawable.shapes.RoundRectShape
import android.media.AudioManager
import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.View.*
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.dock.*
import posidon.launcher.LauncherMenu.PinchListener
import posidon.launcher.external.Kustom
import posidon.launcher.feed.news.FeedLoader
import posidon.launcher.feed.notifications.NotificationService
import posidon.launcher.items.*
import posidon.launcher.items.users.AppLoader
import posidon.launcher.items.users.DrawerAdapter
import posidon.launcher.items.users.ItemLongPress
import posidon.launcher.items.users.SectionedDrawerAdapter
import posidon.launcher.search.ConsoleActivity
import posidon.launcher.search.SearchActivity
import posidon.launcher.storage.Settings
import posidon.launcher.tools.*
import posidon.launcher.tools.Tools.tryAnimate
import posidon.launcher.tools.Tools.updateNavbarHeight
import posidon.launcher.tools.drawable.FastBitmapDrawable
import posidon.launcher.tutorial.WelcomeActivity
import posidon.launcher.view.AlphabetScrollbar
import posidon.launcher.view.GridView
import posidon.launcher.view.NestedScrollView
import posidon.launcher.view.drawer.BottomDrawerBehavior
import posidon.launcher.view.drawer.BottomDrawerBehavior.BottomSheetCallback
import posidon.launcher.view.drawer.BottomDrawerBehavior.STATE_EXPANDED
import posidon.launcher.view.drawer.LockableBottomDrawerBehavior
import posidon.launcher.view.feed.*
import java.lang.ref.WeakReference
import kotlin.concurrent.thread
import kotlin.math.*
import kotlin.system.exitProcess

class Main : FragmentActivity() {

    private lateinit var drawerGrid: GridView
    private lateinit var drawer: View
    private lateinit var drawerScrollBar: AlphabetScrollbar
    lateinit var behavior: LockableBottomDrawerBehavior<View>
    private var dockHeight = 0

    private lateinit var blurBg: LayerDrawable

    private lateinit var searchBar: View

    private lateinit var desktop: NestedScrollView
    private lateinit var desktopContent: View

    private lateinit var feedProgressBar: ProgressBar

    lateinit var widgetLayout: WidgetSection
    private lateinit var contactCardView: ContactCardView
    lateinit var musicCard: MusicCard
    private lateinit var notifications: NotificationCards
    private lateinit var feedRecycler: NewsCards

    private lateinit var batteryBar: ProgressBar

    private lateinit var powerManager: PowerManager

    init {
        Tools.publicContextReference = WeakReference(this)
        instance = this

        setDock = {
            val columnCount = Settings["dock:columns", 5]
            val appSize = min(when (Settings["dockicsize", 1]) {
                0 -> 64.dp.toInt()
                2 -> 84.dp.toInt()
                else -> 74.dp.toInt()
            }, ((Device.displayWidth - 32.dp) / columnCount).toInt())
            val rowCount = Settings["dock:rows", 1]
            val showLabels = Settings["dockLabelsEnabled", false]
            val notifBadgesEnabled = Settings["notif:badges", true]
            val notifBadgesShowNum = Settings["notif:badges:show_num", true]
            val container = findViewById<GridLayout>(R.id.dockContainer).apply {
                this.removeAllViews()
                this.columnCount = columnCount
                this.rowCount = rowCount
            }
            var i = 0
            while (i < columnCount * rowCount) {
                val view = LayoutInflater.from(applicationContext).inflate(R.layout.drawer_item, container, false)
                val img = view.findViewById<ImageView>(R.id.iconimg)
                view.findViewById<View>(R.id.iconFrame).run {
                    layoutParams.height = appSize
                    layoutParams.width = appSize
                }
                val item = Dock[i]
                val label = view.findViewById<TextView>(R.id.icontxt)
                if (showLabels) {
                    label.text = item?.label
                    label.setTextColor(Settings["dockLabelColor", -0x11111112])
                } else {
                    label.visibility = GONE
                }
                if (item is Folder) {
                    img.setImageDrawable(item.icon)
                    val badge = view.findViewById<TextView>(R.id.notificationBadge)
                    if (notifBadgesEnabled) {
                        val notificationCount = item.calculateNotificationCount()
                        if (notificationCount != 0) {
                            badge.visibility = View.VISIBLE
                            badge.text = if (notifBadgesShowNum) notificationCount.toString() else ""
                            ThemeTools.generateNotificationBadgeBGnFG { bg, fg ->
                                badge.background = bg
                                badge.setTextColor(fg)
                            }
                        } else { badge.visibility = View.GONE }
                    } else { badge.visibility = View.GONE }

                    view.setOnClickListener { item.open(this@Main, view, i) }
                    view.setOnLongClickListener(ItemLongPress.folder(this@Main, item, i))
                } else if (item is Shortcut) {
                    if (item.isInstalled(packageManager)) {
                        img.setImageDrawable(item.icon)
                        view.setOnClickListener { item.open(this@Main, it) }
                    } else {
                        Dock[i] = null
                    }
                } else if (item is App) {
                    if (!item.isInstalled(packageManager)) {
                        Dock[i] = null
                        continue
                    }
                    val badge = view.findViewById<TextView>(R.id.notificationBadge)
                    if (notifBadgesEnabled && item.notificationCount != 0) {
                        badge.visibility = View.VISIBLE
                        badge.text = if (notifBadgesShowNum) item.notificationCount.toString() else ""
                        ThemeTools.generateNotificationBadgeBGnFG(item.icon!!) { bg, fg ->
                            badge.background = bg
                            badge.setTextColor(fg)
                        }
                    } else { badge.visibility = View.GONE }
                    img.setImageDrawable(item.icon)
                    view.setOnClickListener { item.open(this@Main, it) }
                    view.setOnLongClickListener(ItemLongPress.dock(this@Main, item, i))
                }
                container.addView(view)
                i++
            }
            val containerHeight = (appSize + if (Settings["dockLabelsEnabled", false]) 18.sp.toInt() else 0) * rowCount
            dockHeight = if (Settings["docksearchbarenabled", false] && !isTablet) {
                containerHeight + 84.dp.toInt()
            } else {
                containerHeight + 14.dp.toInt()
            }
            container.layoutParams.height = containerHeight
            behavior.peekHeight = (dockHeight + Tools.navbarHeight + Settings["dockbottompadding", 10].dp).toInt()
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getRealMetrics(metrics)
            findViewById<View>(R.id.drawercontent).layoutParams.height = metrics.heightPixels
            (findViewById<View>(R.id.homeView).layoutParams as FrameLayout.LayoutParams).topMargin = -dockHeight
            if (Settings["feed:show_behind_dock", false]) {
                (desktop.layoutParams as ViewGroup.MarginLayoutParams).setMargins(0, dockHeight, 0, 0)
                desktopContent.setPadding(0, 12.dp.toInt(), 0, (dockHeight + Tools.navbarHeight + Settings["dockbottompadding", 10].dp).toInt())
            } else {
                (desktop.layoutParams as ViewGroup.MarginLayoutParams).setMargins(0, dockHeight, 0, dockHeight + Tools.navbarHeight + (Settings["dockbottompadding", 10] - 18).dp.toInt())
                desktopContent.setPadding(0, 12.dp.toInt(), 0, 24.dp.toInt())
            }
            if (Settings["dock:background_type", 0] == 1) {
                val bg = drawer.background as LayerDrawable
                bg.setLayerInset(0, 0, 0, 0, Device.displayHeight - Settings["dockbottompadding", 10].dp.toInt())
                bg.setLayerInset(1, 0, behavior.peekHeight, 0, 0)
            }
            (findViewById<View>(R.id.blur).layoutParams as CoordinatorLayout.LayoutParams).topMargin = dockHeight
            window.decorView.setOnDragListener { _, event ->
                when (event.action) {
                    DragEvent.ACTION_DRAG_LOCATION -> {
                        val view = event.localState as View?
                        if (view != null) {
                            val location = IntArray(2)
                            view.getLocationOnScreen(location)
                            val x = abs(event.x - location[0] - view.measuredWidth / 2f)
                            val y = abs(event.y - location[1] - view.measuredHeight / 2f)
                            if (x > view.width / 3.5f || y > view.height / 3.5f) {
                                ItemLongPress.currentPopup?.dismiss()
                                Folder.currentlyOpen?.dismiss()
                                behavior.state = BottomDrawerBehavior.STATE_COLLAPSED
                            }
                        }
                    }
                    DragEvent.ACTION_DRAG_STARTED -> {
                        (event.localState as View?)?.visibility = INVISIBLE
                    }
                    DragEvent.ACTION_DRAG_ENDED -> {
                        (event.localState as View?)?.visibility = VISIBLE
                        ItemLongPress.currentPopup?.isFocusable = true
                        ItemLongPress.currentPopup?.update()
                    }
                    DragEvent.ACTION_DROP -> {
                        (event.localState as View?)?.visibility = VISIBLE
                        if (behavior.state != BottomDrawerBehavior.STATE_EXPANDED) {
                            if (event.y > Device.displayHeight - dockHeight) {
                                val item = LauncherItem(event.clipData.description.label.toString())!!
                                val location = IntArray(2)
                                var i = 0
                                while (i < container.childCount) {
                                    container.getChildAt(i).getLocationOnScreen(location)
                                    val threshHold = min(container.getChildAt(i).height / 2.toFloat(), 100.dp)
                                    if (abs(location[0] - (event.x - container.getChildAt(i).height / 2f)) < threshHold && abs(location[1] - (event.y - container.getChildAt(i).height / 2f)) < threshHold) {
                                        Dock.add(item, i)
                                        break
                                    }
                                    i++
                                }
                            }
                            setDock()
                        }
                    }
                }
                true
            }
        }
        setCustomizations = {

            ThemeTools.setDockBG(drawer, realdock, dockContainerContainer)

            if (shouldSetApps) AppLoader(this@Main, onAppLoaderEnd).execute() else {
                if (Settings["drawer:sections_enabled", false]) {
                    drawerGrid.adapter = SectionedDrawerAdapter()
                    drawerGrid.onItemClickListener = null
                    drawerGrid.onItemLongClickListener = null
                } else {
                    drawerGrid.adapter = DrawerAdapter()
                    drawerGrid.onItemClickListener = AdapterView.OnItemClickListener { _, v, i, _ -> apps[i].open(this@Main, v) }
                    drawerGrid.onItemLongClickListener = ItemLongPress.olddrawer(this@Main)
                }
                setDock()
            }

            if (Settings["drawer:sections_enabled", false]) {
                drawerGrid.numColumns = 1
                drawerGrid.verticalSpacing = 0
            } else {
                drawerGrid.numColumns = Settings["drawer:columns", 4]
                drawerGrid.verticalSpacing = Settings["verticalspacing", 12].dp.toInt()
            }

            behavior.setLocked(!Settings["drawer:slide_up", true])

            applyFontSetting()

            if (Settings["hidestatus", false]) {
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }

            setDrawerSearchBarVisible(Settings["drawersearchbarenabled", true])
            setDrawerSearchbarBGColor(Settings["searchcolor", 0x33000000])
            setDrawerSearchbarFGColor(Settings["searchtxtcolor", -0x1])
            setDrawerSearchbarRadius(Settings["searchradius", 0])

            setSearchHintText(Settings["searchhinttxt", "Search.."])

            setDockSearchBarVisible(Settings["docksearchbarenabled", false])
            setDockSearchbarBelowApps(Settings["dock:search:below_apps", true])
            setDockSearchbarBGColor(Settings["docksearchcolor", -0x22000001])
            setDockSearchbarFGColor(Settings["docksearchtxtcolor", -0x1000000])
            setDockSearchbarRadius(Settings["dock:search:radius", 30])
            setDockHorizontalMargin(Settings["dock:margin_x", 16])

            if (Settings["contacts_card:enabled", false]) {
                contactCardView.visibility = View.VISIBLE
                contactCardView.updateTheme()
            } else {
                contactCardView.visibility = View.GONE
            }

            feedProgressBar.indeterminateDrawable.setTint(accentColor)
            if (Settings["feed:enabled", true]) {
                feedRecycler.visibility = VISIBLE
                feedRecycler.updateTheme(this)
            } else {
                feedRecycler.visibility = GONE
                feedRecycler.adapter = null
            }
            musicCard.updateTheme()
            if (Settings["hidefeed", false]) {
                feedRecycler.hide()
                desktop.setOnScrollChangeListener { _: androidx.core.widget.NestedScrollView, _, y, _, oldY ->
                    val a = 6.dp
                    val distance = oldY - y
                    if (y > a) {
                        feedRecycler.show()
                        if (distance > a || y >= desktopContent.height - dockHeight - desktop.height) {
                            if (!LauncherMenu.isActive) {
                                behavior.state = BottomDrawerBehavior.STATE_COLLAPSED
                            }
                        } else if (distance < -a) {
                            behavior.state = BottomDrawerBehavior.STATE_HIDDEN
                        }
                    } else {
                        if (!LauncherMenu.isActive) {
                            behavior.state = BottomDrawerBehavior.STATE_COLLAPSED
                        }
                        if (y < a && oldY >= a) {
                            feedRecycler.hide()
                        }
                    }
                }
            } else {
                feedRecycler.show()
                desktop.setOnScrollChangeListener { _: androidx.core.widget.NestedScrollView, _, y, _, oldY ->
                    val a = 6.dp
                    val distance = oldY - y
                    if (distance > a || y < a || y + desktop.height >= desktopContent.height - dockHeight) {
                        if (!LauncherMenu.isActive) {
                            behavior.state = BottomDrawerBehavior.STATE_COLLAPSED
                        }
                    } else if (distance < -a) {
                        behavior.state = BottomDrawerBehavior.STATE_HIDDEN
                    }
                }
            }
            val fadingEdge = Settings["feed:fading_edge", true]
            if (fadingEdge && !Settings["hidestatus", false]) {
                desktop.setPadding(0, getStatusBarHeight() - 12.dp.toInt(), 0, 0)
            }
            desktop.isVerticalFadingEdgeEnabled = fadingEdge

            shouldSetApps = false
            customized = false

            if (Settings["notif:cards", true] || Settings["notif:badges", true]) {
                NotificationService.onUpdate = {
                    try {
                        if (Settings["notif:cards", true]) runOnUiThread {
                            notifications.update()
                        }
                        if (Settings["notif:badges", true]) runOnUiThread {
                            drawerGrid.invalidateViews()
                            setDock()
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }
                try { startService(Intent(this, NotificationService::class.java)) }
                catch (e: Exception) {}
            }

            if (Settings["notif:cards", true]) {
                notifications.visibility = VISIBLE
                notifications.updateTheme()
            } else {
                notifications.visibility = GONE
            }
            setDrawerScrollbarEnabled(Settings["drawer:scrollbar_enabled", false])
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings.init(applicationContext)
        if (Settings["init", true]) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
            exitProcess(0)
        }

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            Log.e("posidonLauncher", "uncaught exception", throwable)
            Settings.applyNow()
            startActivity(Intent(this, StackTraceActivity::class.java).apply { putExtra("throwable", throwable) })
            Process.killProcess(Process.myPid())
            exitProcess(0)
        }

        accentColor = Settings["accent", 0x1155ff] or -0x1000000
        Kustom["accent"] = accentColor.toUInt().toString(16)
        setContentView(R.layout.main)

        launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        musicService = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        launcherApps.registerCallback(AppLoader.Callback(this, onAppLoaderEnd))

        updateNavbarHeight(this@Main)

        if (Settings["search:asHome", false]) {
            startActivity(Intent(this, SearchActivity::class.java))
            AppLoader(this.applicationContext, onAppLoaderEnd).execute()
            finish()
            return
        }

        batteryBar = findViewById(R.id.battery)
        registerReceiver(batteryInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        desktop = findViewById<NestedScrollView>(R.id.desktop).apply {
            isNestedScrollingEnabled = false
            isSmoothScrollingEnabled = false
            onTopOverScroll = {
                if (!LauncherMenu.isActive && behavior.state != BottomDrawerBehavior.STATE_EXPANDED) {
                    Gestures.performTrigger(Settings["gesture:feed:top_overscroll", Gestures.PULL_DOWN_NOTIFICATIONS])
                }
            }
            onBottomOverScroll = {
                if (!LauncherMenu.isActive) {
                    Gestures.performTrigger(Settings["gesture:feed:bottom_overscroll", Gestures.OPEN_APP_DRAWER])
                }
            }
            desktopContent = findViewById<View>(R.id.desktopContent)
            desktopContent.setOnLongClickListener(LauncherMenu())
        }

        drawerGrid = findViewById(R.id.drawergrid)
        searchBar = findViewById(R.id.searchbar)
        drawerGrid.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && drawerGrid.canScrollVertically(-1))
                drawerGrid.requestDisallowInterceptTouchEvent(true)
            false
        }
        drawer = findViewById(R.id.drawer)
        behavior = LockableBottomDrawerBehavior.from<View>(drawer).apply {
            state = BottomDrawerBehavior.STATE_COLLAPSED
            isHideable = false
            addBottomSheetCallback(object : BottomSheetCallback() {

                val things = IntArray(5)
                val radii = FloatArray(8)
                val colors = IntArray(3)
                val floats = FloatArray(1)

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomDrawerBehavior.STATE_COLLAPSED -> {
                            if (this@Main.hasWindowFocus() && Settings["kustom:variables:enable", false]) {
                                Kustom["screen"] = "home"
                            }
                            drawerGrid.smoothScrollToPositionFromTop(0, 0, 0)
                            colors[0] = Settings["dock:background_color", -0x78000000] and 0x00ffffff
                            colors[1] = Settings["dock:background_color", -0x78000000]

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                val list = ArrayList<Rect>()
                                list.add(Rect(0, 0, Device.displayWidth, Device.displayHeight))
                                window.decorView.findViewById<View>(android.R.id.content).systemGestureExclusionRects = list
                            }
                        }
                        BottomDrawerBehavior.STATE_EXPANDED -> {
                            if (Settings["kustom:variables:enable", false]) {
                                Kustom["screen"] = "drawer"
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                val list = ArrayList<Rect>()
                                window.decorView.findViewById<View>(android.R.id.content).systemGestureExclusionRects = list
                            }
                        }
                    }
                    ItemLongPress.currentPopup?.dismiss()
                    colors[2] = Settings["drawer:background_color", -0x78000000]
                    floats[0] = dockHeight.toFloat() / (Device.displayHeight + dockHeight)
                    things[0] = if (Tools.canBlurDrawer) Settings["blurLayers", 1] else 0
                    things[1] = Settings["dock:background_color", -0x78000000]
                    things[2] = Settings["dock:background_type", 0]
                    val tr = Settings["dockradius", 30].dp
                    radii[0] = tr
                    radii[1] = tr
                    radii[2] = tr
                    radii[3] = tr
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    val inverseOffset = 1 - slideOffset
                    drawerGrid.alpha = slideOffset
                    drawerScrollBar.alpha = slideOffset
                    desktop.alpha = inverseOffset.pow(1.2f)
                    if (slideOffset >= 0) {
                        try {
                            val bg = drawer.background
                            when (things[2]) {
                                0 -> {
                                    bg as ShapeDrawable
                                    bg.paint.color = ColorTools.blendColors(colors[2], things[1], slideOffset)
                                    bg.shape = RoundRectShape(radii, null, null)
                                }
                                1 -> {
                                    bg as LayerDrawable
                                    colors[1] = ColorTools.blendColors(colors[2], things[1], slideOffset)
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
                    } else if (!Settings["feed:show_behind_dock", false]) {
                        (desktop.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = ((1 + slideOffset) * (dockHeight + Tools.navbarHeight + (Settings["dockbottompadding", 10] - 18).dp)).toInt()
                        desktop.requestLayout()
                    }
                    findViewById<View>(R.id.realdock).alpha = inverseOffset
                }
            })
        }

        drawerScrollBar = AlphabetScrollbar(drawerGrid)
        findViewById<ViewGroup>(R.id.drawercontent).addView(drawerScrollBar)
        (drawerScrollBar.layoutParams as FrameLayout.LayoutParams).apply {
            width = 24.dp.toInt()
            gravity = Gravity.END
        }
        drawerScrollBar.bringToFront()

        feedProgressBar = findViewById(R.id.feedProgressBar)

        widgetLayout = findViewById(R.id.widgets)
        contactCardView = findViewById(R.id.contacts)
        musicCard = findViewById(R.id.musicCard)
        notifications = findViewById(R.id.notifications)
        feedRecycler = findViewById(R.id.feedrecycler)

        widgetLayout.init(0xe1d9e15)

        setCustomizations()
        loadFeed()

        val scaleGestureDetector = ScaleGestureDetector(this@Main, PinchListener())
        findViewById<View>(R.id.homeView).setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP && behavior.state == BottomDrawerBehavior.STATE_COLLAPSED)
                WallpaperManager.getInstance(this@Main).sendWallpaperCommand(
                    v.windowToken,
                    WallpaperManager.COMMAND_TAP,
                    event.x.toInt(),
                    event.y.toInt(),
                    0, null)
            false
        }
        desktop.setOnTouchListener { _, event ->
            if (hasWindowFocus()) {
                scaleGestureDetector.onTouchEvent(event)
                false
            } else true
        }
        if (Settings["mnmlstatus", false]) {
            window.decorView.systemUiVisibility =
                SYSTEM_UI_FLAG_LAYOUT_STABLE or
                SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                SYSTEM_UI_FLAG_LOW_PROFILE
        } else {
            window.decorView.systemUiVisibility =
                SYSTEM_UI_FLAG_LAYOUT_STABLE or
                SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val list = ArrayList<Rect>()
            list.add(Rect(0, 0, Device.displayWidth, Device.displayHeight))
            findViewById<View>(R.id.homeView).systemGestureExclusionRects = list
        }

        blurBg = LayerDrawable(arrayOf<Drawable>(
                ColorDrawable(0x0),
                ColorDrawable(0x0),
                ColorDrawable(0x0),
                ColorDrawable(0x0)
        ))
        findViewById<ImageView>(R.id.blur).setImageDrawable(blurBg)

        System.gc()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        widgetLayout.handleActivityResult(this, requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private val batteryInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            batteryBar.progress = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        }
    }

    private val onAppLoaderEnd = {
        val s = drawerGrid.scrollY
        if (Settings["drawer:sections_enabled", false]) {
            drawerGrid.adapter = SectionedDrawerAdapter()
            drawerGrid.onItemClickListener = null
            drawerGrid.onItemLongClickListener = null
        } else {
            drawerGrid.adapter = DrawerAdapter()
            drawerGrid.onItemClickListener = AdapterView.OnItemClickListener { _, v, i, _ -> apps[i].open(this@Main, v) }
            drawerGrid.onItemLongClickListener = ItemLongPress.olddrawer(this@Main)
        }
        drawerGrid.scrollY = s
        drawerScrollBar.updateAdapter()
        setDock()
    }

    fun loadFeed() {
        if (!Settings["feed:enabled", true] || feedProgressBar.visibility == VISIBLE) {
            return
        }
        if (Settings["feed:show_spinner", true]) {
            feedProgressBar.visibility = VISIBLE
            feedProgressBar.animate().translationY(0f).alpha(1f).setListener(null)
            FeedLoader.loadFeed { success, items ->
                runOnUiThread {
                    if (success) {
                        feedRecycler.updateFeed(items!!)
                    }
                    feedProgressBar.animate().translationY(-(72).dp).alpha(0f).onEnd {
                        feedProgressBar.visibility = GONE
                    }
                }
            }
        } else FeedLoader.loadFeed { success, items ->
            if (success) runOnUiThread {
                feedRecycler.updateFeed(items!!)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        onUpdate()
        setDock()
    }

    override fun onResume() {
        super.onResume()
        if (Settings["search:asHome", false]) {
            startActivity(Intent(this, SearchActivity::class.java))
            finish()
            return
        }
        if (Settings["kustom:variables:enable", false]) {
            Kustom["screen"] = "home"
        }
        widgetLayout.startListening()
        overridePendingTransition(R.anim.home_enter, R.anim.appexit)
        onUpdate()
    }

    private fun onUpdate() {
        val tmp = Tools.navbarHeight
        updateNavbarHeight(this)
        if (Tools.canBlurDrawer) {
            val shouldHide = behavior.state == BottomDrawerBehavior.STATE_COLLAPSED || behavior.state == BottomDrawerBehavior.STATE_HIDDEN
            thread (isDaemon = true) {
                val blurLayers = Settings["blurLayers", 1]
                val radius = Settings["drawer:blur:rad", 15f]
                for (i in 0 until blurLayers) {
                    val bmp = Tools.blurredWall(radius / blurLayers * (i + 1))
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
        thread (isDaemon = true) {
            if (Settings["contacts_card:enabled", false] && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                ContactItem.getList(true).also {
                    runOnUiThread {
                        contactCardView.setItems(it)
                    }
                }
            }
        }
        if (customized || tmp != Tools.navbarHeight) {
            setCustomizations()
        } else if (!powerManager.isPowerSaveMode && Settings["animatedicons", true]) {
            for (app in apps) {
                tryAnimate(app.icon!!)
            }
        }
        loadFeed()
        if (Settings["notif:cards", true] || Settings["notif:badges", true]) {
            NotificationService.onUpdate()
        }
    }

    override fun onPause() {
        super.onPause()
        if (LauncherMenu.isActive) {
            LauncherMenu.dialog!!.dismiss()
        }
        behavior.state = BottomDrawerBehavior.STATE_COLLAPSED
        if (!Settings["feed:keep_pos", false]) {
            desktop.scrollTo(0, 0)
        }
        widgetLayout.stopListening()
        if (Settings["notif:cards", true] && Settings["collapseNotifications", false] && NotificationService.notificationsAmount > 1) {
            notifications.collapse()
        }
        if (Settings["kustom:variables:enable", false]) {
            Kustom["screen"] = "?"
        }
    }

    override fun onStop() {
        super.onStop()
        widgetLayout.stopListening()
    }

    override fun onStart() {
        super.onStart()
        widgetLayout.startListening()

        if (Settings["drawer:sections_enabled", false]) {
            drawerGrid.adapter = SectionedDrawerAdapter()
            drawerGrid.onItemClickListener = null
            drawerGrid.onItemLongClickListener = null
        } else {
            drawerGrid.adapter = DrawerAdapter()
            drawerGrid.onItemClickListener = AdapterView.OnItemClickListener { _, v, i, _ -> apps[i].open(this@Main, v) }
            drawerGrid.onItemLongClickListener = ItemLongPress.olddrawer(this@Main)
        }
        drawerScrollBar.updateAdapter()
    }

    override fun onDestroy() {
        unregisterReceiver(batteryInfoReceiver)
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            if (Settings["notif:cards", true] || Settings["notif:badges", true]) {
                try { startService(Intent(this, NotificationService::class.java)) }
                catch (e: Exception) {}
            }
            if (Settings["mnmlstatus", false]) window.decorView.systemUiVisibility =
                    SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    SYSTEM_UI_FLAG_LOW_PROFILE
            if (shouldSetApps) {
                AppLoader(this@Main, onAppLoaderEnd).execute()
            }

            if (Settings["kustom:variables:enable", false]) {
                if (behavior.state == STATE_EXPANDED) {
                    Kustom["screen"] = "drawer"
                } else {
                    Kustom["screen"] = "home"
                }
            }
        } else {
            window.decorView.systemUiVisibility =
                SYSTEM_UI_FLAG_LAYOUT_STABLE or
                SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
    }

    override fun onBackPressed() = when {
        behavior.state == BottomDrawerBehavior.STATE_EXPANDED -> behavior.state = BottomDrawerBehavior.STATE_COLLAPSED
        widgetLayout.resizing -> widgetLayout.resizing = false
        else -> Gestures.performTrigger(Settings["gesture:back", ""])
    }

    fun openSearch(v: View?) = startActivity(
        Intent(this, if (Settings["dev:console", false]) ConsoleActivity::class.java else SearchActivity::class.java),
        ActivityOptions.makeCustomAnimation(this, R.anim.fadein, R.anim.fadeout).toBundle())

    companion object {

        var appSections = ArrayList<ArrayList<App>>()
        var apps = ArrayList<App>()

        var shouldSetApps = true
        var customized = false

        var accentColor = -0xeeaa01

        lateinit var instance: Main private set

        lateinit var setCustomizations: () -> Unit private set
        lateinit var setDock: () -> Unit private set

        lateinit var launcherApps: LauncherApps
        lateinit var musicService: AudioManager

        fun setSearchHintText(text: String) {
            Settings["searchhinttxt"] = text
            instance.findViewById<TextView>(R.id.searchTxt).text = text
            instance.findViewById<TextView>(R.id.docksearchtxt).text = text
        }

        fun setDrawerSearchbarBGColor(color: Int) {
            Settings["searchcolor"] = color
            val bg = ShapeDrawable()
            val tr = Settings["searchradius", 0].dp
            bg.shape = RoundRectShape(floatArrayOf(tr, tr, tr, tr, 0f, 0f, 0f, 0f), null, null)
            bg.paint.color = color
            instance.searchBar.background = bg
        }

        fun setDrawerSearchbarRadius(radius: Int) {
            Settings["searchradius"] = radius
            val bg = ShapeDrawable()
            val tr = radius.dp
            bg.shape = RoundRectShape(floatArrayOf(tr, tr, tr, tr, 0f, 0f, 0f, 0f), null, null)
            bg.paint.color = Settings["searchcolor", 0x33000000]
            instance.searchBar.background = bg
        }

        fun setDrawerSearchbarFGColor(color: Int) {
            Settings["searchtxtcolor"] = color
            instance.findViewById<TextView>(R.id.searchTxt).setTextColor(color)
            val searchIcon = instance.findViewById<ImageView>(R.id.searchIcon)
            searchIcon.imageTintList = ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(Settings["searchhintcolor", -0x1]))
        }

        fun setDrawerSearchBarVisible(visible: Boolean) {
            Settings["drawersearchbarenabled"] = visible
            if (visible) {
                instance.drawerGrid.setPadding(0, instance.getStatusBarHeight(), 0, Tools.navbarHeight + 56.dp.toInt())
                instance.searchBar.setPadding(0, 0, 0, Tools.navbarHeight)
                instance.searchBar.visibility = VISIBLE
            } else {
                instance.searchBar.visibility = GONE
                instance.drawerGrid.setPadding(0, instance.getStatusBarHeight(), 0, Tools.navbarHeight + 12.dp.toInt())
            }
        }

        fun setDockSearchbarBGColor(color: Int) {
            Settings["docksearchcolor"] = color
            val bg = ShapeDrawable()
            val tr = Settings["dock:search:radius", 30].dp
            bg.shape = RoundRectShape(floatArrayOf(tr, tr, tr, tr, tr, tr, tr, tr), null, null)
            bg.paint.color = color
            instance.findViewById<View>(R.id.docksearchbar).background = bg
        }

        fun setDockSearchbarRadius(radius: Int) {
            val bg = ShapeDrawable()
            Settings["dock:search:radius"] = radius
            val tr = radius.dp
            bg.shape = RoundRectShape(floatArrayOf(tr, tr, tr, tr, tr, tr, tr, tr), null, null)
            bg.paint.color = Settings["docksearchcolor", -0x22000001]
            instance.findViewById<View>(R.id.docksearchbar).background = bg
        }

        fun setDockSearchbarFGColor(color: Int) {
            Settings["docksearchtxtcolor"] = color
            instance.findViewById<TextView>(R.id.docksearchtxt).setTextColor(color)
            val dickSearchIcon = instance.findViewById<ImageView>(R.id.docksearchic)
            dickSearchIcon.imageTintList = ColorStateList.valueOf(color)
            val battery = instance.findViewById<ProgressBar>(R.id.battery)
            battery.progressTintList = ColorStateList.valueOf(color)
            battery.indeterminateTintMode = PorterDuff.Mode.MULTIPLY
            battery.progressBackgroundTintList = ColorStateList.valueOf(color)
            battery.progressBackgroundTintMode = PorterDuff.Mode.MULTIPLY
            (instance.findViewById<ProgressBar>(R.id.battery).progressDrawable as LayerDrawable).getDrawable(3).setTint(if (ColorTools.useDarkText(color)) -0x23000000 else -0x11000001)
        }

        fun setDockSearchBarVisible(visible: Boolean) {
            Settings["docksearchbarenabled"] = visible
            if (visible) {
                instance.findViewById<View>(R.id.docksearchbar).visibility = VISIBLE
                instance.findViewById<View>(R.id.battery).visibility = VISIBLE
            } else {
                instance.findViewById<View>(R.id.docksearchbar).visibility = GONE
                instance.findViewById<View>(R.id.battery).visibility = GONE
            }
        }

        fun setDockSearchbarBelowApps(isBelow: Boolean) {
            Settings["dock:search:below_apps"] = isBelow
            if (isBelow) {
                instance.findViewById<View>(R.id.docksearchbar).bringToFront()
            } else {
                instance.findViewById<View>(R.id.dockContainerContainer).bringToFront()
            }
        }

        fun setDrawerScrollbarEnabled(enabled: Boolean) {
            //Settings["drawer:scrollbar_enabled"] = enabled
            if (enabled) {
                instance.drawerScrollBar.visibility = VISIBLE
                (instance.drawerGrid.layoutParams as FrameLayout.LayoutParams).marginEnd =
                    if (Settings["drawer:sections_enabled", false]) 0
                    else 24.dp.toInt()
                instance.drawerGrid.layoutParams = instance.drawerGrid.layoutParams
                instance.drawerScrollBar.update()
            } else  {
                instance.drawerScrollBar.visibility = GONE
                (instance.drawerGrid.layoutParams as FrameLayout.LayoutParams).marginEnd = 0
                instance.drawerGrid.layoutParams = instance.drawerGrid.layoutParams
            }
        }

        fun setDockHorizontalMargin(margin: Int) {
            val m = margin.dp.toInt()
            Settings["dock:margin_x"] = margin
            instance.realdock.setPadding(m, 0, m, 0)
        }
    }
}