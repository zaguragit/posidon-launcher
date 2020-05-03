package posidon.launcher.feed.news

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import posidon.launcher.LauncherMenu
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.feed.news.FeedAdapter.FeedModelViewHolder
import posidon.launcher.tools.Loader
import posidon.launcher.storage.Settings
import posidon.launcher.tools.ColorTools
import posidon.launcher.tools.Tools
import posidon.launcher.tools.dp
import posidon.launcher.view.SwipeableLayout
import java.util.*
import kotlin.collections.ArrayList

class FeedAdapter(private val feedModels: ArrayList<FeedItem>, private val context: Activity, private val window: Window) : RecyclerView.Adapter<FeedModelViewHolder>() {

    class FeedModelViewHolder(val card: View) : RecyclerView.ViewHolder(card)

    private val maxWidth = Settings["feed:max_img_width", Tools.getDisplayWidth(context)]

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): FeedModelViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(when(Settings["feed:card_layout", 0]) {
            1 -> R.layout.feed_card1
            2 -> R.layout.feed_card2
            else -> R.layout.feed_card0
        }, parent, false)
        v.findViewById<View>(R.id.card).setOnLongClickListener(LauncherMenu(context, window))
        v.findViewById<CardView>(R.id.card).setCardBackgroundColor(Settings["feed:card_bg", -0xdad9d9])
        v.findViewById<TextView>(R.id.source).setTextColor(Settings["feed:card_txt_color", -0x1])
        v.findViewById<TextView>(R.id.title).setTextColor(Settings["feed:card_txt_color", -0x1])
        return FeedModelViewHolder(FrameLayout(context).apply {
            val tp = 9.dp.toInt()
            setPadding(0, tp, 0, tp)
            addView(if (Settings["feed:delete_articles", false]) SwipeableLayout(v).apply {
                setIconColor(if (ColorTools.useDarkText(Main.accentColor)) 0xff000000.toInt() else 0xffffffff.toInt())
                setSwipeColor(Main.accentColor and 0xffffff or 0xdd000000.toInt())
                radius = Settings["feed:card_radius", 15].dp
                id = R.id.separator
            } else v.apply {
                findViewById<CardView>(R.id.card).radius = Settings["feed:card_radius", 15].dp
            })
        })
    }

    override fun onBindViewHolder(holder: FeedModelViewHolder, i: Int) {
        val feedItem = feedModels[i]
        holder.card.findViewById<TextView>(R.id.title).text = feedItem.title
        holder.card.findViewById<TextView>(R.id.source).text = feedItem.source.name
        if (Settings["feed:delete_articles", false]) {
            val swipeableLayout = holder.card.findViewById<SwipeableLayout>(R.id.separator)
            swipeableLayout.reset()
            swipeableLayout.onSwipeAway = {
                feedModels.remove(feedItem)
                notifyItemRemoved(i)
                notifyItemRangeChanged(i, feedModels.size - i)
                val day = Calendar.getInstance()[Calendar.DAY_OF_YEAR]
                Settings.getStrings("feed:deleted_articles").add("$day:" + feedItem.link + ':' + feedItem.title)
                Settings.apply()
            }
        }
        if (Settings["feed:card_img_enabled", true] && feedItem.img != null) {
            if (images.containsKey(feedItem.img)) {
                holder.card.findViewById<ImageView>(R.id.img).setImageBitmap(images[feedItem.img])
                if (Settings["feed:card_text_shadow", true]) {
                    val gradientDrawable = GradientDrawable()
                    gradientDrawable.colors = intArrayOf(0x0, Palette.from(images[feedItem.img]!!).generate().getDarkMutedColor(-0x1000000))
                    holder.card.findViewById<View>(R.id.source).backgroundTintList = ColorStateList.valueOf(Palette.from(images[feedItem.img]!!).generate().getDarkMutedColor(-0xdad9d9) and 0x00ffffff or -0x78000000)
                    holder.card.findViewById<View>(R.id.gradient).background = gradientDrawable
                } else holder.card.findViewById<View>(R.id.gradient).visibility = View.GONE
            } else {
                fun onImageLoadEnd(img: Bitmap) {
                    if (Settings["feed:card_text_shadow", true]) {
                        val gradientDrawable = GradientDrawable()
                        gradientDrawable.colors = intArrayOf(0x0, Palette.from(img).generate().getDarkMutedColor(-0x1000000))
                        holder.card.findViewById<View>(R.id.source).backgroundTintList = ColorStateList.valueOf(Palette.from(img).generate().getDarkVibrantColor(-0xdad9d9) and 0x00ffffff or -0x78000000)
                        holder.card.findViewById<View>(R.id.gradient).background = gradientDrawable
                    } else holder.card.findViewById<View>(R.id.gradient).visibility = View.GONE
                }
                Loader.nullableBitmap(feedItem.img, maxWidth, Loader.bitmap.AUTO, false) {
                    var worked = false
                    if (it != null) try {
                        images[feedItem.img] = it
                        holder.card.findViewById<ImageView>(R.id.img).setImageBitmap(images[feedItem.img])
                        onImageLoadEnd(it)
                        worked = true
                    } catch (e: Exception) { e.printStackTrace() }
                    if (!worked) Loader.nullableBitmap(feedItem.source.domain + '/' + feedItem.img, maxWidth, Loader.bitmap.AUTO, false) {
                        if (it != null) try {
                            images[feedItem.img] = it
                            holder.card.findViewById<ImageView>(R.id.img).setImageBitmap(images[feedItem.img])
                            onImageLoadEnd(it)
                            worked = true
                        } catch (e: Exception) { e.printStackTrace() }
                    }.execute()
                }.execute()
            }
        } else {
            holder.card.findViewById<View>(R.id.img).visibility = View.GONE
            holder.card.findViewById<View>(R.id.card).layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            holder.card.findViewById<View>(R.id.txt).layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            holder.card.findViewById<View>(R.id.gradient).layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        holder.card.findViewById<View>(R.id.card).setOnClickListener {
            try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(feedItem.link.trim { it <= ' ' })), ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slideup, R.anim.home_exit).toBundle()) }
            catch (e: Exception) { e.printStackTrace() }
        }
    }


    override fun getItemCount() = feedModels.size

    companion object {
        private val images: MutableMap<String?, Bitmap?> = HashMap()
    }
}