package posidon.launcher.external

import android.app.Activity
import android.os.Bundle
import android.os.PersistableBundle

class AutoFinishTransparentActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        finish()
    }
}