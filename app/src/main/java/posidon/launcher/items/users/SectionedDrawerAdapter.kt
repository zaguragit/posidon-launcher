package posidon.launcher.items.users

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.SectionIndexer
import posidon.launcher.Global
import posidon.launcher.view.drawer.DrawerView
import posidon.launcher.view.groupView.AppSectionView
import posidon.launcher.view.recycler.HighlightAdapter

class SectionedDrawerAdapter(val drawer: DrawerView) : BaseAdapter(), SectionIndexer,
    HighlightAdapter {

    override fun getCount(): Int = Global.appSections.size
    override fun getItem(i: Int) = Global.appSections[i]
    override fun getItemId(i: Int): Long = 0

    override fun getView(i: Int, cv: View?, parent: ViewGroup): View {
        val section = Global.appSections[i]
        val convertView = cv as AppSectionView? ?: AppSectionView(drawer, "drawer")
        convertView.background = if (highlightI == i) HighlightAdapter.createHighlightDrawable() else null
        convertView.setItems(section, parent)
        convertView.title = section[0].label[0].uppercaseChar().toString()
        return convertView
    }

    private val savedSections = Array(Global.appSections.size) { Global.appSections[it][0].label[0].uppercaseChar() }
    override fun getSections(): Array<Char> = savedSections
    override fun getSectionForPosition(i: Int): Int = savedSections.indexOf(Global.appSections[i][0].label[0].uppercaseChar())
    override fun getPositionForSection(i: Int): Int {
        val char = savedSections[i]
        for (j in Global.appSections.indices) {
            if (Global.appSections[j][0].label[0].uppercaseChar() == char) {
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