package posidon.launcher.items.users

import android.content.ClipData
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.recyclerview.widget.RecyclerView
import io.posidon.android.launcherutils.liveWallpaper.Kustom
import posidon.android.conveniencelib.Colors
import posidon.android.conveniencelib.dp
import posidon.launcher.R
import posidon.launcher.drawable.NonDrawable
import posidon.launcher.items.App
import posidon.launcher.items.Folder
import posidon.launcher.items.LauncherItem
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Dock
import posidon.launcher.tools.Tools
import posidon.launcher.tools.vibrate
import posidon.launcher.view.recycler.LinearLayoutManager

object ItemLongPress {

    const val REMOVE = 0
    const val HIDE = 1
    const val UNHIDE = 2

    var currentPopup: PopupWindow? = null
    fun makePopupWindow(context: Context, item: LauncherItem, onEdit: ((View) -> Unit)?, onRemove: ((View) -> Unit)?, onInfo: ((View) -> Unit)?, removeFunction: Int): PopupWindow {

        if (Settings["kustom:variables:enable", false]) {
            Kustom[context, "posidon", "screen"] = "popup"
        }

        val color = item.getColor()
        val txtColor = if (Colors.getLuminance(color) > .6f) -0xeeeded else -0x1

        val content = if (item is App && item.getShortcuts(context)!!.isNotEmpty()) {
            val shortcuts = item.getShortcuts(context)
            val c = LayoutInflater.from(context).inflate(R.layout.app_long_press_menu_w_shortcuts, null)
            val recyclerView: RecyclerView = c.findViewById(R.id.shortcuts)
            recyclerView.isNestedScrollingEnabled = false
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = ShortcutAdapter(context, shortcuts!!, txtColor)
            recyclerView.background = ShapeDrawable().apply {
                val r = context.dp(18)
                shape = RoundRectShape(floatArrayOf(0f, 0f, 0f, 0f, r, r, r, r), null, null)
                paint.color = 0x33000000
            }
            c
        } else LayoutInflater.from(context).inflate(R.layout.app_long_press_menu, null)
        val window = PopupWindow(content, ListPopupWindow.WRAP_CONTENT, ListPopupWindow.WRAP_CONTENT, true)
        currentPopup = window
        window.setOnDismissListener { currentPopup = null }
        window.setBackgroundDrawable(NonDrawable())

        val title = content.findViewById<TextView>(R.id.title)
        val removeButton = content.findViewById<View>(R.id.removeBtn)
        val editButton = content.findViewById<View>(R.id.editbtn)
        val propertiesButton = content.findViewById<View>(R.id.appinfobtn)

        title.text = item.label
        title.setTextColor(txtColor)

        content.findViewById<View>(R.id.bg).background = run {
            val bg = ShapeDrawable()
            val r = context.dp(18)
            bg.shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
            bg.paint.color = color
            bg
        }

        if (removeButton is TextView) {
            removeButton.setTextColor(txtColor)
            (propertiesButton as TextView).setTextColor(txtColor)
            (editButton as TextView).setTextColor(txtColor)
            removeButton.compoundDrawableTintList = ColorStateList.valueOf(txtColor)
            propertiesButton.compoundDrawableTintList = ColorStateList.valueOf(txtColor)
            editButton.compoundDrawableTintList = ColorStateList.valueOf(txtColor)
            when (removeFunction) {
                HIDE -> {
                    removeButton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_hide, 0, 0, 0)
                    removeButton.setText(R.string.hide)
                }
                UNHIDE -> {
                    removeButton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_hide, 0, 0, 0)
                    removeButton.setText(R.string.unhide)
                }
            }
        } else {
            (removeButton as ImageView).imageTintList = ColorStateList.valueOf(txtColor)
            (propertiesButton as ImageView).imageTintList = ColorStateList.valueOf(txtColor)
            (editButton as ImageView).imageTintList = ColorStateList.valueOf(txtColor)
            when (removeFunction) {
                HIDE, UNHIDE -> {
                    removeButton.setImageResource(R.drawable.ic_hide)
                }
            }
        }

        if (onEdit == null) editButton.visibility = View.GONE else {
            editButton.setOnClickListener {
                window.dismiss()
                onEdit(it)
            }
        }

        if (onRemove == null) removeButton.visibility = View.GONE else {
            removeButton.setOnClickListener {
                window.dismiss()
                onRemove(it)
            }
        }

        if (onInfo == null) propertiesButton.visibility = View.GONE else {
            propertiesButton.setOnClickListener {
                window.dismiss()
                onInfo(it)
            }
        }

        return window
    }

    inline fun onItemLongPress(context: Context, view: View, item: LauncherItem, noinline onEdit: ((View) -> Unit)?, noinline onRemove: ((View) -> Unit)?, dockI: Int = -1, folderI: Int = -1, parentView: View = view, removeFunction: Int = REMOVE) {
        if (currentPopup == null) {
            context.vibrate()

            val icon = view.findViewById<View>(R.id.iconimg)

            val (x, y, gravity) = Tools.getPopupLocationFromView(icon)

            val realItem = if (folderI != -1 && item is Folder) item.items[folderI] else item

            val popupWindow = makePopupWindow(context, realItem, onEdit, onRemove, if (realItem is App) ({
                val color = item.getColor()
                val hsv = FloatArray(3)
                Color.colorToHSV(color, hsv)
                if (hsv[2] > 0.5f) {
                    hsv[2] = 0.5f
                }
                realItem.showProperties(context, Color.HSVToColor(hsv))
            }) else null, removeFunction)

            if (!Settings["locked", false]) {
                popupWindow.isFocusable = false // !
                val shadow = View.DragShadowBuilder(icon)
                val clipData = if (dockI == -1) {
                    ClipData.newPlainText(realItem.toString(), "")
                } else ClipData.newPlainText(realItem.toString(), dockI.toString(16))

                icon.startDragAndDrop(clipData, shadow, view, View.DRAG_FLAG_OPAQUE or View.DRAG_FLAG_GLOBAL)

                if (dockI != -1) {
                    if (folderI != -1 && item is Folder) {
                        item.items.removeAt(folderI)
                        if (folderI < 4) {
                            item.updateIcon()
                        }
                        Dock[dockI] = if (item.items.size == 1) item.items[0] else item
                    } else {
                        Dock[dockI] = null
                    }
                }
            }

            popupWindow.showAtLocation(parentView, gravity, x, y)
        }
    }
}