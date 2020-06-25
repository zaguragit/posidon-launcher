package posidon.launcher.tools

import android.graphics.*
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import androidx.vectordrawable.graphics.drawable.Animatable2Compat

class MaskedDrawable(
    val drawable: Drawable,
    val path: Path
) : Drawable() {


    private val maskPaint = Paint().apply {
        isAntiAlias = true
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    }

    private val paint = Paint().apply {
        isAntiAlias = true
    }

    override fun draw(canvas: Canvas) {
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        val c = Canvas(bitmap)
        drawable.draw(c)
        c.drawPath(path, maskPaint)
        canvas.drawBitmap(bitmap, null, bounds, paint)
    }

    override fun getIntrinsicWidth() = drawable.intrinsicWidth
    override fun getIntrinsicHeight() = drawable.intrinsicHeight

    override fun setAlpha(alpha: Int) { paint.alpha = alpha }
    override fun getAlpha() = paint.alpha

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) { drawable.colorFilter = colorFilter }
}