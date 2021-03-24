package posidon.launcher.desktop

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import posidon.android.conveniencelib.Graphics
import posidon.launcher.R
import posidon.launcher.items.users.AppLoader
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import java.lang.ref.WeakReference

class DesktopMode : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.desktop)
        Tools.appContextReference = WeakReference(applicationContext)
        Settings.init(applicationContext)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        val menuBtn = findViewById<ImageView>(R.id.menuBtn)
        Graphics.tryAnimate(this, menuBtn.drawable)
        AppLoader(this) {}.execute()
    }

    fun showMenu(v: View?) = startActivity(Intent(this, AppList::class.java))
}