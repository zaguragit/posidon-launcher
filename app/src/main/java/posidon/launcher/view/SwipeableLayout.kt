package posidon.launcher.view

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import androidx.cardview.widget.CardView
import posidon.android.conveniencelib.SpringInterpolator
import posidon.android.conveniencelib.dp
import posidon.android.conveniencelib.onEnd
import posidon.launcher.R
import kotlin.math.abs

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
        visibility = GONE
    }

    fun setSwipeColor(color: Int) = backView.setBackgroundColor(color)

    var cornerRadiusCompensation = 0f

    override fun setOnClickListener(l: OnClickListener?) = frontView.setOnClickListener(l)

    init {
        setCardBackgroundColor(0)
        cardElevation = 0f
        radius = 0f
        preventCornerOverlap = true
        addView(backView)
        addView(frontView)
        closeIcon.run {
            layoutParams.width = dp(32).toInt()
            layoutParams.height = dp(32).toInt()
        }
    }

    private val onAnimEndListener = object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {}
        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationStart(animation: Animator?) {}
        override fun onAnimationEnd(animation: Animator?) { onSwipeAway?.invoke() }
    }

    private var initX = 0f
    private var initY = 0f
    private var xOffset = 0f

    fun reset() {
        frontView.translationX = 0f
        backView.clipBounds = Rect(0, 0, 0, 0)
        backView.visibility = GONE
    }

    private fun bounceBack() {
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
            duration = 350L
            onEnd {
                backView.clipBounds = Rect(0, 0, 0, 0)
                backView.visibility = GONE
            }
        }.start()
        frontView.animate().translationX(0f).setInterpolator(SpringInterpolator()).setListener(null).duration = 350L
    }

    private fun sashayAway(direction: Int) {
        ValueAnimator.ofFloat(frontView.translationX, measuredWidth * direction.toFloat()).apply {
            addUpdateListener {
                val f = it.animatedValue as Float
                if (direction == 1)
                    backView.clipBounds = Rect(0, 0, f.toInt(), measuredHeight)
                else
                    backView.clipBounds = Rect(f.toInt() + measuredWidth, 0, measuredWidth, measuredHeight)
                frontView.translationX = f
            }
            interpolator = DecelerateInterpolator()
            duration = 110L
            addListener(onAnimEndListener)
        }.start()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_MOVE -> {
                xOffset = ev.x - initX
                parent.requestDisallowInterceptTouchEvent(true)
                frontView.translationX = xOffset
                backView.clipBounds =
                    if (xOffset > 0) {
                        closeIcon.translationX = dp(18)
                        closeIcon.translationY = (measuredHeight - dp(32)) / 2
                        Rect(0, 0, xOffset.toInt() + cornerRadiusCompensation.toInt(), measuredHeight)
                    }
                    else {
                        closeIcon.translationX = measuredWidth - dp(50)
                        closeIcon.translationY = (measuredHeight - dp(32)) / 2
                        Rect(measuredWidth + xOffset.toInt() - cornerRadiusCompensation.toInt(), 0, measuredWidth, measuredHeight)
                    }
                return true
            }
            MotionEvent.ACTION_UP -> {
                when {
                    xOffset > measuredWidth/7*3 ||
                            xOffset > dp(64) && ev.eventTime - ev.downTime < 160 -> {
                        sashayAway(1)
                    }
                    xOffset < -measuredWidth/7*3 ||
                            xOffset < -dp((64)) && ev.eventTime - ev.downTime < 160 -> {
                        sashayAway(-1)
                    }
                    else -> bounceBack()
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
            val absYOffset = abs(ev.y - initY)
            val absXOffset = abs(xOffset)
            if (abs(absXOffset - absYOffset) > context.dp(2) && absXOffset > absYOffset && !(frontView is ViewGroup && checkForHorizontalScroll(ev, frontView))) {
                true
            } else {
                initX = ev.x
                initY = ev.y
                super.onInterceptTouchEvent(ev)
            }
        }
        MotionEvent.ACTION_UP -> if (abs(xOffset) < context.dp(12) || frontView is ViewGroup && checkForHorizontalScroll(ev, frontView)) {
            super.onInterceptTouchEvent(ev)
        } else true
        else -> {
            if (ev.action == MotionEvent.ACTION_DOWN) {
                initX = ev.x
                initY = ev.y
                backView.visibility = VISIBLE
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