package posidon.launcher.view

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ListPopupWindow
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import posidon.launcher.R
import posidon.launcher.tools.dp

class Spinner : AppCompatTextView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet) : super(context, attr)
    constructor(context: Context, attr: AttributeSet, defStyleAttr: Int) : super(context, attr, defStyleAttr)

    var data: Array<String> = emptyArray()

    var onSelectionChangedListener: ((Spinner) -> Unit)? = null

    inline fun setSelectionChangedListener(
        noinline listener: ((Spinner) -> Unit)?
    ) { onSelectionChangedListener = listener }

    init {
        setOnClickListener {
            var popup: PopupWindow? = null
            popup = PopupWindow(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(8.dp.toInt(), 8.dp.toInt(), 8.dp.toInt(), 8.dp.toInt())
                for (i in data.indices) {
                    addView(TextView(context).apply {
                        text = data[i]
                        setOnClickListener {
                            selectionI = i
                            popup!!.dismiss()
                            onSelectionChangedListener?.invoke(this@Spinner)
                        }
                        textSize = 18f
                        setTextColor(0xffffffff.toInt())
                        setPadding(18.dp.toInt(), 9.dp.toInt(), 18.dp.toInt(), 9.dp.toInt())
                    })
                }
            }, ListPopupWindow.WRAP_CONTENT, ListPopupWindow.WRAP_CONTENT, true).apply {
                val bg = ShapeDrawable()
                val r = 18 * context.resources.displayMetrics.density
                bg.shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
                bg.paint.color = context.resources.getColor(R.color.cardbg)
                setBackgroundDrawable(bg)
            }
            val location = IntArray(2)
            getLocationInWindow(location)
            popup.showAtLocation(this, Gravity.TOP, location[0], location[1])
        }
    }

    val selection: String
        get() = data[selectionI]

    var selectionI: Int = 0
        set(value) {
            field = value
            text = selection
        }
}