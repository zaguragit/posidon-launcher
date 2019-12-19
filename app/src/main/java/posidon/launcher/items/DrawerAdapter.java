package posidon.launcher.items;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import posidon.launcher.R;
import posidon.launcher.tools.Settings;

public class DrawerAdapter extends BaseAdapter {

    private final Context context;
    private final App[] apps;

    public DrawerAdapter(Context context, App[] apps){
        this.context = context;
        this.apps = apps;
    }

	@Override
    public int getCount() {
        return apps.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    static class ViewHolder {
        ImageView icon;
        TextView text;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            if (Settings.getInt("numcolumns", 4) > 2) convertView = li.inflate(R.layout.drawer_item, null);
            else {
                convertView = li.inflate(R.layout.list_item, null);
                if (Settings.getInt("numcolumns", 4) == 2) ((TextView)convertView.findViewById(R.id.icontxt)).setTextSize(18);
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
    }
}