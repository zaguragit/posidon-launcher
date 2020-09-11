package posidon.launcher.customizations

import android.content.Context
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
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Sort
import posidon.launcher.tools.Tools
import posidon.launcher.tools.applyFontSetting

class CustomHiddenAppNotifications : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_hidden_apps)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)
        setapps(findViewById(R.id.list))
    }

    private fun setapps(list: ListView) {
        val pacslist = packageManager.getInstalledPackages(0)
        val pacs = ArrayList<App>()
        for (i in pacslist.indices) {
            pacs.add(App(pacslist[i].packageName).apply {
                icon = pacslist[i].applicationInfo.loadIcon(packageManager)
                label = pacslist[i].applicationInfo.loadLabel(packageManager).toString()
            })
        }
        Sort.labelSort(pacs)
        list.adapter = ListAdapter(this, pacs)
    }

    internal class ListAdapter(private val context: Context, private val apps: ArrayList<App>) : BaseAdapter() {

        override fun getCount() = apps.size
        override fun getItem(position: Int) = null
        override fun getItemId(position: Int) = 0L

        internal class ViewHolder(
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

            val hidden = Settings["notif:ex:${app.packageName}", false]
            if (hidden) {
                finalConvertView.setBackgroundColor(0x33ff0000)
            } else {
                finalConvertView.setBackgroundColor(0x0)
            }
            convertView.setOnClickListener {
                Global.shouldSetApps = true
                Global.customized = true
                if (hidden) {
                    finalConvertView.setBackgroundColor(0x33ff0000)
                    Settings["notif:ex:${app.packageName}"] = false
                    notifyDataSetChanged()
                } else {
                    finalConvertView.setBackgroundColor(0x0)
                    Settings["notif:ex:${app.packageName}"] = true
                    notifyDataSetChanged()
                }
            }

            return convertView
        }
    }
}

