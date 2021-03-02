package posidon.launcher.items

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.*
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.view.DragEvent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import posidon.launcher.Home
import posidon.launcher.R
import posidon.launcher.external.Kustom
import posidon.launcher.items.users.CustomAppIcon
import posidon.launcher.items.users.ItemLongPress
import posidon.launcher.storage.Settings
import posidon.launcher.tools.*
import posidon.launcher.tools.theme.Customizer
import posidon.launcher.tools.theme.Icons
import posidon.launcher.tools.theme.toBitmap
import posidon.launcher.view.drawer.BottomDrawerBehavior
import kotlin.math.abs
import kotlin.math.min

class Folder : LauncherItem {

    val items: ArrayList<LauncherItem>
    var uid: String

    override val icon: Drawable?
    override var label: String?

    constructor(string: String) : super() {
        this.items = ArrayList()
        val appsList = string.substring(7, string.length).split('\t')
        uid = appsList[0]
        for (i in 1 until appsList.size) {
            val app = LauncherItem(appsList[i])
            if (app != null) {
                items.add(app)
            }
        }
        label = Settings["folder:$uid:label", "folder"]
        val customIcon = Customizer.getCustomIcon("folder:$uid:icon")
        icon = (customIcon?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Icons.generateAdaptiveIcon(it)
            } else it
        } ?: icon(Tools.appContext!!))?.let {
            Icons.animateIfShould(Tools.appContext!!, it)
            Icons.badgeMaybe(it, false)
        }
    }

    constructor(items: ArrayList<LauncherItem>) {
        this.items = items
        uid = generateUid()
        label = "folder"
        icon = icon(Tools.appContext!!)?.let {
            Icons.animateIfShould(Tools.appContext!!, it)
            Icons.badgeMaybe(it, false)
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
            drr[0] = ColorDrawable(Settings["folderBG", -0x22eeeded])
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

            val icShape = Settings["icshape", 4]
            if (icShape != 3) {
                canvas.drawPath(Icons.getAdaptiveIconPath(icShape, width, height), Paint().apply {
                    isAntiAlias = true
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                })
            }

            return BitmapDrawable(Tools.appContext!!.resources, bitmap)
        } catch (e: Exception) { e.printStackTrace() }
        return null
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
                Kustom["screen"] = "folder"
            }

            val content = LayoutInflater.from(context).inflate(R.layout.folder_layout, null)
            val popupWindow = PopupWindow(content, ListPopupWindow.WRAP_CONTENT, ListPopupWindow.WRAP_CONTENT, true)

            currentlyOpen = popupWindow

            popupWindow.setBackgroundDrawable(ColorDrawable(0x0))
            val container = content.findViewById<GridLayout>(R.id.container)
            container.columnCount = Settings["folderColumns", 3]
            val title = content.findViewById<TextView>(R.id.title)
            if (Settings["folder:show_title", true]) {
                title.setTextColor(Settings["folder:title_color", 0xffffffff.toInt()])
                title.text = label
            } else {
                title.visibility = View.GONE
                content.findViewById<View>(R.id.separator).visibility = View.GONE
            }
            var i1 = 0
            val appListSize = items.size

            val columnCount = Settings["dock:columns", 5]
            val appSize = min(when (Settings["dockicsize", 1]) {
                0 -> 64.dp.toInt()
                2 -> 84.dp.toInt()
                else -> 74.dp.toInt()
            }, ((Device.displayWidth - 32.dp) / columnCount).toInt())
            val notifBadgesEnabled = Settings["notif:badges", true]
            val notifBadgesShowNum = Settings["notif:badges:show_num", true]
            val labelsEnabled = Settings["folderLabelsEnabled", false]

            while (i1 < appListSize) {
                val appIcon = createItem(i1, context, container, appSize, labelsEnabled, notifBadgesEnabled, notifBadgesShowNum, popupWindow, dockI, view)
                container.addView(appIcon)
                i1++
            }
            popupWindow.setOnDismissListener {
                currentlyOpen = null
            }
            content.findViewById<View>(R.id.bg).background = ShapeDrawable().apply {
                val bgColor = Settings["folderBG", -0x22eeeded]
                val r = Settings["folderCornerRadius", 18].dp
                shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
                paint.color = bgColor
            }
            popupWindow.contentView.setOnDragListener { _, event ->
                when (event.action) {
                    DragEvent.ACTION_DRAG_LOCATION -> {
                        val view = event.localState as View
                        val location = IntArray(2)
                        view.getLocationInWindow(location)
                        val x = abs(event.x - location[0] - view.measuredWidth / 2f)
                        val y = abs(event.y - location[1] - view.measuredHeight / 2f)
                        if (x > view.measuredWidth / 2f || y > view.measuredHeight / 2f) {
                            ItemLongPress.currentPopup?.dismiss()
                            container.getLocationInWindow(location)
                            val x = abs(event.x - location[0] - container.measuredWidth / 2f)
                            val y = abs(event.y - location[1] - container.measuredHeight / 2f)
                            if (x > container.measuredWidth / 2f || y > container.measuredHeight / 2f) {
                                ItemLongPress.currentPopup?.dismiss()
                                currentlyOpen?.dismiss()
                                Home.instance.drawer.state = BottomDrawerBehavior.STATE_COLLAPSED
                            }
                        }

                        var i = 0
                        while (i < container.childCount) {
                            val child = container.getChildAt(i)
                            child.getLocationInWindow(location)
                            val threshHold = min(child.height / 2.toFloat(), 100.dp)
                            val x = abs(location[0] - (event.x - child.height / 2f))
                            val y = abs(location[1] - (event.y - child.height / 2f))
                            if (x < threshHold && y < threshHold) {
                                container.removeView(view)
                                container.addView(view, i)
                                break
                            }
                            i++
                        }
                    }
                    DragEvent.ACTION_DRAG_STARTED -> {
                        (event.localState as View).visibility = View.INVISIBLE
                    }
                    DragEvent.ACTION_DRAG_ENDED -> {
                        (event.localState as View).visibility = View.VISIBLE
                        ItemLongPress.currentPopup?.isFocusable = true
                        ItemLongPress.currentPopup?.update()
                    }
                    DragEvent.ACTION_DROP -> {
                        val location = IntArray(2)
                        var i = 0
                        while (i < container.childCount) {
                            val child = container.getChildAt(i)
                            child.getLocationInWindow(location)
                            val threshHold = min(child.height / 2.toFloat(), 100.dp)
                            val x = abs(location[0] - (event.x - child.height / 2f))
                            val y = abs(location[1] - (event.y - child.height / 2f))
                            if (x < threshHold && y < threshHold) {
                                val dockI = event.clipData.getItemAt(0).text.toString().toInt(16)
                                val item = LauncherItem(event.clipData.description.label.toString())!!
                                items.add(i, item)
                                Dock[dockI] = this
                                break
                            }
                            i++
                        }
                    }
                }
                true
            }
            val (x, y, gravity) = Tools.getPopupLocationFromView(view)
            popupWindow.showAtLocation(view, Gravity.BOTTOM or gravity, x, y)
        }
    }

    private fun createItem(folderI: Int, context: Context, container: GridLayout?, appSize: Int, labelsEnabled: Boolean, notifBadgesEnabled: Boolean, notifBadgesShowNum: Boolean, popupWindow: PopupWindow, dockI: Int, view: View): View? {
        val item = items[folderI]
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
            Customizer.styleLabel("folder:labels", iconTxt, -0x22000001)
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
        appIcon.setOnLongClickListener { v ->
            ItemLongPress.onItemLongPress(context, v, this, onRemove = {
                popupWindow.dismiss()
                items.removeAt(folderI)
                Dock[dockI] = if (items.size == 1) items[0] else this
                Home.instance.dock.loadApps()
            }, onEdit = null, dockI = dockI, folderI = folderI, parentView = view)
            true
        }
        appIcon.setOnClickListener { v ->
            item.open(context, v, -1)
            popupWindow.dismiss()
        }
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
            editWindow.dismiss()
        }
        editLabel.setText(label)
        editWindow.setOnDismissListener {
            label = editLabel.text.toString()
            Settings["folder:${uid}:label"] = label
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
    }
}