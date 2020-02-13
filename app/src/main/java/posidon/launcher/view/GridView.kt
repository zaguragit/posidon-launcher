package posidon.launcher.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.PathInterpolator
import android.widget.GridView


class GridView : GridView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet) : super(context, attr)
    constructor(context: Context, attr: AttributeSet, defStyleAttr: Int) : super(context, attr, defStyleAttr)

    private var maxOverScroll = 0
    private var startTime = 0L
    private val interpolator = PathInterpolator(0.71f, 0.04f, 0.645f, 1.4f)

    override fun invalidate() {
        super.invalidate()
        maxOverScroll = measuredHeight / 24
    }

    override fun overScrollBy(deltaX: Int, deltaY: Int, scrollX: Int, scrollY: Int, scrollRangeX: Int, scrollRangeY: Int, mx: Int, my: Int, isTouchEvent: Boolean): Boolean {
        var overScrollDistance = maxOverScroll
        if (isTouchEvent) {
            startTime = System.currentTimeMillis()
        } else {
            val elapsedTime: Long = System.currentTimeMillis() - startTime
            var interpolation: Float = interpolator.getInterpolation(elapsedTime.toFloat() / 100)
            interpolation = if (interpolation > 1) 1f else interpolation
            overScrollDistance -= (maxOverScroll * interpolation).toInt()
            overScrollDistance = if (overScrollDistance < 0) 0 else overScrollDistance
        }

        val overScrollHorizontal =
                overScrollMode == View.OVER_SCROLL_ALWAYS ||
                overScrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS &&
                computeHorizontalScrollRange() > computeHorizontalScrollExtent()
        val overScrollVertical =
                overScrollMode == View.OVER_SCROLL_ALWAYS ||
                overScrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS &&
                computeVerticalScrollRange() > computeVerticalScrollExtent()

        val maxOverScrollX = if (overScrollHorizontal) overScrollDistance else 0
        val maxOverScrollY = if (overScrollVertical) overScrollDistance else 0

        val left = -maxOverScrollX
        val right = maxOverScrollX + scrollRangeX
        val top = -maxOverScrollY
        val bottom = maxOverScrollY + scrollRangeY

        var newScrollX = scrollX + deltaX
        var clampedX = false
        if (newScrollX > right) {
            newScrollX = right
            clampedX = true
        } else if (newScrollX < left) {
            newScrollX = left
            clampedX = true
        }

        var newScrollY = scrollY + deltaY
        var clampedY = false
        if (newScrollY > bottom) {
            newScrollY = bottom
            clampedY = true
        } else if (newScrollY < top) {
            newScrollY = top
            clampedY = true
        }

        onOverScrolled(newScrollX, newScrollY, clampedX, clampedY)

        return clampedX || clampedY
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        if (this.scrollY != scrollY) {
            onScrollChanged(this.scrollX, scrollY, this.scrollX, this.scrollY)
            this.scrollY = scrollY
            awakenScrollBars()
        }
    }
}