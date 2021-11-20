package posidon.launcher.customizations.settingScreens

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.customizations.FakeLauncherActivity
import posidon.launcher.drawable.FastColorDrawable
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.theme.applyFontSetting
import posidon.launcher.tools.vibrate
import posidon.launcher.view.Spinner
import java.io.FileNotFoundException
import kotlin.system.exitProcess

class CustomOther : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_other)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) window.setDecorFitsSystemWindows(false)
        else window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)
        window.setBackgroundDrawable(FastColorDrawable(Global.getBlackAccent()))

        TextViewCompat.setCompoundDrawableTintList(findViewById(R.id.app_open_anim_label), ColorStateList.valueOf(Global.getPastelAccent()))

        val hapticbar = findViewById<SeekBar>(R.id.hapticbar)
        hapticbar.progress = Settings["hapticfeedback", 14]
        hapticbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Settings["hapticfeedback"] = seekBar.progress
                vibrate()
            }
        })

        findViewById<Spinner>(R.id.animationOptions).apply {
            data = resources.getStringArray(R.array.animationNames)
            selectionI = when(Settings["anim:app_open", "posidon"]) {
                "scale_up" -> 2
                "clip_reveal" -> 1
                else -> 0
            }
            setSelectionChangedListener {
                Settings["anim:app_open"] = when(selectionI) {
                    2 -> "scale_up"
                    1 -> "clip_reveal"
                    else -> "posidon"
                }
            }
        }

        Global.customized = true
    }

    override fun onPause() {
        Global.customized = true
        Settings.apply {
            apply()
        }
        super.onPause()
    }

    fun openHideApps(v: View) = startActivity(Intent(this, CustomHiddenApps::class.java))
    fun stop(v: View): Unit = exitProcess(0)
    fun mkBackup(v: View) = Settings.saveBackup()
    fun useBackup(v: View) {
        val intent = Intent()
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/*"
        startActivityForResult(intent, 1)
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

    fun chooseLauncher(v: View) {
        val packageManager: PackageManager = packageManager
        val componentName = ComponentName(this, FakeLauncherActivity::class.java)
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        val selector = Intent(Intent.ACTION_MAIN)
        selector.addCategory(Intent.CATEGORY_HOME)
        selector.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(selector)
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP)
    }
}