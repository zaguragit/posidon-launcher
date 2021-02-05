package posidon.launcher.view.feed.news

import android.app.Activity
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import posidon.launcher.feed.news.FeedItem
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Device
import posidon.launcher.tools.dp
import posidon.launcher.tools.onEnd
import posidon.launcher.view.LinearLayoutManager
import posidon.launcher.view.feed.FeedSection
import kotlin.math.pow

class NewsCards(c: Activity) : RecyclerView(c), FeedSection {

    init {
        isNestedScrollingEnabled = false
        adapter = NewsAdapter(ArrayList(), c)
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

    inline fun updateFeed(items: List<FeedItem>) {
        (adapter as NewsAdapter).updateFeed(items)
    }

    override fun updateTheme(activity: Activity) {
        val restAtBottom = Settings["feed:rest_at_bottom", false]
        val marginX = Settings["feed:card_margin_x", 16].dp.toInt() / 2
        (layoutParams as LinearLayout.LayoutParams).setMargins(marginX, 0, marginX, 0)
        layoutManager = if (Settings["news:cards:is_staggered", false]) {
            StaggeredGridLayoutManager(2, VERTICAL).apply {
                reverseLayout = restAtBottom
            }
        } else LinearLayoutManager(context).apply {
            reverseLayout = restAtBottom
        }
        adapter = NewsAdapter((adapter as NewsAdapter).items, activity)
    }

    override fun toString() = "news"
}