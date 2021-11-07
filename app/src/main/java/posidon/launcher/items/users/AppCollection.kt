package posidon.launcher.items.users

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.UserHandle
import androidx.palette.graphics.Palette
import io.posidon.android.launcherutils.appLoading.AppLoader
import io.posidon.android.launcherutils.appLoading.SimpleAppCollection
import posidon.android.conveniencelib.toBitmap
import posidon.launcher.items.App
import posidon.launcher.storage.Settings
import posidon.launcher.tools.theme.Customizer
import posidon.launcher.tools.theme.Icons
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.arrayListOf
import kotlin.collections.indexOfFirst
import kotlin.collections.set
import kotlin.collections.sortWith

class AppCollection(val size: Int) : SimpleAppCollection() {

    var tmpApps = ArrayList<App>()
    val tmpAppSections = ArrayList<ArrayList<App>>()
    var tmpHidden = ArrayList<App>()
    var appsByName = HashMap<String, ArrayList<App>>()

    private fun putInSecondMap(app: App) {
        val list = appsByName[app.packageName]
        if (list == null) {
            appsByName[app.packageName] = arrayListOf(app)
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

    override fun addApp(
        context: Context,
        packageName: String,
        name: String,
        profile: UserHandle,
        label: String,
        icon: Drawable,
        extra: AppLoader.ExtraAppInfo<Nothing?>
    ) {
        val lk = "$packageName/$name?label"
        val label = Settings[lk, label].let {
            if (it.isEmpty()) {
                Settings[lk] = null
                label
            } else it
        }

        val app = App(packageName, name, profile, label).apply {
            this.icon = icon
            Icons.animateIfShould(context, icon)
        }

        putInSecondMap(app)
        if (Settings["app:$app:hidden", false]) {
            tmpHidden.add(app)
        } else {
            tmpApps.add(app)
        }
    }

    override fun finalize(context: Context) {

        if (Settings["drawer:sorting", 0] == 1) tmpApps.sortWith { o1, o2 ->
            val iHsv = floatArrayOf(0f, 0f, 0f)
            val jHsv = floatArrayOf(0f, 0f, 0f)
            Color.colorToHSV(Palette.from(o1.icon!!.toBitmap()).generate().getVibrantColor(0xff252627.toInt()), iHsv)
            Color.colorToHSV(Palette.from(o2.icon!!.toBitmap()).generate().getVibrantColor(0xff252627.toInt()), jHsv)
            iHsv[0].compareTo(jHsv[0])
        }
        else tmpApps.sortWith { o1, o2 ->
            o1.label.compareTo(o2.label, ignoreCase = true)
        }

        var currentChar = tmpApps[0].label[0].uppercaseChar()
        var currentSection = ArrayList<App>().also { tmpAppSections.add(it) }
        for (app in tmpApps) {
            if (app.label.startsWith(currentChar, ignoreCase = true)) {
                currentSection.add(app)
            }
            else currentSection = ArrayList<App>().apply {
                add(app)
                tmpAppSections.add(this)
                currentChar = app.label[0].uppercaseChar()
            }
        }
    }

    override fun modifyIcon(
        icon: Drawable,
        packageName: String,
        name: String,
        profile: UserHandle,
    ): Drawable {
        var icon = Customizer.getCustomIcon("app:$packageName/$name/${profile.hashCode()}:icon") ?: icon
        icon = Icons.generateAdaptiveIcon(icon)
        icon = Icons.applyInsets(icon)
        return icon
    }
}