package posidon.launcher.view

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.widget.GridView
import posidon.launcher.tools.Tools
import posidon.launcher.tools.dp

class GridView : GridView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet) : super(context, attr)
    constructor(context: Context, attr: AttributeSet, defStyleAttr: Int) : super(context, attr, defStyleAttr)

    private var maxOverScroll = 48.dp
    private var startTime = System.currentTimeMillis()

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        maxOverScroll = 48.dp
    }

    override fun overScrollBy(deltaX: Int, deltaY: Int, scrollX: Int, scrollY: Int, scrollRangeX: Int, scrollRangeY: Int, mx: Int, my: Int, isTouchEvent: Boolean): Boolean {
        var deltaY = deltaY
        val futureScroll = scrollY + deltaY
        if (futureScroll > scrollRangeY) {
            if (isTouchEvent) {
                deltaY = (deltaY * (1 - (scrollY - scrollRangeY) / maxOverScroll)).toInt()
                println("222_Dsf4df: $deltaY")
                startTime = System.currentTimeMillis()
            } else {
                println("111_DaFefd")
                val elapsedTime: Long = System.currentTimeMillis() - startTime
                val interpolation: Float = Tools.springInterpolate(elapsedTime.toFloat() / 800f)
                deltaY = (deltaY / 2 * interpolation).toInt()
            }
        } else if (futureScroll < 0) {
            if (isTouchEvent) {
                deltaY = (deltaY * (1 + scrollY / maxOverScroll)).toInt()
                println("444_Dsf4df: $deltaY")
                startTime = System.currentTimeMillis()
            } else {
                println("333_DaFefd")
                val elapsedTime: Long = System.currentTimeMillis() - startTime
                val interpolation: Float = Tools.springInterpolate(elapsedTime.toFloat() / 800f)
                deltaY = (deltaY / 2 * interpolation).toInt()
            }
        } else {
            println("555_Dsf4df")
            startTime = System.currentTimeMillis()
        }
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, mx, maxOverScroll.toInt(), isTouchEvent)
    }
}