package posidon.launcher.external

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.os.Parcelable
import android.view.View
import posidon.launcher.Main
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.view.ResizableLayout

object WidgetManager {

    const val REQUEST_PICK_APPWIDGET = 0
    const val REQUEST_CREATE_APPWIDGET = 1
    const val REQUEST_BIND_WIDGET = 2

    private var hostView: AppWidgetHostView? = null
    lateinit var host: AppWidgetHost
        private set

    fun init() {
        host = AppWidgetHost(Tools.publicContext, 0xe1d9e15)
    }

    fun fromIntent(widgetLayout: ResizableLayout, data: Intent?) {
        widgetLayout.visibility = View.VISIBLE
        val widgetManager = AppWidgetManager.getInstance(Tools.publicContext)
        try {
            host.deleteAppWidgetId(hostView!!.appWidgetId)
            widgetLayout.removeView(hostView)
        } catch (e: Exception) { e.printStackTrace() }
        try {
            val id = data!!.extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            val providerInfo = widgetManager.getAppWidgetInfo(id)
            hostView = host.createView(Tools.publicContext!!.applicationContext, id, providerInfo)
            widgetLayout.addView(hostView)
            if (!widgetManager.bindAppWidgetIdIfAllowed(id, providerInfo.provider)) {
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, providerInfo.provider)
                Main.instance.startActivityForResult(intent, REQUEST_BIND_WIDGET)
            }
            Settings["widget"] = providerInfo.provider.packageName + "/" + providerInfo.provider.className + "/" + id
            widgetLayout.addView(hostView)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun fromSettings(widgetLayout: ResizableLayout) {
        val widgetManager = AppWidgetManager.getInstance(Tools.publicContext)
        val str = Settings["widget", "posidon.launcher/posidon.launcher.external.widgets.ClockWidget"]
        if (str.isNotEmpty()) {
            val s = str.split("/").toTypedArray()
            val packageName = s[0]
            val className: String = try { s[1] } catch (ignore: ArrayIndexOutOfBoundsException) { return }
            var providerInfo: AppWidgetProviderInfo? = null
            val appWidgetInfos = widgetManager.installedProviders
            var widgetIsFound = false
            for (j in appWidgetInfos.indices) {
                if (appWidgetInfos[j].provider.packageName == packageName && appWidgetInfos[j].provider.className == className) {
                    providerInfo = appWidgetInfos[j]
                    widgetIsFound = true
                    break
                }
            }
            if (!widgetIsFound) return
            var id: Int
            try { id = s[2].toInt() }
            catch (e: ArrayIndexOutOfBoundsException) {
                id = host.allocateAppWidgetId()
                if (!widgetManager.bindAppWidgetIdIfAllowed(id, providerInfo!!.provider)) { // Request permission - https://stackoverflow.com/a/44351320/1816603
                    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, providerInfo.provider)
                    Main.instance.startActivityForResult(intent, REQUEST_BIND_WIDGET)
                }
            }
            hostView = host.createView(Tools.publicContext!!.applicationContext, id, providerInfo)
            hostView!!.setAppWidget(id, providerInfo)
            widgetLayout.addView(hostView)
        } else widgetLayout.visibility = View.GONE
    }

    fun selectWidget() {
        val appWidgetId = host.allocateAppWidgetId()
        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        val customInfo: ArrayList<out Parcelable?> = ArrayList()
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo)
        val customExtras: ArrayList<out Parcelable?> = ArrayList()
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras)
        Main.instance.startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET)
    }

    fun deleteWidget(widgetLayout: ResizableLayout) {
        host.deleteAppWidgetId(hostView!!.appWidgetId)
        widgetLayout.removeView(hostView)
        hostView = null
        widgetLayout.visibility = View.GONE
        Settings["widget"] = ""
    }
}