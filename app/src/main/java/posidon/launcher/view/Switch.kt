package posidon.launcher.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.util.StateSet
import posidon.android.conveniencelib.dp
import posidon.launcher.Global
import posidon.launcher.tools.Tools
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

    private fun generateTrackDrawable(): Drawable {
        val out = StateListDrawable()
        out.addState(intArrayOf(android.R.attr.state_checked), generateBG(accentColor and 0x00ffffff or 0x55000000))
        out.addState(StateSet.WILD_CARD, generateBG(0xff08090a.toInt()))
        return out
    }

    private fun generateThumbDrawable(): Drawable {
        val out = StateListDrawable()
        out.addState(intArrayOf(android.R.attr.state_checked), generateCircle(accentColor))
        out.addState(StateSet.WILD_CARD, generateCircle(0xff252627.toInt()))
        return out
    }

    companion object {

        fun generateCircle(color: Int): Drawable {
            val r = Tools.appContext!!.dp(24).toInt()
            return LayerDrawable(arrayOf(
                    GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(color)
                        setSize(r, r)
                        setStroke(1, 0xdd000000.toInt())
                    },
                    GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        val highlight = Color.HSVToColor(floatArrayOf(0f, 0f, 0f).apply {
                            Color.colorToHSV(color, this)
                            this[2] *= 1.8f * (1 + this[2] * this[2])
                            this[1] *= 0.4f
                        }) and 0x00ffffff
                        colors = intArrayOf(highlight or 0x55000000, Color.HSVToColor(floatArrayOf(0f, 0f, 0f).apply {
                            Color.colorToHSV(color, this)
                            this[1] *= 1.08f
                            this[2] *= 1.6f
                        }) and 0x00ffffff)
                        setSize(r, r)
                        setStroke(Tools.appContext!!.dp(1).toInt(), highlight or 0x12000000)
                    }
            ))
        }

        fun generateBG(color: Int): Drawable {
            val inset = Tools.appContext!!.dp(3).toInt()
            return LayerDrawable(arrayOf(
                    GradientDrawable().apply {
                        cornerRadius = Tools.appContext!!.dp(11)
                        val shadow = Color.HSVToColor(floatArrayOf(0f, 0f, 0f).apply {
                            Color.colorToHSV(color, this)
                            this[2] *= 0.8f
                            this[1] *= 1.08f
                        }) and 0x00ffffff
                        colors = intArrayOf(shadow or (color and 0xff000000.toInt()), color)
                        setStroke(Tools.appContext!!.dp(1).toInt(), Color.HSVToColor(floatArrayOf(0f, 0f, 0f).apply {
                            Color.colorToHSV(shadow, this)
                            this[2] *= 0.4f
                            this[1] *= 2f
                        }) and 0x00ffffff or 0x33000000)
                    },
                    GradientDrawable().apply {
                        cornerRadius = Tools.appContext!!.dp(12)
                        setStroke(Tools.appContext!!.dp(1).toInt(), 0x06ffffff)
                    }
            )).apply {
                val ii = inset + Tools.appContext!!.dp(1).toInt()
                setLayerInset(0, ii, ii, ii, ii)
                setLayerInset(1, inset, inset, inset, inset)
            }
        }
    }
}
