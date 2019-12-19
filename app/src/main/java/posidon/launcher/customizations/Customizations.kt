/*
 * Copyright (c) 2019 Leo Shneyderis
 * All rights reserved
 */

package posidon.launcher.customizations

import android.animation.Animator
import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.tools.Settings
import posidon.launcher.tools.Tools

class Customizations : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings.init(this)
        Tools.applyFontSetting(this)
        setContentView(R.layout.customizations)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        if (Settings.getBool("devOptionsEnabled", false)) findViewById<View>(R.id.devoptions).visibility = View.VISIBLE
        findViewById<View>(R.id.catlist).setPadding(0, 0, 0, Tools.navbarHeight)
        cardThing()
    }

    fun openApps(v: View) { startActivity(Intent(this, CustomDrawer::class.java)) }
    fun openDock(v: View) { startActivity(Intent(this, CustomDock::class.java)) }
    fun openHome(v: View) { startActivity(Intent(this, CustomHome::class.java)) }
    fun openSearch(v: View) { startActivity(Intent(this, CustomSearch::class.java)) }
    fun openFolders(v: View) { startActivity(Intent(this, CustomFolders::class.java)) }
    fun openTheme(v: View) { startActivity(Intent(this, CustomTheme::class.java)) }
    fun openOther(v: View) { startActivity(Intent(this, CustomOther::class.java)) }
    fun openDev(v: View) { startActivity(Intent(this, CustomDev::class.java)) }
    fun openAbout(v: View) { startActivity(Intent(this, About::class.java)) }

    fun hideCard(v: View) {
        Settings.putBool("rated", true)
        findViewById<View>(R.id.card).animate().alpha(0f).scaleX(0.95f).scaleY(0.95f).translationY(findViewById<View>(R.id.card).measuredHeight.toFloat()).setDuration(200L).setListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(a: Animator) {}
            override fun onAnimationCancel(a: Animator) {}
            override fun onAnimationStart(a: Animator) {}
            override fun onAnimationEnd(a: Animator) {
                findViewById<View>(R.id.card).visibility = View.GONE
            }
        })
    }

    private fun cardThing() {
        if (Main.customized && !Settings.getBool("rated", false)) {
            findViewById<View>(R.id.card).visibility = View.VISIBLE
            findViewById<View>(R.id.yesBtn).setOnClickListener {
                val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                startActivity(i, ActivityOptions.makeCustomAnimation(this, R.anim.slideup, R.anim.slidedown).toBundle())
                Settings.putBool("rated", true)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Settings.getBool("devOptionsEnabled", false)) findViewById<View>(R.id.devoptions).visibility = View.VISIBLE
        else findViewById<View>(R.id.devoptions).visibility = View.GONE
        cardThing()
        System.gc()
    }
}
