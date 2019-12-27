package posidon.launcher.items

import android.content.pm.LauncherApps
import android.os.Build
import android.os.Bundle
import android.os.Process
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.tools.Dock

@RequiresApi(Build.VERSION_CODES.O)
class AddShortcutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_shortcut_activity)
        val launcherApps = getSystemService(LauncherApps::class.java)!!
        val shortcut = launcherApps.getPinItemRequest(intent).shortcutInfo
        if (shortcut != null) {
            Main.launcherApps.pinShortcuts(shortcut.`package`, ArrayList<String>().apply { add(shortcut.id) }, Process.myUserHandle())
            Dock.add(Shortcut(shortcut), 0)
        }
    }
}