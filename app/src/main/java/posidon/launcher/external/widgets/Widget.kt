package posidon.launcher.external.widgets

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import posidon.android.conveniencelib.Device
import posidon.android.conveniencelib.dp
import posidon.launcher.Home
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.view.ResizableLayout
import posidon.launcher.view.feed.WidgetSection
import posidon.launcher.view.recycler.GridLayoutManager

class Widget(
    val widgetId: Int
) {

    companion object {

        const val HOST_ID: Int = 0x3eed6ee2
        const val REQUEST_CREATE_APPWIDGET = 1
        const val REQUEST_BIND_WIDGET = 2

        fun fromIntent(activity: Activity, id: Int, provider: ComponentName): WidgetSection? {
            val widgetManager = AppWidgetManager.getInstance(Tools.appContext)
            return try {
                if (!widgetManager.bindAppWidgetIdIfAllowed(id, provider)) {
                    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
                    activity.startActivityForResult(intent, REQUEST_BIND_WIDGET)
                }
                Settings["widget:$id"] = provider.packageName + "/" + provider.className + "/" + id
                WidgetSection(activity.applicationContext, Widget(id))
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun handleActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?): WidgetSection? {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == REQUEST_CREATE_APPWIDGET) {
                    val extras = data!!.extras
                    val id = extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                    return fromIntent(activity, id, tmpProvider!!).also { tmpProvider = null }
                }
            } else if (resultCode == Activity.RESULT_CANCELED && data != null) {
                val appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                if (appWidgetId != -1) {
                    tmpHost?.let {
                        it.deleteAppWidgetId(appWidgetId)
                        it.deleteHost()
                        tmpHost = null
                    }
                }
            }
            return null
        }

        private var tmpHost: AppWidgetHost? = null
        private var tmpProvider: ComponentName? = null

        fun selectWidget(activity: Activity, onSelect: (WidgetSection) -> Unit) {

            val widgetManager = AppWidgetManager.getInstance(activity.applicationContext)

            BottomSheetDialog(activity, R.style.bottomsheet).apply {
                setContentView(LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(TextView(context).apply {
                        setText(R.string.widgets)
                        setTextColor(context.resources.getColor(R.color.cardtitle))
                        textSize = 24f
                        gravity = Gravity.CENTER_HORIZONTAL
                        val p = dp(8).toInt()
                        setPadding(p, p, p, p)
                    })
                    addView(RecyclerView(context).apply {
                        isVerticalFadingEdgeEnabled = true
                        setFadingEdgeLength(dp(28).toInt())
                        layoutManager = GridLayoutManager(context, 2)

                        class VH(
                            view: View,
                            val label: TextView,
                            val preview: ImageView,
                            val icon: ImageView
                        ) : RecyclerView.ViewHolder(view)

                        val providers = widgetManager.installedProviders
                        val pm = context.packageManager
                        adapter = object : RecyclerView.Adapter<VH>() {

                            override fun getItemCount() = providers.size

                            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
                                val label = TextView(context).apply {
                                    setTextColor(context.resources.getColor(R.color.cardtxt))
                                    textSize = 12f
                                    gravity = Gravity.CENTER_VERTICAL
                                }
                                val preview = ImageView(context)
                                val icon = ImageView(context).apply {
                                    val p = dp(8).toInt()
                                    setPadding(p, p, p, p)
                                }
                                val v = CardView(context).apply {
                                    layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT).apply {
                                        val m = dp(8).toInt()
                                        setMargins(m, m, m, m)
                                    }
                                    setCardBackgroundColor(resources.getColor(R.color.cardbg))
                                    cardElevation = 10f
                                    radius = dp(16)
                                    preventCornerOverlap = true
                                    clipToPadding = true
                                    addView(LinearLayout(context).apply {
                                        orientation = LinearLayout.VERTICAL
                                        val h = dp(48).toInt()
                                        addView(preview, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Device.screenWidth(context) / 2 - h))
                                        addView(View(context).apply { setBackgroundResource(R.drawable.card_separator) }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(2).toInt()))
                                        addView(LinearLayout(context).apply {
                                            orientation = LinearLayout.HORIZONTAL
                                            addView(icon, LinearLayout.LayoutParams(h, h))
                                            addView(label, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h))
                                        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h))
                                    })
                                }
                                return VH(v, label, preview, icon)
                            }

                            val iconCache = arrayOfNulls<Drawable>(providers.size)
                            val previewCache = arrayOfNulls<Drawable>(providers.size)

                            override fun onBindViewHolder(holder: VH, i: Int) {
                                val p = providers[i]
                                holder.label.text = p.loadLabel(pm)
                                holder.icon.setImageDrawable(iconCache[i]
                                    ?: p.loadIcon(context, resources.displayMetrics.densityDpi).also { iconCache[i] = it })
                                holder.preview.setImageDrawable(previewCache[i]
                                    ?: (p.loadPreviewImage(context, resources.displayMetrics.densityDpi) ?: iconCache[i]).also { previewCache[i] = it })
                                holder.itemView.setOnClickListener {
                                    val widgetId = AppWidgetHost(activity.applicationContext, HOST_ID)
                                        .also { tmpHost = it }
                                        .allocateAppWidgetId()
                                    if (p.configure != null) {
                                        try {
                                            tmpProvider = p.provider
                                            activity.startActivityForResult(Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                                                component = p.configure
                                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                                            }, REQUEST_CREATE_APPWIDGET)
                                        } catch (e: Exception) { e.printStackTrace() }
                                    } else fromIntent(activity, widgetId, p.provider)?.let { it1 -> onSelect(it1) }
                                    dismiss()
                                }
                            }
                        }
                    })
                })
                window!!.findViewById<View>(R.id.design_bottom_sheet).background = ShapeDrawable().apply {
                    val r = context.dp(18)
                    shape = RoundRectShape(floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f), null, null)
                    paint.color = 0xff0d0e0f.toInt()
                }
            }.show()
        }
    }

    private var hostView: AppWidgetHostView? = null
    var host: AppWidgetHost = AppWidgetHost(Tools.appContext, HOST_ID)
        private set

    fun fromSettings(widgetLayout: ResizableLayout): Boolean {
        return try {
            val widgetManager = AppWidgetManager.getInstance(widgetLayout.context.applicationContext)
            val str = Settings["widget:$widgetId", "posidon.launcher/posidon.launcher.external.widgets.ClockWidget"]
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
            hostView = host.createView(widgetLayout.context.applicationContext, id, providerInfo).apply {
                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    gravity = Gravity.CENTER
                }
                setAppWidget(id, providerInfo)
            }
            widgetLayout.addView(hostView)
            resize(Settings["widget:$widgetId:height", ViewGroup.LayoutParams.WRAP_CONTENT])
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
        //widgetLayout.visibility = View.GONE
        Settings["widget:$widgetId"] = null
    }

    fun resize(newHeight: Int) {
        val density = Tools.appContext!!.resources.displayMetrics.density
        val width = (Device.screenWidth(Tools.appContext!!) / density).toInt()
        val height = (newHeight / density).toInt()
        try {
            hostView!!.updateAppWidgetSize(null, width, height, width, height)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun startListening() {
        runCatching { host.startListening() }
    }

    fun stopListening() {
        runCatching { host.stopListening() }
    }
}