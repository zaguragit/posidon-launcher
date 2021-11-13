package posidon.launcher.items.users

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.os.UserHandle
import io.posidon.android.launcherutils.appLoading.AppLoader
import io.posidon.android.launcherutils.appLoading.SimpleAppCollection
import posidon.launcher.items.App
import posidon.launcher.storage.Settings
import posidon.launcher.tools.theme.Customizer
import posidon.launcher.tools.theme.Icons
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

class AppCollection(val size: Int) : SimpleAppCollection() {

    var list = ArrayList<App>()
    val sections = ArrayList<ArrayList<App>>()
    var hidden = ArrayList<App>()
    var byName = HashMap<String, ArrayList<App>>()

    private fun putInSecondMap(app: App) {
        val list = byName[app.packageName]
        if (list == null) {
            byName[app.packageName] = arrayListOf(app)
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

        if (!extra.isUserRunning) {
            icon.convertToGrayscale()
        }

        putInSecondMap(app)
        if (Settings["app:$app:hidden", false]) {
            hidden.add(app)
        } else {
            list.add(app)
        }
    }

    override fun finalize(context: Context) {

        if (Settings["drawer:sorting", 0] == 1) list.sortBy { it.hsl[0] }
        else list.sortWith { o1, o2 ->
            o1.label.compareTo(o2.label, ignoreCase = true)
        }

        var currentChar = list[0].label[0].uppercaseChar()
        var currentSection = ArrayList<App>().also { sections.add(it) }
        for (app in list) {
            if (app.label.startsWith(currentChar, ignoreCase = true)) {
                currentSection.add(app)
            }
            else currentSection = ArrayList<App>().apply {
                add(app)
                sections.add(this)
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

    fun Drawable.convertToGrayscale() {
        colorFilter = ColorMatrixColorFilter(ColorMatrix().apply {
            setSaturation(0f)
        })
    }
}