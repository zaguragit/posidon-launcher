package posidon.launcher.feed.news

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import posidon.launcher.LauncherMenu
import posidon.launcher.R
import posidon.launcher.feed.news.FeedAdapter.FeedModelViewHolder
import posidon.launcher.tools.Loader
import posidon.launcher.tools.Settings
import posidon.launcher.tools.Tools
import java.util.*

class FeedAdapter(private val FeedModels: List<FeedItem>, private val context: Activity, private val window: Window) : RecyclerView.Adapter<FeedModelViewHolder>() {

    class FeedModelViewHolder(val rssFeedView: View) : RecyclerView.ViewHolder(rssFeedView)

    private val maxWidth = Settings.get("feed:max_img_width", Tools.getDisplayWidth(context))

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): FeedModelViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(when(Settings.get("feed:card_layout", 0)) {
            1 -> R.layout.feed_card1
            2 -> R.layout.feed_card2
            else -> R.layout.feed_card0
        }, parent, false)
        return FeedModelViewHolder(v)
    }

    override fun onBindViewHolder(holder: FeedModelViewHolder, position: Int) {

        val feedItem = FeedModels[position]
        holder.rssFeedView.findViewById<TextView>(R.id.title).text = feedItem.title
        holder.rssFeedView.findViewById<TextView>(R.id.title).setTextColor(Settings["feed:card_txt_color", -0x1])
        if (feedItem.source.name != null) holder.rssFeedView.findViewById<TextView>(R.id.source).text = feedItem.source.name
        holder.rssFeedView.findViewById<TextView>(R.id.source).setTextColor(Settings["feed:card_txt_color", -0x1])
        if (Settings["feed:card_img_enabled", true] && feedItem.img != null) {
            if (images.containsKey(feedItem.img)) {
                holder.rssFeedView.findViewById<ImageView>(R.id.img).setImageBitmap(images[feedItem.img])
                if (Settings["feed:card_text_shadow", true]) {
                    val gradientDrawable = GradientDrawable()
                    gradientDrawable.colors = intArrayOf(0x0, Palette.from(images[feedItem.img]!!).generate().getDarkMutedColor(-0x1000000))
                    holder.rssFeedView.findViewById<View>(R.id.source).backgroundTintList = ColorStateList.valueOf(Palette.from(images[feedItem.img]!!).generate().getDarkMutedColor(-0xdad9d9) and 0x00ffffff or -0x78000000)
                    holder.rssFeedView.findViewById<View>(R.id.gradient).background = gradientDrawable
                } else holder.rssFeedView.findViewById<View>(R.id.gradient).visibility = View.GONE
            } else {
                fun onImageLoadEnd(img: Bitmap) {
                    if (Settings["feed:card_text_shadow", true]) {
                        val gradientDrawable = GradientDrawable()
                        gradientDrawable.colors = intArrayOf(0x0, Palette.from(img).generate().getDarkMutedColor(-0x1000000))
                        holder.rssFeedView.findViewById<View>(R.id.source).backgroundTintList = ColorStateList.valueOf(Palette.from(img).generate().getDarkVibrantColor(-0xdad9d9) and 0x00ffffff or -0x78000000)
                        holder.rssFeedView.findViewById<View>(R.id.gradient).background = gradientDrawable
                    } else holder.rssFeedView.findViewById<View>(R.id.gradient).visibility = View.GONE
                }
                Loader.nullableBitmap(feedItem.img, maxWidth, Loader.bitmap.AUTO, false) {
                    var worked = false
                    if (it != null) try {
                        images[feedItem.img] = it
                        holder.rssFeedView.findViewById<ImageView>(R.id.img).setImageBitmap(images[feedItem.img])
                        onImageLoadEnd(it)
                        worked = true
                    } catch (e: Exception) { e.printStackTrace() }
                    if (!worked) Loader.nullableBitmap(feedItem.source.domain + '/' + feedItem.img, maxWidth, Loader.bitmap.AUTO, false) {
                        if (it != null) try {
                            images[feedItem.img] = it
                            holder.rssFeedView.findViewById<ImageView>(R.id.img).setImageBitmap(images[feedItem.img])
                            onImageLoadEnd(it)
                            worked = true
                        } catch (e: Exception) { e.printStackTrace() }
                    }.execute()
                }.execute()
            }
        } else {
            holder.rssFeedView.findViewById<View>(R.id.img).visibility = View.GONE
            holder.rssFeedView.findViewById<View>(R.id.card).layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            holder.rssFeedView.findViewById<View>(R.id.txt).layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            holder.rssFeedView.findViewById<View>(R.id.gradient).layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        holder.rssFeedView.findViewById<View>(R.id.card).setOnClickListener {
            try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(feedItem.link.trim { it <= ' ' })), ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slideup, R.anim.home_exit).toBundle()) }
            catch (e: Exception) { e.printStackTrace() }
        }
        holder.rssFeedView.findViewById<View>(R.id.card).setOnLongClickListener(LauncherMenu(context, window))
        holder.rssFeedView.findViewById<CardView>(R.id.card).radius = context.resources.displayMetrics.density * Settings.get("feed:card_radius", 15)
        holder.rssFeedView.findViewById<CardView>(R.id.card).setCardBackgroundColor(Settings.get("feed:card_bg", -0xdad9d9))
    }


    override fun getItemCount(): Int {
        return FeedModels.size
    }

    companion object {
        private val images: MutableMap<String?, Bitmap?> = HashMap()
    }
}