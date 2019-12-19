package posidon.launcher.tutorial

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.customizations.FakeLauncherActivity
import posidon.launcher.tools.Tools


class Tutorial : AppCompatActivity() {

    private val stylebtns = intArrayOf(R.id.stylepixel, R.id.styleoneui, R.id.styleios, R.id.styleposidon)
    private var selectedstyle = -1
    private var done = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tutorial1)
        for (i in stylebtns.indices) {
            findViewById<View>(stylebtns[i]).setOnClickListener {
                if (selectedstyle != -1) findViewById<View>(stylebtns[selectedstyle]).background = getDrawable(R.drawable.button_bg_round)
                val d = getDrawable(R.drawable.button_bg_round)!!.mutate() as GradientDrawable
                d.setColor(resources.getColor(R.color.accent))
                findViewById<View>(stylebtns[i]).background = d
                selectedstyle = i
                checkdone()
            }
        }
    }

    private fun checkdone() {
        if (selectedstyle != -1) {
            done = true
            val t = findViewById<TextView>(R.id.next)
            t.setTextColor(-0x1)
            t.setBackgroundColor(resources.getColor(R.color.accent))
            window.navigationBarColor = resources.getColor(R.color.accent)
        }
    }

    fun grantNotificationAccess(v: View) {
        if (!NotificationManagerCompat.getEnabledListenerPackages(applicationContext).contains(applicationContext.packageName)) {
            applicationContext.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            checkdone()
        } else
            Toast.makeText(this, "Notification access already granted", Toast.LENGTH_LONG).show()
    }

    fun grantStorageAccess(v: View) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
            }
            checkdone()
        } else
            Toast.makeText(this, "Storage access already granted", Toast.LENGTH_LONG).show()
    }

    fun done1(v: View) {
        if (done) {
            val settings = getDefaultSharedPreferences(applicationContext)
            when (selectedstyle) {
                0 -> settings.edit()
                        .putInt("accent", 0x4285F4)
                        .putInt("icshape", 1)//circle
                        .putBoolean("reshapeicons", true)
                        .putInt("dockcolor", 0x66ffffff)
                        .putInt("dockIconCount", 5)
                        .putBoolean("blur", false)
                        .putInt("blurLayers", 1)
                        .putFloat("blurradius", 15f)
                        .putInt("drawercolor", 0xdde0e0e0.toInt())
                        .putInt("labelColor", 0xee252627.toInt())
                        .putBoolean("labelsenabled", false)
                        .putInt("icsize", 1)
                        .putInt("dockicsize", 1)
                        .putInt("searchcolor", 0xffffffff.toInt())
                        .putInt("searchtxtcolor", 0xff333333.toInt())
                        .putInt("searchhintcolor", 0xff888888.toInt())
                        .putInt("docksearchcolor", 0xffffffff.toInt())
                        .putInt("docksearchtxtcolor", 0xff333333.toInt())
                        .putBoolean("docksearchbarenabled", true)
                        .putInt("feed:card_radius", 15)
                        .putInt("feed:card_bg", 0xffffffff.toInt())
                        .putInt("feed:card_txt_color", 0xff252627.toInt())
                        .putInt("feed:card_layout", 1)
                        .putInt("notificationtitlecolor", 0xff000000.toInt())
                        .putInt("notificationtxtcolor", 0xff888888.toInt())
                        .putInt("notificationbgcolor", 0xffffffff.toInt())
                        .putInt("sortAlgorithm", 1)
                        .apply()
                1 -> settings.edit()
                        .putInt("accent", 0x4297FE)
                        .putInt("icshape", 4)//squircle
                        .putBoolean("reshapeicons", false)
                        .putInt("dockcolor", 0x0)
                        .putInt("dockIconCount", 4)
                        .putBoolean("blur", true)
                        .putInt("blurLayers", 2)
                        .putFloat("blurradius", 15f)
                        .putInt("drawercolor", 0x55000000)
                        .putInt("labelColor", 0xeeffffff.toInt())
                        .putBoolean("labelsenabled", false)
                        .putInt("icsize", 2)
                        .putInt("dockicsize", 2)
                        .putInt("searchcolor", 0x33000000)
                        .putInt("searchtxtcolor", 0xffffffff.toInt())
                        .putInt("searchhintcolor", 0xffffffff.toInt())
                        .putInt("docksearchcolor", 0xffffffff.toInt())
                        .putInt("docksearchtxtcolor", 0xff000000.toInt())
                        .putBoolean("docksearchbarenabled", false)
                        .putInt("feed:card_radius", 25)
                        .putInt("feed:card_bg", 0xffffffff.toInt())
                        .putInt("feed:card_txt_color", 0xff252627.toInt())
                        .putInt("feed:card_layout", 2)
                        .putInt("notificationtitlecolor", 0xff000000.toInt())
                        .putInt("notificationtxtcolor", 0xff000000.toInt())
                        .putInt("notificationbgcolor", 0xffffffff.toInt())
                        .putInt("sortAlgorithm", 1)
                        .apply()
                2 -> settings.edit()
                        .putInt("accent", 0x4DD863)
                        .putInt("icshape", 2)//roundrect
                        .putBoolean("reshapeicons", true)
                        .putInt("dockcolor", 0x66eeeeee)
                        .putInt("dockIconCount", 4)
                        .putBoolean("blur", true)
                        .putInt("blurLayers", 3)
                        .putFloat("blurradius", 15f)
                        .putInt("drawercolor", 0x88111111.toInt())
                        .putInt("labelColor", 0xeeeeeeee.toInt())
                        .putBoolean("labelsenabled", false)
                        .putInt("icsize", 1)
                        .putInt("dockicsize", 1)
                        .putInt("searchcolor", 0x33000000)
                        .putInt("searchtxtcolor", 0xffffffff.toInt())
                        .putInt("searchhintcolor", 0xffffffff.toInt())
                        .putInt("docksearchcolor", 0xffffffff.toInt())
                        .putInt("docksearchtxtcolor", 0xff000000.toInt())
                        .putBoolean("docksearchbarenabled", false)
                        .putInt("feed:card_radius", 25)
                        .putInt("feed:card_bg", 0xdd000000.toInt())
                        .putInt("feed:card_txt_color", 0xddffffff.toInt())
                        .putInt("feed:card_layout", 0)
                        .putInt("notificationtitlecolor", 0xdd000000.toInt())
                        .putInt("notificationtxtcolor", 0x88000000.toInt())
                        .putInt("notificationbgcolor", 0xa8eeeeee.toInt())
                        .putInt("sortAlgorithm", 1)
                        .apply()
            }
            setContentView(R.layout.tutorial2)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                findViewById<View>(R.id.storagecard).visibility = View.GONE
            }
        }
    }

    fun done2(v: View) {
        setContentView(R.layout.tutorial3)
    }

    fun done3(v: View) {
        val settings = getDefaultSharedPreferences(applicationContext)
        settings.edit()
                .putBoolean("init", false)
                .putString("dock", when {
                    Tools.isInstalled("org.mozilla.firefox", packageManager) -> "org.mozilla.firefox/org.mozilla.firefox.App"
                    else -> "com.android.chrome/com.google.android.apps.chrome.Main"
                } + '\n' + when {
                    Tools.isInstalled("social.openbook.app", packageManager) -> "social.openbook.app/social.openbook.app.MainActivity"
                    Tools.isInstalled("com.twitter.android", packageManager) -> "com.twitter.android/com.twitter.android.StartActivity"
                    Tools.isInstalled("com.discord", packageManager) -> "com.discord/com.discord.app.AppActivity\$Main"
                    Tools.isInstalled("com.trello", packageManager) -> "com.trello/com.trello.home.HomeActivity"
                    Tools.isInstalled("com.google.android.apps.playconsole", packageManager) -> "com.google.android.apps.playconsole/com.google.android.apps.playconsole.activity.MainAndroidActivity"
                    else -> ""
                } + '\n' + when {
                    Tools.isInstalled("com.oneplus.camera", packageManager) -> "com.oneplus.camera/com.oneplus.camera.OPCameraActivity"
                    Tools.isInstalled("com.oneplus.contacts", packageManager) -> "com.oneplus.contacts/com.oneplus.contacts.activities.OPPeopleActivity"
                    else -> ""
                } + '\n' + when {
                    Tools.isInstalled("org.thunderdog.challegram", packageManager) -> "org.thunderdog.challegram/org.thunderdog.challegram.MainActivity"
                    Tools.isInstalled("com.whatsapp", packageManager) -> "com.whatsapp/com.whatsapp.Main"
                    Tools.isInstalled("com.oneplus.mms", packageManager) -> "com.oneplus.mms/com.android.mms.ui.ConversationList"
                    else -> "com.android.mms/com.android.mms.ui.ConversationList"
                } + '\n' + when {
                    Tools.isInstalled("com.netflix.mediaclient", packageManager) -> "com.netflix.mediaclient/com.netflix.mediaclient.ui.launch.UIWebViewActivity"
                    Tools.isInstalled("com.hbo.android.app", packageManager) -> "com.hbo.android.app/com.hbo.android.app.bootstrap.ui.StartupActivity"
                    else -> ""
                })
                .apply()
        if (!isDefaultLauncher()) chooseLauncher()
        startActivity(Intent(this, Main::class.java))
        finish()
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(R.anim.fadein, R.anim.appexit)
    }

    override fun onResume() {
        super.onResume()
        checkdone()
    }

    private fun chooseLauncher() {
        val packageManager: PackageManager = packageManager
        val componentName = ComponentName(this, FakeLauncherActivity::class.java)
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        val selector = Intent(Intent.ACTION_MAIN)
        selector.addCategory(Intent.CATEGORY_HOME)
        selector.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(selector)
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP)
    }

    private fun isDefaultLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        return packageManager.resolveActivity(intent, 0)?.resolvePackageName == "posidon.launcher"
    }
}
