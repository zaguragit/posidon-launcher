package posidon.launcher.external.quickstep

import android.annotation.TargetApi
import android.app.ActivityManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.hasNavbar

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
    
    private fun onNavigationModeChanged(newMode: NavigationMode) {
        println(newMode.name)
        navigationMode = newMode
    }

    private val mMyBinder: IBinder = object : IOverviewProxy.Stub() {

        override fun onOverviewToggle() {
            Toast.makeText(Tools.appContext!!, "QUICKSTEP.onOverviewToggle()", Toast.LENGTH_LONG).show()
        }

        override fun onQuickStep(event: MotionEvent?) {
            Toast.makeText(Tools.appContext!!, "QUICKSTEP.onQuickStep($event)", Toast.LENGTH_LONG).show()
        }

        override fun onBind(sysUiProxy: ISystemUiProxy?) {
            Toast.makeText(Tools.appContext!!, "QUICKSTEP.onBind(sysUiProxy)", Toast.LENGTH_LONG).show()
            systemUiProxy = sysUiProxy
        }

        override fun onOverviewShown(triggeredFromAltTab: Boolean) {
            Toast.makeText(Tools.appContext!!, "QUICKSTEP.onOverviewShown($triggeredFromAltTab)", Toast.LENGTH_LONG).show()
        }

        override fun onOverviewHidden(triggeredFromAltTab: Boolean, triggeredFromHomeKey: Boolean) {
            Toast.makeText(Tools.appContext!!, "QUICKSTEP.onOverviewHidden($triggeredFromAltTab, $triggeredFromHomeKey)", Toast.LENGTH_LONG).show()
        }

        override fun onTip(actionType: Int, viewType: Int) {
            Toast.makeText(Tools.appContext!!, "QUICKSTEP.onTip($actionType, $viewType)", Toast.LENGTH_LONG).show()
        }

        override fun onMotionEvent(event: MotionEvent?) {
            Toast.makeText(Tools.appContext!!, "QUICKSTEP.onMotionEvent($event)", Toast.LENGTH_LONG).show()
        }

        override fun onPreMotionEvent(downHitTarget: Int) {
            Toast.makeText(Tools.appContext!!, "QUICKSTEP.onPreMotionEvent($downHitTarget)", Toast.LENGTH_LONG).show()
        }

        override fun onQuickScrubStart() {
            Toast.makeText(Tools.appContext!!, "QUICKSTEP.onQuickScrubStart()", Toast.LENGTH_LONG).show()
        }

        override fun onQuickScrubEnd() {
            Toast.makeText(Tools.appContext!!, "QUICKSTEP.onQuickScrubEnd()", Toast.LENGTH_LONG).show()
        }

        override fun onQuickScrubProgress(progress: Float) {
            Toast.makeText(Tools.appContext!!, "QUICKSTEP.onQuickScrubProgress($progress)", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Settings.init(applicationContext)

        if (hasNavbar) onNavigationModeChanged(NavigationMode.THREE_BUTTONS)
        else onNavigationModeChanged(NavigationMode.GESTURAL)
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d("QUICKSTEP", "Touch service connected")
        activityManager = getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        return mMyBinder
    }
}