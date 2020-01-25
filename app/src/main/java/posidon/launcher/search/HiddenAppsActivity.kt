package posidon.launcher.search

import android.os.Bundle
import android.widget.GridView
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.R
import posidon.launcher.items.App
import posidon.launcher.items.DrawerAdapter
import posidon.launcher.tools.Settings

class HiddenAppsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hidden_apps_activity)
        val grid = findViewById<GridView>(R.id.grid)
        grid.numColumns = Settings.getInt("drawer:columns", 4)
        grid.adapter = DrawerAdapter(this, App.hidden.toTypedArray())
        grid.setOnItemClickListener { _, view, i, _ ->
            App.hidden[i].open(this, view)
        }
    }
}