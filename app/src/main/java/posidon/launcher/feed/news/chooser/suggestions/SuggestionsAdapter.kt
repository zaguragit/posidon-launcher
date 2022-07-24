package posidon.launcher.feed.news.chooser.suggestions

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import io.posidon.android.conveniencelib.units.dp
import io.posidon.android.conveniencelib.units.toPixels

class SuggestionsAdapter(
    val context: Context
) : BaseExpandableListAdapter() {

    override fun getGroupCount() = Suggestions.topics.size
    override fun getChildrenCount(topicI: Int) = Suggestions.topics[topicI].sources.size
    override fun getGroup(topicI: Int) = Suggestions.topics[topicI]
    override fun getChild(topicI: Int, sourceI: Int) = Suggestions.topics[topicI].sources[sourceI]
    override fun getGroupId(topicI: Int) = topicI.toLong()
    override fun getChildId(topicI: Int, sourceI: Int) = sourceI.toLong()
    override fun hasStableIds() = false

    override fun getGroupView(topicI: Int, p1: Boolean, cv: View?, parent: ViewGroup?): View {
        val convertView = (cv ?: TextView(context).apply {
            val h = 16.dp.toPixels(this)
            val v = 8.dp.toPixels(this)
            setPadding(h, v + 8.dp.toPixels(this), h, v)
            textSize = 20f
        }) as TextView

        convertView.text = Suggestions[topicI].name

        return convertView
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun getChildView(topicI: Int, sourceI: Int, isLast: Boolean, cv: View?, parent: ViewGroup): View {
        val convertView = (cv ?: TextView(context).apply {
            val h = 16.dp.toPixels(this)
            setPadding(h + 16.dp.toPixels(this), 2.dp.toPixels(this), h, 8.dp.toPixels(this))
            textSize = 16f
        }) as TextView

        convertView.text = Suggestions[topicI][sourceI].name

        return convertView
    }

    override fun isChildSelectable(topicI: Int, sourceI: Int) = true
}
