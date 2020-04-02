/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package posidon.launcher.external.quickstep

import android.graphics.Rect
import android.os.*
import android.view.MotionEvent

/**
 * Temporary callbacks into SystemUI.
 */
interface ISystemUiProxy : IInterface {
    /** Local-side IPC implementation stub class.  */
    abstract class Stub : Binder(), ISystemUiProxy {
        override fun asBinder(): IBinder {
            return this
        }

        @Throws(RemoteException::class)
        public override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            val descriptor = interfaceDescriptor!!
            return when (code) {
                IBinder.INTERFACE_TRANSACTION -> {
                    reply!!.writeString(descriptor)
                    true
                }
                TRANSACTION_startScreenPinning -> {
                    data.enforceInterface(descriptor)
                    val _arg0: Int
                    _arg0 = data.readInt()
                    startScreenPinning(_arg0)
                    reply!!.writeNoException()
                    true
                }
                TRANSACTION_onSplitScreenInvoked -> {
                    data.enforceInterface(descriptor)
                    onSplitScreenInvoked()
                    reply!!.writeNoException()
                    true
                }
                TRANSACTION_onOverviewShown -> {
                    data.enforceInterface(descriptor)
                    val _arg0: Boolean
                    _arg0 = 0 != data.readInt()
                    onOverviewShown(_arg0)
                    reply!!.writeNoException()
                    true
                }
                TRANSACTION_getNonMinimizedSplitScreenSecondaryBounds -> {
                    data.enforceInterface(descriptor)
                    val _result = nonMinimizedSplitScreenSecondaryBounds
                    reply!!.writeNoException()
                    if (_result != null) {
                        reply.writeInt(1)
                        _result.writeToParcel(reply, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
                    } else {
                        reply.writeInt(0)
                    }
                    true
                }
                TRANSACTION_setNavBarButtonAlpha -> {
                    data.enforceInterface(descriptor)
                    val _arg0: Float
                    _arg0 = data.readFloat()
                    val _arg1: Boolean
                    _arg1 = 0 != data.readInt()
                    setNavBarButtonAlpha(_arg0, _arg1)
                    reply!!.writeNoException()
                    true
                }
                TRANSACTION_onStatusBarMotionEvent -> {
                    data.enforceInterface(descriptor)
                    val _arg0: MotionEvent?
                    _arg0 = if (0 != data.readInt()) {
                        MotionEvent.CREATOR.createFromParcel(data)
                    } else {
                        null
                    }
                    onStatusBarMotionEvent(_arg0)
                    reply!!.writeNoException()
                    true
                }
                TRANSACTION_onAssistantProgress -> {
                    data.enforceInterface(descriptor)
                    val _arg0: Float
                    _arg0 = data.readFloat()
                    onAssistantProgress(_arg0)
                    reply!!.writeNoException()
                    true
                }
                TRANSACTION_onAssistantGestureCompletion -> {
                    data.enforceInterface(descriptor)
                    val _arg0: Float
                    _arg0 = data.readFloat()
                    onAssistantGestureCompletion(_arg0)
                    reply!!.writeNoException()
                    true
                }
                TRANSACTION_startAssistant -> {
                    data.enforceInterface(descriptor)
                    val _arg0: Bundle?
                    _arg0 = if (0 != data.readInt()) {
                        Bundle.CREATOR.createFromParcel(data)
                    } else {
                        null
                    }
                    startAssistant(_arg0)
                    reply!!.writeNoException()
                    true
                }
                TRANSACTION_monitorGestureInput -> {
                    data.enforceInterface(descriptor)
                    val _arg0: String?
                    _arg0 = data.readString()
                    val _arg1: Int
                    _arg1 = data.readInt()
                    val _result = monitorGestureInput(_arg0, _arg1)
                    reply!!.writeNoException()
                    if (_result != null) {
                        reply.writeInt(1)
                        _result.writeToParcel(reply, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
                    } else {
                        reply.writeInt(0)
                    }
                    true
                }
                TRANSACTION_notifyAccessibilityButtonClicked -> {
                    data.enforceInterface(descriptor)
                    val _arg0: Int
                    _arg0 = data.readInt()
                    notifyAccessibilityButtonClicked(_arg0)
                    reply!!.writeNoException()
                    true
                }
                TRANSACTION_notifyAccessibilityButtonLongClicked -> {
                    data.enforceInterface(descriptor)
                    notifyAccessibilityButtonLongClicked()
                    reply!!.writeNoException()
                    true
                }
                TRANSACTION_stopScreenPinning -> {
                    data.enforceInterface(descriptor)
                    stopScreenPinning()
                    reply!!.writeNoException()
                    true
                }
                else -> {
                    super.onTransact(code, data, reply, flags)
                }
            }
        }

        private class Proxy internal constructor(private val mRemote: IBinder) : ISystemUiProxy {
            override fun asBinder(): IBinder {
                return mRemote
            }

            /**
             * Proxies SurfaceControl.screenshotToBuffer().
             * @Removed
             * GraphicBufferCompat screenshot(in Rect sourceCrop, int width, int height, int minLayer,
             * int maxLayer, boolean useIdentityTransform, int rotation) = 0;
             */
            /**
             * Begins screen pinning on the provided {@param taskId}.
             */
            @Throws(RemoteException::class)
            override fun startScreenPinning(taskId: Int) {
                val _data = Parcel.obtain()
                val _reply = Parcel.obtain()
                try {
                    _data.writeInterfaceToken(interfaceDescriptor)
                    _data.writeInt(taskId)
                    mRemote.transact(TRANSACTION_startScreenPinning, _data, _reply, 0)
                    _reply.readException()
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
            }

            /**
             * Notifies SystemUI that split screen has been invoked.
             */
            @Throws(RemoteException::class)
            override fun onSplitScreenInvoked() {
                val _data = Parcel.obtain()
                val _reply = Parcel.obtain()
                try {
                    _data.writeInterfaceToken(interfaceDescriptor)
                    mRemote.transact(TRANSACTION_onSplitScreenInvoked, _data, _reply, 0)
                    _reply.readException()
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
            }

            /**
             * Notifies SystemUI that Overview is shown.
             */
            @Throws(RemoteException::class)
            override fun onOverviewShown(fromHome: Boolean) {
                val _data = Parcel.obtain()
                val _reply = Parcel.obtain()
                try {
                    _data.writeInterfaceToken(interfaceDescriptor)
                    _data.writeInt(if (fromHome) 1 else 0)
                    mRemote.transact(TRANSACTION_onOverviewShown, _data, _reply, 0)
                    _reply.readException()
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
            }

            /**
             * Get the secondary split screen app's rectangle when not minimized.
             */
            override val nonMinimizedSplitScreenSecondaryBounds: Rect?
                @Throws(RemoteException::class)
                get() {
                    val _data = Parcel.obtain()
                    val _reply = Parcel.obtain()
                    val _result: Rect?
                    _result = try {
                        _data.writeInterfaceToken(interfaceDescriptor)
                        mRemote.transact(TRANSACTION_getNonMinimizedSplitScreenSecondaryBounds, _data, _reply, 0)
                        _reply.readException()
                        if (0 != _reply.readInt()) Rect.CREATOR.createFromParcel(_reply) else null
                    } finally {
                        _reply.recycle()
                        _data.recycle()
                    }
                    return _result
                }
            /**
             * Control the {@param alpha} of the back button in the navigation bar and {@param animate} if
             * needed from current value
             */
            //void setBackButtonAlpha(float alpha, boolean animate) = 8;
            /**
             * Control the {@param alpha} of the option nav bar button (back-button in 2 button mode
             * and home bar in no-button mode) and {@param animate} if needed from current value
             */
            @Throws(RemoteException::class)
            override fun setNavBarButtonAlpha(alpha: Float, animate: Boolean) {
                val _data = Parcel.obtain()
                val _reply = Parcel.obtain()
                try {
                    _data.writeInterfaceToken(interfaceDescriptor)
                    _data.writeFloat(alpha)
                    _data.writeInt(if (animate) 1 else 0)
                    mRemote.transact(TRANSACTION_setNavBarButtonAlpha, _data, _reply, 0)
                    _reply.readException()
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
            }

            /**
             * Proxies motion events from the homescreen UI to the status bar. Only called when
             * swipe down is detected on WORKSPACE. The sender guarantees the following order of events on
             * the tracking pointer.
             *
             * Normal gesture: DOWN, MOVE/POINTER_DOWN/POINTER_UP)*, UP or CANCLE
             */
            @Throws(RemoteException::class)
            override fun onStatusBarMotionEvent(event: MotionEvent?) {
                val _data = Parcel.obtain()
                val _reply = Parcel.obtain()
                try {
                    _data.writeInterfaceToken(interfaceDescriptor)
                    if (event != null) {
                        _data.writeInt(1)
                        event.writeToParcel(_data, 0)
                    } else {
                        _data.writeInt(0)
                    }
                    mRemote.transact(TRANSACTION_onStatusBarMotionEvent, _data, _reply, 0)
                    _reply.readException()
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
            }

            /**
             * Proxies the assistant gesture's progress started from navigation bar.
             */
            @Throws(RemoteException::class)
            override fun onAssistantProgress(progress: Float) {
                val _data = Parcel.obtain()
                val _reply = Parcel.obtain()
                try {
                    _data.writeInterfaceToken(interfaceDescriptor)
                    _data.writeFloat(progress)
                    mRemote.transact(TRANSACTION_onAssistantProgress, _data, _reply, 0)
                    _reply.readException()
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
            }

            /**
             * Proxies the assistant gesture fling velocity (in pixels per millisecond) upon completion.
             * Velocity is 0 for drag gestures.
             */
            @Throws(RemoteException::class)
            override fun onAssistantGestureCompletion(velocity: Float) {
                val _data = Parcel.obtain()
                val _reply = Parcel.obtain()
                try {
                    _data.writeInterfaceToken(interfaceDescriptor)
                    _data.writeFloat(velocity)
                    mRemote.transact(TRANSACTION_onAssistantGestureCompletion, _data, _reply, 0)
                    _reply.readException()
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
            }

            /**
             * Start the assistant.
             */
            @Throws(RemoteException::class)
            override fun startAssistant(bundle: Bundle?) {
                val _data = Parcel.obtain()
                val _reply = Parcel.obtain()
                try {
                    _data.writeInterfaceToken(interfaceDescriptor)
                    if (bundle != null) {
                        _data.writeInt(1)
                        bundle.writeToParcel(_data, 0)
                    } else {
                        _data.writeInt(0)
                    }
                    mRemote.transact(TRANSACTION_startAssistant, _data, _reply, 0)
                    _reply.readException()
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
            }

            /**
             * Creates a new gesture monitor
             */
            @Throws(RemoteException::class)
            override fun monitorGestureInput(name: String?, displayId: Int): Bundle? {
                val _data = Parcel.obtain()
                val _reply = Parcel.obtain()
                val _result: Bundle?
                _result = try {
                    _data.writeInterfaceToken(interfaceDescriptor)
                    _data.writeString(name)
                    _data.writeInt(displayId)
                    mRemote.transact(TRANSACTION_monitorGestureInput, _data, _reply, 0)
                    _reply.readException()
                    if (0 != _reply.readInt()) Bundle.CREATOR.createFromParcel(_reply) else null
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
                return _result
            }

            /**
             * Notifies that the accessibility button in the system's navigation area has been clicked
             */
            @Throws(RemoteException::class)
            override fun notifyAccessibilityButtonClicked(displayId: Int) {
                val _data = Parcel.obtain()
                val _reply = Parcel.obtain()
                try {
                    _data.writeInterfaceToken(interfaceDescriptor)
                    _data.writeInt(displayId)
                    mRemote.transact(TRANSACTION_notifyAccessibilityButtonClicked, _data, _reply, 0)
                    _reply.readException()
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
            }

            /**
             * Notifies that the accessibility button in the system's navigation area has been long clicked
             */
            @Throws(RemoteException::class)
            override fun notifyAccessibilityButtonLongClicked() {
                val _data = Parcel.obtain()
                val _reply = Parcel.obtain()
                try {
                    _data.writeInterfaceToken(interfaceDescriptor)
                    mRemote.transact(TRANSACTION_notifyAccessibilityButtonLongClicked, _data, _reply, 0)
                    _reply.readException()
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
            }

            /**
             * Ends the system screen pinning.
             */
            @Throws(RemoteException::class)
            override fun stopScreenPinning() {
                val _data = Parcel.obtain()
                val _reply = Parcel.obtain()
                try {
                    _data.writeInterfaceToken(interfaceDescriptor)
                    mRemote.transact(TRANSACTION_stopScreenPinning, _data, _reply, 0)
                    _reply.readException()
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
            }

        }

        companion object {
            const val interfaceDescriptor = "ISystemUiProxy"

            /**
             * Cast an IBinder object into an ISystemUiProxy interface,
             * generating a proxy if needed.
             */
            fun asInterface(obj: IBinder?): ISystemUiProxy? {
                if (obj == null) return null
                val iin = obj.queryLocalInterface(interfaceDescriptor)
                return if (iin != null && iin is ISystemUiProxy) iin else Proxy(obj)
            }

            const val TRANSACTION_startScreenPinning = IBinder.FIRST_CALL_TRANSACTION + 1
            const val TRANSACTION_onSplitScreenInvoked = IBinder.FIRST_CALL_TRANSACTION + 5
            const val TRANSACTION_onOverviewShown = IBinder.FIRST_CALL_TRANSACTION + 6
            const val TRANSACTION_getNonMinimizedSplitScreenSecondaryBounds = IBinder.FIRST_CALL_TRANSACTION + 7
            const val TRANSACTION_setNavBarButtonAlpha = IBinder.FIRST_CALL_TRANSACTION + 19
            const val TRANSACTION_onStatusBarMotionEvent = IBinder.FIRST_CALL_TRANSACTION + 9
            const val TRANSACTION_onAssistantProgress = IBinder.FIRST_CALL_TRANSACTION + 12
            const val TRANSACTION_onAssistantGestureCompletion = IBinder.FIRST_CALL_TRANSACTION + 18
            const val TRANSACTION_startAssistant = IBinder.FIRST_CALL_TRANSACTION + 13
            const val TRANSACTION_monitorGestureInput = IBinder.FIRST_CALL_TRANSACTION + 14
            const val TRANSACTION_notifyAccessibilityButtonClicked = IBinder.FIRST_CALL_TRANSACTION + 15
            const val TRANSACTION_notifyAccessibilityButtonLongClicked = IBinder.FIRST_CALL_TRANSACTION + 16
            const val TRANSACTION_stopScreenPinning = IBinder.FIRST_CALL_TRANSACTION + 17
        }

        /** Construct the stub at attach it to the interface.  */
        init { attachInterface(this, interfaceDescriptor) }
    }
    /**
     * Proxies SurfaceControl.screenshotToBuffer().
     * @Removed
     * GraphicBufferCompat screenshot(in Rect sourceCrop, int width, int height, int minLayer,
     * int maxLayer, boolean useIdentityTransform, int rotation) = 0;
     */
    /**
     * Begins screen pinning on the provided {@param taskId}.
     */
    @Throws(RemoteException::class)
    fun startScreenPinning(taskId: Int)

    /**
     * Notifies SystemUI that split screen has been invoked.
     */
    @Throws(RemoteException::class)
    fun onSplitScreenInvoked()

    /**
     * Notifies SystemUI that Overview is shown.
     */
    @Throws(RemoteException::class)
    fun onOverviewShown(fromHome: Boolean)

    /**
     * Get the secondary split screen app's rectangle when not minimized.
     */
    @get:Throws(RemoteException::class)
    val nonMinimizedSplitScreenSecondaryBounds: Rect?
    /**
     * Control the {@param alpha} of the back button in the navigation bar and {@param animate} if
     * needed from current value
     */
    //void setBackButtonAlpha(float alpha, boolean animate) = 8;
    /**
     * Control the {@param alpha} of the option nav bar button (back-button in 2 button mode
     * and home bar in no-button mode) and {@param animate} if needed from current value
     */
    @Throws(RemoteException::class)
    fun setNavBarButtonAlpha(alpha: Float, animate: Boolean)

    /**
     * Proxies motion events from the homescreen UI to the status bar. Only called when
     * swipe down is detected on WORKSPACE. The sender guarantees the following order of events on
     * the tracking pointer.
     *
     * Normal gesture: DOWN, MOVE/POINTER_DOWN/POINTER_UP)*, UP or CANCLE
     */
    @Throws(RemoteException::class)
    fun onStatusBarMotionEvent(event: MotionEvent?)

    /**
     * Proxies the assistant gesture's progress started from navigation bar.
     */
    @Throws(RemoteException::class)
    fun onAssistantProgress(progress: Float)

    /**
     * Proxies the assistant gesture fling velocity (in pixels per millisecond) upon completion.
     * Velocity is 0 for drag gestures.
     */
    @Throws(RemoteException::class)
    fun onAssistantGestureCompletion(velocity: Float)

    /**
     * Start the assistant.
     */
    @Throws(RemoteException::class)
    fun startAssistant(bundle: Bundle?)

    /**
     * Creates a new gesture monitor
     */
    @Throws(RemoteException::class)
    fun monitorGestureInput(name: String?, displayId: Int): Bundle?

    /**
     * Notifies that the accessibility button in the system's navigation area has been clicked
     */
    @Throws(RemoteException::class)
    fun notifyAccessibilityButtonClicked(displayId: Int)

    /**
     * Notifies that the accessibility button in the system's navigation area has been long clicked
     */
    @Throws(RemoteException::class)
    fun notifyAccessibilityButtonLongClicked()

    /**
     * Ends the system screen pinning.
     */
    @Throws(RemoteException::class)
    fun stopScreenPinning()
}