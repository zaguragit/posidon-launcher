package posidon.launcher.search

import android.Manifest
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
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
import androidx.core.content.ContextCompat
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.items.*
import posidon.launcher.storage.Settings
import posidon.launcher.tools.*
import posidon.launcher.tools.Tools.searchOptimize
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.pow

private typealias D = Double
private interface Op : (D, D) -> D { val string: String }

class SearchActivity : AppCompatActivity() {

    private val operators: MutableMap<String, Op> = HashMap()
    private lateinit var smartBox: View
    private lateinit var answerBox: View
    private lateinit var grid: GridView
    private lateinit var searchTxt: EditText
    private var canReadContacts = false

    private var topPaddingWhenSmartBoxIsShown = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_layout)
        applyFontSetting()
        smartBox = findViewById(R.id.smartbox)
        answerBox = findViewById(R.id.instantAnswer)
        topPaddingWhenSmartBoxIsShown = (82.dp + 46.sp).toInt()
        searchTxt = findViewById(R.id.searchTxt)
        searchTxt.requestFocus()
        grid = findViewById(R.id.searchgrid)
        val stackFromBottom = Settings["search:start_from_bottom", false]
        grid.isStackFromBottom = stackFromBottom
        if (stackFromBottom) {
            findViewById<View>(R.id.searchResultsPusher).visibility = View.VISIBLE
        }
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

        val add = object : Op {
            override val string = "+"
            override fun invoke(x: D, y: D) = x + y
        }

        val sub = object : Op {
            override val string = "-"
            override fun invoke(x: D, y: D) = x - y
        }

        val mul = object : Op {
            override val string = "*"
            override fun invoke(x: D, y: D) = x * y
        }

        val div = object : Op {
            override val string = "/"
            override fun invoke(x: D, y: D) = x / y
        }


        val rem = object : Op {
            override val string = "%"
            override fun invoke(x: D, y: D) = x % y
        }

        val pow = object : Op {
            override val string = "^"
            override fun invoke(x: D, y: D) = x.pow(y)
        }


        val and = object : Op {
            override val string = "&"
            override fun invoke(x: D, y: D) = (x.toInt() and y.toInt()).toDouble()
        }

        val or  = object : Op {
            override val string = "|"
            override fun invoke(x: D, y: D) = (x.toInt() or  y.toInt()).toDouble()
        }

        val xor = object : Op {
            override val string = "xor"
            override fun invoke(x: D, y: D) = (x.toInt() xor y.toInt()).toDouble()
        }

        operators["+"] = add
        operators["plus"] = add
        operators["add"] = add
        operators["-"] = sub
        operators["minus"] = sub
        operators["subtract"] = sub
        operators["*"] = mul
        operators["x"] = mul
        operators["times"] = mul
        operators["multiply"] = mul
        operators["/"] = div
        operators[":"] = div
        operators["over"] = div
        operators["divide"] = div
        operators["&"] = and
        operators["and"] = and
        operators["|"] = or
        operators["or"] = or
        operators["xor"] = xor
        operators["%"] = rem
        operators["rem"] = rem
        operators["remainder"] = rem
        operators["mod"] = rem
        operators["pow"] = pow
        operators["^"] = pow
        operators["power"] = pow

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

        canReadContacts = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        search("")
    }

    private var stillWantIP = false
    private var currentString = ""

    private fun search(string: String) {
        currentString = string
        val searchOptimizedString = searchOptimize(string)
        if (string.isEmpty()) {
            grid.adapter = SearchAdapter(this, listOf())
            findViewById<View>(R.id.fail).visibility = View.GONE
            answerBox.visibility = View.GONE
            return
        }
        stillWantIP = false
        val results = ArrayList<LauncherItem>()
        val showHidden = searchOptimizedString == searchOptimize("hidden") || searchOptimizedString == searchOptimize("hiddenapps")
        if (showHidden) {
            findViewById<View>(R.id.fail).visibility = View.GONE
            val app = InternalItem("Hidden apps", getDrawable(R.drawable.hidden_apps)) {
                startActivity(Intent(this, HiddenAppsActivity::class.java))
            }
            results.add(app)
        }
        findViewById<View>(R.id.fail).visibility = View.GONE
        for (app in Main.apps) {
            if (searchOptimize(app.label!!).contains(searchOptimizedString) ||
                app.label!!.contains(string) ||
                searchOptimize(app.packageName).contains(searchOptimizedString) ||
                app.packageName.contains(string)) {
                results.add(app)
                continue
            }
            for (word in app.label!!.split(' ', ',', '.', '-', '+', '&', '_')) {
                if (searchOptimize(word).contains(searchOptimizedString) || word.contains(string)) {
                    results.add(app)
                    break
                }
            }
        }
        kotlin.runCatching {
            val settingList = SettingsItem.getList()
            for (setting in settingList) {
                if (searchOptimize(setting.label!!).contains(searchOptimizedString) ||
                    setting.label!!.contains(string) ||
                    searchOptimize(setting.action).contains(searchOptimizedString) ||
                    setting.action.contains(string)) {
                    results.add(setting)
                    continue
                }
                for (word in setting.label!!.split(' ', '-', '_')) {
                    if (searchOptimize(word).contains(searchOptimizedString) || word.contains(string)) {
                        results.add(setting)
                        break
                    }
                }
            }
        }
        if (canReadContacts) kotlin.runCatching {
            val contactList = ContactItem.getList()
            for (contact in contactList) {
                if (searchOptimize(contact.label!!).contains(searchOptimizedString) ||
                    contact.label!!.contains(string) ||
                    contact.phone.contains(searchOptimizedString) ||
                    contact.phone.contains(string)) {
                    results.add(contact)
                    continue
                }
                for (word in contact.label!!.split(' ', '-', '_')) {
                    if (searchOptimize(word).contains(searchOptimizedString) || word.contains(string)) {
                        results.add(contact)
                        break
                    }
                }
            }
        }
        try {
            Sort.labelSort(results)
            grid.adapter = SearchAdapter(this, results)
            grid.onItemClickListener = OnItemClickListener { _, view, i, _ ->
                when (val r = results[i]) {
                    is App -> r.open(this@SearchActivity, view)
                    is InternalItem -> r.open()
                    is SettingsItem -> r.open()
                    is ContactItem -> r.open()
                }
            }
            grid.onItemLongClickListener = ItemLongPress.search(this, results)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var isShowingSmartCard = false
        try {
            var tmp = string.trim { it <= ' ' }
                .replace(Regex("[= ]"), "")
                .replace(Regex("(\\+-|-\\+)"), "-")
                .replace(Regex("(\\+\\+|--)"), "+")
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
                    val op = operators[math[i]]!!
                    bufferNum = op(bufferNum, java.lang.Double.valueOf(math[i + 1]))
                    math[i] = op.string
                    smartBox.visibility = View.VISIBLE
                    isShowingSmartCard = true
                    findViewById<TextView>(R.id.type).setText(R.string.math_operation)
                    findViewById<TextView>(R.id.result).text = "${math.joinToString(" ")} = $bufferNum"
                    findViewById<View>(R.id.fail).visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            if (results.isEmpty()) {
                findViewById<View>(R.id.fail).visibility = View.VISIBLE
                findViewById<TextView>(R.id.failtxt).text = getString(R.string.no_results_for, string)
            }
            val words = string.toLowerCase().split(' ', ',', '.', '-', '+', '&', '_')
            if (words.contains("ip")) {
                stillWantIP = true
                smartBox.visibility = View.VISIBLE
                isShowingSmartCard = true
                smartBox.findViewById<TextView>(R.id.type).setText(R.string.ip_address_external)
                smartBox.findViewById<TextView>(R.id.result).text = ""
                Loader.Text("https://checkip.amazonaws.com") {
                    if (stillWantIP) smartBox.findViewById<TextView>(R.id.result).text = it.trimEnd()
                }.execute()
                findViewById<View>(R.id.fail).visibility = View.GONE
            } else if (
                words.contains("pi") ||
                words.contains("π")
            ) {
                smartBox.visibility = View.VISIBLE
                isShowingSmartCard = true
                findViewById<TextView>(R.id.type).setText(R.string.value_of_pi)
                findViewById<TextView>(R.id.result).text = "\u03c0 = ${Math.PI}"
                findViewById<View>(R.id.fail).visibility = View.GONE
            } else {
                smartBox.visibility = View.GONE
            }
        }
        answerBox.visibility = View.GONE
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
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        hideKeyboard()

        if (Settings["kustom:variables:enable", false]) {
            Kustom["screen"] = "?"
        }

        finish()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            if (Settings["kustom:variables:enable", false]) {
                Kustom["screen"] = "search"
            }
        }
    }
}