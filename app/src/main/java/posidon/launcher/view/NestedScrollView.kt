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
        if (clampedY && scrollY == 0 && oldScrollY == 0 && superOldScrollY == 0 && canScrollVertically(1) && pointerCount == 1)
            onTopOverScroll()
    }

    private var superOldScrollY = 0
    private var pointerCount = 0

    override fun dispatchTouchEvent(e: MotionEvent): Boolean {
        if ((e.action == MotionEvent.ACTION_MOVE && scrollY > superOldScrollY) || e.action == MotionEvent.ACTION_DOWN)
            superOldScrollY = scrollY
        pointerCount = e.pointerCount
        return super.dispatchTouchEvent(e)
    }
}