package posidon.launcher.view

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import androidx.cardview.widget.CardView
import posidon.launcher.R
import posidon.launcher.tools.SpringInterpolator
import posidon.launcher.tools.dp
import kotlin.math.abs
import kotlin.math.min


class SwipeableLayout(
    val frontView: View,
    var onSwipeAway: (() -> Unit)? = null
) : CardView(frontView.context) {

    val closeIcon = ImageView(context).apply {
        setImageResource(R.drawable.ic_cross)
    }

    val backView = FrameLayout(context).apply {
        addView(closeIcon)
        clipBounds = Rect(0, 0, 0, 0)
    }

    fun setSwipeColor(color: Int) = backView.setBackgroundColor(color)

    init {
        setCardBackgroundColor(0)
        cardElevation = 0f
        radius = 0f
        preventCornerOverlap = true
        addView(backView)
        addView(frontView)
        closeIcon.run {
            layoutParams.width = 32.dp.toInt()
            layoutParams.height = 32.dp.toInt()
        }
    }

    private val onAnimEndListener = object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {}
        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationStart(animation: Animator?) {}
        override fun onAnimationEnd(animation: Animator?) { onSwipeAway?.invoke() }
    }

    var initX = 0f
    var initY = 0f
    var xOffset = 0f

    fun reset() { frontView.translationX = 0f }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_MOVE -> {
                if (xOffset > 0) {
                    closeIcon.translationX = 18.dp
                    closeIcon.translationY = (measuredHeight - 32.dp) / 2
                } else {
                    closeIcon.translationX = measuredWidth - 50.dp
                    closeIcon.translationY = (measuredHeight - 32.dp) / 2
                }
                xOffset = ev.x - initX
                if (abs(xOffset * 1.2) > min(abs(ev.y - initY), measuredHeight.toFloat())) {
                    parent.requestDisallowInterceptTouchEvent(true)
                    frontView.translationX = xOffset
                    backView.clipBounds =
                        if (xOffset > 0) Rect(0, 0, xOffset.toInt(), measuredHeight)
                        else Rect(measuredWidth + xOffset.toInt(), 0, measuredWidth, measuredHeight)
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                when {
                    xOffset > measuredWidth/7*3 -> {
                        ValueAnimator.ofInt(xOffset.toInt(), measuredWidth).apply {
                            addUpdateListener { backView.clipBounds = Rect(0, 0, it.animatedValue as Int, measuredHeight) }
                            interpolator = SpringInterpolator()
                            duration = 210L
                        }.start()
                        frontView.animate().translationX(measuredWidth.toFloat()).setInterpolator(LinearInterpolator()).setListener(onAnimEndListener).duration = 100L
                    } xOffset < -measuredWidth/7*3 -> {
                        ValueAnimator.ofInt(xOffset.toInt(), -measuredWidth).apply {
                            addUpdateListener { backView.clipBounds = Rect(measuredWidth + it.animatedValue as Int, 0, measuredWidth, measuredHeight) }
                            interpolator = SpringInterpolator()
                            duration = 210L
                        }.start()
                        frontView.animate().translationX(-measuredWidth.toFloat()).setInterpolator(LinearInterpolator()).setListener(onAnimEndListener).duration = 100L
                    } xOffset > 64.dp && ev.eventTime - ev.downTime < 160 -> {
                        ValueAnimator.ofInt(xOffset.toInt(), measuredWidth).apply {
                            addUpdateListener { backView.clipBounds = Rect(0, 0, it.animatedValue as Int, measuredHeight) }
                            interpolator = SpringInterpolator()
                            duration = 210L
                        }.start()
                        frontView.animate().translationX(measuredWidth.toFloat()).setInterpolator(LinearInterpolator()).setListener(onAnimEndListener).duration = 100L
                    } xOffset < -64.dp && ev.eventTime - ev.downTime < 160 -> {
                        ValueAnimator.ofInt(xOffset.toInt(), -measuredWidth).apply {
                            addUpdateListener { backView.clipBounds = Rect(measuredWidth + it.animatedValue as Int, 0, measuredWidth, measuredHeight) }
                            interpolator = SpringInterpolator()
                            duration = 210L
                        }.start()
                        frontView.animate().translationX(-measuredWidth.toFloat()).setInterpolator(LinearInterpolator()).setListener(onAnimEndListener).duration = 100L
                    } else -> {
                        ValueAnimator.ofInt(xOffset.toInt(), 0).apply {
                            addUpdateListener {
                                val n = it.animatedValue as Int
                                backView.clipBounds = when {
                                    n > 0 -> Rect(0, 0, n, measuredHeight)
                                    n < 0 -> Rect(measuredWidth + n, 0, measuredWidth, measuredHeight)
                                    else -> Rect(0, 0, 0, 0)
                                }
                            }
                            interpolator = SpringInterpolator()
                            duration = 210L
                            addListener(object : Animator.AnimatorListener {
                                override fun onAnimationRepeat(animation: Animator?) {}
                                override fun onAnimationCancel(animation: Animator?) {}
                                override fun onAnimationStart(animation: Animator?) {}
                                override fun onAnimationEnd(animation: Animator?) { backView.clipBounds = Rect(0, 0, 0, 0) }
                            })
                        }.start()
                        frontView.animate().translationX(0f).setInterpolator(SpringInterpolator()).duration = 210L
                    }
                }
                xOffset = 0f
                return true
            }
        }
        return super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent) = when (ev.action) {
        MotionEvent.ACTION_MOVE -> {
            xOffset = ev.x - initX
            if (abs(xOffset * 1.2) > min(abs(ev.y - initY), measuredHeight.toFloat()) && !(frontView is ViewGroup && checkForHorizontalScroll(ev, frontView))) true
            else super.onInterceptTouchEvent(ev)
        }
        MotionEvent.ACTION_UP -> if (abs(xOffset) < 8.dp || frontView is ViewGroup && checkForHorizontalScroll(ev, frontView)) super.onInterceptTouchEvent(ev) else true
        else -> {
            if (ev.action == MotionEvent.ACTION_DOWN) {
                initX = ev.x
                initY = ev.y
            }
            super.onInterceptTouchEvent(ev)
        }
    }

    fun setIconColor(value: Int) { closeIcon.imageTintList = ColorStateList.valueOf(value) }

    companion object {
        private fun checkForHorizontalScroll(ev: MotionEvent, viewGroup: ViewGroup): Boolean {
            for (i in 0 until viewGroup.childCount) {
                val child = viewGroup.getChildAt(i)
                if (child is ViewGroup &&
                    child.x <= ev.x && child.x + child.measuredWidth >= ev.x &&
                    child.y <= ev.y && child.y + child.measuredHeight >= ev.y) {
                    return if (child is HorizontalScrollView && (child.canScrollHorizontally(1) || child.canScrollHorizontally(-1)) || child is SwipeableLayout) {
                        true
                    } else checkForHorizontalScroll(ev, child)
                }
            }
            return false
        }
    }
}