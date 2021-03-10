package posidon.launcher.view.feed.notifications

import android.app.RemoteInput
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import posidon.launcher.Global
import posidon.launcher.Home
import posidon.launcher.R
import posidon.launcher.feed.notifications.Notification
import posidon.launcher.feed.notifications.NotificationService
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Gestures
import posidon.launcher.tools.dp
import posidon.launcher.tools.theme.ColorTools
import posidon.launcher.view.SwipeableLayout

class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private var groups = ArrayList<ArrayList<Notification>>()

    override fun getItemCount() = groups.size

    class NotificationViewHolder(
        val view: ViewGroup,
        val card: SwipeableLayout,
        val linearLayout: LinearLayout
    ) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): NotificationViewHolder {
        val context = parent.context
        val view = RelativeLayout(context)
        val hMargin = Settings["feed:card_margin_x", 16].dp.toInt()
        val vMargin = Settings["feed:card_margin_y", 9].dp.toInt()
        view.setPadding(hMargin, vMargin, hMargin, vMargin)

        val ll = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        val card = SwipeableLayout(ll).apply {
            val bg = Settings["notif:card_swipe_bg_color", 0x880d0e0f.toInt()]
            setIconColor(if (ColorTools.useDarkText(bg)) 0xff000000.toInt() else 0xffffffff.toInt())
            setSwipeColor(bg)
            preventCornerOverlap = true
            elevation = 0f
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            radius = Settings["feed:card_radius", 15].dp
        }
        view.addView(card)

        return NotificationViewHolder(view, card, ll)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, i: Int) {
        val context = holder.view.context
        val group = groups[i]
        holder.linearLayout.removeAllViews()
        for (notificationI in group.indices) {
            val notification = group[notificationI]
            val retView: View
            val view: View = if (notification.isSummary) {
                holder.card.onSwipeAway = {
                    Home.instance.runOnUiThread {
                        val iter = group.iterator()
                        for (n in iter) n.cancel()
                    }
                }
                retView = LayoutInflater.from(context).inflate(R.layout.notification_normal_summary, null)
                retView.apply {
                    setBackgroundColor(Settings["notificationbgcolor", -0x1])
                }
            } else {
                val v1 = when {
                    notification.progress != -1f -> {
                        LayoutInflater.from(context).inflate(R.layout.notification_progress, null).apply {
                            val progressBar = findViewById<ProgressBar>(R.id.progress)
                            if (notification.progress == -2f) {
                                progressBar.isIndeterminate = true
                            } else {
                                progressBar.isIndeterminate = false
                                progressBar.progressTintList = ColorStateList.valueOf(Global.accentColor)
                                progressBar.progressBackgroundTintList = ColorStateList.valueOf(Global.accentColor)
                                val percent = (notification.progress * 100).toInt()
                                progressBar.progress = percent
                                progressBar.max = 100
                            }
                        }
                    }
                    notification.bigPic == null -> {
                        LayoutInflater.from(context).inflate(R.layout.notification_normal, null)
                    }
                    else -> {
                        LayoutInflater.from(context).inflate(R.layout.notification_big_pic, null).apply {
                            findViewById<ImageView>(R.id.bigPic).setImageDrawable(notification.bigPic)
                        }
                    }
                }.apply {
                    setBackgroundColor(Settings["notificationbgcolor", -0x1])
                    findViewById<TextView>(R.id.txt).maxLines = Settings["notif:text:max_lines", 3]
                    val padding = 8.dp.toInt()
                    when {
                        notificationI == 0 || (notificationI == 1 && group[0].isSummary) -> {
                            if (notificationI == group.lastIndex) {
                                setPadding(padding, padding, padding, padding)
                            }
                            else {
                                setPadding(padding, padding, padding, 0)
                            }
                        }
                        notificationI == group.lastIndex -> setPadding(padding, 0, padding, padding)
                        else -> setPadding(padding, 0, padding, 0)
                    }
                }
                retView = SwipeableLayout(v1) {
                    try {
                        group.remove(notification)
                        if (group.size == 1 && group[0].isSummary) {
                            group[0].cancel()
                        }
                        notification.cancel()
                    }
                    catch (e: Exception) { e.printStackTrace() }
                    NotificationService.update()
                }.apply {
                    val bg = Settings["notif:card_swipe_bg_color", 0x880d0e0f.toInt()]
                    setIconColor(if (ColorTools.useDarkText(bg)) 0xff000000.toInt() else 0xffffffff.toInt())
                    setSwipeColor(bg)
                }
                if (notification.actions != null && Settings["notificationActionsEnabled", false]) {
                    v1.findViewById<LinearLayout>(R.id.action_list).visibility = View.VISIBLE
                    for (action in notification.actions) {
                        val a = TextView(context)
                        a.text = action.title
                        a.textSize = 14f
                        a.setTextColor(Settings["notificationActionTextColor", -0xdad9d9])
                        val r = Settings["notif:actions:radius", 24].dp
                        val bg = ShapeDrawable(RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null))
                        bg.paint.color = Settings["notificationActionBGColor", 0x88e0e0e0.toInt()]
                        a.background = bg
                        val vPadding = 10.dp.toInt()
                        val hPadding = 15.dp.toInt()
                        a.setPadding(hPadding, vPadding, hPadding, vPadding)
                        v1.findViewById<LinearLayout>(R.id.action_list).addView(a)
                        (a.layoutParams as LinearLayout.LayoutParams).leftMargin = 6.dp.toInt()
                        (a.layoutParams as LinearLayout.LayoutParams).rightMargin = 6.dp.toInt()
                        a.setOnClickListener {
                            try {
                                val oldInputs = action.remoteInputs
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && oldInputs != null) {
                                    v1.findViewById<View>(R.id.bottomSeparator).visibility = View.VISIBLE
                                    v1.findViewById<View>(R.id.reply).apply lin@ {
                                        visibility = View.VISIBLE
                                        val imm = getSystemService(context, InputMethodManager::class.java)!!
                                        val textArea = findViewById<EditText>(R.id.replyText).apply {
                                            setTextColor(Settings["notificationtitlecolor", -0xeeeded])
                                            setHintTextColor(Settings["notificationtxtcolor", -0xdad9d9])
                                            requestFocus()
                                            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                                            setOnFocusChangeListener { _, hasFocus ->
                                                if (!hasFocus) {
                                                    text.clear()
                                                    this@lin.visibility = View.GONE
                                                    v1.findViewById<View>(R.id.bottomSeparator).visibility = View.GONE
                                                    imm.hideSoftInputFromWindow(windowToken, 0)
                                                }
                                            }
                                        }
                                        findViewById<ImageView>(R.id.cancel).apply {
                                            imageTintList = ColorStateList.valueOf(Settings["notificationtitlecolor", -0xeeeded])
                                            backgroundTintList = ColorStateList.valueOf(Settings["notificationtitlecolor", -0xeeeded] and 0xffffff or 0x33ffffff)
                                            setOnClickListener {
                                                textArea.text.clear()
                                                this@lin.visibility = View.GONE
                                                v1.findViewById<View>(R.id.bottomSeparator).visibility = View.GONE
                                            }
                                        }
                                        findViewById<ImageView>(R.id.replySend).apply {
                                            imageTintList = ColorStateList.valueOf(Settings["notificationtitlecolor", -0xeeeded])
                                            backgroundTintList = ColorStateList.valueOf(Settings["notificationtitlecolor", -0xeeeded] and 0xffffff or 0x33ffffff)
                                            setOnClickListener {
                                                val intent = Intent()
                                                val bundle = Bundle()
                                                val actualInputs: ArrayList<RemoteInput> = ArrayList()
                                                for (input in oldInputs) {
                                                    bundle.putCharSequence(input.resultKey, textArea.text)
                                                    val builder = RemoteInput.Builder(input.resultKey)
                                                    builder.setLabel(input.label)
                                                    builder.setChoices(input.choices)
                                                    builder.setAllowFreeFormInput(input.allowFreeFormInput)
                                                    builder.addExtras(input.extras)
                                                    actualInputs.add(builder.build())
                                                }
                                                val inputs = actualInputs.toArray(arrayOfNulls<RemoteInput>(actualInputs.size))
                                                RemoteInput.addResultsToIntent(inputs, intent, bundle)
                                                action.actionIntent.send(context, 0, intent)
                                                textArea.text.clear()
                                                this@lin.visibility = View.GONE
                                                v1.findViewById<View>(R.id.bottomSeparator).visibility = View.GONE
                                            }
                                        }
                                    }
                                } else {
                                    action.actionIntent.send()
                                }
                            }
                            catch (e: Exception) { e.printStackTrace() }
                        }
                    }
                }
                v1
            }

            view.findViewById<TextView>(R.id.title).run {
                text = notification.title
                setTextColor(Settings["notificationtitlecolor", -0xeeeded])
            }
            view.findViewById<TextView>(R.id.txt).run {
                text = notification.text
                setTextColor(Settings["notificationtxtcolor", -0xdad9d9])
            }

            view.findViewById<ImageView>(R.id.iconimg).setImageDrawable(notification.icon)

            view.setOnClickListener { notification.open() }
            view.setOnLongClickListener(Gestures::onLongPress)
            holder.linearLayout.addView(retView)
        }
    }

    fun update(groups: ArrayList<ArrayList<Notification>>) {
        this.groups = groups
        notifyDataSetChanged()
    }
}
