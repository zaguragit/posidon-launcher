package posidon.launcher.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.util.StateSet
import posidon.launcher.Global
import posidon.launcher.tools.dp
import posidon.launcher.tools.vibrate


class Switch(
    context: Context,
    attrs: AttributeSet? = null
) : android.widget.Switch(context, attrs) {

    var accentColor = Global.accentColor
        set(value) {
            field = value
            trackDrawable = generateTrackDrawable()
            thumbDrawable = generateThumbDrawable()
            refreshDrawableState()
        }

    init {
        trackDrawable = generateTrackDrawable()
        thumbDrawable = generateThumbDrawable()
    }

    override fun performClick(): Boolean {
        context.vibrate()
        return super.performClick()
    }

    private fun generateThumbDrawable(): Drawable {
        val out = StateListDrawable()
        out.addState(intArrayOf(android.R.attr.state_checked), generateCircle(accentColor))
        out.addState(StateSet.WILD_CARD, generateCircle(0xff252627.toInt()))
        return out
    }

    private fun generateCircle(color: Int): Drawable {
        val r = 24.dp.toInt()
        val out = GradientDrawable()
        out.shape = GradientDrawable.OVAL
        out.setColor(color)
        out.setSize(r, r)
        out.setStroke(1, 0xdd000000.toInt())
        return out
    }

    private fun generateTrackDrawable(): Drawable {
        val out = StateListDrawable()
        out.addState(intArrayOf(android.R.attr.state_checked), generateBG(accentColor and 0x00ffffff or 0x55000000))
        out.addState(StateSet.WILD_CARD, generateBG(0xff08090a.toInt()))
        return out
    }

    private fun generateBG(color: Int): Drawable {
        val out = GradientDrawable()
        out.cornerRadius = 12.dp
        out.setColor(color)
        out.setStroke(10.dp.toInt(), 0x0)
        return out
    }
}
