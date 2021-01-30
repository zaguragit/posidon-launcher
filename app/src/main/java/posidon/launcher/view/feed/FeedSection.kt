package posidon.launcher.view.feed

import android.app.Activity
import posidon.launcher.external.Widget

interface FeedSection {

    fun updateTheme(activity: Activity)

    fun onPause() {}
    fun onResume(activity: Activity) {}

    fun onAdd(feed: Feed, i: Int) {}
    fun updateIndex(i: Int) {}
    fun onDelete(feed: Feed) {}

    companion object {
        operator fun invoke(
            context: Activity,
            string: String
        ): FeedSection = when (string) {
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
                    else -> throw Exception("Invalid feed section type found: $key")
                }
            }
        }
    }
}