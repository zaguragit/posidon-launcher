package posidon.launcher.customizations

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.tools.Settings
import posidon.launcher.tools.Tools

class IconPackPicker : AppCompatActivity() {

    private var lastclicked: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Tools.applyFontSetting(this)
        setContentView(R.layout.custom_icon_pack_picker)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)
        val pm = packageManager
        lastclicked = findViewById(R.id.systemicons)

        try { findViewById<ImageView>(R.id.iconimg).setImageDrawable(pm.getApplicationIcon("com.android.systemui")) } catch (ignore: Exception) {}
        if (Settings.getString("iconpack", "system") == "system") {
            findViewById<View>(R.id.systemicons).background = getDrawable(R.drawable.selection)
        }

        findViewById<View>(R.id.systemicons).setOnClickListener { v ->
            Settings.putString("iconpack", "system")
            v.background = getDrawable(R.drawable.selection)
            if (lastclicked !== v) {
                lastclicked!!.setBackgroundColor(0x0)
                lastclicked = v
            }
        }

        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory("com.anddoes.launcher.THEME")
        val pacslist = pm.queryIntentActivities(mainIntent, 0)
        val apps = arrayOfNulls<App>(pacslist.size)
        for (i in pacslist.indices) {
            apps[i] = App()
            apps[i]!!.icon = pacslist[i].loadIcon(pm)
            apps[i]!!.packageName = pacslist[i].activityInfo.packageName
            apps[i]!!.label = pacslist[i].loadLabel(pm).toString()
        }
        val grid = findViewById<GridView>(R.id.grid)
        grid.adapter = IconPackListAdapter(this, apps as Array<App>)
        grid.onItemClickListener = AdapterView.OnItemClickListener { _, view, position, _ ->
            Settings.putString("iconpack", apps[position].packageName)
            view.background = getDrawable(R.drawable.selection)
            if (lastclicked !== view) {
                lastclicked!!.setBackgroundColor(0x0)
                lastclicked = view
            }
        }
    }

    internal inner class IconPackListAdapter(private val context: Context, private val pacsForAdapter: Array<App>) : BaseAdapter() {

        override fun getCount(): Int = pacsForAdapter.size
        override fun getItem(position: Int): Any? = null
        override fun getItemId(position: Int): Long = 0

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
            } else viewHolder = convertView.tag as ViewHolder
            viewHolder.icon!!.setImageDrawable(pacsForAdapter[position].icon)
            viewHolder.text!!.text = pacsForAdapter[position].label
            when (Settings.getInt("icsize", 1)) {
                0 -> viewHolder.icon!!.setPadding(64, 64, 64, 64)
                1 -> viewHolder.icon!!.setPadding(32, 32, 32, 32)
                2 -> viewHolder.icon!!.setPadding(0, 0, 0, 0)
            }
            if (Settings.getString("iconpack", "system") == pacsForAdapter[position].packageName) {
                convertView.background = getDrawable(R.drawable.selection)
                lastclicked = convertView
            } else convertView.setBackgroundColor(0x0)
            return convertView
        }
    }
}
