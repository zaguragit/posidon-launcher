/*
 * Copyright (c) 2020 Leo Shneyderis
 * All rights reserved
 */

package posidon.launcher.customizations

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.tools.ColorTools
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.applyFontSetting


class CustomFolders : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_folders)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)

        val columnSlider = findViewById<SeekBar>(R.id.columnSlider)
        columnSlider!!.progress = Settings["folderColumns", 3]
        val c = findViewById<TextView>(R.id.columnNum)
        c.text = Settings["folderColumns", 3].toString()
        columnSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                c.text = progress.toString()
                Settings["folderColumns"] = columnSlider.progress
            }
        })

        findViewById<View>(R.id.bgColorPrev).background = ColorTools.colorCircle(Settings["folderBG", -0x22eeeded])
        findViewById<View>(R.id.labelColorPrev).background = ColorTools.colorCircle(Settings["folder:label_color", -0x22000001])
        findViewById<Switch>(R.id.labelsEnabled).isChecked = Settings["folderLabelsEnabled", false]
        findViewById<View>(R.id.titleColorPrev).background = ColorTools.colorCircle(Settings["folder:title_color", 0xffffffff.toInt()])
        findViewById<Switch>(R.id.titleEnabled).isChecked = Settings["folder:show_title", true]
        findViewById<SeekBar>(R.id.radiusSlider).progress = Settings["folderCornerRadius", 18]
        Main.customized = true
    }

    fun pickColor(v: View) = ColorTools.pickColor(this, Settings["folderBG", -0x22eeeded]) {
        v as ViewGroup
        v.getChildAt(1).background = ColorTools.colorCircle(it)
        Settings["folderBG"] = it
    }

    fun pickLabelColor(v: View) = ColorTools.pickColor(this, Settings["folder:label_color", -0x22000001]) {
        v as ViewGroup
        v.getChildAt(1).background = ColorTools.colorCircle(it)
        Settings["folder:label_color"] = it
    }

    fun pickTitleColor(v: View) = ColorTools.pickColor(this, Settings["folder:title_color", 0xffffffff.toInt()]) {
        v as ViewGroup
        v.getChildAt(1).background = ColorTools.colorCircle(it)
        Settings["folder:title_color"] = it
    }

    override fun onPause() {
        Main.customized = true
        Settings.apply {
            putNotSave("folderLabelsEnabled", findViewById<Switch>(R.id.labelsEnabled).isChecked)
            putNotSave("folder:show_title", findViewById<Switch>(R.id.titleEnabled).isChecked)
            putNotSave("folderCornerRadius", findViewById<SeekBar>(R.id.radiusSlider).progress)
            apply()
        }
        super.onPause()
    }
}