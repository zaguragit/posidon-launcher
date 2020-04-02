/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package posidon.launcher.external.quickstep

import android.graphics.Region
import android.os.*

interface ISystemGestureExclusionListener : IInterface {
    /** Local-side IPC implementation stub class.  */
    abstract class Stub : Binder(), ISystemGestureExclusionListener {
        override fun asBinder(): IBinder = this

        @Throws(RemoteException::class)
        public override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            val descriptor = interfaceDescriptor!!
            return when (code) {
                IBinder.INTERFACE_TRANSACTION -> {
                    reply!!.writeString(descriptor)
                    true
                }
                TRANSACTION_onSystemGestureExclusionChanged -> {
                    data.enforceInterface(descriptor)
                    val _arg0: Int
                    _arg0 = data.readInt()
                    val _arg1: Region?
                    _arg1 = if (0 != data.readInt()) Region.CREATOR.createFromParcel(data) else null
                    val _arg2: Region?
                    _arg2 = if (0 != data.readInt()) Region.CREATOR.createFromParcel(data) else null
                    onSystemGestureExclusionChanged(_arg0, _arg1, _arg2)
                    true
                }
                else -> {
                    super.onTransact(code, data, reply, flags)
                }
            }
        }

        private class Proxy internal constructor(private val mRemote: IBinder) : ISystemGestureExclusionListener {
            override fun asBinder(): IBinder = mRemote

            /**
             * Called when the system gesture exclusion for the given display changed.
             * @param displayId the display whose system gesture exclusion changed
             * @param systemGestureExclusion a `Region` where the app would like priority over the
             * system gestures, in display coordinates. Certain restrictions
             * might be applied such that apps don't get all the exclusions
             * they request.
             * @param systemGestureExclusionUnrestricted a `Region` where the app would like priority
             * over the system gestures, in display coordinates, without
             * any restrictions applied. Null if no restrictions have been
             * applied.
             */
            @Throws(RemoteException::class)
            override fun onSystemGestureExclusionChanged(displayId: Int, systemGestureExclusion: Region?, systemGestureExclusionUnrestricted: Region?) {
                val _data = Parcel.obtain()
                try {
                    _data.writeInterfaceToken(interfaceDescriptor)
                    _data.writeInt(displayId)
                    if (systemGestureExclusion != null) {
                        _data.writeInt(1)
                        systemGestureExclusion.writeToParcel(_data, 0)
                    } else _data.writeInt(0)
                    if (systemGestureExclusionUnrestricted != null) {
                        _data.writeInt(1)
                        systemGestureExclusionUnrestricted.writeToParcel(_data, 0)
                    } else _data.writeInt(0)
                    mRemote.transact(TRANSACTION_onSystemGestureExclusionChanged, _data, null, IBinder.FLAG_ONEWAY)
                } finally { _data.recycle() }
            }

        }

        companion object {
            const val interfaceDescriptor = "ISystemGestureExclusionListener"

            /**
             * Cast an IBinder object into an ISystemGestureExclusionListener interface,
             * generating a proxy if needed.
             */
            fun asInterface(obj: IBinder?): ISystemGestureExclusionListener? {
                if (obj == null) return null
                val iin = obj.queryLocalInterface(interfaceDescriptor)
                return if (iin != null && iin is ISystemGestureExclusionListener) iin else Proxy(obj)
            }

            const val TRANSACTION_onSystemGestureExclusionChanged = IBinder.FIRST_CALL_TRANSACTION + 0
        }

        /** Construct the stub at attach it to the interface.  */
        init { attachInterface(this, interfaceDescriptor) }
    }

    /**
     * Called when the system gesture exclusion for the given display changed.
     * @param displayId the display whose system gesture exclusion changed
     * @param systemGestureExclusion a `Region` where the app would like priority over the
     * system gestures, in display coordinates. Certain restrictions
     * might be applied such that apps don't get all the exclusions
     * they request.
     * @param systemGestureExclusionUnrestricted a `Region` where the app would like priority
     * over the system gestures, in display coordinates, without
     * any restrictions applied. Null if no restrictions have been
     * applied.
     */
    @Throws(RemoteException::class)
    fun onSystemGestureExclusionChanged(displayId: Int, systemGestureExclusion: Region?, systemGestureExclusionUnrestricted: Region?)
}