package posidon.launcher.wall

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.palette.graphics.Palette
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import posidon.launcher.R
import posidon.launcher.tools.Loader.bitmap
import posidon.launcher.tools.Tools
import posidon.launcher.tools.Tools.animate
import posidon.launcher.tools.Tools.applyFontSetting
import posidon.launcher.tools.Tools.centerCropWallpaper
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference

class WallActivity : AppCompatActivity() {
    private var loading: ImageView? = null
    private var index = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting(this)
        setContentView(R.layout.wall_preview)
        loading = findViewById(R.id.loading)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        if (img != null && img!!.height / img!!.width < resources.displayMetrics.heightPixels / resources.displayMetrics.widthPixels) img = centerCropWallpaper(this@WallActivity, img!!)
        findViewById<ImageView>(R.id.theimg).setImageBitmap(img)
        val extras = intent.extras
        if (extras == null) {
            loading!!.visibility = View.GONE
            findViewById<View>(R.id.downloadbtn).visibility = View.GONE
        } else {
            animate(loading!!.drawable)
            index = extras.getInt("index")
            bitmap(Gallery.walls[index].url, {
                var img = it!!
                if (img.height / img.width < resources.displayMetrics.heightPixels / resources.displayMetrics.widthPixels) img = centerCropWallpaper(this@WallActivity, img)
                //else img = Bitmap.createBitmap(img, 0, 0, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().widthPixels/img.getWidth()*img.getHeight());
                findViewById<ImageView>(R.id.theimg).setImageBitmap(img)
                WallActivity.img = img
                Tools.clearAnimation(loading!!.drawable)
                loading!!.visibility = View.GONE
            }).execute()
            findViewById<View>(R.id.downloadbtn).setOnClickListener { saveBitmap(img, Gallery.walls[index].name) }
            findViewById<View>(R.id.downloadbtn).background = btnBG()
            try {
                findViewById<TextView>(R.id.nametxt).text = Gallery.walls[index].name
                findViewById<TextView>(R.id.authortxt).text = Gallery.walls[index].author
            } catch (ignore: Exception) {}
        }
        var bottompadding = Tools.navbarHeight
        if (bottompadding == 0) bottompadding = (20 * resources.displayMetrics.density).toInt()
        findViewById<View>(R.id.bottomstuff).setPadding(0, 0, 0, bottompadding)
        findViewById<View>(R.id.applybtn).background = btnBG()
        findViewById<View>(R.id.applybtn).setOnClickListener {
            findViewById<View>(R.id.bottomstuff).animate().alpha(0f)
            val dialog = BottomSheetDialog(this@WallActivity, R.style.bottomsheet)
            dialog.setContentView(R.layout.wall_apply_dialog)
            dialog.window!!.findViewById<View>(R.id.design_bottom_sheet).setBackgroundColor(0x0)
            dialog.findViewById<View>(R.id.home)!!.background = dialogBtnBG()
            dialog.findViewById<View>(R.id.home)!!.setOnClickListener {
                SetWall(img!!, WeakReference(this@WallActivity), 0).execute()
                dialog.dismiss()
            }
            dialog.findViewById<View>(R.id.lock)!!.background = dialogBtnBG()
            dialog.findViewById<View>(R.id.lock)!!.setOnClickListener {
                SetWall(img!!, WeakReference(this@WallActivity), 1).execute()
                dialog.dismiss()
            }
            dialog.findViewById<View>(R.id.both)!!.background = dialogBtnBG()
            dialog.findViewById<View>(R.id.both)!!.setOnClickListener {
                SetWall(img!!, WeakReference(this@WallActivity), 2).execute()
                dialog.dismiss()
            }
            dialog.setOnDismissListener { findViewById<View>(R.id.bottomstuff).animate().alpha(1f) }
            dialog.show()
        }
        System.gc()
    }

    override fun onPause() {
        overridePendingTransition(R.anim.slideup, R.anim.slidedown)
        super.onPause()
    }

    private fun btnBG(): ShapeDrawable {
        val r = 24 * resources.displayMetrics.density
        val out = ShapeDrawable(RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null))
        out.paint.color = Palette.from(img!!).generate().getVibrantColor(-0xdad9d9)
        return out
    }

    private fun dialogBtnBG(): ShapeDrawable {
        val r = 24 * resources.displayMetrics.density
        val out = ShapeDrawable(RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null))
        out.paint.color = Palette.from(img!!).generate().getDarkMutedColor(Palette.from(img!!).generate().getDominantColor(-0xeeeded))
        return out
    }

    private fun saveBitmap(bitmap: Bitmap?, name: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        } else {
            val direct = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) getExternalFilesDir("Wallpapers") else File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/Wallpapers")
            if (direct != null && (direct.exists() || direct.mkdirs())) {
                val file = File(direct, name.replace(' ', '_') + ".png")
                if (file.exists()) Snackbar.make(findViewById(R.id.nametxt), "Already Saved: " + file.absolutePath, Snackbar.LENGTH_SHORT).show() else try {
                    val out = FileOutputStream(file)
                    bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                    out.close()
                    Snackbar.make(findViewById(R.id.nametxt), "Saved: " + file.absolutePath, Snackbar.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 0 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) saveBitmap(img, Gallery.walls[index].name)
    }

    companion object {
        @JvmField
        var img: Bitmap? = null
    }
}