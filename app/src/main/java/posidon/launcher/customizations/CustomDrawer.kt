/*
 * Copyright (c) 2019 Leo Shneyderis
 * All rights reserved
 */

package posidon.launcher.customizations

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.tools.ColorTools
import posidon.launcher.tools.Settings
import posidon.launcher.tools.Tools


class CustomDrawer : AppCompatActivity() {

    private var icsize: SeekBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Tools.applyFontSetting(this)
        setContentView(R.layout.custom_drawer)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)

        icsize = findViewById(R.id.iconsizeslider)
        icsize!!.progress = Settings.getInt("icsize", 1)

        val columnslider = findViewById<SeekBar>(R.id.columnslider)
        columnslider.progress = Settings.getInt("numcolumns", 4) - 1
        val c = findViewById<TextView>(R.id.columnnum)
        c.text = Settings.getInt("numcolumns", 4).toString()
        columnslider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                c.text = (progress + 1).toString()
                Settings.putInt("numcolumns", progress + 1)
            }
        })

        val verticalspacingslider = findViewById<SeekBar>(R.id.verticalspacingslider)
        verticalspacingslider.progress = Settings.getInt("verticalspacing", 12)
        val verticalspacingnum = findViewById<TextView>(R.id.verticalspacingnum)
        verticalspacingnum.text = Settings.getInt("verticalspacing", 12).toString()
        verticalspacingslider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                verticalspacingnum.text = progress.toString()
                Settings.putInt("verticalspacing", progress)
            }
        })

        findViewById<Switch>(R.id.labelsenabled).isChecked = Settings.getBool("labelsenabled", false)
        findViewById<View>(R.id.bgColorPrev).background = ColorTools.colorcircle(Settings.getInt("drawercolor", -0x78000000))
        findViewById<View>(R.id.labelColorPrev).background = ColorTools.colorcircle(Settings.getInt("labelColor", 0xeeeeeeee.toInt()))

        findViewById<Spinner>(R.id.sortingOptions).setSelection(Settings.getInt("sortAlgorithm", 1))

        findViewById<Switch>(R.id.blurswitch).isChecked = Settings.getBool("blur", true)
        val blurSlider = findViewById<SeekBar>(R.id.blurSlider)
        blurSlider.progress = Settings.getFloat("blurradius", 15f).toInt()
        val blurNum = findViewById<TextView>(R.id.blurNum)
        blurNum.text = Settings.getFloat("blurradius", 15f).toInt().toString()
        blurSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                window.setBackgroundDrawable(BitmapDrawable(resources, Tools.blurredWall(this@CustomDrawer, progress.toFloat())))
                Settings.putFloat("blurradius", progress.toFloat())
                blurNum.text = progress.toString()
            }
        })

        val blurLayerSlider = findViewById<SeekBar>(R.id.blurLayerSlider)
        blurLayerSlider.progress = Settings.getInt("blurLayers", 1) - 1
        val blurLayerNum = findViewById<TextView>(R.id.blurLayerNum)
        blurLayerNum.text = Settings.getInt("blurLayers", 1).toString()
        blurLayerSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                blurLayerNum.text = (progress + 1).toString()
                Settings.putInt("blurLayers", progress + 1)
            }
        })
        Main.customized = true
    }

    fun pickBGColor(v: View) { ColorTools.pickColor(this, "drawercolor", -0x78000000) }
    fun pickLabelColor(v: View) { ColorTools.pickColor(this, "labelColor", 0xeeeeeeee.toInt()) }

    override fun onPause() {
        Settings.putInt("icsize", icsize!!.progress)
        Settings.putBool("labelsenabled", findViewById<Switch>(R.id.labelsenabled).isChecked)
        if (Settings.getInt("sortAlgorithm", 1) != findViewById<Spinner>(R.id.sortingOptions).selectedItemPosition) {
            Settings.putInt("sortAlgorithm", findViewById<Spinner>(R.id.sortingOptions).selectedItemPosition)
            Main.shouldSetApps = true
        }
        Settings.putBool("blur", findViewById<Switch>(R.id.blurswitch).isChecked)
        super.onPause()
    }
}