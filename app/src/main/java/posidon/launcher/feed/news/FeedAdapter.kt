package posidon.launcher.feed.news

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import posidon.launcher.Home
import posidon.launcher.LauncherMenu
import posidon.launcher.R
import posidon.launcher.feed.news.FeedAdapter.FeedModelViewHolder
import posidon.launcher.storage.Settings
import posidon.launcher.tools.*
import posidon.launcher.view.SwipeableLayout
import java.util.*

class FeedAdapter(
    private val feedModels: ArrayList<FeedItem>,
    private val context: Activity
) : RecyclerView.Adapter<FeedModelViewHolder>() {

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
            layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT)
            val tp = Settings["feed:card_margin_y", 9].dp.toInt()
            setPadding(0, tp, 0, tp)
            addView(if (Settings["feed:delete_articles", false]) SwipeableLayout(v).apply {
                val bg = Settings["feed:card_swipe_bg_color", 0x880d0e0f.toInt()]
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
                val a = "$day:" + feedItem.link + ':' + feedItem.title
                Settings.getStrings("feed:deleted_articles").add(a)
                Settings.apply()
                if (Settings["feed:undo_article_removal_opt", false]) {
                    Snackbar.make(context.findViewById(android.R.id.content), R.string.removed, Snackbar.LENGTH_LONG).apply {
                        setAction(R.string.undo) {
                            feedModels.add(i, feedItem)
                            notifyItemInserted(i)
                            notifyItemRangeChanged(i, feedModels.size - i)
                            Settings.getStrings("feed:deleted_articles").remove(a)
                            Settings.apply()
                        }
                        view.setPadding(0, 0, 0, Tools.navbarHeight)
                        view.layoutParams = view.layoutParams
                        setActionTextColor(Home.accentColor)
                        view.background = context.resources.getDrawable(R.drawable.card, null)
                    }.show()
                }
            }
        }

        if (Settings["feed:card_img_enabled", true] && feedItem.img != null) {

            fun onImageLoadEnd(img: Bitmap) = try {
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
            } catch (e: Exception) { e.printStackTrace() }

            if (images.containsKey(feedItem.img)) {
                onImageLoadEnd(images[feedItem.img]!!)
            } else {
                holder.image.setImageDrawable(null)
                feedItem.tryLoadImage(maxWidth, Loader.AUTO) { Home.instance.runOnUiThread {
                    images[feedItem.img] = it
                    onImageLoadEnd(it)
                }}
            }
        } else {
            holder.image.visibility = View.GONE
            holder.card.findViewById<View>(R.id.card).layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            holder.card.findViewById<View>(R.id.txt).layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            holder.card.findViewById<View>(R.id.gradient).layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        holder.card.findViewById<View>(R.id.card).setOnClickListener {
            feedItem.open(context)
        }
    }

    override fun getItemCount() = feedModels.size

    fun updateFeed(feedModels: List<FeedItem>) {
        this.feedModels.clear()
        this.feedModels.addAll(feedModels)
        this.notifyDataSetChanged()
    }

    companion object {
        private val images = HashMap<String?, Bitmap?>()
    }
}