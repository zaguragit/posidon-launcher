/*
 * Copyright (c) 2019 Leo Shneyderis
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


class CustomDock : AppCompatActivity() {

    private var icsize: SeekBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Tools.applyFontSetting(this)
        setContentView(R.layout.custom_dock)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)

        icsize = findViewById(R.id.dockiconsizeslider)
        icsize!!.progress = Settings.getInt("dockicsize", 1)

        val iccount = findViewById<SeekBar>(R.id.columnSlider)
        iccount!!.progress = Settings.getInt("dockIconCount", 5)
        val c = findViewById<TextView>(R.id.iccountnum)
        c.text = Settings.getInt("dockIconCount", 5).toString()
        iccount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                c.text = progress.toString()
                Settings.putInt("dockIconCount", iccount.progress)
            }
        })

        val rowCount = findViewById<SeekBar>(R.id.dockRowCountSlider)
        rowCount!!.progress = Settings.getInt("dockRowCount", 1) - 1
        val c2 = findViewById<TextView>(R.id.icRowNum)
        c2.text = Settings.getInt("dockRowCount", 1).toString()
        rowCount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                c2.text = (progress + 1).toString()
                Settings.putInt("dockRowCount", rowCount.progress + 1)
            }
        })

        val docklabelswitch = findViewById<Switch>(R.id.labelsEnabled)
        docklabelswitch.isChecked = Settings.getBool("dockLabelsEnabled", false)

        findViewById<View>(R.id.bgColorPrev).background = ColorTools.colorcircle(Settings.getInt("dockcolor", -0x78000000))
        findViewById<View>(R.id.labelColorPrev).background = ColorTools.colorcircle(Settings.getInt("dockLabelColor", -0x11111112))

        (findViewById<View>(R.id.radiusSlider) as SeekBar).progress = Settings.getInt("dockradius", 30)

        val bottompadding = findViewById<SeekBar>(R.id.bottompaddingslider)
        bottompadding.progress = Settings.getInt("dockbottompadding", 10)
        (findViewById<View>(R.id.bottompadding) as TextView).text = Settings.getInt("dockbottompadding", 10).toString()
        bottompadding.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                (findViewById<View>(R.id.bottompadding) as TextView).text = progress.toString()
                Settings.putInt("dockbottompadding", progress)
            }
        })
        Main.customized = true
    }

    fun pickColor(v: View) { ColorTools.pickColor(this, "dockcolor", -0x78000000) }
    fun pickLabelColor(v: View) { ColorTools.pickColor(this, "dockLabelColor", -0x11111112) }

    override fun onPause() {
        Settings.putInt("dockicsize", icsize!!.progress)
        val docklabelswitch = findViewById<Switch>(R.id.labelsEnabled)
        Settings.putBool("dockLabelsEnabled", docklabelswitch.isChecked)
        Settings.putInt("dockradius", (findViewById<View>(R.id.radiusSlider) as SeekBar).progress)
        super.onPause()
    }
}