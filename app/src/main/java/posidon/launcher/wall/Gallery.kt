package posidon.launcher.wall

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.GridView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pixplicity.sharp.Sharp
import posidon.android.conveniencelib.Graphics
import posidon.android.conveniencelib.dp
import posidon.android.conveniencelib.toBitmap
import posidon.launcher.Global
import posidon.launcher.Home
import posidon.launcher.R
import posidon.launcher.drawable.FastColorDrawable
import posidon.launcher.storage.ExternalStorage
import posidon.launcher.storage.Settings
import posidon.launcher.tools.*
import posidon.launcher.tools.theme.ColorTools.pickColorNoAlpha
import posidon.launcher.tools.theme.applyFontSetting
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.net.URL
import kotlin.concurrent.thread

class Gallery : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings.init(applicationContext)
        applyFontSetting()
        setContentView(R.layout.wall_gallery)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) window.setDecorFitsSystemWindows(false)
        else window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.setBackgroundDrawable(FastColorDrawable(Global.getBlackAccent()))
        val sidepadding = dp(28).toInt()
        val gridsidepadding = dp(15).toInt()
        val toolbarHeight = Tools.navbarHeight + dp(64).toInt()
        findViewById<View>(R.id.toolbar).setPadding(sidepadding, 0, sidepadding, Tools.navbarHeight)
        findViewById<View>(R.id.toolbar).layoutParams.height = toolbarHeight
        findViewById<View>(R.id.gallery).setPadding(gridsidepadding, getStatusBarHeight() + dp(4).toInt(), gridsidepadding, toolbarHeight + dp(20).toInt())
        findViewById<View>(R.id.pickwallbtn).setOnClickListener {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) pickFile()
            else requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 2)
        }
        findViewById<View>(R.id.colorwallbtn).setOnClickListener {
            pickColorNoAlpha(this, 0) {
                val wallManager = WallpaperManager.getInstance(this)
                val c = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                c.eraseColor(0xff000000.toInt() or it)
                if (wallManager.isWallpaperSupported) {
                    try { wallManager.setBitmap(c) }
                    catch (e: Exception) {}
                } else Toast.makeText(this, "For some reason wallpapers are not supported.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, Home::class.java))
            }
        }
        val loading = findViewById<ImageView>(R.id.loading)
        Graphics.tryAnimate(this, loading.drawable)
        val gallery = findViewById<GridView>(R.id.gallery)


        thread(isDaemon = true) {
            try {
                var currentWall = Wall()
                val input = InputStreamReader(URL(REPO + INDEX_FILE).openStream())
                val bufferReader = BufferedReader(input)
                bufferReader.forEachLine {
                    try {
                        if (it.isEmpty()) {
                            walls.add(currentWall)
                            currentWall = Wall()
                        } else {
                            when (it[0]) {
                                'n' -> currentWall.name = it.substring(2)
                                'a' -> currentWall.author = it.substring(2)
                                't' -> currentWall.type = when (it.substring(2)) {
                                    "svg" -> Wall.Type.SVG
                                    "varied" -> Wall.Type.Varied
                                    else -> Wall.Type.Bitmap
                                }
                                'd' -> {
                                    val dir = it.substring(2)
                                    val builder = StringBuilder(REPO).append(IMG_PATH).append(dir)
                                    when (currentWall.type) {
                                        Wall.Type.Bitmap -> {
                                            val stream = URL(builder.append("/thumb.jpg").toString()).openConnection().getInputStream()
                                            currentWall.img = BitmapFactory.decodeStream(stream)
                                            stream.close()
                                            currentWall.url = dir
                                        }
                                        Wall.Type.SVG -> {
                                            val stream = URL(builder.append("/img.svg").toString()).openConnection().getInputStream()
                                            val drawable = Sharp.loadInputStream(stream).drawable
                                            stream.close()
                                            if (drawable != null) {
                                                currentWall.img = drawable.toBitmap(420, (420f * drawable.intrinsicHeight / drawable.intrinsicWidth).toInt())
                                            }
                                            currentWall.url = dir
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }
                input.close()
                runOnUiThread {
                    loading.visibility = View.GONE
                    gallery.adapter = WallAdapter(this@Gallery)
                    Graphics.clearAnimation(loading.drawable)
                    gallery.setOnItemClickListener { _, _, i, _ ->
                        val intent = Intent(this, WallActivity::class.java)
                        intent.putExtra("index", i)
                        WallActivity.img = walls[i].img
                        startActivity(intent, ActivityOptions.makeCustomAnimation(this, R.anim.slideup, R.anim.slidedown).toBundle())
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    loading.visibility = View.GONE
                    //findViewById<View>(R.id.fail).visibility = View.VISIBLE
                    Graphics.clearAnimation(loading.drawable)
                }
                e.printStackTrace()
            }
        }
    }

    private fun pickFile() = ExternalStorage.pickFile(this, "image/*")

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 2 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickFile()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            ExternalStorage.onActivityResultPickFile(this, requestCode, data) {
                WallActivity.img = null
                if (it != null) {
                    try { WallActivity.img = BitmapFactory.decodeStream(it) }
                    catch (e: FileNotFoundException) { e.printStackTrace() }
                }
                startActivity(Intent(this, WallActivity::class.java))
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        overridePendingTransition(R.anim.fadein, R.anim.slidedown)
        super.onPause()
    }

    companion object {
        const val REPO = "https://raw.githubusercontent.com/lposidon/walls/master/"
        const val INDEX_FILE = "index"
        const val IMG_PATH = "img/"
        var walls = ArrayList<Wall>()
    }
}