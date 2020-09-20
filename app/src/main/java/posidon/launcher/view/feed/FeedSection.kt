package posidon.launcher.view.feed

import android.app.Activity
import android.content.Context
import posidon.launcher.external.Widget

interface FeedSection {

    fun updateTheme(activity: Activity)

    fun onPause() {}
    fun onResume(activity: Activity) {}

    fun onAdd(feed: Feed) {}

    fun onDelete(feed: Feed) {}

    companion object {
        operator fun invoke(
            context: Context,
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
                    "widget" -> {
                        WidgetSection(context, Widget(value.toInt()))
                    }
                    else -> throw Exception("Invalid feed section type found")
                }
            }
        }
    }
}