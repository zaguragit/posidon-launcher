package posidon.launcher.search

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView.OnItemClickListener
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.BuildConfig
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.tools.Loader
import posidon.launcher.tools.Settings
import posidon.launcher.tools.Tools
import java.util.*

class SearchActivity : AppCompatActivity() {
    private val operators: MutableMap<String, Arithmetic> = HashMap()
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_layout)
        Tools.applyFontSetting(this@SearchActivity)
        val searchTxt = findViewById<EditText>(R.id.searchTxt)
        searchTxt.requestFocus()
        val searchgrid = findViewById<GridView>(R.id.searchgrid)
        search("", searchgrid)
        searchTxt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                search(s.toString(), searchgrid)
            }
        })
        searchTxt.setOnEditorActionListener { _, actionId, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER ||
                    actionId == EditorInfo.IME_ACTION_DONE) {
                onPause()
            }
            false
        }
        var bg = ShapeDrawable()
        val tr = Settings["searchradius", 0] * resources.displayMetrics.density
        bg.shape = RoundRectShape(floatArrayOf(tr, tr, tr, tr, 0f, 0f, 0f, 0f), null, null)
        bg.paint.color = Settings["searchcolor", 0x33000000]
        findViewById<View>(R.id.searchbar).background = bg
        bg = ShapeDrawable()
        bg.shape = RectShape()
        bg.paint.color = Settings["searchUiBg", -0x78000000]
        window.setBackgroundDrawable(bg)
        searchTxt.setTextColor(Settings["searchtxtcolor", -0x1])
        (findViewById<View>(R.id.failtxt) as TextView).setTextColor(Settings["searchtxtcolor", -0x1])
        searchTxt.setHintTextColor(Settings["searchhintcolor", -0x1])
        searchTxt.hint = Settings["searchhinttxt", "Search.."]
        (findViewById<View>(R.id.searchIcon) as ImageView).imageTintList = ColorStateList.valueOf(Settings["searchhintcolor", -0x1])
        (findViewById<View>(R.id.kill) as ImageView).imageTintList = ColorStateList.valueOf(Settings["searchhintcolor", -0x1])
        findViewById<View>(R.id.kill).setOnClickListener { startActivity(Intent(this@SearchActivity, Main::class.java)) }
        operators["+"] = Add()
        operators["plus"] = Add()
        operators["-"] = Subtract()
        operators["minus"] = Subtract()
        operators["*"] = Multiply()
        operators["x"] = Multiply()
        operators["times"] = Multiply()
        operators["/"] = Divide()
        operators[":"] = Divide()
        operators["over"] = Divide()
        operators["&"] = And()
        operators["and"] = And()
        operators["|"] = Or()
        operators["or"] = Or()
    }

    private var stillWantIP = false
    private fun search(string: String, grid: GridView) {
        stillWantIP = false
        var j = 0
        if (Main.apps != null) {
            val showHidden = cook(string) == cook("hidden") || cook(string) == cook("hiddenapps")
            if (showHidden) j++
            for (app in Main.apps) {
                for (word in app!!.label!!.split(" ").toTypedArray()) if (cook(word).contains(cook(string)) || word.contains(string)) {
                    j++
                    break
                }
            }
            val results = arrayOfNulls<App>(j)
            if (j > 0) {
                findViewById<View>(R.id.fail).visibility = View.GONE
                j = 0
                for (app in Main.apps) {
                    for (word in app!!.label!!.split(" ").toTypedArray()) {
                        if (cook(word).contains(cook(string)) || word.contains(string)) {
                            results[j] = app
                            j++
                            break
                        }
                    }
                }
            }
            if (showHidden) {
                findViewById<View>(R.id.fail).visibility = View.GONE
                val app = App()
                app.label = "Hidden apps"
                app.packageName = BuildConfig.APPLICATION_ID
                app.name = HiddenAppsActivity::class.java.name
                app.icon = getDrawable(R.drawable.hidden_apps)
                results[j] = app
                j++
            }
            try {
                grid.adapter = SearchAdapter(this, results)
                grid.onItemClickListener = OnItemClickListener { _, view, i, _ -> results[i]!!.open(this@SearchActivity, view) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        try {
            var tmp = string.trim { it <= ' ' }
                    .replace(" ", "")
                    .replace("=", "")
                    .replace("-+", "-")
                    .replace("+-", "-")
                    .replace("++", "+")
                    .replace("--", "+")
            for (op in operators.keys) tmp = tmp.replace(op, " $op ")
            if (tmp[1] == '-') tmp = "-" + tmp.substring(3)
            val math = tmp.toLowerCase().split(" ").toTypedArray()
            var bufferNum = java.lang.Double.valueOf(math[0])
            for (i in math.indices) {
                try {
                    math[i].toDouble()
                } catch (e: Exception) {
                    bufferNum = operators[math[i]]!!.apply(bufferNum, java.lang.Double.valueOf(math[i + 1]))
                    findViewById<View>(R.id.smartbox).visibility = View.VISIBLE
                    (findViewById<View>(R.id.type) as TextView).setText(R.string.math_operation)
                    (findViewById<View>(R.id.result) as TextView).text = "$tmp = $bufferNum"
                    findViewById<View>(R.id.fail).visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            if (j == 0) {
                findViewById<View>(R.id.fail).visibility = View.VISIBLE
                (findViewById<View>(R.id.failtxt) as TextView).text = getString(R.string.no_results_for, string)
            }
            if (string.contains("ip", ignoreCase = true)) {
                stillWantIP = true
                findViewById<View>(R.id.smartbox).visibility = View.VISIBLE
                (findViewById<View>(R.id.type) as TextView).setText(R.string.ip_address_external)
                (findViewById<View>(R.id.result) as TextView).text = ""
                Loader.text("https://checkip.amazonaws.com") {
                    if (stillWantIP) (findViewById<View>(R.id.result) as TextView).text = it
                }.execute()
                findViewById<View>(R.id.fail).visibility = View.GONE
            } else if (string.contains("pi", ignoreCase = true) || string.contains("π", ignoreCase = true)) {
                findViewById<View>(R.id.smartbox).visibility = View.VISIBLE
                (findViewById<View>(R.id.type) as TextView).setText(R.string.value_of_pi)
                (findViewById<View>(R.id.result) as TextView).text = "\u03c0 = " + Math.PI
                findViewById<View>(R.id.fail).visibility = View.GONE
            } else findViewById<View>(R.id.smartbox).visibility = View.GONE
        }
    }

    internal abstract class Arithmetic { abstract fun apply(x: Double, y: Double): Double }
    internal class Add : Arithmetic() { override fun apply(x: Double, y: Double): Double = x + y }
    internal class Subtract : Arithmetic() { override fun apply(x: Double, y: Double): Double = x - y }
    internal class Multiply : Arithmetic() { override fun apply(x: Double, y: Double): Double = x * y }
    internal class Divide : Arithmetic() { override fun apply(x: Double, y: Double): Double = x / y }
    internal class And : Arithmetic() { override fun apply(x: Double, y: Double): Double = (x.toInt() and y.toInt()).toDouble() }
    internal class Or : Arithmetic() { override fun apply(x: Double, y: Double): Double = (x.toInt() or y.toInt()).toDouble() }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        hideKeyboard(this)
        finish()
    }

    private fun cook(s: String): String {
        return s.toLowerCase()
                .replace(",", "")
                .replace(".", "")
                .replace("ñ", "n")
                .replace("ck", "c")
                .replace("cc", "c")
                .replace("z", "s")
                .replace("wh", "w")
                .replace("ts", "s")
                .replace("tz", "s")
                .replace("gh", "g")
                .replace("-", "")
                .replace("_", "")
                .replace("/", "")
                .replace("&", "")
    }

    companion object {
        private fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view = activity.currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) view = View(activity)
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}