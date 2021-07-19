package posidon.launcher.view.feed

import android.app.Activity
import android.util.Log
import posidon.launcher.external.widgets.Widget
import posidon.launcher.view.feed.news.NewsCards
import posidon.launcher.view.feed.notifications.NotificationCards

interface FeedSection {

    fun updateTheme(activity: Activity)

    fun onPause() {}
    fun onResume(activity: Activity) {}

    fun onAdd(feed: Feed, i: Int) {}
    fun updateIndex(i: Int) {}
    fun onDelete(feed: Feed) {}

    fun onAppsLoaded(iterator: MutableIterator<FeedSection>) {}

    override fun toString(): String

    companion object {
        operator fun invoke(
            context: Activity,
            sections: MutableList<String>,
            i: Int
        ): FeedSection? = when (val string = sections[i]) {
            "starred_contacts" -> ContactCardView(context)
            "news" -> NewsCards(context)
            "music" -> MusicCard(context)
            "notifications" -> NotificationCards(context)
            else -> {
                val key = string.substringBefore(':')
                val value = string.substringAfter(':')
                when (key) {
                    "widget" -> WidgetSection(context, Widget(value.toInt()))
                    "spacer" -> SpacerSection(context)
                    else -> {
                        Log.e("posidon launcher", "Invalid feed section type found: $key")
                        sections.removeAt(i)
                        null
                    }
                }
            }
        }
    }
}