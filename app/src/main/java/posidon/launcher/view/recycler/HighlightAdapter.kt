package posidon.launcher.view.recycler

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import io.posidon.android.conveniencelib.units.dp
import io.posidon.android.conveniencelib.units.toFloatPixels
import posidon.launcher.Global

interface HighlightAdapter {

    fun highlight(i: Int)
    fun unhighlight()

    companion object {
        fun createHighlightDrawable(resources: Resources): Drawable {
            val bg = ShapeDrawable()
            val r = 12.dp.toFloatPixels(resources)
            bg.shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
            bg.paint.color = Global.accentColor and 0xffffff or 0x55000000
            return bg
        }
    }
}