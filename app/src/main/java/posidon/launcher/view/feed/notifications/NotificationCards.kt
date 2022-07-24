package posidon.launcher.view.feed.notifications

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.luminance
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.conveniencelib.units.dp
import io.posidon.android.conveniencelib.units.toFloatPixels
import io.posidon.android.conveniencelib.units.toPixels
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.feed.notifications.NotificationService
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Gestures
import posidon.launcher.view.feed.Feed
import posidon.launcher.view.feed.FeedSection
import posidon.launcher.view.recycler.LinearLayoutManager

class NotificationCards : LinearLayout, FeedSection {

    constructor(c: Context) : super(c)
    constructor(c: Context, a: AttributeSet?) : super(c, a)
    constructor(c: Context, a: AttributeSet?, sa: Int) : super(c, a, sa)

    private val notificationAdapter = NotificationAdapter()

    private val notifications = RecyclerView(context).apply {
        isNestedScrollingEnabled = false
        layoutManager = LinearLayoutManager(context)
        adapter = notificationAdapter
    }


    private val arrowUp = ImageView(context).apply {
        setImageResource(R.drawable.arrow_up)
        visibility = GONE
        imageTintList = ColorStateList.valueOf(0xffffffff.toInt())
    }

    private val parentNotificationTitle = TextView(context).apply {
        textSize = 17f
        gravity = Gravity.CENTER_VERTICAL
        run {
            val h = 8.dp.toPixels(context)
            setPadding(h, 0, h, 0)
        }
    }

    private val parentNotificationBtn = ImageView(context).apply {
        setImageResource(R.drawable.ic_notification)
        run {
            val p = 4.dp.toPixels(context)
            setPaddingRelative(p, p, p, p)
        }
    }

    private val parentNotification = LinearLayout(context).apply {

        orientation = HORIZONTAL
        run {
            val v = 12.dp.toPixels(context)
            setPadding(v, v, v, v)
        }

        addView(arrowUp, LayoutParams(MATCH_PARENT, 48.dp.toPixels(context)))
        addView(parentNotificationTitle, LayoutParams(0, 48.dp.toPixels(context), 1f))
        addView(parentNotificationBtn, LayoutParams(48.dp.toPixels(context), 48.dp.toPixels(context)))

        setOnLongClickListener(Gestures::onLongPress)
        setOnClickListener {
            if (notifications.visibility == VISIBLE)
                collapse() else expand()
        }
    }

    init {
        orientation = VERTICAL
        addView(parentNotification, LayoutParams(MATCH_PARENT, 72.dp.toPixels(context)))
        addView(notifications, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
    }

    fun collapse() {
        val firstScroll = feed.scroll.scrollY
        val firstMaxScroll = feed.desktopContent.height
        notifications.visibility = GONE
        arrowUp.visibility = GONE
        parentNotification.background.alpha = 255
        feed.scroll.post {
            feed.scrollUpdate(firstScroll, firstMaxScroll)
        }
    }

    fun expand() {
        val firstScroll = feed.scroll.scrollY
        val firstMaxScroll = feed.desktopContent.height
        notifications.visibility = VISIBLE
        arrowUp.visibility = VISIBLE
        parentNotification.background.alpha = 127
        feed.scroll.post {
            feed.scrollUpdate(firstScroll, firstMaxScroll)
        }
    }

    var isCollapsingEnabled = true
        set(value) {
            field = value
            if (value) {
                collapse()
                parentNotification.visibility = VISIBLE
            } else {
                expand()
                parentNotification.visibility = GONE
            }
        }

    private lateinit var feed: Feed

    override fun onAdd(feed: Feed, i: Int) {
        this.feed = feed
    }

    fun update() {
        if (Settings["collapseNotifications", false]) {
            if (NotificationService.notificationsAmount > 1) {
                parentNotification.visibility = VISIBLE
                parentNotificationTitle.text = resources.getQuantityString(
                    R.plurals.num_notifications,
                    NotificationService.notificationsAmount,
                    NotificationService.notificationsAmount
                )
                if (notifications.visibility == VISIBLE) {
                    parentNotification.background.alpha = 127
                    arrowUp.visibility = VISIBLE
                } else {
                    parentNotification.background.alpha = 255
                    arrowUp.visibility = GONE
                }
            } else {
                parentNotification.visibility = GONE
                notifications.visibility = VISIBLE
            }
        }
        notificationAdapter.update(NotificationService.notificationGroups)
    }

    override fun updateTheme(activity: Activity) {
        val marginX = Settings["feed:card_margin_x", 16].dp.toPixels(context)
        val marginY = Settings["feed:card_margin_y", 9].dp.toPixels(context)
        (parentNotification.layoutParams as LayoutParams).apply {
            leftMargin = marginX
            rightMargin = marginX
            topMargin = marginY
            bottomMargin = marginY
        }
        parentNotification.layoutParams = parentNotification.layoutParams
        val notificationBackground = ShapeDrawable()
        val r = Settings["feed:card_radius", 15].dp.toFloatPixels(context)
        notificationBackground.shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
        notificationBackground.paint.color = Settings["notificationbgcolor", -0x1]
        parentNotification.background = notificationBackground
        parentNotificationTitle.setTextColor(Settings["notificationtitlecolor", -0xeeeded])
        parentNotificationBtn.imageTintList = ColorStateList.valueOf(if (Global.accentColor.luminance > .6f) -0x1000000 else -0x1)
        parentNotificationBtn.backgroundTintList = ColorStateList.valueOf(Global.accentColor)
        parentNotificationBtn.imageTintList = ColorStateList.valueOf(Global.accentColor)
        parentNotificationBtn.backgroundTintList = ColorStateList.valueOf(Global.accentColor and 0x00ffffff or 0x33000000)
        isCollapsingEnabled = Settings["collapseNotifications", false] && NotificationService.notificationsAmount > 1
        val restAtBottom = Settings["feed:rest_at_bottom", false]
        (notifications.layoutManager as LinearLayoutManager).apply {
            reverseLayout = restAtBottom
        }
        removeView(parentNotification)
        addView(parentNotification, if (restAtBottom) 1 else 0)
    }

    override fun onPause() {
        if (Settings["collapseNotifications", false] && NotificationService.notificationsAmount > 1) {
            collapse()
        }
    }

    override fun toString() = "notifications"
}