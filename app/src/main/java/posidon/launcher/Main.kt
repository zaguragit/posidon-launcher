package posidon.launcher

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
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
import android.widget.GridView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import posidon.launcher.LauncherMenu.PinchListener
import posidon.launcher.external.Widget
import posidon.launcher.external.Widget.REQUEST_CREATE_APPWIDGET
import posidon.launcher.external.Widget.REQUEST_PICK_APPWIDGET
import posidon.launcher.feed.news.FeedAdapter
import posidon.launcher.feed.news.FeedItem
import posidon.launcher.feed.news.FeedLoader
import posidon.launcher.feed.notifications.NotificationAdapter
import posidon.launcher.feed.notifications.NotificationService
import posidon.launcher.items.*
import posidon.launcher.search.ConsoleActivity
import posidon.launcher.search.SearchActivity
import posidon.launcher.storage.Settings
import posidon.launcher.tools.*
import posidon.launcher.tools.Tools.animate
import posidon.launcher.tools.Tools.blurredWall
import posidon.launcher.tools.Tools.canBlurWall
import posidon.launcher.tools.Tools.updateNavbarHeight
import posidon.launcher.tutorial.WelcomeActivity
import posidon.launcher.view.*
import posidon.launcher.view.BottomDrawerBehavior.BottomSheetCallback
import posidon.launcher.view.ResizableLayout.OnResizeListener
import java.lang.ref.WeakReference
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.*
import kotlin.system.exitProcess

