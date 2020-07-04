package posidon.launcher.feed.news.chooser

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.view.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.dp
import posidon.launcher.tools.getStatusBarHeight
import posidon.launcher.tools.vibrate

class FeedChooser : AppCompatActivity() {

    private lateinit var grid: RecyclerView
    private val feedUrls: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.feed_chooser)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        grid = findViewById(R.id.grid)
        grid.layoutManager = GridLayoutManager(this, 2)
        val padding = 4.dp.toInt()
        grid.setPadding(padding, getStatusBarHeight(), padding, Tools.navbarHeight + padding)

        feedUrls.addAll(Settings["feedUrls", defaultSources].split("|"))
        if (feedUrls.size == 1 && feedUrls[0].replace(" ", "") == "") {
            feedUrls.removeAt(0)
            Settings.putNotSave("feedUrls", "")
            Settings.apply()
        }

        grid.adapter = FeedChooserAdapter(this@FeedChooser, feedUrls)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.backgroundTintList = ColorStateList.valueOf(Main.accentColor and 0x00ffffff or 0x33000000)
        fab.imageTintList = ColorStateList.valueOf(Main.accentColor)
        fab.setOnClickListener {
            vibrate()
            val dialog = BottomSheetDialog(this, R.style.bottomsheet)
            dialog.setContentView(R.layout.feed_chooser_option_edit_dialog)
            dialog.window!!.findViewById<View>(R.id.design_bottom_sheet).setBackgroundResource(R.drawable.bottom_sheet)
            dialog.findViewById<TextView>(R.id.done)!!.setTextColor(Main.accentColor)
            dialog.findViewById<TextView>(R.id.done)!!.backgroundTintList = ColorStateList.valueOf(Main.accentColor and 0x00ffffff or 0x33000000)
            dialog.findViewById<TextView>(R.id.done)!!.setOnClickListener {
                dialog.dismiss()
                feedUrls.add(dialog.findViewById<EditText>(R.id.title)!!.text.toString().replace('|', ' '))
                grid.adapter!!.notifyDataSetChanged()
                Settings.putNotSave("feedUrls", feedUrls.joinToString("|"))
                Settings.apply()
            }
            dialog.findViewById<TextView>(R.id.remove)!!.visibility = View.GONE
            dialog.show()
        }
        (fab.layoutParams as FrameLayout.LayoutParams).bottomMargin = 20.dp.toInt() + Tools.navbarHeight
    }

    companion object {
        const val defaultSources = "androidpolice.com/feed"
    }
}