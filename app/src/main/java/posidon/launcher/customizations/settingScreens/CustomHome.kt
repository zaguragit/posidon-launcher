package posidon.launcher.customizations.settingScreens

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextClock
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.drawable.FastColorDrawable
import posidon.launcher.feed.order.FeedOrderActivity
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.theme.applyFontSetting
import posidon.launcher.view.feed.Feed

class CustomHome : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_home)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) window.setDecorFitsSystemWindows(false)
        else window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)
        window.setBackgroundDrawable(FastColorDrawable(Global.getBlackAccent()))

        TextViewCompat.setCompoundDrawableTintList(findViewById(R.id.date_label), ColorStateList.valueOf(Global.getPastelAccent()))

        val sections = Feed.getSectionsFromSettings()
        if (sections.indexOfFirst { it.startsWith("widget:") && (
            Settings[it, "posidon.launcher/posidon.launcher.external.widgets.ClockWidget"]
                .startsWith("posidon.launcher/posidon.launcher.external.widgets.ClockWidget") ||
            Settings[it, "posidon.launcher/posidon.launcher.external.widgets.ClockWidget"]
                .startsWith("posidon.launcher/posidon.launcher.external.widgets.BigWidget")
        )} == -1) {
            findViewById<View>(R.id.dateFormatCard).visibility = View.GONE
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
        Global.customized = true
    }

    override fun onPause() {
        Global.customized = true
        Settings.apply {
            putNotSave("datef", findViewById<EditText>(R.id.dateformat).text.toString())
            apply()
        }
        super.onPause()
    }

    fun openFeedOrder(v: View) = startActivity(Intent(this, FeedOrderActivity::class.java))
}