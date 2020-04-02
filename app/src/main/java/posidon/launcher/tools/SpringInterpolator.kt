package posidon.launcher.tools

import android.view.animation.Interpolator

class SpringInterpolator : Interpolator {
    override fun getInterpolation(x: Float) = Tools.springInterpolate(x)
}