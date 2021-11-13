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
import android.media.AudioManager
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
import posidon.android.conveniencelib.Colors
import posidon.android.conveniencelib.Device
import posidon.android.conveniencelib.dp
import posidon.launcher.R
import posidon.launcher.storage.Settings

class MusicCard : CardView, FeedSection {

    constructor(c: Context) : super(c)
    constructor(c: Context, a: AttributeSet?) : super(c, a)
    constructor(c: Context, a: AttributeSet?, sa: Int) : super(c, a, sa)

    val musicService = context.getSystemService(AudioManager::class.java)

    val musicCardImage = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
    }

    val musicCardTrackTitle = TextView(context).apply {
        textSize = 18f
        setPadding(0, 0, 0, dp(8).toInt())
        setTypeface(typeface, Typeface.BOLD)
    }

    val musicCardTrackArtist = TextView(context).apply {
        textSize = 15f
    }

    val musicPrev = ImageView(context).apply {
        run {
            val p = context.dp(4).toInt()
            setPadding(p, p, p, p)
        }
        setImageResource(R.drawable.ic_track_previous)
        setOnClickListener {
            musicService.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS))
            musicService.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS))
            musicPlay.setImageResource(R.drawable.ic_pause)
        }
    }

    val musicPlay = ImageView(context).apply {
        setImageResource(R.drawable.ic_play)
        setOnClickListener {
            it as ImageView
            if (musicService.isMusicActive) {
                musicService.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE))
                musicService.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE))
                it.setImageResource(R.drawable.ic_play)
            } else {
                musicService.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY))
                musicService.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY))
                it.setImageResource(R.drawable.ic_pause)
            }
        }
    }

    val musicNext = ImageView(context).apply {
        run {
            val p = context.dp(4).toInt()
            setPadding(p, p, p, p)
        }
        setImageResource(R.drawable.ic_track_next)
        setOnClickListener {
            musicService.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT))
            musicService.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT))
            musicPlay.setImageResource(R.drawable.ic_pause)
        }
    }

    val musicCardOverImg = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPaddingRelative(dp(16).toInt(), dp(18).toInt(), dp(128).toInt(), 0)

        val linearLayout = LinearLayout(context).apply {
            this.orientation = LinearLayout.HORIZONTAL
            this.gravity = Gravity.BOTTOM
            setPadding(0, dp(10).toInt(), 0, dp(14).toInt())
            layoutDirection = LAYOUT_DIRECTION_LTR

            addView(musicPrev, LinearLayout.LayoutParams(dp(36).toInt(), dp(32).toInt()))
            addView(musicPlay, LinearLayout.LayoutParams(dp(36).toInt(), dp(32).toInt()).apply {
                setMargins(dp(12).toInt(), 0, dp(12).toInt(), 0)
            })
            addView(musicNext, LinearLayout.LayoutParams(dp(36).toInt(), dp(32).toInt()))
        }

        addView(musicCardTrackTitle, LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            marginStart = dp(6).toInt()
        })
        addView(musicCardTrackArtist, LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            marginStart = dp(6).toInt()
        })
        addView(linearLayout, LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }

    init {
        radius = dp(16)
        cardElevation = 0f
        setCardBackgroundColor(context.resources.getColor(R.color.cardbg))

        addView(musicCardImage, LayoutParams(dp(136).toInt(), MATCH_PARENT).apply {
            minimumHeight = dp(136).toInt()
            gravity = Gravity.END
        })
        addView(musicCardOverImg, LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }

    override fun updateTheme(activity: Activity) {
        val marginX = dp(Settings["feed:card_margin_x", 16]).toInt()
        val marginY = dp(Settings["feed:card_margin_y", 9]).toInt()
        (layoutParams as MarginLayoutParams).apply {
            leftMargin = marginX
            rightMargin = marginX
            bottomMargin = marginY
            topMargin = marginY
        }
        radius = dp(Settings["feed:card_radius", 15])
    }

    fun updateTrack(color: Int, title: CharSequence?, subtitle: CharSequence?, icon: Drawable, contentIntent: PendingIntent?) {
        setOnClickListener {
            contentIntent?.send()
        }
        musicCardImage.setImageDrawable(icon)
        musicCardTrackTitle.apply {
            setTextColor(if (Colors.getLuminance(color) >.6f) -0xeeeded else -0x1)
            text = title
        }
        musicCardTrackArtist.apply {
            setTextColor(if (Colors.getLuminance(color) >.6f) -0xeeeded else -0x1)
            text = subtitle
        }
        val marginX = dp(Settings["feed:card_margin_x", 16]).toInt()
        val w = dp(136).toInt()
        musicCardOverImg.background =
            if (resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_LTR) {
                LayerDrawable(arrayOf(
                    ColorDrawable(color),
                    GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(color, color and 0x00ffffff))
                )).apply {
                    setLayerInset(0, 0, 0, w, 0)
                    setLayerInset(1, Device.screenWidth(context) - w - marginX * 2, 0, 0, 0)
                }
            } else {
                LayerDrawable(arrayOf(
                    ColorDrawable(color),
                    GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, intArrayOf(color, color and 0x00ffffff))
                )).apply {
                    setLayerInset(0, w, 0, 0, 0)
                    setLayerInset(1, 0, 0, Device.screenWidth(context) - w - marginX * 2, 0)
                }
            }
        musicPrev.imageTintList = ColorStateList.valueOf(if (Colors.getLuminance(color) > .6f) -0xeeeded else -0x1)
        musicPlay.imageTintList = ColorStateList.valueOf(if (Colors.getLuminance(color) > .6f) -0xeeeded else -0x1)
        musicNext.imageTintList = ColorStateList.valueOf(if (Colors.getLuminance(color) > .6f) -0xeeeded else -0x1)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            if (musicService.isMusicActive) {
                musicPlay.setImageResource(R.drawable.ic_pause)
            } else {
                musicPlay.setImageResource(R.drawable.ic_play)
            }
        }
    }

    override fun toString() = "music"
}