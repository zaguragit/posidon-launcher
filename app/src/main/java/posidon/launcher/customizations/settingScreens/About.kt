package posidon.launcher.customizations.settingScreens

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import posidon.android.conveniencelib.dp
import posidon.android.loader.text.TextLoader
import posidon.launcher.BuildConfig
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.drawable.FastColorDrawable
import posidon.launcher.storage.Settings
import posidon.launcher.tools.*
import posidon.launcher.tools.theme.applyFontSetting
import posidon.launcher.view.recycler.LinearLayoutManager
import java.net.URL

class About : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_about)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) window.setDecorFitsSystemWindows(false)
        else window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)
        window.setBackgroundDrawable(FastColorDrawable(Global.getBlackAccent()))
        val description = findViewById<TextView>(R.id.appname)
        description.text = getString(R.string.app_name) + " - " + BuildConfig.VERSION_NAME
        TextLoader.load("https://posidon.io/launcher/contributors/pictureUrls") {
            var leoLink: String? = null
            var sajidShaikLink: String? = null
            for (line in it.split('\n')) {
                if (line.startsWith("Leo: "))
                    leoLink = line.substring(5)
                else if (line.startsWith("SajidShaik: "))
                    sajidShaikLink = line.substring(12)
            }
            leoLink?.let { link ->
                ImageLoader.loadBitmap(link) {
                    img -> runOnUiThread { findViewById<ImageView>(R.id.leoProfile).setImageBitmap(img) }
                }
            }
            sajidShaikLink?.let { link ->
                ImageLoader.loadBitmap(link) {
                    img -> runOnUiThread { findViewById<ImageView>(R.id.sajidShaikProfile).setImageBitmap(img) }
                }
            }
        }

        findViewById<View>(R.id.maincard).setOnLongClickListener {
            if (Settings["dev:enabled", false]) {
                Settings["dev:enabled"] = false
                Toast.makeText(this@About, "Developer mode disabled", Toast.LENGTH_SHORT).show()
            } else {
                Settings["dev:enabled"] = true
                Toast.makeText(this@About, "Developer mode enabled", Toast.LENGTH_SHORT).show()
            }
            true
        }

        try { findViewById<ImageView>(R.id.img).setImageResource(R.drawable.logo_wide) } catch (ignore: Exception) {}

        val contributorList = findViewById<RecyclerView>(R.id.contributorList).apply {
            layoutManager = LinearLayoutManager(this@About)
            isNestedScrollingEnabled = false
        }

        TextLoader.load("https://api.github.com/repos/lposidon/posidonLauncher/contributors") {
            val array = JSONArray(it)
            val contributors = ArrayList<Contributor>()
            for (i in 0 until array.length()) {
                val c = array.getJSONObject(i)
                val name = c.getString("login")
                if (name == "leoxshn") continue
                contributors.add(Contributor(
                    name,
                    BitmapFactory.decodeStream(URL(c.getString("avatar_url")).openStream()),
                    c.getString("html_url")
                ))
            }
            runOnUiThread {
                contributorList.adapter = ListAdapter(this, contributors)
                findViewById<View>(R.id.title).visibility = View.VISIBLE
            }
        }
    }

    fun openTwitter(v: View) {
        val uri = Uri.parse("https://twitter.com/posidon")
        val i = Intent(Intent.ACTION_VIEW, uri)
        startActivity(i, ActivityOptions.makeCustomAnimation(this, R.anim.slideup, R.anim.slidedown).toBundle())
    }

    fun openTelegram(v: View) { try {
        val uri = Uri.parse("https://t.me/posidonlauncher")
        val i = Intent(Intent.ACTION_VIEW, uri)
        startActivity(i, ActivityOptions.makeCustomAnimation(this, R.anim.slideup, R.anim.slidedown).toBundle())
    } catch (ignore: Exception) {} }

    fun openGitHub(v: View) { try {
        val uri = Uri.parse("https://github.com/lposidon/posidonLauncher")
        val i = Intent(Intent.ACTION_VIEW, uri)
        startActivity(i, ActivityOptions.makeCustomAnimation(this, R.anim.slideup, R.anim.slidedown).toBundle())
    } catch (ignore: Exception) {} }

    fun openWebsite(v: View) {
        val uri = Uri.parse("https://posidon.io/launcher")
        val i = Intent(Intent.ACTION_VIEW, uri)
        startActivity(i, ActivityOptions.makeCustomAnimation(this, R.anim.slideup, R.anim.slidedown).toBundle())
    }

    class Contributor(
        val name: String,
        val icon: Bitmap,
        val url: String
    )

    class ListAdapter(
        private val context: Context,
        private val contributors: ArrayList<Contributor>
    ) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

        class ViewHolder(
            val view: View,
            var icon: ImageView,
            var text: TextView
        ) : RecyclerView.ViewHolder(view)

        override fun getItemCount() = contributors.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
            return ViewHolder(v,
                v.findViewById<ImageView>(R.id.iconimg).apply {
                    val p = context.dp(8).toInt()
                    setPadding(p, p, p, p)
                },
                v.findViewById(R.id.icontxt)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, i: Int) {

            val contributor = contributors[i]
            holder.icon.setImageBitmap(contributor.icon)
            holder.text.text = contributor.name

            holder.view.setOnClickListener {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(contributor.url)),
                    ActivityOptions.makeCustomAnimation(context, R.anim.slideup, R.anim.slidedown).toBundle()
                )
            }
        }
    }
}