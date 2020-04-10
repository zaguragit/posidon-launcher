package posidon.launcher.wall

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.palette.graphics.Palette
import posidon.launcher.R

internal class WallAdapter(private val context: Context) : BaseAdapter() {

    override fun getCount(): Int = Gallery.walls!!.size
    override fun getItem(position: Int): Any? = null
    override fun getItemId(position: Int): Long = 0

    internal class ViewHolder {
        var pic: ImageView? = null
        var label: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        val viewHolder: ViewHolder
        val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (convertView == null) {
            convertView = li.inflate(R.layout.wall_item, null)
            viewHolder = ViewHolder()
            viewHolder.pic = convertView.findViewById(R.id.pic)
            viewHolder.label = convertView.findViewById(R.id.label)
            convertView.tag = viewHolder
        } else viewHolder = convertView.tag as ViewHolder
        val wall: Wall = Gallery.walls[position]
        viewHolder.pic!!.setImageBitmap(wall.img)
        val btnbg = ShapeDrawable(RectShape())
        try { btnbg.paint.color = 0x00FFFFFF and Palette.from(wall.img!!).generate().getDarkMutedColor(context.resources.getColor(R.color.walllabelbg)) or -0x56000000 }
        catch (e: Exception) { btnbg.paint.color = context.resources.getColor(R.color.walllabelbg) }
        viewHolder.label!!.background = btnbg
        viewHolder.label!!.text = wall.name
        return convertView
    }
}