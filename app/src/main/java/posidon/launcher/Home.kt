package posidon.launcher

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.drawable.*
import android.os.*
import android.util.Log
import android.view.*
import android.view.View.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.external.Kustom
import posidon.launcher.external.Widget
import posidon.launcher.feed.notifications.NotificationService
import posidon.launcher.items.users.AppLoader
import posidon.launcher.search.SearchActivity
import posidon.launcher.storage.Settings
import posidon.launcher.tools.*
import posidon.launcher.tools.Tools.tryAnimate
import posidon.launcher.tools.Tools.updateNavbarHeight
import posidon.launcher.tutorial.WelcomeActivity
import posidon.launcher.view.ResizableLayout
import posidon.launcher.view.drawer.BottomDrawerBehavior
import posidon.launcher.view.drawer.BottomDrawerBehavior.*
import posidon.launcher.view.drawer.DrawerView
import posidon.launcher.view.feed.Feed
import java.lang.ref.WeakReference
import kotlin.system.exitProcess

class Home : AppCompatActivity() {

    val drawer by lazy { findViewById<DrawerView>(R.id.drawer)!! }
    private val dock by lazy { drawer.dock }

    val feed by lazy { findViewById<Feed>(R.id.feed)!! }

    val homeView by lazy { findViewById<ViewGroup>(R.id.homeView)!! }

    private val batteryInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            dock.battery.progress = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        }
    }

    private lateinit var powerManager: PowerManager

    init { instance = this }
    companion object { lateinit var instance: Home private set }

    private fun setCustomizations() {

        feed.update(this, drawer)
        dock.updateTheme(drawer)
        drawer.update()
        drawer.locked = !Settings["drawer:slide_up", true]

        applyFontSetting()

        if (Settings["hidestatus", false]) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        drawer.updateTheme()

        run {
            val searchHint = Settings["searchhinttxt", "Search.."]
            drawer.searchTxt.text = searchHint
            dock.searchTxt.text = searchHint
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Settings["gesture:back", ""] == "") {
                window.decorView.findViewById<View>(android.R.id.content).systemGestureExclusionRects = listOf(Rect(0, 0, Device.displayWidth, Device.displayHeight))
            } else {
                window.decorView.findViewById<View>(android.R.id.content).systemGestureExclusionRects = listOf()
            }
        }

        Global.shouldSetApps = false
        Global.customized = false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Tools.appContextReference = WeakReference(applicationContext)
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
        Global.accentColor = Settings["accent", 0x1155ff] or -0x1000000

        updateNavbarHeight(this@Home)

        if (Settings["search:asHome", false]) {
            startActivity(Intent(this, SearchActivity::class.java))
            finish()
            return
        }

        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

        setContentView(R.layout.main)

        (getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps).registerCallback(AppLoader.Callback(this, drawer::onAppLoaderEnd))
        registerReceiver(batteryInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        drawer.init(this)
        feed.init(drawer)

        setCustomizations()

        homeView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP && drawer.state == STATE_COLLAPSED)
                WallpaperManager.getInstance(this@Home).sendWallpaperCommand(
                    v.windowToken,
                    WallpaperManager.COMMAND_TAP,
                    event.x.toInt(),
                    event.y.toInt(),
                    0, null)
            false
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

        findViewById<ImageView>(R.id.blur).setImageDrawable(drawer.blurBg)

        System.gc()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val w = Widget.handleActivityResult(this, requestCode, resultCode, data)
        if (w != null) {
            feed.add(w)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        onUpdate()
        dock.loadApps(drawer, feed, feed.desktopContent, this)
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
        overridePendingTransition(R.anim.home_enter, R.anim.appexit)
        onUpdate()
        feed.onResume(this)
    }

    private fun onUpdate() {
        if (!Settings["search:asHome", false]) {
            val tmp = Tools.navbarHeight
            updateNavbarHeight(this)
            drawer.tryBlur()
            if (Global.customized || tmp != Tools.navbarHeight) {
                setCustomizations()
            } else if (!powerManager.isPowerSaveMode && Settings["animatedicons", true]) {
                for (app in Global.apps) {
                    tryAnimate(app.icon!!)
                }
            }
            if (Settings["news:load_on_resume", true]) {
                feed.loadNews(this)
            }
            if (feed.notifications != null || Settings["notif:badges", true]) {
                NotificationService.onUpdate()
            }
        }
        if (!powerManager.isPowerSaveMode && Settings["animatedicons", true]) {
            for (app in Global.apps) {
                tryAnimate(app.icon!!)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (LauncherMenu.isActive) {
            LauncherMenu.dialog!!.dismiss()
        }
        drawer.state = STATE_COLLAPSED
        if (!Settings["feed:keep_pos", false]) {
            feed.scroll.scrollTo(0, if (Settings["feed:rest_at_bottom", false]) feed.desktopContent.height else 0)
        }
        if (Settings["kustom:variables:enable", false]) {
            Kustom["screen"] = "?"
        }
        feed.onPause()
    }

    override fun onStart() {
        super.onStart()
        drawer.onAppLoaderEnd()
        feed.loadNews(this)
    }

    override fun onDestroy() {
        runCatching {
            unregisterReceiver(batteryInfoReceiver)
        }
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!Settings["search:asHome", false]) {
            if (hasFocus) {
                if (feed.notifications != null || Settings["notif:badges", true]) {
                    try { startService(Intent(this, NotificationService::class.java)) }
                    catch (e: Exception) {}
                }
                if (Settings["mnmlstatus", false]) window.decorView.systemUiVisibility =
                    SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    SYSTEM_UI_FLAG_LOW_PROFILE
                drawer.loadAppsIfShould()
                drawer.setKustomVars()
            } else {
                window.decorView.systemUiVisibility =
                    SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
        }
    }

    override fun onBackPressed() = when {
        drawer.state == BottomDrawerBehavior.STATE_EXPANDED -> drawer.state = STATE_COLLAPSED
        ResizableLayout.currentlyResizing != null -> ResizableLayout.currentlyResizing?.resizing = false
        else -> Gestures.performTrigger(Settings["gesture:back", ""])
    }

    fun openSearch(v: View) = SearchActivity.open(this)

    fun setDock() = dock.loadApps(drawer, feed, feed.desktopContent, this)
}