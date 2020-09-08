package posidon.launcher.view.feed

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import posidon.launcher.feed.news.FeedAdapter
import posidon.launcher.feed.news.FeedItem
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Device
import posidon.launcher.tools.dp
import posidon.launcher.tools.onEnd
import posidon.launcher.view.LinearLayoutManager
import kotlin.math.pow

class NewsCards : RecyclerView, FeedSection {

    constructor(c: Context) : super(c)
    constructor(c: Context, a: AttributeSet?) : super(c, a)
    constructor(c: Context, a: AttributeSet?, sa: Int) : super(c, a, sa)

    init {
        layoutManager = LinearLayoutManager(context)
        isNestedScrollingEnabled = false
    }

    fun updateTheme(activity: Activity) {
        val marginX = Settings["feed:card_margin_x", 16].dp.toInt()
        (layoutParams as LinearLayout.LayoutParams).setMargins(marginX, 0, marginX, 0)
        adapter = FeedAdapter(ArrayList(), activity)
    }

    var wasHiddenLastTime = true

    fun hide() {
        if (!wasHiddenLastTime) {
            animate().alpha(0f).setInterpolator { it.pow((it + 8) / 3) }.onEnd {
                translationX = Device.displayWidth.toFloat()
                wasHiddenLastTime = true
            }.duration = 180L
        }
    }

    fun show() {
        if (wasHiddenLastTime) {
            translationX = 0f
            animate().alpha(1f).setInterpolator { it.pow(3 / (it + 8)) }.onEnd {
                wasHiddenLastTime = false
            }.duration = 200L
        }
    }

    inline fun updateFeed(items: ArrayList<FeedItem>) {
        (adapter as FeedAdapter).updateFeed(items)
    }
}