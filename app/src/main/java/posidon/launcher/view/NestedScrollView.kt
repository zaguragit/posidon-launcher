package posidon.launcher.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.widget.NestedScrollView

class NestedScrollView : NestedScrollView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet) : super(context, attr)
    constructor(context: Context, attr: AttributeSet, defStyleAttr: Int) : super(context, attr, defStyleAttr)

    var onTopOverScroll = {}

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        val oldScrollY = this.scrollY
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        //println(System.currentTimeMillis() - timeSincePress)
        if (clampedY &&
            scrollY == 0 &&
            oldScrollY == 0 &&
            superOldScrollY == 0 &&
            pointerCount == 1 &&
            System.currentTimeMillis() - timeSincePress < 240) onTopOverScroll()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        //println("TT2")
        if (ev.action == MotionEvent.ACTION_MOVE && scrollY > superOldScrollY) {
            superOldScrollY = scrollY
        }
        pointerCount = ev.pointerCount
        return super.onTouchEvent(ev)
    }

    private var timeSincePress = 0L
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            timeSincePress = System.currentTimeMillis()
            superOldScrollY = scrollY
        }
        pointerCount = ev.pointerCount
        return super.onInterceptTouchEvent(ev)
    }

    private var superOldScrollY = 0
    private var pointerCount = 0
}