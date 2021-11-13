package posidon.launcher.view.drawer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.annotation.IntDef
import posidon.android.conveniencelib.Colors
import posidon.android.conveniencelib.dp
import posidon.launcher.items.users.ItemLongPress
import posidon.launcher.tools.theme.mainFont
import posidon.launcher.view.recycler.HighlightAdapter
import kotlin.math.roundToInt

open class AlphabetScrollbar(
    private val listView: AbsListView,
    @Orientation
    var orientation: Int,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(listView.context, attrs, defStyleAttr) {

    var onStartScroll = {}
    var onCancelScroll = {}

    var floatingFactor = 0f
        set(value) {
            field = value
            invalidate()
        }

    var textColor = 0
    var floatingColor = 0
    var highlightColor = 0

    fun updateAdapter() {
        sectionIndexer = listView.adapter.let {
            if (it is SectionIndexer && it.sections.isArrayOf<Char>()) it else null
        }
    }

    fun updateTheme() {
        paint.apply {
            typeface = context.mainFont
            textSize = context.dp(16)
        }
        invalidate()
    }

    private var paint = Paint().apply {
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private var sectionIndexer: SectionIndexer? = listView.adapter.let {
        if (it is SectionIndexer && it.sections.isArrayOf<Char>())
            it else null
    }

    init {
        listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScroll(v: AbsListView?, visItem0: Int, visItems: Int, items: Int) = this@AlphabetScrollbar.invalidate()
            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
                ItemLongPress.currentPopup?.dismiss()
            }
        })
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (sectionIndexer != null && sectionIndexer!!.sections.isNotEmpty()) {
            if (floatingFactor != 0f) {
                paint.setShadowLayer(floatingFactor * 8f, 0f, floatingFactor * 3f, 0x33000000)
                paint.color = Colors.blend(floatingColor, textColor, floatingFactor)
            } else {
                paint.clearShadowLayer()
                paint.color = textColor
            }
            val insideHeight = height - paddingTop - paddingBottom
            val insideWidth = width - paddingLeft - paddingRight
            val (a, b) = if (orientation == VERTICAL) {
                insideHeight / sectionIndexer!!.sections.lastIndex.toFloat() to insideWidth / 2f + paddingLeft
            } else {
                insideWidth / sectionIndexer!!.sections.lastIndex.toFloat() to (insideHeight + paint.textSize) / 2f + paddingTop
            }
            for (i in sectionIndexer!!.sections.indices) {
                val (x, y) = if (orientation == VERTICAL) {
                    b to a * i + paddingTop
                } else {
                    a * i + paddingLeft to b
                }
                if (getSectionI() == i && floatingFactor == 0f) {
                    val tmp = paint.typeface
                    paint.typeface = Typeface.create(tmp, Typeface.BOLD)
                    paint.color = highlightColor
                    canvas.drawText(sectionIndexer!!.sections[i].toString(), x, y, paint)
                    paint.color = textColor
                    paint.typeface = tmp
                } else canvas.drawText(sectionIndexer!!.sections[i].toString(), x, y, paint)
            }
        }
    }

    private inline fun getSectionI() = if (currentSection == -1)
        sectionIndexer?.getSectionForPosition(listView.firstVisiblePosition) ?: -1
    else currentSection

    private var currentSection = -1

    private inline fun scrollTo(i: Int) {
        listView.smoothScrollToPositionFromTop(i, listView.height / 2, 150)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                val i = coordsToIndex(event.x, event.y)
                currentSection = i
                sectionIndexer?.getPositionForSection(i)?.let {
                    scrollTo(it)
                    if (sectionIndexer is HighlightAdapter) {
                        (sectionIndexer as HighlightAdapter).highlight(it)
                        listView.invalidateViews()
                    }
                }
                invalidate()
            }
            MotionEvent.ACTION_DOWN -> {
                onStartScroll()
                parent.requestDisallowInterceptTouchEvent(true)
                listView.adapter.let {
                    if (it is SectionIndexer && it.sections.isArrayOf<Char>()) {
                        sectionIndexer = it
                    }
                    val i = coordsToIndex(event.x, event.y)
                    currentSection = i
                    sectionIndexer?.getPositionForSection(i)?.let { it1 ->
                        scrollTo(it1)
                        if (sectionIndexer is HighlightAdapter) {
                            (sectionIndexer as HighlightAdapter).highlight(it1)
                            listView.invalidateViews()
                        }
                    }
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (sectionIndexer is HighlightAdapter) {
                    (sectionIndexer as HighlightAdapter).unhighlight()
                    listView.invalidateViews()
                }
                currentSection = -1
            }
            MotionEvent.ACTION_CANCEL -> {
                onCancelScroll()
                currentSection = -1
            }
            else -> currentSection = -1
        }
        return true
    }

    private fun coordsToIndex(x: Float, y: Float): Int {
        if (orientation == VERTICAL) {
            val out = ((y - paddingTop) / (height - paddingTop - paddingBottom) * sectionIndexer!!.sections.lastIndex).roundToInt()
            if (out < 0) return 0
            if (out > sectionIndexer?.sections?.lastIndex ?: 0) return sectionIndexer?.sections?.lastIndex ?: 0
            return out
        } else {
            val out = ((x - paddingLeft) / (width - paddingLeft - paddingRight) * sectionIndexer!!.sections.lastIndex).roundToInt()
            if (out < 0) return 0
            if (out > sectionIndexer?.sections?.lastIndex ?: 0) return sectionIndexer?.sections?.lastIndex ?: 0
            return out
        }
    }

    companion object {
        @IntDef(VERTICAL, HORIZONTAL)
        annotation class Orientation

        const val VERTICAL = 0
        const val HORIZONTAL = 1
    }
}