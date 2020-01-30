package posidon.launcher.items

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import posidon.launcher.R
import posidon.launcher.tools.Settings

class DrawerAdapter(private val context: Context, private val apps: Array<App?>) : BaseAdapter() {
    override fun getCount(): Int {
        return apps.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    internal class ViewHolder {
        var icon: ImageView? = null
        var text: TextView? = null
    }

    override fun getView(position: Int, cv: View?, parent: ViewGroup): View? {
        var convertView = cv
        val viewHolder: ViewHolder
        val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (convertView == null) {
            if (Settings["drawer:columns", 4] > 2) convertView = li.inflate(R.layout.drawer_item, null) else {
                convertView = li.inflate(R.layout.list_item, null)
                if (Settings["drawer:columns", 4] == 2) (convertView.findViewById<View>(R.id.icontxt) as TextView).textSize = 18f
            }
            viewHolder = ViewHolder()
            viewHolder.icon = convertView.findViewById(R.id.iconimg)
            viewHolder.text = convertView.findViewById(R.id.icontxt)
            convertView.tag = viewHolder
        } else viewHolder = convertView.tag as ViewHolder
        viewHolder.icon!!.setImageDrawable(apps[position]!!.icon)
        if (Settings["labelsenabled", false]) {
            viewHolder.text!!.text = apps[position]!!.label
            viewHolder.text!!.visibility = View.VISIBLE
            viewHolder.text!!.setTextColor(Settings["labelColor", -0x11111112])
        } else viewHolder.text!!.visibility = View.INVISIBLE
        var appSize = 0
        when (Settings["icsize", 1]) {
            0 -> appSize = (context.resources.displayMetrics.density * 64).toInt()
            1 -> appSize = (context.resources.displayMetrics.density * 74).toInt()
            2 -> appSize = (context.resources.displayMetrics.density * 84).toInt()
        }
        viewHolder.icon!!.layoutParams.height = appSize
        viewHolder.icon!!.layoutParams.width = appSize
        return convertView

        /*

        final ViewHolder viewHolder;
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            if (Settings.getInt("drawer:columns", 4) > 2) convertView = li.inflate(R.layout.drawer_item, null);
            else {
                convertView = li.inflate(R.layout.list_item, null);
                if (Settings.getInt("drawer:columns", 4) == 2) ((TextView)convertView.findViewById(R.id.icontxt)).setTextSize(18);
            }
            viewHolder = new ViewHolder();
            viewHolder.icon = convertView.findViewById(R.id.iconimg);
            viewHolder.text = convertView.findViewById(R.id.icontxt);
            convertView.setTag(viewHolder);
        } else viewHolder = (ViewHolder)convertView.getTag();

        viewHolder.icon.setImageDrawable(apps[position].icon);
        if (Settings.getBool("labelsenabled", false)) {
            viewHolder.text.setText(apps[position].label);
            viewHolder.text.setVisibility(View.VISIBLE);
            viewHolder.text.setTextColor(Settings.getInt("labelColor", 0xeeeeeeee));
        } else viewHolder.text.setVisibility(View.INVISIBLE);
        int appsize = 0;
        switch (Settings.getInt("icsize", 1)) {
            case 0: appsize = (int) (context.getResources().getDisplayMetrics().density * 64); break;
            case 1: appsize = (int) (context.getResources().getDisplayMetrics().density * 74); break;
            case 2: appsize = (int) (context.getResources().getDisplayMetrics().density * 84); break;
        }
        viewHolder.icon.getLayoutParams().height = appsize;
        viewHolder.icon.getLayoutParams().width = appsize;
        return convertView;

        */
    }

}