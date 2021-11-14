package posidon.launcher.tutorial

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import posidon.launcher.Home
import posidon.launcher.R
import posidon.launcher.customizations.FakeLauncherActivity
import posidon.launcher.customizations.IconPackPicker
import posidon.launcher.feed.news.chooser.FeedChooser
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import java.io.FileNotFoundException
import java.lang.ref.WeakReference


class Tutorial : AppCompatActivity() {

    private val styleButtons = intArrayOf(R.id.stylepixel, R.id.styleoneui, R.id.styleios, R.id.styleposidon)
    private var selectedStyle = -1
    private var done = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tutorial1)
        Tools.appContextReference = WeakReference(applicationContext)
        Settings.init(applicationContext)
        for (i in styleButtons.indices) {
            findViewById<View>(styleButtons[i]).setOnClickListener {
                if (selectedStyle != -1) {
                    findViewById<TextView>(if (selectedStyle == styleButtons.size) R.id.backup else styleButtons[selectedStyle]).apply {
                        background = getDrawable(R.drawable.button_bg_round)
                        setTextColor(0xffdddddd.toInt())
                    }
                }
                val d = getDrawable(R.drawable.button_bg_round)!!.mutate() as GradientDrawable
                d.setColor(getColor(R.color.accent))
                it as TextView
                it.background = d
                it.setTextColor(0xffddeeff.toInt())
                selectedStyle = i
                checkDone()
            }
        }
        findViewById<View>(R.id.backup).setOnClickListener {
            if (selectedStyle != -1) {
                findViewById<TextView>(if (selectedStyle == styleButtons.size) R.id.backup else styleButtons[selectedStyle]).apply {
                    background = getDrawable(R.drawable.button_bg_round)
                    setTextColor(0xffdddddd.toInt())
                }
            }
            val d = getDrawable(R.drawable.button_bg_round)!!.mutate() as GradientDrawable
            d.setColor(getColor(R.color.accent))
            it as TextView
            it.background = d
            it.setTextColor(0xffddeeff.toInt())
            selectedStyle = styleButtons.size
            checkDone()
        }
    }

    private fun checkDone() {
        if (selectedStyle != -1) {
            done = true
            val t = findViewById<TextView>(R.id.next)
            t.setTextColor(-0x1)
            t.setBackgroundColor(getColor(R.color.accent))
            window.navigationBarColor = getColor(R.color.accent)
        }
    }

    fun grantNotificationAccess(v: View) {
        if (!NotificationManagerCompat.getEnabledListenerPackages(applicationContext).contains(applicationContext.packageName)) {
            applicationContext.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            checkDone()
        } else Toast.makeText(this, "Notification access already granted", Toast.LENGTH_LONG).show()
    }

    fun grantStorageAccess(v: View) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
            checkDone()
        } else
            Toast.makeText(this, "Storage access already granted", Toast.LENGTH_LONG).show()
    }

    fun grantContactsAccess(v: View) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 0)
            checkDone()
        } else
            Toast.makeText(this, "Storage access already granted", Toast.LENGTH_LONG).show()
    }

    fun done1(v: View) {
        if (done) Settings.apply {
            when (selectedStyle) {
                0 -> { // Pixel
                    putNotSave("accent", 0x4285F4)
                    putNotSave("icshape", 1)
                    putNotSave("reshapeicons", true)
                    putNotSave("dock:background_color", 0xffffff)
                    putNotSave("dock:background_type", 0)
                    putNotSave("dock:columns", 5)
                    putNotSave("blurLayers", 1)
                    putNotSave("drawer:blur", false)
                    putNotSave("drawer:blur:rad", 15f)
                    putNotSave("drawer:background_color", 0xefefefef.toInt())
                    putNotSave("drawer:sorting", 0)
                    putNotSave("drawer:labels:color", 0xee252627.toInt())
                    putNotSave("labelsenabled", true)
                    putNotSave("drawer:icons:size", 64)
                    putNotSave("drawer:columns", 5)
                    putNotSave("dock:icons:size", 64)
                    putNotSave("search:blur", false)
                    putNotSave("searchUiBg", 0xefefefef.toInt())
                    putNotSave("searchcolor", 0xbfffffff.toInt())
                    putNotSave("searchtxtcolor", 0xff333333.toInt())
                    putNotSave("searchhintcolor", 0xff888888.toInt())
                    putNotSave("docksearchcolor", 0xffffffff.toInt())
                    putNotSave("docksearchtxtcolor", 0xff333333.toInt())
                    putNotSave("docksearchbarenabled", true)
                    putNotSave("feed:card_radius", 15)
                    putNotSave("feed:card_bg", 0xffffffff.toInt())
                    putNotSave("feed:card_txt_color", 0xff252627.toInt())
                    putNotSave("feed:card_layout", 1)
                    putNotSave("feed:card_margin_x", 16)
                    putNotSave("notificationtitlecolor", 0xff000000.toInt())
                    putNotSave("notificationtxtcolor", 0xff888888.toInt())
                    putNotSave("notificationbgcolor", 0xffffffff.toInt())
                    putNotSave("icon:tint_white_bg", false)
                    apply()
                }
                1 -> { // One UI
                    putNotSave("accent", 0x4297FE)
                    putNotSave("icshape", 4)
                    putNotSave("reshapeicons", false)
                    putNotSave("dock:background_color", 0x0)
                    putNotSave("dock:background_type", 0)
                    putNotSave("dock:columns", 4)
                    putNotSave("folderBG", 0xefefefef.toInt())
                    putNotSave("folder:labels:color", 0xdd000000.toInt())
                    putNotSave("folder:title_color", 0xff000000.toInt())
                    putNotSave("blurLayers", 2)
                    putNotSave("drawer:blur", true)
                    putNotSave("drawer:blur:rad", 15f)
                    putNotSave("drawer:background_color", 0x55000000)
                    putNotSave("drawer:sorting", 0)
                    putNotSave("drawer:labels:color", 0xeeffffff.toInt())
                    putNotSave("labelsenabled", false)
                    putNotSave("drawer:icons:size", 82)
                    putNotSave("drawer:columns", 4)
                    putNotSave("dock:icons:size", 82)
                    putNotSave("searchcolor", 0x33000000)
                    putNotSave("searchtxtcolor", 0xffffffff.toInt())
                    putNotSave("searchhintcolor", 0xffffffff.toInt())
                    putNotSave("docksearchcolor", 0xffffffff.toInt())
                    putNotSave("docksearchtxtcolor", 0xff000000.toInt())
                    putNotSave("docksearchbarenabled", false)
                    putNotSave("feed:card_radius", 25)
                    putNotSave("feed:card_bg", 0xffffffff.toInt())
                    putNotSave("feed:card_txt_color", 0xff252627.toInt())
                    putNotSave("feed:card_layout", 2)
                    putNotSave("feed:card_margin_x", 0)
                    putNotSave("notificationtitlecolor", 0xff000000.toInt())
                    putNotSave("notificationtxtcolor", 0xff000000.toInt())
                    putNotSave("notificationbgcolor", 0xffffffff.toInt())
                    putNotSave("icon:tint_white_bg", false)
                    apply()
                }
                2 -> { // iOS
                    putNotSave("accent", 0x4DD863)
                    putNotSave("icshape", 2)
                    putNotSave("reshapeicons", true)
                    putNotSave("dock:background_color", 0x55ededed)
                    putNotSave("dock:background_type", 2)
                    putNotSave("dock:columns", 4)
                    putNotSave("folderBG", 0x55efefef)
                    putNotSave("blurLayers", 3)
                    putNotSave("drawer:blur", true)
                    putNotSave("drawer:blur:rad", 15f)
                    putNotSave("drawer:background_color", 0x88111111.toInt())
                    putNotSave("drawer:sorting", 0)
                    putNotSave("drawer:labels:color", 0xddeeeeee.toInt())
                    putNotSave("labelsenabled", true)
                    putNotSave("drawer:icons:size", 82)
                    putNotSave("drawer:columns", 4)
                    putNotSave("dock:icons:size", 82)
                    putNotSave("searchcolor", 0x33000000)
                    putNotSave("searchtxtcolor", 0xffffffff.toInt())
                    putNotSave("searchhintcolor", 0xffffffff.toInt())
                    putNotSave("docksearchcolor", 0xffffffff.toInt())
                    putNotSave("docksearchtxtcolor", 0xff000000.toInt())
                    putNotSave("docksearchbarenabled", false)
                    putNotSave("feed:card_radius", 20)
                    putNotSave("feed:card_bg", 0xdd000000.toInt())
                    putNotSave("feed:card_txt_color", 0xddffffff.toInt())
                    putNotSave("feed:card_layout", 0)
                    putNotSave("feed:card_margin_x", 20)
                    putNotSave("feed:card_margin_y", 10)
                    putNotSave("notificationtitlecolor", 0xdd000000.toInt())
                    putNotSave("notificationtxtcolor", 0x88000000.toInt())
                    putNotSave("notificationbgcolor", 0xa8ededed.toInt())
                    putNotSave("icon:tint_white_bg", false)
                    putNotSave("icon:background", 0xffffffff.toInt())
                    apply()
                }
                4 -> {
                    val intent = Intent()
                    intent.action = Intent.ACTION_OPEN_DOCUMENT
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "application/*"
                    startActivityForResult(intent, 1)
                }
            }
            setContentView(R.layout.tutorial2)
        }
    }

    fun done2(v: View) {
        setContentView(R.layout.tutorial3)
        Tools.updateNavbarHeight(this)
    }

    fun done3(v: View) {
        Settings.apply {
            putNotSave("dock", when {
                Tools.isInstalled("org.mozilla.fenix", packageManager) -> "org.mozilla.fenix/org.mozilla.fenix.App"
                Tools.isInstalled("org.mozilla.firefox", packageManager) -> "org.mozilla.firefox/org.mozilla.firefox.App"
                else -> "com.android.chrome/com.google.android.apps.chrome.Main"
            } + '\n' + when {
                Tools.isInstalled("social.openbook.app", packageManager) -> "social.openbook.app/social.openbook.app.MainActivity"
                Tools.isInstalled("com.twitter.android", packageManager) -> "com.twitter.android/com.twitter.android.StartActivity"
                Tools.isInstalled("com.discord", packageManager) -> "com.discord/com.discord.app.AppActivity\$Main"
                Tools.isInstalled("com.trello", packageManager) -> "com.trello/com.trello.home.HomeActivity"
                Tools.isInstalled("com.topjohbwu.magisk", packageManager) -> "com.topjohbwu.magisk/a.c"
                Tools.isInstalled("com.google.android.apps.playconsole", packageManager) -> "com.google.android.apps.playconsole/com.google.android.apps.playconsole.activity.MainAndroidActivity"
                else -> ""
            } + '\n' + when {
                Tools.isInstalled("com.oneplus.camera", packageManager) -> "com.oneplus.camera/com.oneplus.camera.OPCameraActivity"
                Tools.isInstalled("com.oneplus.contacts", packageManager) -> "com.oneplus.contacts/com.oneplus.contacts.activities.OPPeopleActivity"
                else -> ""
            } + '\n' + when {
                Tools.isInstalled("org.thunderdog.challegram", packageManager) -> "org.thunderdog.challegram/org.thunderdog.challegram.MainActivity"
                Tools.isInstalled("org.telegram.messenger", packageManager) -> "org.telegram.messenger/org.telegram.ui.LaunchActivity"
                Tools.isInstalled("com.whatsapp", packageManager) -> "com.whatsapp/com.whatsapp.Main"
                Tools.isInstalled("com.oneplus.mms", packageManager) -> "com.oneplus.mms/com.android.mms.ui.ConversationList"
                else -> "com.android.mms/com.android.mms.ui.ConversationList"
            } + '\n' + when {
                Tools.isInstalled("com.netflix.mediaclient", packageManager) -> "com.netflix.mediaclient/com.netflix.mediaclient.ui.launch.UIWebViewActivity"
                Tools.isInstalled("com.hbo.android.app", packageManager) -> "com.hbo.android.app/com.hbo.android.app.bootstrap.ui.StartupActivity"
                Tools.isInstalled("com.aspiro.tidal", packageManager) -> "com.aspiro.tidal/com.aspiro.wamp.LoginFragmentActivity"
                else -> ""
            })
            apply()
        }
        setContentView(R.layout.tutorial4)
    }

    fun done4(v: View) {
        Settings.apply {
            putNotSave("init", false)
            apply()
        }
        if (!Tools.isDefaultLauncher) chooseLauncher()
        startActivity(Intent(this, Home::class.java))
        finish()
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(R.anim.fadein, R.anim.appexit)
    }

    override fun onResume() {
        super.onResume()
        checkDone()
    }

    fun chooseFeeds(v: View) { startActivity(Intent(this, FeedChooser::class.java)) }
    fun iconPackSelector(v: View) { startActivity(Intent(this, IconPackPicker::class.java)) }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                try { data?.data?.let {
                    Settings.restoreFromBackup(it)
                    Toast.makeText(this, "Backup restored!", Toast.LENGTH_LONG).show()
                }}
                catch (e: FileNotFoundException) { e.printStackTrace() }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
