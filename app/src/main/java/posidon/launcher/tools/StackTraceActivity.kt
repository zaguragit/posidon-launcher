package posidon.launcher.tools

import android.app.ActivityManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import posidon.launcher.BuildConfig
import posidon.launcher.Home
import posidon.launcher.R
import posidon.launcher.storage.Settings


class StackTraceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        System.gc()
        kotlin.runCatching {
            if (Settings["dev:hide_crash_logs", true]) {
                startActivity(Intent(this, Home::class.java))
                finish()
                return
            }
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stack_trace_activity)
        val t = intent.extras!!["throwable"] as Throwable

        val str = StringBuilder().apply {
            appendLine(t.toString())
            appendLine()
            appendLine("Device info:")
            appendLine("    api: " + Build.VERSION.SDK_INT)
            appendLine("    brand: " + Build.BRAND)
            appendLine("    model: " + Build.MODEL)
            appendLine("    ram: " + run {
                val memInfo = ActivityManager.MemoryInfo()
                (getSystemService(ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(memInfo)
                memInfo.totalMem
            })
            appendLine("Version: " + BuildConfig.VERSION_NAME + " (code: " + BuildConfig.VERSION_CODE + ')')
            appendLine()
            for (tr in t.stackTrace) append("at: ").append(tr).append("\n")
            for (throwable in t.suppressed)
                for (tr in throwable.stackTrace)
                    append("at: ").append(tr).append("\n")
            t.cause?.let { for (tr in it.stackTrace) append("at: ").append(tr).append("\n") }
        }.toString()

        findViewById<TextView>(R.id.stackTrace).text = str

        findViewById<View>(R.id.send).setOnClickListener {
            ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(str)
                .setSubject("posidon launcher: crash log")
                .addEmailTo("it@posidon.io")
                .startChooser()
        }
    }
}