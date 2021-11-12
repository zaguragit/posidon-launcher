package posidon.launcher.customizations.settingScreens.general

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import posidon.android.conveniencelib.dp
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.drawable.FastColorDrawable
import posidon.launcher.items.App
import posidon.launcher.tools.Tools
import posidon.launcher.tools.theme.applyFontSetting
import kotlin.concurrent.thread

abstract class AppTickingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_hidden_apps)
        window.setBackgroundDrawable(FastColorDrawable(Global.getBlackAccent()))
        thread {
            val apps = getApps()
            runOnUiThread {
                val adapter = Adapter(this, apps)
                findViewById<ListView>(R.id.list).adapter = adapter
                window.navigationBarColor = resources.getColor(R.color.settingscardbg)

                findViewById<EditText>(R.id.search).addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        adapter.search(s.toString())
                    }
                })
            }
        }
    }

    abstract fun getApps(): List<App>
    abstract fun isTicked(app: App): Boolean
    abstract fun setTicked(app: App, isTicked: Boolean)

    class Adapter(
        private val activity: AppTickingActivity,
        private var apps: List<App>
    ) : BaseAdapter() {

        private val originalApps = apps

        private val appSize = activity.dp(56).toInt()

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
                convertView = LayoutInflater.from(activity).inflate(R.layout.list_item, parent, false)!!
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

            val hidden = activity.isTicked(app)
            if (hidden) {
                convertView.setBackgroundColor(0x33ff0000)
            } else {
                convertView.setBackgroundColor(0x0)
            }

            convertView.setOnClickListener {
                Global.shouldSetApps = true
                Global.customized = true
                if (hidden) {
                    convertView.setBackgroundColor(0x33ff0000)
                    activity.setTicked(app, false)
                    notifyDataSetChanged()
                } else {
                    convertView.setBackgroundColor(0x0)
                    activity.setTicked(app, true)
                    notifyDataSetChanged()
                }
            }

            return convertView
        }

        fun search(string: String) = thread (isDaemon = false) {
            val searchOptimizedString = Tools.searchOptimize(string)

            val results = ArrayList<App>()

            var i = 0
            for (app in originalApps) {
                if (
                    Tools.searchOptimize(app.label).contains(searchOptimizedString) ||
                    app.label.contains(string) ||
                    Tools.searchOptimize(app.packageName).contains(searchOptimizedString) ||
                    app.packageName.contains(string)
                ) {
                    results.add(app)
                    i++
                    continue
                }
                for (word in app.label.split(' ', ',', '.', '-', '+', '&', '_')) {
                    if (Tools.searchOptimize(word).contains(searchOptimizedString) || word.contains(string)) {
                        results.add(app)
                        i++
                        break
                    }
                }
            }

            apps = results
            activity.runOnUiThread {
                notifyDataSetChanged()
            }
        }
    }
}