package posidon.launcher.view

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.StateListDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.AttributeSet
import android.util.StateSet
import posidon.launcher.Main
import posidon.launcher.tools.Tools


class Switch(context: Context?, attrs: AttributeSet?) : android.widget.Switch(context, attrs) {

    init {
        this.trackDrawable = generateTrackDrawable()
        this.thumbDrawable = generateThumbDrawable()
    }

    override fun performClick(): Boolean {
        Tools.vibrate(context)
        return super.performClick()
    }

    private fun generateThumbDrawable(): StateListDrawable {
        val out = StateListDrawable()
        out.addState(intArrayOf(android.R.attr.state_checked), generateCircle(Main.accentColor))
        out.addState(StateSet.WILD_CARD, generateCircle(0xff252627.toInt()))
        return out
    }

    private fun generateCircle(color: Int): ShapeDrawable {
        val out = ShapeDrawable(OvalShape())
        out.paint.color = color
        out.intrinsicHeight = (24 * resources.displayMetrics.density).toInt()
        out.intrinsicWidth = (24 * resources.displayMetrics.density).toInt()
        return out
    }

    private fun generateTrackDrawable(): StateListDrawable {
        val out = StateListDrawable()
        out.addState(intArrayOf(android.R.attr.state_checked), generateBG(Main.accentColor and 0x00ffffff or 0x55000000))
        out.addState(StateSet.WILD_CARD, generateBG(0xff08090a.toInt()))
        return out
    }

    private fun generateBG(color: Int): GradientDrawable {
        val out = GradientDrawable()
        out.cornerRadius = 12 * resources.displayMetrics.density
        out.setColor(color)
        out.setStroke((10 * resources.displayMetrics.density).toInt(), 0x0)
        return out
    }
}
