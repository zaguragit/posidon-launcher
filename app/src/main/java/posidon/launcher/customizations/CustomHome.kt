package posidon.launcher.customizations

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextClock
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.feed.news.RemovedArticles
import posidon.launcher.feed.news.chooser.FeedChooser
import posidon.launcher.storage.Settings
import posidon.launcher.tools.*
import posidon.launcher.view.Spinner
import posidon.launcher.view.Switch

class CustomHome : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_home)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)

        findViewById<View>(R.id.clockcolorprev).background = ColorTools.colorCircle(Settings["clockcolor", -0x1])

        val widget = Settings["widget", "posidon.launcher/posidon.launcher.external.widgets.ClockWidget"]
        when {
            widget.startsWith("posidon.launcher/posidon.launcher.external.widgets.ClockWidget") -> {}
            widget.startsWith("posidon.launcher/posidon.launcher.external.widgets.BigWidget") -> {}
            else -> findViewById<View>(R.id.dateFormatCard).visibility = View.GONE
        }

        val dateformat = Settings["datef", resources.getString(R.string.defaultdateformat)]
        val datefprev = findViewById<TextClock>(R.id.datefprev)
        val dateftxt = findViewById<EditText>(R.id.dateformat)
        dateftxt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val i = dateftxt.text.toString()
                datefprev.format12Hour = i
                datefprev.format24Hour = i
            }
        })
        dateftxt.setText(dateformat, TextView.BufferType.EDITABLE)


        findViewById<Switch>(R.id.showBehindDock).isChecked = Settings["feed:show_behind_dock", false]
        findViewById<Switch>(R.id.feedenabled).isChecked = Settings["feed:enabled", true]
        findViewById<Switch>(R.id.hidefeed).isChecked = Settings["hidefeed", false]

        val newsCardMaxImageWidthSlider = findViewById<SeekBar>(R.id.newsCardMaxImageWidthSlider)
        val maxWidth = Settings["feed:max_img_width", Device.displayWidth]
        newsCardMaxImageWidthSlider.progress = (maxWidth.toFloat() / Device.displayWidth.toFloat() * 6).toInt() - 1
        newsCardMaxImageWidthSlider.max = 5
        val newsCardMaxImageWidthNum = findViewById<TextView>(R.id.newsCardMaxImageWidthNum)
        newsCardMaxImageWidthNum.text = maxWidth.toString()
        newsCardMaxImageWidthSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) { Settings.apply() }
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val newVal: Int = Device.displayWidth / 6 * (progress + 1)
                newsCardMaxImageWidthNum.text = newVal.toString()
                Settings["feed:max_img_width"] = newVal
            }
        })

        val newscardradiusslider = findViewById<SeekBar>(R.id.newscardradiusslider)
        newscardradiusslider.progress = Settings["feed:card_radius", 15]
        val newscardradiusnum = findViewById<TextView>(R.id.newscardradiusnum)
        newscardradiusnum.text = Settings["feed:card_radius", 15].toString()
        newscardradiusslider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                newscardradiusnum.text = progress.toString()
                Settings["feed:card_radius"] = progress
            }
        })

        val cardHorizontalMarginSeekbar = findViewById<SeekBar>(R.id.cardHorizontalMarginSeekbar)
        cardHorizontalMarginSeekbar.progress = Settings["feed:card_margin_x", 16]
        val cardHorizontalMarginNum = findViewById<TextView>(R.id.cardHorizontalMarginNum)
        cardHorizontalMarginNum.text = Settings["feed:card_margin_x", 16].toString()
        cardHorizontalMarginSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                cardHorizontalMarginNum.text = progress.toString()
                Settings["feed:card_margin_x"] = progress
            }
        })

        findViewById<View>(R.id.newscardbgprev).background = ColorTools.colorCircle(Settings["feed:card_bg", -0xdad9d9])
        findViewById<View>(R.id.newscardtxtprev).background = ColorTools.colorCircle(Settings["feed:card_txt_color", -0x1])
        findViewById<Switch>(R.id.newscardenableimg).isChecked = Settings["feed:card_img_enabled", true]
        findViewById<Switch>(R.id.newscardblackgradient).isChecked = Settings["feed:card_text_shadow", true]

        findViewById<Switch>(R.id.delete_articles).isChecked = Settings["feed:delete_articles", false]

        findViewById<Spinner>(R.id.readMethods).apply {
            data = resources.getStringArray(R.array.articleReadingMethods)
            selectionI = when(Settings["feed:openLinks", "browse"]) {
                "webView" -> 1; "app" -> 2; else -> 0
            }
            setSelectionChangedListener {
                Settings["feed:openLinks"] = when(selectionI) {
                    1 -> "webView"; 2 -> "app"; else -> "browse"
                }
            }
        }

        findViewById<Switch>(R.id.showFeedSpinner).isChecked = Settings["feed:show_spinner", true]

        Main.customized = true
    }

    fun pickclockcolor(v: View) = ColorTools.pickColor(this, Settings["clockcolor", -0x1]) {
        v as ViewGroup
        v.getChildAt(1).background = ColorTools.colorCircle(it)
        Settings["clockcolor"] = it
    }

    fun picknewscardcolor(v: View) = ColorTools.pickColor(this, Settings["feed:card_bg", -0xdad9d9]) {
        v as ViewGroup
        v.getChildAt(1).background = ColorTools.colorCircle(it)
        Settings["feed:card_bg"] = it
    }

    fun picknewscardtxtcolor(v: View) = ColorTools.pickColor(this, Settings["feed:card_txt_color", -0x1]) {
        v as ViewGroup
        v.getChildAt(1).background = ColorTools.colorCircle(it)
        Settings["feed:card_txt_color"] = it
    }

    fun chooseFeeds(v: View) = startActivity(Intent(this, FeedChooser::class.java))
    fun chooseLayouts(v: View) {
        val dialog = BottomSheetDialog(this, R.style.bottomsheet)
        dialog.setContentView(R.layout.custom_home_feed_card_layout_chooser)
        dialog.window!!.findViewById<View>(R.id.design_bottom_sheet).setBackgroundResource(R.drawable.bottom_sheet)
        dialog.findViewById<View>(R.id.card0)!!.setOnClickListener {
            vibrate()
            Settings["feed:card_layout"] = 0
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.card1)!!.setOnClickListener {
            vibrate()
            Settings["feed:card_layout"] = 1
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.card2)!!.setOnClickListener {
            vibrate()
            Settings["feed:card_layout"] = 2
            dialog.dismiss()
        }
        dialog.show()
    }
    fun seeRemovedArticles(v: View) = startActivity(Intent(this, RemovedArticles::class.java))

    override fun onPause() {
        Main.customized = true
        Settings.apply {
            putNotSave("datef", findViewById<EditText>(R.id.dateformat).text.toString())
            putNotSave("feed:enabled", findViewById<Switch>(R.id.feedenabled).isChecked)
            putNotSave("hidefeed", findViewById<Switch>(R.id.hidefeed).isChecked)
            putNotSave("feed:delete_articles", findViewById<Switch>(R.id.delete_articles).isChecked)
            putNotSave("feed:card_img_enabled", findViewById<Switch>(R.id.newscardenableimg).isChecked)
            putNotSave("feed:card_text_shadow", findViewById<Switch>(R.id.newscardblackgradient).isChecked)
            putNotSave("feed:show_behind_dock", findViewById<Switch>(R.id.showBehindDock).isChecked)
            putNotSave("feed:show_spinner", findViewById<Switch>(R.id.showFeedSpinner).isChecked)
            apply()
        }
        super.onPause()
    }
}