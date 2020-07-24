package posidon.launcher.customizations

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Sort
import posidon.launcher.tools.Tools
import posidon.launcher.tools.applyFontSetting

class CustomHiddenApps : AppCompatActivity() {

    private var pm: PackageManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_hidden_apps)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)
        pm = packageManager
        setapps(findViewById(R.id.list))
    }

    private fun setapps(list: ListView) {
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val pacslist = pm!!.queryIntentActivities(mainIntent, 0)
        val pacs = ArrayList<App>()
        for (i in pacslist.indices) {
            pacs.add(App(pacslist[i].activityInfo.packageName, pacslist[i].activityInfo.name).apply {
                icon = pacslist[i].loadIcon(pm)
                label = pacslist[i].loadLabel(pm).toString()
            })
        }
        Sort.labelSort(pacs)
        list.adapter = ListAdapter(this, pacs)
    }

    internal class ListAdapter(private val context: Context, private val apps: ArrayList<App>) : BaseAdapter() {

        override fun getCount() = apps.size
        override fun getItem(position: Int) = null
        override fun getItemId(position: Int) = 0L

        internal inner class ViewHolder(
                var icon: ImageView,
                var text: TextView
        )

        override fun getView(i: Int, cv: View?, parent: ViewGroup): View {
            var convertView = cv
            val viewHolder: ViewHolder

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
                viewHolder = ViewHolder(convertView!!.findViewById(R.id.iconimg), convertView.findViewById(R.id.icontxt))
                convertView.tag = viewHolder
            } else {
                viewHolder = convertView.tag as ViewHolder
            }
            val app = apps[i]
            viewHolder.icon.setImageDrawable(app.icon)
            viewHolder.text.text = app.label
            when (Settings["icsize", 1]) {
                0 -> viewHolder.icon.setPadding(64, 64, 64, 64)
                1 -> viewHolder.icon.setPadding(32, 32, 32, 32)
                2 -> viewHolder.icon.setPadding(0, 0, 0, 0)
            }
            val finalConvertView = convertView

            val hidden = Settings[app.packageName + "/" + app.name + "?hidden", false]
            if (hidden) {
                finalConvertView.setBackgroundColor(0x33ff0000)
            } else {
                finalConvertView.setBackgroundColor(0x0)
            }
            convertView.setOnClickListener {
                Main.shouldSetApps = true
                Main.customized = true
                if (hidden) {
                    finalConvertView.setBackgroundColor(0x33ff0000)
                    Settings[app.packageName + "/" + app.name + "?hidden"] = false
                    notifyDataSetChanged()
                } else {
                    finalConvertView.setBackgroundColor(0x0)
                    Settings[app.packageName + "/" + app.name + "?hidden"] = true
                    notifyDataSetChanged()
                }
            }

            return convertView
        }
    }
}