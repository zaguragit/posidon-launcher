/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package posidon.launcher.external.quickstep

import android.graphics.Region
import android.os.*

interface IOverviewProxy : IInterface {
    /** Local-side IPC implementation stub class.  */
    abstract class Stub : Binder(), IOverviewProxy {
        override fun asBinder(): IBinder = this

        @Throws(RemoteException::class)
        public override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            val descriptor = interfaceDescriptor
            return when (code) {
                IBinder.INTERFACE_TRANSACTION -> {
                    reply!!.writeString(descriptor)
                    true
                }
                TRANSACTION_onActiveNavBarRegionChanges -> {
                    //data.enforceInterface(descriptor);
                    val _arg0: Region? = if (0 != data.readInt()) Region.CREATOR.createFromParcel(data) else null
                    onActiveNavBarRegionChanges(_arg0)
                    reply!!.writeNoException()
                    true
                }
                TRANSACTION_onInitialize -> {
                    //data.enforceInterface(descriptor);
                    val _arg0: Bundle? = if (0 != data.readInt()) Bundle.CREATOR.createFromParcel(data) else null
                    onInitialize(_arg0)
                    reply!!.writeNoException()
                    true
                }
                TRANSACTION_onOverviewToggle -> {
                    //data.enforceInterface(descriptor);
                    onOverviewToggle()
                    reply!!.writeNoException()
                    true
                }
                TRANSACTION_onOverviewShown -> {
                    //data.enforceInterface(descriptor);
                    val _arg0: Boolean = 0 != data.readInt()
                    onOverviewShown(_arg0)
                    reply!!.writeNoException()
                    true
                }
                TRANSACTION_onOverviewHidden -> {
                    //data.enforceInterface(descriptor);
                    val _arg0: Boolean = 0 != data.readInt()
                    val _arg1: Boolean = 0 != data.readInt()
                    onOverviewHidden(_arg0, _arg1)
                    reply!!.writeNoException()
                    true
                }
                TRANSACTION_onTip -> {
                    //data.enforceInterface(descriptor);
                    val _arg0: Int = data.readInt()
                    val _arg1: Int = data.readInt()
                    onTip(_arg0, _arg1)
                    reply!!.writeNoException()
                    true
                }
                TRANSACTION_onAssistantAvailable -> {
                    //data.enforceInterface(descriptor);
                    val _arg0: Boolean = 0 != data.readInt()
                    onAssistantAvailable(_arg0)
                    reply!!.writeNoException()
                    true
                }
                TRANSACTION_onAssistantVisibilityChanged -> {
                    //data.enforceInterface(descriptor);
                    val _arg0: Float = data.readFloat()
                    onAssistantVisibilityChanged(_arg0)
                    reply!!.writeNoException()
                    true
                }
                TRANSACTION_onBackAction -> {
                    //data.enforceInterface(descriptor);
                    val _arg0: Boolean = 0 != data.readInt()
                    val _arg1: Int = data.readInt()
                    val _arg2: Int = data.readInt()
                    val _arg3: Boolean = 0 != data.readInt()
                    val _arg4: Boolean = 0 != data.readInt()
                    onBackAction(_arg0, _arg1, _arg2, _arg3, _arg4)
                    reply!!.writeNoException()
                    true
                }
                TRANSACTION_onSystemUiStateChanged -> {
                    //data.enforceInterface(descriptor);
                    val _arg0: Int = data.readInt()
                    onSystemUiStateChanged(_arg0)
                    reply!!.writeNoException()
                    true
                }
                else -> super.onTransact(code, data, reply, flags)
            }
        }

        private class Proxy internal constructor(private val mRemote: IBinder) : IOverviewProxy {

            override fun asBinder(): IBinder = mRemote

