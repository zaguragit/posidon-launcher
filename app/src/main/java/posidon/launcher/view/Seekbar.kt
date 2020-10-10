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
import posidon.launcher.tools.dp

class Seekbar(context: Context?, attrs: AttributeSet? = null) : AppCompatSeekBar(context, attrs) {

    init {
        progressDrawable = generateDrawable()
        thumb = generateThumb()
        splitTrack = false
    }

    private fun generateDrawable(): Drawable {
        val out = LayerDrawable(arrayOf(generateTrackBG(), generateTrackFG()))
        out.setId(0, android.R.id.background)
        out.setId(1, android.R.id.progress)
        return out
    }

    private fun generateTrackBG(): Drawable {
        val out = GradientDrawable()
        out.cornerRadius = 12.dp
        out.setColor(0xff08090a.toInt())
        out.setStroke(10.dp.toInt(), 0x0)
        return out
    }

    private fun generateTrackFG(): Drawable {
        val out = GradientDrawable()
        out.cornerRadius = 12.dp
        out.setColor(Global.accentColor and 0x00ffffff or 0x88000000.toInt())
        out.setStroke(10.dp.toInt(), 0x0)
        return ClipDrawable(out, Gravity.LEFT, GradientDrawable.Orientation.BL_TR.ordinal)
    }

    private fun generateThumb(): Drawable {
        val r = 24.dp.toInt()
        val out = GradientDrawable()
        out.shape = GradientDrawable.OVAL
        out.setColor(Global.accentColor)
        out.setSize(r, r)
        out.setStroke(1, 0xdd000000.toInt())
        return out
    }
}