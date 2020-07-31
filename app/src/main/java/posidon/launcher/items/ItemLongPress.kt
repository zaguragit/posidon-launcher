package posidon.launcher.items

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.palette.graphics.Palette
import posidon.launcher.view.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.*
import java.util.*

object ItemLongPress {

    var currentPopup: PopupWindow? = null
    private fun popupWindow(context: Context, methods: Methods, showRemove: Boolean, item: LauncherItem): PopupWindow {
        context.vibrate()
        val content: View
        var color: Int
        val txtColor: Int
        return if (item is App) {
            color = Palette.from(item.icon!!.toBitmap()).generate().getDominantColor(-0xdad9d9)
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            if (hsv[1] < 0.2f) {
                color = Palette.from(item.icon!!.toBitmap()).generate().getVibrantColor(-0xdad9d9)
                Color.colorToHSV(color, hsv)
            }
            txtColor = if (ColorTools.useDarkText(color)) -0xeeeded else -0x1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && item.getShortcuts(context)!!.isNotEmpty()) {
                val shortcuts = item.getShortcuts(context)
                content = LayoutInflater.from(context).inflate(R.layout.app_long_press_menu_w_shortcuts, null)
                val recyclerView: RecyclerView = content.findViewById(R.id.shortcuts)
                recyclerView.isNestedScrollingEnabled = false
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = ShortcutAdapter(context, shortcuts!!, txtColor)
                val bg = ShapeDrawable()
                val r = 18.dp
                bg.shape = RoundRectShape(floatArrayOf(0f, 0f, 0f, 0f, r, r, r, r), null, null)
                bg.paint.color = 0x33000000
                recyclerView.background = bg
            } else content = LayoutInflater.from(context).inflate(R.layout.app_long_press_menu, null)
            val window = PopupWindow(content, ListPopupWindow.WRAP_CONTENT, ListPopupWindow.WRAP_CONTENT, true)
            currentPopup = window
            window.setOnDismissListener { currentPopup = null }
            window.setBackgroundDrawable(ColorDrawable(0x0))
            val appinfobtn = content.findViewById<View>(R.id.appinfobtn)
            appinfobtn.setOnClickListener {
                window.dismiss()
                item.showProperties(context, Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], if (hsv[2] > 0.5f) 0.5f else hsv[2])))
            }
            val title = content.findViewById<TextView>(R.id.title)
            val removeBtn = content.findViewById<View>(R.id.removeBtn)
            val editBtn = content.findViewById<View>(R.id.editbtn)
            val bg = ShapeDrawable()
            val r = 18.dp
            bg.shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
            bg.paint.color = color
            content.findViewById<View>(R.id.bg).background = bg
            title.text = item.label
            title.setTextColor(txtColor)
            if (removeBtn is TextView) {
                removeBtn.setTextColor(txtColor)
                (appinfobtn as TextView).setTextColor(txtColor)
                (editBtn as TextView).setTextColor(txtColor)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    removeBtn.compoundDrawableTintList = ColorStateList.valueOf(txtColor)
                    appinfobtn.compoundDrawableTintList = ColorStateList.valueOf(txtColor)
                    editBtn.compoundDrawableTintList = ColorStateList.valueOf(txtColor)
                }
            } else {
                (removeBtn as ImageView).imageTintList = ColorStateList.valueOf(txtColor)
                (appinfobtn as ImageView).imageTintList = ColorStateList.valueOf(txtColor)
                (editBtn as ImageView).imageTintList = ColorStateList.valueOf(txtColor)
            }
            if (!showRemove) removeBtn.visibility = View.GONE else {
                removeBtn.setOnClickListener { v ->
                    window.dismiss()
                    methods.onRemove(v)
                }
            }
            editBtn.setOnClickListener { v ->
                window.dismiss()
                methods.onEdit(v)
            }
            window
        } else {
            color = -0xdad9d9
            txtColor = -0x1
            content = LayoutInflater.from(context).inflate(R.layout.app_long_press_menu, null)
            val window = PopupWindow(content, ListPopupWindow.WRAP_CONTENT, ListPopupWindow.WRAP_CONTENT, true)
            currentPopup = window
            window.setOnDismissListener { currentPopup = null }
            window.setBackgroundDrawable(ColorDrawable(0x0))
            content.findViewById<View>(R.id.appinfobtn).visibility = View.GONE
            content.findViewById<TextView>(R.id.title).text = item.label
            val removeBtn = content.findViewById<View>(R.id.removeBtn)
            val editbtn = content.findViewById<View>(R.id.editbtn)
            val bg = ShapeDrawable()
            val r = 18.dp
            bg.shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
            bg.paint.color = color
            content.findViewById<View>(R.id.bg).background = bg
            (removeBtn as TextView).setTextColor(txtColor)
            (editbtn as TextView).setTextColor(txtColor)
            removeBtn.setOnClickListener { v ->
                window.dismiss()
                methods.onRemove(v)
            }
            editbtn.setOnClickListener { v ->
                window.dismiss()
                methods.onEdit(v)
            }
            window
        }
    }

	fun dock(context: Context, app: App, i: Int) = View.OnLongClickListener { view ->
        if (currentPopup == null) {
            val location = IntArray(2)
            val icon = view.findViewById<View>(R.id.iconimg)
            icon.getLocationInWindow(location)
            val gravity = if (location[0] > Device.displayWidth / 2) Gravity.END else Gravity.START
            val x = if (location[0] > Device.displayWidth / 2) Device.displayWidth - location[0] - view.measuredWidth else location[0]
            val popupWindow = popupWindow(context, object : Methods {
                override fun onRemove(v: View) {
                    Dock[i] = null
                    Main.setDock()
                }
                override fun onEdit(v: View) { showAppEditDialog(context, app, v) }
            }, true, app)
            popupWindow.isFocusable = false
            if (!Settings["locked", false]) {
                val myShadow = View.DragShadowBuilder(icon)
                val clipData = ClipData.newPlainText(app.toString(), "")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    icon.startDragAndDrop(clipData, myShadow, view, View.DRAG_FLAG_OPAQUE or View.DRAG_FLAG_GLOBAL)
                } else {
                    icon.startDrag(clipData, myShadow, view, 0)
                }
                Dock[i] = null
            }
            var y = -view.y.toInt() + view.height * Settings["dock:rows", 1] + Tools.navbarHeight + (Settings["dockbottompadding", 10] + 12).dp.toInt()
            if (Settings["docksearchbarenabled", false] && Settings["dock:search:below_apps", true] && !context.isTablet) {
                y += 68.dp.toInt()
            }
            popupWindow.showAtLocation(view, Gravity.BOTTOM or gravity, x, y)
        }
        true
    }

    fun insideFolder(context: Context, app: App, i: Int, v: View?, folderI: Int, folderWindow: PopupWindow) = View.OnLongClickListener { view ->
        if (currentPopup == null) {
            val icon = view.findViewById<View>(R.id.iconimg)
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            val gravity = if (location[0] > Device.displayWidth / 2) Gravity.END else Gravity.START
            val x = if (location[0] > Device.displayWidth / 2) Device.displayWidth - location[0] - view.measuredWidth else location[0]
            val popupWindow = popupWindow(context, object : Methods {
                override fun onRemove(v: View) {
                    folderWindow.dismiss()
                    val f = Dock[i] as Folder
                    f.apps.removeAt(folderI)
                    Dock[i] = if (f.apps.size == 1) f.apps[0] else f
                    Main.setDock()
                }
                override fun onEdit(v: View) { showAppEditDialog(context, app, v) }
            }, true, app)

            if (!Settings["locked", false]) {
                val myShadow = View.DragShadowBuilder(icon)
                val clipData = ClipData.newPlainText(app.toString(), i.toString(16)).apply { addItem(ClipData.Item(folderI.toString(16))) }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    icon.startDragAndDrop(clipData, myShadow, view, View.DRAG_FLAG_OPAQUE or View.DRAG_FLAG_GLOBAL)
                } else {
                    icon.startDrag(clipData, myShadow, view, 0)
                }

                val f = Dock[i] as Folder
                f.apps.removeAt(folderI)
                Dock[i] = if (f.apps.size == 1) f.apps[0] else f
            }

            popupWindow.showAtLocation(v, Gravity.BOTTOM or gravity, x, Device.displayHeight - location[1] + Tools.navbarHeight)
        }
        true
    }

    fun olddrawer(context: Context) = OnItemLongClickListener { _, view, position, _ ->
        if (currentPopup == null) try {
            val app = Main.apps[position]
            val icon = view.findViewById<View>(R.id.iconimg)
            val location = IntArray(2)
            val popupWindow = popupWindow(context, object : Methods {
                override fun onRemove(v: View) {}
                override fun onEdit(v: View) { showAppEditDialog(context, app, v) }
            }, false, app)
            popupWindow.isFocusable = false
            icon.getLocationInWindow(location)
            if (!Settings["locked", false]) {
                val myShadow = View.DragShadowBuilder(icon)
                val data = ClipData.newPlainText(app.toString(), "")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    icon.startDragAndDrop(data, myShadow, view, View.DRAG_FLAG_OPAQUE or View.DRAG_FLAG_GLOBAL)
                } else {
                    icon.startDrag(data, myShadow, view, 0)
                }
            }
            val gravity = if (location[0] > Device.displayWidth / 2) Gravity.END else Gravity.START
            val x = if (location[0] > Device.displayWidth / 2) Device.displayWidth - location[0] - icon.measuredWidth else location[0]
            if (location[1] < Device.displayHeight / 2f) {
                popupWindow.showAtLocation(icon, Gravity.TOP or gravity, x, location[1] + icon.measuredHeight + 4.dp.toInt())
            } else popupWindow.showAtLocation(
                icon, Gravity.BOTTOM or gravity, x,
                Device.displayHeight - location[1] + 4.dp.toInt() + Tools.navbarHeight)
        } catch (e: Exception) { e.printStackTrace() }
        true
    }

    fun drawer(context: Context, app: App) = View.OnLongClickListener {
        if (currentPopup == null) try {
            val icon = it.findViewById<View>(R.id.iconimg)
            val location = IntArray(2)
            val popupWindow = popupWindow(context, object : Methods {
                override fun onRemove(v: View) {}
                override fun onEdit(v: View) { showAppEditDialog(context, app, v) }
            }, false, app)
            popupWindow.isFocusable = false
            icon.getLocationInWindow(location)
            if (!Settings["locked", false]) {
                val myShadow = View.DragShadowBuilder(icon)
                val data = ClipData.newPlainText(app.toString(), "")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    icon.startDragAndDrop(data, myShadow, it, View.DRAG_FLAG_OPAQUE or View.DRAG_FLAG_GLOBAL)
                } else {
                    icon.startDrag(data, myShadow, it, 0)
                }
            }
            val gravity = if (location[0] > Device.displayWidth / 2) Gravity.END else Gravity.START
            val x = if (location[0] > Device.displayWidth / 2) Device.displayWidth - location[0] - icon.measuredWidth else location[0]
            if (location[1] < Device.displayHeight / 2f) {
                popupWindow.showAtLocation(icon, Gravity.TOP or gravity, x, location[1] + icon.measuredHeight + 4.dp.toInt())
            } else {
                popupWindow.showAtLocation(icon, Gravity.BOTTOM or gravity, x, Device.displayHeight - location[1] + 4.dp.toInt() + Tools.navbarHeight)
            }
        } catch (e: Exception) { e.printStackTrace() }
        true
    }

    fun search(activity: Activity, apps: ArrayList<LauncherItem>) = OnItemLongClickListener { _, view, i, _ ->
        val app = apps[i]
        if (currentPopup == null && app is App) try {
            val icon = view.findViewById<View>(R.id.iconimg)
            val location = IntArray(2)
            val popupWindow = popupWindow(activity, object : Methods {
                override fun onRemove(v: View) {}
                override fun onEdit(v: View) {
                    showAppEditDialog(activity, app, v)
                }
            }, false, app)
            popupWindow.isFocusable = false
            icon.getLocationInWindow(location)
            if (!Settings["locked", false]) {
                val myShadow = View.DragShadowBuilder(icon)
                val data = ClipData.newPlainText(app.toString(), "")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    icon.startDragAndDrop(data, myShadow, view, View.DRAG_FLAG_OPAQUE or View.DRAG_FLAG_GLOBAL)
                } else {
                    icon.startDrag(data, myShadow, view, 0)
                }
            }
            val gravity = if (location[0] > Device.displayWidth / 2) Gravity.END else Gravity.START
            val x = if (location[0] > Device.displayWidth / 2) Device.displayWidth - location[0] - icon.measuredWidth else location[0]
            if (location[1] < Device.displayHeight / 2f) popupWindow.showAtLocation(
                    icon, Gravity.TOP or gravity, x, location[1] + icon.measuredHeight)
            else popupWindow.showAtLocation(
                    icon, Gravity.BOTTOM or gravity, x, Device.displayHeight - location[1] + 4.dp.toInt() + Tools.navbarHeight)
        } catch (e: Exception) { e.printStackTrace() }
        true
    }

	fun folder(context: Context, folder: Folder, i: Int) = View.OnLongClickListener { view ->
        if (currentPopup == null) {
            val location = IntArray(2)
            val icon = view.findViewById<View>(R.id.iconimg)
            icon.getLocationInWindow(location)
            val gravity = if (location[0] > Device.displayWidth / 2) Gravity.END else Gravity.START
            val x = if (location[0] > Device.displayWidth / 2) Device.displayWidth - location[0] - view.measuredWidth else location[0]
            val popupWindow = popupWindow(context, object : Methods {
                override fun onRemove(v: View) {
                    Dock[i] = null
                    Main.setDock()
                }
                override fun onEdit(v: View) {
                    val editContent = LayoutInflater.from(context).inflate(R.layout.app_edit_menu, null)
                    val editWindow = PopupWindow(editContent, ListPopupWindow.WRAP_CONTENT, ListPopupWindow.WRAP_CONTENT, true)
                    val editLabel = editContent.findViewById<EditText>(R.id.editlabel)
                    editContent.findViewById<ImageView>(R.id.iconimg).setImageDrawable(folder.icon)
                    editContent.findViewById<ImageView>(R.id.iconimg).setOnClickListener {
                        val intent = Intent(context, CustomAppIcon::class.java)
                        intent.putExtra("key", "folder:${folder.uid}:icon")
                        context.startActivity(intent)
                        editWindow.dismiss()
                    }
                    editLabel.setText(folder.label)
                    editWindow.setOnDismissListener {
                        folder.label = editLabel.text.toString().replace('\t', ' ')
                        Settings["folder:${folder.uid}:label"] = folder.label
                        Dock[i] = folder
                        Main.setDock()
                    }
                    editWindow.showAtLocation(v, Gravity.CENTER, 0, 0)
                }
            }, true, folder)

            if (!Settings["locked", false]) {
                val myShadow = View.DragShadowBuilder(icon)
                val clipData = ClipData.newPlainText(folder.toString(), "")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    icon.startDragAndDrop(clipData, myShadow, view, View.DRAG_FLAG_OPAQUE or View.DRAG_FLAG_GLOBAL)
                } else {
                    icon.startDrag(clipData, myShadow, view, 0)
                }

                Dock[i] = null
            }

            var y = (-view.y + view.height * Settings["dock:rows", 1] + Tools.navbarHeight + (Settings["dockbottompadding", 10] + 12).dp).toInt()
            if (Settings["docksearchbarenabled", false] && Settings["dock:search:below_apps", true] && !context.isTablet) {
                y += 68.dp.toInt()
            }

            popupWindow.showAtLocation(view, Gravity.BOTTOM or gravity, x, y)
        }
        true
    }

    private fun showAppEditDialog(context: Context, app: App, v: View) {
        val editContent = LayoutInflater.from(context).inflate(R.layout.app_edit_menu, null)
        val editWindow = PopupWindow(editContent, ListPopupWindow.WRAP_CONTENT, ListPopupWindow.WRAP_CONTENT, true)
        val editLabel = editContent.findViewById<EditText>(R.id.editlabel)
        editContent.findViewById<ImageView>(R.id.iconimg).setImageDrawable(app.icon)
        editContent.findViewById<View>(R.id.edit).backgroundTintList = ColorStateList.valueOf(Palette.from(app.icon!!.toBitmap()).generate().let { it.getDarkVibrantColor(it.getDarkMutedColor(0xff1155ff.toInt())) })
        editContent.findViewById<ImageView>(R.id.iconimg).setOnClickListener {
            val intent = Intent(context, CustomAppIcon::class.java)
            intent.putExtra("key", "app:$app:icon")
            context.startActivity(intent)
            editWindow.dismiss()
        }
        editLabel.setText(Settings[app.packageName + "/" + app.name + "?label", app.label!!])
        editWindow.setOnDismissListener {
            Settings[app.packageName + "/" + app.name + "?label"] = editLabel.text.toString().replace('\t', ' ')
            Main.shouldSetApps = true
            Main.setDock()
        }
        editWindow.showAtLocation(v, Gravity.CENTER, 0, 0)
    }

    internal interface Methods {
        fun onRemove(v: View)
        fun onEdit(v: View)
    }
}