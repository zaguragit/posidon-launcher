package posidon.launcher.items

import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.LauncherApps.ShortcutQuery
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.android.material.bottomsheet.BottomSheetDialog
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.toBitmap
import posidon.launcher.tools.vibrate
import java.util.*
import kotlin.collections.ArrayList

class App(
    val packageName: String,
    val name: String? = null,
    val userHandle: UserHandle = Process.myUserHandle()
) : LauncherItem() {

    var notificationCount = 0

    inline fun open(context: Context, view: View) {
        try {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.component = ComponentName(packageName, name!!)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            Main.launcherApps.startMainActivity(ComponentName(packageName, name), userHandle, null, when (Settings["anim:app_open", "posidon"]) {
                "scale_up" -> ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.measuredWidth, view.measuredHeight).toBundle()
                "clip_reveal" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        ActivityOptions.makeClipRevealAnimation(view, 0, 0, view.measuredWidth, view.measuredHeight).toBundle()
                    else ActivityOptions.makeCustomAnimation(context, R.anim.appopen, R.anim.home_exit).toBundle()
                }
                else -> ActivityOptions.makeCustomAnimation(context, R.anim.appopen, R.anim.home_exit).toBundle()
            })
        } catch (e: Exception) { e.printStackTrace() }
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    fun getShortcuts(context: Context): List<ShortcutInfo>? {
        val shortcutQuery = ShortcutQuery()
        shortcutQuery.setQueryFlags(ShortcutQuery.FLAG_MATCH_DYNAMIC or ShortcutQuery.FLAG_MATCH_MANIFEST or ShortcutQuery.FLAG_MATCH_PINNED)
        shortcutQuery.setPackage(packageName)
        return try {
            (context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps).getShortcuts(shortcutQuery, Process.myUserHandle())
        } catch (e: Exception) { emptyList() }
    }

    fun showProperties(context: Context, appcolor: Int) {
        val d = BottomSheetDialog(context, R.style.bottomsheet)
        d.setContentView(R.layout.app_properties)
        val g = context.getDrawable(R.drawable.bottom_sheet) as GradientDrawable
        g.setColor(appcolor)
        d.window!!.findViewById<View>(R.id.design_bottom_sheet).background = g
        d.findViewById<TextView>(R.id.appname)!!.text = label
        d.findViewById<ImageView>(R.id.iconimg)!!.setImageBitmap(icon!!.toBitmap())
        try {
            d.findViewById<TextView>(R.id.version)!!.text = context.packageManager.getPackageInfo(packageName, 0).versionName
        } catch (ignored: PackageManager.NameNotFoundException) {}
        if (Settings["dev:show_app_component", false]) {
            d.findViewById<TextView>(R.id.componentname)!!.text = "$packageName/$name"
            d.findViewById<View>(R.id.component)!!.visibility = View.VISIBLE
        }
        d.findViewById<View>(R.id.openinsettings)!!.setOnClickListener {
            d.dismiss()
            val i = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            i.data = Uri.parse("package:$packageName")
            context.startActivity(i, ActivityOptions.makeCustomAnimation(context, R.anim.slideup, R.anim.home_exit).toBundle())
        }
        d.findViewById<View>(R.id.uninstallbtn)!!.setOnClickListener {
            context.vibrate()
            d.dismiss()
            val uninstallIntent = Intent("android.intent.action.DELETE")
            uninstallIntent.data = Uri.parse("package:$packageName")
            context.startActivity(uninstallIntent)
        }
        d.show()
    }

    override fun toString() = "$packageName/$name/${userHandle.hashCode()}"

    inline fun isInstalled(packageManager: PackageManager) = Tools.isInstalled(packageName, packageManager)

    companion object {
        private var appsByName = HashMap<String, ArrayList<App>>()
        private var appsByName2 = HashMap<String, ArrayList<App>>()
        val hidden = ArrayList<App>()

        operator fun get(component: String): App? {
            val a = component.split('/')
            val list = appsByName[a[0]] ?: return null
            return if (a.size == 3) list.find {
                it.name == a[1] && it.userHandle.hashCode() == a[2].toInt()
            } else list.find {
                it.name == a[1]
            }
        }

        operator fun get(packageName: String, name: String): App? {
            val list = appsByName[packageName] ?: return null
            return list.find { it.name == name }
        }

        operator fun get(packageName: String, name: String, userId: Int): App? {
            val list = appsByName[packageName] ?: return null
            return list.find { it.name == name && it.userHandle.hashCode() == userId }
        }

        fun getJustPackage(packageName: String): ArrayList<App>? = appsByName[packageName]

        fun putInSecondMap(app: App) {
            val list = appsByName2[app.packageName]
            if (list == null) {
                appsByName2[app.packageName] = arrayListOf(app)
                return
            }
            val thisAppI = list.indexOfFirst {
                it.name == app.name && it.userHandle.hashCode() == app.userHandle.hashCode()
            }
            if (thisAppI == -1) {
                list.add(app)
                return
            }
            list[thisAppI] = app
        }

        fun swapMaps() {
            val tmp = appsByName
            appsByName = appsByName2
            appsByName2 = tmp
        }

        fun clearSecondMap() = appsByName2.clear()

        fun removePackage(packageName: String) {
            hidden.removeAll { it.packageName == packageName }
            appsByName.remove(packageName)
        }
    }
}