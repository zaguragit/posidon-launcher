/*
 * Copyright (c) 2019 Leo Shneyderis
 * All rights reserved
 */

package posidon.launcher.customizations

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.tools.ColorTools
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.applyFontSetting
import posidon.launcher.view.Spinner


class CustomTheme : AppCompatActivity() {

    private var icShapeViews: Array<View>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_theme)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)
        val pm = packageManager
        icShapeViews = arrayOf(findViewById(R.id.icshapedef), findViewById(R.id.icshaperound), findViewById(R.id.icshaperoundrect), findViewById(R.id.icshaperect), findViewById(R.id.icshapesquircle))

        findViewById<TextView>(R.id.icShapeTxt).setTextColor(Main.accentColor)
        val i = findViewById<TextView>(R.id.iconpackselector)
        try { i.text = pm.getApplicationLabel(pm.getApplicationInfo(Settings["iconpack", "system"], 0)) }
        catch (e: PackageManager.NameNotFoundException) { e.printStackTrace() }

        val fontName = findViewById<TextView>(R.id.fontname)
        when (Settings["font", "lexendDeca"]) {
            "sansserif" -> fontName.text = getString(R.string.sans_serif)
            "posidonsans" -> fontName.text = getString(R.string.posidon_sans)
            "monospace" -> fontName.text = getString(R.string.monospace)
            "ubuntu" -> fontName.text = getString(R.string.ubuntu)
            "lexendDeca" -> fontName.text = getString(R.string.lexend_deca)
            "openDyslexic" -> fontName.text = getString(R.string.open_dyslexic)
        }
        findViewById<View>(R.id.fontbox).setOnClickListener {
            val d = Dialog(this@CustomTheme)
            d.setContentView(R.layout.font_list)
            d.findViewById<View>(R.id.sansserif).setOnClickListener {
                d.dismiss()
                Settings["font"] = "sansserif"
                fontName.text = getString(R.string.sans_serif)
                applyFontSetting()
            }
            d.findViewById<View>(R.id.posidonsans).setOnClickListener {
                d.dismiss()
                Settings["font"] = "posidonsans"
                fontName.text = getString(R.string.posidon_sans)
                applyFontSetting()
            }
            d.findViewById<View>(R.id.monospace).setOnClickListener {
                d.dismiss()
                Settings["font"] = "monospace"
                fontName.text = getString(R.string.monospace)
                applyFontSetting()
            }
            d.findViewById<View>(R.id.ubuntu).setOnClickListener {
                d.dismiss()
                Settings["font"] = "ubuntu"
                fontName.text = getString(R.string.ubuntu)
                applyFontSetting()
            }
            d.findViewById<View>(R.id.lexendDeca).setOnClickListener {
                d.dismiss()
                Settings["font"] = "lexendDeca"
                fontName.text = getString(R.string.lexend_deca)
                applyFontSetting()
            }
            d.findViewById<View>(R.id.open_dyslexic).setOnClickListener {
                d.dismiss()
                Settings["font"] = "openDyslexic"
                fontName.text = getString(R.string.open_dyslexic)
                applyFontSetting()
            }
            d.show()
        }

        findViewById<View>(R.id.accentcolorprev).background = ColorTools.colorcircle(Settings["accent", 0x1155ff] or -0x1000000)
        findViewById<View>(R.id.iconBackground).background = ColorTools.colorcircle(Settings["icon:background", 0xff252627.toInt()])

        findViewById<Switch>(R.id.animatedicons).isChecked = Settings["animatedicons", true]
        findViewById<Switch>(R.id.reshapeicons).isChecked = Settings["reshapeicons", false]

        findViewById<Spinner>(R.id.iconBackgrounds).data = resources.getStringArray(R.array.iconBackgrounds)
        findViewById<Spinner>(R.id.iconBackgrounds).selectionI = when(Settings["icon:background_type", "custom"]) {
            "dominant" -> 0
            "lv" -> 1
            "dv" -> 2
            "lm" -> 3
            "dm" -> 4
            else -> 5
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            findViewById<View>(R.id.icshapesettings).visibility = View.GONE
            findViewById<View>(R.id.recolorWhiteBGs).visibility = View.GONE
        } else {
            icShapeViews!![Settings["icshape", 4]].setBackgroundResource(R.drawable.selection)
            findViewById<Switch>(R.id.recolorWhiteBGsSwitch).isChecked = Settings["icon:tint_white_bg", true]
        }
        Main.shouldSetApps = true
        Main.customized = true
    }

    fun pickAccentColor(v: View) { ColorTools.pickColorNoAlpha(this, "accent", 0x1155ff) }
    fun pickIconBGColor(v: View) { ColorTools.pickColor(this, "icon:background", -0x1) }
    fun iconPackSelector(v: View) { startActivity(Intent(this, IconPackPicker::class.java)) }

    override fun onPause() {
        Main.customized = true
        Settings.apply {
            putNotSave("animatedicons", findViewById<Switch>(R.id.animatedicons).isChecked)
            putNotSave("reshapeicons", findViewById<Switch>(R.id.reshapeicons).isChecked)
            putNotSave("icon:tint_white_bg", findViewById<Switch>(R.id.recolorWhiteBGsSwitch).isChecked)
            putNotSave("icon:background_type", when(findViewById<Spinner>(R.id.iconBackgrounds).selectionI) {
                0 -> "dominant"
                1 -> "lv"
                2 -> "dv"
                3 -> "lm"
                4 -> "dm"
                else -> "custom"
            })
            Main.accentColor = Settings["accent", 0x1155ff] or -0x1000000
            apply()
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        val pm = packageManager
        val i = findViewById<TextView>(R.id.iconpackselector)
        try { i.text = pm.getApplicationLabel(pm.getApplicationInfo(Settings["iconpack", "system"], 0)) }
        catch (e: PackageManager.NameNotFoundException) { e.printStackTrace() }
    }

    fun icshapedef(v: View) {
        icShapeViews!![Settings["icshape", 4]].setBackgroundColor(0x0)
        Settings["icshape"] = 0
        v.setBackgroundResource(R.drawable.selection)
    }

    fun icshaperound(v: View) {
        icShapeViews!![Settings["icshape", 4]].setBackgroundColor(0x0)
        Settings["icshape"] = 1
        v.setBackgroundResource(R.drawable.selection)
    }

    fun icshaperoundrect(v: View) {
        icShapeViews!![Settings["icshape", 4]].setBackgroundColor(0x0)
        Settings["icshape"] = 2
        v.setBackgroundResource(R.drawable.selection)
    }

    fun icshaperect(v: View) {
        icShapeViews!![Settings["icshape", 4]].setBackgroundColor(0x0)
        Settings["icshape"] = 3
        v.setBackgroundResource(R.drawable.selection)
    }

    fun icshapesquircle(v: View) {
        icShapeViews!![Settings["icshape", 4]].setBackgroundColor(0x0)
        Settings["icshape"] = 4
        v.setBackgroundResource(R.drawable.selection)
    }
}