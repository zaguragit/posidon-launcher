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


class CustomSearch : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Tools.applyFontSetting(this)
        setContentView(R.layout.custom_search)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)

        findViewById<Switch>(R.id.drawersearchbar).isChecked = Settings.getBool("drawersearchbarenabled", true)
        findViewById<View>(R.id.searchcolorprev).background = ColorTools.colorcircle(Settings.getInt("searchcolor", 0x33000000))
        findViewById<View>(R.id.searchtxtcolorprev).background = ColorTools.colorcircle(Settings.getInt("searchtxtcolor", -0x1))
        findViewById<View>(R.id.searchhintcolorprev).background = ColorTools.colorcircle(Settings.getInt("searchhintcolor", -0x1))
        findViewById<TextView>(R.id.hinttxt).text = Settings.getString("searchhinttxt", "Search..")
        findViewById<SeekBar>(R.id.searchradiusslider).progress = Settings.getInt("searchradius", 0)
        findViewById<Switch>(R.id.docksearchbar).isChecked = Settings.getBool("docksearchbarenabled", false)
        findViewById<View>(R.id.docksearchcolorprev).background = ColorTools.colorcircle(Settings.getInt("docksearchcolor", -0x22000001))
        findViewById<View>(R.id.docksearchtxtcolorprev).background = ColorTools.colorcircle(Settings.getInt("docksearchtxtcolor", -0x1000000))
        findViewById<SeekBar>(R.id.docksearchradiusslider).progress = Settings.getInt("docksearchradius", 30)
        findViewById<View>(R.id.uiBgColorPrev).background = ColorTools.colorcircle(Settings.getInt("searchUiBg", -0x78000000))
        Main.customized = true
    }

    fun picksearchcolor(v: View) { ColorTools.pickColor(this, "searchcolor", 0x33000000) }
    fun picksearchtxtcolor(v: View) { ColorTools.pickColor(this, "searchtxtcolor", -0x1) }
    fun picksearchhintcolor(v: View) { ColorTools.pickColor(this, "searchhintcolor", -0x1) }
    fun pickdocksearchcolor(v: View) { ColorTools.pickColor(this, "docksearchcolor", -0x22000001) }
    fun pickdocksearchtxtcolor(v: View) { ColorTools.pickColor(this, "docksearchtxtcolor", -0x1000000) }
    fun pickSearchUiBgColor(v: View) { ColorTools.pickColor(this, "searchUiBg", -0x78000000) }

    override fun onPause() {
        Settings.apply {
            putNotSave("searchhinttxt", findViewById<TextView>(R.id.hinttxt).text.toString())
            putNotSave("searchradius", findViewById<SeekBar>(R.id.searchradiusslider).progress)
            putNotSave("docksearchbarenabled", findViewById<Switch>(R.id.docksearchbar).isChecked)
            putNotSave("drawersearchbarenabled", findViewById<Switch>(R.id.drawersearchbar).isChecked)
            apply()
        }
        super.onPause()
    }
}