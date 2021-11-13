package posidon.launcher.feed.order

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import posidon.android.conveniencelib.dp
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.external.widgets.Widget
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.getStatusBarHeight
import posidon.launcher.tools.theme.applyFontSetting
import posidon.launcher.view.feed.Feed
import posidon.launcher.view.recycler.LinearLayoutManager

class FeedOrderActivity : AppCompatActivity() {

    lateinit var recycler: RecyclerView

    val sections = Feed.getSectionsFromSettings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_feed_order)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) window.setDecorFitsSystemWindows(false)
        else window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        recycler = findViewById<RecyclerView>(R.id.recycler).apply {
            val p = dp(4).toInt()
            setPadding(p, getStatusBarHeight(), p, Tools.navbarHeight + p)

            layoutManager = LinearLayoutManager(this@FeedOrderActivity, RecyclerView.VERTICAL, false)
            isNestedScrollingEnabled = false
            adapter = OrderAdapter(this@FeedOrderActivity, sections)
        }

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

        findViewById<FloatingActionButton>(R.id.fab).apply {
            backgroundTintList = ColorStateList.valueOf(Global.accentColor and 0x00ffffff or 0x33000000)
            imageTintList = ColorStateList.valueOf(Global.accentColor)
            (layoutParams as FrameLayout.LayoutParams).bottomMargin = dp(20).toInt() + Tools.navbarHeight
            setOnClickListener {
                Feed.selectFeedSectionToAdd(this@FeedOrderActivity, ::onItemSelect)
            }
        }

        Global.customized = true
    }

    private fun onItemSelect(it: String) {
        sections.add(0, it)
        Settings.apply()
        recycler.adapter!!.notifyItemInserted(0)
    }

    override fun onPause() {
        Global.customized = true
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val w = Widget.handleActivityResult(this, requestCode, resultCode, data)
        w?.let { it1 -> onItemSelect(it1.toString()) }
        super.onActivityResult(requestCode, resultCode, data)
    }
}