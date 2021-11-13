package posidon.launcher.tools.theme

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.ColorInt
import androidx.core.app.ActivityCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import posidon.android.conveniencelib.Colors
import posidon.android.conveniencelib.dp
import posidon.android.conveniencelib.toBitmap
import posidon.launcher.R
import posidon.launcher.drawable.ColorPreviewDrawable
import posidon.launcher.tools.Tools
import posidon.launcher.view.recycler.LinearLayoutManager

object ColorTools {

    inline fun colorPreview(@ColorInt color: Int): Drawable {
        return ColorPreviewDrawable(color)
    }

    inline fun iconBadge(@ColorInt color: Int): Drawable {
        val d = GradientDrawable()
        d.shape = GradientDrawable.OVAL
        d.setColor(color)
        d.setStroke(1, 0x55000000)
        return d
    }

    fun pickColor(context: Context, @ColorInt initColor: Int, onSelect: (color: Int) -> Unit) {
        val d = BottomSheetDialog(context, R.style.bottomsheet)
        d.setContentView(R.layout.color_picker)
        d.window!!.findViewById<View>(R.id.design_bottom_sheet).setBackgroundResource(R.drawable.bottom_sheet)
        val alpha = d.findViewById<SeekBar>(R.id.alpha)!!
        val red = d.findViewById<SeekBar>(R.id.red)!!
        val green = d.findViewById<SeekBar>(R.id.green)!!
        val blue = d.findViewById<SeekBar>(R.id.blue)!!
        val txt = d.findViewById<EditText>(R.id.hextxt)!!
        val okBtn = d.findViewById<TextView>(R.id.ok)!!
        d.findViewById<RecyclerView>(R.id.recycler)!!.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = ColorAdapter(getWallpaperColors(context).apply {
                add(0xffee6170.toInt())
                add(0xff32afce.toInt())
                add(0xff37a051.toInt())
                add(0xffd35744.toInt())
                add(0xffddb63f.toInt())
                add(0xff5a5bfa.toInt())
                add(0xff5ae1be.toInt())
                add(0xff17c25b.toInt())
            }).apply { onItemClickListener = { color -> txt.setText(Integer.toHexString(color)) }}
        }
        var updatingAllowed = true
        txt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = try {
                val color = s.toString().toLong(16).toInt()
                d.findViewById<View>(R.id.bgColorPrev)!!.setBackgroundColor(color)
                if (updatingAllowed) {
                    updatingAllowed = false
                    alpha.progress = color shr 24 and 0xff
                    red.progress = color shr 16 and 0xff
                    green.progress = color shr 8 and 0xff
                    blue.progress = color and 0xff
                    updatingAllowed = true
                }
                val txtColor = if (Colors.getLuminance(color) > .6f) -0xdad9d9 else -0x1
                txt.setTextColor(txtColor)
                okBtn.setTextColor(txtColor)
                okBtn.backgroundTintList = ColorStateList.valueOf(0x00ffffff and txtColor or 0x33000000)
            } catch (ignore: NumberFormatException) {}
        })
        txt.setText(Integer.toHexString(initColor))
        val txtColor = if (Colors.getLuminance(initColor) > .6f) -0xdad9d9 else -0x1
        txt.setTextColor(txtColor)
        okBtn.setTextColor(txtColor)
        okBtn.backgroundTintList = ColorStateList.valueOf(0x00ffffff and txtColor or 0x33000000)
        okBtn.setOnClickListener {
            var newColor = initColor
            try { newColor = txt.text.toString().toLong(16).toInt() }
            catch (e: NumberFormatException) { Toast.makeText(context, "That's not a color.", Toast.LENGTH_SHORT).show() }
            onSelect(newColor)
            d.dismiss()
        }
        val hex = StringBuilder(txt.text.toString())
        while (hex.length != 8) hex.insert(0, 0)
        alpha.progress = hex.substring(0, 2).toLong(16).toInt()
        red.progress = hex.substring(2, 4).toLong(16).toInt()
        green.progress = hex.substring(4, 6).toLong(16).toInt()
        blue.progress = hex.substring(6, 8).toLong(16).toInt()
        alpha.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (updatingAllowed) {
                    updatingAllowed = false
                    val color = progress * 256 * 256 * 256 + red.progress * 256 * 256 + green.progress * 256 + blue.progress
                    txt.setText(Integer.toHexString(color))
                    val txtColor = if (Colors.getLuminance(color) > .6f) -0xdad9d9 else -0x1
                    txt.setTextColor(txtColor)
                    okBtn.setTextColor(txtColor)
                    okBtn.backgroundTintList = ColorStateList.valueOf(0x00ffffff and txtColor or 0x33000000)
                    updatingAllowed = true
                }
            }
        })
        red.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (updatingAllowed) {
                    updatingAllowed = false
                    val color = alpha.progress * 256 * 256 * 256 + progress * 256 * 256 + green.progress * 256 + blue.progress
                    txt.setText(Integer.toHexString(color))
                    val txtColor = if (Colors.getLuminance(color) > .6f) -0xdad9d9 else -0x1
                    txt.setTextColor(txtColor)
                    okBtn.setTextColor(txtColor)
                    okBtn.backgroundTintList = ColorStateList.valueOf(0x00ffffff and txtColor or 0x33000000)
                    updatingAllowed = true
                }
            }
        })
        green.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (updatingAllowed) {
                    updatingAllowed = false
                    val color = alpha.progress * 256 * 256 * 256 + red.progress * 256 * 256 + progress * 256 + blue.progress
                    txt.setText(Integer.toHexString(color))
                    val txtColor = if (Colors.getLuminance(color) > .6f) -0xdad9d9 else -0x1
                    txt.setTextColor(txtColor)
                    okBtn.setTextColor(txtColor)
                    okBtn.backgroundTintList = ColorStateList.valueOf(0x00ffffff and txtColor or 0x33000000)
                    updatingAllowed = true
                }
            }
        })
        blue.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (updatingAllowed) {
                    updatingAllowed = false
                    val color = alpha.progress * 256 * 256 * 256 + red.progress * 256 * 256 + green.progress * 256 + progress
                    txt.setText(Integer.toHexString(color))
                    val txtColor = if (Colors.getLuminance(color) > .6f) -0xdad9d9 else -0x1
                    txt.setTextColor(txtColor)
                    okBtn.setTextColor(txtColor)
                    okBtn.backgroundTintList = ColorStateList.valueOf(0x00ffffff and txtColor or 0x33000000)
                    updatingAllowed = true
                }
            }
        })
        d.show()
    }

    fun pickColorNoAlpha(context: Context, @ColorInt initColor: Int, onSelect: (color: Int) -> Unit) {
        val d = BottomSheetDialog(context, R.style.bottomsheet)
        d.setContentView(R.layout.color_picker)
        d.window!!.findViewById<View>(R.id.design_bottom_sheet).setBackgroundResource(R.drawable.bottom_sheet)
        val red = d.findViewById<SeekBar>(R.id.red)!!
        val green = d.findViewById<SeekBar>(R.id.green)!!
        val blue = d.findViewById<SeekBar>(R.id.blue)!!
        val txt = d.findViewById<EditText>(R.id.hextxt)!!
        val okBtn = d.findViewById<TextView>(R.id.ok)!!
        d.findViewById<RecyclerView>(R.id.recycler)!!.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = ColorAdapter(getWallpaperColors(context).apply {
                add(0xffee6170.toInt())
                add(0xff32afce.toInt())
                add(0xff37a051.toInt())
                add(0xffd35744.toInt())
                add(0xffddb63f.toInt())
                add(0xff5a5bfa.toInt())
                add(0xff5ae1be.toInt())
                add(0xff17c25b.toInt())
            }).apply { onItemClickListener = { color -> txt.setText(Integer.toHexString(color and 0xffffff)) }}
        }
        var updatingAllowed = true
        val hex = StringBuilder(txt.text.toString())
        txt.filters = arrayOf<InputFilter>(LengthFilter(6))
        txt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = try {
                val color = "ff$s".toLong(16).toInt()
                d.findViewById<View>(R.id.bgColorPrev)!!.setBackgroundColor(color)
                if (updatingAllowed) {
                    updatingAllowed = false
                    red.progress = color shr 16 and 0xff
                    green.progress = color shr 8 and 0xff
                    blue.progress = color and 0xff
                    updatingAllowed = true
                }
                val txtColor = if (Colors.getLuminance(color) > .6f) -0xdad9d9 else -0x1
                txt.setTextColor(txtColor)
                okBtn.setTextColor(txtColor)
                okBtn.backgroundTintList = ColorStateList.valueOf(0x00ffffff and txtColor or 0x33000000)
            } catch (ignore: NumberFormatException) {}
        })
        txt.setText(initColor.toString(16).padStart(6, '0'))
        val txtColor = if (Colors.getLuminance(initColor) > .6f) -0xdad9d9 else -0x1
        txt.setTextColor(txtColor)
        okBtn.setTextColor(txtColor)
        okBtn.backgroundTintList = ColorStateList.valueOf(0x00ffffff and txtColor or 0x33000000)
        okBtn.setOnClickListener {
            var newColor = initColor
            try { newColor = txt.text.toString().toLong(16).toInt() }
            catch (e: NumberFormatException) { Toast.makeText(context, "That's not a color.", Toast.LENGTH_SHORT).show() }
            onSelect(newColor)
            d.dismiss()
        }
        d.findViewById<View>(R.id.alpha)!!.visibility = View.GONE
        while (hex.length != 8) hex.insert(0, 0)
        red.progress = hex.substring(2, 4).toLong(16).toInt()
        green.progress = hex.substring(4, 6).toLong(16).toInt()
        blue.progress = hex.substring(6, 8).toLong(16).toInt()
        red.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (updatingAllowed) {
                    updatingAllowed = false
                    val color = progress * 256 * 256 + green.progress * 256 + blue.progress
                    val hex = StringBuilder(Integer.toHexString(color))
                    while (hex.length != 6) hex.insert(0, 0)
                    txt.setText(hex.toString())
                    val txtColor = if (Colors.getLuminance(color) > .6f) -0xdad9d9 else -0x1
                    txt.setTextColor(txtColor)
                    okBtn.setTextColor(txtColor)
                    okBtn.backgroundTintList = ColorStateList.valueOf(0x00ffffff and txtColor or 0x33000000)
                    updatingAllowed = true
                }
            }
        })
        green.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (updatingAllowed) {
                    updatingAllowed = false
                    val color = red.progress * 256 * 256 + progress * 256 + blue.progress
                    val hex = StringBuilder(Integer.toHexString(color))
                    while (hex.length != 6) hex.insert(0, 0)
                    txt.setText(hex.toString())
                    val txtColor = if (Colors.getLuminance(color) > .6f) -0xdad9d9 else -0x1
                    txt.setTextColor(txtColor)
                    okBtn.setTextColor(txtColor)
                    okBtn.backgroundTintList = ColorStateList.valueOf(0x00ffffff and txtColor or 0x33000000)
                    updatingAllowed = true
                }
            }
        })
        blue.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (updatingAllowed) {
                    updatingAllowed = false
                    val color = red.progress * 256 * 256 + green.progress * 256 + progress
                    val hex = StringBuilder(Integer.toHexString(color))
                    while (hex.length != 6) hex.insert(0, 0)
                    txt.setText(hex.toString())
                    val txtColor = if (Colors.getLuminance(color) > .6f) -0xdad9d9 else -0x1
                    txt.setTextColor(txtColor)
                    okBtn.setTextColor(txtColor)
                    okBtn.backgroundTintList = ColorStateList.valueOf(0x00ffffff and txtColor or 0x33000000)
                    updatingAllowed = true
                }
            }
        })
        d.show()
    }

    fun getWallpaperColors(context: Context) = ArrayList<Int>().apply {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val palette = Palette.from(WallpaperManager.getInstance(context).fastDrawable.toBitmap()).generate()
            add(palette.getDominantColor(0xff1155ff.toInt()))
            add(palette.getDarkVibrantColor(0xff1155ff.toInt()))
            add(palette.getVibrantColor(0xff1155ff.toInt()))
            add(palette.getLightMutedColor(0xff1155ff.toInt()))
            add(palette.getMutedColor(0xff1155ff.toInt()))
            add(palette.getDarkMutedColor(0xff1155ff.toInt()))
            add(palette.getLightVibrantColor(0xff1155ff.toInt()))
        }
    }

    class ColorAdapter(
        private val colors: List<Int>
    ) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

        var onItemClickListener: ((color: Int) -> Unit)? = null

        class ColorViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

        override fun getItemCount() = colors.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ColorViewHolder(ImageView(Tools.appContext).apply {
                val p = parent.dp(8).toInt()
                val size = parent.dp(36).toInt() + p * 2
                maxWidth = size
                maxHeight = size
                minimumWidth = size
                minimumHeight = size
                setPadding(p, p, p, p)
            })

        override fun onBindViewHolder(holder: ColorViewHolder, i: Int) {
            holder.imageView.apply {
                setImageDrawable(colorPreview(colors[i]))
                setOnClickListener {
                    onItemClickListener?.invoke(colors[i])
                }
            }
        }
    }
}