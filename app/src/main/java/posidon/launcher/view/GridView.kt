package posidon.launcher.view

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.View
import android.widget.GridView
import posidon.launcher.tools.Tools
import posidon.launcher.tools.dp

class GridView : GridView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet) : super(context, attr)
    constructor(context: Context, attr: AttributeSet, defStyleAttr: Int) : super(context, attr, defStyleAttr)

    private var maxOverScroll = 64.dp
    private var startTime = System.currentTimeMillis()

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        maxOverScroll = 64.dp
    }

    override fun overScrollBy(deltaX: Int, deltaY: Int, scrollX: Int, scrollY: Int, scrollRangeX: Int, scrollRangeY: Int, mx: Int, my: Int, isTouchEvent: Boolean): Boolean {
        var deltaY = deltaY
        val mo: Float
        if ((scrollY + deltaY >= scrollRangeY || scrollY + deltaY <= 0) && !isTouchEvent) {
            mo = maxOverScroll
            val elapsedTime: Long = System.currentTimeMillis() - startTime
            val interpolation: Float = Tools.springInterpolate(elapsedTime.toFloat() / 1000f)
            deltaY = (deltaY * interpolation).toInt()
        } else {
            startTime = System.currentTimeMillis()
            mo = 0f
        }
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, mx, mo.toInt(), isTouchEvent)
    }
}