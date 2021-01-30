package posidon.launcher.external.quickstep

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.dp
import posidon.launcher.view.LinearLayoutManager
import kotlin.math.max

@RequiresApi(Build.VERSION_CODES.Q)
class QuickStepActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.quickstep)
        if (Settings["mnmlstatus", false]) window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LOW_PROFILE
        else window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.setBackgroundDrawable(ColorDrawable(0xdd000000.toInt()))

        val taskDescription = findViewById<LinearLayout>(R.id.taskDescription)
        val recycler = findViewById<RecyclerView>(R.id.recycler)

        (taskDescription.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = max(Tools.navbarHeight, 24.dp.toInt()) + 32.dp.toInt()

        if (QuickStepService.recentTasks.isEmpty()) {
            taskDescription.visibility = GONE
        } else {
            setTaskI(0)
            for (i in 1 until QuickStepService.recentTasks.size) {
                println(QuickStepService.recentTasks[i].toString())
            }
        }

        recycler.run {
            layoutManager = LinearLayoutManager(this@QuickStepActivity)
            adapter = QuickStepAdapter()
        }
    }

    fun setTaskI(i: Int) {
        val icon = findViewById<ImageView>(R.id.icon)
        val label = findViewById<TextView>(R.id.label)
        val info = packageManager.resolveActivity(QuickStepService.recentTasks[i]!!.baseIntent, 0)!!
        icon.setImageDrawable(App[info.resolvePackageName, info.activityInfo.parentActivityName]?.icon ?: info.loadIcon(packageManager))
        label.text = info.loadLabel(packageManager)
    }

    init { INSTANCE = this }
    companion object { var INSTANCE: QuickStepActivity? = null }
}