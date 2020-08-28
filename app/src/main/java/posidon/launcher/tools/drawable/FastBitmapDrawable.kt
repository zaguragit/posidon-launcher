package posidon.launcher.tools.drawable

import android.graphics.*
import android.graphics.drawable.Drawable

internal class FastBitmapDrawable(val bitmap: Bitmap?) : Drawable() {

    private val width = bitmap?.width ?: 0
    override fun getIntrinsicWidth() = width
    override fun getMinimumWidth() = width

    private val height = bitmap?.height ?: 0
    override fun getIntrinsicHeight() = height
    override fun getMinimumHeight() = height

    override fun getAlpha() = paint.alpha
    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }
    private var paint = Paint()

    override fun draw(canvas: Canvas) { bitmap?.let { canvas.drawBitmap(it, 0f, 0f, paint) } }

    override fun getOpacity() = if (paint.alpha == 255) PixelFormat.TRANSLUCENT else PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) = throw UnsupportedOperationException("Not supported with this drawable")
    override fun setDither(dither: Boolean) = throw UnsupportedOperationException("Not supported with this drawable")
    override fun setFilterBitmap(filter: Boolean) = throw UnsupportedOperationException("Not supported with this drawable")
}