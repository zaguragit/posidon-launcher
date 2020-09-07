package posidon.launcher.feed.news.chooser.suggestions

import posidon.launcher.R
import posidon.launcher.tools.Tools

object Suggestions {

    val list = mapOf(
        "Tech (English)" to R.raw.suggestions_tech_en,
        "United States" to R.raw.suggestions_united_states,
        "Tech (Spanish)" to R.raw.suggestions_tech_es,
        "Spain" to R.raw.suggestions_spain
    )

    val topics by lazy { list.map {
        Topic(Tools.publicContext!!, it.key, it.value)
    }}

    inline operator fun get(i: Int) = topics[i]
}