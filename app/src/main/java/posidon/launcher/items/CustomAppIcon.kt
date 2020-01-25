package posidon.launcher.items

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.tools.Settings
import posidon.launcher.tools.ThemeTools
import posidon.launcher.tools.Tools

class CustomAppIcon : AppCompatActivity() {

    private val iconPacks = ArrayList<App>()
    private lateinit var app: String
    private lateinit var gridView: GridView
    private lateinit var defaultOption: View
    private lateinit var searchBar: EditText
    private var state = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gridView = GridView(this)
        setContentView(LinearLayout(this).apply {
            val li = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            addView(li.inflate(R.layout.list_item, null).apply {
                try { findViewById<ImageView>(R.id.iconimg).setImageDrawable(packageManager.getApplicationIcon("com.android.systemui")) }
                catch (ignore: Exception) {}
                findViewById<TextView>(R.id.icontxt).text = "Default"
                setOnClickListener {
                    Settings.put("app:$app:icon", "")
                    finish()
                }
                defaultOption = this
            })
            addView(EditText(this@CustomAppIcon).apply {
                hint = "Search icons"
                addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {}
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        (gridView.adapter as IconsAdapter).search(s.toString())
                    }
                })
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, (64 * resources.displayMetrics.density).toInt())
                setBackgroundColor(0xdd111213.toInt())
                visibility = View.GONE
                searchBar = this
            })
            addView(gridView)
            orientation = LinearLayout.VERTICAL
        })
        window.decorView.setBackgroundColor(0x55111213)

        app = intent.extras!!.getString("packageName", null)

        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory("com.anddoes.launcher.THEME")
        val pacslist = packageManager.queryIntentActivities(mainIntent, 0)
        for (i in pacslist.indices) {
            iconPacks.add(App())
            iconPacks[i].icon = pacslist[i].loadIcon(packageManager)
            iconPacks[i].packageName = pacslist[i].activityInfo.packageName
            iconPacks[i].label = pacslist[i].loadLabel(packageManager).toString()
        }

        gridView.adapter = IconpacksAdapter()
        gridView.setOnItemClickListener { _,_,i,_ -> try {
            gridView.numColumns = 4
            gridView.adapter = IconsAdapter(iconPacks[i].packageName!!)
            state = 1
            defaultOption.visibility = View.GONE
            searchBar.visibility = View.VISIBLE
        } catch (e: Exception) {}}
    }

    internal inner class IconpacksAdapter : BaseAdapter() {

        override fun getCount(): Int = iconPacks.size
        override fun getItem(position: Int): Any? = null
        override fun getItemId(position: Int): Long = 0

        inner class ViewHolder {
            var icon: ImageView? = null
            var text: TextView? = null
        }

        override fun getView(position: Int, cv: View?, parent: ViewGroup): View? {
            var convertView = cv
            val viewHolder: ViewHolder
            val li = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            if (convertView == null) {
                convertView = li.inflate(R.layout.list_item, null)
                viewHolder = ViewHolder()
                viewHolder.icon = convertView.findViewById(R.id.iconimg)
                viewHolder.text = convertView.findViewById(R.id.icontxt)
                convertView.tag = viewHolder
            } else viewHolder = convertView.tag as ViewHolder
            viewHolder.icon!!.setImageDrawable(iconPacks[position].icon)
            viewHolder.text!!.text = iconPacks[position].label
            viewHolder.text!!.visibility = View.VISIBLE
            viewHolder.text!!.setTextColor(Settings.getInt("labelColor", -0x11111112))
            var appSize = 0
            when (Settings.getInt("icsize", 1)) {
                0 -> appSize = (resources.displayMetrics.density * 64).toInt()
                1 -> appSize = (resources.displayMetrics.density * 74).toInt()
                2 -> appSize = (resources.displayMetrics.density * 84).toInt()
            }
            viewHolder.icon!!.layoutParams.height = appSize
            viewHolder.icon!!.layoutParams.width = appSize
            return convertView
        }
    }

    internal inner class IconsAdapter(val iconPack: String) : BaseAdapter() {

        val icons: ArrayList<String>
        val themeRes = packageManager.getResourcesForApplication(iconPack)
        val searchResults = ArrayList<String>()

        init {
            icons = try { ThemeTools.getResourceNames(themeRes, iconPack) }
            catch (ignore: Exception) { ArrayList() }
            search("")
        }

        fun search(term: String) {
            searchResults.clear()
            for (string in icons) if (cook(string).contains(cook(term))) searchResults.add(string)
            notifyDataSetChanged()
        }

        private fun cook(s: String): String {
            return s.toLowerCase()
                    .replace(",", "")
                    .replace(".", "")
                    .replace("ñ", "n")
                    .replace("ck", "c")
                    .replace("cc", "c")
                    .replace("z", "s")
                    .replace("wh", "w")
                    .replace("ts", "s")
                    .replace("tz", "s")
                    .replace("gh", "g")
                    .replace("-", "")
                    .replace("_", "")
                    .replace("/", "")
                    .replace("&", "")
        }

        override fun getCount(): Int = searchResults.size
        override fun getItem(position: Int): Any? = null
        override fun getItemId(position: Int): Long = 0

        inner class ViewHolder { var icon: ImageView? = null }

        override fun getView(i: Int, cv: View?, parent: ViewGroup): View? {
            var convertView = cv
            val viewHolder: ViewHolder
            val li = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            if (convertView == null) {
                convertView = li.inflate(R.layout.drawer_item, null)
                viewHolder = ViewHolder()
                convertView.findViewById<View>(R.id.icontxt).visibility = View.GONE
                viewHolder.icon = convertView.findViewById(R.id.iconimg)
                convertView.tag = viewHolder
            } else viewHolder = convertView.tag as ViewHolder

            try {
                val intRes = themeRes.getIdentifier(searchResults[i], "drawable", iconPack)
                viewHolder.icon!!.setImageDrawable(Tools.animate(themeRes.getDrawable(intRes)))
            } catch (ignore: Exception) {}
            var appSize = 0
            when (Settings.getInt("icsize", 1)) {
                0 -> appSize = (resources.displayMetrics.density * 64).toInt()
                1 -> appSize = (resources.displayMetrics.density * 74).toInt()
                2 -> appSize = (resources.displayMetrics.density * 84).toInt()
            }
            viewHolder.icon!!.layoutParams.height = appSize
            viewHolder.icon!!.layoutParams.width = appSize
            viewHolder.icon!!.setOnClickListener {
                println("ref:$iconPack|${searchResults[i]}")
                Settings.put("app:$app:icon", "ref:$iconPack|${searchResults[i]}")
                Main.shouldSetApps = true
                finish()
            }
            return convertView
        }
    }

    override fun onBackPressed() {
        when (state) {
            0 -> super.onBackPressed()
            1 -> {
                state = 0
                gridView.numColumns = 1
                searchBar.setText("")
                gridView.adapter = IconpacksAdapter()
                defaultOption.visibility = View.VISIBLE
                searchBar.visibility = View.GONE
            }
        }
    }
}