package posidon.launcher.search

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import posidon.android.conveniencelib.dp
import posidon.android.conveniencelib.hideKeyboard
import posidon.android.loader.text.TextLoader
import posidon.launcher.R
import posidon.launcher.drawable.FastColorDrawable
import posidon.launcher.storage.Settings
import posidon.launcher.view.NestedScrollView

class ConsoleActivity : AppCompatActivity() {

    val text by lazy {
        TextView(this).apply {
            textSize = 12f
            setTextColor(0xffffffff.toInt())
            setPadding(dp(20).toInt(), 0, dp(20).toInt(), 0)
        }
    }
    private val input by lazy {
        EditText(this).apply {
            textSize = 12f
            imeOptions = EditorInfo.IME_ACTION_DONE
            setSingleLine()
            maxLines = 1
            background = null
            setTextColor(0xffffffff.toInt())
            setPadding(dp(20).toInt(), 0, dp(20).toInt(), dp(12).toInt())
        }
    }
    private val linearLayout by lazy {
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(text)
            addView(input)
        }
    }
    private val scroll by lazy {
        NestedScrollView(this).apply {
            addView(linearLayout)
            isVerticalFadingEdgeEnabled = true
            setFadingEdgeLength(dp(64).toInt())
            descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val font = resources.getFont(R.font.roboto_mono)
        text.typeface = font
        input.typeface = font
        setContentView(FrameLayout(this).apply { addView(scroll) })
        input.requestFocus()
        input.setOnEditorActionListener { _, actionId, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
                val string = input.text.toString()
                input.text.clear()
                if (string.isEmpty()) {
                    print("\n")
                } else {
                    printCommand(string)
                    when (string) {
                        "help" -> {
                            print("\nhidden  | show the list of hidden apps")
                            print("\nip      | print the device ip address")
                            print("\npi      | print the value of pi")
                            print("\nconf    | set a setting (can cause crashes)")
                        }
                        "hidden" -> startActivity(Intent(this, HiddenAppsActivity::class.java))
                        "ip" -> {
                            TextLoader.load("https://checkip.amazonaws.com") { runOnUiThread {
                                printEquation(getText(R.string.ip_address_external), it.trimEnd())
                            }}
                        }
                        "pi" -> printEquation("\u03c0", Math.PI.toString())
                        else -> {
                            val tokens = string.split(' ')
                            when (tokens[0]) {
                                "conf" -> {
                                    when (tokens.size) {
                                        3 -> {
                                            when (tokens[1]) {
                                                "int" -> print("\n" + Settings.getInt(tokens[2]))
                                                "float" -> print("\n" + Settings.getFloat(tokens[2]))
                                                "string" -> print("\n" + Settings.getString(tokens[2])?.let { "\"$it\"" })
                                                "bool" -> print("\n" + Settings.getBoolean(tokens[2]))
                                                "list" -> print("\n" + Settings.getStrings(tokens[2])?.joinToString(prefix = "[", separator = ", ", postfix = "]") { "\"$it\"" })
                                                else -> printError("\n\"${tokens[1]}\" isn't a valid type!")
                                            }
                                        }
                                        4 -> when (tokens[1]) {
                                            "int" -> {
                                                val value: Int? = tokens[3].toIntOrNull()
                                                if (value == null) {
                                                    printError("\n\"${tokens[3]}\" isn't of type int!")
                                                } else {
                                                    Settings[tokens[2]] = value
                                                }
                                            }
                                            "float" -> {
                                                val value: Float? = tokens[3].toFloatOrNull()
                                                if (value == null) {
                                                    printError("\n\"${tokens[3]}\" isn't of type float!")
                                                } else {
                                                    Settings[tokens[2]] = value
                                                }
                                            }
                                            "string" -> Settings[tokens[2]] = tokens[3]
                                            "bool" -> {
                                                val value: Boolean = tokens[3] == "true"
                                                if (!value && tokens[3] != "false") {
                                                    printError("\n\"${tokens[3]}\" isn't of type bool!")
                                                } else {
                                                    Settings[tokens[2]] = value
                                                }
                                            }
                                            else -> printError("\n\"${tokens[1]}\" isn't a valid type!")
                                        }
                                        else -> print("\nusage: conf <type (int|float|string|bool|list(not writeable))> <setting> <value (optional)>")
                                    }
                                }
                                else -> printError("\n\"$string\" isn't a valid command!")
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
        window.setBackgroundDrawable(FastColorDrawable(0xdd000000.toInt()))
    }

    private inline fun print(any: Any?) = text.append(any.toString())
    private inline fun print(string: CharSequence) = text.append(string)

    private inline fun printError(string: CharSequence) = print(SpannableString(string).apply {
        setSpan(ForegroundColorSpan(0xffff1212.toInt()), 0, string.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    })

    private inline fun printEquation(name: CharSequence, value: CharSequence) {
        print("\n")
        print(SpannableString(name).apply {
            setSpan(ForegroundColorSpan(0xffb250fc.toInt()), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        })
        print(SpannableString(" = ").apply {
            setSpan(ForegroundColorSpan(0xff7e8082.toInt()), 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        })
        print(value)
    }

    private inline fun printCommand(string: CharSequence) {
        print("\n")
        print(SpannableString("> ").apply {
            setSpan(ForegroundColorSpan(0xff5dadf7.toInt()), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        })
        print(string)
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        hideKeyboard()
        finish()
    }
}