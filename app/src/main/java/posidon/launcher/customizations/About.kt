package posidon.launcher.customizations

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import posidon.launcher.BuildConfig
import posidon.launcher.R
import posidon.launcher.tools.Loader
import posidon.launcher.tools.Settings
import posidon.launcher.tools.Tools

class About : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Tools.applyFontSetting(this)
        setContentView(R.layout.custom_about)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)
        val description = findViewById<TextView>(R.id.appname)
        description.text = getString(R.string.app_name) + " - " + BuildConfig.VERSION_NAME
        Loader.bitmap("https://pbs.twimg.com/profile_images/1177585321636028416/2QGnyOET_400x400.jpg", Loader.bitmap.Listener { img -> findViewById<ImageView>(R.id.sajidshaikprofile).setImageBitmap(img) }).execute()
        Tools.animate(findViewById<ImageView>(R.id.devprofile).drawable)

        findViewById<View>(R.id.maincard).setOnLongClickListener {
            if (Settings.getBool("devOptionsEnabled", false)) {
                Settings.putBool("devOptionsEnabled", false)
                Toast.makeText(this@About, "Developer mode disabled", Toast.LENGTH_SHORT).show()
            } else {
                Settings.putBool("devOptionsEnabled", true)
                Toast.makeText(this@About, "Developer mode enabled", Toast.LENGTH_SHORT).show()
            }
            true
        }

        try { findViewById<ImageView>(R.id.img).setImageResource(R.drawable.logo_wide) } catch (ignore: Exception) {}
    }

    fun openTwitter(v: View) {
        val uri = Uri.parse("https://mobile.twitter.com/lposidon")
        val i = Intent(Intent.ACTION_VIEW, uri)
        startActivity(i, ActivityOptions.makeCustomAnimation(this, R.anim.slideup, R.anim.slidedown).toBundle())
    }

    fun openTelegram(v: View) { try {
        val uri = Uri.parse("https://t.me/posidonlauncher")
        val i = Intent(Intent.ACTION_VIEW, uri)
        startActivity(i, ActivityOptions.makeCustomAnimation(this, R.anim.slideup, R.anim.slidedown).toBundle())
    } catch (ignore: Exception) {} }

    fun openWebsite(v: View) {
        val uri = Uri.parse("https://leoxshn.github.io/posidon-web")
        val i = Intent(Intent.ACTION_VIEW, uri)
        startActivity(i, ActivityOptions.makeCustomAnimation(this, R.anim.slideup, R.anim.slidedown).toBundle())
    }
}