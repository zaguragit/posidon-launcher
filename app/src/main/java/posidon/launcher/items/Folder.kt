package posidon.launcher.items

import android.content.Context
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
import posidon.launcher.items.users.ItemLongPress
import posidon.launcher.storage.Settings
import posidon.launcher.tools.*
import posidon.launcher.view.drawer.BottomDrawerBehavior
import kotlin.math.abs
import kotlin.math.min

class Folder(string: String) : LauncherItem() {

    val items = ArrayList<LauncherItem>()

    var uid: String

    init {
        val appsList = string.substring(7, string.length).split('\t')
        uid = appsList[0]
        for (i in 1 until appsList.size) {
            val app = LauncherItem(appsList[i])
            if (app != null) {
                items.add(app)
            }
        }

        label = Settings["folder:$uid:label", "folder"]

        val customIcon = Settings["folder:$uid:icon", ""]
        icon = if (customIcon != "") {
            try {
                val data = customIcon.split(':').toTypedArray()[1].split('|').toTypedArray()
                val t = Tools.publicContext!!.packageManager.getResourcesForApplication(data[0])
                val intRes = t.getIdentifier(data[1], "drawable", data[0])
                ThemeTools.badgeMaybe(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ThemeTools.generateAdaptiveIcon(t.getDrawable(intRes))
                } else t.getDrawable(intRes), false)
            } catch (e: Exception) {
                e.printStackTrace()
                icon(Tools.publicContext!!)
            }
        } else icon(Tools.publicContext!!)
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
                canvas.drawPath(ThemeTools.getAdaptiveIconPath(icShape, width, height), Paint().apply {
                    isAntiAlias = true
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                })
            }

            return ThemeTools.badgeMaybe(BitmapDrawable(Tools.publicContext!!.resources, bitmap), false)
        } catch (e: Exception) { e.printStackTrace() }
        return null
    }

    fun clear() {
        items.clear()
    }

    fun calculateNotificationCount(): Int {
        var count = 0
        for (item in items) {
            if (item is App) {
                count += item.notificationCount
            }
        }
        return count
    }

    fun open(context: Context, view: View, i: Int) {
        if (currentlyOpen == null) {

            val bgColor = Settings["folderBG", -0x22eeeded]
            val r = Settings["folderCornerRadius", 18].dp
            val labelsEnabled = Settings["folderLabelsEnabled", false]
            val columnCount = Settings["dock:columns", 5]
            val appSize = min(when (Settings["dockicsize", 1]) {
                0 -> 64.dp.toInt()
                2 -> 84.dp.toInt()
                else -> 74.dp.toInt()
            }, ((Device.displayWidth - 32.dp) / columnCount).toInt())
            val notifBadgesEnabled = Settings["notif:badges", true]
            val notifBadgesShowNum = Settings["notif:badges:show_num", true]

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
            while (i1 < appListSize) {
                val item = items[i1]
                val appIcon = LayoutInflater.from(context).inflate(R.layout.drawer_item, null)
                val icon = appIcon.findViewById<ImageView>(R.id.iconimg)
                appIcon.findViewById<View>(R.id.iconFrame).run {
                    layoutParams.height = appSize
                    layoutParams.width = appSize
                }
                icon.setImageDrawable(item.icon)
                val iconTxt = appIcon.findViewById<TextView>(R.id.icontxt)
                if (labelsEnabled) {
                    iconTxt.text = item.label
                    iconTxt.setTextColor(Settings["folder:label_color", -0x22000001])
                } else iconTxt.visibility = View.GONE
                if (item is App) {
                    val badge = appIcon.findViewById<TextView>(R.id.notificationBadge)
                    if (notifBadgesEnabled && item.notificationCount != 0) {
                        badge.visibility = View.VISIBLE
                        badge.text = if (notifBadgesShowNum) item.notificationCount.toString() else ""
                        ThemeTools.generateNotificationBadgeBGnFG(item.icon!!) { bg, fg ->
                            badge.background = bg
                            badge.setTextColor(fg)
                        }
                    } else {
                        badge.visibility = View.GONE
                    }
                    appIcon.setOnClickListener { v ->
                        item.open(context, v)
                        popupWindow.dismiss()
                    }
                    val finalI = i1
                    appIcon.setOnLongClickListener(ItemLongPress.insideFolder(context, item, i, view, finalI, popupWindow, this))
                } else if (item is Shortcut) {
                    appIcon.setOnClickListener { v ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            item.open(context, v)
                        }
                        popupWindow.dismiss()
                    }
                }
                container.addView(appIcon)
                i1++
            }
            popupWindow.setOnDismissListener {
                currentlyOpen = null
            }
            content.findViewById<View>(R.id.bg).background = ShapeDrawable().apply {
                shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
                paint.color = bgColor
            }
            val location = IntArray(2)
            view.getLocationInWindow(location)
            val gravity = if (location[0] > Device.displayWidth / 2) {
                Gravity.END
            } else {
                Gravity.START
            }
            val x = if (location[0] > Device.displayWidth / 2) {
                Device.displayWidth - location[0] - view.measuredWidth
            } else {
                location[0]
            }
            var y = (-view.y + view.height * Settings["dock:rows", 1] + Tools.navbarHeight + (Settings["dockbottompadding", 10] + 14).dp).toInt()
            if (Settings["docksearchbarenabled", false] && Settings["dock:search:below_apps", true] && !context.isTablet) {
                y += 68.dp.toInt()
            }
            popupWindow.contentView.setOnDragListener { _, event ->
                when (event.action) {
                    DragEvent.ACTION_DRAG_LOCATION -> {
                        val view = event.localState as View?
                        if (view != null) {
                            val location = IntArray(2)
                            view.getLocationInWindow(location)
                            val x = abs(event.x - location[0] - view.measuredWidth / 2f)
                            val y = abs(event.y - location[1] - view.measuredHeight / 2f)
                            if (x > view.width / 3.5f || y > view.height / 3.5f) {
                                ItemLongPress.currentPopup?.dismiss()
                                currentlyOpen?.dismiss()
                                Home.instance.drawer.state = BottomDrawerBehavior.STATE_COLLAPSED
                            }
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
                        val i = event.clipData.getItemAt(0).text.toString().toInt(16)
                        LauncherItem(event.clipData.description.label.toString())?.let { items.add(i, it) }
                    }
                }
                true
            }
            popupWindow.showAtLocation(view, Gravity.BOTTOM or gravity, x, y)
        }
    }

    companion object {
        var currentlyOpen: PopupWindow? = null
    }
}