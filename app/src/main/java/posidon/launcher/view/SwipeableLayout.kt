package posidon.launcher.view

import android.animation.Animator
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import posidon.launcher.R
import posidon.launcher.tools.SpringInterpolator
import posidon.launcher.tools.dp
import kotlin.math.abs
import kotlin.math.min

class SwipeableLayout(
    val frontView: View,
    var onSwipeAway: () -> Unit
) : FrameLayout(frontView.context) {

    val backView = ImageView(context).apply {
        setImageResource(R.drawable.ic_cross)
    }

    init {
        addView(backView)
        addView(frontView)
        backView.run {
            layoutParams.width = 32.dp.toInt()
            layoutParams.height = 32.dp.toInt()
        }
    }

    private val onAnimEndListener = object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {}
        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationStart(animation: Animator?) {}
        override fun onAnimationEnd(animation: Animator?) = onSwipeAway()
    }

    var initX = 0f
    var initY = 0f
    var xOffset = 0f

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_MOVE -> {
                xOffset = ev.x - initX
                if (xOffset > 0) {
                    backView.translationX = 18.dp
                    backView.translationY = (measuredHeight - 32.dp) / 2
                } else {
                    backView.translationX = measuredWidth - 50.dp
                    backView.translationY = (measuredHeight - 32.dp) / 2
                }
                if (abs(xOffset * 1.2) > min(abs(ev.y - initY), measuredHeight.toFloat())) {
                    parent.requestDisallowInterceptTouchEvent(true)
                    frontView.translationX = xOffset
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                when {
                    xOffset > measuredWidth/7*3 ->
                        frontView.animate().translationX(measuredWidth.toFloat()).setInterpolator(LinearInterpolator()).setListener(onAnimEndListener).duration = 100L
                    xOffset < -measuredWidth/7*3 ->
                        frontView.animate().translationX(-measuredWidth.toFloat()).setInterpolator(LinearInterpolator()).setListener(onAnimEndListener).duration = 100L
                    xOffset > 64.dp && ev.eventTime - ev.downTime < 160 ->
                        frontView.animate().translationX(measuredWidth.toFloat()).setInterpolator(LinearInterpolator()).setListener(onAnimEndListener).duration = 100L
                    xOffset < -64.dp && ev.eventTime - ev.downTime < 160 ->
                        frontView.animate().translationX(-measuredWidth.toFloat()).setInterpolator(LinearInterpolator()).setListener(onAnimEndListener).duration = 100L
                    else -> frontView.animate().translationX(0f).setInterpolator(SpringInterpolator()).duration = 210L
                }
                xOffset = 0f
                return true
            }
        }
        return super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent) = when (ev.action) {
        MotionEvent.ACTION_MOVE -> true
        MotionEvent.ACTION_UP -> if (abs(xOffset) == 0f) super.onInterceptTouchEvent(ev) else true
        else -> {
            if (ev.action == MotionEvent.ACTION_DOWN) {
                initX = ev.x
                initY = ev.y
            }
            super.onInterceptTouchEvent(ev)
        }
    }

    fun setIconColor(value: Int) { backView.imageTintList = ColorStateList.valueOf(value) }
}