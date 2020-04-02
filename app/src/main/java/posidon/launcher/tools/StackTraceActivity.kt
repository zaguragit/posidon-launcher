package posidon.launcher.tools

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import posidon.launcher.Main
import posidon.launcher.R


class StackTraceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stack_trace_activity)
        val t = intent.extras!!["throwable"] as Throwable
        findViewById<TextView>(R.id.throwable).apply {
            text = t.toString()
            setTextColor(ColorTools.blendColors(Main.accentColor, 0xffffffff.toInt(), 0.5f))
        }
        val strBuilder = StringBuilder()
        for (tr in t.stackTrace) strBuilder.append("at: ").append(tr).append("\n")
        for (throwable in t.suppressed)
            for (tr in throwable.stackTrace)
                strBuilder.append("at: ").append(tr).append("\n")
        t.cause?.let { for (tr in it.stackTrace) strBuilder.append("at: ").append(tr).append("\n") }
        findViewById<TextView>(R.id.stackTrace).text = strBuilder.toString()

        findViewById<View>(R.id.send).setOnClickListener {
            ShareCompat.IntentBuilder
                    .from(this)
                    .setType("text/plain")
                    .setText(t.toString() + '\n' + strBuilder.toString())
                    .setSubject("posidon launcher: crash stack trace")
                    .addEmailTo("it@posidon.io")
                    .startChooser()
        }
    }
}