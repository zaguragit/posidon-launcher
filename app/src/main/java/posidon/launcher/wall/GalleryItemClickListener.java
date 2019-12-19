package posidon.launcher.wall;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import posidon.launcher.R;

class GalleryItemClickListener implements AdapterView.OnItemClickListener {

    private final Context context;

    GalleryItemClickListener(Context context) { this.context = context; }

    @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent i = new Intent(context, WallActivity.class);
        i.putExtra("index", position);
        WallActivity.img = Gallery.walls.get(position).img;
        context.startActivity(i, ActivityOptions.makeCustomAnimation(context, R.anim.slideup, R.anim.slidedown).toBundle());
        System.gc();
    }
}
