package posidon.launcher.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import posidon.launcher.items.App;
import posidon.launcher.R;
import posidon.launcher.tools.Settings;

class SearchAdapter extends BaseAdapter {

    private final Context context;
    private final App[] results;

    SearchAdapter(Context context, App[] results) {
        this.context = context;
        this.results = results;
    }

	@Override public int getCount() { return results.length; }
    @Override public Object getItem(int position) { return null; }
    @Override public long getItemId(int position) { return 0; }
    @Override public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

        ImageView icon = convertView.findViewById(R.id.iconimg);
        TextView text = convertView.findViewById(R.id.icontxt);

        icon.setImageDrawable(results[position].icon);
        text.setText(results[position].label);
        text.setTextColor(Settings.getInt("searchtxtcolor", 0xFFFFFFFF));
        int appSize = 0;
        switch (Settings.getInt("icsize", 1)) {
            case 0:
                appSize = (int) (context.getResources().getDisplayMetrics().density * 64);
                break;
            case 1:
                appSize = (int) (context.getResources().getDisplayMetrics().density * 72);
                break;
            case 2:
                appSize = (int) (context.getResources().getDisplayMetrics().density * 96);
                break;
        }
        icon.getLayoutParams().height = appSize;
        icon.getLayoutParams().width = appSize;
        return convertView;
    }
}