package posidon.launcher.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import posidon.launcher.Main
import posidon.launcher.R

class ResizableLayout(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private lateinit var dragHandle: View
    private lateinit var crossButton: View
    var stopResizingOnFingerLift = true
    var onResizeListener: OnResizeListener? = null

    private fun init() {
        View.inflate(context, R.layout.resizable, this)
        clipChildren = false
        dragHandle = findViewById(R.id.handle)
        dragHandle.visibility = if (resizing) View.VISIBLE else View.GONE
        dragHandle.backgroundTintList = ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(Main.accentColor))
        dragHandle.backgroundTintMode = PorterDuff.Mode.MULTIPLY
        dragHandle.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_MOVE) {
                if (event.rawY >= MIN_HEIGHT * resources.displayMetrics.density) {
                    layoutParams.height = (event.rawY - y).toInt()
                    layoutParams = layoutParams
                    if (onResizeListener != null) onResizeListener!!.onUpdate(layoutParams.height)
                    invalidate()
                }
            } else if (event.action == MotionEvent.ACTION_UP) {
                if (stopResizingOnFingerLift) resizing = false
                if (onResizeListener != null) onResizeListener!!.onStop(layoutParams.height)
                invalidate()
            }
            requestDisallowInterceptTouchEvent(true)
            invalidate()
            true
        }
        crossButton = findViewById(R.id.cross)
        crossButton.setOnClickListener {
            resizing = false
            if (onResizeListener != null) onResizeListener!!.onCrossPress()
        }
    }

    var resizing = false
        set(value) {
            field = value
            dragHandle.visibility = if (field) View.VISIBLE else View.GONE
            crossButton.visibility = if (field) View.VISIBLE else View.GONE
            if (layoutParams != null && layoutParams.height < MIN_HEIGHT * resources.displayMetrics.density) layoutParams.height = (MIN_HEIGHT * resources.displayMetrics.density).toInt()
        }

    override fun addView(child: View, index: Int) {
        super.addView(child, index)
        bringChildToFront(dragHandle)
        bringChildToFront(crossButton)
    }

    interface OnResizeListener {
        fun onUpdate(newHeight: Int)
        fun onStop(newHeight: Int)
        fun onCrossPress()
    }

    companion object {
        private const val MIN_HEIGHT = 96
    }

    init {
        init()
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ResizableLayout, 0, 0)
        try { resizing = a.getBoolean(R.styleable.ResizableLayout_resizing, false) } finally { a.recycle() }
    }
}