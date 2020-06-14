package posidon.launcher.items

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.SectionIndexer
import posidon.launcher.Main
import posidon.launcher.tools.Tools
import posidon.launcher.view.AppSectionView
import posidon.launcher.view.HighlightAdapter

class SectionedDrawerAdapter : BaseAdapter(), SectionIndexer, HighlightAdapter {

    override fun getCount(): Int = Main.appSections.size
    override fun getItem(i: Int) = Main.appSections[i]
    override fun getItemId(i: Int): Long = 0

    override fun getView(i: Int, cv: View?, parent: ViewGroup): View? {
        val section = Main.appSections[i]
        val convertView = cv as AppSectionView? ?: AppSectionView(Tools.publicContext!!)
        convertView.background = if (highlightI == i) HighlightAdapter.createHighlightDrawable() else null
        convertView.setApps(section)
        convertView.title = section[0].label!![0].toUpperCase().toString()
        return convertView
    }

    private val savedSections = Array(Main.appSections.size) { Main.appSections[it][0].label!![0].toUpperCase() }
    override fun getSections(): Array<Char> = savedSections
    override fun getSectionForPosition(i: Int): Int = savedSections.indexOf(Main.appSections[i][0].label!![0].toUpperCase())
    override fun getPositionForSection(i: Int): Int {
        val char = savedSections[i]
        for (j in Main.appSections.indices) {
            if (Main.appSections[j][0].label!![0].toUpperCase() == char) {
                return j
            }
        }
        return 0
    }

    private var highlightI = -1

    override fun highlight(i: Int) {
        highlightI = i
    }

    override fun unhighlight() {
        highlightI = -1
    }
}