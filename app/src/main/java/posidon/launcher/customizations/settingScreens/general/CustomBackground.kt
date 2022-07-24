package posidon.launcher.customizations.settingScreens.general

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import io.posidon.android.conveniencelib.units.dp
import io.posidon.android.conveniencelib.units.toPixels
import posidon.launcher.Global
import posidon.launcher.R
import posidon.launcher.tools.Tools
import posidon.launcher.tools.theme.Customizer
import posidon.launcher.view.setting.NumberBarSettingView

class CustomBackground : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extras = intent.extras!!
        val namespace = extras.getString("namespace")!!
        val onlyCurveTop = extras.getBoolean("onlyCurveTop")
        val defColor = extras.getInt("defColor")
        val defRadius = extras.getInt("defRadius")
        val bgPreview: View
        setContentView(CoordinatorLayout(this).apply {
            fitsSystemWindows = true
            setPadding(0, 0, 0, Tools.navbarHeight)
            addView(NestedScrollView(context).apply {
                setFadingEdgeLength(64.dp.toPixels(context))
                isVerticalFadingEdgeEnabled = true
                addView(LinearLayout(context).apply {
                    orientation = VERTICAL
                    addView(TextView(context).apply {
                        gravity = Gravity.CENTER
                        setText(R.string.background)
                        setTextColor(0xffffffff.toInt())
                        textSize = 32f
                    }, ViewGroup.LayoutParams(MATCH_PARENT, 96.dp.toPixels(context)))
                    addView(View(context).apply {
                        background = Customizer.genBG(context, namespace, onlyCurveTop, defColor, defRadius)
                        bgPreview = this
                    }, ViewGroup.MarginLayoutParams(MATCH_PARENT, 160.dp.toPixels(context)).apply {
                        val m = 8.dp.toPixels(context)
                        setMargins(m, m, m, m)
                    })
                    addView(createSettings(bgPreview, namespace, onlyCurveTop, defColor, defRadius), ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
                }, ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
            }, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
        })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) window.setDecorFitsSystemWindows(false)
        else window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        Global.customized = true
    }

    override fun onPause() {
        Global.customized = true
        super.onPause()
    }

    private fun createSettings(bgPreview: View, namespace: String, onlyCurveTop: Boolean, defRadius: Int, defColor: Int): View {
        val l = LinearLayout(this).apply {
            orientation = VERTICAL
        }
        NumberBarSettingView(this).apply {
            label = getString(R.string.radius)
            //key = "$namespace:radius"
            //default = 30
            //max = 30
            //reload()
        }
        return l
        /*
        <LinearLayout
            style="@style/settingscard"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">
            <posidon.launcher.view.setting.NumberBarSettingView
                style="@style/settingsEntry"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                app:label="@string/radius"
                app:key="radius"
                app:def="30"
                app:max="30"/>
            <posidon.launcher.view.setting.ColorSettingView
                style="@style/settingsEntry"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                app:drawable="@drawable/ic_color"
                app:label="@string/color"
                app:key="color"
                app:def="#ff111213"/>
            <posidon.launcher.view.setting.SwitchSettingView
                style="@style/settingsEntry"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                app:drawable="@drawable/ic_color_dropper"
                app:label="@string/tint_background"
                app:key="news:cards:source:tint_bg"
                app:def="true"/>
        </LinearLayout>
         */
    }

    companion object {
        fun run(context: Context, namespace: String, onlyCurveTop: Boolean, defColor: Int, defRadius: Int) {
            val i = Intent(context, CustomBackground::class.java)
                .putExtra("namespace", namespace)
                .putExtra("onlyCurveTop", onlyCurveTop)
                .putExtra("defColor", defColor)
                .putExtra("defRadius", defRadius)
            context.startActivity(i)
        }
    }
}