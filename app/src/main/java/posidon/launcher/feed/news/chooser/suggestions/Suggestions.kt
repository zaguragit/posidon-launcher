package posidon.launcher.feed.news.chooser.suggestions

import posidon.launcher.R
import posidon.launcher.tools.Tools

object Suggestions {

    val list = mapOf(
        "United States" to R.raw.suggestions_united_states,
        "Spain" to R.raw.suggestions_spain,
        "Russia" to R.raw.suggestions_russia,
        "India" to R.raw.suggestions_india,
        "Ukraine" to R.raw.suggestions_ukraine,
        "Tech (English)" to R.raw.suggestions_tech_en,
        "Tech (Spanish)" to R.raw.suggestions_tech_es,
        "Tech (Italian)" to R.raw.suggestions_tech_it,
        "Tech (Gujarati)" to R.raw.suggestions_tech_gu
    )

    val topics by lazy { list.map {
        Topic(Tools.appContext!!, it.key, it.value)
    }}

    inline operator fun get(i: Int) = topics[i]
}