package posidon.launcher.customizations

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import posidon.android.conveniencelib.dp
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.applyFontSetting

class IconPackPicker : AppCompatActivity() {

    private var lastclicked: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_icon_pack_picker)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) window.setDecorFitsSystemWindows(false)
        else window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)
        val pm = packageManager
        lastclicked = findViewById(R.id.systemicons)

        try { findViewById<ImageView>(R.id.iconimg).setImageDrawable(pm.getApplicationIcon("com.android.systemui")) } catch (ignore: Exception) {}
        if (Settings["iconpack", "system"] == "system") {
            findViewById<View>(R.id.systemicons).background = getDrawable(R.drawable.selection)
        }

        findViewById<View>(R.id.systemicons).setOnClickListener { v ->
            Settings.putNotSave("iconpack", "system")
            Settings.apply()
            v.background = getDrawable(R.drawable.selection)
            if (lastclicked !== v) {
                lastclicked!!.setBackgroundColor(0x0)
                lastclicked = v
            }
        }

        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory("com.anddoes.launcher.THEME")
        val pacslist = pm.queryIntentActivities(mainIntent, 0)
        val apps = Array(pacslist.size) {
            val app = App(pacslist[it].activityInfo.packageName, pacslist[it].activityInfo.name, label = pacslist[it].loadLabel(pm).toString())
            app.icon = pacslist[it].loadIcon(pm)
            app
        }
        apps.sortWith { o1, o2 ->
            o1.label.compareTo(o2.label, ignoreCase = true)
        }
        val grid = findViewById<GridView>(R.id.grid)
        grid.adapter = IconPackListAdapter(this, apps)
        grid.onItemClickListener = AdapterView.OnItemClickListener { _, view, position, _ ->
            Settings.putNotSave("iconpack", apps[position].packageName)
            Settings.apply()
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

        internal inner class ViewHolder(var icon: ImageView, var text: TextView)

        override fun getView(position: Int, cv: View?, parent: ViewGroup): View {
            var convertView = cv
            val viewHolder: ViewHolder
            if (convertView == null) {
                val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                convertView = li.inflate(R.layout.list_item, parent, false)!!
                viewHolder = ViewHolder(convertView.findViewById(R.id.iconimg), convertView.findViewById(R.id.icontxt))
                val appSize = when (Settings["dockicsize", 1]) {
                    0 -> context.dp(64).toInt()
                    2 -> context.dp(84).toInt()
                    else -> context.dp(74).toInt()
                }
                viewHolder.icon.layoutParams = FrameLayout.LayoutParams(appSize, appSize)
                convertView.tag = viewHolder
            } else viewHolder = convertView.tag as ViewHolder
            viewHolder.icon.setImageDrawable(pacsForAdapter[position].icon)
            viewHolder.text.text = pacsForAdapter[position].label
            if (Settings["iconpack", "system"] == pacsForAdapter[position].packageName) {
                convertView.background = getDrawable(R.drawable.selection)
                lastclicked = convertView
            } else convertView.setBackgroundColor(0x0)
            return convertView
        }
    }
}
