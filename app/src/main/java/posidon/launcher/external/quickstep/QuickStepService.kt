package posidon.launcher.external.quickstep

import android.annotation.TargetApi
import android.app.ActivityManager
import android.app.Service
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Region
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Main
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools

@TargetApi(Build.VERSION_CODES.Q)
class QuickStepService : Service() {

    companion object {

        private var navigationMode = NavigationMode.THREE_BUTTONS

        private var defaultDisplayId: Int = 0

        private var systemUiProxy: ISystemUiProxy? = null

        lateinit var recentTasks: List<ActivityManager.RecentTaskInfo?> private set
        private lateinit var activityManager: ActivityManager

        fun postAsyncCallback(handler: Handler, callback: Runnable?) {
            val msg = Message.obtain(handler, callback)
            msg.isAsynchronous = true
            handler.sendMessage(msg)
        }
    }

    enum class NavigationMode(val hasGestures: Boolean, val resValue: Int) {
        THREE_BUTTONS(false, 0), GESTURAL(true, 2);
    }
    
    fun onNavigationModeChanged(newMode: NavigationMode) {
        println(newMode.name)
        navigationMode = newMode
    }

    private val mMyBinder: IBinder = object : IOverviewProxy.Stub() {

        override fun onActiveNavBarRegionChanges(activeRegion: Region?) {
            println("QUICKSTEP.onActiveNavBarRegionChanges(${activeRegion!!.bounds})")
            if (Tools.hasNavbar(this@QuickStepService)) onNavigationModeChanged(NavigationMode.THREE_BUTTONS)
            else onNavigationModeChanged(NavigationMode.GESTURAL)
        }

        override fun onInitialize(params: Bundle?) {
            println("QUICKSTEP.onInitialize($params)")
            Main.instance.runOnUiThread {
                Toast.makeText(this@QuickStepService, params.toString(), Toast.LENGTH_LONG).show()
            }
            systemUiProxy = ISystemUiProxy.Stub.asInterface(params!!.getBinder("extra_sysui_proxy"))
            systemUiProxy!!.onSplitScreenInvoked()
            systemUiProxy!!.monitorGestureInput("swipe-up", defaultDisplayId)
        }

        override fun onOverviewToggle() {
            println("QUICKSTEP.onOverviewToggle()")
            if (QuickStepActivity.INSTANCE?.hasWindowFocus() == true) {
                if (recentTasks.isEmpty()) startActivity(Intent(this@QuickStepService, Main::class.java))
                else startActivity(recentTasks[0]!!.baseIntent)
            } else {
                recentTasks = activityManager.getRecentTasks(Int.MAX_VALUE, ActivityManager.RECENT_IGNORE_UNAVAILABLE)
                startActivity(Intent(baseContext, QuickStepActivity::class.java).apply { addFlags(FLAG_ACTIVITY_NEW_TASK) })
            }
        }

        override fun onOverviewShown(triggeredFromAltTab: Boolean) {
            println("QUICKSTEP.onOverviewShown($triggeredFromAltTab)")
        }

        override fun onOverviewHidden(triggeredFromAltTab: Boolean, triggeredFromHomeKey: Boolean) {
            println("QUICKSTEP.onOverviewHidden($triggeredFromAltTab, $triggeredFromHomeKey)")
        }

        override fun onTip(actionType: Int, viewType: Int) {
            println("QUICKSTEP.onTip($actionType, $viewType)")
        }

        override fun onAssistantAvailable(available: Boolean) {
            println("QUICKSTEP.onAssistantAvailable($available)")
        }

        override fun onAssistantVisibilityChanged(visibility: Float) {
            println("QUICKSTEP.onAssistantVisibilityChanged($visibility)")
        }

        override fun onBackAction(completed: Boolean, downX: Int, downY: Int, isButton: Boolean, gestureSwipeLeft: Boolean) {
            println("QUICKSTEP.onBackAction($completed, $downX, $downY, $isButton, $gestureSwipeLeft)")
        }

        override fun onSystemUiStateChanged(stateFlags: Int) {
            println("QUICKSTEP.onSystemUiStateChanged($stateFlags)")
        }
    }

    override fun onCreate() {
        super.onCreate()
        Settings.init(this)

        if (Tools.hasNavbar(this@QuickStepService)) onNavigationModeChanged(NavigationMode.THREE_BUTTONS)
        else onNavigationModeChanged(NavigationMode.GESTURAL)
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d("QUICKSTEP", "Touch service connected")
        activityManager = getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        return mMyBinder
    }
}