package posidon.launcher.view.feed

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import posidon.launcher.external.widgets.Widget
import posidon.launcher.storage.Settings
import posidon.launcher.view.ResizableLayout

class WidgetSection(
    context: Context,
    var widget: Widget
) : ResizableLayout(context), FeedSection {

    override fun onAdd(feed: Feed, i: Int) {
        layoutParams.height = Settings["widget:${widget.widgetId}:height", ViewGroup.LayoutParams.WRAP_CONTENT]
        onResizeListener = object : OnResizeListener {
            override fun onStop(newHeight: Int) { Settings["widget:${widget.widgetId}:height"] = newHeight }
            override fun onCrossPress() = feed.remove(this@WidgetSection)
            override fun onMajorUpdate(newHeight: Int) = widget.resize(newHeight)
            override fun onUpdate(newHeight: Int) {
                layoutParams.height = newHeight
                layoutParams = layoutParams
            }
        }

        if (!widget.fromSettings(this)) {
            feed.remove(this)
            return
        }
        widget.startListening()
    }

    override fun updateTheme(activity: Activity) {}

    override fun onPause() = widget.stopListening()
    override fun onResume(activity: Activity) = widget.startListening()

    override fun toString() = "widget:${widget.widgetId}"

    override fun onDelete(feed: Feed) {
        widget.deleteWidget(this@WidgetSection)
    }
}