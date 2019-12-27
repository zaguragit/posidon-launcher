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
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.android.material.bottomsheet.BottomSheetDialog
import posidon.launcher.R
import posidon.launcher.tools.Settings
import posidon.launcher.tools.Tools
import java.util.*

class App : LauncherItem() {
    @JvmField
    var name: String? = null
    @JvmField
    var packageName: String? = null
    fun open(context: Context, view: View) {
        try {
            val launchintent = Intent(Intent.ACTION_MAIN)
            launchintent.component = ComponentName(packageName!!, name!!)
            launchintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            when (Settings.getString("anim:app_open", "posidon")) {
                "scale_up" -> context.startActivity(launchintent, ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.measuredWidth, view.measuredHeight).toBundle())
                "clip_reveal" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        context.startActivity(launchintent, ActivityOptions.makeClipRevealAnimation(view, 0, 0, view.measuredWidth, view.measuredHeight).toBundle())
                    else context.startActivity(launchintent, ActivityOptions.makeCustomAnimation(context, R.anim.appopen, R.anim.home_exit).toBundle())
                }
                else -> context.startActivity(launchintent, ActivityOptions.makeCustomAnimation(context, R.anim.appopen, R.anim.home_exit).toBundle())
            }
        } catch (ignore: Exception) {}
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    fun getShortcuts(context: Context): List<ShortcutInfo>? {
        val shortcutQuery = ShortcutQuery()
        shortcutQuery.setQueryFlags(ShortcutQuery.FLAG_MATCH_DYNAMIC or ShortcutQuery.FLAG_MATCH_MANIFEST or ShortcutQuery.FLAG_MATCH_PINNED)
        shortcutQuery.setPackage(packageName)
        return try {
            (Objects.requireNonNull(context.getSystemService(Context.LAUNCHER_APPS_SERVICE)) as LauncherApps).getShortcuts(shortcutQuery, Process.myUserHandle())
        } catch (e: Exception) { emptyList() }
    }

    fun showProperties(context: Context, appcolor: Int) {
        val d = BottomSheetDialog(context, R.style.bottomsheet)
        d.setContentView(R.layout.app_properties)
        val g = context.getDrawable(R.drawable.bottom_sheet) as GradientDrawable
        g.setColor(appcolor)
        d.window!!.findViewById<View>(R.id.design_bottom_sheet).background = g
        d.findViewById<TextView>(R.id.appname)!!.text = label
        d.findViewById<ImageView>(R.id.iconimg)!!.setImageBitmap(Tools.drawable2bitmap(icon))
        try {
            d.findViewById<TextView>(R.id.version)!!.text = context.packageManager.getPackageInfo(packageName, 0).versionName
        } catch (ignored: PackageManager.NameNotFoundException) {}
        if (Settings.getBool("showcomponent", false)) {
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
            Tools.vibrate(context)
            d.dismiss()
            val uninstallIntent = Intent("android.intent.action.DELETE")
            uninstallIntent.data = Uri.parse("package:$packageName")
            context.startActivity(uninstallIntent)
        }
        /*context.findViewById(R.id.drawergrid).animate().scaleX(0.9f).scaleY(0.9f).setInterpolator(new PathInterpolator(0.245f, 1.275f, 0.405f, 1.005f)).setDuration(300L);
        context.findViewById(R.id.desktop).animate().scaleX(0.9f).scaleY(0.9f).setInterpolator(new PathInterpolator(0.245f, 1.275f, 0.405f, 1.005f)).setDuration(300L);
        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                context.findViewById(R.id.drawergrid).animate().scaleX(1).scaleY(1).setDuration(300L);
                context.findViewById(R.id.desktop).animate().scaleX(1).scaleY(1).setDuration(300L);
                context.getWindow().setNavigationBarColor(0x0);
            }
        });*/d.show()
    }

    override fun toString(): String {
        return "$packageName/$name"
    }

    companion object {
        private var appsByName = HashMap<String, App>()
        private var appsByName2 = HashMap<String, App>()
        @JvmField
        val hidden = ArrayList<App>()
        @JvmStatic
        operator fun get(component: String?): App? {
            return appsByName[component]
        }

        @JvmStatic
        fun putInSecondMap(component: String, app: App) {
            appsByName2[component] = app
        }

        @JvmStatic
        fun swapMaps() {
            val tmp = appsByName
            appsByName = appsByName2
            appsByName2 = tmp
        }

        @JvmStatic
        fun clearSecondMap() {
            appsByName2.clear()
        }
    }
}