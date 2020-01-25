/*
 * Copyright (c) 2019 Leo Shneyderis
 * All rights reserved
 */

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
import posidon.launcher.tools.Settings
import posidon.launcher.tools.Sort
import posidon.launcher.tools.Tools

class CustomHiddenApps : AppCompatActivity() {

    private var pm: PackageManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Tools.applyFontSetting(this)
        setContentView(R.layout.custom_hidden_apps)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)
        pm = packageManager
        setapps(findViewById<View>(R.id.list) as ListView)
    }

    private fun setapps(list: ListView) {
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val pacslist = pm!!.queryIntentActivities(mainIntent, 0)
        val pacs = arrayOfNulls<App>(pacslist.size)
        for (i in pacslist.indices) {
            pacs[i] = App()
            pacs[i]!!.icon = pacslist[i].loadIcon(pm)
            pacs[i]!!.packageName = pacslist[i].activityInfo.packageName
            pacs[i]!!.name = pacslist[i].activityInfo.name
            pacs[i]!!.label = pacslist[i].loadLabel(pm).toString()
        }
        Sort.labelSort(pacs)
        list.adapter = ListAdapter(this, pacs)
    }

    internal inner class ListAdapter(private val context: Context, private val pacsForAdapter: Array<App?>) : BaseAdapter() {

        override fun getCount(): Int {
            return pacsForAdapter.size
        }

        override fun getItem(position: Int): Any? {
            return null
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        internal inner class ViewHolder {
            var icon: ImageView? = null
            var text: TextView? = null
        }

        override fun getView(position: Int, cv: View?, parent: ViewGroup): View {
            var convertView = cv
            val viewHolder: ViewHolder
            val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            if (convertView == null) {
                convertView = li.inflate(R.layout.list_item, null)
                viewHolder = ViewHolder()
                viewHolder.icon = convertView!!.findViewById(R.id.iconimg)
                viewHolder.text = convertView.findViewById(R.id.icontxt)
                convertView.tag = viewHolder
            } else
                viewHolder = convertView.tag as ViewHolder
            viewHolder.icon!!.setImageDrawable(pacsForAdapter[position]!!.icon)
            viewHolder.text!!.text = pacsForAdapter[position]!!.label
            when (Settings.getInt("icsize", 1)) {
                0 -> viewHolder.icon!!.setPadding(64, 64, 64, 64)
                1 -> viewHolder.icon!!.setPadding(32, 32, 32, 32)
                2 -> viewHolder.icon!!.setPadding(0, 0, 0, 0)
            }
            val finalConvertView = convertView

            val hidden = Settings.getBool(pacsForAdapter[position]!!.packageName + "/" + pacsForAdapter[position]!!.name + "?hidden", false)
            if (hidden) finalConvertView.setBackgroundColor(0x33ff0000)
            else finalConvertView.setBackgroundColor(0x0)
            convertView.setOnClickListener {
                Main.shouldSetApps = true
                Main.customized = true
                if (hidden) {
                    finalConvertView.setBackgroundColor(0x33ff0000)
                    Settings.put(pacsForAdapter[position]!!.packageName + "/" + pacsForAdapter[position]!!.name + "?hidden", false)
                    notifyDataSetChanged()
                } else {
                    finalConvertView.setBackgroundColor(0x0)
                    Settings.put(pacsForAdapter[position]!!.packageName + "/" + pacsForAdapter[position]!!.name + "?hidden", true)
                    notifyDataSetChanged()
                }
            }

            return convertView
        }
    }
}

