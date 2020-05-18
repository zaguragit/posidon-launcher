package posidon.launcher.feed.news.readers

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.feed_web_view.*
import posidon.launcher.R

class ArticleActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.feed_web_view)
        val extras = intent.extras!!
        val url = extras.getString("url")
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                sourceTitle.apply {
                    text = webView.title
                    setCompoundDrawablesRelative(BitmapDrawable(webView.favicon), null, null, null)
                }
            }
        }
        webView.loadUrl(url)
    }

    fun exit(v: View) = finish()
}