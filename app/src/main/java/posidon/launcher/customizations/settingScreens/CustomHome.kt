package posidon.launcher.customizations.settingScreens

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.feed.order.FeedOrderActivity
import posidon.launcher.storage.Settings
import posidon.launcher.view.setting.*

class CustomHome : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings.init(applicationContext)
        configureWindowForSettings()
        setSettingsContentView(R.string.settings_title_feed) {
            card {
                clickable(
                    labelId = R.string.sections,
                    iconId = R.drawable.ic_sections,
                    onClick = ::openFeedOrder,
                )
            }
            card {
                numberSeekBar(
                    labelId = R.string.corner_radius,
                    key = "feed:card_radius",
                    default = 15,
                    max = 50,
                )
                numberSeekBar(
                    labelId = R.string.horizontal_margin,
                    key = "feed:card_margin_x",
                    default = 16,
                    max = 32,
                )
                numberSeekBar(
                    labelId = R.string.vertical_margin,
                    key = "feed:card_margin_y",
                    default = 9,
                    max = 16,
                )
                switch(
                    labelId = R.string.show_behind_dock,
                    iconId = R.drawable.ic_visible,
                    key = "feed:show_behind_dock",
                    default = false,
                )
                switch(
                    labelId = R.string.keep_position,
                    iconId = R.drawable.ic_apps,
                    key = "feed:keep_pos",
                    default = false,
                )
                switch(
                    labelId = R.string.rest_at_bottom,
                    iconId = R.drawable.ic_arrow_down,
                    key = "feed:rest_at_bottom",
                    default = false,
                )
                switch(
                    labelId = R.string.fading_edge,
                    iconId = R.drawable.ic_visible,
                    key = "feed:fading_edge",
                    default = true,
                )
            }
            card {
                title(labelId = R.string.starred_contacts)
                numberSeekBar(
                    labelId = R.string.columns,
                    key = "contacts_card:columns",
                    default = 5,
                    max = 5,
                    startsWith1 = true,
                )
                color(
                    labelId = R.string.background,
                    key = "contacts_card:bg_color",
                    default = 0xffffffff.toInt(),
                )
                color(
                    labelId = R.string.text_color,
                    key = "contacts_card:text_color",
                    default = 0xff252627.toInt(),
                )
            }
        }
        Global.customized = true
    }

    override fun onPause() {
        Global.customized = true
        super.onPause()
    }

    fun openFeedOrder(v: View) = startActivity(Intent(this, FeedOrderActivity::class.java))
}