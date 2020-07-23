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
import posidon.launcher.view.Spinner


class CustomDock : AppCompatActivity() {

    private var icsize: SeekBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_dock)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)

        findViewById<Spinner>(R.id.animationOptions).data = resources.getStringArray(R.array.bgModes)
        findViewById<Spinner>(R.id.animationOptions).selectionI = Settings["dock:background_type", 0]

        icsize = findViewById(R.id.dockiconsizeslider)
        icsize!!.progress = Settings["dockicsize", 1]

        run {
            val iccount = findViewById<SeekBar>(R.id.columnSlider)
            iccount!!.progress = Settings["dock:columns", 5]
            val c = findViewById<TextView>(R.id.iccountnum)
            c.text = Settings["dock:columns", 5].toString()
            iccount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    c.text = progress.toString()
                    Settings["dock:columns"] = iccount.progress
                }
            })
        }

        run {
            val rowCount = findViewById<SeekBar>(R.id.dockRowCountSlider)
            rowCount!!.progress = Settings["dock:rows", 1] - 1
            val c2 = findViewById<TextView>(R.id.icRowNum)
            c2.text = Settings["dock:rows", 1].toString()
            rowCount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    c2.text = (progress + 1).toString()
                    Settings["dock:rows"] = rowCount.progress + 1
                }
            })
        }

        val docklabelswitch = findViewById<Switch>(R.id.labelsEnabled)
        docklabelswitch.isChecked = Settings["dockLabelsEnabled", false]

        findViewById<View>(R.id.bgColorPrev).background = ColorTools.colorCircle(Settings["dock:background_color", -0x78000000])
        findViewById<View>(R.id.labelColorPrev).background = ColorTools.colorCircle(Settings["dockLabelColor", -0x11111112])

        findViewById<SeekBar>(R.id.radiusSlider).progress = Settings["dockradius", 30]

        run {
            val bottompadding = findViewById<SeekBar>(R.id.bottompaddingslider)
            bottompadding.progress = Settings["dockbottompadding", 10]
            findViewById<TextView>(R.id.bottompadding).text = Settings["dockbottompadding", 10].toString()
            bottompadding.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    findViewById<TextView>(R.id.bottompadding).text = progress.toString()
                    Settings["dockbottompadding"] = progress
                }
            })
        }

        run {
            val xMargin = findViewById<SeekBar>(R.id.xMarginSlider)
            xMargin.progress = Settings["dock:margin_x", 16]
            findViewById<TextView>(R.id.xMarginNum).text = Settings["dock:margin_x", 16].toString()
            xMargin.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    findViewById<TextView>(R.id.xMarginNum).text = progress.toString()
                    Settings["dock:margin_x"] = progress
                }
            })
        }

        Main.customized = true
    }

    fun pickColor(v: View) = ColorTools.pickColor(this, Settings["dock:background_color", -0x78000000]) {
        v as ViewGroup
        v.getChildAt(1).background = ColorTools.colorCircle(it)
        Settings["dock:background_color"] = it
    }

    fun pickLabelColor(v: View) = ColorTools.pickColor(this, Settings["dockLabelColor", -0x11111112]) {
        v as ViewGroup
        v.getChildAt(1).background = ColorTools.colorCircle(it)
        Settings["dockLabelColor"] = it
    }

    override fun onPause() {
        Main.customized = true
        Settings.apply {
            putNotSave("dock:background_type", findViewById<Spinner>(R.id.animationOptions).selectionI)
            putNotSave("dockicsize", icsize!!.progress)
            putNotSave("dockLabelsEnabled", findViewById<Switch>(R.id.labelsEnabled).isChecked)
            putNotSave("dockradius", findViewById<SeekBar>(R.id.radiusSlider).progress)
            apply()
        }
        super.onPause()
    }
}