package posidon.launcher.view.feed

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.ViewGroup
import posidon.launcher.external.Widget
import posidon.launcher.storage.Settings
import posidon.launcher.view.ResizableLayout

class WidgetSection(
    context: Context,
    attrs: AttributeSet? = null
) : ResizableLayout(context, attrs), FeedSection {

    lateinit var widget: Widget

    fun init(uid: Int) {
        widget = Widget(uid)

        layoutParams.height = Settings["widget:$uid:height", ViewGroup.LayoutParams.WRAP_CONTENT]

        onResizeListener = object : OnResizeListener {
            override fun onStop(newHeight: Int) { Settings["widget:$uid:height"] = newHeight }
            override fun onCrossPress() = widget.deleteWidget(this@WidgetSection)
            override fun onMajorUpdate(newHeight: Int) = widget.resize(newHeight)
            override fun onUpdate(newHeight: Int) {
                layoutParams.height = newHeight
                layoutParams = layoutParams
            }
        }

        widget.startListening()
        widget.fromSettings(this)
    }

    fun handleIntent(data: Intent?) = widget.fromIntent(this, data)
    fun startListening() = widget.startListening()
    fun stopListening() = widget.stopListening()

    fun handleActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Widget.REQUEST_PICK_APPWIDGET) {
                val extras = data!!.extras
                if (extras != null) {
                    val id = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                    val widgetInfo = AppWidgetManager.getInstance(context).getAppWidgetInfo(id)
                    if (widgetInfo.configure != null) {
                        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                        intent.component = widgetInfo.configure
                        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                        runCatching {
                            activity.startActivityForResult(intent, Widget.REQUEST_CREATE_APPWIDGET)
                        }
                    } else {
                        handleIntent(data)
                    }
                }
            } else if (requestCode == Widget.REQUEST_CREATE_APPWIDGET) {
                handleIntent(data)
            }
        } else if (resultCode == Activity.RESULT_CANCELED && data != null) {
            val appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            if (appWidgetId != -1) {
                widget.host.deleteAppWidgetId(appWidgetId)
            }
        }
    }

    override fun updateTheme(activity: Activity) {

    }
}