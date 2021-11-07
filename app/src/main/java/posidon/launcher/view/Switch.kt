package posidon.launcher.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.util.StateSet
import androidx.appcompat.widget.SwitchCompat
import posidon.android.conveniencelib.dp
import posidon.launcher.Global
import posidon.launcher.tools.vibrate

class Switch(
    context: Context,
    attrs: AttributeSet? = null
) : SwitchCompat(context, attrs) {

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

    private fun generateTrackDrawable(): Drawable {
        val out = StateListDrawable()
        out.addState(intArrayOf(android.R.attr.state_checked), generateBG(context, accentColor and 0x00ffffff or 0x55000000))
        out.addState(StateSet.WILD_CARD, generateBG(context, 0x0effffff))
        return out
    }

    private fun generateThumbDrawable(): Drawable {
        val out = StateListDrawable()
        out.addState(intArrayOf(android.R.attr.state_checked), generateCircle(context, accentColor))
        out.addState(StateSet.WILD_CARD, generateCircle(context, 0x2affffff))
        return out
    }

    companion object {

        fun generateCircle(context: Context, color: Int): Drawable {
            val r = context.dp(18).toInt()
            val inset = context.dp(4).toInt()
            return LayerDrawable(arrayOf(
                GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(color)
                    setSize(r, r)
                    setStroke(1, 0x33000000)
                },
            )).apply {
                setLayerInset(0, inset, inset, inset, inset)
            }
        }

        fun generateBG(context: Context, color: Int): Drawable {
            return GradientDrawable().apply {
                cornerRadius = Float.MAX_VALUE
                setColor(color)
                setStroke(1, 0x88000000.toInt())
            }
        }
    }
}
