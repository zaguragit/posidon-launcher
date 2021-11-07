package posidon.launcher.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.TextView

@SuppressLint("AppCompatCustomView")
class FontFitTextView : TextView {
    // Attributes
    private val textPaint: Paint = Paint()
    var defaultTextSize = 0f

    constructor(context: Context?) : super(context) {
        initialize()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initialize()
    }

    private fun initialize() {
        textPaint.set(this.paint)
        defaultTextSize = textSize
    }

    /* Re size the font so the specified text fits in the text box
     * assuming the text box is the specified width.
     */
    private fun refitText(text: String, textWidth: Int) {
        if (textWidth <= 0 || text.isEmpty()) return
        val targetWidth = textWidth - this.paddingLeft - this.paddingRight

        // this is most likely a non-relevant call
        if (targetWidth <= 2) return

        // text already fits with the xml-defined font size?
        textPaint.set(this.paint)
        textPaint.textSize = defaultTextSize
        if (textPaint.measureText(text) <= targetWidth) {
            this.setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
            return
        }

        // adjust text size using binary search for efficiency
        var hi = defaultTextSize
        var lo = 2f
        val threshold = 0.5f // How close we have to be
        while (hi - lo > threshold) {
            val size = (hi + lo) / 2
            textPaint.textSize = size
            if (textPaint.measureText(text) >= targetWidth) hi = size // too big
            else lo = size // too small
        }

        // Use lo so that we undershoot rather than overshoot
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, lo)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val height = measuredHeight
        refitText(this.text.toString(), parentWidth)
        setMeasuredDimension(parentWidth, height)
    }

    override fun onTextChanged(
        text: CharSequence, start: Int,
        before: Int, after: Int
    ) {
        refitText(text.toString(), this.width)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w != oldw || h != oldh) {
            refitText(this.text.toString(), w)
        }
    }
}