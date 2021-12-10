package posidon.launcher.customizations

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.google.android.material.bottomsheet.BottomSheetDialog
import posidon.android.conveniencelib.dp
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.drawable.FastColorDrawable
import posidon.launcher.items.App
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.theme.applyFontSetting

class IconPackPicker : AppCompatActivity() {

    private var lastclicked: View? = null
    private lateinit var selectionDrawable: Drawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_icon_pack_picker)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) window.setDecorFitsSystemWindows(false)
        else window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)
        window.setBackgroundDrawable(FastColorDrawable(Global.getBlackAccent()))
        val pm = packageManager
        selectionDrawable = getDrawable(R.drawable.selection)!!
        val systemIconsView = findViewById<View>(R.id.systemicons)
        lastclicked = systemIconsView

        try { findViewById<ImageView>(R.id.iconimg).setImageDrawable(pm.getApplicationIcon("com.android.systemui")) } catch (ignore: Exception) {}
        if (Settings["iconpack", "system"] == "system") {
            systemIconsView.background = selectionDrawable
        }

        findViewById<View>(R.id.systemicons).setOnClickListener { v ->
            Settings.putNotSave("iconpack", "system")
            Settings.apply()
            v.background = selectionDrawable
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
        grid.adapter = IconPackListAdapter(apps)
        grid.onItemClickListener = AdapterView.OnItemClickListener { _, view, position, _ ->
            Settings.putNotSave("iconpack", apps[position].packageName)
            Settings.apply()
            view.background = selectionDrawable
            if (lastclicked !== view) {
                lastclicked!!.setBackgroundColor(0x0)
                lastclicked = view
            }
        }
    }

    internal inner class IconPackListAdapter(private val pacsForAdapter: Array<App>) : BaseAdapter() {

        override fun getCount(): Int = pacsForAdapter.size
        override fun getItem(position: Int): Any? = null
        override fun getItemId(position: Int): Long = 0

        internal inner class ViewHolder(var icon: ImageView, var text: TextView)

        override fun getView(position: Int, cv: View?, parent: ViewGroup): View {
            var convertView = cv
            val viewHolder: ViewHolder
            if (convertView == null) {
                val li = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                convertView = li.inflate(R.layout.list_item, parent, false)!!
                viewHolder = ViewHolder(convertView.findViewById(R.id.iconimg), convertView.findViewById(R.id.icontxt))
                val appSize = parent.dp(72).toInt()
                viewHolder.icon.layoutParams = FrameLayout.LayoutParams(appSize, appSize)
                viewHolder.icon.setPadding(parent.dp(8).toInt())
                convertView.tag = viewHolder
            } else viewHolder = convertView.tag as ViewHolder
            viewHolder.icon.setImageDrawable(pacsForAdapter[position].icon)
            viewHolder.text.text = pacsForAdapter[position].label
            if (Settings["iconpack", "system"] == pacsForAdapter[position].packageName) {
                convertView.background = selectionDrawable
                lastclicked = convertView
            } else convertView.setBackgroundColor(0x0)
            return convertView
        }
    }

    fun requestClearCustomIcons(v: View) {
        val dialog = BottomSheetDialog(v.context, R.style.bottomsheet).apply {
            setContentView(R.layout.confirmation_dialog)
            window!!.findViewById<View>(R.id.design_bottom_sheet).setBackgroundResource(R.drawable.bottom_sheet)
        }
        dialog.findViewById<TextView>(R.id.button)!!.apply {
            setText(R.string.clear_custom_icons)
            setOnClickListener {
                clearCustomIcons()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    fun clearCustomIcons() {
        Settings.apply {
            stringKeys.removeIf {
                it.startsWith("app:") && it.endsWith(":icon")
            }
            apply()
        }
    }
}
