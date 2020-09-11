package posidon.launcher.external

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Global
import posidon.launcher.Home
import posidon.launcher.storage.Settings

class ApplyIcons : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try { Settings["iconpack"] = intent.extras!!.getString("iconpack") }
        catch (ignore: Exception) {}
        Global.shouldSetApps = true
        startActivity(Intent(this, Home::class.java))
        finish()
    }
}