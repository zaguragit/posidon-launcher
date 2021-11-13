package posidon.launcher.desktop

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import io.posidon.android.launcherutils.appLoading.AppLoader
import io.posidon.android.launcherutils.appLoading.IconConfig
import posidon.android.conveniencelib.Graphics
import posidon.android.conveniencelib.dp
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.items.users.AppCollection
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import java.lang.ref.WeakReference

@RequiresApi(Build.VERSION_CODES.Q)
class DesktopMode : FragmentActivity() {

    val appLoader = AppLoader(::AppCollection)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.desktop)
        Tools.appContextReference = WeakReference(applicationContext)
        Settings.init(applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        val menuBtn = findViewById<ImageView>(R.id.menuBtn)
        Graphics.tryAnimate(this, menuBtn.drawable)
        loadApps()
    }

    fun loadApps() {
        val iconConfig = IconConfig(
            size = dp(65).toInt(),
            density = resources.configuration.densityDpi,
            packPackages = arrayOf(Settings["iconpack", "system"]),
        )
        appLoader.async(applicationContext, iconConfig) {
            App.onFinishLoad(it.list, it.sections, it.hidden, it.byName)
        }
    }

    fun showMenu(v: View?) = startActivity(Intent(this, AppList::class.java))
}