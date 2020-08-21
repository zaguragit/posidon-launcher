package posidon.launcher.customizations

import android.content.Context
import android.os.Bundle
import android.os.UserManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Sort
import posidon.launcher.tools.Tools
import posidon.launcher.tools.applyFontSetting
import posidon.launcher.tools.dp

class CustomHiddenApps : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_hidden_apps)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)

        val apps = ArrayList<App>()
        val userManager = Main.instance.getSystemService(Context.USER_SERVICE) as UserManager

        for (profile in userManager.userProfiles) {
            val appList = Main.launcherApps.getActivityList(null, profile)
            for (i in appList.indices) {
                App[appList[i].applicationInfo.packageName, appList[i].name, profile.hashCode()]?.let { apps.add(it) }
            }
        }

        Sort.labelSort(apps)
        apps.sortWith { o1, o2 ->
            o1.label!!.compareTo(o2.label!!, ignoreCase = true)
        }
        findViewById<ListView>(R.id.list).adapter = ListAdapter(this, apps)
    }

    class ListAdapter(
        private val context: Context,
        private val apps: ArrayList<App>
    ) : BaseAdapter() {

        private val appSize = when (Settings["icsize", 1]) {
            0 -> 64.dp.toInt()
            2 -> 84.dp.toInt()
            else -> 74.dp.toInt()
        }

        override fun getCount() = apps.size
        override fun getItem(position: Int) = null
        override fun getItemId(position: Int) = 0L

        class ViewHolder(
            var icon: ImageView,
            var iconFrame: FrameLayout,
            var text: TextView
        )

        override fun getView(i: Int, cv: View?, parent: ViewGroup): View {
            var convertView = cv
            val viewHolder: ViewHolder

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)!!
                viewHolder = ViewHolder(
                    convertView.findViewById(R.id.iconimg),
                    convertView.findViewById(R.id.iconFrame),
                    convertView.findViewById(R.id.icontxt)).apply {
                    iconFrame.layoutParams.run {
                        width = appSize
                        height = appSize
                    }
                }
                convertView.tag = viewHolder
            } else {
                viewHolder = convertView.tag as ViewHolder
            }
            val app = apps[i]

            viewHolder.icon.setImageDrawable(app.icon)
            viewHolder.text.text = app.label

            val hidden = Settings["app:$app:hidden", false]
            if (hidden) {
                convertView.setBackgroundColor(0x33ff0000)
            } else {
                convertView.setBackgroundColor(0x0)
            }
            convertView.setOnClickListener {
                Main.shouldSetApps = true
                Main.customized = true
                if (hidden) {
                    convertView.setBackgroundColor(0x33ff0000)
                    Settings["app:$app:hidden"] = false
                    notifyDataSetChanged()
                } else {
                    convertView.setBackgroundColor(0x0)
                    Settings["app:$app:hidden"] = true
                    notifyDataSetChanged()
                }
            }

            return convertView
        }
    }
}