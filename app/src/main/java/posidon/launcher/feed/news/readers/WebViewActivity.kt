package posidon.launcher.feed.news.readers

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class WebViewActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this)
        setContentView(webView)
        webView.settings.javaScriptEnabled = true
        val url = intent.extras!!.getString("url")!!
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                setTaskDescription(ActivityManager.TaskDescription(webView.title, webView.favicon))
            }
        }
        webView.loadUrl(url)
    }
}