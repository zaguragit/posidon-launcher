package posidon.launcher.view.drawer

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.GridView.STRETCH_COLUMN_WIDTH
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import posidon.launcher.R
import posidon.launcher.search.SearchActivity
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.dp
import posidon.launcher.tools.getStatusBarHeight
import posidon.launcher.view.GridView

class DrawerView : LinearLayout {

    fun updateTheme() {
        val searchBarEnabled = Settings["drawersearchbarenabled", true]
        run {
            val searchBarHeight = if (searchBarEnabled) 56.dp.toInt() else 0
            val scrollbarWidth = if (
                Settings["drawer:scrollbar:enabled", false] && // isEnabled
                Settings["drawer:scrollbar:position", 1] == 2 // isHorizontal
            ) Settings["drawer:scrollbar:width", 24].dp.toInt() else 0
            searchBarVBox.setPadding(0, 0, 0, Tools.navbarHeight + if (Settings["drawer:scrollbar:show_outside", false]) scrollbarWidth else 0)
            drawerGrid.setPadding(0, context.getStatusBarHeight(), 0, Tools.navbarHeight + searchBarHeight + scrollbarWidth + 12.dp.toInt())
        }
        if (!searchBarEnabled) {
            searchBar.visibility = GONE
            return
        }
        searchBar.visibility = VISIBLE
        searchTxt.setTextColor(Settings["searchtxtcolor", -0x1])
        searchIcon.imageTintList = ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(Settings["searchhintcolor", -0x1]))
        searchBarVBox.background = ShapeDrawable().apply {
            val tr = Settings["searchradius", 0].dp
            shape = RoundRectShape(floatArrayOf(tr, tr, tr, tr, 0f, 0f, 0f, 0f), null, null)
            paint.color = Settings["searchcolor", 0x33000000]
        }
    }

    constructor(c: Context) : super(c)
    constructor(c: Context, a: AttributeSet?) : super(c, a)
    constructor(c: Context, a: AttributeSet?, sa: Int) : super(c, a, sa)

    val dock = DockView(context)

    val drawerGrid = GridView(context).apply {
        gravity = Gravity.CENTER_HORIZONTAL
        stretchMode = STRETCH_COLUMN_WIDTH
        selector = ColorDrawable(0)
        isVerticalScrollBarEnabled = false
        isVerticalFadingEdgeEnabled = true
        setFadingEdgeLength(72.dp.toInt())
        clipToPadding = false
        alpha = 0f
        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && canScrollVertically(-1))
                requestDisallowInterceptTouchEvent(true)
            false
        }
    }

    val searchIcon = ImageView(context).apply {
        run {
            val p = 12.dp.toInt()
            setPadding(p, p, p, p)
        }
        setImageResource(R.drawable.ic_search)
        imageTintList = ColorStateList.valueOf(0xffffffff.toInt())
    }

    val searchTxt = TextView(context).apply {
        run {
            val p = 12.dp.toInt()
            setPadding(p, p, p, p)
        }
        gravity = Gravity.CENTER_VERTICAL
        textSize = 16f
    }
    val searchBar = LinearLayout(context).apply {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setOnClickListener {
            SearchActivity.open(context)
        }
        val height = 56.dp.toInt()
        addView(searchIcon, LayoutParams(height, height).apply {
            marginStart = 8.dp.toInt()
        })
        addView(searchTxt, LayoutParams(MATCH_PARENT, height).apply {
            marginStart = -16.dp.toInt()
        })
    }

    val searchBarVBox = LinearLayout(context).apply {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        addView(searchBar, ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
    }

    val drawerContent = FrameLayout(context).apply {
        addView(drawerGrid, LayoutParams(MATCH_PARENT, MATCH_PARENT))
        addView(searchBarVBox, FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            this.gravity = Gravity.BOTTOM
        })
    }

    private lateinit var behavior: LockableBottomDrawerBehavior<DrawerView>

    fun init() {
        behavior = LockableBottomDrawerBehavior.from(this).apply {
            isHideable = false
            peekHeight = 0
            state = BottomDrawerBehavior.STATE_COLLAPSED
        }
    }


    fun addCallback(callback: BottomDrawerBehavior.BottomSheetCallback) = behavior.addBottomSheetCallback(callback)
    fun setLocked(locked: Boolean) = behavior.setLocked(locked)

    var state: Int
        get() = behavior.state
        set(value) = behavior.setState(value)

    var peekHeight: Int
        get() = behavior.peekHeight
        set(value) = behavior.setPeekHeight(value)

    init {
        orientation = VERTICAL
        addView(dock, LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM
        })
        addView(drawerContent, LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }
}