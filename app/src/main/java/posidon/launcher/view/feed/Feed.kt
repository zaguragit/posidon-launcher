package posidon.launcher.view.feed

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import posidon.android.conveniencelib.dp
import posidon.android.conveniencelib.onEnd
import posidon.android.loader.rss.RssItem
import posidon.android.loader.rss.RssLoader
import posidon.android.loader.rss.RssSource
import posidon.launcher.Global
import posidon.launcher.Home
import posidon.launcher.LauncherMenu
import posidon.launcher.R
import posidon.launcher.external.widgets.Widget
import posidon.launcher.feed.news.chooser.FeedChooser
import posidon.launcher.feed.notifications.NotificationService
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Gestures
import posidon.launcher.tools.getStatusBarHeight
import posidon.launcher.view.NestedScrollView
import posidon.launcher.view.drawer.DrawerView
import posidon.launcher.view.feed.news.NewsCards
import posidon.launcher.view.feed.notifications.NotificationCards
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.abs

class Feed : FrameLayout {

    lateinit var drawer: DrawerView

    inline fun init(drawer: DrawerView) {
        this.drawer = drawer
        onTopOverScroll = {
            if (!LauncherMenu.isActive && drawer.state != BottomSheetBehavior.STATE_EXPANDED) {
                Gestures.performTrigger(Settings["gesture:feed:top_overscroll", Gestures.PULL_DOWN_NOTIFICATIONS])
            }
        }
        onBottomOverScroll = {
            if (!LauncherMenu.isActive) {
                Gestures.performTrigger(Settings["gesture:feed:bottom_overscroll", Gestures.OPEN_APP_DRAWER])
            }
        }
    }

    constructor(c: Context) : super(c)
    constructor(c: Context, a: AttributeSet?) : super(c, a)
    constructor(c: Context, a: AttributeSet?, sa: Int) : super(c, a, sa)

    private val sections = ArrayList<FeedSection>()

    val desktopContent = LinearLayout(context).apply {
        orientation = VERTICAL
        setOnLongClickListener(Gestures::onLongPress)
    }

