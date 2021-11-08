package posidon.launcher.view.feed.news

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import posidon.android.conveniencelib.Colors
import posidon.android.conveniencelib.dp
import posidon.android.loader.rss.RssItem
import posidon.launcher.Global
import posidon.launcher.Home
import posidon.launcher.R
import posidon.launcher.feed.news.readers.WebViewActivity
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Gestures
import posidon.launcher.tools.ImageLoader
import posidon.launcher.tools.Tools
import posidon.launcher.tools.open
import posidon.launcher.view.SwipeableLayout
import posidon.launcher.view.feed.news.NewsAdapter.ViewHolder
import java.util.*

class NewsAdapter(
    private val items: ArrayList<RssItem>,
    private val activity: Activity
) : RecyclerView.Adapter<ViewHolder>() {

    class ViewHolder(
        val card: View,
        val title: TextView?,
        val source: TextView?,
        val image: ImageView?,
        val gradient: View?,
        val swipeableLayout: SwipeableLayout?
    ) : RecyclerView.ViewHolder(card)

    private val maxWidth = Settings["feed:max_img_width", 720]

    @SuppressLint("RtlHardcoded")
    override fun onCreateViewHolder(parent: ViewGroup, type: Int): ViewHolder {
        val context = parent.context

        val image = if (Settings["news:cards:image", true]) ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
        } else null

        val gradient = if (Settings["feed:card_text_shadow", true]) View(context) else null

        val title = if (Settings["news:cards:title", true]) TextView(context).apply {
            this.gravity = Gravity.BOTTOM
            run {
                val p = context.dp(16).toInt()
                setPadding(p, p, p, p)
            }
            textSize = 18f
            setTextColor(Settings["feed:card_txt_color", -0x1])
        } else null

        val source = if (Settings["news:cards:source", true]) TextView(context).apply {
            run {
                val h = context.dp(12).toInt()
                val v = context.dp(8).toInt()
                setPadding(h, v, h, v)
            }
            textSize = 12f
            run {
                val bg = ShapeDrawable()
                val r = dp(Settings["news:cards:source:radius", 30])
                bg.shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
                bg.paint.color = Settings["news:cards:source:bg_color", 0xff111213.toInt()]
                background = bg
            }
            backgroundTintMode = PorterDuff.Mode.SRC_IN
            setTextColor(Settings["news:cards:source:fg_color", -0x1])
        } else null

        val r = context.dp(Settings["feed:card_radius", 15])

        val v = CardView(context).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            radius = r
            cardElevation = 0f
            preventCornerOverlap = true
            setOnLongClickListener(Gestures::onLongPress)
            setCardBackgroundColor(Settings["feed:card_bg", -0xdad9d9])
        }

        v.run {
            val separateImg = Settings["news:cards:sep_txt", false]
            val height = if (Settings["news:cards:wrap_content", true] && !separateImg) MATCH_PARENT else dp(Settings["news:cards:height", 240]).toInt()

            if (!separateImg && image != null) {
                addView(image, ViewGroup.LayoutParams(MATCH_PARENT, height))
            }
            addView(gradient, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                this.gravity = Gravity.BOTTOM
            })
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL

                fun selectAlignment(i: Int) = when (i) {
                    1 -> Gravity.CENTER_HORIZONTAL
                    2 -> Gravity.RIGHT
                    else -> Gravity.LEFT
                }

                if (separateImg && image != null) {
                    addView(image, ViewGroup.LayoutParams(MATCH_PARENT, height))
                }

                val sourceGoesFirst = Settings["news:cards:source:show_above_text", false]
                if (!sourceGoesFirst && title != null) addView(title, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))

                if (source != null) {
                    addView(source, LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                        val m = dp(6).toInt()
                        setMargins(m, m, m, m)
                        gravity = selectAlignment(Settings["news:cards:source:align", 0])
                    })
                }

                if (sourceGoesFirst && title != null) addView(title, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
            }, FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                gravity = Gravity.BOTTOM
            })
        }

        var swipeableLayout: SwipeableLayout? = null

        return ViewHolder((if (Settings["feed:delete_articles", false]) SwipeableLayout(v).apply {
            val bg = Settings["feed:card_swipe_bg_color", 0x880d0e0f.toInt()]
            setIconColor(if (Colors.getLuminance(bg) > .6f) 0xff000000.toInt() else 0xffffffff.toInt())
            setSwipeColor(bg)
            cornerRadiusCompensation = r
            radius = r
            swipeableLayout = this
        } else v).apply {
            layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                val marginY = dp(Settings["feed:card_margin_y", 9]).toInt()
                val marginX = dp(Settings["feed:card_margin_x", 16]).toInt() / 2
                setMargins(marginX, marginY, marginX, marginY)
            }
        }, title, source, image, gradient, swipeableLayout)
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        val feedItem = items[i]
        holder.title?.text = feedItem.title
        holder.source?.text = feedItem.source.name

        if (Settings["feed:delete_articles", false]) {
            val swipeableLayout = holder.swipeableLayout!!
            swipeableLayout.reset()
            swipeableLayout.onSwipeAway = { deleteArticle(feedItem, i) }
        }

        if (holder.image != null) when {
            feedItem.img == null -> holder.image.visibility = View.GONE
            images.containsKey(feedItem.img) -> {
                holder.image.visibility = View.VISIBLE
                onImageLoadEnd(holder, images[feedItem.img]!!)
            }
            else -> {
                holder.image.visibility = View.VISIBLE
                holder.image.setImageDrawable(null)
                val onLoad = { it: Bitmap ->
                    Home.instance.runOnUiThread {
                        images[feedItem.img] = it
                        onImageLoadEnd(holder, it)
                    }
                }
                ImageLoader.loadNullableBitmap(feedItem.img!!, maxWidth, ImageLoader.AUTO, false) {
                    if (it == null) {
                        ImageLoader.loadNullableBitmap(feedItem.source.domain + '/' + feedItem.img, maxWidth, ImageLoader.AUTO, false) {
                            if (it != null) onLoad(it)
                        }
                    } else onLoad(it)
                }
            }
        }

        holder.card.setOnClickListener {
            try {
                val activityOptions = ActivityOptionsCompat.makeCustomAnimation(
                    holder.itemView.context,
                    R.anim.slideup,
                    R.anim.home_exit
                ).toBundle()

                if (Settings["news:open_in_app", false]) {
                    holder.itemView.context.open(WebViewActivity::class.java, activityOptions) {
                        putExtra("url", feedItem.link)
                    }
                } else holder.itemView.context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(feedItem.link.trim { it <= ' ' })), activityOptions)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun deleteArticle(feedItem: RssItem, i: Int) {
        items.remove(feedItem)
        notifyItemRemoved(i)
        notifyItemRangeChanged(i, items.size - i)
        val day = Calendar.getInstance()[Calendar.DAY_OF_YEAR]
        val a = "$day:" + feedItem.link + ':' + feedItem.title
        Settings.getStringsOrSetEmpty("feed:deleted_articles").add(a)
        Settings.apply()
        if (Settings["feed:undo_article_removal_opt", false]) {
            Snackbar.make(activity.findViewById(android.R.id.content), R.string.removed, Snackbar.LENGTH_LONG).apply {
                setAction(R.string.undo) {
                    items.add(i, feedItem)
                    notifyItemInserted(i)
                    notifyItemRangeChanged(i, items.size - i)
                    Settings.getStringsOrSetEmpty("feed:deleted_articles").remove(a)
                    Settings.apply()
                }
                view.setPadding(0, 0, 0, Tools.navbarHeight)
                view.layoutParams = view.layoutParams
                setActionTextColor(Global.accentColor)
                view.background = ContextCompat.getDrawable(context, R.drawable.card)
            }.show()
        }
    }

    override fun getItemCount() = items.size

    fun updateFeed(feedModels: List<RssItem>) {
        this.items.clear()
        this.items.addAll(feedModels)
        this.notifyDataSetChanged()
    }

    companion object {
        private val images = HashMap<String?, Bitmap?>()

        private fun onImageLoadEnd(holder: ViewHolder, img: Bitmap) {
            try {
                holder.image?.setImageBitmap(img)
                if (holder.gradient != null) {
                    Palette.from(img).generate {
                        val gradientDrawable = GradientDrawable()
                        if (it == null) {
                            gradientDrawable.colors = intArrayOf(0x0, -0x1000000)
                            holder.source?.backgroundTintList = null
                        } else {
                            gradientDrawable.colors = intArrayOf(0x0, it.getDarkMutedColor(-0x1000000))
                            if (Settings["news:cards:source:tint_bg", true]) {
                                holder.source?.backgroundTintList = ColorStateList.valueOf(it.getDarkMutedColor(-0xdad9d9) and 0x00ffffff or -0x78000000)
                            }
                        }
                        holder.gradient.background = gradientDrawable
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}