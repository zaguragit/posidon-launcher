package posidon.launcher.external

import android.annotation.TargetApi
import android.content.ComponentName
import android.content.Intent
import android.graphics.RectF
import android.os.*
import android.util.Log
import android.view.SurfaceControl

/**
 * Class to encapsulate the handshake protocol between Launcher and gestureNav.
 */
class GestureNavContract(
    val componentName: ComponentName,
    val user: UserHandle,
    private val callback: Message
) {
    /**
     * Sends the position information to the receiver
     */
    @TargetApi(Build.VERSION_CODES.Q)
    fun sendEndPosition(position: RectF?, surfaceControl: SurfaceControl?) {
        val result = Bundle()
        result.putParcelable(EXTRA_ICON_POSITION, position)
        result.putParcelable(EXTRA_ICON_SURFACE, surfaceControl)
        val callback = Message.obtain()
        callback.copyFrom(this.callback)
        callback.data = result
        try {
            callback.replyTo.send(callback)
        } catch (e: RemoteException) {
            Log.e(TAG, "Error sending icon position", e)
        }
    }

    companion object {
        private const val TAG = "GestureNavContract"
        const val EXTRA_GESTURE_CONTRACT = "gesture_nav_contract_v1"
        const val EXTRA_ICON_POSITION = "gesture_nav_contract_icon_position"
        const val EXTRA_ICON_SURFACE = "gesture_nav_contract_surface_control"
        const val EXTRA_REMOTE_CALLBACK = "android.intent.extra.REMOTE_CALLBACK"

        /**
         * Clears and returns the GestureNavContract if it was present in the intent.
         */
        fun fromIntent(intent: Intent): GestureNavContract? {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
            val extras = intent.getBundleExtra(EXTRA_GESTURE_CONTRACT)
                ?: return null
            intent.removeExtra(EXTRA_GESTURE_CONTRACT)
            val componentName = extras.getParcelable<ComponentName>(Intent.EXTRA_COMPONENT_NAME)
            val userHandle = extras.getParcelable<UserHandle>(Intent.EXTRA_USER)
            val callback = extras.getParcelable<Message>(EXTRA_REMOTE_CALLBACK)
            return if (componentName != null && userHandle != null && callback != null && callback.replyTo != null) {
                GestureNavContract(componentName, userHandle, callback)
            } else null
        }
    }
}