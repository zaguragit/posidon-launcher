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
import posidon.android.conveniencelib.Device
import posidon.android.conveniencelib.dp
import posidon.launcher.Global
import posidon.launcher.LauncherMenu
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.vibrate
import java.lang.ref.WeakReference
import kotlin.math.abs

open class ResizableLayout(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private var dragHandle: View
    private var crossButton: View
    var stopResizingOnFingerLift = true
    var onResizeListener: OnResizeListener? = null
    private val maxHeight get() = Device.screenHeight(context) * 2 / 3

    var resizing = false
        set(value) {
            field = value
            if (field) {
                dragHandle.visibility = VISIBLE
                crossButton.visibility = VISIBLE
                dragHandle.backgroundTintList = ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(Global.accentColor))
            } else {
                dragHandle.visibility = GONE
                crossButton.visibility = GONE
            }
            if (layoutParams != null && layoutParams.height < context.dp(MIN_HEIGHT))
                layoutParams.height = context.dp(MIN_HEIGHT).toInt()
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
        private const val MIN_HEIGHT = 64
        private var currentlyResizingRef = WeakReference<ResizableLayout>(null)
        val currentlyResizing: ResizableLayout? get() = currentlyResizingRef.get()
    }

    init {
        View.inflate(context, R.layout.resizable, this)
        clipChildren = false
        dragHandle = findViewById(R.id.handle)
        dragHandle.visibility = if (resizing) VISIBLE else GONE
        dragHandle.backgroundTintList = ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(Global.accentColor))
        dragHandle.backgroundTintMode = PorterDuff.Mode.MULTIPLY
        var oldHeight = 0
        var oldMillis = System.currentTimeMillis()
        dragHandle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val location = intArrayOf(0, 0)
                    getLocationOnScreen(location)
                    if (event.rawY - location[1] >= context.dp(MIN_HEIGHT) && event.rawY - location[1] <= maxHeight) {
                        val height = (event.rawY - location[1]).toInt()
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
            currentlyResizing?.resizing = false
            resizing = true
            currentlyResizingRef = WeakReference(this)
        }
    }

    private var startX = 0f
    private var startY = 0f
    private var startRawX = 0f
    private var startRawY = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                startRawX = event.rawX
                startRawY = event.rawY
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (event.eventTime - event.downTime < ViewConfiguration.getLongPressTimeout() || !hasWindowFocus())
                    longPressHandler.removeCallbacksAndMessages(null)
            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_OUTSIDE -> {
                longPressHandler.removeCallbacksAndMessages(null)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!(Tools.isAClick(startX, event.x, startY, event.y) && Tools.isAClick(startRawX, event.rawX, startRawY, event.rawY))) {
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
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_OUTSIDE -> {
                longPressHandler.removeCallbacksAndMessages(null)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!(Tools.isAClick(startX, event.x, startY, event.y) && Tools.isAClick(startRawX, event.rawX, startRawY, event.rawY))) {
                    longPressHandler.removeCallbacksAndMessages(null)
                }
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
}