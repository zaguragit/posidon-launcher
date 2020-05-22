package posidon.launcher.tools

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
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
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.view.LinearLayoutManager

object ColorTools {

    @ColorInt
    fun blendColors(@ColorInt color1: Int, @ColorInt color2: Int, ratio: Float): Int {
        var ratio = ratio
        if (ratio > 1) ratio = 1f else if (ratio < 0) ratio = 0f
        val inverseRatio = 1f - ratio
        val a = Color.alpha(color1) * ratio + Color.alpha(color2) * inverseRatio
        val r = Color.red(color1) * ratio + Color.red(color2) * inverseRatio
        val g = Color.green(color1) * ratio + Color.green(color2) * inverseRatio
        val b = Color.blue(color1) * ratio + Color.blue(color2) * inverseRatio
        return Color.argb(a.toInt(), r.toInt(), g.toInt(), b.toInt())
    }

    inline fun useDarkText(@ColorInt bg: Int) = bg shr 8 and 0xFF > 240 || (bg shr 8 and 0xFF /*green*/ > 200 && bg shr 16 and 0xFF /*red*/ > 120)

    inline fun colorCircle(@ColorInt color: Int): Drawable {
        val d = GradientDrawable()
        d.shape = GradientDrawable.OVAL
        d.setColor(color)
        d.setStroke(1, -0x1000000)
        return d
    }

    inline fun notificationBadge(@ColorInt color: Int): Drawable {
        val d = GradientDrawable()
        d.shape = GradientDrawable.OVAL
        d.setColor(color)
        d.setStroke(1, 0x55000000)
        return d
    }

