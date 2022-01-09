package posidon.launcher

import android.app.Activity
import android.app.ActivityOptions
import android.app.Dialog
import android.graphics.Rect
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.animation.PathInterpolator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.posidon.android.launcherutils.liveWallpaper.Kustom
import posidon.android.conveniencelib.Device
import posidon.android.conveniencelib.dp
import posidon.launcher.customizations.settingScreens.Customizations
import posidon.launcher.feed.order.FeedOrderActivity
import posidon.launcher.storage.Settings
import posidon.launcher.tools.open
import posidon.launcher.tools.vibrate
import posidon.launcher.view.drawer.DrawerView

object LauncherMenu {

    val isActive get() = dialog != null
    var dialog: Dialog? = null

    fun openOverview(home: Activity) {
        if (!isActive) {
            val window = home.window
            home.vibrate()
            val homescreen = window.decorView.findViewById<View>(android.R.id.content)
            val scrollbarPosition = Settings["drawer:scrollbar:position", 1]
            val scrollbarWidth = if (Settings["drawer:scrollbar:enabled", false] && Settings["drawer:scrollbar:show_outside", false]) {
                if (scrollbarPosition == 0) {
                    -home.dp(Settings["drawer:scrollbar:width", 24])
                }
                else home.dp(Settings["drawer:scrollbar:width", 24])
            } else 0f
            val page = homescreen.findViewById<View>(R.id.feed)
            val drawer = homescreen.findViewById<DrawerView>(R.id.drawer)
            dialog = Dialog(home, R.style.longpressmenusheet).apply {
                setContentView(R.layout.menu)
                this.window!!.setGravity(Gravity.BOTTOM)
                findViewById<View>(R.id.custombtn).setOnClickListener {
                    home.open(Customizations::class.java, ActivityOptions.makeCustomAnimation(home, R.anim.slideup, R.anim.home_exit).toBundle())
                    dialog?.dismiss()
                }
                findViewById<View>(R.id.sectionsBtn).setOnClickListener {
                    home.open(FeedOrderActivity::class.java, ActivityOptions.makeCustomAnimation(home, R.anim.slideup, R.anim.home_exit).toBundle())
                    dialog?.dismiss()
                }
                setOnDismissListener {
                    exit(homescreen, window, drawer)
                }
            }
            page.animate().apply {
                if (scrollbarPosition != 2)
                    translationX(scrollbarWidth / 2f)
            }.scaleX(0.99f).scaleY(0.99f).translationY(-home.dp(96)).setInterpolator(PathInterpolator(0.245f, 1.275f, 0.405f, 1.005f)).duration = 200L
            drawer.scrollBar.animate().apply {
                if (scrollbarPosition != 2)
                    translationX(scrollbarWidth)
            }.duration = 100L
            drawer.isHideable = true
            drawer.state = BottomSheetBehavior.STATE_HIDDEN
            page.setBackgroundResource(R.drawable.page)
            window.setBackgroundDrawableResource(R.drawable.black_gradient)
            homescreen.setOnClickListener { dialog?.dismiss() }
            dialog?.show()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                homescreen.systemGestureExclusionRects = listOf()
            }
            if (Settings["kustom:variables:enable", false]) {
                Kustom[home, "posidon", "screen"] = "overview"
            }
        }
    }

    private fun exit(homescreen: View, window: Window, drawer: DrawerView) {
        drawer.scrollBar.animate().translationX(0f).translationY(0f)
        drawer.isHideable = false
        drawer.state = BottomSheetBehavior.STATE_COLLAPSED
        val page = homescreen.findViewById<View>(R.id.feed)
        page.animate().translationX(0f).scaleX(1f).scaleY(1f).translationY(0f).duration = 200L
        page.setBackgroundColor(0x0)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Settings["gesture:back", ""] == "") {
            homescreen.systemGestureExclusionRects = listOf(Rect(0, 0, Device.screenWidth(homescreen.context), Device.screenHeight(homescreen.context)))
        }
        dialog = null
        if (Settings["kustom:variables:enable", false]) {
            Kustom[homescreen.context, "posidon", "screen"] = "home"
        }
    }
}