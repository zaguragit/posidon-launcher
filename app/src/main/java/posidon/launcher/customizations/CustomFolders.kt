/*
 * Copyright (c) 2020 Leo Shneyderis
 * All rights reserved
 */

package posidon.launcher.customizations

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.tools.ColorTools
import posidon.launcher.tools.Settings
import posidon.launcher.tools.Tools


class CustomFolders : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Tools.applyFontSetting(this)
        setContentView(R.layout.custom_folders)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)

        val columnSlider = findViewById<SeekBar>(R.id.columnSlider)
        columnSlider!!.progress = Settings.getInt("folderColumns", 3)
        val c = findViewById<TextView>(R.id.columnNum)
        c.text = Settings.getInt("folderColumns", 3).toString()
        columnSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                c.text = progress.toString()
                Settings.putInt("folderColumns", columnSlider.progress)
            }
        })

        findViewById<View>(R.id.bgColorPrev).background = ColorTools.colorcircle(Settings.getInt("folderBG", -0x22eeeded))
        findViewById<View>(R.id.labelColorPrev).background = ColorTools.colorcircle(Settings.getInt("folder:label_color", -0x22000001))
        findViewById<Switch>(R.id.labelsEnabled).isChecked = Settings.getBool("folderLabelsEnabled", false)
        findViewById<SeekBar>(R.id.radiusSlider).progress = Settings.getInt("folderCornerRadius", 18)
        Main.customized = true
    }

    fun pickColor(v: View) { ColorTools.pickColor(this, "folderBG", -0x22eeeded) }
    fun pickLabelColor(v: View) { ColorTools.pickColor(this, "folder:label_color", -0x22000001) }

    override fun onPause() {
        Settings.putBool("folderLabelsEnabled", findViewById<Switch>(R.id.labelsEnabled).isChecked)
        Settings.putInt("folderCornerRadius", (findViewById<View>(R.id.radiusSlider) as SeekBar).progress)
        super.onPause()
    }
}