    fun pickColor(context: Context?, initColor: Int, onSelect: (color: Int) -> Unit) {
        val d = BottomSheetDialog(context!!, R.style.bottomsheet)
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
            adapter = ColorAdapter(getWallpaperColors().apply {
                add(0xffee6170.toInt())
                add(0xff32afce.toInt())
                add(0xff37a051.toInt())
                add(0xffd35744.toInt())
                add(0xffddb63f.toInt())
                add(0xff5a5bfa.toInt())
                add(0xff5ae1be.toInt())
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
                val txtColor = if (useDarkText(color)) -0xdad9d9 else -0x1
                txt.setTextColor(txtColor)
                okBtn.setTextColor(txtColor)
                okBtn.backgroundTintList = ColorStateList.valueOf(0x00ffffff and txtColor or 0x33000000)
            } catch (ignore: NumberFormatException) {}
        })
        txt.setText(Integer.toHexString(initColor))
        val txtColor = if (useDarkText(initColor)) -0xdad9d9 else -0x1
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
                    val txtColor = if (useDarkText(color)) -0xdad9d9 else -0x1
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
                    val txtColor = if (useDarkText(color)) -0xdad9d9 else -0x1
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
                    val txtColor = if (useDarkText(color)) -0xdad9d9 else -0x1
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
                    val txtColor = if (useDarkText(color)) -0xdad9d9 else -0x1
                    txt.setTextColor(txtColor)
                    okBtn.setTextColor(txtColor)
                    okBtn.backgroundTintList = ColorStateList.valueOf(0x00ffffff and txtColor or 0x33000000)
                    updatingAllowed = true
                }
            }
        })
        d.show()
    }

    fun pickColor(context: Context?, settingskey: String?, @ColorInt defaultcolor: Int) {
        val d = BottomSheetDialog(context!!, R.style.bottomsheet)
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
            adapter = ColorAdapter(getWallpaperColors().apply {
                add(0xffee6170.toInt())
                add(0xff32afce.toInt())
                add(0xff37a051.toInt())
                add(0xffd35744.toInt())
                add(0xffddb63f.toInt())
                add(0xff5a5bfa.toInt())
                add(0xff5ae1be.toInt())
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
                val txtColor = if (useDarkText(color)) -0xdad9d9 else -0x1
                txt.setTextColor(txtColor)
                okBtn.setTextColor(txtColor)
                okBtn.backgroundTintList = ColorStateList.valueOf(0x00ffffff and txtColor or 0x33000000)
            } catch (ignore: NumberFormatException) {}
        })
        txt.setText(Integer.toHexString(Settings[settingskey!!, defaultcolor]))
        val txtColor = if (useDarkText(Settings[settingskey, defaultcolor])) -0xdad9d9 else -0x1
        txt.setTextColor(txtColor)
        okBtn.setTextColor(txtColor)
        okBtn.backgroundTintList = ColorStateList.valueOf(0x00ffffff and txtColor or 0x33000000)
        okBtn.setOnClickListener {
            d.dismiss()
            try { Settings[settingskey] = txt.text.toString().toLong(16).toInt() }
            catch (e: NumberFormatException) { Toast.makeText(context, "That's not a color.", Toast.LENGTH_SHORT).show() }
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
                    val txtColor = if (useDarkText(color)) -0xdad9d9 else -0x1
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
                    val txtColor = if (useDarkText(color)) -0xdad9d9 else -0x1
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
                    val txtColor = if (useDarkText(color)) -0xdad9d9 else -0x1
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
                    val txtColor = if (useDarkText(color)) -0xdad9d9 else -0x1
                    txt.setTextColor(txtColor)
                    okBtn.setTextColor(txtColor)
                    okBtn.backgroundTintList = ColorStateList.valueOf(0x00ffffff and txtColor or 0x33000000)
                    updatingAllowed = true
                }
            }
        })
        d.show()
    }

    fun pickColorNoAlpha(context: Context?, initColor: Int, onSelect: (color: Int) -> Unit) {
        val d = BottomSheetDialog(context!!, R.style.bottomsheet)
        d.setContentView(R.layout.color_picker)
        d.window!!.findViewById<View>(R.id.design_bottom_sheet).setBackgroundResource(R.drawable.bottom_sheet)
        val red = d.findViewById<SeekBar>(R.id.red)!!
        val green = d.findViewById<SeekBar>(R.id.green)!!
        val blue = d.findViewById<SeekBar>(R.id.blue)!!
        val txt = d.findViewById<EditText>(R.id.hextxt)!!
        val okBtn = d.findViewById<TextView>(R.id.ok)!!
        d.findViewById<RecyclerView>(R.id.recycler)!!.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = ColorAdapter(getWallpaperColors().apply {
                add(0xffee6170.toInt())
                add(0xff32afce.toInt())
                add(0xff37a051.toInt())
                add(0xffd35744.toInt())
                add(0xffddb63f.toInt())
                add(0xff5a5bfa.toInt())
                add(0xff5ae1be.toInt())
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
                val txtColor = if (useDarkText(color)) -0xdad9d9 else -0x1
                txt.setTextColor(txtColor)
                okBtn.setTextColor(txtColor)
                okBtn.backgroundTintList = ColorStateList.valueOf(0x00ffffff and txtColor or 0x33000000)
            } catch (ignore: NumberFormatException) {}
        })
        txt.setText(Integer.toHexString(initColor))
        val txtColor = if (useDarkText(initColor)) -0xdad9d9 else -0x1
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
                    val txtColor = if (useDarkText(color)) -0xdad9d9 else -0x1
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
                    val txtColor = if (useDarkText(color)) -0xdad9d9 else -0x1
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
                    val txtColor = if (useDarkText(color)) -0xdad9d9 else -0x1
                    txt.setTextColor(txtColor)
                    okBtn.setTextColor(txtColor)
                    okBtn.backgroundTintList = ColorStateList.valueOf(0x00ffffff and txtColor or 0x33000000)
                    updatingAllowed = true
                }
            }
        })
        d.show()
    }

	@SuppressLint("SetTextI18n")
    fun pickWallColor(context: Context) {
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
            adapter = ColorAdapter(getWallpaperColors().apply {
                add(0xffee6170.toInt())
                add(0xff32afce.toInt())
                add(0xff37a051.toInt())
                add(0xffd35744.toInt())
                add(0xffddb63f.toInt())
                add(0xff5a5bfa.toInt())
                add(0xff5ae1be.toInt())
            }).apply { onItemClickListener = { color -> txt.setText(Integer.toHexString(color and 0xffffff)) }}
        }
        var updatingAllowed = true
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
                val txtColor = if (useDarkText(color)) -0xdad9d9 else -0x1
                txt.setTextColor(txtColor)
                okBtn.setTextColor(txtColor)
                okBtn.backgroundTintList = ColorStateList.valueOf(0x00ffffff and txtColor or 0x33000000)
            } catch (ignore: NumberFormatException) {}
        })
        txt.setText("000000")
        okBtn.setOnClickListener {
            d.dismiss()
            try {
                val myWallpaperManager = WallpaperManager.getInstance(context)
                val c = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                c.eraseColor(("ff" + txt.text.toString()).toLong(16).toInt())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (myWallpaperManager.isWallpaperSupported) {
                        try { myWallpaperManager.setBitmap(c) }
                        catch (ignore: Exception) {}
                    } else Toast.makeText(context, "For some reason wallpapers are not supported.", Toast.LENGTH_LONG).show()
                } else {
                    try { myWallpaperManager.setBitmap(c) }
                    catch (ignore: Exception) {}
                }
                context.startActivity(Intent(context, Main::class.java))
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "That's not a color.", Toast.LENGTH_SHORT).show()
            }
        }
        d.findViewById<View>(R.id.alpha)!!.visibility = View.GONE
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
                    val txtColor = if (useDarkText(color)) -0xdad9d9 else -0x1
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
                    val txtColor = if (useDarkText(color)) -0xdad9d9 else -0x1
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
                    val txtColor = if (useDarkText(color)) -0xdad9d9 else -0x1
                    txt.setTextColor(txtColor)
                    okBtn.setTextColor(txtColor)
                    okBtn.backgroundTintList = ColorStateList.valueOf(0x00ffffff and txtColor or 0x33000000)
                    updatingAllowed = true
                }
            }
        })
        d.show()
    }

    fun getWallpaperColors() = ArrayList<Int>().apply {
        if (ActivityCompat.checkSelfPermission(Tools.publicContext!!, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val palette = Palette.from(WallpaperManager.getInstance(Tools.publicContext).fastDrawable.toBitmap()).generate()
            add(palette.getDominantColor(0xff1155ff.toInt()))
            add(palette.getDarkVibrantColor(0xff1155ff.toInt()))
            add(palette.getVibrantColor(0xff1155ff.toInt()))
            add(palette.getLightMutedColor(0xff1155ff.toInt()))
            add(palette.getMutedColor(0xff1155ff.toInt()))
            add(palette.getDarkMutedColor(0xff1155ff.toInt()))
            add(palette.getLightVibrantColor(0xff1155ff.toInt()))
        }
    }

    class ColorAdapter(val colors: List<Int>) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

        var onItemClickListener: ((color: Int) -> Unit)? = null

        class ColorViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

        override fun getItemCount() = colors.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                ColorViewHolder(ImageView(Tools.publicContext).apply {
                    maxWidth = 72.dp.toInt()
                    maxHeight = 72.dp.toInt()
                    minimumWidth = 72.dp.toInt()
                    minimumHeight = 72.dp.toInt()
                    val p = 8.dp.toInt()
                    setPadding(p, p, p, p)
                })

        override fun onBindViewHolder(holder: ColorViewHolder, i: Int) {
            holder.imageView.apply {
                setImageDrawable(colorCircle(colors[i]))
                setOnClickListener {
                    onItemClickListener?.invoke(colors[i])
                }
            }
        }
    }
}