package posidon.launcher.wall

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.GridView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pixplicity.sharp.Sharp
import posidon.launcher.R
import posidon.launcher.tools.ColorTools.pickWallColor
import posidon.launcher.tools.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.Tools.animate
import posidon.launcher.tools.Tools.applyFontSetting
import posidon.launcher.tools.Tools.clearAnimation
import posidon.launcher.tools.Tools.getStatusBarHeight
import posidon.launcher.tools.dp
import posidon.launcher.tools.toBitmap
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.net.URL
import kotlin.concurrent.thread

class Gallery : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings.init(this)
        applyFontSetting(this)
        setContentView(R.layout.wall_gallery)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        val sidepadding = (28 * resources.displayMetrics.density).toInt()
        val gridsidepadding = (15 * resources.displayMetrics.density).toInt()
        val toolbarHeight = Tools.navbarHeight + (64 * resources.displayMetrics.density).toInt()
        findViewById<View>(R.id.toolbar).setPadding(sidepadding, 0, sidepadding, Tools.navbarHeight)
        findViewById<View>(R.id.toolbar).layoutParams.height = toolbarHeight
        findViewById<View>(R.id.gallery).setPadding(gridsidepadding, getStatusBarHeight(this) + 4.dp(this).toInt(), gridsidepadding, toolbarHeight + 20.dp(this).toInt())
        findViewById<View>(R.id.pickwallbtn).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) pickFile()
                else requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 2)
            } else Toast.makeText(this@Gallery, "Please, grant the 'read external files' permission in settings", Toast.LENGTH_LONG).show()
        }
        findViewById<View>(R.id.colorwallbtn).setOnClickListener { pickWallColor(this@Gallery) }
        val loading = findViewById<ImageView>(R.id.loading)
        animate(loading.drawable)
        val gallery = findViewById<GridView>(R.id.gallery)


        thread {
            try {
                var currentWall = Wall()
                val bufferReader = BufferedReader(InputStreamReader(URL(REPO + INDEX_FILE).openStream()))
                bufferReader.forEachLine {
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
                                val d = REPO + IMG_PATH + it.substring(2)
                                when (currentWall.type) {
                                    Wall.Type.Bitmap -> {
                                        val stream = URL("$d/thumb.jpg").openConnection().getInputStream()
                                        currentWall.img = BitmapFactory.decodeStream(stream)
                                        stream.close()
                                        currentWall.url = "$d/img.png"
                                    }
                                    Wall.Type.SVG -> {
                                        val stream = URL("$d/img.svg").openConnection().getInputStream()
                                        val drawable = Sharp.loadInputStream(stream).drawable
                                        stream.close()
                                        if (drawable != null) currentWall.img = drawable.toBitmap(420, (420f * drawable.intrinsicHeight / drawable.intrinsicWidth).toInt())
                                        currentWall.url = "$d/img.svg"
                                    }
                                }
                            }
                        }
                    }
                }
                runOnUiThread {
                    if (loading.drawable is AnimatedVectorDrawable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) (loading.drawable as AnimatedVectorDrawable).clearAnimationCallbacks() else clearAnimation(loading.drawable)
                    loading.visibility = View.GONE
                    gallery.adapter = WallAdapter(this@Gallery)
                    gallery.setOnItemClickListener { _, _, i, _ ->
                        val intent = Intent(this, WallActivity::class.java)
                        intent.putExtra("index", i)
                        WallActivity.img = walls[i].img
                        startActivity(intent, ActivityOptions.makeCustomAnimation(this, R.anim.slideup, R.anim.slidedown).toBundle())
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    if (loading.drawable is AnimatedVectorDrawable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) (loading.drawable as AnimatedVectorDrawable).clearAnimationCallbacks() else clearAnimation(loading.drawable)
                    loading.visibility = View.GONE
                    findViewById<View>(R.id.fail).visibility = View.VISIBLE
                }
                e.printStackTrace()
            }
        }
    }

    private fun pickFile() {
        val intent = Intent()
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, 1)
        System.gc()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 2 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) pickFile()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                WallActivity.img = null
                try { WallActivity.img = BitmapFactory.decodeStream(baseContext.contentResolver.openInputStream(data!!.data!!)) }
                catch (e: FileNotFoundException) { e.printStackTrace() }
                startActivity(Intent(this, WallActivity::class.java))
                System.gc()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        overridePendingTransition(R.anim.fadein, R.anim.slidedown)
        super.onPause()
    }

    companion object {
        const val REPO = "https://raw.githubusercontent.com/leoxshn/walls/master/"
        const val INDEX_FILE = "index"
        const val IMG_PATH = "img/"
        var walls = ArrayList<Wall>()
    }
}