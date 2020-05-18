package posidon.launcher.search

import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.R
import posidon.launcher.storage.Settings
import posidon.launcher.tools.*
import posidon.launcher.view.NestedScrollView
import java.lang.StringBuilder

class ConsoleActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val font = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            resources.getFont(R.font.roboto_mono)
        } else Typeface.MONOSPACE
        val text = TextView(this).apply {
            typeface = font
            textSize = 12f
            setTextColor(0xffffffff.toInt())
            setPadding(20.dp.toInt(), 0, 20.dp.toInt(), 0)
        }
        val input = EditText(this).apply {
            typeface = font
            textSize = 12f
            imeOptions = EditorInfo.IME_ACTION_DONE
            setSingleLine()
            maxLines = 1
            background = null
            setTextColor(0xffffffff.toInt())
            setPadding(20.dp.toInt(), 0, 20.dp.toInt(), 12.dp.toInt())
        }
        val linearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(text)
            addView(input)
        }
        val scroll = NestedScrollView(this).apply {
            addView(linearLayout)
            isVerticalFadingEdgeEnabled = true
            setFadingEdgeLength(64.dp.toInt())
            descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        }
        setContentView(FrameLayout(this).apply {
            addView(scroll)
        })
        input.requestFocus()
        input.setOnEditorActionListener { _, actionId, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
                val string = input.text.toString()
                input.text.clear()
                if (string.isEmpty()) {
                    text.append("\n")
                } else {
                    text.append("\n> $string")
                    when (string) {
                        "hidden" -> startActivity(Intent(this, HiddenAppsActivity::class.java))
                        "ip" -> {
                            Loader.text("https://checkip.amazonaws.com") {
                                text.append("\n")
                                text.append(getText(R.string.ip_address_external))
                                text.append(" = ")
                                text.append(it.trimEnd())
                            }.execute()
                        }
                        "pi" -> text.append("\n\u03c0 = " + Math.PI)
                        else -> {
                            val tokens = string.split(' ')
                            when (tokens[0]) {
                                "conf" -> {
                                    if (tokens.size != 3 && tokens.size != 4) {
                                        text.append("\nusage: conf <type (int|float|string|bool)> <setting> <value (optional)>")
                                    } else when (tokens[1]) {
                                        "int" -> {
                                            val value: Int? = tokens[3].toIntOrNull()
                                            if (value == null) {
                                                text.append("\n\"${tokens[3]}\" isn't of type int!")
                                            } else {
                                                Settings[tokens[2]] = value
                                            }
                                        }
                                        "float" -> {
                                            val value: Float? = tokens[3].toFloatOrNull()
                                            if (value == null) {
                                                text.append("\n\"${tokens[3]}\" isn't of type float!")
                                            } else {
                                                Settings[tokens[2]] = value
                                            }
                                        }
                                        "string" -> Settings[tokens[2]] = tokens[3]
                                        "bool" -> {
                                            val value: Boolean = tokens[3] == "true"
                                            if (!value && tokens[3] != "false") {
                                                text.append("\n\"${tokens[3]}\" isn't of type bool!")
                                            } else {
                                                Settings[tokens[2]] = value
                                            }
                                        }
                                        else -> text.append("\n\"${tokens[1]}\" isn't a valid type!")
                                    }
                                }
                                else -> text.append("\n\"$string\" isn't a valid command!")
                            }
                        }
                    }
                }
                scroll.arrowScroll(1)
                scroll.post {
                    linearLayout.invalidate()
                    scroll.scrollY = linearLayout.measuredHeight
                }
            }
            true
        }
        window.setBackgroundDrawable(ColorDrawable(0xdd000000.toInt()))
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        hideKeyboard()
        finish()
    }
}