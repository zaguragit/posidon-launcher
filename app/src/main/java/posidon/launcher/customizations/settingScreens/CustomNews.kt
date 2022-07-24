package posidon.launcher.customizations.settingScreens

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.customizations.RemovedArticles
import posidon.launcher.feed.news.chooser.FeedChooser
import posidon.launcher.feed.news.opml.OPML
import posidon.launcher.feed.news.opml.OpmlElement
import posidon.launcher.storage.ExternalStorage
import posidon.launcher.storage.Settings
import posidon.launcher.view.setting.*
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class CustomNews : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings.init(applicationContext)
        configureWindowForSettings()
        setSettingsContentView(R.string.settings_title_news) {
            card {
                clickable(R.string.feed_sources, R.drawable.ic_news, ::chooseFeeds)
                switch(
                    R.string.show_feed_spinner,
                    R.drawable.ic_apps,
                    "feed:show_spinner",
                    true,
                )
                switch(
                    R.string.open_in_app,
                    R.drawable.ic_news,
                    "news:open_in_app",
                    false,
                )
                switch(
                    R.string.load_on_resume,
                    R.drawable.ic_apps,
                    "news:load_on_resume",
                    true,
                )
                numberSeekBar(
                    R.string.max_days_age,
                    "news:max_days_age",
                    default = 5,
                    max = 30,
                )
            }
            card {
                title(R.string.layout)
                numberSeekBar(
                    R.string.max_cards,
                    "feed:max_news",
                    default = 48,
                    max = 100,
                )
                switch(
                    R.string.setting_hide_until_user_scrolls,
                    R.drawable.ic_visible,
                    "hidefeed",
                    false,
                )
                switch(
                    R.string.staggered,
                    R.drawable.ic_staggered_grid,
                    "news:cards:is_staggered",
                    false,
                )
            }
            card {
                title(R.string.cards)
                color(
                    R.string.background,
                    R.drawable.ic_color,
                    "feed:card_bg",
                    0xff252627.toInt(),
                )
                switch(
                    R.string.text_shadow,
                    R.drawable.ic_label,
                    "feed:card_text_shadow",
                    true,
                )
                switch(
                    R.string.adjust_height_to_content,
                    R.drawable.ic_apps,
                    "news:cards:wrap_content",
                    true,
                )
            }
            card {
                switchTitle(R.string.text, "news:cards:title", true)
                color(
                    R.string.text_color,
                    R.drawable.ic_color,
                    "feed:card_txt_color",
                    0xFFFFFFFF.toInt(),
                )
                switch(
                    R.string.separate_text,
                    R.drawable.ic_apps,
                    "news:cards:sep_txt",
                    false,
                )
            }
            card {
                switchTitle(R.string.images, "news:cards:image", true)
                numberSeekBar(
                    R.string.max_image_width,
                    "feed:max_img_width",
                    default = 720,
                    max = 1024,
                    startsWith1 = true,
                )
                numberSeekBar(
                    R.string.height,
                    "news:cards:height",
                    default = 240,
                    max = 320,
                )
            }
            card {
                switchTitle(R.string.show_source, "news:cards:source", true)
                numberSeekBar(
                    R.string.radius,
                    "news:cards:source:radius",
                    default = 30,
                    max = 30,
                )
                color(
                    R.string.background,
                    R.drawable.ic_color,
                    "news:cards:source:bg_color",
                    0xff111213.toInt(),
                )
                switch(
                    R.string.tint_background,
                    R.drawable.ic_color_dropper,
                    "news:cards:source:tint_bg",
                    true,
                )
                color(
                    R.string.text_color,
                    R.drawable.ic_color,
                    "news:cards:source:fg_color",
                    0xffffffff.toInt(),
                )
                spinner(
                    R.string.align,
                    R.drawable.ic_apps,
                    "news:cards:source:align",
                    default = 0,
                    array = R.array.horizontalAlignment,
                )
                switch(
                    R.string.show_above_text,
                    R.drawable.ic_arrow_up,
                    "news:cards:source:show_above_text",
                    false,
                )
            }
            card {
                switchTitle(R.string.swipe_to_remove, "feed:delete_articles", false)
                color(
                    R.string.swipe_bg_color,
                    R.drawable.ic_color,
                    "feed:card_swipe_bg_color",
                    0x880d0e0f.toInt(),
                )
                switch(
                    R.string.undo_popup,
                    R.drawable.ic_apps,
                    "feed:undo_article_removal_opt",
                    false,
                )
                clickable(R.string.removed, R.drawable.ic_visible, ::seeRemovedArticles)
            }
            card {
                title(R.string.opml)
                clickable(R.string.export, R.drawable.ic_save, ::exportOPML)
                clickable(R.string._import, R.drawable.ic_apps, ::importOPML)
            }
        }
        Global.customized = true
    }

    fun chooseFeeds(v: View) = startActivity(Intent(this, FeedChooser::class.java))
    fun seeRemovedArticles(v: View) = startActivity(Intent(this, RemovedArticles::class.java))

    fun exportOPML(v: View) {
        val feedUrls = ArrayList(Settings["feedUrls", FeedChooser.defaultSources].split("|"))
        if (feedUrls.size == 1 && feedUrls[0].replace(" ", "") == "") {
            feedUrls.removeAt(0)
            Settings.putNotSave("feedUrls", "")
            Settings.apply()
        }

        ExternalStorage.writeOutsideScope(this, "posidon_feed_sources_${SimpleDateFormat("MMdHHmmss", Locale.getDefault()).format(Date())}.opml") { out, path ->
            try {
                OPML.writeDocument(feedUrls.map {
                    OpmlElement(it, it)
                }, out.bufferedWriter(Charsets.UTF_8))
                Toast.makeText(this, "Saved: $path", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun importOPML(v: View) =
        try { ExternalStorage.pickFile(this, "*/*") }
        catch (e: Exception) { e.printStackTrace() }

    private fun doImportOPML(input: InputStream?) {
        val feedUrls = ArrayList(Settings["feedUrls", FeedChooser.defaultSources].split("|"))
        if (feedUrls.size == 1 && feedUrls[0].replace(" ", "") == "") {
            feedUrls.removeAt(0)
            Settings.putNotSave("feedUrls", "")
        }

        try {
            val new = OPML.readDocument(input!!.bufferedReader(Charsets.UTF_8))
            var amountOfNewSources = 0
            for (element in new) {
                if (!feedUrls.contains(element.xmlUrl)) {
                    feedUrls.add(element.xmlUrl)
                    amountOfNewSources++
                }
            }
            Settings.putNotSave("feedUrls", feedUrls.joinToString("|"))
            Toast.makeText(this, "Imported $amountOfNewSources sources", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
        Settings.apply()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            ExternalStorage.onActivityResultPickFile(this, requestCode, data, ::doImportOPML)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}