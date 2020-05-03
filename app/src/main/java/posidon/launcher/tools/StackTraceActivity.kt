package posidon.launcher.tools

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import posidon.launcher.BuildConfig
import posidon.launcher.R


class StackTraceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stack_trace_activity)
        val t = intent.extras!!["throwable"] as Throwable

        val str = StringBuilder().apply {
            appendln(t.toString())
            appendln()
            appendln("Device.api: " + Build.VERSION.SDK_INT)
            appendln("Device.brand: " + Build.BRAND)
            appendln("Device.model: " + Build.MODEL)
            appendln("Version.code: " + BuildConfig.VERSION_CODE)
            appendln("Version.name: " + BuildConfig.VERSION_NAME)
            appendln()
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
                    .setSubject("posidon launcher: crash stack trace")
                    .addEmailTo("it@posidon.io")
                    .startChooser()
        }
    }
}