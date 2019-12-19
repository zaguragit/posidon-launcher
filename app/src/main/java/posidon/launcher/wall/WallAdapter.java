package posidon.launcher.wall;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.palette.graphics.Palette;

import posidon.launcher.R;

class WallAdapter extends BaseAdapter {

    private final Context context;

    WallAdapter(Context context) { this.context = context; }

	@Override public int getCount() { return Gallery.walls.size(); }
    @Override public Object getItem(int position) { return null; }
    @Override public long getItemId(int position) { return 0; }

    static class ViewHolder {
        ImageView pic;
        TextView label;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = li.inflate(R.layout.wall_item, null);
            viewHolder = new ViewHolder();
            viewHolder.pic = convertView.findViewById(R.id.pic);
            viewHolder.label = convertView.findViewById(R.id.label);
            convertView.setTag(viewHolder);
        } else viewHolder = (ViewHolder)convertView.getTag();
        Wall wall = Gallery.walls.get(position);
        if (wall != null) {
            viewHolder.pic.setImageBitmap(wall.img);
            ShapeDrawable btnbg = new ShapeDrawable(new RectShape());
            try { btnbg.getPaint().setColor(0x00FFFFFF & Palette.from(wall.img).generate().getDarkMutedColor(context.getResources().getColor(R.color.walllabelbg)) | 0xaa000000); }
            catch (Exception e) { btnbg.getPaint().setColor(context.getResources().getColor(R.color.walllabelbg)); }
            viewHolder.label.setBackground(btnbg);
            viewHolder.label.setText(wall.name);
        }
        return convertView;
    }
}