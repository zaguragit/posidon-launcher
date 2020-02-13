package posidon.launcher.wall

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
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
import posidon.launcher.R
import posidon.launcher.tools.ColorTools.pickWallColor
import posidon.launcher.tools.Settings.init
import posidon.launcher.tools.Tools
import posidon.launcher.tools.Tools.animate
import posidon.launcher.tools.Tools.applyFontSetting
import posidon.launcher.tools.Tools.clearAnimation
import posidon.launcher.tools.Tools.getStatusBarHeight
import posidon.launcher.wall.WallActivity
import java.io.FileNotFoundException

class Gallery : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init(this)
        applyFontSetting(this)
        setContentView(R.layout.wall_gallery)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        val sidepadding = (28 * resources.displayMetrics.density).toInt()
        val gridsidepadding = (12 * resources.displayMetrics.density).toInt()
        val toolbarHeight = Tools.navbarHeight + (64 * resources.displayMetrics.density).toInt()
        findViewById<View>(R.id.toolbar).setPadding(sidepadding, 0, sidepadding, Tools.navbarHeight)
        findViewById<View>(R.id.toolbar).layoutParams.height = toolbarHeight
        findViewById<View>(R.id.gallery).setPadding(gridsidepadding, getStatusBarHeight(this), gridsidepadding, toolbarHeight)
        findViewById<View>(R.id.pickwallbtn).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    pickwall()
                } else {
                    AlertDialog.Builder(this@Gallery).setTitle("Permission needed").setMessage("posidon launcher needs the permission to read storage to access the images on your device")
                            .setPositiveButton("OK") { dialog, which -> requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 2) }
                            .setNegativeButton("NO") { dialog, which -> dialog.dismiss() }
                            .setIcon(getDrawable(R.drawable.ic_files)).show()
                }
            } else Toast.makeText(this@Gallery, "Please, grant the 'read external files' permission in settings", Toast.LENGTH_LONG).show()
        }
        findViewById<View>(R.id.colorwallbtn).setOnClickListener { pickWallColor(this@Gallery) }
        val loading = findViewById<ImageView>(R.id.loading)
        animate(loading.drawable)
        val gallery = findViewById<GridView>(R.id.gallery)
        val l = WallLoader()
        l.listener = { walls ->
            Companion.walls = walls
            if (loading.drawable is AnimatedVectorDrawable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) (loading.drawable as AnimatedVectorDrawable).clearAnimationCallbacks() else clearAnimation(loading.drawable)
            loading.visibility = View.GONE
            if (walls != null) {
                gallery.adapter = WallAdapter(this@Gallery)
                gallery.onItemClickListener = GalleryItemClickListener(this@Gallery)
            } else findViewById<View>(R.id.fail).visibility = View.VISIBLE
        }
        l.execute()
    }

    private fun pickwall() {
        val intent = Intent()
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, 1)
        System.gc()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 2) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) pickwall() else Toast.makeText(this, "No permission, no wallpapers ;(", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                WallActivity.img = null
                try {
                    WallActivity.img = BitmapFactory.decodeStream(baseContext.contentResolver.openInputStream(data!!.data!!))
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
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
        var walls: List<Wall> = emptyList()
    }
}