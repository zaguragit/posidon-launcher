package posidon.launcher.external

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.os.Parcelable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import posidon.launcher.Home
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Device
import posidon.launcher.tools.Tools
import posidon.launcher.view.ResizableLayout
import posidon.launcher.view.feed.WidgetSection

class Widget(
    val hostId: Int
) {

    companion object {
        const val REQUEST_PICK_APPWIDGET = 0
        const val REQUEST_CREATE_APPWIDGET = 1
        const val REQUEST_BIND_WIDGET = 2

        fun fromIntent(activity: Activity, data: Intent?): WidgetSection? {
            val widgetManager = AppWidgetManager.getInstance(Tools.publicContext)
            try {
                val widgetId = data!!.extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                val hostId = Tools.generateWidgetHostUid()
                val providerInfo = widgetManager.getAppWidgetInfo(widgetId)
                if (!widgetManager.bindAppWidgetIdIfAllowed(widgetId, providerInfo.provider)) {
                    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, providerInfo.provider)
                    activity.startActivityForResult(intent, REQUEST_BIND_WIDGET)
                }
                Settings["widget:$hostId"] = providerInfo.provider.packageName + "/" + providerInfo.provider.className + "/" + widgetId
                return WidgetSection(Tools.publicContext!!, Widget(hostId))
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        fun handleActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?): WidgetSection? {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == REQUEST_PICK_APPWIDGET) {
                    val extras = data!!.extras
                    if (extras != null) {
                        val id = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                        val widgetInfo = AppWidgetManager.getInstance(activity).getAppWidgetInfo(id)
                        if (widgetInfo.configure != null) {
                            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                            intent.component = widgetInfo.configure
                            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                            runCatching {
                                activity.startActivityForResult(intent, REQUEST_CREATE_APPWIDGET)
                            }
                        } else return fromIntent(activity, data)
                    } else return fromIntent(activity, data)
                } else if (requestCode == REQUEST_CREATE_APPWIDGET) {
                    return fromIntent(activity, data)
                }
            } else if (resultCode == Activity.RESULT_CANCELED && data != null) {
                val appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                if (appWidgetId != -1) {
                    tmpHost?.deleteAppWidgetId(appWidgetId)
                }
            }
            return null
        }

        private var tmpHost: AppWidgetHost? = null
        fun selectWidget(activity: Activity) {
            val hostId = Tools.generateWidgetHostUid()
            tmpHost = AppWidgetHost(Tools.publicContext, hostId)
            val appWidgetId = tmpHost!!.allocateAppWidgetId()
            val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
            pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            val customInfo: ArrayList<out Parcelable?> = ArrayList()
            pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo)
            val customExtras: ArrayList<out Parcelable?> = ArrayList()
            pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras)
            activity.startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET)
        }
    }

    private var hostView: AppWidgetHostView? = null
    var host: AppWidgetHost = AppWidgetHost(Tools.publicContext, hostId)
        private set

    fun fromSettings(widgetLayout: ResizableLayout): Boolean {
        return try {
            val widgetManager = AppWidgetManager.getInstance(Tools.publicContext)
            val str = Settings["widget:$hostId", "posidon.launcher/posidon.launcher.external.widgets.ClockWidget"]
            val s = str.split("/").toTypedArray()
            val packageName = s[0]
            val className: String = s[1]
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
            if (!widgetIsFound) return false
            var id: Int
            try { id = s[2].toInt() } catch (e: ArrayIndexOutOfBoundsException) {
                id = host.allocateAppWidgetId()
                if (!widgetManager.bindAppWidgetIdIfAllowed(id, providerInfo!!.provider)) { // Request permission - https://stackoverflow.com/a/44351320/1816603
                    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, providerInfo.provider)
                    Home.instance.startActivityForResult(intent, REQUEST_BIND_WIDGET)
                }
            }
            widgetLayout.removeView(hostView)
            hostView = host.createView(Tools.publicContext!!.applicationContext, id, providerInfo).apply {
                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    gravity = Gravity.CENTER
                }
                setAppWidget(id, providerInfo)
            }
            widgetLayout.addView(hostView)
            resize(Settings["widget:$hostId:height", ViewGroup.LayoutParams.WRAP_CONTENT])
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteWidget(widgetLayout: ResizableLayout) {
        hostView?.appWidgetId?.let { host.deleteAppWidgetId(it) }
        widgetLayout.removeView(hostView)
        hostView = null
        widgetLayout.visibility = View.GONE
        Settings["widget:$hostId"] = ""
    }

    fun resize(newHeight: Int) {
        val density = Tools.publicContext!!.resources.displayMetrics.density
        val width = (Device.displayWidth / density).toInt()
        val height = (newHeight / density).toInt()
        try {
            hostView!!.updateAppWidgetSize(null, width, height, width, height)
            println("resized")
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun startListening() {
        runCatching { host.startListening() }
    }

    fun stopListening() {
        runCatching { host.stopListening() }
    }
}