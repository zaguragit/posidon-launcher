package posidon.launcher.view

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.AbsListView
import android.widget.SectionIndexer
import posidon.launcher.storage.Settings
import posidon.launcher.tools.dp
import posidon.launcher.tools.getStatusBarHeight
import posidon.launcher.tools.mainFont

class AlphabetScrollbar(
    val listView: AbsListView,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(listView.context, attrs, defStyleAttr) {

    var sectionIndexer: SectionIndexer? = listView.adapter.let {
        if (it is SectionIndexer && it.sections.isArrayOf<Char>())
            it else null
    }

    private var fg = 0
    private var topPadding = 0f
    private var paint = Paint()

    init {
        listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScroll(v: AbsListView?, visItem0: Int, visItems: Int, items: Int) = this@AlphabetScrollbar.invalidate()
            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {}
        })
        update()
    }

    fun updateAdapter() {
        sectionIndexer = listView.adapter.let {
            if (it is SectionIndexer && it.sections.isArrayOf<Char>()) it else null
        }
    }

    fun update() {
        fg = Settings["labelColor", -0x11111112]
        topPadding = listView.paddingTop + context.getStatusBarHeight() + Settings["dockbottompadding", 10].dp
        paint.apply {
            color = fg
            alpha = 180
            isAntiAlias = true
            typeface = context.mainFont
            textSize = 16.dp
        }
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (sectionIndexer != null && sectionIndexer!!.sections.isNotEmpty()) {
            val sectionHeight = (height - topPadding - listView.paddingBottom) / sectionIndexer!!.sections.size
            for (i in sectionIndexer!!.sections.indices) {
                if (getLetterIToHighlight() == i) {
                    val tmp = paint.typeface
                    paint.typeface = Typeface.create(tmp, Typeface.BOLD)
                    paint.alpha = 255
                    canvas.drawText(sectionIndexer!!.sections[i].toString(), 0f, sectionHeight * i + topPadding, paint)
                    paint.alpha = 180
                    paint.typeface = tmp
                } else canvas.drawText(sectionIndexer!!.sections[i].toString(), 0f, sectionHeight * i + topPadding, paint)
            }
        }
    }

    private inline fun getLetterIToHighlight() = sectionIndexer?.getSectionForPosition(listView.firstVisiblePosition) ?: -1

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                sectionIndexer?.getPositionForSection(yToIndex(event.y))?.let {
                    listView.smoothScrollToPositionFromTop(it, topPadding.toInt(), 150)
                    if (sectionIndexer is HighlightAdapter) {
                        (sectionIndexer as HighlightAdapter).highlight(it)
                        listView.invalidateViews()
                    }
                }
                invalidate()
            }
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
                listView.adapter.let {
                    if (it is SectionIndexer && it.sections.isArrayOf<Char>()) {
                        sectionIndexer = it
                    }
                    sectionIndexer?.getPositionForSection(yToIndex(event.y))?.let { it1 ->
                        listView.smoothScrollToPositionFromTop(it1, topPadding.toInt(), 150)
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
            }
        }
        return true
    }

    private fun yToIndex(y: Float): Int {
        val out = ((y - topPadding) / (height - topPadding - listView.paddingBottom) * sectionIndexer!!.sections.size).toInt()
        if (out < 0) return 0
        if (out > sectionIndexer?.sections?.lastIndex ?: 0) return sectionIndexer?.sections?.lastIndex ?: 0
        return out
    }
}