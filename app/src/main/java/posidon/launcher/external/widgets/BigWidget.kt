package posidon.launcher.external.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import posidon.launcher.R
import posidon.launcher.storage.Settings

class BigWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Settings.init(context.applicationContext)
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_big)
            views.setTextColor(R.id.time, Settings["clockcolor", -0x1])
            views.setTextColor(R.id.date, Settings["clockcolor", -0x1])
            views.setCharSequence(R.id.date, "setFormat12Hour", Settings["datef", context.resources.getString(R.string.defaultdateformat)])
            views.setCharSequence(R.id.date, "setFormat24Hour", Settings["datef", context.resources.getString(R.string.defaultdateformat)])
            /*views.setOnClickPendingIntent(R.id.time, PendingIntent.getActivity(context, 0, Intent(Intent.ACTION_QUICK_CLOCK)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0))
            views.setOnClickPendingIntent(R.id.date, PendingIntent.getActivity(context, 0, Intent(Intent.ACTION_DEFAULT)
                    .addCategory(Intent.CATEGORY_APP_CALENDAR)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0))*/
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}