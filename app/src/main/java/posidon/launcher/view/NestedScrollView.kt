package posidon.launcher.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.widget.NestedScrollView

open class NestedScrollView : NestedScrollView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet?) : super(context, attr)
    constructor(context: Context, attr: AttributeSet?, defStyleAttr: Int) : super(context, attr, defStyleAttr)

    var onTopOverScroll = {}
    var onBottomOverScroll = {}

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        val oldScrollY = this.scrollY
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        if (clampedY &&
            System.currentTimeMillis() - timeSincePress < 240 &&
            pointerCount == 1 &&
            oldScrollY == scrollY &&
            superOldScrollY == oldScrollY) {
            if (oldPointerY < newPointerY) {
                onTopOverScroll()
            } else if (oldPointerY > newPointerY) {
                onBottomOverScroll()
            }
        }
    }

    private var oldPointerY = 0f
    private var newPointerY = 0f
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_MOVE) {
            oldPointerY = newPointerY
            newPointerY = ev.y
            if (scrollY > superOldScrollY) {
                superOldScrollY = scrollY
            }
        }
        pointerCount = ev.pointerCount
        return super.onTouchEvent(ev)
    }

    private var timeSincePress = 0L
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            timeSincePress = System.currentTimeMillis()
            superOldScrollY = scrollY
            oldPointerY = ev.y
            newPointerY = ev.y
        }
        pointerCount = ev.pointerCount
        return super.onInterceptTouchEvent(ev)
    }

    private var superOldScrollY = 0
    private var pointerCount = 0
}