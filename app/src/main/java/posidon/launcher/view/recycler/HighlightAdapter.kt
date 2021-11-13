package posidon.launcher.view.recycler

import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import posidon.android.conveniencelib.dp
import posidon.launcher.Global
import posidon.launcher.tools.Tools

interface HighlightAdapter {

    fun highlight(i: Int)
    fun unhighlight()

    companion object {
        fun createHighlightDrawable(): Drawable {
            val bg = ShapeDrawable()
            val r = Tools.appContext!!.dp(12)
            bg.shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
            bg.paint.color = Global.accentColor and 0xffffff or 0x55000000
            return bg
        }
    }
}