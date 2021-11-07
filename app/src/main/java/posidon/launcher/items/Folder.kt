package posidon.launcher.items

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.*
import android.graphics.drawable.shapes.RoundRectShape
import android.view.*
import android.widget.*
import io.posidon.android.launcherutils.liveWallpaper.Kustom
import posidon.android.conveniencelib.Device
import posidon.android.conveniencelib.dp
import posidon.android.conveniencelib.toBitmap
import posidon.launcher.Home
import posidon.launcher.R
import posidon.launcher.drawable.FastColorDrawable
import posidon.launcher.drawable.NonDrawable
import posidon.launcher.items.users.ItemLongPress
import posidon.launcher.items.users.customAppIcon.CustomAppIcon
import posidon.launcher.storage.Settings
import posidon.launcher.tools.*
import posidon.launcher.tools.theme.Customizer
import posidon.launcher.tools.theme.Icons
import kotlin.math.abs
import kotlin.math.min

class Folder : LauncherItem {

    val items: ArrayList<LauncherItem>
    var uid: String

    override var icon: Drawable? = null
        private set
    override var label: String?

    constructor(string: String) : super() {
        val (u, i) = parseFolderData(string)
        uid = u
        items = i
        label = Settings["folder:$uid:label", "folder"]
        updateIcon()
    }

    constructor(uid: String, items: ArrayList<LauncherItem>) {
        this.items = items
        this.uid = uid
        label = Settings["folder:$uid:label", "folder"]
        updateIcon()
    }