    val scroll = NestedScrollView(context).apply {
        overScrollMode = OVER_SCROLL_ALWAYS
        isNestedScrollingEnabled = false
        isSmoothScrollingEnabled = false

        setOnLongClickListener(Gestures::onLongPress)
        setOnTouchListener(::onScrollTouch)

        addView(desktopContent, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
    }

    private val scaleGestureDetector = ScaleGestureDetector(context, Gestures.PinchListener)
    private fun onScrollTouch(v: View?, event: MotionEvent?) = if (hasWindowFocus()) {
        scaleGestureDetector.onTouchEvent(event)
        false
    } else true

    val spinner = ProgressBar(context, null, R.style.Widget_AppCompat_ProgressBar).apply {
        indeterminateDrawable = ContextCompat.getDrawable(context, R.drawable.progress)
        indeterminateTintMode = PorterDuff.Mode.MULTIPLY
        isIndeterminate = true
        visibility = GONE
    }

    var musicCard: MusicCard? = null
        private set

    var notifications: NotificationCards? = null
        private set

    var newsCards: NewsCards? = null
        private set

    init {
        addView(scroll, LayoutParams(MATCH_PARENT, MATCH_PARENT))
        addView(spinner, LayoutParams(dp(48).toInt(), dp(48).toInt()).apply {
            topMargin = dp(72).toInt()
            gravity = Gravity.CENTER_HORIZONTAL
        })
    }

    var onTopOverScroll by scroll::onTopOverScroll
    var onBottomOverScroll by scroll::onBottomOverScroll

    fun clearSections() {
        sections.clear()
        desktopContent.removeAllViews()
        musicCard = null
        notifications = null
        newsCards = null
    }

    inline fun add(section: FeedSection) {
        getSectionsFromSettings().add(section.toString())
        Settings.apply()
        internalAdd(section)
    }

    inline fun add(section: FeedSection, i: Int) {
        getSectionsFromSettings().add(i, section.toString())
        Settings.apply()
        internalAdd(section, i)
        updateIndices(i + 1)
    }

    fun remove(section: FeedSection) {
        getSectionsFromSettings().remove(section.toString()).let { if (!it) println("Couldn't remove feed section: $section") }
        Settings.apply()
        sections.remove(section)
        desktopContent.removeView(section as View)
        section.onDelete(this)
    }

    fun remove(section: FeedSection, i: Int) {
        getSectionsFromSettings().removeAt(i)
        Settings.apply()
        sections.removeAt(i)
        desktopContent.removeViewAt(i)
        section.onDelete(this)
        updateIndices(i)
    }

    fun internalAdd(section: FeedSection): FeedSection {
        val i = sections.size
        return internalAdd(section, i)
    }

    fun internalAdd(section: FeedSection, i: Int): FeedSection {
        sections.add(section)
        desktopContent.addView(section as View)
        section.onAdd(this, i)
        return section
    }

    private fun updateTheme(activity: Home, drawer: DrawerView) {
        spinner.indeterminateDrawable.setTint(Global.accentColor)

        if (Settings["hidefeed", false]) {
            newsCards?.hide()
            scroll.setOnScrollChangeListener { _, _, y, _, oldY ->
                val a = dp(6)
                if (y > a) {
                    val distance = oldY - y
                    newsCards?.show()
                    handleDockOnScroll(distance, a, y, drawer)
                } else {
                    if (!LauncherMenu.isActive) {
                        drawer.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                    if (y < a && oldY >= a) {
                        newsCards?.hide()
                    }
                }
            }
        } else {
            newsCards?.show()
            scroll.setOnScrollChangeListener { _, _, y, _, oldY ->
                val a = dp(6)
                val distance = oldY - y
                handleDockOnScroll(distance, a, y, drawer)
            }
        }
        val fadingEdge = Settings["feed:fading_edge", true]
        if (fadingEdge && !Settings["hidestatus", false]) {
            scroll.setPadding(0, context.getStatusBarHeight() - dp(12).toInt(), 0, 0)
        }
        scroll.isVerticalFadingEdgeEnabled = fadingEdge

        if (notifications != null || Settings["notif:badges", true]) {
            NotificationService.onUpdate = {
                try {
                    if (notifications != null) activity.runOnUiThread {
                        notifications?.update()
                    }
                    if (Settings["notif:badges", true]) activity.runOnUiThread {
                        drawer.drawerGrid.invalidateViews()
                        drawer.dock.loadApps()
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
            try { activity.startService(Intent(context, NotificationService::class.java)) }
            catch (e: Exception) {}
        }
    }

    private fun handleDockOnScroll(distance: Int, threshold: Float, y: Int, drawer: DrawerView) {
        if (distance > threshold || y < threshold || y >= desktopContent.height - drawer.dock.dockHeight - height) {
            if (!LauncherMenu.isActive) {
                drawer.isHideable = false
                drawer.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        } else if (distance < -threshold) {
            drawer.isHideable = true
            drawer.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    fun loadNews() {
        val newsCards = newsCards
        if (newsCards == null || spinner.visibility == VISIBLE) {
            return
        }
        if (Settings["feed:show_spinner", true]) {
            spinner.visibility = VISIBLE
            spinner.animate().translationY(0f).alpha(1f).setListener(null)
            loadFeed { erroredSources, items ->
                val success = items.isNotEmpty()
                onNewsLoaded(success, newsCards, items)
                post {
                    spinner.animate().translationY(dp(-72)).alpha(0f).onEnd {
                        spinner.visibility = GONE
                    }
                }
            }
        } else loadFeed { erroredSources, items ->
            val success = items.isNotEmpty()
            onNewsLoaded(success, newsCards, items)
        }
    }

    private fun onNewsLoaded(success: Boolean, newsCards: NewsCards, items: List<RssItem>) {
        if (success) post {
            var firstScroll = 0
            var firstMaxScroll = 0
            scroll.post {
                firstScroll = scroll.scrollY
                firstMaxScroll = desktopContent.height
            }
            newsCards.updateFeed(items)
            scroll.post {
                scrollUpdate(firstScroll, firstMaxScroll)
            }
        }
    }

    private inline fun loadFeed(
        noinline onFinished: (erroredSources: List<RssSource>, items: List<RssItem>) -> Unit
    ): Thread {
        val maxAge = Settings["news:max_days_age", 5]
        val deleted = Settings.getStringsOrSetEmpty("feed:deleted_articles")
        val today = Calendar.getInstance()[Calendar.DAY_OF_YEAR]
        val deletedIter = deleted.iterator()
        for (article in deletedIter) {
            val day = article.substringBefore(':').toDouble()
            if (abs(day - today) >= if (maxAge == 0) 60 else maxAge) {
                deletedIter.remove()
            }
        }
        Settings["feed:deleted_articles"] = deleted
        val sources = Settings["feedUrls", FeedChooser.defaultSources].split("|")
        return RssLoader.load(sources, maxItems = Settings["feed:max_news", 48], filter = { title, link, time ->
            val isNewEnough = if (maxAge == 0) true else {
                val day = Calendar.getInstance().apply { this.time = time }[Calendar.DAY_OF_YEAR]
                abs(day - today) < maxAge
            }
            if (isNewEnough) {
                if (Settings["feed:delete_articles", false]) {
                    var show = true
                    for (string in deleted) {
                        if (string.substringAfter(':') == "$link:$title") {
                            show = false; break
                        }
                    }
                    show
                } else true
            } else false
        }, doSorting = true, onFinished = onFinished)
    }

    inline fun scrollUpdate(firstScroll: Int, firstMaxScroll: Int) {
        scroll.scrollTo(0, if (Settings["feed:rest_at_bottom", false]) firstScroll + desktopContent.height - firstMaxScroll else firstScroll)
    }

    fun onResume(activity: Activity) {
        for (section in sections) {
            section.onResume(activity)
        }
    }

    fun onPause() {
        for (section in sections) {
            section.onPause()
        }
    }

    fun update(activity: Home, drawer: DrawerView) {
        val map = HashMap<String, FeedSection>()
        for (s in sections) {
            map[s.toString()] = s
        }
        clearSections()
        val s = getSectionsFromSettings()
        var i = 0
        var removeCount = 0
        while (i < s.size) {
            (map[s[i]] ?: FeedSection(activity, s, i))?.let { section ->
                section as View
                if (section.parent == desktopContent) {
                    remove(section, i - removeCount++)
                } else {
                    internalAdd(section)
                    when (section) {
                        is MusicCard -> musicCard = section
                        is NotificationCards -> notifications = section
                        is NewsCards -> newsCards = section
                    }
                    section.updateTheme(activity)
                }
            }
            i++
        }
        updateTheme(activity, drawer)
    }

    fun updateIndices(fromI: Int) {
        for (i in fromI until sections.size)
            sections[i].updateIndex(i)
    }

    fun onAppsLoaded() {
        val iterator = iterator()
        for (s in iterator) {
            s.onAppsLoaded(iterator)
        }
    }

    operator fun iterator() = object : MutableIterator<FeedSection> {

        private var limit: Int = sections.size

        private var cursor = 0
        private var lastRet = -1 // index of last element returned; -1 if no such

        override fun hasNext(): Boolean {
            return cursor < limit
        }

        override fun next(): FeedSection {
            val i = cursor
            if (i >= limit) throw NoSuchElementException()
            cursor = i + 1
            return sections[i.also { lastRet = it }]
        }

        override fun remove() {
            check(lastRet >= 0)
            try {
                remove(sections[lastRet])
                cursor = lastRet
                lastRet = -1
                limit--
            } catch (ex: IndexOutOfBoundsException) {
                throw ConcurrentModificationException()
            }
        }
    }

    companion object {

        fun getSectionsFromSettings() = Settings.getStringsOrSet("feed:sections") {
            arrayListOf("music", "notifications", "news")
        }

        fun selectFeedSectionToAdd(activity: Activity, onSelect: (String) -> Unit) {
            val sections = getSectionsFromSettings()
            BottomSheetDialog(activity, R.style.bottomsheet).apply {
                setContentView(R.layout.feed_section_options)
                window!!.findViewById<View>(R.id.design_bottom_sheet).setBackgroundResource(R.drawable.bottom_sheet)
                findViewById<View>(R.id.notifications_section)!!.run {
                    if (sections.contains("notifications")) {
                        visibility = GONE
                    } else setOnClickListener {
                        onSelect("notifications")
                        dismiss()
                    }
                }
                findViewById<View>(R.id.news_section)!!.run {
                    if (sections.contains("news")) {
                        visibility = GONE
                    } else setOnClickListener {
                        onSelect("news")
                        dismiss()
                    }
                }
                findViewById<View>(R.id.starred_contacts_section)!!.run {
                    if (sections.contains("starred_contacts")) {
                        visibility = GONE
                    } else setOnClickListener {
                        onSelect("starred_contacts")
                        dismiss()
                    }
                }
                findViewById<View>(R.id.music_section)!!.run {
                    if (sections.contains("music")) {
                        visibility = GONE
                    } else setOnClickListener {
                        onSelect("music")
                        dismiss()
                    }
                }
                findViewById<View>(R.id.widget_section)!!.setOnClickListener {
                    Widget.selectWidget(activity) {
                        onSelect(it.toString())
                    }
                    dismiss()
                }
                findViewById<View>(R.id.spacer_section)!!.setOnClickListener {
                    onSelect("spacer:96")
                    dismiss()
                }
                show()
            }
        }
    }


    private var oldPointerY = 0f
    private var newPointerY = 0f
    private var timeSincePress = 0L

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                timeSincePress = System.currentTimeMillis()
                oldPointerY = ev.y
                newPointerY = ev.y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                oldPointerY = newPointerY
                newPointerY = ev.y
                if (System.currentTimeMillis() - timeSincePress < 240 && ev.pointerCount == 1) {
                    if (oldPointerY < newPointerY) {
                        onTopOverScroll()
                    } else if (oldPointerY > newPointerY) {
                        onBottomOverScroll()
                    }
                }
            }
        }
        return super.onTouchEvent(ev)
    }
}