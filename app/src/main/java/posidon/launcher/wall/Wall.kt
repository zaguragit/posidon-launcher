package posidon.launcher.wall

import android.graphics.Bitmap

class Wall {
    var name: String? = null
    var img: Bitmap? = null
    var url: String? = null
    var author: String? = null
    var type: Type = Type.Bitmap

    enum class Type {
        Bitmap,
        SVG,
        Varied
    }
}