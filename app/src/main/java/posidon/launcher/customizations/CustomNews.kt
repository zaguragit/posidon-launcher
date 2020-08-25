package posidon.launcher.customizations

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.feed.news.RemovedArticles
import posidon.launcher.feed.news.chooser.FeedChooser
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Device
import posidon.launcher.tools.Tools
import posidon.launcher.tools.applyFontSetting
import posidon.launcher.tools.vibrate
import posidon.launcher.view.Spinner

class CustomNews : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_news)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)

        run {
            val newsCardMaxImageWidthSlider = findViewById<SeekBar>(R.id.newsCardMaxImageWidthSlider)
            val maxWidth = Settings["feed:max_img_width", Device.displayWidth]
            newsCardMaxImageWidthSlider.progress = (maxWidth.toFloat() / Device.displayWidth.toFloat() * 6).toInt() - 1
            newsCardMaxImageWidthSlider.max = 5
            val newsCardMaxImageWidthNum = findViewById<TextView>(R.id.newsCardMaxImageWidthNum)
            newsCardMaxImageWidthNum.text = maxWidth.toString()
            newsCardMaxImageWidthSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) = Settings.apply()
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    val newVal: Int = Device.displayWidth / 6 * (progress + 1)
                    newsCardMaxImageWidthNum.text = newVal.toString()
                    Settings["feed:max_img_width"] = newVal
                }
            })
        }

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

        Main.customized = true
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
}