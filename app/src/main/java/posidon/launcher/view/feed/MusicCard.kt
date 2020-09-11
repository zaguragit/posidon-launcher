package posidon.launcher.view.feed

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import posidon.launcher.Home
import posidon.launcher.R
import posidon.launcher.feed.notifications.NotificationService
import posidon.launcher.storage.Settings
import posidon.launcher.tools.ColorTools
import posidon.launcher.tools.Device
import posidon.launcher.tools.dp

class MusicCard : CardView, FeedSection {

    constructor(c: Context) : super(c)
    constructor(c: Context, a: AttributeSet?) : super(c, a)
    constructor(c: Context, a: AttributeSet?, sa: Int) : super(c, a, sa)

    val musicCardImage = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
    }

    val musicCardTrackTitle = TextView(context).apply {
        textSize = 18f
        setPadding(0, 0, 0, 8.dp.toInt())
        setTypeface(typeface, Typeface.BOLD_ITALIC)
    }

    val musicCardTrackArtist = TextView(context).apply {
        textSize = 15f
    }

    val musicPrev = ImageView(context).apply {
        run {
            val p = 4.dp.toInt()
            setPadding(p, p, p, p)
        }
        setImageResource(R.drawable.ic_arrow_left)
        setOnClickListener {
            Home.musicService.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS))
            Home.musicService.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS))
            musicPlay.setImageResource(R.drawable.ic_pause)
        }
    }

    val musicPlay = ImageView(context).apply {
        setImageResource(R.drawable.ic_play)
        setOnClickListener {
            it as ImageView
            if (Home.musicService.isMusicActive) {
                Home.musicService.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE))
                Home.musicService.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE))
                it.setImageResource(R.drawable.ic_play)
            } else {
                Home.musicService.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY))
                Home.musicService.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY))
                it.setImageResource(R.drawable.ic_pause)
            }
        }
    }

    val musicNext = ImageView(context).apply {
        run {
            val p = 4.dp.toInt()
            setPadding(p, p, p, p)
        }
        setImageResource(R.drawable.ic_arrow_right)
        setOnClickListener {
            Home.musicService.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT))
            Home.musicService.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT))
            musicPlay.setImageResource(R.drawable.ic_pause)
        }
    }

    val musicCardOverImg = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPaddingRelative(16.dp.toInt(), 18.dp.toInt(), 128.dp.toInt(), 0)

        val linearLayout = LinearLayout(context).apply {
            this.orientation = LinearLayout.HORIZONTAL
            this.gravity = Gravity.BOTTOM
            setPadding(0, 10.dp.toInt(), 0, 14.dp.toInt())
            layoutDirection = LAYOUT_DIRECTION_LTR

            addView(musicPrev, LinearLayout.LayoutParams(36.dp.toInt(), 32.dp.toInt()))
            addView(musicPlay, LinearLayout.LayoutParams(36.dp.toInt(), 32.dp.toInt()).apply {
                setMargins(6.dp.toInt(), 0, 6.dp.toInt(), 0)
            })
            addView(musicNext, LinearLayout.LayoutParams(36.dp.toInt(), 32.dp.toInt()))
        }

        addView(musicCardTrackTitle, LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            marginStart = 6.dp.toInt()
        })
        addView(musicCardTrackArtist, LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            marginStart = 6.dp.toInt()
        })
        addView(linearLayout, LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }

    init {
        radius = 16.dp
        cardElevation = 0f
        setCardBackgroundColor(context.resources.getColor(R.color.cardbg))

        addView(musicCardImage, LayoutParams(136.dp.toInt(), MATCH_PARENT).apply {
            minimumHeight = 136.dp.toInt()
            gravity = Gravity.END
        })
        addView(musicCardOverImg, LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }

    override fun doShow() = true

    override fun updateTheme(activity: Activity) {
        val marginX = Settings["feed:card_margin_x", 16].dp.toInt()
        val marginY = Settings["feed:card_margin_y", 9].dp.toInt()
        (layoutParams as MarginLayoutParams).apply {
            leftMargin = marginX
            rightMargin = marginX
            bottomMargin = marginY
        }
        radius = Settings["feed:card_radius", 15].dp
    }

    fun updateTrack(color: Int, title: CharSequence?, subtitle: CharSequence?, icon: Drawable, contentIntent: PendingIntent?) {
        setOnClickListener {
            contentIntent?.send()
        }
        musicCardImage.setImageDrawable(icon)
        musicCardTrackTitle.apply {
            setTextColor(if (ColorTools.useDarkText(color)) -0xeeeded else -0x1)
            text = title
        }
        musicCardTrackArtist.apply {
            setTextColor(if (ColorTools.useDarkText(color)) -0xeeeded else -0x1)
            text = subtitle
        }
        val marginX = Settings["feed:card_margin_x", 16].dp.toInt()
        musicCardOverImg.background =
            if (NotificationService.instance.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_LTR) {
                LayerDrawable(arrayOf(
                        ColorDrawable(color),
                        GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(color, color and 0x00ffffff))
                )).apply {
                    setLayerInset(0, 0, 0, 136.dp.toInt(), 0)
                    setLayerInset(1, Device.displayWidth - 136.dp.toInt() - marginX * 2, 0, 0, 0)
                }
            } else {
                LayerDrawable(arrayOf(
                        ColorDrawable(color),
                        GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, intArrayOf(color, color and 0x00ffffff))
                )).apply {
                    setLayerInset(0, 136.dp.toInt(), 0, 0, 0)
                    setLayerInset(1, 0, 0, Device.displayWidth - 136.dp.toInt() - marginX * 2, 0)
                }
            }
        musicPrev.imageTintList = ColorStateList.valueOf(if (ColorTools.useDarkText(color)) -0xeeeded else -0x1)
        musicPlay.imageTintList = ColorStateList.valueOf(if (ColorTools.useDarkText(color)) -0xeeeded else -0x1)
        musicNext.imageTintList = ColorStateList.valueOf(if (ColorTools.useDarkText(color)) -0xeeeded else -0x1)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            if (Home.musicService.isMusicActive) {
                musicPlay.setImageResource(R.drawable.ic_pause)
            } else {
                musicPlay.setImageResource(R.drawable.ic_play)
            }
        }
    }

    override fun toString() = "music"
}