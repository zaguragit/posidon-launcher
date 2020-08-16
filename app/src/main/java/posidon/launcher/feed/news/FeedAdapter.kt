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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import posidon.launcher.LauncherMenu
import posidon.launcher.R
import posidon.launcher.feed.news.FeedAdapter.FeedModelViewHolder
import posidon.launcher.feed.news.readers.ArticleActivity
import posidon.launcher.feed.news.readers.WebViewActivity
import posidon.launcher.storage.Settings
import posidon.launcher.tools.ColorTools
import posidon.launcher.tools.Device
import posidon.launcher.tools.Loader
import posidon.launcher.tools.dp
import posidon.launcher.view.SwipeableLayout
import java.util.*

class FeedAdapter(private val feedModels: ArrayList<FeedItem>, private val context: Activity) : RecyclerView.Adapter<FeedModelViewHolder>() {

    class FeedModelViewHolder(
        val card: View,
        val title: TextView,
        val source: TextView,
        val image: ImageView,
        val swipeableLayout: SwipeableLayout?
    ) : RecyclerView.ViewHolder(card)

    private val maxWidth = Settings["feed:max_img_width", Device.displayWidth]

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): FeedModelViewHolder {
        val v = LayoutInflater.from(context).inflate(when(Settings["feed:card_layout", 0]) {
            1 -> R.layout.feed_card1
            2 -> R.layout.feed_card2
            else -> R.layout.feed_card0
        }, parent, false)
        val title = v.findViewById<TextView>(R.id.title)
        val source = v.findViewById<TextView>(R.id.source)
        v.findViewById<View>(R.id.card).setOnLongClickListener(LauncherMenu())
        v.findViewById<CardView>(R.id.card).setCardBackgroundColor(Settings["feed:card_bg", -0xdad9d9])
        source.setTextColor(Settings["feed:card_txt_color", -0x1])
        title.setTextColor(Settings["feed:card_txt_color", -0x1])
        var swipeableLayout: SwipeableLayout? = null
        return FeedModelViewHolder(FrameLayout(context).apply {
            val tp = Settings["feed:card_margin_y", 9].dp.toInt()
            setPadding(0, tp, 0, tp)
            addView(if (Settings["feed:delete_articles", false]) SwipeableLayout(v).apply {
                val bg = Settings["notif:card_swipe_bg_color", 0x880d0e0f.toInt()]
                setIconColor(if (ColorTools.useDarkText(bg)) 0xff000000.toInt() else 0xffffffff.toInt())
                setSwipeColor(bg)
                radius = Settings["feed:card_radius", 15].dp
                v.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                swipeableLayout = this
            } else v.apply {
                findViewById<CardView>(R.id.card).radius = Settings["feed:card_radius", 15].dp
            })
        }, title, source, v.findViewById(R.id.img), swipeableLayout)
    }

    override fun onBindViewHolder(holder: FeedModelViewHolder, i: Int) {
        holder.card.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT)
        val feedItem = feedModels[i]
        holder.title.text = feedItem.title
        holder.source.text = feedItem.source.name
        if (Settings["feed:delete_articles", false]) {
            val swipeableLayout = holder.swipeableLayout!!
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
                holder.image.setImageBitmap(images[feedItem.img])
                if (Settings["feed:card_text_shadow", true]) {
                    Palette.from(images[feedItem.img]!!).generate {
                        val gradientDrawable = GradientDrawable()
                        if (it == null) {
                            gradientDrawable.colors = intArrayOf(0x0, -0x1000000)
                            holder.source.backgroundTintList = ColorStateList.valueOf(-0xdad9d9 and 0x00ffffff or -0x78000000)
                        } else {
                            gradientDrawable.colors = intArrayOf(0x0, it.getDarkMutedColor(-0x1000000))
                            holder.source.backgroundTintList = ColorStateList.valueOf(it.getDarkMutedColor(-0xdad9d9) and 0x00ffffff or -0x78000000)
                        }
                        holder.card.findViewById<View>(R.id.gradient).background = gradientDrawable
                    }
                } else holder.card.findViewById<View>(R.id.gradient).visibility = View.GONE
            } else {
                holder.image.setImageDrawable(null)
                fun onImageLoadEnd(img: Bitmap) {
                    images[feedItem.img] = img
                    holder.image.setImageBitmap(img)
                    if (Settings["feed:card_text_shadow", true]) {
                        Palette.from(img).generate {
                            val gradientDrawable = GradientDrawable()
                            if (it == null) {
                                gradientDrawable.colors = intArrayOf(0x0, -0x1000000)
                                holder.source.backgroundTintList = ColorStateList.valueOf(-0xdad9d9 and 0x00ffffff or -0x78000000)
                            } else {
                                gradientDrawable.colors = intArrayOf(0x0, it.getDarkMutedColor(-0x1000000))
                                holder.source.backgroundTintList = ColorStateList.valueOf(it.getDarkMutedColor(-0xdad9d9) and 0x00ffffff or -0x78000000)
                            }
                            holder.card.findViewById<View>(R.id.gradient).background = gradientDrawable
                        }
                    } else holder.card.findViewById<View>(R.id.gradient).visibility = View.GONE
                }
                Loader.NullableBitmap(feedItem.img, maxWidth, Loader.Bitmap.AUTO, false) {
                    var worked = false
                    if (it != null) try {
                        onImageLoadEnd(it)
                        worked = true
                    } catch (e: Exception) { e.printStackTrace() }
                    if (!worked) {
                        Loader.NullableBitmap(feedItem.source.domain + '/' + feedItem.img, maxWidth, Loader.Bitmap.AUTO, false) {
                            if (it != null) try {
                                onImageLoadEnd(it)
                                worked = true
                            } catch (e: Exception) { e.printStackTrace() }
                        }.execute()
                    }
                }.execute()
            }
        } else {
            holder.image.visibility = View.GONE
            holder.card.findViewById<View>(R.id.card).layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            holder.card.findViewById<View>(R.id.txt).layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            holder.card.findViewById<View>(R.id.gradient).layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        holder.card.findViewById<View>(R.id.card).setOnClickListener {
            try {
                val activityOptions = ActivityOptionsCompat.makeCustomAnimation(
                    context,
                    R.anim.slideup,
                    R.anim.home_exit
                ).toBundle()

                when (Settings["feed:openLinks", "browse"]) {
                    "webView" -> context.startActivity(Intent(context, WebViewActivity::class.java).apply {
                        putExtra("url", feedItem.link)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                    }, activityOptions)
                    "app" -> context.startActivity(Intent(context, ArticleActivity::class.java).apply {
                        putExtra("url", feedItem.link)
                        putExtra("sourceName", feedItem.source.name)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                    }, activityOptions)
                    else -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(feedItem.link.trim { it <= ' ' })), activityOptions)
                }
            }
            catch (e: Exception) { e.printStackTrace() }
        }
    }


    override fun getItemCount() = feedModels.size

    fun updateFeed(feedModels: ArrayList<FeedItem>) {
        this.feedModels.clear()
        this.feedModels.addAll(feedModels)
        this.notifyDataSetChanged()
    }

    companion object {
        private val images: MutableMap<String?, Bitmap?> = HashMap()
    }
}