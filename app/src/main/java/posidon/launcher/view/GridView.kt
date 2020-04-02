package posidon.launcher.view

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.widget.GridView
import posidon.launcher.tools.Tools

class GridView : GridView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet) : super(context, attr)
    constructor(context: Context, attr: AttributeSet, defStyleAttr: Int) : super(context, attr, defStyleAttr)

    private var maxOverScroll = 32 * context.resources.displayMetrics.density
    private var startTime = System.currentTimeMillis()

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        maxOverScroll = 64 * context.resources.displayMetrics.density
    }

    override fun overScrollBy(deltaX: Int, deltaY: Int, scrollX: Int, scrollY: Int, scrollRangeX: Int, scrollRangeY: Int, mx: Int, my: Int, isTouchEvent: Boolean): Boolean {
        var deltaY = deltaY
        var maxOverScroll = maxOverScroll
        if (isTouchEvent) {
            startTime = System.currentTimeMillis()
            maxOverScroll = 0f
        } else if (scrollY + deltaY >= scrollRangeY || scrollY + deltaY <= 0) {
            val elapsedTime: Long = System.currentTimeMillis() - startTime
            println(elapsedTime)
            val interpolation: Float = Tools.springInterpolate(elapsedTime.toFloat() / 5000f)
            deltaY = (deltaY * interpolation).toInt()
        }
        println("overScrollByY($deltaY, $scrollY, $scrollRangeY, $isTouchEvent)")
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, mx, maxOverScroll.toInt(), isTouchEvent)
    }
}