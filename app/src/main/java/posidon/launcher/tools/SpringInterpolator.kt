package posidon.launcher.tools

import android.view.animation.Interpolator
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

class SpringInterpolator : Interpolator {
    override fun getInterpolation(x: Float): Float {
        return 1 + (2f.pow(-10f * x) * sin(2 * PI * (x - 0.075f))).toFloat()
    }
}