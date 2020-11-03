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
import com.google.android.material.bottomsheet.BottomSheetDialog
import posidon.launcher.Global
import posidon.launcher.Home
import posidon.launcher.LauncherMenu
import posidon.launcher.R
import posidon.launcher.external.Widget
import posidon.launcher.feed.news.FeedLoader
import posidon.launcher.feed.notifications.NotificationService
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Gestures
import posidon.launcher.tools.dp
import posidon.launcher.tools.getStatusBarHeight
import posidon.launcher.tools.onEnd
import posidon.launcher.view.NestedScrollView
import posidon.launcher.view.drawer.BottomDrawerBehavior
import posidon.launcher.view.drawer.DrawerView

class Feed : FrameLayout {

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
        val scaleGestureDetector = ScaleGestureDetector(context, Gestures.PinchListener())
        setOnTouchListener { _, event ->
            if (hasWindowFocus()) {
                scaleGestureDetector.onTouchEvent(event)
                false
            } else true
        }

        addView(desktopContent, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
    }

    val spinner = ProgressBar(context, null, R.style.Widget_AppCompat_ProgressBar).apply {
        indeterminateDrawable = resources.getDrawable(R.drawable.progress, null)
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
        addView(spinner, LayoutParams(48.dp.toInt(), 48.dp.toInt()).apply {
            topMargin = 72.dp.toInt()
            gravity = Gravity.CENTER_HORIZONTAL
        })
    }

    var onTopOverScroll by scroll::onTopOverScroll
    var onBottomOverScroll by scroll::onBottomOverScroll

    fun add(section: FeedSection) = add(section, 0)
    fun add(section: FeedSection, i: Int) {
        getSectionsFromSettings().add(i,section.toString())
        Settings.apply()
        sections.add(i, section)
        desktopContent.addView(section as View, i)
        section.onAdd(this)
    }
    fun remove(section: FeedSection) {
        getSectionsFromSettings().remove(section.toString())
        Settings.apply()
        sections.remove(section)
        desktopContent.removeView(section as View)
        section.onDelete(this)
    }

    fun internalAdd(section: FeedSection, i: Int): FeedSection {
        sections.add(i, section)
        desktopContent.addView(section as View, i)
        section.onAdd(this)
        return section
    }

    private fun updateTheme(activity: Activity, drawer: DrawerView) {
        spinner.indeterminateDrawable.setTint(Global.accentColor)

        if (Settings["hidefeed", false]) {
            newsCards?.hide()
            scroll.setOnScrollChangeListener { _: androidx.core.widget.NestedScrollView, _, y, _, oldY ->
                val a = 6.dp
                val distance = oldY - y
                if (y > a) {
                    newsCards?.show()
                    if (distance > a || y >= desktopContent.height - drawer.dock.dockHeight - height) {
                        if (!LauncherMenu.isActive) {
                            drawer.state = BottomDrawerBehavior.STATE_COLLAPSED
                        }
                    } else if (distance < -a) {
                        drawer.state = BottomDrawerBehavior.STATE_HIDDEN
                    }
                } else {
                    if (!LauncherMenu.isActive) {
                        drawer.state = BottomDrawerBehavior.STATE_COLLAPSED
                    }
                    if (y < a && oldY >= a) {
                        newsCards?.hide()
                    }
                }
            }
        } else {
            newsCards?.show()
            scroll.setOnScrollChangeListener { _: androidx.core.widget.NestedScrollView, _, y, _, oldY ->
                val a = 6.dp
                val distance = oldY - y
                if (distance > a || y < a || y + height >= desktopContent.height - drawer.dock.dockHeight) {
                    if (!LauncherMenu.isActive) {
                        drawer.state = BottomDrawerBehavior.STATE_COLLAPSED
                    }
                } else if (distance < -a) {
                    drawer.state = BottomDrawerBehavior.STATE_HIDDEN
                }
            }
        }
        val fadingEdge = Settings["feed:fading_edge", true]
        if (fadingEdge && !Settings["hidestatus", false]) {
            scroll.setPadding(0, context.getStatusBarHeight() - 12.dp.toInt(), 0, 0)
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
                        Home.setDock()
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
            try { activity.startService(Intent(context, NotificationService::class.java)) }
            catch (e: Exception) {}
        }
    }

    fun loadNews(activity: Activity) {
        val newsCards = newsCards
        if (newsCards == null || spinner.visibility == VISIBLE) {
            return
        }
        if (Settings["feed:show_spinner", true]) {
            spinner.visibility = VISIBLE
            spinner.animate().translationY(0f).alpha(1f).setListener(null)
            FeedLoader.loadFeed { success, items ->
                activity.runOnUiThread {
                    if (success) {
                        newsCards.updateFeed(items)
                    }
                    spinner.animate().translationY((-72).dp).alpha(0f).onEnd {
                        spinner.visibility = GONE
                    }
                }
            }
        } else FeedLoader.loadFeed { success, items ->
            if (success) activity.runOnUiThread {
                newsCards.updateFeed(items)
            }
        }
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

    fun update(activity: Activity, drawer: DrawerView) {
        val map = HashMap<String, FeedSection>()
        for (s in sections) {
            map[s.toString()] = s
        }
        sections.clear()
        desktopContent.removeAllViews()
        val s = getSectionsFromSettings()
        musicCard = null
        notifications = null
        newsCards = null
        for (section in s.reversed()) {
            internalAdd(map[section] ?: FeedSection(activity, section), 0).also {
                when (it) {
                    is MusicCard -> musicCard = it
                    is NotificationCards -> notifications = it
                    is NewsCards -> newsCards = it
                }
                it.updateTheme(activity)
            }
        }
        updateTheme(activity, drawer)
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
                    Widget.selectWidget(activity)
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