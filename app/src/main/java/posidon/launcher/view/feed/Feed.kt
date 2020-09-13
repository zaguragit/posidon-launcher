package posidon.launcher.view.feed

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import posidon.launcher.Global
import posidon.launcher.Home
import posidon.launcher.LauncherMenu
import posidon.launcher.R
import posidon.launcher.external.Widget
import posidon.launcher.feed.news.FeedLoader
import posidon.launcher.feed.notifications.NotificationService
import posidon.launcher.storage.Settings
import posidon.launcher.tools.dp
import posidon.launcher.tools.getStatusBarHeight
import posidon.launcher.tools.onEnd
import posidon.launcher.view.NestedScrollView
import posidon.launcher.view.drawer.BottomDrawerBehavior
import posidon.launcher.view.drawer.DrawerView

class Feed : NestedScrollView {

    constructor(c: Context) : super(c)
    constructor(c: Context, a: AttributeSet?) : super(c, a)
    constructor(c: Context, a: AttributeSet?, sa: Int) : super(c, a, sa)

    private val sections = ArrayList<FeedSection>()

    val desktopContent = LinearLayout(context).apply {
        orientation = VERTICAL
        setOnLongClickListener(LauncherMenu())
    }

    val spinner = ProgressBar(context, null, R.style.Widget_AppCompat_ProgressBar).apply {
        indeterminateDrawable = resources.getDrawable(R.drawable.progress, null)
        indeterminateTintMode = PorterDuff.Mode.MULTIPLY
        visibility = GONE
    }

    var musicCard: MusicCard? = null
        private set

    var notifications: NotificationCards? = null
        private set

    var newsCards: NewsCards? = null
        private set

    init {
        overScrollMode = OVER_SCROLL_ALWAYS
        addView(RelativeLayout(context).apply {
            addView(desktopContent, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
            addView(spinner, LayoutParams(MATCH_PARENT, 48.dp.toInt()).apply {
                topMargin = 72.dp.toInt()
            })
        }, LayoutParams(MATCH_PARENT, WRAP_CONTENT))

        isNestedScrollingEnabled = false
        isSmoothScrollingEnabled = false
    }

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

    fun internalAdd(section: FeedSection, i: Int) {
        sections.add(i, section)
        desktopContent.addView(section as View, i)
        section.onAdd(this)
    }

    fun updateTheme(activity: Activity, drawer: DrawerView) {
        spinner.indeterminateDrawable.setTint(Global.accentColor)

        for (section in sections) {
            section as View
            section.updateTheme(activity)
        }

        if (Settings["hidefeed", false]) {
            newsCards?.hide()
            setOnScrollChangeListener { _: androidx.core.widget.NestedScrollView, _, y, _, oldY ->
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
            setOnScrollChangeListener { _: androidx.core.widget.NestedScrollView, _, y, _, oldY ->
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
            setPadding(0, context.getStatusBarHeight() - 12.dp.toInt(), 0, 0)
        }
        isVerticalFadingEdgeEnabled = fadingEdge

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
                    spinner.animate().translationY(-(72).dp).alpha(0f).onEnd {
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

    fun update() {
        sections.clear()
        desktopContent.removeAllViews()
        val s = getSectionsFromSettings()
        for (section in s.reversed()) {
            internalAdd(FeedSection(context, section).also {
                when (it) {
                    is MusicCard -> musicCard = it
                    is NotificationCards -> notifications = it
                    is NewsCards -> newsCards = it
                }
            }, 0)
        }
    }

    companion object {

        fun getSectionsFromSettings() = Settings.getStringsOrSet("feed:sections") {
            arrayListOf("music", "notifications", "news")
        }

        fun selectFeedSectionToAdd(activity: Activity, onSelect: (FeedSection) -> Unit) {
            val sections = getSectionsFromSettings()
            BottomSheetDialog(activity, R.style.bottomsheet).apply {
                setContentView(R.layout.feed_section_options)
                window!!.findViewById<View>(R.id.design_bottom_sheet).setBackgroundResource(R.drawable.bottom_sheet)
                findViewById<View>(R.id.notifications_section)!!.run {
                    if (sections.contains("notifications")) {
                        visibility = GONE
                    } else setOnClickListener {
                        onSelect(NotificationCards(context))
                        dismiss()
                    }
                }
                findViewById<View>(R.id.news_section)!!.run {
                    if (sections.contains("news")) {
                        visibility = GONE
                    } else setOnClickListener {
                        onSelect(NewsCards(context))
                        dismiss()
                    }
                }
                findViewById<View>(R.id.starred_contacts_section)!!.run {
                    if (sections.contains("starred_contacts")) {
                        visibility = GONE
                    } else setOnClickListener {
                        onSelect(ContactCardView(context))
                        dismiss()
                    }
                }
                findViewById<View>(R.id.music_section)!!.run {
                    if (sections.contains("music")) {
                        visibility = GONE
                    } else setOnClickListener {
                        onSelect(MusicCard(context))
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
}