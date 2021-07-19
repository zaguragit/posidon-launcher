package posidon.launcher.items.users

import android.app.ActivityOptions
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import posidon.launcher.R
import posidon.launcher.items.users.ShortcutAdapter.ShortcutViewHolder

class ShortcutAdapter(
    private val context: Context,
    private val shortcuts: List<ShortcutInfo>,
    private val txtColor: Int
) : RecyclerView.Adapter<ShortcutViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShortcutViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.shortcut, parent, false)
        return ShortcutViewHolder(v)
    }

    class ShortcutViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onBindViewHolder(holder: ShortcutViewHolder, position: Int) {
        val txt = holder.view.findViewById<TextView>(R.id.icontxt)
        txt.text = shortcuts[position].shortLabel
        txt.setTextColor(txtColor)
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        holder.view.findViewById<ImageView>(R.id.iconimg).setImageDrawable(launcherApps.getShortcutIconDrawable(shortcuts[position], context.resources.displayMetrics.densityDpi))
        holder.view.setOnClickListener {
            ItemLongPress.currentPopup?.dismiss()
            launcherApps.startShortcut(shortcuts[position], null, ActivityOptions.makeCustomAnimation(context, R.anim.appopen, R.anim.home_exit).toBundle())
        }
    }

    override fun getItemCount(): Int = shortcuts.size

}