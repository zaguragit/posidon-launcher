package posidon.launcher.external.quickstep

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import posidon.launcher.tools.Tools
import posidon.launcher.tools.dp

class QuickStepAdapter : RecyclerView.Adapter<QuickStepAdapter.ViewHolder>() {

    class ViewHolder(val card: View, val text: TextView) : RecyclerView.ViewHolder(card)

    override fun getItemCount() = QuickStepService.recentTasks.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = Tools.publicContext!!
        val text = TextView(context).apply {
            setTextColor(0xffffffff.toInt())
            textSize = 18f
        }
        val card = CardView(context).apply {
            addView(text)
            layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                leftMargin = 24.dp.toInt()
                rightMargin = 24.dp.toInt()
                topMargin = 10.dp.toInt()
                bottomMargin = 10.dp.toInt()
            }
            setCardBackgroundColor(0xff252627.toInt())
            radius = 25.dp
        }
        return ViewHolder(card, text)
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        val tasks = QuickStepService.recentTasks
        val context = Tools.publicContext!!
        holder.text.run {
            val info = context.packageManager.resolveActivity(tasks[i]!!.baseIntent, 0)!!
            //icon.setImageDrawable(App[info.let { "${it.resolvePackageName}/${it.activityInfo.parentActivityName}" }]?.icon ?: info.loadIcon(packageManager))
            text = info.loadLabel(context.packageManager)
        }
        holder.card.run {
            setOnClickListener {
                context.startActivity(tasks[i]!!.baseIntent)
            }
        }
    }
}