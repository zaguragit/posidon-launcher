package posidon.launcher

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.app.WallpaperManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.*
import android.content.pm.LauncherApps
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.*
import android.graphics.drawable.shapes.RoundRectShape
import android.os.*
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.*
import android.view.View.*
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import posidon.launcher.LauncherMenu.PinchListener
import posidon.launcher.feed.news.FeedAdapter
import posidon.launcher.feed.news.FeedItem
import posidon.launcher.feed.news.FeedLoader
import posidon.launcher.feed.notifications.NotificationAdapter
import posidon.launcher.feed.notifications.NotificationService
import posidon.launcher.feed.notifications.SwipeToDeleteCallback
import posidon.launcher.items.*
import posidon.launcher.items.App.Companion.clearSecondMap
import posidon.launcher.items.App.Companion.get
import posidon.launcher.items.App.Companion.putInSecondMap
import posidon.launcher.items.App.Companion.swapMaps
import posidon.launcher.items.ItemLongPress.dock
import posidon.launcher.items.ItemLongPress.drawer
import posidon.launcher.items.ItemLongPress.folder
import posidon.launcher.items.ItemLongPress.insideFolder
import posidon.launcher.search.SearchActivity
import posidon.launcher.tools.ColorTools
import posidon.launcher.tools.Settings
import posidon.launcher.tools.Sort.colorSort
import posidon.launcher.tools.Sort.labelSort
import posidon.launcher.tools.ThemeTools
import posidon.launcher.tools.Tools
import posidon.launcher.tools.Tools.adaptic
import posidon.launcher.tools.Tools.animate
import posidon.launcher.tools.Tools.applyFontSetting
import posidon.launcher.tools.Tools.blurredWall
import posidon.launcher.tools.Tools.canBlurWall
import posidon.launcher.tools.Tools.getDisplayHeight
import posidon.launcher.tools.Tools.getDisplayWidth
import posidon.launcher.tools.Tools.getResizedBitmap
import posidon.launcher.tools.Tools.getResizedMatrix
import posidon.launcher.tools.Tools.getStatusBarHeight
import posidon.launcher.tools.Tools.isInstalled
import posidon.launcher.tools.Tools.isTablet
import posidon.launcher.tools.Tools.updateNavbarHeight
import posidon.launcher.tutorial.WelcomeActivity
import posidon.launcher.view.ResizableLayout
import posidon.launcher.view.ResizableLayout.OnResizeListener
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.abs
import kotlin.math.min

