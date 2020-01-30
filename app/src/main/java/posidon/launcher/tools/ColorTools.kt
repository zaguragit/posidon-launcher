package posidon.launcher.tools

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
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
import android.widget.EditText
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.annotation.ColorInt
import com.google.android.material.bottomsheet.BottomSheetDialog
import posidon.launcher.Main
import posidon.launcher.R

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

    fun colorcircle(@ColorInt color: Int): Drawable {
        val d = GradientDrawable()
        d.shape = GradientDrawable.OVAL
        d.setColor(color)
        d.setStroke(1, -0x1000000)
        return d
    }

    fun pickColor(context: Context?, settingskey: String?, @ColorInt defaultcolor: Int) {
        val d = BottomSheetDialog(context!!, R.style.bottomsheet)
        d.setContentView(R.layout.color_picker)
        d.window!!.findViewById<View>(R.id.design_bottom_sheet).setBackgroundResource(R.drawable.bottom_sheet)
        val txt = d.findViewById<EditText>(R.id.hextxt)
        txt!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                try {
                    d.findViewById<View>(R.id.bgColorPrev)!!.setBackgroundColor(s.toString().toLong(16).toInt())
                } catch (ignore: NumberFormatException) {}
            }
        })
        txt.setText(Integer.toHexString(Settings[settingskey!!, defaultcolor]))
        txt.setTextColor(if (useDarkText(Settings[settingskey, defaultcolor])) -0xdad9d9 else -0x1)
        d.findViewById<View>(R.id.ok)!!.setOnClickListener {
            d.dismiss()
            try {
                Settings[settingskey] = txt.text.toString().toLong(16).toInt()
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "That's not a color.", Toast.LENGTH_SHORT).show()
            }
        }
        val alpha = d.findViewById<SeekBar>(R.id.alpha)
        val red = d.findViewById<SeekBar>(R.id.red)
        val green = d.findViewById<SeekBar>(R.id.green)
        val blue = d.findViewById<SeekBar>(R.id.blue)
        val hex = StringBuilder(txt.text.toString())
        while (hex.length != 8) hex.insert(0, 0)
        alpha!!.progress = hex.substring(0, 2).toLong(16).toInt()
        red!!.progress = hex.substring(2, 4).toLong(16).toInt()
        green!!.progress = hex.substring(4, 6).toLong(16).toInt()
        blue!!.progress = hex.substring(6, 8).toLong(16).toInt()
        alpha.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val color = progress * 256 * 256 * 256 + red.progress * 256 * 256 + green.progress * 256 + blue.progress
                txt.setText(Integer.toHexString(color))
                txt.setTextColor(if (useDarkText(color)) -0xdad9d9 else -0x1)
            }
        })
        red.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val color = alpha.progress * 256 * 256 * 256 + progress * 256 * 256 + green.progress * 256 + blue.progress
                txt.setText(Integer.toHexString(color))
                txt.setTextColor(if (useDarkText(color)) -0xdad9d9 else -0x1)
            }
        })
        green.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val color = alpha.progress * 256 * 256 * 256 + red.progress * 256 * 256 + progress * 256 + blue.progress
                txt.setText(Integer.toHexString(color))
                txt.setTextColor(if (useDarkText(color)) -0xdad9d9 else -0x1)
            }
        })
        blue.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val color = alpha.progress * 256 * 256 * 256 + red.progress * 256 * 256 + green.progress * 256 + progress
                txt.setText(Integer.toHexString(color))
                txt.setTextColor(if (useDarkText(color)) -0xdad9d9 else -0x1)
            }
        })
        System.gc()
        d.show()
    }

    fun pickColorNoAlpha(context: Context?, settingskey: String?, @ColorInt defaultcolor: Int) {
        val d = BottomSheetDialog(context!!, R.style.bottomsheet)
        d.setContentView(R.layout.color_picker)
        d.window!!.findViewById<View>(R.id.design_bottom_sheet).setBackgroundResource(R.drawable.bottom_sheet)
        val txt = d.findViewById<EditText>(R.id.hextxt)!!
        txt.filters = arrayOf<InputFilter>(LengthFilter(6))
        txt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                try {
                    d.findViewById<View>(R.id.bgColorPrev)!!.setBackgroundColor("ff$s".toLong(16).toInt())
                } catch (ignored: NumberFormatException) {}
            }
        })
        txt.setText(Integer.toHexString(Settings[settingskey!!, defaultcolor]))
        txt.setTextColor(if (useDarkText(Settings[settingskey, defaultcolor])) -0xdad9d9 else -0x1)
        d.findViewById<View>(R.id.ok)!!.setOnClickListener {
            d.dismiss()
            try {
                Settings[settingskey] = txt.text.toString().toLong(16).toInt()
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "That's not a color.", Toast.LENGTH_SHORT).show()
            }
        }
        d.findViewById<View>(R.id.alpha)!!.visibility = View.GONE
        val red = d.findViewById<SeekBar>(R.id.red)
        val green = d.findViewById<SeekBar>(R.id.green)
        val blue = d.findViewById<SeekBar>(R.id.blue)
        val hex = StringBuilder(txt.text.toString())
        while (hex.length != 8) hex.insert(0, 0)
        red!!.progress = hex.substring(2, 4).toLong(16).toInt()
        green!!.progress = hex.substring(4, 6).toLong(16).toInt()
        blue!!.progress = hex.substring(6, 8).toLong(16).toInt()
        red.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val color = progress * 256 * 256 + green.progress * 256 + blue.progress
                val hex = StringBuilder(Integer.toHexString(color))
                while (hex.length != 6) hex.insert(0, 0)
                txt.setText(hex.toString())
                txt.setTextColor(if (useDarkText(color)) -0xdad9d9 else -0x1)
            }
        })
        green.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val color = red.progress * 256 * 256 + progress * 256 + blue.progress
                val hex = StringBuilder(Integer.toHexString(color))
                while (hex.length != 6) hex.insert(0, 0)
                txt.setText(hex.toString())
                txt.setTextColor(if (useDarkText(color)) -0xdad9d9 else -0x1)
            }
        })
        blue.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val color = red.progress * 256 * 256 + green.progress * 256 + progress
                val hex = StringBuilder(Integer.toHexString(color))
                while (hex.length != 6) hex.insert(0, 0)
                txt.setText(hex.toString())
                txt.setTextColor(if (useDarkText(color)) -0xdad9d9 else -0x1)
            }
        })
        System.gc()
        d.show()
    }

    @JvmStatic
	fun pickWallColor(context: Context) {
        val d = BottomSheetDialog(context, R.style.bottomsheet)
        d.setContentView(R.layout.color_picker)
        d.window!!.findViewById<View>(R.id.design_bottom_sheet).setBackgroundResource(R.drawable.bottom_sheet)
        val txt = d.findViewById<EditText>(R.id.hextxt)!!
        txt.filters = arrayOf<InputFilter>(LengthFilter(6))
        txt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                try {
                    d.findViewById<View>(R.id.bgColorPrev)!!.setBackgroundColor("ff$s".toLong(16).toInt())
                } catch (ignore: NumberFormatException) {}
            }
        })
        txt.setText("000000")
        d.findViewById<View>(R.id.ok)!!.setOnClickListener {
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
        val red = d.findViewById<SeekBar>(R.id.red)
        val green = d.findViewById<SeekBar>(R.id.green)
        val blue = d.findViewById<SeekBar>(R.id.blue)
        red!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val color = progress * 256 * 256 + green!!.progress * 256 + blue!!.progress
                val hex = StringBuilder(Integer.toHexString(color))
                while (hex.length != 6) hex.insert(0, 0)
                txt.setText(hex.toString())
                txt.setTextColor(if (useDarkText(color)) -0xdad9d9 else -0x1)
            }
        })
        green!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val color = red.progress * 256 * 256 + progress * 256 + blue!!.progress
                val hex = StringBuilder(Integer.toHexString(color))
                while (hex.length != 6) hex.insert(0, 0)
                txt.setText(hex.toString())
                txt.setTextColor(if (useDarkText(color)) -0xdad9d9 else -0x1)
            }
        })
        blue!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val color = red.progress * 256 * 256 + green.progress * 256 + progress
                val hex = StringBuilder(Integer.toHexString(color))
                while (hex.length != 6) hex.insert(0, 0)
                txt.setText(hex.toString())
                txt.setTextColor(if (useDarkText(color)) -0xdad9d9 else -0x1)
            }
        })
        System.gc()
        d.show()
    }
}