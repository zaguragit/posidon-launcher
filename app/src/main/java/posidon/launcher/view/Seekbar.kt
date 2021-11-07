package posidon.launcher.view

import android.content.Context
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatSeekBar
import posidon.launcher.Global

class Seekbar(context: Context, attrs: AttributeSet? = null) : AppCompatSeekBar(context, attrs) {

    init {
        progressDrawable = generateDrawable()
        thumb = generateThumb()
        splitTrack = false
    }

    private fun generateDrawable(): Drawable {
        val out = LayerDrawable(arrayOf(
            Switch.generateBG(context, 0xff08090a.toInt()),
            ClipDrawable(Switch.generateBG(context, Global.accentColor and 0x00ffffff or 0x88000000.toInt()), Gravity.LEFT, GradientDrawable.Orientation.BL_TR.ordinal)
        ))
        out.setId(0, android.R.id.background)
        out.setId(1, android.R.id.progress)
        return out
    }

    private fun generateThumb(): Drawable {
        return Switch.generateCircle(context, Global.accentColor)
    }
}