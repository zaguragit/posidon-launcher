package posidon.launcher.external

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import posidon.launcher.tools.Settings

object WidgetManager {

    const val REQUEST_PICK_APPWIDGET = 0
    const val REQUEST_CREATE_APPWIDGET = 1
    const val REQUEST_BIND_WIDGET = 2

    private lateinit var hostView: AppWidgetHostView
    private lateinit var widgetHost: AppWidgetHost
    private lateinit var context: Context

    fun init(context: Context) {
        widgetHost = AppWidgetHost(context, 0xe1d9e15)
        this.context = context
    }

    fun fromIntent(data: Intent?): AppWidgetHostView? {
        val widgetManager = AppWidgetManager.getInstance(context)
        try {
            widgetHost.deleteAppWidgetId(hostView.appWidgetId)
        } catch (e: Exception) { e.printStackTrace() }
        try {
            val id = data!!.extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            val providerInfo = widgetManager.getAppWidgetInfo(id)
            val hostView = widgetHost.createView(context.applicationContext, id, providerInfo)
            hostView.isLongClickable = false
            if (!widgetManager.bindAppWidgetIdIfAllowed(id, providerInfo.provider)) {
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, providerInfo.provider)
                //startActivityForResult(intent, REQUEST_BIND_WIDGET)
            }
            Settings["widget"] = providerInfo.provider.packageName + "/" + providerInfo.provider.className + "/" + id
            return hostView
        } catch (e: Exception) { e.printStackTrace() }
        return null
    }

    fun fromSettings(): AppWidgetHostView? {
        val widgetManager = AppWidgetManager.getInstance(context)
        val str = Settings["widget", "posidon.launcher/posidon.launcher.external.widgets.ClockWidget"]
        if (str != "") {
            val s = str.split("/").toTypedArray()
            val packageName = s[0]
            val className: String = try { s[1] }
                        catch (ignore: ArrayIndexOutOfBoundsException) { return null }
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
            if (!widgetIsFound) return null
            var id: Int
            try {
                id = s[2].toInt()
            } catch (e: ArrayIndexOutOfBoundsException) {
                id = widgetHost.allocateAppWidgetId()
                if (!widgetManager.bindAppWidgetIdIfAllowed(id, providerInfo!!.provider)) { // Request permission - https://stackoverflow.com/a/44351320/1816603
                    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, providerInfo.provider)
                    //startActivityForResult(intent, REQUEST_BIND_WIDGET)
                }
            }
            val hostView = widgetHost.createView(context.applicationContext, id, providerInfo)
            hostView.setAppWidget(id, providerInfo)
            return hostView
        }
        return null
    }

    fun selectWidget() {
        val appWidgetId = widgetHost.allocateAppWidgetId()
        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        val customInfo: ArrayList<out Parcelable?> = ArrayList()
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo)
        val customExtras: ArrayList<out Parcelable?> = ArrayList()
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras)
        //startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET)
    }

    fun deleteWidget() {
        widgetHost.deleteAppWidgetId(hostView.appWidgetId)
        Settings["widget"] = ""
    }
}