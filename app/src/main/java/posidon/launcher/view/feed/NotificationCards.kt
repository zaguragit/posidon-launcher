package posidon.launcher.view.feed

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
import androidx.recyclerview.widget.RecyclerView
import posidon.launcher.Home
import posidon.launcher.LauncherMenu
import posidon.launcher.R
import posidon.launcher.feed.notifications.NotificationAdapter
import posidon.launcher.feed.notifications.NotificationService
import posidon.launcher.storage.Settings
import posidon.launcher.tools.ColorTools
import posidon.launcher.tools.dp
import posidon.launcher.tools.mainFont
import posidon.launcher.view.LinearLayoutManager

class NotificationCards : LinearLayout, FeedSection {

    constructor(c: Context) : super(c)
    constructor(c: Context, a: AttributeSet?) : super(c, a)
    constructor(c: Context, a: AttributeSet?, sa: Int) : super(c, a, sa)


    private val notifications = RecyclerView(context).apply {
        isNestedScrollingEnabled = false
        layoutManager = LinearLayoutManager(context)
    }


    private val arrowUp = ImageView(context).apply {
        setImageResource(R.drawable.arrow_up)
        visibility = GONE
        imageTintList = ColorStateList.valueOf(0xffffffff.toInt())
    }

    private val parentNotificationTitle = TextView(context).apply {
        textSize = 17f
        gravity = Gravity.CENTER_VERTICAL
    }

    private val parentNotificationBtn = ImageView(context).apply {
        setImageResource(R.drawable.ic_notification)
        setBackgroundResource(R.drawable.button_bg_round)
        run {
            val p = 8.dp.toInt()
            setPadding(p, p, p, p)
        }
    }

    private val parentNotification = LinearLayout(context).apply {

        orientation = HORIZONTAL
        run {
            val h = 20.dp.toInt()
            val v = 12.dp.toInt()
            setPadding(h, v, h, v)
        }

        addView(arrowUp, LayoutParams(MATCH_PARENT, 48.dp.toInt()))
        addView(parentNotificationTitle, LayoutParams(0, 48.dp.toInt(), 1f))
        addView(parentNotificationBtn, LayoutParams(48.dp.toInt(), 48.dp.toInt()))

        setOnLongClickListener(LauncherMenu())
        setOnClickListener {
            if (notifications.visibility == VISIBLE) {
                //desktop.scrollTo(0, 0)
                collapse()
            } else {
                expand()
            }
        }
    }

    init {
        orientation = VERTICAL
        addView(parentNotification, LayoutParams(MATCH_PARENT, 72.dp.toInt()))
        addView(notifications, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
    }

    fun collapse() {
        notifications.visibility = GONE
        arrowUp.visibility = GONE
        parentNotification.background.alpha = 255
    }

    fun expand() {
        notifications.visibility = VISIBLE
        arrowUp.visibility = VISIBLE
        parentNotification.background.alpha = 127
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

    fun update() {
        if (Settings["collapseNotifications", false]) {
            if (NotificationService.notificationsAmount > 1) {
                parentNotification.visibility = VISIBLE
                parentNotificationTitle.text = resources.getString(
                        R.string.num_notifications,
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
        notifications.adapter = NotificationAdapter(context)
    }

    override fun updateTheme(activity: Activity) {
        val marginX = Settings["feed:card_margin_x", 16].dp.toInt()
        val marginY = Settings["feed:card_margin_y", 9].dp.toInt()
        (parentNotification.layoutParams as LayoutParams).apply {
            leftMargin = marginX
            rightMargin = marginX
            topMargin = marginY
            bottomMargin = marginY
        }
        parentNotification.layoutParams = parentNotification.layoutParams
        val notificationBackground = ShapeDrawable()
        val r = Settings["feed:card_radius", 15].dp
        notificationBackground.shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
        notificationBackground.paint.color = Settings["notificationbgcolor", -0x1]
        parentNotification.background = notificationBackground
        parentNotificationTitle.setTextColor(Settings["notificationtitlecolor", -0xeeeded])
        parentNotificationTitle.typeface = context.mainFont
        parentNotificationBtn.imageTintList = ColorStateList.valueOf(if (ColorTools.useDarkText(Home.accentColor)) -0x1000000 else -0x1)
        parentNotificationBtn.backgroundTintList = ColorStateList.valueOf(Home.accentColor)
        parentNotificationBtn.imageTintList = ColorStateList.valueOf(Home.accentColor)
        parentNotificationBtn.backgroundTintList = ColorStateList.valueOf(Home.accentColor and 0x00ffffff or 0x33000000)
        isCollapsingEnabled = Settings["collapseNotifications", false] && NotificationService.notificationsAmount > 1
    }
}