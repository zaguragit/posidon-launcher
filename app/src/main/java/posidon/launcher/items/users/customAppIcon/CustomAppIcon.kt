package posidon.launcher.items.users.customAppIcon

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
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.launcherutils.IconTheming
import posidon.android.conveniencelib.Graphics
import posidon.android.conveniencelib.dp
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.storage.Settings
import posidon.launcher.tools.theme.Icons
import posidon.launcher.view.recycler.GridLayoutManager
import posidon.launcher.view.recycler.LinearLayoutManager
import kotlin.concurrent.thread

class CustomAppIcon : AppCompatActivity() {

    private val iconPacks = ArrayList<App>()
    private lateinit var key: String
    private lateinit var recycler: RecyclerView
    private lateinit var defaultOption: View
    private lateinit var searchBar: EditText
    private var state = 0

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var gridLayoutManager: GridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recycler = RecyclerView(this)
        linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        gridLayoutManager = GridLayoutManager(this, 4)
        key = intent.extras!!.getString("key", null)
        setContentView(LinearLayout(this).apply {
            val li = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            addView(li.inflate(R.layout.list_item, this, false).apply {
                runCatching {
                    findViewById<ImageView>(R.id.iconimg).setImageDrawable(Icons.applyInsets(
                        Icons.generateAdaptiveIcon(packageManager.getApplicationIcon("com.android.systemui"))
                    ))
                }
                findViewById<TextView>(R.id.icontxt).text = context.getString(R.string._default)
                setOnClickListener {
                    Settings[key] = null
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
                        (recycler.adapter as IconsAdapter).search(s.toString())
                    }
                })
                setPadding(dp(18).toInt(), 0, dp(18).toInt(), 0)
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, dp(64).toInt())
                setBackgroundColor(0xdd111213.toInt())
                visibility = View.GONE
                searchBar = this
            })
            addView(recycler, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
            orientation = LinearLayout.VERTICAL
        })
        window.decorView.setBackgroundColor(0x55111213)
        window.statusBarColor = 0xdd111213.toInt()

        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory("com.anddoes.launcher.THEME")
        val pacslist = packageManager.queryIntentActivities(mainIntent, 0)
        for (i in pacslist.indices) {
            iconPacks.add(App(pacslist[i].activityInfo.packageName, pacslist[i].activityInfo.name, label = pacslist[i].loadLabel(packageManager).toString()))
            iconPacks[i].icon = Graphics.tryAnimate(this, Icons.applyInsets(Icons.generateAdaptiveIcon(pacslist[i].loadIcon(packageManager))))
        }

        recycler.layoutManager = linearLayoutManager
        recycler.adapter = IconpacksAdapter(iconPacks, ::onSelectIconPack)
    }

    override fun onBackPressed() {
        when (state) {
            0 -> super.onBackPressed()
            1 -> {
                state = 0
                recycler.layoutManager = linearLayoutManager
                searchBar.setText("")
                recycler.adapter = IconpacksAdapter(iconPacks, ::onSelectIconPack)
                defaultOption.visibility = View.VISIBLE
                searchBar.visibility = View.GONE
            }
        }
    }

    private fun onSelectIconPack(packageName: String) {
        thread(isDaemon = true) {
            try {
                val res = packageManager.getResourcesForApplication(packageName)
                val rn = IconTheming.getResourceNames(res, packageName)
                runOnUiThread {
                    try {
                        recycler.layoutManager = gridLayoutManager
                        recycler.adapter = IconsAdapter(packageName, rn, res, ::onSelectIcon)
                        state = 1
                        defaultOption.visibility = View.GONE
                        searchBar.visibility = View.VISIBLE
                    } catch (e: Exception) { e.printStackTrace() }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                recycler.adapter = null
            }
        }
    }

    private fun onSelectIcon(value: String) {
        Settings[key] = value
        Global.shouldSetApps = true
        finish()
    }
}