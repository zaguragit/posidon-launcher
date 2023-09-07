package posidon.launcher.tutorial

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import posidon.launcher.Home
import posidon.launcher.R
import posidon.launcher.customizations.FakeLauncherActivity
import posidon.launcher.feed.news.chooser.FeedChooser
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import java.io.FileNotFoundException
import java.lang.ref.WeakReference


class Tutorial : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setup_permissions)
        Tools.appContextReference = WeakReference(applicationContext)
        Settings.init(applicationContext)
    }

    fun grantNotificationAccess(v: View) {
        if (!NotificationManagerCompat.getEnabledListenerPackages(applicationContext).contains(applicationContext.packageName)) {
            applicationContext.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } else Toast.makeText(this, "Notification access already granted", Toast.LENGTH_LONG).show()
    }

    fun grantStorageAccess(v: View) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        } else
            Toast.makeText(this, "Storage access already granted", Toast.LENGTH_LONG).show()
    }

    fun grantContactsAccess(v: View) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 0)
        } else
            Toast.makeText(this, "Storage access already granted", Toast.LENGTH_LONG).show()
    }

    fun done2(v: View) {
        setContentView(R.layout.setup_settings)
        findViewById<View>(R.id.backup).setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "application/*"
            startActivityForResult(intent, 1)
        }
        Tools.updateNavbarHeight(this)
    }

    fun done3(v: View) {
        setContentView(R.layout.setup_tutorial)
    }

    fun done4(v: View) {
        Settings.apply {
            putNotSave("init", false)
            apply()
        }
        if (!Tools.isDefaultLauncher(v.context.packageManager)) chooseLauncher()
        startActivity(Intent(this, Home::class.java))
        finish()
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(R.anim.fadein, R.anim.appexit)
    }

    fun chooseFeeds(v: View) { startActivity(Intent(this, FeedChooser::class.java)) }

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
