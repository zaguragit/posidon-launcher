package posidon.launcher.drawable

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

internal class NonDrawable : Drawable() {

    override fun draw(canvas: Canvas) {}

    override fun getOpacity() = PixelFormat.TRANSPARENT

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(cf: ColorFilter?) {}

    override fun getMinimumWidth() = 0
    override fun getMinimumHeight() = 0
}