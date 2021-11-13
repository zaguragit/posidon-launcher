package posidon.launcher.search

import android.Manifest
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.DragEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView.OnItemClickListener
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.posidon.android.launcherutils.appLoading.AppLoader
import io.posidon.android.launcherutils.appLoading.IconConfig
import io.posidon.android.launcherutils.liveWallpaper.Kustom
import posidon.android.conveniencelib.dp
import posidon.android.conveniencelib.hideKeyboard
import posidon.android.conveniencelib.sp
import posidon.android.loader.duckduckgo.DuckInstantAnswer
import posidon.android.loader.text.TextLoader
import posidon.launcher.Global
import posidon.launcher.Home
import posidon.launcher.R
import posidon.launcher.drawable.FastColorDrawable
import posidon.launcher.items.*
import posidon.launcher.items.users.AppCallback
import posidon.launcher.items.users.AppCollection
import posidon.launcher.items.users.ItemLongPress
import posidon.launcher.search.parsing.Parser
import posidon.launcher.storage.Settings
import posidon.launcher.tools.*
import posidon.launcher.tools.Tools.searchOptimize
import posidon.launcher.tools.theme.Icons
import posidon.launcher.tools.theme.Wallpaper
import posidon.launcher.tools.theme.applyFontSetting
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import kotlin.math.abs

class SearchActivity : AppCompatActivity() {

    private lateinit var smartBox: View
    private lateinit var answerBox: View
    private lateinit var grid: GridView
    private lateinit var searchTxt: EditText
    private var canReadContacts = false

    private var topPaddingWhenSmartBoxIsShown = 0

    private var stillWantIP = false
    private var currentString = ""

    private val onAppLoaderEnd = { search(currentString) }

    private val daxResultIcon by lazy {
        Icons.applyInsets(Icons.generateAdaptiveIcon(getDrawable(R.drawable.dax)!!))
    }