            @Throws(RemoteException::class)
            override fun onActiveNavBarRegionChanges(activeRegion: Region?) {
                val _data = Parcel.obtain()
                val _reply = Parcel.obtain()
                try {
                    _data.writeInterfaceToken(interfaceDescriptor)
                    if (activeRegion != null) {
                        _data.writeInt(1)
                        activeRegion.writeToParcel(_data, 0)
                    } else _data.writeInt(0)
                    mRemote.transact(TRANSACTION_onActiveNavBarRegionChanges, _data, _reply, 0)
                    _reply.readException()
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
            }

            @Throws(RemoteException::class)
            override fun onInitialize(params: Bundle?) {
                val _data = Parcel.obtain()
                val _reply = Parcel.obtain()
                try {
                    _data.writeInterfaceToken(interfaceDescriptor)
                    if (params != null) {
                        _data.writeInt(1)
                        params.writeToParcel(_data, 0)
                    } else _data.writeInt(0)
                    mRemote.transact(TRANSACTION_onInitialize, _data, _reply, 0)
                    _reply.readException()
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
            }

            /**
             * Sent when overview button is pressed to toggle show/hide of overview.
             */
            @Throws(RemoteException::class)
            override fun onOverviewToggle() {
                val _data = Parcel.obtain()
                val _reply = Parcel.obtain()
                try {
                    _data.writeInterfaceToken(interfaceDescriptor)
                    mRemote.transact(TRANSACTION_onOverviewToggle, _data, _reply, 0)
                    _reply.readException()
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
            }

            /**
             * Sent when overview is to be shown.
             */
            @Throws(RemoteException::class)
            override fun onOverviewShown(triggeredFromAltTab: Boolean) {
                val _data = Parcel.obtain()
                val _reply = Parcel.obtain()
                try {
                    _data.writeInterfaceToken(interfaceDescriptor)
                    _data.writeInt(if (triggeredFromAltTab) 1 else 0)
                    mRemote.transact(TRANSACTION_onOverviewShown, _data, _reply, 0)
                    _reply.readException()
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
            }

            /**
             * Sent when overview is to be hidden.
             */
            @Throws(RemoteException::class)
            override fun onOverviewHidden(triggeredFromAltTab: Boolean, triggeredFromHomeKey: Boolean) {
                val _data = Parcel.obtain()
                val _reply = Parcel.obtain()
                try {
                    _data.writeInterfaceToken(interfaceDescriptor)
                    _data.writeInt(if (triggeredFromAltTab) 1 else 0)
                    _data.writeInt(if (triggeredFromHomeKey) 1 else 0)
                    mRemote.transact(TRANSACTION_onOverviewHidden, _data, _reply, 0)
                    _reply.readException()
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
            }

            /**
             * Sent when there was an action on one of the onboarding tips view.
             */
            @Throws(RemoteException::class)
            override fun onTip(actionType: Int, viewType: Int) {
                val _data = Parcel.obtain()
                val _reply = Parcel.obtain()
                try {
                    _data.writeInterfaceToken(interfaceDescriptor)
                    _data.writeInt(actionType)
                    _data.writeInt(viewType)
                    mRemote.transact(TRANSACTION_onTip, _data, _reply, 0)
                    _reply.readException()
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
            }

            /**
             * Sent when device assistant changes its default assistant whether it is available or not.
             */
            @Throws(RemoteException::class)
            override fun onAssistantAvailable(available: Boolean) {
                val _data = Parcel.obtain()
                val _reply = Parcel.obtain()
                try {
                    _data.writeInterfaceToken(interfaceDescriptor)
                    _data.writeInt(if (available) 1 else 0)
                    mRemote.transact(TRANSACTION_onAssistantAvailable, _data, _reply, 0)
                    _reply.readException()
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
            }

            /**
             * Sent when the assistant changes how visible it is to the user.
             */
            @Throws(RemoteException::class)
            override fun onAssistantVisibilityChanged(visibility: Float) {
                val _data = Parcel.obtain()
                val _reply = Parcel.obtain()
                try {
                    _data.writeInterfaceToken(interfaceDescriptor)
                    _data.writeFloat(visibility)
                    mRemote.transact(TRANSACTION_onAssistantVisibilityChanged, _data, _reply, 0)
                    _reply.readException()
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
            }

            /**
             * Sent when back is triggered.
             */
            @Throws(RemoteException::class)
            override fun onBackAction(completed: Boolean, downX: Int, downY: Int, isButton: Boolean, gestureSwipeLeft: Boolean) {
                val _data = Parcel.obtain()
                val _reply = Parcel.obtain()
                try {
                    _data.writeInterfaceToken(interfaceDescriptor)
                    _data.writeInt(if (completed) 1 else 0)
                    _data.writeInt(downX)
                    _data.writeInt(downY)
                    _data.writeInt(if (isButton) 1 else 0)
                    _data.writeInt(if (gestureSwipeLeft) 1 else 0)
                    mRemote.transact(TRANSACTION_onBackAction, _data, _reply, 0)
                    _reply.readException()
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
            }

            /**
             * Sent when some system ui state changes.
             */
            @Throws(RemoteException::class)
            override fun onSystemUiStateChanged(stateFlags: Int) {
                val _data = Parcel.obtain()
                val _reply = Parcel.obtain()
                try {
                    _data.writeInterfaceToken(interfaceDescriptor)
                    _data.writeInt(stateFlags)
                    mRemote.transact(TRANSACTION_onSystemUiStateChanged, _data, _reply, 0)
                    _reply.readException()
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
            }

        }

        companion object {
            const val interfaceDescriptor = "IOverviewProxy"

            /**
             * Cast an IBinder object into an IOverviewProxy interface,
             * generating a proxy if needed.
             */
            fun asInterface(obj: IBinder?): IOverviewProxy? {
                if (obj == null) return null
                val iin = obj.queryLocalInterface(interfaceDescriptor)
                return if (iin != null && iin is IOverviewProxy) iin else Proxy(obj)
            }

            const val TRANSACTION_onActiveNavBarRegionChanges = IBinder.FIRST_CALL_TRANSACTION + 11
            const val TRANSACTION_onInitialize = IBinder.FIRST_CALL_TRANSACTION + 12
            const val TRANSACTION_onOverviewToggle = IBinder.FIRST_CALL_TRANSACTION + 6
            const val TRANSACTION_onOverviewShown = IBinder.FIRST_CALL_TRANSACTION + 7
            const val TRANSACTION_onOverviewHidden = IBinder.FIRST_CALL_TRANSACTION + 8
            const val TRANSACTION_onTip = IBinder.FIRST_CALL_TRANSACTION + 10
            const val TRANSACTION_onAssistantAvailable = IBinder.FIRST_CALL_TRANSACTION + 13
            const val TRANSACTION_onAssistantVisibilityChanged = IBinder.FIRST_CALL_TRANSACTION + 14
            const val TRANSACTION_onBackAction = IBinder.FIRST_CALL_TRANSACTION + 15
            const val TRANSACTION_onSystemUiStateChanged = IBinder.FIRST_CALL_TRANSACTION + 16
        }

        /** Construct the stub at attach it to the interface.  */
        init {
            attachInterface(this, interfaceDescriptor)
        }
    }

    @Throws(RemoteException::class)
    fun onActiveNavBarRegionChanges(activeRegion: Region?)

    @Throws(RemoteException::class)
    fun onInitialize(params: Bundle?)

    /**
     * Sent when overview button is pressed to toggle show/hide of overview.
     */
    @Throws(RemoteException::class)
    fun onOverviewToggle()

    /**
     * Sent when overview is to be shown.
     */
    @Throws(RemoteException::class)
    fun onOverviewShown(triggeredFromAltTab: Boolean)

    /**
     * Sent when overview is to be hidden.
     */
    @Throws(RemoteException::class)
    fun onOverviewHidden(triggeredFromAltTab: Boolean, triggeredFromHomeKey: Boolean)

    /**
     * Sent when there was an action on one of the onboarding tips view.
     */
    @Throws(RemoteException::class)
    fun onTip(actionType: Int, viewType: Int)

    /**
     * Sent when device assistant changes its default assistant whether it is available or not.
     */
    @Throws(RemoteException::class)
    fun onAssistantAvailable(available: Boolean)

    /**
     * Sent when the assistant changes how visible it is to the user.
     */
    @Throws(RemoteException::class)
    fun onAssistantVisibilityChanged(visibility: Float)

    /**
     * Sent when back is triggered.
     */
    @Throws(RemoteException::class)
    fun onBackAction(completed: Boolean, downX: Int, downY: Int, isButton: Boolean, gestureSwipeLeft: Boolean)

    /**
     * Sent when some system ui state changes.
     */
    @Throws(RemoteException::class)
    fun onSystemUiStateChanged(stateFlags: Int)
}