package posidon.launcher.search

import android.app.ActivityOptions
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.graphics.drawable.shapes.RoundRectShape
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.DragEvent
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.items.InternalItem
import posidon.launcher.items.ItemLongPress
import posidon.launcher.items.LauncherItem
import posidon.launcher.storage.Settings
import posidon.launcher.tools.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.pow

private typealias D = Double

class SearchActivity : AppCompatActivity() {

    private val operators: MutableMap<String, C> = HashMap()
    private lateinit var smartBox: View
    private lateinit var answerBox: View
    private lateinit var grid: GridView
    private lateinit var searchTxt: EditText

    private var bottomPaddingWhenSmartBoxIsShown = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_layout)
        applyFontSetting()
        smartBox = findViewById(R.id.smartbox)
        answerBox = findViewById(R.id.instantAnswer)
        bottomPaddingWhenSmartBoxIsShown = (82.dp + 46.sp).toInt()
        searchTxt = findViewById(R.id.searchTxt)
        searchTxt.requestFocus()
        grid = findViewById(R.id.searchgrid)
        grid.isStackFromBottom = Settings["search:start_from_bottom", false]
        searchTxt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                search(s.toString())
            }
        })
        searchTxt.setOnEditorActionListener { _, actionId, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER ||
                    actionId == EditorInfo.IME_ACTION_DONE) {
                onPause()
            }
            false
        }
        findViewById<View>(R.id.searchbar).background = ShapeDrawable().apply {
            val tr = Settings["searchradius", 0].dp
            shape = RoundRectShape(floatArrayOf(tr, tr, tr, tr, 0f, 0f, 0f, 0f), null, null)
            paint.color = Settings["searchcolor", 0x33000000]
        }
        if (Tools.canBlurSearch) {
            val arr = arrayOf(BitmapDrawable(Tools.blurredWall(Settings["search:blur:rad", 15f])), ColorDrawable(Settings["searchUiBg", -0x78000000]))
            window.setBackgroundDrawable(LayerDrawable(arr))
        } else {
            window.setBackgroundDrawable(ColorDrawable(Settings["searchUiBg", -0x78000000]))
        }
        searchTxt.setTextColor(Settings["searchtxtcolor", -0x1])
        findViewById<TextView>(R.id.failtxt).setTextColor(Settings["searchtxtcolor", -0x1])
        searchTxt.setHintTextColor(Settings["searchhintcolor", -0x1])
        searchTxt.hint = Settings["searchhinttxt", "Search.."]
        findViewById<ImageView>(R.id.searchIcon).apply {
            imageTintList = ColorStateList.valueOf(Settings["searchhintcolor", -0x1])
            imageTintMode = PorterDuff.Mode.MULTIPLY
        }
        findViewById<ImageView>(R.id.kill).apply {
            imageTintList = ColorStateList.valueOf(Settings["searchhintcolor", -0x1])
            imageTintMode = PorterDuff.Mode.MULTIPLY
            setOnClickListener {
                if (Settings["search:asHome", false]) {
                    searchTxt.text.clear()
                } else {
                    startActivity(Intent(this@SearchActivity, Main::class.java))
                }
            }
        }
        val add = Add()
        val sub = Sub()
        val mul = Mul()
        val div = Div()
        val and = And()
        val or = Or()
        val xor = Xor()
        val rem = Rem()
        val pow = Pow()
        operators["+"] = add
        operators["plus"] = add
        operators["-"] = sub
        operators["minus"] = sub
        operators["*"] = mul
        operators["x"] = mul
        operators["times"] = mul
        operators["/"] = div
        operators[":"] = div
        operators["over"] = div
        operators["&"] = and
        operators["and"] = and
        operators["|"] = or
        operators["or"] = or
        operators["xor"] = xor
        operators["%"] = rem
        operators["rem"] = rem
        operators["pow"] = pow
        operators["^"] = pow

        window.decorView.findViewById<View>(android.R.id.content).setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_LOCATION -> {
                    val icon = event.localState as View
                    val location = IntArray(2)
                    icon.getLocationOnScreen(location)
                    val y = abs(event.y - location[1])
                    if (y > icon.height / 3.5f) {
                        ItemLongPress.currentPopup?.dismiss()
                        finish()
                    }
                    true
                }
                DragEvent.ACTION_DRAG_STARTED -> {
                    (event.localState as View).visibility = View.INVISIBLE
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    (event.localState as View).visibility = View.VISIBLE
                    ItemLongPress.currentPopup?.isFocusable = true
                    ItemLongPress.currentPopup?.update()
                    true
                }
                else -> false
            }
        }

        search("")
    }

    private var stillWantIP = false
    private var currentString = ""

    private fun search(string: String) {
        currentString = string
        if (string.isEmpty()) {
            grid.adapter = SearchAdapter(this, listOf())
            findViewById<View>(R.id.fail).visibility = View.GONE
            answerBox.visibility = View.GONE
            grid.setPadding(0, 0, 0, 64.dp.toInt())
            //grid.onItemClickListener = OnItemClickListener { _, view, i, _ -> results[i].open(this@SearchActivity, view) }
            //grid.onItemLongClickListener = ItemLongPress.search(this, results)
            return
        }
        stillWantIP = false
        val showHidden = searchOptimize(string) == searchOptimize("hidden") || searchOptimize(string) == searchOptimize("hiddenapps")
        val results = ArrayList<LauncherItem>()
        findViewById<View>(R.id.fail).visibility = View.GONE
        for (app in Main.apps) {
            if (searchOptimize(app.label!!).contains(searchOptimize(string)) ||
                app.label!!.contains(string) ||
                searchOptimize(app.packageName).contains(searchOptimize(string)) ||
                app.packageName.contains(string)) {
                results.add(app)
                continue
            }
            for (word in app.label!!.split(" ").toTypedArray()) {
                if (searchOptimize(word).contains(searchOptimize(string)) || word.contains(string)) {
                    results.add(app)
                    break
                }
            }
        }
        if (showHidden) {
            findViewById<View>(R.id.fail).visibility = View.GONE
            val app = InternalItem("Hidden apps", getDrawable(R.drawable.hidden_apps)) {
                startActivity(Intent(this, HiddenAppsActivity::class.java))
            }
            results.add(app)
        }
        try {
            grid.adapter = SearchAdapter(this, results)
            grid.onItemClickListener = OnItemClickListener { _, view, i, _ ->
                when (val r = results[i]) {
                    is App -> r.open(this@SearchActivity, view)
                    is InternalItem -> r.open()
                }
            }
            grid.onItemLongClickListener = ItemLongPress.search(this, results)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var isShowingSmartCard = false
        try {
            var tmp = string.trim { it <= ' ' }
                .replace(" ", "")
                .replace("=", "")
                .replace("-+", "-")
                .replace("+-", "-")
                .replace("++", "+")
                .replace("--", "+")
            for (op in operators.keys) {
                tmp = tmp.replace(op, " $op ")
            }
            if (tmp[1] == '-') {
                tmp = "-" + tmp.substring(3)
            }
            val math = tmp.toLowerCase().split(" ").toTypedArray()
            var bufferNum = java.lang.Double.valueOf(math[0])
            for (i in math.indices) {
                try {
                    math[i].toDouble()
                } catch (e: Exception) {
                    bufferNum = operators[math[i]]!!.a(bufferNum, java.lang.Double.valueOf(math[i + 1]))
                    smartBox.visibility = View.VISIBLE
                    isShowingSmartCard = true
                    grid.setPadding(0, bottomPaddingWhenSmartBoxIsShown, 0, 64.dp.toInt())
                    findViewById<TextView>(R.id.type).setText(R.string.math_operation)
                    findViewById<TextView>(R.id.result).text = "$tmp = $bufferNum"
                    findViewById<View>(R.id.fail).visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            if (results.isEmpty()) {
                findViewById<View>(R.id.fail).visibility = View.VISIBLE
                findViewById<TextView>(R.id.failtxt).text = getString(R.string.no_results_for, string)
            }
            val words = string.split(' ', ',', '.', '-')
            if (words.contains("ip")) {
                stillWantIP = true
                smartBox.visibility = View.VISIBLE
                isShowingSmartCard = true
                grid.setPadding(0, bottomPaddingWhenSmartBoxIsShown, 0, 64.dp.toInt())
                smartBox.findViewById<TextView>(R.id.type).setText(R.string.ip_address_external)
                smartBox.findViewById<TextView>(R.id.result).text = ""
                Loader.Text("https://checkip.amazonaws.com") {
                    if (stillWantIP) smartBox.findViewById<TextView>(R.id.result).text = it.trimEnd()
                }.execute()
                findViewById<View>(R.id.fail).visibility = View.GONE
            } else if (
                words.contains("pi") ||
                words.contains("PI") ||
                words.contains("π")
            ) {
                smartBox.visibility = View.VISIBLE
                isShowingSmartCard = true
                grid.setPadding(0, bottomPaddingWhenSmartBoxIsShown, 0, 64.dp.toInt())
                findViewById<TextView>(R.id.type).setText(R.string.value_of_pi)
                findViewById<TextView>(R.id.result).text = "\u03c0 = ${Math.PI}"
                findViewById<View>(R.id.fail).visibility = View.GONE
            } else {
                smartBox.visibility = View.GONE
                grid.setPadding(0, 0, 0, 64.dp.toInt())
            }
        }
        answerBox.visibility = View.GONE
        grid.setPadding(0, 0, 0, 64.dp.toInt())
        if (results.size < 6 && !isShowingSmartCard && string.length > 3 && !showHidden) {
            DuckInstantAnswer.load(string) { instantAnswer ->
                if (currentString == string) {
                    runOnUiThread {
                        answerBox.visibility = View.VISIBLE
                        findViewById<View>(R.id.fail).visibility = View.GONE
                        answerBox.findViewById<TextView>(R.id.instantAnswerTitle).text = instantAnswer.title
                        answerBox.findViewById<TextView>(R.id.instantAnswerText).text = instantAnswer.description
                        answerBox.findViewById<TextView>(R.id.instantAnswerSource).apply {
                            text = instantAnswer.sourceName
                            setOnClickListener {
                                val uri = Uri.parse(instantAnswer.sourceUrl)
                                val i = Intent(Intent.ACTION_VIEW, uri)
                                startActivity(i, ActivityOptions.makeCustomAnimation(this@SearchActivity, R.anim.slideup, R.anim.slidedown).toBundle())
                            }
                        }
                        answerBox.findViewById<View>(R.id.instantAnswerDuckDuckGoLink).setOnClickListener {
                            val uri = Uri.parse(instantAnswer.searchUrl)
                            val i = Intent(Intent.ACTION_VIEW, uri)
                            startActivity(i, ActivityOptions.makeCustomAnimation(this@SearchActivity, R.anim.slideup, R.anim.slidedown).toBundle())
                        }
                        answerBox.post {
                            grid.setPadding(0, answerBox.measuredHeight + 16.dp.toInt(), 0, 64.dp.toInt())
                        }
                    }
                }
            }
        }
    }

    private interface C { fun a(x: D, y: D): D }
    private class Add : C { override fun a(x: D, y: D) = x + y }
    private class Sub : C { override fun a(x: D, y: D) = x - y }
    private class Mul : C { override fun a(x: D, y: D) = x * y }
    private class Div : C { override fun a(x: D, y: D) = x / y }
    private class And : C { override fun a(x: D, y: D) = (x.toInt() and y.toInt()).toDouble() }
    private class Or  : C { override fun a(x: D, y: D) = (x.toInt() or  y.toInt()).toDouble() }
    private class Xor : C { override fun a(x: D, y: D) = (x.toInt() xor y.toInt()).toDouble() }
    private class Rem : C { override fun a(x: D, y: D) = (x.toInt() %   y.toInt()).toDouble() }
    private class Pow : C { override fun a(x: D, y: D) = x.pow(y) }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        hideKeyboard()
        finish()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            search(searchTxt.text.toString())
        }
    }

    companion object {
        fun searchOptimize(s: String) = s.toLowerCase()
            .replace('ñ', 'n')
            .replace('e', '3')
            .replace('a', '4')
            .replace('i', '1')
            .replace('¿', '?')
            .replace('¡', '!')
            .replace(Regex("(k|cc|ck)"), "c")
            .replace(Regex("(z|ts|sc|cs|tz)"), "s")
            .replace(Regex("(gh|wh)"), "h")
            .replace(Regex("[-'&/_,.:;*\"]"), "")
    }
}