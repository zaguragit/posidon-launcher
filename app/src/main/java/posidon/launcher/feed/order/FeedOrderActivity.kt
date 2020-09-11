package posidon.launcher.feed.order

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import posidon.launcher.Home
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.applyFontSetting
import posidon.launcher.tools.dp
import posidon.launcher.tools.getStatusBarHeight
import posidon.launcher.view.LinearLayoutManager

class FeedOrderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_feed_order)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        val recycler = findViewById<RecyclerView>(R.id.recycler)

        run {
            val p = 4.dp.toInt()
            recycler.setPadding(p, getStatusBarHeight(), p, Tools.navbarHeight + p)
        }


        val sections = Settings.getStringsOrSet("feed:sections") {
            arrayListOf("notifications", "news")
        }

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

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

        }).attachToRecyclerView(recycler)

        val fab = findViewById<FloatingActionButton>(R.id.fab).apply {
            backgroundTintList = ColorStateList.valueOf(Home.accentColor and 0x00ffffff or 0x33000000)
            imageTintList = ColorStateList.valueOf(Home.accentColor)
            (layoutParams as FrameLayout.LayoutParams).bottomMargin = 20.dp.toInt() + Tools.navbarHeight
        }

        fab.setOnClickListener {
            sections.add(0, "notifications")
            recycler.adapter!!.notifyItemInserted(0)
            recycler.adapter!!.notifyItemRangeChanged(1, sections.size)
        }
    }
}