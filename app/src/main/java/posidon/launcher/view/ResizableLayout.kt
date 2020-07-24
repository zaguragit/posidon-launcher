package posidon.launcher.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import posidon.launcher.LauncherMenu
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Device
import posidon.launcher.tools.dp
import posidon.launcher.tools.vibrate
import kotlin.math.abs

class ResizableLayout(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private var dragHandle: View
    private var crossButton: View
    var stopResizingOnFingerLift = true
    var onResizeListener: OnResizeListener? = null
    private val maxHeight get() = Device.displayHeight * 2 / 3

    var resizing = false
        set(value) {
            field = value
            if (field) {
                dragHandle.visibility = VISIBLE
                crossButton.visibility = VISIBLE
                dragHandle.backgroundTintList = ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(Main.accentColor))
            } else {
                dragHandle.visibility = GONE
                crossButton.visibility = GONE
            }
            if (layoutParams != null && layoutParams.height < MIN_HEIGHT.dp)
                layoutParams.height = MIN_HEIGHT.dp.toInt()
        }

    override fun addView(child: View, index: Int) {
        super.addView(child, index)
        bringChildToFront(dragHandle)
        bringChildToFront(crossButton)
    }

    interface OnResizeListener {
        fun onUpdate(newHeight: Int)
        fun onMajorUpdate(newHeight: Int)
        fun onStop(newHeight: Int)
        fun onCrossPress()
    }

    companion object {
        private const val MIN_HEIGHT = 96
    }

    init {
        View.inflate(context, R.layout.resizable, this)
        clipChildren = false
        dragHandle = findViewById(R.id.handle)
        dragHandle.visibility = if (resizing) VISIBLE else GONE
        dragHandle.backgroundTintList = ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(Main.accentColor))
        dragHandle.backgroundTintMode = PorterDuff.Mode.MULTIPLY
        var oldHeight = 0
        var oldMillis = System.currentTimeMillis()
        dragHandle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val location = intArrayOf(0, 0)
                    getLocationOnScreen(location)
                    if (location[1] + event.rawY >= MIN_HEIGHT.dp && location[1] + event.rawY <= maxHeight) {
                        val height = (event.rawY - y).toInt()
                        layoutParams.height = height
                        layoutParams = layoutParams
                        onResizeListener?.run {
                            onUpdate(layoutParams.height)
                            val currentMillis = System.currentTimeMillis()
                            if (currentMillis - oldMillis > 45 && abs(height - oldHeight) > 5) {
                                onMajorUpdate(layoutParams.height)
                                oldHeight = height
                                oldMillis = currentMillis
                            }
                        }
                        invalidate()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    longPressHandler.removeCallbacks(onLongPress)
                    if (stopResizingOnFingerLift) resizing = false
                    onResizeListener?.run {
                        onStop(layoutParams.height)
                        onMajorUpdate(layoutParams.height)
                    }
                    invalidate()
                }
            }
            requestDisallowInterceptTouchEvent(true)
            invalidate()
            true
        }
        crossButton = findViewById(R.id.cross)
        crossButton.setOnClickListener {
            resizing = false
            onResizeListener?.onCrossPress()
        }
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ResizableLayout, 0, 0)
        try { resizing = a.getBoolean(R.styleable.ResizableLayout_resizing, false) } finally { a.recycle() }
    }

    private val longPressHandler = Handler()
    private val onLongPress = Runnable {
        if (!Settings["locked", false] && !LauncherMenu.isActive && hasWindowFocus()) {
            context.vibrate()
            resizing = true
        }
    }

    var startX = 0f
    var startY = 0f
    var startRawX = 0f
    var startRawY = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_CANCEL -> {
                longPressHandler.removeCallbacksAndMessages(null)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!(isAClick(startX, event.x, startY, event.y) && isAClick(startRawX, event.rawX, startRawY, event.rawY))) {
                    longPressHandler.removeCallbacksAndMessages(null)
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                if (event.eventTime - event.downTime > ViewConfiguration.getLongPressTimeout() && hasWindowFocus())
                    return true
                longPressHandler.removeCallbacksAndMessages(null)
            }
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                startRawX = event.rawX
                startRawY = event.rawY
                if (!resizing) {
                    longPressHandler.removeCallbacksAndMessages(null)
                    longPressHandler.postDelayed(onLongPress, ViewConfiguration.getLongPressTimeout().toLong())
                }
            }
        }
        return super.onInterceptTouchEvent(event)
    }

    inline fun isAClick(startX: Float, endX: Float, startY: Float, endY: Float): Boolean {
        val threshold = 16.dp
        return abs(startX - endX) < threshold && abs(startY - endY) < threshold
    }
}