package posidon.launcher.feed.news.readers

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ArticleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(0)
        val extras = intent.extras!!
        val url = extras.getString("url")
    }
}