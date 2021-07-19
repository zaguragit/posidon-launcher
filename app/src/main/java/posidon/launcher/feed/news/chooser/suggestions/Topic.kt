package posidon.launcher.feed.news.chooser.suggestions

import android.content.Context
import org.json.JSONArray
import posidon.android.conveniencelib.loadRaw
import posidon.android.loader.rss.RssSource

class Topic (
    val context: Context,
    val name: String,
    val id: Int
) {

    val sources by lazy { context.loadRaw(id) {
        val array = JSONArray(it.readText())
        try {
            Array(array.length()) {
                val obj = array.getJSONObject(it)
                RssSource(
                    obj.getString("name"),
                    obj.getString("url"), "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            arrayOf()
        }
    }}

    inline operator fun get(i: Int) = sources[i]
}