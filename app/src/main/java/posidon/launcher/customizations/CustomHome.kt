package posidon.launcher.customizations

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextClock
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Home
import posidon.launcher.R
import posidon.launcher.feed.order.FeedOrderActivity
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.applyFontSetting

class CustomHome : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_home)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)

        val widget = Settings["widget", "posidon.launcher/posidon.launcher.external.widgets.ClockWidget"]
        when {
            widget.startsWith("posidon.launcher/posidon.launcher.external.widgets.ClockWidget") -> {}
            widget.startsWith("posidon.launcher/posidon.launcher.external.widgets.BigWidget") -> {}
            else -> findViewById<View>(R.id.dateFormatCard).visibility = View.GONE
        }

        run {
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
        }
        Home.customized = true
    }

    override fun onPause() {
        Home.customized = true
        Settings.apply {
            putNotSave("datef", findViewById<EditText>(R.id.dateformat).text.toString())
            apply()
        }
        super.onPause()
    }

    fun openFeedOrder(v: View) = startActivity(Intent(this, FeedOrderActivity::class.java))
}