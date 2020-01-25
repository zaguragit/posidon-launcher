package posidon.launcher.external

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Main
import posidon.launcher.tools.Settings

class ApplyIcons : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try { Settings.put("iconpack", intent.extras!!.getString("iconpack")) }
        catch (ignore: Exception) {}
        Main.shouldSetApps = true
        startActivity(Intent(this, Main::class.java))
        finish()
    }
}