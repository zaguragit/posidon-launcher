package posidon.launcher.customizations.settingScreens

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.feed.order.FeedOrderActivity
import posidon.launcher.storage.Settings
import posidon.launcher.view.setting.configureWindowForSettings

class CustomHome : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings.init(applicationContext)
        configureWindowForSettings()
        setContentView(R.layout.custom_home)
        Global.customized = true
    }

    override fun onPause() {
        Global.customized = true
        super.onPause()
    }

    fun openFeedOrder(v: View) = startActivity(Intent(this, FeedOrderActivity::class.java))
}