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
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.tools.ColorTools
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.toBitmap
import posidon.launcher.tools.vibrate
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
                val r = 18 * context.resources.displayMetrics.density
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
            val r = 18 * context.resources.displayMetrics.density
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
            if (!showRemove) removeBtn.visibility = GONE else {
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
            window.setBackgroundDrawable(ColorDrawable(0x0))
            content.findViewById<View>(R.id.appinfobtn).visibility = View.GONE
            content.findViewById<TextView>(R.id.title).text = item.label
            val removeBtn = content.findViewById<View>(R.id.removeBtn)
            val editbtn = content.findViewById<View>(R.id.editbtn)
            val bg = ShapeDrawable()
            val r = 18 * context.resources.displayMetrics.density
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

	fun dock(context: Context, app: App, i: Int) = OnLongClickListener { view ->
        if (currentPopup == null) {
            val location = IntArray(2)
            val icon = view.findViewById<View>(R.id.iconimg)
            icon.getLocationInWindow(location)
            val gravity = if (location[0] > Tools.getDisplayWidth(context) / 2) Gravity.END else Gravity.START
            val x = if (location[0] > Tools.getDisplayWidth(context) / 2) Tools.getDisplayWidth(context) - location[0] - view.measuredWidth else location[0]
            val popupWindow = popupWindow(context, object : Methods {
                override fun onRemove(v: View) {
                    var data = Settings["dock", ""].split("\n").toTypedArray()
                    if (data.size <= i) data = Arrays.copyOf(data, i + 1)
                    data[i] = ""
                    Settings["dock"] = TextUtils.join("\n", data)
                    Main.setDock()
                }
                override fun onEdit(v: View) { showAppEditDialog(context, app, v) }
            }, true, app)
            popupWindow.isFocusable = false
            val myShadow = DragShadowBuilder(icon)
            val clipData = ClipData.newPlainText("", "")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) icon.startDragAndDrop(clipData, myShadow, arrayOf(app, view, popupWindow), 0) else icon.startDrag(clipData, myShadow, arrayOf(app, view, popupWindow), 0)
            var data = Settings["dock", ""].split("\n").toTypedArray()
            if (data.size <= i) data = Arrays.copyOf(data, i + 1)
            data[i] = ""
            Settings["dock"] = TextUtils.join("\n", data)
            popupWindow.showAtLocation(
                    view, Gravity.BOTTOM or gravity, x,
                    (-view.y + view.height * Settings["dock:rows", 1] + Tools.navbarHeight + (Settings["dockbottompadding", 10] + 12) * context.resources.displayMetrics.density).toInt()
            )
        }
        true
    }

	fun insideFolder(context: Context, app: App, i: Int, v: View?, folderIndex: Int, folderWindow: PopupWindow) = OnLongClickListener { view ->
        if (currentPopup == null) {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            val gravity = if (location[0] > Tools.getDisplayWidth(context) / 2) Gravity.END else Gravity.START
            val x = if (location[0] > Tools.getDisplayWidth(context) / 2) Tools.getDisplayWidth(context) - location[0] - view.measuredWidth else location[0]
            popupWindow(context, object : Methods {
                override fun onRemove(v: View) {
                    folderWindow.dismiss()
                    var data = Settings["dock", ""].split("\n").toTypedArray()
                    if (data.size <= i) data = Arrays.copyOf(data, i + 1)
                    val f = Folder(context, data[i])
                    f.apps.removeAt(folderIndex)
                    data[i] = if (f.apps.size == 1) f.apps[0]!!.packageName + "/" + f.apps[0]!!.name else f.toString()
                    Settings["dock"] = TextUtils.join("\n", data)
                    Main.setDock()
                }
                override fun onEdit(v: View) { showAppEditDialog(context, app, v) }
            }, true, app).showAtLocation(v, Gravity.BOTTOM or gravity, x, Tools.getDisplayHeight(context) - location[1] + Tools.navbarHeight)
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
                override fun onEdit(v: View) { showAppEditDialog(context, app!!, v) }
            }, false, app!!)
            popupWindow.isFocusable = false
            icon.getLocationInWindow(location)
            val myShadow = DragShadowBuilder(icon)
            val data = ClipData.newPlainText("", "")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) icon.startDragAndDrop(data, myShadow, arrayOf(app, view, popupWindow), 0) else icon.startDrag(data, myShadow, arrayOf(app, view, popupWindow), 0)
            val gravity = if (location[0] > Tools.getDisplayWidth(context) / 2) Gravity.END else Gravity.START
            val x = if (location[0] > Tools.getDisplayWidth(context) / 2) Tools.getDisplayWidth(context) - location[0] - icon.measuredWidth else location[0]
            if (location[1] < Tools.getDisplayHeight(context) / 2f) popupWindow.showAtLocation(icon, Gravity.TOP or gravity, x, location[1] + icon.measuredHeight) else popupWindow.showAtLocation(
                    icon, Gravity.BOTTOM or gravity, x,
                    context.resources.displayMetrics.heightPixels - location[1] + (4 * context.resources.displayMetrics.density).toInt() + Tools.navbarHeight
            )
        } catch (ignore: Exception) {}
        true
    }

    fun drawer(context: Context, app: App) = OnLongClickListener {
        if (currentPopup == null) try {
            val icon = it.findViewById<View>(R.id.iconimg)
            val location = IntArray(2)
            val popupWindow = popupWindow(context, object : Methods {
                override fun onRemove(v: View) {}
                override fun onEdit(v: View) { showAppEditDialog(context, app, v) }
            }, false, app)
            popupWindow.isFocusable = false
            icon.getLocationInWindow(location)
            val myShadow = DragShadowBuilder(icon)
            val data = ClipData.newPlainText("", "")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) icon.startDragAndDrop(data, myShadow, arrayOf(app, it, popupWindow), 0) else icon.startDrag(data, myShadow, arrayOf(app, it, popupWindow), 0)
            val gravity = if (location[0] > Tools.getDisplayWidth(context) / 2) Gravity.END else Gravity.START
            val x = if (location[0] > Tools.getDisplayWidth(context) / 2) Tools.getDisplayWidth(context) - location[0] - icon.measuredWidth else location[0]
            if (location[1] < Tools.getDisplayHeight(context) / 2f) popupWindow.showAtLocation(icon, Gravity.TOP or gravity, x, location[1] + icon.measuredHeight) else popupWindow.showAtLocation(
                    icon, Gravity.BOTTOM or gravity, x,
                    context.resources.displayMetrics.heightPixels - location[1] + (4 * context.resources.displayMetrics.density).toInt() + Tools.navbarHeight
            )
        } catch (ignore: Exception) {}
        true
    }

    fun search(activity: Activity, apps: Array<App?>) = OnItemLongClickListener { _, view, position, _ ->
        if (currentPopup == null) try {
            val app = apps[position]!!
            val icon = view.findViewById<View>(R.id.iconimg)
            val location = IntArray(2)
            val popupWindow = popupWindow(activity, object : Methods {
                override fun onRemove(v: View) {}
                override fun onEdit(v: View) {
                    showAppEditDialog(activity, app, v)
                }
            }, false, app)
            //popupWindow.isFocusable = false
            icon.getLocationInWindow(location)
            /*val myShadow = DragShadowBuilder(icon)
            val data = ClipData.newPlainText("", "")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) icon.startDragAndDrop(data, myShadow, arrayOf(app, view, popupWindow), 0)
            else icon.startDrag(data, myShadow, arrayOf(app, view, popupWindow), 0)*/
            val gravity = if (location[0] > Tools.getDisplayWidth(activity) / 2) Gravity.END else Gravity.START
            val x = if (location[0] > Tools.getDisplayWidth(activity) / 2) Tools.getDisplayWidth(activity) - location[0] - icon.measuredWidth else location[0]
            if (location[1] < Tools.getDisplayHeight(activity) / 2f) popupWindow.showAtLocation(
                        icon, Gravity.TOP or gravity, x,
                        location[1] + icon.measuredHeight
            ) else popupWindow.showAtLocation(
                    icon, Gravity.BOTTOM or gravity, x,
                    Tools.getDisplayHeight(activity) - location[1] + (4 * activity.resources.displayMetrics.density).toInt() + Tools.navbarHeight
            )
        } catch (e: Exception) { e.printStackTrace() }
        true
    }

	fun folder(context: Context, folder: Folder, i: Int) = OnLongClickListener { view ->
        if (currentPopup == null) {
            val location = IntArray(2)
            val icon = view.findViewById<View>(R.id.iconimg)
            icon.getLocationInWindow(location)
            val gravity = if (location[0] > Tools.getDisplayWidth(context) / 2) Gravity.END else Gravity.START
            val x = if (location[0] > Tools.getDisplayWidth(context) / 2) Tools.getDisplayWidth(context) - location[0] - view.measuredWidth else location[0]
            val popupWindow = popupWindow(context, object : Methods {
                override fun onRemove(v: View) {
                    var data = Settings["dock", ""].split("\n").toTypedArray()
                    if (data.size <= i) data = Arrays.copyOf(data, i + 1)
                    data[i] = ""
                    Settings["dock"] = TextUtils.join("\n", data)
                    Main.setDock()
                }

                override fun onEdit(v: View) {
                    val editContent = LayoutInflater.from(context).inflate(R.layout.app_edit_menu, null)
                    val editWindow = PopupWindow(editContent, ListPopupWindow.WRAP_CONTENT, ListPopupWindow.WRAP_CONTENT, true)
                    val editLabel = editContent.findViewById<EditText>(R.id.editlabel)
                    editContent.findViewById<ImageView>(R.id.iconimg).setImageDrawable(folder.icon)
                    editContent.findViewById<View>(R.id.edit).visibility = GONE
                    editLabel.setText(folder.label)
                    editWindow.setOnDismissListener {
                        folder.label = editLabel.text.toString().replace('\n', ' ').replace('¬', ' ')
                        val data = Settings["dock", ""].split("\n").toTypedArray()
                        data[i] = folder.toString()
                        Settings["dock"] = TextUtils.join("\n", data)
                        Main.setDock()
                    }
                    editWindow.showAtLocation(v, Gravity.CENTER, 0, 0)
                }
            }, true, folder)
            val myShadow = DragShadowBuilder(icon)
            val clipData = ClipData.newPlainText("", "")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) icon.startDragAndDrop(clipData, myShadow, arrayOf(folder, view, popupWindow), 0) else icon.startDrag(clipData, myShadow, arrayOf(folder, view, popupWindow), 0)
            var data = Settings["dock", ""].split("\n").toTypedArray()
            if (data.size <= i) data = Arrays.copyOf(data, i + 1)
            data[i] = ""
            Settings["dock"] = TextUtils.join("\n", data)
            popupWindow.showAtLocation(
                    view, Gravity.BOTTOM or gravity, x,
                    (-view.y + view.height * Settings["dock:rows", 1] + Tools.navbarHeight + (Settings["dockbottompadding", 10] + 12) * context.resources.displayMetrics.density).toInt()
            )
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
            intent.putExtra("packageName", app.packageName)
            context.startActivity(intent)
            editWindow.dismiss()
        }
        editLabel.setText(Settings[app.packageName + "/" + app.name + "?label", app.label!!])
        editWindow.setOnDismissListener {
            Settings[app.packageName + "/" + app.name + "?label"] = editLabel.text.toString().replace('\n', ' ').replace('¬', ' ')
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