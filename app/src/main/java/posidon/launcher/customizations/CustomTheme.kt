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
import posidon.launcher.tools.Settings
import posidon.launcher.tools.Tools


class CustomTheme : AppCompatActivity() {

    private var icShapeViews: Array<View>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Tools.applyFontSetting(this)
        setContentView(R.layout.custom_theme)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)
        val pm = packageManager
        icShapeViews = arrayOf(findViewById(R.id.icshapedef), findViewById(R.id.icshaperound), findViewById(R.id.icshaperoundrect), findViewById(R.id.icshaperect), findViewById(R.id.icshapesquircle))

        findViewById<TextView>(R.id.icShapeTxt).setTextColor(Main.accentColor)
        val i = findViewById<TextView>(R.id.iconpackselector)
        try { i.text = pm.getApplicationLabel(pm.getApplicationInfo(Settings.getString("iconpack", "system")!!, 0)) }
        catch (e: PackageManager.NameNotFoundException) { e.printStackTrace() }

        val fontName = findViewById<TextView>(R.id.fontname)
        when (Settings.getString("font", "ubuntu")) {
            "sansserif" -> fontName.text = getString(R.string.sans_serif)
            "posidonsans" -> fontName.text = getString(R.string.posidon_sans)
            "monospace" -> fontName.text = getString(R.string.monospace)
            "ubuntu" -> fontName.text = getString(R.string.ubuntu)
            "openDyslexic" -> fontName.text = getString(R.string.open_dyslexic)
        }
        findViewById<View>(R.id.fontbox).setOnClickListener {
            val d = Dialog(this@CustomTheme)
            d.setContentView(R.layout.font_list)
            d.findViewById<View>(R.id.sansserif).setOnClickListener {
                d.dismiss()
                Settings.putString("font", "sansserif")
                fontName.text = getString(R.string.sans_serif)
                Tools.applyFontSetting(this@CustomTheme)
            }
            d.findViewById<View>(R.id.posidonsans).setOnClickListener {
                d.dismiss()
                Settings.putString("font", "posidonsans")
                fontName.text = getString(R.string.posidon_sans)
                Tools.applyFontSetting(this@CustomTheme)
            }
            d.findViewById<View>(R.id.monospace).setOnClickListener {
                d.dismiss()
                Settings.putString("font", "monospace")
                fontName.text = getString(R.string.monospace)
                Tools.applyFontSetting(this@CustomTheme)
            }
            d.findViewById<View>(R.id.ubuntu).setOnClickListener {
                d.dismiss()
                Settings.putString("font", "ubuntu")
                fontName.text = getString(R.string.ubuntu)
                Tools.applyFontSetting(this@CustomTheme)
            }
            d.findViewById<View>(R.id.open_dyslexic).setOnClickListener {
                d.dismiss()
                Settings.putString("font", "openDyslexic")
                fontName.text = getString(R.string.open_dyslexic)
                Tools.applyFontSetting(this@CustomTheme)
            }
            d.show()
        }

        findViewById<View>(R.id.accentcolorprev).background = ColorTools.colorcircle(Settings.getInt("accent", 0x1155ff) or -0x1000000)

        (findViewById<View>(R.id.animatedicons) as Switch).isChecked = Settings.getBool("animatedicons", true)
        (findViewById<View>(R.id.reshapeicons) as Switch).isChecked = Settings.getBool("reshapeicons", false)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) findViewById<View>(R.id.icshapesettings).visibility = View.GONE
        else icShapeViews!![Settings.getInt("icshape", 4)].setBackgroundResource(R.drawable.selection)
        Main.shouldSetApps = true
        Main.customized = true
    }

    fun pickAccentColor(v: View) { ColorTools.pickColorNoAlpha(this, "accent", 0x1155ff) }
    fun iconPackSelector(v: View) { startActivity(Intent(this, IconPackPicker::class.java)) }

    override fun onPause() {
        Settings.putBool("animatedicons", (findViewById<View>(R.id.animatedicons) as Switch).isChecked)
        Settings.putBool("reshapeicons", (findViewById<View>(R.id.reshapeicons) as Switch).isChecked)
        Main.accentColor = Settings.getInt("accent", 0x1155ff) or -0x1000000
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        val pm = packageManager
        val i = findViewById<TextView>(R.id.iconpackselector)
        try { i.text = pm.getApplicationLabel(pm.getApplicationInfo(Settings.getString("iconpack", "system")!!, 0)) }
        catch (e: PackageManager.NameNotFoundException) { e.printStackTrace() }
    }

    fun icshapedef(v: View) {
        icShapeViews!![Settings.getInt("icshape", 4)].setBackgroundColor(0x0)
        Settings.putInt("icshape", 0)
        v.setBackgroundResource(R.drawable.selection)
    }

    fun icshaperound(v: View) {
        icShapeViews!![Settings.getInt("icshape", 4)].setBackgroundColor(0x0)
        Settings.putInt("icshape", 1)
        v.setBackgroundResource(R.drawable.selection)
    }

    fun icshaperoundrect(v: View) {
        icShapeViews!![Settings.getInt("icshape", 4)].setBackgroundColor(0x0)
        Settings.putInt("icshape", 2)
        v.setBackgroundResource(R.drawable.selection)
    }

    fun icshaperect(v: View) {
        icShapeViews!![Settings.getInt("icshape", 4)].setBackgroundColor(0x0)
        Settings.putInt("icshape", 3)
        v.setBackgroundResource(R.drawable.selection)
    }

    fun icshapesquircle(v: View) {
        icShapeViews!![Settings.getInt("icshape", 4)].setBackgroundColor(0x0)
        Settings.putInt("icshape", 4)
        v.setBackgroundResource(R.drawable.selection)
    }
}