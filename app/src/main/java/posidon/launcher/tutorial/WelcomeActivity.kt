package posidon.launcher.tutorial

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.R

class WelcomeActivity : AppCompatActivity() {

    private lateinit var img: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tutorial_welcome)
        img = findViewById(R.id.img)
        try {
            img.setImageResource(R.drawable.logo_wide)
            img.animate().alpha(1f).scaleX(1f).scaleY(1f).duration = 500L
        } catch (ignore: Exception) {}
    }

    override fun onResume() {
        super.onResume()
        img.alpha = 0f
        img.scaleX = 1.2f
        img.scaleY = 1.2f
        img.animate().alpha(1f).scaleX(1f).scaleY(1f).duration = 500L
    }

    fun start(v: View) {
        startActivity(Intent(this, Tutorial::class.java), ActivityOptions.makeCustomAnimation(this, R.anim.zoom_in_enter, R.anim.zoom_in_exit).toBundle())
    }
}
