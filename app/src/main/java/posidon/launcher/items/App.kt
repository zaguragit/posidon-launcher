package posidon.launcher.items

import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.LauncherApps.ShortcutQuery
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.palette.graphics.Palette
import com.google.android.material.bottomsheet.BottomSheetDialog
import posidon.launcher.Global
import posidon.launcher.Home
import posidon.launcher.R
import posidon.launcher.items.users.CustomAppIcon
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.open
import posidon.launcher.tools.toBitmap
import posidon.launcher.tools.vibrate
import java.util.*
import kotlin.collections.ArrayList

class App(
    val packageName: String,
    val name: String? = null,
    val userHandle: UserHandle = Process.myUserHandle()
) : LauncherItem() {

    override var notificationCount = 0

    inline fun open(context: Context, view: View?) {
        try {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.component = ComponentName(packageName, name!!)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            (context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps).startMainActivity(ComponentName(packageName, name), userHandle, null, when (Settings["anim:app_open", "posidon"]) {
                "scale_up" -> ActivityOptions.makeScaleUpAnimation(view, 0, 0, view?.measuredWidth ?: 0, view?.measuredHeight ?: 0).toBundle()
                "clip_reveal" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        ActivityOptions.makeClipRevealAnimation(view, 0, 0, view?.measuredWidth ?: 0, view?.measuredHeight ?: 0).toBundle()
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
        val appName = d.findViewById<EditText>(R.id.appname)!!.apply {
            setText(label)
        }
        d.findViewById<View>(R.id.iconFrame)!!.run {
            findViewById<ImageView>(R.id.iconimg)!!.run {
                setImageBitmap(icon!!.toBitmap())
            }
            Palette.from(icon!!.toBitmap()).generate {
                if (it != null)
                    findViewById<View>(R.id.edit)!!.backgroundTintList = ColorStateList.valueOf(it.getDarkVibrantColor(it.getDarkMutedColor(0xff1155ff.toInt())))
            }
            setOnClickListener {
                val intent = Intent(context, CustomAppIcon::class.java)
                intent.putExtra("key", "app:${this@App}:icon")
                context.startActivity(intent)
                d.dismiss()
            }
        }
        try {
            d.findViewById<TextView>(R.id.version)!!.text = context.packageManager.getPackageInfo(packageName, 0).versionName
        } catch (ignored: PackageManager.NameNotFoundException) {}
        if (Settings["dev:show_app_component", false]) {
            d.findViewById<TextView>(R.id.componentname)!!.text = "$packageName/$name"
            d.findViewById<View>(R.id.component)!!.visibility = View.VISIBLE
        }
        d.findViewById<View>(R.id.openinsettings)!!.setOnClickListener {
            d.dismiss()
            context.open(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, ActivityOptions.makeCustomAnimation(context, R.anim.slideup, R.anim.home_exit).toBundle()) {
                data = Uri.parse("package:$packageName")
            }
        }
        d.findViewById<View>(R.id.uninstallbtn)!!.setOnClickListener {
            context.vibrate()
            d.dismiss()
            val uninstallIntent = Intent("android.intent.action.DELETE")
            uninstallIntent.data = Uri.parse("package:$packageName")
            context.startActivity(uninstallIntent)
        }
        d.setOnDismissListener {
            Settings["$packageName/$name?label"] = appName.text.toString().replace('\t', ' ')
            Global.shouldSetApps = true
            Home.instance.setDock()
        }
        d.show()
    }

    override fun getColor(): Int {
        val palette = Palette.from(icon!!.toBitmap()).generate()
        val def = -0xdad9d9
        var color = palette.getDominantColor(def)
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        if (hsv[1] < 0.2f) {
            color = palette.getVibrantColor(def)
        }
        return color
    }

    override fun toString() = "$packageName/$name/${userHandle.hashCode()}"

    inline fun isInstalled(packageManager: PackageManager) = Tools.isInstalled(packageName, packageManager)

    companion object {
        private var appsByName = HashMap<String, ArrayList<App>>()
        var hidden = ArrayList<App>()
            private set

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

        fun removePackage(packageName: String) {
            hidden.removeAll { it.packageName == packageName }
            appsByName.remove(packageName)
        }

        fun setMapAndClearLast(appsByName: HashMap<String, ArrayList<App>>) {
            val tmp = this.appsByName
            this.appsByName = appsByName
            tmp.clear()
        }

        fun onFinishLoad(tmpApps: ArrayList<App>, tmpAppSections: ArrayList<ArrayList<App>>, tmpHidden: ArrayList<App>, appsByName: HashMap<String, ArrayList<App>>) {
            run {
                val tmp = Global.apps
                Global.apps = tmpApps
                tmp.clear()
            }
            run {
                val tmp = Global.appSections
                Global.appSections = tmpAppSections
                tmp.clear()
            }
            hidden = tmpHidden
            setMapAndClearLast(appsByName)
        }
    }
}