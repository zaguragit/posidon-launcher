package posidon.launcher.search

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.net.Uri
import org.json.JSONException
import org.json.JSONObject
import posidon.launcher.R
import posidon.launcher.tools.Loader

class DuckInstantAnswer private constructor(
    val title: String,
    val sourceName: String,
    val sourceUrl: String,
    val description: String,
    val searchUrl: String
) {

    companion object {

        fun search(context: Context, string: String) {
            val encoded = Uri.encode(string)
            val url = "https://duckduckgo.com/?q=$encoded&t=posidon.launcher"
            val uri = Uri.parse(url)
            val i = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(i, ActivityOptions.makeCustomAnimation(context, R.anim.slideup, R.anim.slidedown).toBundle())
        }

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