package posidon.launcher.wall

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import posidon.launcher.R
import posidon.launcher.wall.WallActivity

internal class GalleryItemClickListener(private val context: Context) : OnItemClickListener {
    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        val i = Intent(context, WallActivity::class.java)
        i.putExtra("index", position)
        WallActivity.img = Gallery.Companion.walls!!.get(position).img
        context.startActivity(i, ActivityOptions.makeCustomAnimation(context, R.anim.slideup, R.anim.slidedown).toBundle())
        System.gc()
    }

}