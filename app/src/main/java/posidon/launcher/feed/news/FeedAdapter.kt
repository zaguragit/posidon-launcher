package posidon.launcher.feed.news

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
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
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import posidon.launcher.Global
import posidon.launcher.Home
import posidon.launcher.R
import posidon.launcher.feed.news.FeedAdapter.ViewHolder
import posidon.launcher.storage.Settings
import posidon.launcher.tools.*
import posidon.launcher.view.SwipeableLayout
import java.util.*

class FeedAdapter(
    val items: ArrayList<FeedItem>,
    private val context: Activity
) : RecyclerView.Adapter<ViewHolder>() {

    class ViewHolder(
        val card: View,
        val title: TextView?,
        val source: TextView?,
        val image: ImageView?,
        val gradient: View?,
        val swipeableLayout: SwipeableLayout?
    ) : RecyclerView.ViewHolder(card)

    private val maxWidth = Settings["feed:max_img_width", Device.displayWidth]

    @SuppressLint("RtlHardcoded")
    override fun onCreateViewHolder(parent: ViewGroup, type: Int): ViewHolder {

        val image = if (Settings["news:cards:image", true]) ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
        } else null

        val gradient = if (Settings["feed:card_text_shadow", true]) View(context) else null

        val title = if (Settings["news:cards:title", true]) TextView(context).apply {
            this.gravity = Gravity.BOTTOM
            run {
                val p = 16.dp.toInt()
                setPadding(p, p, p, p)
            }
            textSize = 18f
            setTextColor(Settings["feed:card_txt_color", -0x1])
        } else null

        val source = if (Settings["news:cards:source", true]) TextView(context).apply {
            run {
                val h = 12.dp.toInt()
                val v = 8.dp.toInt()
                setPadding(h, v, h, v)
            }
            textSize = 12f
            run {
                val bg = ShapeDrawable()
                val r = Settings["news:cards:source:radius", 30].dp
                bg.shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
                bg.paint.color = Settings["news:cards:source:bg_color", 0xff111213.toInt()]
                background = bg
            }
            backgroundTintMode = PorterDuff.Mode.SRC_IN
            setTextColor(Settings["news:cards:source:fg_color", -0x1])
        } else null

        val r = Settings["feed:card_radius", 15].dp

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
            val height = if (Settings["news:cards:wrap_content", true] && !separateImg) MATCH_PARENT else Settings["news:cards:height", 240].dp.toInt()

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
                        val m = 6.dp.toInt()
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

        val holder = ViewHolder((if (Settings["feed:delete_articles", false]) SwipeableLayout(v).apply {
            val bg = Settings["feed:card_swipe_bg_color", 0x880d0e0f.toInt()]
            setIconColor(if (ColorTools.useDarkText(bg)) 0xff000000.toInt() else 0xffffffff.toInt())
            setSwipeColor(bg)
            cornerRadiusCompensation = r
            radius = r
            swipeableLayout = this
        } else v).apply {
            layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT).apply {
                val marginY = Settings["feed:card_margin_y", 9].dp.toInt()
                val marginX = Settings["feed:card_margin_x", 16].dp.toInt() / 2
                setMargins(marginX, marginY, marginX, marginY)
            }
        }, title, source, image, gradient, swipeableLayout)

        return holder
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
                feedItem.tryLoadImage(maxWidth, Loader.AUTO) { Home.instance.runOnUiThread {
                    images[feedItem.img] = it
                    onImageLoadEnd(holder, it)
                }}
            }
        }

        holder.card.setOnClickListener {
            feedItem.open(context)
        }
    }

    private fun deleteArticle(feedItem: FeedItem, i: Int) {
        items.remove(feedItem)
        notifyItemRemoved(i)
        notifyItemRangeChanged(i, items.size - i)
        val day = Calendar.getInstance()[Calendar.DAY_OF_YEAR]
        val a = "$day:" + feedItem.link + ':' + feedItem.title
        Settings.getStrings("feed:deleted_articles").add(a)
        Settings.apply()
        if (Settings["feed:undo_article_removal_opt", false]) {
            Snackbar.make(context.findViewById(android.R.id.content), R.string.removed, Snackbar.LENGTH_LONG).apply {
                setAction(R.string.undo) {
                    items.add(i, feedItem)
                    notifyItemInserted(i)
                    notifyItemRangeChanged(i, items.size - i)
                    Settings.getStrings("feed:deleted_articles").remove(a)
                    Settings.apply()
                }
                view.setPadding(0, 0, 0, Tools.navbarHeight)
                view.layoutParams = view.layoutParams
                setActionTextColor(Global.accentColor)
                view.background = context.resources.getDrawable(R.drawable.card, null)
            }.show()
        }
    }

    override fun getItemCount() = items.size

    fun updateFeed(feedModels: List<FeedItem>) {
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