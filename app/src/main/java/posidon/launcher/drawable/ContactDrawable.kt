package posidon.launcher.drawable

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

internal class ContactDrawable(
    val color: Int,
    val character: Char,
    val paint: Paint
) : Drawable() {

    override fun draw(canvas: Canvas) {
        canvas.drawColor(color)
        val x = bounds.width() / 2f
        val y = (bounds.height() - (paint.descent() + paint.ascent())) / 2f
        canvas.drawText(charArrayOf(character), 0, 1, x, y, paint)
    }

    override fun getOpacity() = PixelFormat.OPAQUE

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(cf: ColorFilter?) {}

    override fun getIntrinsicWidth() = 128
    override fun getIntrinsicHeight() = 128
    override fun getMinimumWidth() = 128
    override fun getMinimumHeight() = 128
}