    constructor(items: ArrayList<LauncherItem>) {
        this.items = items
        uid = generateUid()
        label = "folder"
        icon = icon(Tools.appContext!!)?.let {
            Icons.animateIfShould(Tools.appContext!!, it)
            Icons.applyInsets(it)
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (app in items) {
            sb.append("\t").append(app.toString())
        }
        return "folder:$uid$sb"
    }

    private fun icon(context: Context): Drawable? {
        try {
            val previewApps = min(items.size, 4)
            val drr = arrayOfNulls<Drawable>(previewApps + 1)
            drr[0] = FastColorDrawable(Settings["folderBG", -0x22eeeded])
            for (i in 1..previewApps) {
                drr[i] = BitmapDrawable(context.resources, items[i - 1].icon!!.toBitmap())
            }
            val layerDrawable = LayerDrawable(drr)
            val width = layerDrawable.intrinsicWidth
            val height = layerDrawable.intrinsicHeight
            val paddingNear = width / 6
            val paddingFar = width / 12 * 7
            val paddingMedium = (paddingFar + paddingNear) / 2
            when (previewApps) {
                0 -> {}
                1 -> layerDrawable.setLayerInset(1, paddingMedium, paddingMedium, paddingMedium, paddingMedium)
                2 -> {
                    layerDrawable.setLayerInset(1, paddingNear, paddingMedium, paddingFar, paddingMedium)
                    layerDrawable.setLayerInset(2, paddingFar, paddingMedium, paddingNear, paddingMedium)
                }
                3 -> {
                    layerDrawable.setLayerInset(1, paddingNear, paddingNear, paddingFar, paddingFar)
                    layerDrawable.setLayerInset(2, paddingFar, paddingNear, paddingNear, paddingFar)
                    layerDrawable.setLayerInset(3, paddingMedium, paddingFar, paddingMedium, paddingNear)
                }
                else -> {
                    layerDrawable.setLayerInset(1, paddingNear, paddingNear, paddingFar, paddingFar)
                    layerDrawable.setLayerInset(2, paddingFar, paddingNear, paddingNear, paddingFar)
                    layerDrawable.setLayerInset(3, paddingNear, paddingFar, paddingFar, paddingNear)
                    layerDrawable.setLayerInset(4, paddingFar, paddingFar, paddingNear, paddingNear)
                }
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap!!)
            layerDrawable.setBounds(0, 0, width, height)
            layerDrawable.draw(canvas)

            val iconShape = Icons.IconShape(Settings["icshape", 4])
            if (!iconShape.isSquare) {
                canvas.drawPath(iconShape.getPath(width, height), Paint().apply {
                    isAntiAlias = true
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                })
            }

            return BitmapDrawable(Tools.appContext!!.resources, bitmap)
        } catch (e: Exception) { e.printStackTrace() }
        return null
    }

    fun updateIcon() {
        val customIcon = Customizer.getCustomIcon("folder:$uid:icon")
        icon = (customIcon?.let {
            Icons.generateAdaptiveIcon(it)
        } ?: icon(Tools.appContext!!))?.let {
            Icons.animateIfShould(Tools.appContext!!, it)
            Icons.applyInsets(it)
        }
    }

    fun clear() {
        items.clear()
    }

    override val notificationCount: Int
        get() {
            var count = 0
            for (item in items) {
                if (item is App) {
                    count += item.notificationCount
                }
            }
            return count
        }

    override fun open(context: Context, view: View, dockI: Int) {
        if (currentlyOpen == null) {

            if (Settings["kustom:variables:enable", false]) {
                Kustom[context, "posidon", "screen"] = "folder"
            }

            val content = LayoutInflater.from(context).inflate(R.layout.folder_layout, null)
            val popupWindow = PopupWindow(content, ListPopupWindow.WRAP_CONTENT, ListPopupWindow.WRAP_CONTENT, true)
            popupWindow.setOnDismissListener {
                currentlyOpen = null
            }
            currentlyOpen = popupWindow
            popupWindow.setBackgroundDrawable(NonDrawable())

            populateWindowView(content, context, { folderI, v: View, folder: Folder ->
                ItemLongPress.onItemLongPress(context, v, folder, onRemove = {
                    popupWindow.dismiss()
                    folder.items.removeAt(folderI)
                    if (folderI < 4) {
                        folder.updateIcon()
                    }
                    Dock[dockI] = if (folder.items.size == 1) folder.items[0] else folder
                    Home.instance.dock.loadApps()
                }, onEdit = null, dockI = dockI, folderI = folderI, parentView = view)
                true
            }, { folderI, v: View, folder: Folder ->
                folder.items[folderI].open(context, v, -1)
                popupWindow.dismiss()
            })

            content.findViewById<View>(R.id.bg).background = ShapeDrawable().apply {
                val bgColor = Settings["folder:window:bg_color", -0x22eeeded]
                val r = context.dp(Settings["folder:radius", 18])
                shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
                paint.color = bgColor
            }

            val (x, y, gravity) = Tools.getPopupLocationFromView(view)
            popupWindow.showAtLocation(view, Gravity.BOTTOM or gravity, x, y)
        }
    }

    fun populateWindowView(content: View, context: Context, onItemLongPress: (folderI: Int, View, Folder) -> Boolean, onItemClick: (folderI: Int, View, Folder) -> Unit) {
        val container = content.findViewById<GridLayout>(R.id.container)
        container.columnCount = Settings["folder:columns", 3]
        val title = content.findViewById<TextView>(R.id.title)
        if (Settings["folder:show_title", true]) {
            title.setTextColor(Settings["folder:title_color", 0xffffffff.toInt()])
            title.text = label
        } else {
            title.visibility = View.GONE
            content.findViewById<View>(R.id.separator).visibility = View.GONE
        }
        val appListSize = items.size
        val columnCount = Settings["dock:columns", 5]
        val appSize = min(context.dp(Settings["dock:icons:size", 74]).toInt(), ((Device.screenWidth(context) - context.dp(32)) / columnCount).toInt())
        val notifBadgesEnabled = Settings["notif:badges", true]
        val notifBadgesShowNum = Settings["notif:badges:show_num", true]
        val labelsEnabled = Settings["folderLabelsEnabled", false]
        container.removeAllViews()
        for (folderI in 0 until appListSize) {
            val item = items[folderI]
            println("$item")
            val appIcon = createItem(item, context, container, appSize, labelsEnabled, notifBadgesEnabled, notifBadgesShowNum, { v -> onItemLongPress(folderI, v, this) }, { v -> onItemClick(folderI, v, this) })
            container.addView(appIcon)
        }
        content.setOnDragListener(this@Folder::onDrag)
    }

    private fun onDrag(v: View, event: DragEvent): Boolean {
        when (event.action) {
            DragEvent.ACTION_DRAG_LOCATION -> {
                val container = v.findViewById<GridLayout>(R.id.container)
                val location = IntArray(2)
                val view = event.localState as View?
                if (view != null) {
                    view.getLocationInWindow(location)
                    location[0] -= v.x.toInt()
                    location[1] -= v.y.toInt()
                    val x = abs(event.x - location[0] - view.measuredWidth / 2f)
                    val y = abs(event.y - location[1] - view.measuredHeight / 2f)
                    if (x > view.measuredWidth / 2f || y > view.measuredHeight / 2f) {
                        ItemLongPress.currentPopup?.dismiss()
                        container.getLocationInWindow(location)
                        val x = abs(event.x - location[0] - container.measuredWidth / 2f)
                        val y = abs(event.y - location[1] - container.measuredHeight / 2f)
                        if (x > container.measuredWidth / 2f || y > container.measuredHeight / 2f) {
                            currentlyOpen?.dismiss()
                        }
                    }
                }

                var i = 0
                while (i < container.childCount) {
                    val child = container.getChildAt(i)
                    child.getLocationInWindow(location)
                    location[0] -= v.x.toInt()
                    location[1] -= v.y.toInt()
                    val threshHold = min(child.height / 2.toFloat(), v.dp(100))
                    val x = abs(location[0] - (event.x - child.height / 2f))
                    val y = abs(location[1] - (event.y - child.height / 2f))
                    println("$x, $y")
                    if (x < threshHold && y < threshHold) {
                        (view!!.parent as ViewGroup?)?.removeView(view)
                        container.addView(view, i)
                        break
                    }
                    i++
                }
            }
            DragEvent.ACTION_DRAG_STARTED -> {
                (event.localState as View?)?.visibility = View.INVISIBLE
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                (event.localState as View?)?.visibility = View.VISIBLE
                ItemLongPress.currentPopup?.isFocusable = true
                ItemLongPress.currentPopup?.update()
            }
            DragEvent.ACTION_DROP -> {
                val container = v.findViewById<GridLayout>(R.id.container)
                val location = IntArray(2)
                var i = 0
                while (i < container.childCount) {
                    val child = container.getChildAt(i)
                    child.getLocationInWindow(location)
                    val threshHold = min(child.height / 2.toFloat(), v.dp(100))
                    val x = abs(location[0] - (event.x - child.height / 2f))
                    val y = abs(location[1] - (event.y - child.height / 2f))
                    if (x < threshHold && y < threshHold) {
                        val dockI = event.clipData.getItemAt(0).text.toString().toInt(16)
                        val item = LauncherItem(event.clipData.description.label.toString())!!
                        items.add(i, item)
                        if (i < 4) {
                            updateIcon()
                        }
                        Dock[dockI] = this
                        break
                    }
                    i++
                }
            }
        }
        return true
    }

    private fun createItem(item: LauncherItem, context: Context, container: GridLayout?, appSize: Int, labelsEnabled: Boolean, notifBadgesEnabled: Boolean, notifBadgesShowNum: Boolean, onItemLongPress: View.OnLongClickListener, onItemClick: View.OnClickListener): View? {
        val appIcon = LayoutInflater.from(context).inflate(R.layout.drawer_item, container, false)
        val icon = appIcon.findViewById<ImageView>(R.id.iconimg)
        appIcon.findViewById<View>(R.id.iconFrame).run {
            layoutParams.height = appSize
            layoutParams.width = appSize
        }
        icon.setImageDrawable(item.icon)
        val iconTxt = appIcon.findViewById<TextView>(R.id.icontxt)
        if (labelsEnabled) {
            iconTxt.text = item.label
            Customizer.styleLabel("folder:labels", iconTxt, -0x22000001, 12f)
        } else iconTxt.visibility = View.GONE
        if (notifBadgesEnabled && item.notificationCount != 0) {
            val badge = appIcon.findViewById<TextView>(R.id.notificationBadge)
            badge.visibility = View.VISIBLE
            badge.text = if (notifBadgesShowNum) item.notificationCount.toString() else ""
            Icons.generateNotificationBadgeBGnFG(item.icon!!) { bg, fg ->
                badge.background = bg
                badge.setTextColor(fg)
            }
        }
        appIcon.setOnLongClickListener(onItemLongPress)
        appIcon.setOnClickListener(onItemClick)
        return appIcon
    }

    inline fun edit(view: View, dockI: Int) {
        val context = view.context
        val editContent = LayoutInflater.from(context).inflate(R.layout.app_edit_menu, null)
        val editWindow = PopupWindow(editContent, androidx.appcompat.widget.ListPopupWindow.WRAP_CONTENT, androidx.appcompat.widget.ListPopupWindow.WRAP_CONTENT, true)
        val editLabel = editContent.findViewById<EditText>(R.id.editlabel)
        editContent.findViewById<ImageView>(R.id.iconimg).setImageDrawable(this@Folder.icon)
        editContent.findViewById<ImageView>(R.id.iconimg).setOnClickListener {
            val intent = Intent(context, CustomAppIcon::class.java)
            intent.putExtra("key", "folder:${uid}:icon")
            context.startActivity(intent)
        }
        editLabel.setText(label)
        editWindow.setOnDismissListener {
            label = editLabel.text.toString()
            Settings["folder:${uid}:label"] = label
            updateIcon()
            Dock[dockI] = this@Folder
            Home.instance.dock.loadApps()
        }
        editWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
    }

    companion object {
        var currentlyOpen: PopupWindow? = null

        private var uidCounter = -1
        fun generateUid(): String {
            if (uidCounter == -1) {
                uidCounter = Settings["folder:uids:count", 0]
            }
            val str = uidCounter++.toString(16).padStart(8, '_')
            Settings["folder:uids:count"] = uidCounter
            return str
        }

        fun parseFolderData(string: String): Pair<String, ArrayList<LauncherItem>> {
            val items = ArrayList<LauncherItem>()
            val appsList = string.substring(7, string.length).split('\t')
            val uid = appsList[0]
            for (i in 1 until appsList.size) {
                val app = LauncherItem(appsList[i])
                if (app != null) {
                    items.add(app)
                }
            }
            return uid to items
        }
    }
}