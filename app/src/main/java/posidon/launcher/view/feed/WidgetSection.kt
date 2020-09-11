package posidon.launcher.view.feed

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import posidon.launcher.external.Widget
import posidon.launcher.storage.Settings
import posidon.launcher.view.ResizableLayout

class WidgetSection(
    context: Context,
    var widget: Widget
) : ResizableLayout(context), FeedSection {

    override fun onAdd(feed: Feed) {
        layoutParams.height = Settings["widget:${widget.hostId}:height", ViewGroup.LayoutParams.WRAP_CONTENT]

        onResizeListener = object : OnResizeListener {
            override fun onStop(newHeight: Int) { Settings["widget:${widget.hostId}:height"] = newHeight }
            override fun onCrossPress() = feed.remove(this@WidgetSection)
            override fun onMajorUpdate(newHeight: Int) = widget.resize(newHeight)
            override fun onUpdate(newHeight: Int) {
                layoutParams.height = newHeight
                layoutParams = layoutParams
            }
        }

        widget.startListening()
        widget.fromSettings(this)
    }

    fun startListening() = widget.startListening()
    fun stopListening() = widget.stopListening()

    override fun updateTheme(activity: Activity) {

    }

    override fun onPause() = stopListening()
    override fun onResume(activity: Activity) = startListening()

    override fun toString() = "widget:${widget.hostId}"

    override fun onDelete(feed: Feed) {
        widget.deleteWidget(this@WidgetSection)
    }
}