package posidon.launcher.search

import android.net.Uri
import org.json.JSONException
import org.json.JSONObject
import posidon.launcher.tools.Loader

class DuckInstantAnswer private constructor(
    val title: String,
    val sourceName: String,
    val sourceUrl: String,
    val description: String,
    val searchUrl: String
) {

    companion object {
        fun load(string: String, onLoad: (DuckInstantAnswer) -> Unit) {
            val encoded = Uri.encode(string)
            val url = "https://api.duckduckgo.com/?q=$encoded&format=json&t=posidon.launcher"
            Loader.loadText(url) {
                try {
                    val jObject = JSONObject(it)
                    val title = jObject.getString("Heading")
                    if (title.isBlank()) {
                        return@loadText
                    }
                    val sourceName = jObject.getString("AbstractSource")
                    if (sourceName.isBlank()) {
                        return@loadText
                    }
                    val sourceUrl = jObject.getString("AbstractURL")
                    if (sourceUrl.isBlank()) {
                        return@loadText
                    }
                    val type = jObject.getString("Type")
                    if (type.isBlank()) {
                        return@loadText
                    }
                    val description = if (type == "D") {
                        jObject.getJSONArray("RelatedTopics").getJSONObject(0).getString("Text")
                    } else {
                        jObject.getString("AbstractText")
                    }
                    if (description.isBlank()) {
                        return@loadText
                    }
                    onLoad(DuckInstantAnswer(title, sourceName, sourceUrl, description, "https://duckduckgo.com/?q=$encoded&t=posidon.launcher"))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }
}