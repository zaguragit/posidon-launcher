package posidon.launcher.feed.order

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.external.Widget
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.applyFontSetting
import posidon.launcher.tools.dp
import posidon.launcher.tools.getStatusBarHeight
import posidon.launcher.view.LinearLayoutManager
import posidon.launcher.view.feed.Feed

class FeedOrderActivity : AppCompatActivity() {

    lateinit var recycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_feed_order)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        recycler = findViewById(R.id.recycler)

        run {
            val p = 4.dp.toInt()
            recycler.setPadding(p, getStatusBarHeight(), p, Tools.navbarHeight + p)
        }

        val sections = Feed.getSectionsFromSettings()

        recycler.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recycler.isNestedScrollingEnabled = false
        recycler.adapter = OrderAdapter(this, sections)

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END, 0
        ) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val adapter = recyclerView.adapter!!
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition

                sections.add(to, sections.removeAt(from))
                Settings.apply()

                adapter.notifyItemMoved(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

        }).attachToRecyclerView(recycler)

        val fab = findViewById<FloatingActionButton>(R.id.fab).apply {
            backgroundTintList = ColorStateList.valueOf(Global.accentColor and 0x00ffffff or 0x33000000)
            imageTintList = ColorStateList.valueOf(Global.accentColor)
            (layoutParams as FrameLayout.LayoutParams).bottomMargin = 20.dp.toInt() + Tools.navbarHeight
        }

        fab.setOnClickListener {
            Feed.selectFeedSectionToAdd(this) {
                sections.add(0, it)
                Settings.apply()
                recycler.adapter!!.notifyItemInserted(0)
            }
        }

        Global.customized = true
    }

    override fun onPause() {
        Global.customized = true
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val w = Widget.handleActivityResult(this, requestCode, resultCode, data)
        if (w != null) {
            val sections = Feed.getSectionsFromSettings()
            sections.add(0, w.toString())
            Settings.apply()
            recycler.adapter!!.notifyItemInserted(0)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}