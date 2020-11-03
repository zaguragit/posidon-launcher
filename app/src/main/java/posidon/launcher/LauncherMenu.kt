package posidon.launcher

import android.app.ActivityOptions
import android.app.Dialog
import android.content.Intent
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.animation.PathInterpolator
import posidon.launcher.customizations.Customizations
import posidon.launcher.external.Kustom
import posidon.launcher.external.Widget
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Device
import posidon.launcher.tools.Tools
import posidon.launcher.tools.vibrate
import posidon.launcher.view.drawer.BottomDrawerBehavior
import posidon.launcher.view.drawer.DrawerView
import posidon.launcher.wall.Gallery

object LauncherMenu {

    var isActive = false
    var dialog: Dialog? = null

    fun openOverview() {
        if (!isActive) {
            val context = Tools.appContext!!
            val window = Home.instance.window
            isActive = true
            context.vibrate()
            val homescreen = window.decorView.findViewById<View>(android.R.id.content)
            val page = homescreen.findViewById<View>(R.id.feed)
            page.animate().scaleX(0.65f).scaleY(0.65f).translationY(page.height * -0.05f).setInterpolator(PathInterpolator(0.245f, 1.275f, 0.405f, 1.005f)).duration = 450L
            val drawer = homescreen.findViewById<DrawerView>(R.id.drawer)
            drawer.state = BottomDrawerBehavior.STATE_HIDDEN
            dialog = Dialog(Home.instance, R.style.longpressmenusheet)
            dialog!!.setContentView(R.layout.menu)
            dialog!!.window!!.setGravity(Gravity.BOTTOM)
            dialog!!.findViewById<View>(R.id.custombtn).setOnClickListener {
                val i = Intent(context, Customizations::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                context.startActivity(i, ActivityOptions.makeCustomAnimation(context, R.anim.slideup, R.anim.home_exit).toBundle())
                dialog!!.dismiss()
            }
            dialog!!.findViewById<View>(R.id.wallbtn).setOnClickListener {
                val i = Intent(context, Gallery::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                context.startActivity(i, ActivityOptions.makeCustomAnimation(context, R.anim.slideup, R.anim.home_exit).toBundle())
                dialog!!.dismiss()
            }
            dialog!!.findViewById<View>(R.id.widgetpickerbtn).setOnClickListener {
                Widget.selectWidget(Home.instance)
                dialog!!.dismiss()
            }
            page.setBackgroundResource(R.drawable.page)
            if (Tools.canBlurDrawer) {
                window.setBackgroundDrawable(LayerDrawable(arrayOf(BitmapDrawable(context.resources, Tools.blurredWall(Settings["drawer:blur:rad", 15f])), context.getDrawable(R.drawable.black_gradient))))
            } else {
                window.setBackgroundDrawableResource(R.drawable.black_gradient)
            }
            homescreen.setOnClickListener { dialog!!.dismiss() }
            dialog!!.setOnDismissListener { exit(homescreen, window, drawer) }
            dialog!!.show()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                homescreen.systemGestureExclusionRects = listOf()
            }
            if (Settings["kustom:variables:enable", false]) {
                Kustom["screen"] = "overview"
            }
        }
    }

    private fun exit(homescreen: View, window: Window, drawer: DrawerView) {
        drawer.state = BottomDrawerBehavior.STATE_COLLAPSED
        val page = homescreen.findViewById<View>(R.id.feed)
        page.animate().scaleX(1f).scaleY(1f).translationY(0f).duration = 400L
        page.setBackgroundColor(0x0)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Settings["gesture:back", ""] == "") {
            homescreen.systemGestureExclusionRects = listOf(Rect(0, 0, Device.displayWidth, Device.displayHeight))
        }
        isActive = false
        if (Settings["kustom:variables:enable", false]) {
            Kustom["screen"] = "home"
        }
    }
}