    val appLoader = AppLoader(::AppCollection)

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_layout)
        applyFontSetting()
        smartBox = findViewById(R.id.smartbox)
        answerBox = findViewById(R.id.instantAnswer)
        topPaddingWhenSmartBoxIsShown = (dp(82) + sp(46)).toInt()
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
        searchTxt.setOnEditorActionListener { v, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> onPause()
                EditorInfo.IME_ACTION_GO -> {
                    val a = grid.adapter
                    if (a != null && a.count != 0) {
                        a.getItem(0).let { it as LauncherItem }.open(this, v, -1)
                    } else {
                        searchOnDuckDuckGo(this, searchTxt.text.toString())
                    }
                }
            }
            false
        }
        searchTxt.imeOptions = if (Settings["search:enter_is_go", false]) EditorInfo.IME_ACTION_GO else EditorInfo.IME_ACTION_DONE
        findViewById<View>(R.id.searchbar).background = ShapeDrawable().apply {
            val tr = dp(Settings["searchradius", 0])
            shape = RoundRectShape(floatArrayOf(tr, tr, tr, tr, 0f, 0f, 0f, 0f), null, null)
            paint.color = Settings["searchcolor", 0x33000000]
        }
        if (Tools.canBlurSearch) {
            val arr = arrayOf(BitmapDrawable(Wallpaper.blurredWall(Settings["search:blur:rad", 15f])), FastColorDrawable(Settings["searchUiBg", -0x78000000]))
            window.setBackgroundDrawable(LayerDrawable(arr))
        } else {
            window.setBackgroundDrawable(FastColorDrawable(Settings["searchUiBg", -0x78000000]))
        }
        searchTxt.setTextColor(Settings["searchtxtcolor", -0x1])
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
                    startActivity(Intent(this@SearchActivity, Home::class.java))
                }
            }
        }

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

        if (Settings["search:asHome", false]) {
            (getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps).registerCallback(AppCallback(::loadApps))
            loadApps()
        }
    }

    fun loadApps() {
        val iconConfig = IconConfig(
            size = dp(65).toInt(),
            density = resources.configuration.densityDpi,
            packPackages = arrayOf(Settings["iconpack", "system"]),
        )
        appLoader.async(applicationContext, iconConfig) {
            App.onFinishLoad(it.list, it.sections, it.hidden, it.byName)
            Home.instance.runOnUiThread {
                onAppLoaderEnd()
            }
        }
    }

    private fun search(string: String) {
        currentString = string
        val searchOptimizedString = searchOptimize(string)
        val packageSearch = Settings["search:use_package_names", false]
        if (string.isEmpty()) {
            grid.adapter = SearchAdapter(this, listOf())
            answerBox.visibility = View.GONE
            return
        }
        stillWantIP = false
        val results = ArrayList<LauncherItem>()
        val showHidden = searchOptimizedString == searchOptimize("hidden") || searchOptimizedString == searchOptimize("hiddenapps")
        if (showHidden) {
            val app = LauncherItem.make("Hidden apps", getDrawable(R.drawable.hidden_apps)) { context, _, _ ->
                context.startActivity(Intent(applicationContext, HiddenAppsActivity::class.java))
            }
            results.add(app)
        }
        val appLoaderThread = thread (isDaemon = true) {
            var i = 0
            for (app in Global.apps) {
                if (searchOptimize(app.label).contains(searchOptimizedString) ||
                    app.label.contains(string) ||
                    packageSearch && (
                        searchOptimize(app.packageName).contains(searchOptimizedString) ||
                        app.packageName.contains(string)
                    )) {
                    results.add(app)
                    i++
                    continue
                }
                for (word in app.label.split(' ', ',', '.', '-', '+', '&', '_')) {
                    if (searchOptimize(word).contains(searchOptimizedString) || word.contains(string)) {
                        results.add(app)
                        i++
                        break
                    }
                }
                if (i > 30) break
            }
            if (Settings["search:include_hidden_apps", false]) {
                for (app in App.hidden) {
                    if (searchOptimize(app.label).contains(searchOptimizedString) ||
                        app.label.contains(string) ||
                        packageSearch && (
                            searchOptimize(app.packageName).contains(searchOptimizedString) ||
                            app.packageName.contains(string)
                        )) {
                        results.add(app)
                        i++
                        continue
                    }
                    for (word in app.label.split(' ', ',', '.', '-', '+', '&', '_')) {
                        if (searchOptimize(word).contains(searchOptimizedString) || word.contains(string)) {
                            results.add(app)
                            i++
                            break
                        }
                    }
                    if (i > 30) break
                }
            }
        }
        val settingLoaderThread = thread (isDaemon = true) {
            if (Settings["search:use_shortcuts", true]) kotlin.runCatching {
                val settingList = SettingsItem.getList()
                var i = 0
                for (setting in settingList) {
                    if (searchOptimize(setting.label!!).contains(searchOptimizedString) ||
                        setting.label!!.contains(string) ||
                        searchOptimize(setting.action).contains(searchOptimizedString) ||
                        setting.action.contains(string)) {
                        results.add(setting)
                        i++
                        continue
                    }
                    for (word in setting.label!!.split(' ', '-', '_')) {
                        if (searchOptimize(word).contains(searchOptimizedString) || word.contains(string)) {
                            results.add(setting)
                            i++
                            break
                        }
                    }
                    if (i > 10) break
                }
            }
        }
        if (canReadContacts && Settings["search:use_contacts", true]) {
            kotlin.runCatching {
                val contactList = ContactItem.getList()
                var i = 0
                for (contact in contactList) {
                    if (searchOptimize(contact.label).contains(searchOptimizedString) ||
                        contact.label.contains(string) ||
                        contact.phone.contains(searchOptimizedString) ||
                        contact.phone.contains(string)) {
                        results.add(contact)
                        i++
                        continue
                    }
                    for (word in contact.label.split(' ', '-', '_')) {
                        if (searchOptimize(word).contains(searchOptimizedString) || word.contains(string)) {
                            results.add(contact)
                            i++
                            break
                        }
                    }
                    if (i > 16) break
                }
            }
        }

        kotlin.runCatching {
            appLoaderThread.join()
            settingLoaderThread.join()
        }

        try {
            if (results.isEmpty()) {
                results.add(LauncherItem.make(getString(R.string.x_on_duckduckgo, string), daxResultIcon) { context, _, _ ->
                    searchOnDuckDuckGo(context, string)
                })
            } else Sort.labelSort(results)
            grid.adapter = SearchAdapter(this, results)
            grid.onItemClickListener = OnItemClickListener { _, view, i, _ ->
                results[i].open(this, view, -1)
            }
            grid.setOnItemLongClickListener { _, view, i, _ ->
                val app = results[i]
                if (app is App) {
                    ItemLongPress.onItemLongPress(this, view, app, null, null)
                }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var isShowingSmartCard = false
        try {
            val (result, operation) = Parser(string).parseOperation()
            smartBox.visibility = View.VISIBLE
            isShowingSmartCard = true
            findViewById<TextView>(R.id.type).setText(R.string.math_operation)
            findViewById<TextView>(R.id.result).text = "$operation = $result"
        } catch (e: Exception) {
            e.printStackTrace()
            val words =
                string.lowercase(Locale.getDefault()).split(' ', ',', '.', '-', '+', '&', '_')
            if (words.contains("ip")) {
                stillWantIP = true
                smartBox.visibility = View.VISIBLE
                isShowingSmartCard = true
                smartBox.findViewById<TextView>(R.id.type).setText(R.string.ip_address_external)
                smartBox.findViewById<TextView>(R.id.result).text = ""
                TextLoader.load("https://checkip.amazonaws.com") {
                    runOnUiThread {
                        if (stillWantIP) smartBox.findViewById<TextView>(R.id.result).text =
                            it.trimEnd()
                    }
                }
            } else if (
                words.contains("pi") ||
                words.contains("π")
            ) {
                smartBox.visibility = View.VISIBLE
                isShowingSmartCard = true
                findViewById<TextView>(R.id.type).setText(R.string.value_of_pi)
                findViewById<TextView>(R.id.result).text = "\u03c0 = ${Math.PI}"
            } else {
                smartBox.visibility = View.GONE
            }
        }
        answerBox.visibility = View.GONE
        if (results.size < 6 && !isShowingSmartCard && string.length > 3 && !showHidden && Settings["search:ddg_instant_answers", true]) {
            DuckInstantAnswer.load(string, "posidon.launcher") { instantAnswer ->
                if (currentString == string) {
                    runOnUiThread {
                        answerBox.visibility = View.VISIBLE
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

    fun searchOnDuckDuckGo(context: Context, string: String) {
        val encoded = Uri.encode(string)
        val url = "https://duckduckgo.com/?q=$encoded&t=posidon.launcher"
        val uri = Uri.parse(url)
        val i = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(i, ActivityOptions.makeCustomAnimation(context, R.anim.slideup, R.anim.slidedown).toBundle())
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        hideKeyboard()

        if (Settings["kustom:variables:enable", false]) {
            Kustom[this, "posidon", "screen"] = "?"
        }

        finish()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            if (Settings["kustom:variables:enable", false]) {
                Kustom[this, "posidon", "screen"] = "search"
            }
        }
    }

    companion object {
        fun open(context: Context) = context.startActivity(
            Intent(context, if (Settings["dev:console", false]) ConsoleActivity::class.java else SearchActivity::class.java),
            ActivityOptions.makeCustomAnimation(context, R.anim.fadein, R.anim.fadeout).toBundle())
    }
}