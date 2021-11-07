package posidon.launcher.drawable

import android.graphics.*
import android.graphics.drawable.Drawable

class ColorPreviewDrawable(
    val color: Int
) : Drawable() {

    private val strokePaint = Paint().apply {
        this.isAntiAlias = true
        this.color = -0x1000000
    }
    private val paint = Paint().apply {
        this.isAntiAlias = true
        this.color = this@ColorPreviewDrawable.color
        this.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
    }

    val strokeWidth = 2f

    override fun draw(canvas: Canvas) {
        canvas.drawRoundRect(
            bounds.left.toFloat(),
            bounds.top.toFloat(),
            bounds.right.toFloat(),
            bounds.bottom.toFloat(),
            bounds.width() / 4f,
            bounds.height() / 4f,
            strokePaint,
        )
        canvas.drawRoundRect(
            bounds.left.toFloat() + strokeWidth,
            bounds.top.toFloat() + strokeWidth,
            bounds.right.toFloat() - strokeWidth,
            bounds.bottom.toFloat() - strokeWidth,
            (bounds.width() - strokeWidth * 2f) / 4f,
            (bounds.height() - strokeWidth * 2f) / 4f,
            paint,
        )
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}