class Main : AppCompatActivity() {

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
            val container = findViewById<GridLayout>(R.id.dockContainer)
            container.removeAllViews()
            container.columnCount = columnCount
            container.rowCount = rowCount
            var i = 0
            while (i < columnCount * rowCount) {
                val view = LayoutInflater.from(applicationContext).inflate(R.layout.drawer_item, container, false)
                val img = view.findViewById<ImageView>(R.id.iconimg)
                view.findViewById<View>(R.id.iconFrame).run {
                    layoutParams.height = appSize
                    layoutParams.width = appSize
                }
                val item = Dock[i]
                if (!showLabels) view.findViewById<View>(R.id.icontxt).visibility = GONE
                if (item is Folder) {
                    img.setImageDrawable(item.icon)
                    if (showLabels) {
                        view.findViewById<TextView>(R.id.icontxt).text = item.label
                        view.findViewById<TextView>(R.id.icontxt).setTextColor(Settings["dockLabelColor", -0x11111112])
                    }
                    if (notifBadgesEnabled) {
                        var notificationCount = 0
                        for (app in item.apps) {
                            notificationCount += app.notificationCount
                        }
                        if (notificationCount != 0) {
                            val badge = view.findViewById<TextView>(R.id.notificationBadge)
                            badge.visibility = View.VISIBLE
                            badge.text = notificationCount.toString()
                            badge.background = ColorTools.iconBadge(accentColor)
                            badge.setTextColor(if (ColorTools.useDarkText(accentColor)) 0xff111213.toInt() else 0xffffffff.toInt())
                        } else { view.findViewById<TextView>(R.id.notificationBadge).visibility = View.GONE }
                    } else { view.findViewById<TextView>(R.id.notificationBadge).visibility = View.GONE }
                    val finalI = i
                    val bgColor = Settings["folderBG", -0x22eeeded]
                    val r = Settings["folderCornerRadius", 18].dp
                    val labelsEnabled = Settings["folderLabelsEnabled", false]
                    view.setOnClickListener { if (!Folder.currentlyOpen) {
                        Folder.currentlyOpen = true
                        val content = LayoutInflater.from(this@Main).inflate(R.layout.folder_layout, null)
                        val popupWindow = PopupWindow(content, ListPopupWindow.WRAP_CONTENT, ListPopupWindow.WRAP_CONTENT, true)
                        popupWindow.setBackgroundDrawable(ColorDrawable(0x0))
                        val container = content.findViewById<GridLayout>(R.id.container)
                        container.columnCount = Settings["folderColumns", 3]
                        if (Settings["folder:show_title", true]) {
                            val title = content.findViewById<TextView>(R.id.title)
                            title.setTextColor(Settings["folder:title_color", 0xffffffff.toInt()])
                            title.text = item.label
                        } else {
                            content.findViewById<View>(R.id.title).visibility = View.GONE
                            content.findViewById<View>(R.id.separator).visibility = View.GONE
                        }
                        val appList: List<App?> = item.apps
                        var i1 = 0
                        val appListSize = appList.size
                        while (i1 < appListSize) {
                            val app = appList[i1]
                            if (app == null) {
                                item.apps.removeAt(i1)
                                Dock[finalI] = item
                            } else {
                                val appIcon = LayoutInflater.from(applicationContext).inflate(R.layout.drawer_item, null)
                                val icon = appIcon.findViewById<ImageView>(R.id.iconimg)
                                appIcon.findViewById<View>(R.id.iconFrame).run {
                                    layoutParams.height = appSize
                                    layoutParams.width = appSize
                                }
                                icon.setImageDrawable(app.icon)
                                if (labelsEnabled) {
                                    val iconTxt = appIcon.findViewById<TextView>(R.id.icontxt)
                                    iconTxt.text = app.label
                                    iconTxt.setTextColor(Settings["folder:label_color", -0x22000001])
                                } else appIcon.findViewById<View>(R.id.icontxt).visibility = GONE
                                if (notifBadgesEnabled && app.notificationCount != 0) {
                                    val badge = appIcon.findViewById<TextView>(R.id.notificationBadge)
                                    badge.visibility = View.VISIBLE
                                    badge.text = app.notificationCount.toString()
                                    Palette.from(app.icon!!.toBitmap()).generate {
                                        val color = it?.getDominantColor(0xff111213.toInt()) ?: 0xff111213.toInt()
                                        badge.background = ColorTools.iconBadge(color)
                                        badge.setTextColor(if (ColorTools.useDarkText(color)) 0xff111213.toInt() else 0xffffffff.toInt())
                                    }
                                } else { appIcon.findViewById<TextView>(R.id.notificationBadge).visibility = View.GONE }
                                appIcon.setOnClickListener { view ->
                                    app.open(this@Main, view)
                                    popupWindow.dismiss()
                                }
                                appIcon.setOnLongClickListener(ItemLongPress.insideFolder(this@Main, app, finalI, view, i1, popupWindow))
                                //appIcon.setOnLongClickListener(ItemLongPress.insideFolderNew(this@Main, app, finalI, view, i1, popupWindow))
                                container.addView(appIcon)
                            }
                            i1++
                        }
                        popupWindow.setOnDismissListener { Folder.currentlyOpen = false }
                        val bg = ShapeDrawable()
                        bg.shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
                        bg.paint.color = bgColor
                        content.findViewById<View>(R.id.bg).background = bg
                        val location = IntArray(2)
                        view.getLocationInWindow(location)
                        val gravity = if (location[0] > Device.displayWidth / 2) Gravity.END else Gravity.START
                        val x = if (location[0] > Device.displayWidth / 2) {
                            Device.displayWidth - location[0] - view.measuredWidth
                        } else location[0]
                        var y = (-view.y + view.height * Settings["dock:rows", 1] + Tools.navbarHeight + (Settings["dockbottompadding", 10] + 14).dp).toInt()
                        if (Settings["dock:search:below_apps", false] && !isTablet) {
                            y += 68.dp.toInt()
                        }
                        popupWindow.showAtLocation(view, Gravity.BOTTOM or gravity, x, y)
                    }}
                    view.setOnLongClickListener(ItemLongPress.folder(this@Main, item, i))
                } else if (item is Shortcut) {
                    println("SHORTCUTT!!")
                    if (item.isInstalled(packageManager)) {
                        if (showLabels) {
                            view.findViewById<TextView>(R.id.icontxt).text = item.label
                            view.findViewById<TextView>(R.id.icontxt).setTextColor(Settings["dockLabelColor", -0x11111112])
                        }
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
                    if (showLabels) {
                        view.findViewById<TextView>(R.id.icontxt).text = item.label
                        view.findViewById<TextView>(R.id.icontxt).setTextColor(Settings["dockLabelColor", -0x11111112])
                    }
                    if (notifBadgesEnabled && item.notificationCount != 0) {
                        val badge = view.findViewById<TextView>(R.id.notificationBadge)
                        badge.visibility = View.VISIBLE
                        badge.text = item.notificationCount.toString()
                        Palette.from(item.icon!!.toBitmap()).generate {
                            val color = it?.getDominantColor(0xff111213.toInt()) ?: 0xff111213.toInt()
                            badge.background = ColorTools.iconBadge(color)
                            badge.setTextColor(if (ColorTools.useDarkText(color)) 0xff111213.toInt() else 0xffffffff.toInt())
                        }
                    } else { view.findViewById<TextView>(R.id.notificationBadge).visibility = View.GONE }
                    img.setImageDrawable(item.icon)
                    view.setOnClickListener { item.open(this@Main, it) }
                    view.setOnLongClickListener(ItemLongPress.dock(this@Main, item, i))
                }
                container.addView(view)
                i++
            }
            while (i < columnCount * rowCount) {
                val view = LayoutInflater.from(applicationContext).inflate(R.layout.drawer_item, null)
                view.findViewById<View>(R.id.iconFrame).run {
                    layoutParams.height = appSize
                    layoutParams.width = appSize
                }
                if (!showLabels) view.findViewById<View>(R.id.icontxt).visibility = GONE
                container.addView(view)
                i++
            }
            val containerHeight = (appSize + if (Settings["dockLabelsEnabled", false]) 18.sp else 0f).toInt() * rowCount
            dockHeight = if (Settings["docksearchbarenabled", false] && !isTablet) containerHeight + 84.dp.toInt() else containerHeight + 14.dp.toInt()
            container.layoutParams.height = containerHeight
            behavior.peekHeight = (dockHeight + Tools.navbarHeight + Settings["dockbottompadding", 10].dp).toInt()
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getRealMetrics(metrics)
            findViewById<View>(R.id.drawercontent).layoutParams.height = metrics.heightPixels
            (findViewById<View>(R.id.homeView).layoutParams as FrameLayout.LayoutParams).topMargin = -dockHeight
            if (Settings["feed:show_behind_dock", false]) {
                (desktop.layoutParams as CoordinatorLayout.LayoutParams).setMargins(0, dockHeight, 0, 0)
                findViewById<View>(R.id.desktopContent).setPadding(0, 0, 0, (dockHeight + Tools.navbarHeight + Settings["dockbottompadding", 10].dp).toInt())
            } else {
                (desktop.layoutParams as CoordinatorLayout.LayoutParams).setMargins(0, dockHeight, 0, dockHeight + Tools.navbarHeight + (Settings["dockbottompadding", 10] - 18).dp.toInt())
                findViewById<View>(R.id.desktopContent).setPadding(0, 6.dp.toInt(), 0, 24.dp.toInt())
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
                        val objs = event.localState as Array<*>
                        val view = objs[1] as View
                        val location = IntArray(2)
                        view.getLocationOnScreen(location)
                        println("loc: ${location[0]}, ${location[1]}")
                        println("drag: ${event.x}, ${event.y}")
                        val x = abs(event.x - location[0] - view.measuredWidth / 2f)
                        val y = abs(event.y - location[1] - view.measuredHeight / 2f)
                        if (x > view.width / 3.5f || y > view.height / 3.5f) {
                            (objs[2] as PopupWindow).dismiss()
                            behavior.state = BottomDrawerBehavior.STATE_COLLAPSED
                        }
                    }
                    DragEvent.ACTION_DRAG_STARTED -> {
                        ((event.localState as Array<*>)[1] as View).visibility = INVISIBLE
                    }
                    DragEvent.ACTION_DRAG_ENDED -> {
                        val objs = event.localState as Array<*>
                        (objs[1] as View).visibility = VISIBLE
                        (objs[2] as PopupWindow).isFocusable = true
                        (objs[2] as PopupWindow).update()
                    }
                    DragEvent.ACTION_DROP -> {
                        ((event.localState as Array<*>)[1] as View).visibility = VISIBLE
                        if (behavior.state != BottomDrawerBehavior.STATE_EXPANDED) {
                            if (event.y > Device.displayHeight - dockHeight) {
                                val item = (event.localState as Array<*>)[0] as LauncherItem
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
            applyFontSetting()

            when (Settings["dock:background_type", 0]) {
                0 -> { drawer.background = ShapeDrawable().apply {
                    val tr = Settings["dockradius", 30].dp
                    shape = RoundRectShape(floatArrayOf(tr, tr, tr, tr, 0f, 0f, 0f, 0f), null, null)
                    paint.color = Settings["dock:background_color", -0x78000000]
                }}
                1 -> { drawer.background = LayerDrawable(arrayOf(
                    GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(
                        Settings["dock:background_color", -0x78000000] and 0x00ffffff,
                        Settings["dock:background_color", -0x78000000])),
                    GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(
                        Settings["dock:background_color", -0x78000000],
                        Settings["drawer:background_color", -0x78000000]))
                ))}
            }

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

            if (Settings["hidestatus", false]) window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) else window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

            setDrawerSearchBarVisible(Settings["drawersearchbarenabled", true])
            setDrawerSearchbarBGColor(Settings["searchcolor", 0x33000000])
            setDrawerSearchbarFGColor(Settings["searchtxtcolor", -0x1])
            setDrawerSearchbarRadius(Settings["searchradius", 0])

            setSearchHintText(Settings["searchhinttxt", "Search.."])

            setDockSearchBarVisible(Settings["docksearchbarenabled", false])
            setDockSearchbarBelowApps(Settings["dock:search:below_apps", false])
            setDockSearchbarBGColor(Settings["docksearchcolor", -0x22000001])
            setDockSearchbarFGColor(Settings["docksearchtxtcolor", -0x1000000])
            setDockSearchbarRadius(Settings["dock:search:radius", 30])

            if (Settings["drawer:sections_enabled", false]) {
                drawerGrid.numColumns = 1
                drawerGrid.verticalSpacing = 0
            } else {
                drawerGrid.numColumns = Settings["drawer:columns", 4]
                drawerGrid.verticalSpacing = Settings["verticalspacing", 12].dp.toInt()
            }
            val marginX = Settings["feed:card_margin_x", 16].dp.toInt()
            if (Settings["feed:enabled", true]) {
                feedRecycler.visibility = VISIBLE
                (feedRecycler.layoutParams as LinearLayout.LayoutParams).setMargins(marginX, 0, marginX, 0)
            } else {
                feedRecycler.visibility = GONE
            }
            (findViewById<View>(R.id.musicCard).layoutParams as LinearLayout.LayoutParams).leftMargin = marginX
            (findViewById<View>(R.id.musicCard).layoutParams as LinearLayout.LayoutParams).rightMargin = marginX
            if (Settings["hidefeed", false]) {
                feedRecycler.translationX = Device.displayWidth.toFloat()
                feedRecycler.alpha = 0f
                var wasHiddenLastTime = true
                val fadeOutAnimListener = object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationStart(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        feedRecycler.translationX = findViewById<View>(R.id.homeView).width.toFloat()
                        wasHiddenLastTime = true
                    }
                }
                val fadeInAnimListener = object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationStart(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        wasHiddenLastTime = false
                    }
                }
                desktop.setOnScrollChangeListener(androidx.core.widget.NestedScrollView.OnScrollChangeListener { _, _, y, _, oldY ->
                    val a = 6.dp
                    val distance = oldY - y
                    if (y > a) {
                        if (wasHiddenLastTime) {
                            feedRecycler.translationX = 0f
                            feedRecycler.animate().setListener(fadeInAnimListener).alpha(1f).setInterpolator { it.pow(3 / (it + 8)) }.duration = 200L
                        }
                        if (distance > a || y >= findViewById<View>(R.id.desktopContent).height - dockHeight - desktop.height) {
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
                            if (!wasHiddenLastTime) {
                                feedRecycler.animate().setListener(fadeOutAnimListener).alpha(0f).setInterpolator { it.pow((it + 8) / 3) }.duration = 180L
                            }
                        }
                    }
                })
            } else {
                feedRecycler.translationX = 0f
                feedRecycler.alpha = 1f
                desktop.setOnScrollChangeListener(androidx.core.widget.NestedScrollView.OnScrollChangeListener { _, _, y, _, oldY ->
                    val a = 6.dp
                    val distance = oldY - y
                    if (distance > a || y < a || y + desktop.height >= findViewById<View>(R.id.desktopContent).height - dockHeight) {
                        if (!LauncherMenu.isActive) {
                            behavior.state = BottomDrawerBehavior.STATE_COLLAPSED
                        }
                    } else if (distance < -a) {
                        behavior.state = BottomDrawerBehavior.STATE_HIDDEN
                    }
                })
            }
            if (!Settings["hidestatus", false]) desktop.setPadding(0, getStatusBarHeight() - 12.dp.toInt(), 0, 0)

            shouldSetApps = false
            customized = false

            if (Settings["notif:enabled", true]) {
                val parentNotificationTitle = findViewById<TextView>(R.id.parentNotificationTitle)
                NotificationService.onUpdate = {
                    try {
                        runOnUiThread {
                            if (Settings["collapseNotifications", false]) {
                                if (NotificationService.notificationsAmount > 1) {
                                    findViewById<View>(R.id.parentNotification).visibility = VISIBLE
                                    parentNotificationTitle.text = resources.getString(
                                            R.string.num_notifications,
                                            NotificationService.notificationsAmount
                                    )
                                    if (notifications.visibility == VISIBLE) {
                                        findViewById<View>(R.id.parentNotification).background.alpha = 127
                                        findViewById<View>(R.id.arrowUp).visibility = VISIBLE
                                    } else {
                                        findViewById<View>(R.id.parentNotification).background.alpha = 255
                                        findViewById<View>(R.id.arrowUp).visibility = GONE
                                    }
                                } else {
                                    findViewById<View>(R.id.parentNotification).visibility = GONE
                                    notifications.visibility = VISIBLE
                                }
                            }
                            notifications.adapter = NotificationAdapter(this@Main)
                            if (Settings["notif:badges", true]) {
                                drawerGrid.invalidateViews()
                                setDock()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                try { startService(Intent(this, NotificationService::class.java)) }
                catch (e: Exception) {}
                (findViewById<View>(R.id.parentNotification).layoutParams as LinearLayout.LayoutParams).leftMargin = marginX
                (findViewById<View>(R.id.parentNotification).layoutParams as LinearLayout.LayoutParams).rightMargin = marginX
                val notificationBackground = ShapeDrawable()
                val r = Settings["feed:card_radius", 15].dp
                notificationBackground.shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
                notificationBackground.paint.color = Settings["notificationbgcolor", -0x1]
                findViewById<View>(R.id.parentNotification).background = notificationBackground
                parentNotificationTitle.setTextColor(Settings["notificationtitlecolor", -0xeeeded])
                parentNotificationTitle.typeface = mainFont
                val parentNotificationBtn = findViewById<ImageView>(R.id.parentNotificationBtn)
                parentNotificationBtn.imageTintList = ColorStateList.valueOf(if (ColorTools.useDarkText(accentColor)) -0x1000000 else -0x1)
                parentNotificationBtn.backgroundTintList = ColorStateList.valueOf(accentColor)
                parentNotificationBtn.imageTintList = ColorStateList.valueOf(accentColor)
                parentNotificationBtn.backgroundTintList = ColorStateList.valueOf(accentColor and 0x00ffffff or 0x33000000)
                if (Settings["collapseNotifications", false] && NotificationService.notificationsAmount > 1) {
                    notifications.visibility = GONE
                    findViewById<View>(R.id.arrowUp).visibility = GONE
                    findViewById<View>(R.id.parentNotification).visibility = VISIBLE
                    findViewById<View>(R.id.parentNotification).background.alpha = 255
                } else {
                    notifications.visibility = VISIBLE
                    findViewById<View>(R.id.parentNotification).visibility = GONE
                }
            } else {
                notifications.visibility = GONE
                findViewById<View>(R.id.parentNotification).visibility = GONE
            }
            setDrawerScrollbarEnabled(Settings["drawer:scrollbar_enabled", false])
        }
    }

    private lateinit var drawerGrid: GridView
    private lateinit var drawer: View
    private lateinit var drawerScrollBar: AlphabetScrollbar
    lateinit var behavior: BottomDrawerBehavior<View>
    private var dockHeight = 0

    private lateinit var blurBg: LayerDrawable

    private lateinit var searchBar: View

    private lateinit var desktop: NestedScrollView
    private lateinit var feedRecycler: RecyclerView

    private lateinit var notifications: RecyclerView
    private lateinit var widgetLayout: ResizableLayout

    private lateinit var batteryBar: ProgressBar

    private lateinit var powerManager: PowerManager

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings.init(this)
        if (Settings["init", true]) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
            exitProcess(0)
        }
        if (Settings["dock:isOld", true]) {
            Dock.convert()
            Settings["dock:isOld"] = false
        }

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            Log.e("posidonLauncher", "uncaught exception", throwable)
            Settings.apply()
            startActivity(Intent(this, StackTraceActivity::class.java).apply { putExtra("throwable", throwable) })
            Process.killProcess(Process.myPid())
            exitProcess(0)
        }

        Widget.init()
        accentColor = Settings["accent", 0x1155ff] or -0x1000000
        setContentView(R.layout.main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) launcherApps = getSystemService(LauncherApps::class.java)
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

        /*val filter = IntentFilter()
        filter.addAction(Intent.ACTION_PACKAGE_ADDED)
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED)
        filter.addDataScheme("package")
        *///receiver = AppChangeReceiver()
        //registerReceiver(receiver, filter)

        launcherApps.registerCallback(object : LauncherApps.Callback() {
            override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle?, replacing: Boolean) {
                AppLoader(this@Main, onAppLoaderEnd).execute()
            }

            override fun onPackageChanged(packageName: String, user: UserHandle?) {
                AppLoader(this@Main, onAppLoaderEnd).execute()
            }

            override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle?, replacing: Boolean) {
                AppLoader(this@Main, onAppLoaderEnd).execute()
            }

            override fun onPackageAdded(packageName: String, user: UserHandle?) {
                AppLoader(this@Main, onAppLoaderEnd).execute()
            }

            override fun onPackageRemoved(packageName: String, user: UserHandle?) {
                apps.removeAll { it.packageName == packageName }
                for (section in appSections) {
                    section.removeAll { it.packageName == packageName }
                }
                App.removePackage(packageName)
                onAppLoaderEnd()
            }
        })

        if (Settings["search:asHome", false]) {
            startActivity(Intent(this, SearchActivity::class.java))
            AppLoader(this@Main, onAppLoaderEnd).execute()
            finish()
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
        }
        updateNavbarHeight(this@Main)
        drawerGrid = findViewById(R.id.drawergrid)
        searchBar = findViewById(R.id.searchbar)
        drawerGrid.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && drawerGrid.canScrollVertically(-1))
                drawerGrid.requestDisallowInterceptTouchEvent(true)
            false
        }
        drawer = findViewById(R.id.drawer)
        behavior = BottomDrawerBehavior.from<View>(findViewById(R.id.drawer)).apply {
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
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                val list = ArrayList<Rect>()
                                window.decorView.findViewById<View>(android.R.id.content).systemGestureExclusionRects = list
                            }
                        }
                    }
                    colors[2] = Settings["drawer:background_color", -0x78000000]
                    floats[0] = dockHeight.toFloat() / (Device.displayHeight + dockHeight)
                    things[0] = if (canBlurWall(this@Main)) Settings["blurLayers", 1] else 0
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
                            if (bg is ShapeDrawable) {
                                bg.paint.color = ColorTools.blendColors(colors[2], things[1], slideOffset)
                                bg.shape = RoundRectShape(radii, null, null)
                            } else if (bg is LayerDrawable) {
                                colors[1] = ColorTools.blendColors(colors[2], things[1], slideOffset)
                                (bg.getDrawable(0) as GradientDrawable).colors = intArrayOf(colors[0], colors[1])
                                (bg.getDrawable(1) as GradientDrawable).colors = intArrayOf(colors[1], colors[2])
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

        feedRecycler = findViewById<RecyclerView>(R.id.feedrecycler).apply {
            layoutManager = LinearLayoutManager(this@Main)
            isNestedScrollingEnabled = false
        }

        notifications = findViewById<RecyclerView>(R.id.notifications).apply {
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(this@Main)
        }

        findViewById<View>(R.id.parentNotification).apply {
            setOnLongClickListener(LauncherMenu())
            setOnClickListener {
                if (notifications.visibility == VISIBLE) {
                    desktop.scrollTo(0, 0)
                    notifications.visibility = GONE
                    it.background.alpha = 255
                    it.findViewById<View>(R.id.arrowUp).visibility = GONE
                } else {
                    notifications.visibility = VISIBLE
                    it.background.alpha = 127
                    it.findViewById<View>(R.id.arrowUp).visibility = VISIBLE
                }
            }
        }

        setCustomizations()

        widgetLayout = findViewById<ResizableLayout>(R.id.widgets).apply {
            layoutParams.height = Settings["widgetHeight", ViewGroup.LayoutParams.WRAP_CONTENT]
            layoutParams = layoutParams
            onResizeListener = object : OnResizeListener {
                override fun onStop(newHeight: Int) { Settings["widgetHeight"] = newHeight }
                override fun onCrossPress() = Widget.deleteWidget(widgetLayout)
                override fun onMajorUpdate(newHeight: Int) = Widget.resize(newHeight)

                override fun onUpdate(newHeight: Int) {
                    layoutParams.height = newHeight
                    layoutParams = layoutParams
                }
            }
        }
        Widget.host.startListening()
        Widget.fromSettings(widgetLayout)
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

        findViewById<View>(R.id.musicPrev).setOnClickListener {
            (getSystemService(Context.AUDIO_SERVICE) as AudioManager).apply {
                dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS))
                dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS))
                findViewById<ImageView>(R.id.musicPlay).setImageResource(R.drawable.ic_pause)
            }
        }
        findViewById<View>(R.id.musicPlay).setOnClickListener {
            it as ImageView
            (getSystemService(Context.AUDIO_SERVICE) as AudioManager).apply {
                if (isMusicActive) {
                    dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE))
                    dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE))
                    it.setImageResource(R.drawable.ic_play)
                } else {
                    dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY))
                    dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY))
                    it.setImageResource(R.drawable.ic_pause)
                }
            }
        }
        findViewById<View>(R.id.musicNext).setOnClickListener {
            (getSystemService(Context.AUDIO_SERVICE) as AudioManager).apply {
                dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT))
                dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT))
                findViewById<ImageView>(R.id.musicPlay).setImageResource(R.drawable.ic_pause)
            }
        }
        System.gc()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_APPWIDGET) {
                val extras = data!!.extras
                if (extras != null) {
                    val id = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                    val widgetInfo = AppWidgetManager.getInstance(this).getAppWidgetInfo(id)
                    if (widgetInfo.configure != null) {
                        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                        intent.component = widgetInfo.configure
                        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                        startActivityForResult(intent, REQUEST_CREATE_APPWIDGET)
                    } else Widget.fromIntent(widgetLayout, data)
                }
            } else if (requestCode == REQUEST_CREATE_APPWIDGET) Widget.fromIntent(widgetLayout, data)
        } else if (resultCode == Activity.RESULT_CANCELED && data != null) {
            val appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            if (appWidgetId != -1) Widget.host.deleteAppWidgetId(appWidgetId)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private val batteryInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            batteryBar.progress = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        }
    }

    private val onAppLoaderEnd = {
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
        setDock()
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        onUpdate()
        setDock()
    }

    override fun onResume() {
        if (Settings["search:asHome", false]) {
            startActivity(Intent(this, SearchActivity::class.java))
            finish()
            return
        }
        super.onResume()
        Widget.host.startListening()
        overridePendingTransition(R.anim.home_enter, R.anim.appexit)
        //setWallpaperOffset(0.5f, 0.5f)
        onUpdate()
    }

    private fun onUpdate() {
        val tmp = Tools.navbarHeight
        updateNavbarHeight(this)
        if (Settings["feed:enabled", true]) FeedLoader(object : FeedLoader.Listener {
            override fun onFinished(feedModels: ArrayList<FeedItem>) {
                feedRecycler.adapter = FeedAdapter(feedModels, this@Main)
            }
        }).execute()
        if (Settings["notif:enabled", true]) {
            NotificationService.onUpdate()
        }
        if (canBlurWall(this)) {
            val shouldHide = behavior.state == BottomDrawerBehavior.STATE_COLLAPSED || behavior.state == BottomDrawerBehavior.STATE_HIDDEN
            thread(isDaemon = true) {
                val blurLayers = Settings["blurLayers", 1]
                val radius = Settings["blurradius", 15f]
                for (i in 0 until blurLayers) {
                    val bmp = blurredWall(this@Main, radius / blurLayers * (i + 1))
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
        if (customized || tmp != Tools.navbarHeight) {
            setCustomizations()
            if (Settings["notif:enabled", true]) {
                try {
                    notifications.recycledViewPool.clear()
                    notifications.adapter!!.notifyDataSetChanged()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else if (!powerManager.isPowerSaveMode && Settings["animatedicons", true]) {
            for (app in apps) {
                animate(app.icon!!)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (LauncherMenu.isActive) {
            LauncherMenu.dialog!!.dismiss()
        }
        behavior.state = BottomDrawerBehavior.STATE_COLLAPSED
        desktop.scrollTo(0, 0)
        Widget.host.stopListening()
        if (Settings["notif:enabled", true] && Settings["collapseNotifications", false] && NotificationService.notificationsAmount > 1) {
            notifications.visibility = GONE
            findViewById<View>(R.id.arrowUp).visibility = GONE
            findViewById<View>(R.id.parentNotification).background.alpha = 255
        }
    }

    override fun onStop() {
        super.onStop()
        Widget.host.stopListening()
    }

    override fun onStart() {
        super.onStart()
        Widget.host.startListening()

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
        //try { unregisterReceiver(receiver) }
        //catch (e: Exception) {}
        unregisterReceiver(batteryInfoReceiver)
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            if (Settings["notif:enabled", true]) {
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
            val playBtn = findViewById<ImageView>(R.id.musicPlay)
            if ((getSystemService(Context.AUDIO_SERVICE) as AudioManager).isMusicActive) {
                playBtn.setImageResource(R.drawable.ic_pause)
            } else {
                playBtn.setImageResource(R.drawable.ic_play)
            }
        } else window.decorView.systemUiVisibility =
                SYSTEM_UI_FLAG_LAYOUT_STABLE or
                SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    inner class AppChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            AppLoader(context, onAppLoaderEnd).execute()
        }
    }

    override fun onBackPressed() {
        when {
            behavior.state == BottomDrawerBehavior.STATE_EXPANDED -> behavior.state = BottomDrawerBehavior.STATE_COLLAPSED
            widgetLayout.resizing -> widgetLayout.resizing = false
            else -> Gestures.performTrigger(Settings["gesture:back", ""])
        }
    }

    fun openSearch(v: View?) = startActivity(
        Intent(this, if (Settings["dev:console", false]) ConsoleActivity::class.java else SearchActivity::class.java),
        ActivityOptions.makeCustomAnimation(this, R.anim.fadein, R.anim.fadeout).toBundle())

    companion object {
        var appSections = ArrayList<ArrayList<App>>()
        var shouldSetApps = true
        var customized = false
        var apps = ArrayList<App>()
        var accentColor = -0xeeaa01
        //var receiver: AppChangeReceiver? = null
        lateinit var instance: Main private set
        lateinit var setCustomizations: () -> Unit private set
        lateinit var setDock: () -> Unit private set
        @RequiresApi(api = Build.VERSION_CODES.M)
        lateinit var launcherApps: LauncherApps

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
            instance.findViewById<ImageView>(R.id.searchIcon).imageTintList = ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(Settings["searchhintcolor", -0x1]))
            instance.findViewById<ImageView>(R.id.searchIcon).imageTintMode = PorterDuff.Mode.MULTIPLY
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
            instance.findViewById<ImageView>(R.id.docksearchic).imageTintList = ColorStateList.valueOf(color)
            instance.findViewById<ImageView>(R.id.docksearchic).imageTintMode = PorterDuff.Mode.MULTIPLY
            instance.findViewById<ProgressBar>(R.id.battery).progressTintList = ColorStateList.valueOf(color)
            instance.findViewById<ProgressBar>(R.id.battery).indeterminateTintMode = PorterDuff.Mode.MULTIPLY
            instance.findViewById<ProgressBar>(R.id.battery).progressBackgroundTintList = ColorStateList.valueOf(color)
            instance.findViewById<ProgressBar>(R.id.battery).progressBackgroundTintMode = PorterDuff.Mode.MULTIPLY
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
                instance.findViewById<View>(R.id.dockContainer).bringToFront()
            }
        }

        fun setDrawerScrollbarEnabled(enabled: Boolean) {
            Settings["drawer:scrollbar_enabled"] = enabled
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
    }
}