class Main : AppCompatActivity() {
    private lateinit var drawerGrid: GridView
    private lateinit var searchBar: View
    private var powerManager: PowerManager? = null
    private lateinit var desktop: NestedScrollView
    private lateinit var feedRecycler: RecyclerView
    private lateinit var notifications: RecyclerView
    private lateinit var blurBg: LayerDrawable
    private lateinit var widgetManager: AppWidgetManager
    private lateinit var widgetHost: AppWidgetHost
    private lateinit var hostView: AppWidgetHostView
    private lateinit var widgetLayout: ResizableLayout
    private var dockHeight = 0
    private lateinit var behavior: BottomSheetBehavior<*>
    private lateinit var batteryBar: ProgressBar
    private val batteryInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            batteryBar.progress = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings.init(this)
        if (Settings["init", true]) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }
        accentColor = Settings["accent", 0x1155ff] or -0x1000000
        setContentView(R.layout.main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) launcherApps = getSystemService(LauncherApps::class.java)

        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_PACKAGE_ADDED)
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED)
        filter.addDataScheme("package")
        receiver = AppChangeReceiver()
        registerReceiver(receiver, filter)

        batteryBar = findViewById(R.id.battery)
        registerReceiver(batteryInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        methods = object : Methods {
            override fun setDock() {
                val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(findViewById<View>(R.id.drawer))
                var appSize = 0
                when (Settings["dockicsize", 1]) {
                    0 -> appSize = (resources.displayMetrics.density * 64).toInt()
                    1 -> appSize = (resources.displayMetrics.density * 74).toInt()
                    2 -> appSize = (resources.displayMetrics.density * 84).toInt()
                }
                val data = Settings["dock", ""].split("\n").toTypedArray()
                val container = findViewById<GridLayout>(R.id.dockContainer)
                container.removeAllViews()
                val columnCount = Settings["dock:columns", 5]
                val rowCount = Settings["dock:rows", 1]
                val showLabels = Settings["dockLabelsEnabled", false]
                container.columnCount = columnCount
                container.rowCount = rowCount
                appSize = min(appSize, ((getDisplayWidth(this@Main) - 32 * resources.displayMetrics.density) / columnCount).toInt())
                var i = 0
                while (i < data.size && i < columnCount * rowCount) {
                    val string = data[i]
                    val view = LayoutInflater.from(applicationContext).inflate(R.layout.drawer_item, null)
                    val img = view.findViewById<ImageView>(R.id.iconimg)
                    img.layoutParams.height = appSize
                    img.layoutParams.width = appSize
                    if (data[i].startsWith("folder(") && data[i].endsWith(")")) {
                        val folder = Folder(this@Main, data[i])
                        img.setImageDrawable(folder.icon)
                        if (showLabels) {
                            (view.findViewById<View>(R.id.icontxt) as TextView).text = folder.label
                            (view.findViewById<View>(R.id.icontxt) as TextView).setTextColor(Settings["dockLabelColor", -0x11111112])
                        } else view.findViewById<View>(R.id.icontxt).visibility = GONE
                        val finalI = i
                        val finalAppSize = appSize
                        val bgColor = Settings["folderBG", -0x22eeeded]
                        val r = Settings["folderCornerRadius", 18] * resources.displayMetrics.density
                        val labelsEnabled = Settings["folderLabelsEnabled", false]
                        view.setOnClickListener {
                            val content = LayoutInflater.from(this@Main).inflate(R.layout.folder_layout, null)
                            val popupWindow = PopupWindow(content, ListPopupWindow.WRAP_CONTENT, ListPopupWindow.WRAP_CONTENT, true)
                            popupWindow.setBackgroundDrawable(ColorDrawable(0x0))
                            val container = content.findViewById<GridLayout>(R.id.container)
                            container.columnCount = Settings["folderColumns", 3]
                            val appList: List<App?> = folder.apps
                            var i1 = 0
                            val appListSize = appList.size
                            while (i1 < appListSize) {
                                val app = appList[i1]
                                if (app == null) {
                                    folder.apps.removeAt(i1)
                                    data[finalI] = folder.toString()
                                    Settings["dock"] = TextUtils.join("\n", data)
                                } else {
                                    val appIcon = LayoutInflater.from(applicationContext).inflate(R.layout.drawer_item, null)
                                    val icon = appIcon.findViewById<ImageView>(R.id.iconimg)
                                    icon.layoutParams.height = finalAppSize
                                    icon.layoutParams.width = finalAppSize
                                    icon.setImageDrawable(app.icon)
                                    if (labelsEnabled) {
                                        val iconTxt = appIcon.findViewById<TextView>(R.id.icontxt)
                                        iconTxt.text = app.label
                                        iconTxt.setTextColor(Settings["folder:label_color", -0x22000001])
                                    } else appIcon.findViewById<View>(R.id.icontxt).visibility = GONE
                                    appIcon.setOnClickListener { view ->
                                        app.open(this@Main, view)
                                        popupWindow.dismiss()
                                    }
                                    appIcon.setOnLongClickListener(insideFolder(this@Main, app, finalI, view, i1, popupWindow))
                                    container.addView(appIcon)
                                }
                                i1++
                            }
                            val bg = ShapeDrawable()
                            bg.shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
                            bg.paint.color = bgColor
                            content.findViewById<View>(R.id.bg).background = bg
                            val location = IntArray(2)
                            view.getLocationInWindow(location)
                            val gravity = if (location[0] > getDisplayWidth(this@Main) / 2) Gravity.END else Gravity.START
                            val x = if (location[0] > getDisplayWidth(this@Main) / 2) getDisplayWidth(this@Main) - location[0] - view.measuredWidth else location[0]
                            popupWindow.showAtLocation(
                                    view, Gravity.BOTTOM or gravity, x,
                                    (-view.y + view.height * Settings["dock:rows", 1] + Tools.navbarHeight + (Settings["dockbottompadding", 10] + 12) * resources.displayMetrics.density).toInt()
                            )
                        }
                        view.setOnLongClickListener(folder(this@Main, folder, i))
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && data[i].startsWith("shortcut:")) {
                        val shortcut = Shortcut(string)
                        if (!showLabels) view.findViewById<View>(R.id.icontxt).visibility = GONE
                        if (isInstalled(shortcut.packageName, packageManager)) {
                            if (showLabels) {
                                (view.findViewById<View>(R.id.icontxt) as TextView).text = shortcut.label
                                (view.findViewById<View>(R.id.icontxt) as TextView).setTextColor(Settings["dockLabelColor", -0x11111112])
                            }
                            img.setImageDrawable(shortcut.icon)
                            view.setOnClickListener { view -> shortcut.open(this@Main, view) }
                            //view.setOnLongClickListener(ItemLongPress.dock(Main.this, app, i));
                        } else {
                            data[i] = ""
                            Settings["dock"] = TextUtils.join("\n", data)
                        }
                    } else {
                        val app = get(string)
                        if (!showLabels) view.findViewById<View>(R.id.icontxt).visibility = GONE
                        if (app == null) {
                            data[i] = ""
                            Settings["dock"] = TextUtils.join("\n", data)
                        } else {
                            if (showLabels) {
                                (view.findViewById<View>(R.id.icontxt) as TextView).text = app.label
                                (view.findViewById<View>(R.id.icontxt) as TextView).setTextColor(Settings["dockLabelColor", -0x11111112])
                            }
                            img.setImageDrawable(app.icon)
                            view.setOnClickListener { view -> app.open(this@Main, view) }
                            view.setOnLongClickListener(dock(this@Main, app, i))
                        }
                    }
                    container.addView(view)
                    i++
                }
                while (i < columnCount * rowCount) {
                    val view = LayoutInflater.from(applicationContext).inflate(R.layout.drawer_item, null)
                    val img = view.findViewById<ImageView>(R.id.iconimg)
                    img.layoutParams.height = appSize
                    img.layoutParams.width = appSize
                    if (!showLabels) view.findViewById<View>(R.id.icontxt).visibility = GONE
                    container.addView(view)
                    i++
                }
                val containerHeight = (appSize * rowCount + resources.displayMetrics.density * if (Settings["dockLabelsEnabled", false]) 18 * rowCount else 0).toInt()
                dockHeight = if (Settings["docksearchbarenabled", false] && !isTablet(this@Main)) (containerHeight + resources.displayMetrics.density * 84).toInt() else (containerHeight + resources.displayMetrics.density * 14).toInt()
                container.layoutParams.height = containerHeight
                behavior.peekHeight = (dockHeight + Tools.navbarHeight + Settings["dockbottompadding", 10] * resources.displayMetrics.density).toInt()
                val metrics = DisplayMetrics()
                windowManager.defaultDisplay.getRealMetrics(metrics)
                findViewById<View>(R.id.drawercontent).layoutParams.height = metrics.heightPixels
                (findViewById<View>(R.id.homeView).layoutParams as FrameLayout.LayoutParams).topMargin = -dockHeight
                if (Settings["feed:show_behind_dock", false]) {
                    (desktop.layoutParams as CoordinatorLayout.LayoutParams).setMargins(0, dockHeight, 0, 0)
                    findViewById<View>(R.id.desktopContent).setPadding(0, 0, 0, (dockHeight + Tools.navbarHeight + Settings["dockbottompadding", 10] * resources.displayMetrics.density).toInt())
                } else {
                    (desktop.layoutParams as CoordinatorLayout.LayoutParams).setMargins(0, dockHeight, 0, (dockHeight + Tools.navbarHeight + (Settings["dockbottompadding", 10] - 18) * resources.displayMetrics.density).toInt())
                    findViewById<View>(R.id.desktopContent).setPadding(0, (6 * resources.displayMetrics.density).toInt(), 0, (24 * resources.displayMetrics.density).toInt())
                }
                //desktop.setPadding(0, dockHeight, 0, (int) (dockHeight + Tools.navbarHeight + (Settings.getInt("dockbottompadding", 10) - 18) * getResources().getDisplayMetrics().density));
                (findViewById<View>(R.id.blur).layoutParams as CoordinatorLayout.LayoutParams).topMargin = dockHeight
                window.decorView.findViewById<View>(android.R.id.content).setOnDragListener(object : OnDragListener {
                    override fun onDrag(v: View, event: DragEvent): Boolean {
                        when (event.action) {
                            DragEvent.ACTION_DRAG_LOCATION -> {
                                val objs = event.localState as Array<*>
                                val icon = objs[1] as View
                                val location = IntArray(2)
                                icon.getLocationOnScreen(location)
                                val x = abs(event.x - location[0] - icon.width / 2f)
                                val y = abs(event.y - location[1] - icon.height / 2f)
                                if (x > icon.width / 2f || y > icon.height / 2f) {
                                    (objs[2] as PopupWindow).dismiss()
                                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
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
                                if (behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                                    if (event.y > getDisplayHeight(this@Main) - dockHeight) {
                                        val item = (event.localState as Array<*>)[0] as LauncherItem?
                                        if (item is App) {
                                            val location = IntArray(2)
                                            run {
                                                var i = 0
                                                while (i < container.childCount) {
                                                    container.getChildAt(i).getLocationOnScreen(location)
                                                    val threshHold = min(container.getChildAt(i).height / 2.toFloat(), 100 * resources.displayMetrics.density)
                                                    if (abs(location[0] - (event.x - container.getChildAt(i).height / 2f)) < threshHold && abs(location[1] - (event.y - container.getChildAt(i).height / 2f)) < threshHold) {
                                                        var data: Array<String?> = Settings["dock", ""].split("\n").toTypedArray()
                                                        if (data.size <= i) data = data.copyOf(i + 1)
                                                        if (data[i] == null || data[i] == "" || data[i] == "null") {
                                                            data[i] = item.packageName + "/" + item.name
                                                            Settings["dock"] = TextUtils.join("\n", data)
                                                        } else {
                                                            if (data[i]!!.startsWith("folder(") && data[i]!!.endsWith(")")) data[i] = "folder(" + data[i]!!.substring(7, data[i]!!.length - 1) + "¬" + item.packageName + "/" + item.name + ")" else data[i] = "folder(" + "folder¬" + data[i] + "¬" + item.packageName + "/" + item.name + ")"
                                                            Settings["dock"] = TextUtils.join("\n", data)
                                                        }
                                                        break
                                                    }
                                                    i++
                                                }
                                            }
                                        } else if (item is Folder) {
                                            val location = IntArray(2)
                                            run {
                                                var i = 0
                                                while (i < container.childCount) {
                                                    container.getChildAt(i).getLocationOnScreen(location)
                                                    val threshHold = min(container.getChildAt(i).height / 2.toFloat(), 100 * resources.displayMetrics.density)
                                                    if (abs(location[0] - (event.x - container.getChildAt(i).height / 2f)) < threshHold && abs(location[1] - (event.y - container.getChildAt(i).height / 2f)) < threshHold) {
                                                        var data: Array<String?> = Settings["dock", ""].split("\n").toTypedArray()
                                                        if (data.size <= i) data = data.copyOf(i + 1)
                                                        if (data[i] == null || data[i] == "" || data[i] == "null") {
                                                            data[i] = item.toString()
                                                            Settings["dock"] = TextUtils.join("\n", data)
                                                        } else {
                                                            var folderContent = item.toString().substring(7, item.toString().length - 1)
                                                            if (data[i]!!.startsWith("folder(") && data[i]!!.endsWith(")")) {
                                                                folderContent = folderContent.substring(folderContent.indexOf('¬') + 1)
                                                                data[i] = "folder(" + data[i]!!.substring(7, data[i]!!.length - 1) + "¬" + folderContent + ")"
                                                            } else data[i] = "folder(" + folderContent + "¬" + data[i] + ")"
                                                            Settings["dock"] = TextUtils.join("\n", data)
                                                        }
                                                        break
                                                    }
                                                    i++
                                                }
                                            }
                                        }
                                    }
                                    setDock()
                                }
                            }
                        }
                        return true
                    }
                })
            }

            override fun setCustomizations() {
                applyFontSetting(this@Main)
                if (Settings["hidestatus", false]) window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) else window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                if (Settings["drawersearchbarenabled", true]) {
                    drawerGrid.setPadding(0, getStatusBarHeight(this@Main), 0, Tools.navbarHeight + (56 * resources.displayMetrics.density).toInt())
                    searchBar.setPadding(0, 0, 0, Tools.navbarHeight)
                    searchBar.visibility = VISIBLE
                    val bg = ShapeDrawable()
                    val tr = Settings["searchradius", 0] * resources.displayMetrics.density
                    bg.shape = RoundRectShape(floatArrayOf(tr, tr, tr, tr, 0f, 0f, 0f, 0f), null, null)
                    bg.paint.color = Settings["searchcolor", 0x33000000]
                    searchBar.background = bg
                    val t = findViewById<TextView>(R.id.searchTxt)
                    t.setTextColor(Settings["searchhintcolor", -0x1])
                    t.text = Settings["searchhinttxt", "Search.."]
                    (findViewById<View>(R.id.searchIcon) as ImageView).imageTintList = ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(Settings["searchhintcolor", -0x1]))
                } else {
                    searchBar.visibility = GONE
                    drawerGrid.setPadding(0, getStatusBarHeight(this@Main), 0, Tools.navbarHeight + (12 * resources.displayMetrics.density).toInt())
                }
                if (Settings["docksearchbarenabled", false]) {
                    findViewById<View>(R.id.docksearchbar).visibility = VISIBLE
                    findViewById<View>(R.id.battery).visibility = VISIBLE
                    val bg = ShapeDrawable()
                    val tr = Settings["docksearchradius", 30] * resources.displayMetrics.density
                    bg.shape = RoundRectShape(floatArrayOf(tr, tr, tr, tr, tr, tr, tr, tr), null, null)
                    bg.paint.color = Settings["docksearchcolor", -0x22000001]
                    findViewById<View>(R.id.docksearchbar).background = bg
                    val t = findViewById<TextView>(R.id.docksearchtxt)
                    t.setTextColor(Settings["docksearchtxtcolor", -0x1000000])
                    t.text = Settings["searchhinttxt", "Search.."]
                    (findViewById<View>(R.id.docksearchic) as ImageView).imageTintList = ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(Settings["docksearchtxtcolor", -0x1000000]))
                    (findViewById<View>(R.id.docksearchic) as ImageView).imageTintMode = PorterDuff.Mode.MULTIPLY
                    (findViewById<View>(R.id.battery) as ProgressBar).progressTintList = ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(Settings["docksearchtxtcolor", -0x1000000]))
                    (findViewById<View>(R.id.battery) as ProgressBar).indeterminateTintMode = PorterDuff.Mode.MULTIPLY
                    (findViewById<View>(R.id.battery) as ProgressBar).progressBackgroundTintList = ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(Settings["docksearchtxtcolor", -0x1000000]))
                    (findViewById<View>(R.id.battery) as ProgressBar).progressBackgroundTintMode = PorterDuff.Mode.MULTIPLY
                    ((findViewById<View>(R.id.battery) as ProgressBar).progressDrawable as LayerDrawable).getDrawable(3).setTint(if (ColorTools.useDarkText(Settings["docksearchtxtcolor", -0x1000000])) -0x23000000 else -0x11000001)
                } else {
                    findViewById<View>(R.id.docksearchbar).visibility = GONE
                    findViewById<View>(R.id.battery).visibility = GONE
                }
                drawerGrid.numColumns = Settings["drawer:columns", 4]
                drawerGrid.verticalSpacing = (resources.displayMetrics.density * Settings["verticalspacing", 12]).toInt()
                feedRecycler.visibility = if (Settings["feedenabled", true]) VISIBLE else GONE
                val marginX = (Settings["feed:card_margin_x", 16] * resources.displayMetrics.density).toInt()
                (feedRecycler.layoutParams as LinearLayout.LayoutParams).setMargins(marginX, 0, marginX, 0)
                (findViewById<View>(R.id.parentNotification).layoutParams as LinearLayout.LayoutParams).leftMargin = marginX
                (findViewById<View>(R.id.parentNotification).layoutParams as LinearLayout.LayoutParams).rightMargin = marginX
                if (Settings["hidefeed", false]) {
                    feedRecycler.translationX = findViewById<View>(R.id.homeView).width.toFloat()
                    feedRecycler.alpha = 0f
                    desktop.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, y, _, oldY ->
                        val a = 6 * resources.displayMetrics.density
                        if (y > a) {
                            feedRecycler.translationX = 0f
                            feedRecycler.alpha = 1f
                        } else if (y < a && oldY >= a) {
                            feedRecycler.translationX = findViewById<View>(R.id.homeView).width.toFloat()
                            feedRecycler.alpha = 0f
                        }
                        if (!LauncherMenu.isActive) {
                            if (y + desktop.height < findViewById<View>(R.id.desktopContent).height - dockHeight) {
                                val distance = oldY - y
                                if ((y < a || distance > a) && behavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                                    behavior.setHideable(false)
                                } else if (distance < -a && behavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                                    behavior.isHideable = true
                                    behavior.state = BottomSheetBehavior.STATE_HIDDEN
                                }
                            } else if (behavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                                behavior.isHideable = false
                            }
                        }
                    })
                } else {
                    feedRecycler.translationX = 0f
                    feedRecycler.alpha = 1f
                    desktop.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, y, _, oldY ->
                        if (!LauncherMenu.isActive) {
                            if (y + desktop.height < findViewById<View>(R.id.desktopContent).height - dockHeight) {
                                val a = 6 * resources.displayMetrics.density
                                val distance = oldY - y
                                if ((y < a || distance > a) && behavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                                    behavior.setHideable(false)
                                } else if (distance < -a && behavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                                    behavior.isHideable = true
                                    behavior.state = BottomSheetBehavior.STATE_HIDDEN
                                }
                            } else if (behavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                                behavior.isHideable = false
                            }
                        }
                    })
                }
                if (!Settings["hidestatus", false]) desktop.setPadding(0, (getStatusBarHeight(this@Main) - 12 * resources.displayMetrics.density).toInt(), 0, 0)
                if (shouldSetApps) AppLoader(this@Main, onAppLoaderEnd).execute() else {
                    drawerGrid.adapter = DrawerAdapter(this@Main, apps)
                    setDock()
                }
                shouldSetApps = false
                customized = false
                when (Settings["dock:background_type", 0]) {
                    0 -> {
                        val bg = ShapeDrawable()
                        val tr = Settings["dockradius", 30] * resources.displayMetrics.density
                        bg.shape = RoundRectShape(floatArrayOf(tr, tr, tr, tr, 0f, 0f, 0f, 0f), null, null)
                        bg.paint.color = Settings["dock:background_color", -0x78000000]
                        findViewById<View>(R.id.drawer).background = bg
                    }
                    1 -> {
                        val bg = GradientDrawable()
                        bg.setColors(intArrayOf(
                                Settings["dock:background_color", -0x78000000] and 0x00ffffff,
                                Settings["dock:background_color", -0x78000000],
                                Settings["drawer:background_color", -0x78000000]
                        ), floatArrayOf(0f, dockHeight.toFloat() / (getDisplayHeight(this@Main) + dockHeight), 1f))
                        findViewById<View>(R.id.drawer).background = bg
                    }
                }
                val notificationBackground = ShapeDrawable()
                val r = resources.displayMetrics.density * Settings["feed:card_radius", 15]
                notificationBackground.shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
                notificationBackground.paint.color = Settings["notificationbgcolor", -0x1]
                findViewById<View>(R.id.parentNotification).background = notificationBackground
                val parentNotificationTitle = findViewById<TextView>(R.id.parentNotificationTitle)
                parentNotificationTitle.setTextColor(Settings["notificationtitlecolor", -0xeeeded])
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
            }
        }
        desktop = findViewById(R.id.desktop)
        desktop.isNestedScrollingEnabled = false
        desktop.isSmoothScrollingEnabled = false
        updateNavbarHeight(this@Main)
        drawerGrid = findViewById(R.id.drawergrid)
        searchBar = findViewById(R.id.searchbar)
        drawerGrid.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && drawerGrid.canScrollVertically(-1)) drawerGrid.requestDisallowInterceptTouchEvent(true)
            false
        }
        behavior = BottomSheetBehavior.from(findViewById<View>(R.id.drawer))
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior.isHideable = false
        val things = IntArray(5)
        val radii = FloatArray(8)
        val colors = IntArray(3)
        val floats = FloatArray(1)
        behavior.setBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) drawerGrid.smoothScrollToPositionFromTop(0, 0, 0)
                things[0] = Settings["blurLayers", 1]
                things[1] = Settings["dockradius", 30]
                things[2] = Settings["dock:background_type", 0]
                things[3] = Settings["dock:background_color", -0x78000000]
                things[4] = if (canBlurWall(this@Main)) 1 else 0
                val tr = things[1] * resources.displayMetrics.density
                radii[0] = tr
                radii[1] = tr
                radii[2] = tr
                radii[3] = tr
                colors[0] = Settings["dock:background_color", -0x78000000] and 0x00ffffff
                colors[1] = Settings["dock:background_color", -0x78000000]
                colors[2] = Settings["drawer:background_color", -0x78000000]
                floats[0] = dockHeight.toFloat() / (getDisplayHeight(this@Main) + dockHeight)
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val inverseOffset = 1 - slideOffset
                drawerGrid.alpha = slideOffset
                desktop.alpha = inverseOffset
                if (slideOffset >= 0) {
                    if (things[2] == 0) {
                        val bg = findViewById<View>(R.id.drawer).background as ShapeDrawable
                        bg.paint.color = ColorTools.blendColors(colors[2], things[3], slideOffset)
                        bg.shape = RoundRectShape(radii, null, null)
                    } else if (things[2] == 1) {
                        val bg = findViewById<View>(R.id.drawer).background as GradientDrawable
                        val invSquaredOffset = 1 - slideOffset * slideOffset
                        bg.setColors(colors, floatArrayOf(0f, floats[0] * invSquaredOffset, invSquaredOffset))
                    }
                    if (things[4] == 1) {
                        val repetitive = (slideOffset * 255).toInt() * things[0]
                        for (i in 0 until things[0]) blurBg.getDrawable(i).alpha = Math.min(repetitive - (i shl 8) + i, 255)
                    }
                    desktop.translationY = -200 * slideOffset
                } else if (!Settings["feed:show_behind_dock", false]) {
                    (desktop.layoutParams as CoordinatorLayout.LayoutParams).bottomMargin = ((1 + slideOffset) * (dockHeight + Tools.navbarHeight + (Settings["dockbottompadding", 10] - 18) *
                            resources.displayMetrics.density)).toInt()
                    desktop.layoutParams = desktop.layoutParams
                }
                findViewById<View>(R.id.realdock).alpha = inverseOffset
            }
        })
        feedRecycler = findViewById(R.id.feedrecycler)
        feedRecycler.layoutManager = LinearLayoutManager(this@Main)
        feedRecycler.isNestedScrollingEnabled = false
        if (Settings["feedenabled", true]) {
            FeedLoader(object : FeedLoader.Listener {
                override fun onFinished(feedModels: List<FeedItem>) {
                    feedRecycler.adapter = FeedAdapter(feedModels, this@Main, window)
                }
            }).execute()
            feedRecycler.visibility = VISIBLE
        } else feedRecycler.visibility = GONE
        NotificationService.contextReference = WeakReference(this)
        notifications = findViewById(R.id.notifications)
        notifications.isNestedScrollingEnabled = false
        notifications.layoutManager = LinearLayoutManager(this)
        val touchHelper = ItemTouchHelper(SwipeToDeleteCallback())
        touchHelper.attachToRecyclerView(notifications)
        val parentNotificationTitle = findViewById<TextView>(R.id.parentNotificationTitle)
        findViewById<View>(R.id.parentNotification).setOnLongClickListener(LauncherMenu(this@Main, window))
        findViewById<View>(R.id.parentNotification).setOnClickListener {
            if (notifications.visibility == VISIBLE) {
                desktop.scrollTo(0, 0)
                notifications.visibility = GONE
                findViewById<View>(R.id.parentNotification).background.alpha = 255
                findViewById<View>(R.id.arrowUp).visibility = GONE
            } else {
                notifications.visibility = VISIBLE
                findViewById<View>(R.id.parentNotification).background.alpha = 127
                findViewById<View>(R.id.arrowUp).visibility = VISIBLE
            }
        }
        try {
            NotificationService.listener = object : NotificationService.Listener {
                override fun onUpdate() {
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
                            notifications.adapter = NotificationAdapter(this@Main, window)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            startService(Intent(this, NotificationService::class.java))
        } catch (ignore: Exception) {}
        widgetLayout = findViewById(R.id.widgets)
        widgetLayout.layoutParams.height = Settings["widgetHeight", ViewGroup.LayoutParams.WRAP_CONTENT]
        widgetLayout.layoutParams = widgetLayout.layoutParams
        widgetLayout.onResizeListener = object : OnResizeListener {
            override fun onStop(newHeight: Int) { Settings["widgetHeight"] = newHeight }
            override fun onCrossPress() = deleteWidget()
            override fun onUpdate(newHeight: Int) {
                widgetLayout.layoutParams.height = newHeight
                widgetLayout.layoutParams = widgetLayout.layoutParams
            }
        }
        widgetManager = AppWidgetManager.getInstance(this)
        widgetHost = AppWidgetHost(this, 0xe1d9e15)
        widgetHost.startListening()
        createWidget()
        val scaleGestureDetector = ScaleGestureDetector(
                this@Main, PinchListener(this@Main, window)
        )
        findViewById<View>(R.id.homeView).setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP && behavior.state == BottomSheetBehavior.STATE_COLLAPSED) WallpaperManager.getInstance(this@Main).sendWallpaperCommand(
                    v.windowToken,
                    WallpaperManager.COMMAND_TAP,
                    event.x.toInt(),
                    event.y.toInt(),
                    0, null
            )
            false
        }
        findViewById<View>(R.id.desktop).setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            false
        }
        if (Settings["mnmlstatus", false]) window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LAYOUT_STABLE or
                SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                SYSTEM_UI_FLAG_LOW_PROFILE else window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LAYOUT_STABLE or
                SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val list = ArrayList<Rect>()
            list.add(Rect(0, 0, getDisplayWidth(this), getDisplayHeight(this)))
            findViewById<View>(R.id.homeView).systemGestureExclusionRects = list
        }
        methods.setCustomizations()
        blurBg = LayerDrawable(arrayOf<Drawable>(
                ColorDrawable(0x0),
                ColorDrawable(0x0),
                ColorDrawable(0x0),
                ColorDrawable(0x0)
        ))
        (findViewById<View>(R.id.blur) as ImageView).setImageDrawable(blurBg)
        System.gc()
    }

    fun selectWidget() {
        val appWidgetId = widgetHost.allocateAppWidgetId()
        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        val customInfo: ArrayList<out Parcelable?> = ArrayList()
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo)
        val customExtras: ArrayList<out Parcelable?> = ArrayList()
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras)
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_APPWIDGET) {
                val extras = data!!.extras
                if (extras != null) {
                    val id = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                    val widgetInfo = widgetManager.getAppWidgetInfo(id)
                    if (widgetInfo.configure != null) {
                        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                        intent.component = widgetInfo.configure
                        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                        startActivityForResult(intent, REQUEST_CREATE_APPWIDGET)
                    } else createWidget(data)
                }
            } else if (requestCode == REQUEST_CREATE_APPWIDGET) createWidget(data)
            //else if (requestCode == REQUEST_BIND_WIDGET) createWidget();
        } else if (resultCode == Activity.RESULT_CANCELED && data != null) {
            val appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            if (appWidgetId != -1) widgetHost.deleteAppWidgetId(appWidgetId)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun deleteWidget() {
        widgetHost.deleteAppWidgetId(hostView.appWidgetId)
        widgetLayout.removeView(hostView)
        widgetLayout.visibility = GONE
        Settings["widget"] = ""
    }

    fun createWidget(data: Intent?) {
        widgetLayout.visibility = VISIBLE
        try {
            widgetHost.deleteAppWidgetId(hostView.appWidgetId)
            widgetLayout.removeView(hostView)
        } catch (e: Exception) { e.printStackTrace() }
        try {
            val id = data!!.extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            val providerInfo = widgetManager.getAppWidgetInfo(id)
            hostView = widgetHost.createView(applicationContext, id, providerInfo)
            hostView.isLongClickable = false
            widgetLayout.addView(hostView)
            val onLongClickListener = OnLongClickListener {
                widgetLayout.performLongClick()
                true
            }
            for (i in 0 until hostView.childCount) hostView.getChildAt(i).setOnLongClickListener(onLongClickListener)
            if (!widgetManager.bindAppWidgetIdIfAllowed(id, providerInfo.provider)) {
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, providerInfo.provider)
                startActivityForResult(intent, REQUEST_BIND_WIDGET)
            }
            Settings["widget"] = providerInfo.provider.packageName + "/" + providerInfo.provider.className + "/" + id
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun createWidget() {
        val str = Settings["widget", "posidon.launcher/posidon.launcher.external.widgets.ClockWidget"]
        if (str != "") {
            val s = str.split("/").toTypedArray()
            val packageName = s[0]
            val className: String
            className = try {
                s[1]
            } catch (ignore: ArrayIndexOutOfBoundsException) {
                return
            }
            var providerInfo: AppWidgetProviderInfo? = null
            val appWidgetInfos = widgetManager.installedProviders
            var widgetIsFound = false
            for (j in appWidgetInfos.indices) {
                if (appWidgetInfos[j].provider.packageName == packageName && appWidgetInfos[j].provider.className == className) {
                    providerInfo = appWidgetInfos[j]
                    widgetIsFound = true
                    break
                }
            }
            if (!widgetIsFound) return
            var id: Int
            try {
                id = s[2].toInt()
            } catch (e: ArrayIndexOutOfBoundsException) {
                id = widgetHost.allocateAppWidgetId()
                if (!widgetManager.bindAppWidgetIdIfAllowed(id, providerInfo!!.provider)) { // Request permission - https://stackoverflow.com/a/44351320/1816603
                    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, providerInfo.provider)
                    startActivityForResult(intent, REQUEST_BIND_WIDGET)
                }
            }
            hostView = widgetHost.createView(applicationContext, id, providerInfo)
            hostView.setAppWidget(id, providerInfo)
            widgetLayout.addView(hostView)
            val onLongClickListener = OnLongClickListener {
                widgetLayout.performLongClick()
                true
            }
            for (i in 0 until hostView.childCount) hostView.getChildAt(i).setOnLongClickListener(onLongClickListener)
        } else widgetLayout.visibility = GONE
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        onUpdate()
        methods.setDock()
    }

    override fun onResume() {
        super.onResume()
        widgetHost.startListening()
        overridePendingTransition(R.anim.home_enter, R.anim.appexit)
        onUpdate()
    }

    private fun onUpdate() {
        val tmp = Tools.navbarHeight
        updateNavbarHeight(this)
        if (Settings["feedenabled", true]) FeedLoader(object : FeedLoader.Listener {
            override fun onFinished(feedModels: List<FeedItem>) {
                feedRecycler.adapter = FeedAdapter(feedModels, this@Main, window)
            }
        }).execute(null as Void?)
        NotificationService.listener!!.onUpdate()
        if (canBlurWall(this)) {
            val blurLayers = Settings["blurLayers", 1]
            val radius = Settings["blurradius", 15f]
            val shouldHide = behavior.state == BottomSheetBehavior.STATE_COLLAPSED || behavior.state == BottomSheetBehavior.STATE_HIDDEN
            Thread(Runnable {
                for (i in 0 until blurLayers) {
                    val bd = BitmapDrawable(resources,
                            blurredWall(this@Main, radius / blurLayers * (i + 1))
                    )
                    if (shouldHide) bd.alpha = 0
                    blurBg.setId(i, i)
                    blurBg.setDrawableByLayerId(i, bd)
                }
            }).start()
        }
        if (tmp != Tools.navbarHeight || customized) {
            methods.setCustomizations()
            try { notifications.adapter!!.notifyDataSetChanged() }
            catch (e: Exception) { e.printStackTrace() }
        } else if (powerManager?.isPowerSaveMode == false && Settings["animatedicons", true]) for (app in apps) animate(app!!.icon!!)
        System.gc()
    }

    override fun onPause() {
        super.onPause()
        if (LauncherMenu.isActive) LauncherMenu.dialog!!.dismiss()
        if (behavior.state != BottomSheetBehavior.STATE_COLLAPSED) behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        desktop.scrollTo(0, 0)
        widgetHost.stopListening()
        if (Settings["collapseNotifications", false] && NotificationService.notificationsAmount > 1) {
            notifications.visibility = GONE
            findViewById<View>(R.id.arrowUp).visibility = GONE
            findViewById<View>(R.id.parentNotification).background.alpha = 255
        }
        System.gc()
    }

    override fun onStop() {
        super.onStop()
        widgetHost.stopListening()
    }

    override fun onDestroy() {
        try { unregisterReceiver(receiver) }
        catch (ignore: Exception) {}
        unregisterReceiver(batteryInfoReceiver)
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            try {
                startService(Intent(this, NotificationService::class.java))
            } catch (ignore: Exception) {}
            if (Settings["mnmlstatus", false]) window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    SYSTEM_UI_FLAG_LOW_PROFILE
            if (shouldSetApps) AppLoader(this@Main, onAppLoaderEnd).execute() //setApps(drawerGrid);
        } else window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LAYOUT_STABLE or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    inner class AppChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            AppLoader(this@Main, onAppLoaderEnd).execute()
        }
    }

    override fun onBackPressed() {
        if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) behavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
        else if (widgetLayout.resizing) widgetLayout.resizing = false
    }

    fun openSearch(v: View?) = startActivity(
            Intent(this, SearchActivity::class.java),
            ActivityOptions.makeCustomAnimation(this, R.anim.fadein, R.anim.fadeout).toBundle()
    )

    interface Methods {
        fun setDock()
        fun setCustomizations()
    }

    private class AppLoader(context: Context, private val onEnd: () -> Unit) : AsyncTask<Void?, Void?, Void?>() {
        private lateinit var tmpApps: Array<App?>
        private val context: WeakReference<Context> = WeakReference(context)
        override fun doInBackground(objects: Array<Void?>): Void? {
            App.hidden.clear()
            val packageManager = context.get()!!.packageManager
            var skippedapps = 0
            val pacslist = packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
            tmpApps = arrayOfNulls(pacslist.size)
            val ICONSIZE = (65 * context.get()!!.resources.displayMetrics.density).toInt()
            var themeRes: Resources? = null
            val iconpackName = Settings["iconpack", "system"]
            var iconResource: String?
            var intres: Int
            var intresiconback = 0
            var intresiconfront = 0
            var intresiconmask = 0
            val scaleFactor: Float
            val p = Paint(Paint.FILTER_BITMAP_FLAG)
            p.isAntiAlias = true
            val origP = Paint(Paint.FILTER_BITMAP_FLAG)
            origP.isAntiAlias = true
            val maskp = Paint(Paint.FILTER_BITMAP_FLAG)
            maskp.isAntiAlias = true
            maskp.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            if (iconpackName.compareTo("") != 0) {
                try {
                    themeRes = packageManager.getResourcesForApplication(iconpackName)
                } catch (ignore: Exception) {
                }
                if (themeRes != null) {
                    val backAndMaskAndFront = ThemeTools.getIconBackAndMaskResourceName(themeRes, iconpackName)
                    if (backAndMaskAndFront[0] != null) intresiconback = themeRes.getIdentifier(backAndMaskAndFront[0], "drawable", iconpackName)
                    if (backAndMaskAndFront[1] != null) intresiconmask = themeRes.getIdentifier(backAndMaskAndFront[1], "drawable", iconpackName)
                    if (backAndMaskAndFront[2] != null) intresiconfront = themeRes.getIdentifier(backAndMaskAndFront[2], "drawable", iconpackName)
                }
            }
            val uniformOptions = BitmapFactory.Options()
            uniformOptions.inScaled = false
            var origCanv: Canvas
            var canvas: Canvas
            scaleFactor = ThemeTools.getScaleFactor(themeRes, iconpackName)
            var back: Bitmap? = null
            var mask: Bitmap? = null
            var front: Bitmap? = null
            var scaledBitmap: Bitmap?
            var scaledOrig: Bitmap
            var orig: Bitmap
            if (iconpackName.compareTo("") != 0 && themeRes != null) {
                if (intresiconback != 0) back = BitmapFactory.decodeResource(themeRes, intresiconback, uniformOptions)
                if (intresiconmask != 0) mask = BitmapFactory.decodeResource(themeRes, intresiconmask, uniformOptions)
                if (intresiconfront != 0) front = BitmapFactory.decodeResource(themeRes, intresiconfront, uniformOptions)
            }
            for (i in pacslist.indices) {
                val app = App()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        app.icon = adaptic(context.get()!!,
                                packageManager.getActivityIcon(ComponentName(
                                        pacslist[i].activityInfo.packageName,
                                        pacslist[i].activityInfo.name)
                                )
                        )
                    } catch (e: Exception) {
                        app.icon = pacslist[i].loadIcon(packageManager)
                        e.printStackTrace()
                    }
                } else app.icon = pacslist[i].loadIcon(packageManager)
                app.packageName = pacslist[i].activityInfo.packageName
                app.name = pacslist[i].activityInfo.name
                app.label = Settings[app.packageName + "/" + app.name + "?label", pacslist[i].loadLabel(packageManager).toString()]
                intres = 0
                iconResource = ThemeTools.getResourceName(themeRes, iconpackName, "ComponentInfo{" + app.packageName + "/" + app.name + "}")
                if (iconResource != null) intres = themeRes!!.getIdentifier(iconResource, "drawable", iconpackName)
                if (intres != 0) try {
                    app.icon = themeRes!!.getDrawable(intres)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) app.icon = adaptic(context.get()!!, app.icon!!)
                    try {
                        if (!(context.get()!!.getSystemService(Context.POWER_SERVICE) as PowerManager).isPowerSaveMode && Settings["animatedicons", true]) animate(app.icon!!)
                    } catch (ignore: Exception) {}
                } catch (e: Exception) {
                    e.printStackTrace()
                } else try {
                    orig = Bitmap.createBitmap(app.icon!!.intrinsicWidth, app.icon!!.intrinsicHeight, Bitmap.Config.ARGB_8888)
                    app.icon!!.setBounds(0, 0, app.icon!!.intrinsicWidth, app.icon!!.intrinsicHeight)
                    app.icon!!.draw(Canvas(orig))
                    scaledOrig = Bitmap.createBitmap(ICONSIZE, ICONSIZE, Bitmap.Config.ARGB_8888)
                    scaledBitmap = Bitmap.createBitmap(ICONSIZE, ICONSIZE, Bitmap.Config.ARGB_8888)
                    canvas = Canvas(scaledBitmap)
                    if (back != null) canvas.drawBitmap(back, getResizedMatrix(back, ICONSIZE, ICONSIZE), p)
                    origCanv = Canvas(scaledOrig)
                    orig = getResizedBitmap(orig, (ICONSIZE * scaleFactor).toInt(), (ICONSIZE * scaleFactor).toInt())
                    origCanv.drawBitmap(orig, scaledOrig.width - orig.width / 2f - scaledOrig.width / 2f, scaledOrig.width - orig.width / 2f - scaledOrig.width / 2f, origP)
                    if (mask != null) origCanv.drawBitmap(mask, getResizedMatrix(mask, ICONSIZE, ICONSIZE), maskp)
                    if (back != null) canvas.drawBitmap(getResizedBitmap(scaledOrig, ICONSIZE, ICONSIZE), 0f, 0f, p) else canvas.drawBitmap(getResizedBitmap(scaledOrig, ICONSIZE, ICONSIZE), 0f, 0f, p)
                    if (front != null) canvas.drawBitmap(front, getResizedMatrix(front, ICONSIZE, ICONSIZE), p)
                    app.icon = BitmapDrawable(context.get()!!.resources, scaledBitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val customIcon = Settings["app:" + app.packageName + ":icon", ""]
                if (customIcon != "") try {
                    val data = customIcon.split(':').toTypedArray()[1].split('|').toTypedArray()
                    println(data[0])
                    println(data[1])
                    val t = packageManager.getResourcesForApplication(data[0])
                    val intRes = t.getIdentifier(data[1], "drawable", data[0])
                    app.icon = animate(t.getDrawable(intRes))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                putInSecondMap(app.packageName + "/" + app.name, app)
                if (Settings[pacslist[i].activityInfo.packageName + "/" + pacslist[i].activityInfo.name + "?hidden", false]) {
                    skippedapps++
                    App.hidden.add(app)
                } else tmpApps[i - skippedapps] = app
            }
            tmpApps = tmpApps.copyOf(tmpApps.size - skippedapps)
            if (Settings["sortAlgorithm", 1] == 1) colorSort(tmpApps) else labelSort(tmpApps)
            return null
        }

        override fun onPostExecute(v: Void?) {
            apps = tmpApps
            swapMaps()
            clearSecondMap()
            onEnd()
        }
    }

    private val onAppLoaderEnd = {
        drawerGrid.adapter = DrawerAdapter(this@Main, apps)
        drawerGrid.onItemLongClickListener = drawer(this@Main)
        drawerGrid.onItemClickListener = OnItemClickListener { _, v, i, _ -> apps[i]!!.open(this@Main, v) }
        methods.setDock()
    }

    companion object {
        const val REQUEST_PICK_APPWIDGET = 0
        const val REQUEST_CREATE_APPWIDGET = 1
        const val REQUEST_BIND_WIDGET = 2
        var shouldSetApps = true
        var customized = false
        var apps: Array<App?> = emptyArray()
        var accentColor = -0xeeaa01
        var receiver: AppChangeReceiver? = null
        lateinit var methods: Methods
        @RequiresApi(api = Build.VERSION_CODES.M)
        lateinit var launcherApps: LauncherApps
    }
}