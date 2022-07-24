package posidon.launcher.customizations

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.luminance
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.conveniencelib.getStatusBarHeight
import io.posidon.android.conveniencelib.units.dp
import io.posidon.android.conveniencelib.units.toPixels
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.drawable.FastColorDrawable
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.view.recycler.LinearLayoutManager

class RemovedArticles : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@RemovedArticles)
            adapter = Adapter(this@RemovedArticles)
            setPadding(0, getStatusBarHeight(), 0, Tools.navbarHeight)
        })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) window.setDecorFitsSystemWindows(false)
        else window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.setBackgroundDrawable(FastColorDrawable(Global.getBlackAccent()))
    }

    class Adapter(
        val context: Context
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        class ViewHolder(layout: View, val label: TextView, val button: View) : RecyclerView.ViewHolder(layout)

        private val removedList = Settings.getStringsOrSetEmpty("feed:deleted_articles")

        override fun getItemCount() = removedList.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val label = TextView(context).apply {
                isSingleLine = true
                maxLines = 1
                includeFontPadding = false
                textSize = 18f
                setTextColor(context.resources.getColor(R.color.cardtxt))
            }
            val button = TextView(context).apply {
                text = context.getString(R.string.restore)
                textSize = 15f
                setTextColor(if (Global.accentColor.luminance > 6f) 0xff000000.toInt() else 0xffffffff.toInt())
                background = context.getDrawable(R.drawable.button_bg_round)
                backgroundTintList = ColorStateList.valueOf(Global.accentColor)
                val h = 20.dp.toPixels(context)
                val v = 8.dp.toPixels(context)
                setPadding(h, v, h, v)
                includeFontPadding = false
            }
            val layout = LinearLayout(context).apply {
                addView(label)
                addView(button)
                gravity = Gravity.CENTER_VERTICAL
                val h = 12.dp.toPixels(context)
                val v = 6.dp.toPixels(context)
                setPadding(h, v, h, v)
            }
            (label.layoutParams as LinearLayout.LayoutParams).apply {
                width = MATCH_PARENT
                weight = 1f
                marginEnd = 12.dp.toPixels(context)
            }
            (button.layoutParams as LinearLayout.LayoutParams).apply {
                width = WRAP_CONTENT
                weight = 0f
            }
            return ViewHolder(layout, label, button)
        }

        override fun onBindViewHolder(holder: ViewHolder, i: Int) {
            val str = removedList[i]
            holder.button.setOnClickListener {
                removedList.remove(str)
                notifyItemRemoved(i)
                notifyItemRangeChanged(i, removedList.size - i)
                Settings.apply()
            }
            val a = str.indexOf(':', str.indexOf(':') + 1)
            val b = str.indexOf(':', a + 1)
            holder.label.text = str.substring(if (b == -1) a + 1 else b + 1)
        }
    }
}