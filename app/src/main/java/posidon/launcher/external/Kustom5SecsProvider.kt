package posidon.launcher.external

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri

class Kustom5SecsProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        throw UnsupportedOperationException("Unsupported")
    }

    override fun getType(uri: Uri): String? {
        // Not supported
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // Not supported
        throw UnsupportedOperationException("Unsupported")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        if (PATH_RESET_5SEC_DELAY == uri.lastPathSegment) {
            /**
             * Lets ensure this gets executed only by Kustom
             */
            checkCallingPackage()
            /**
             * This assumes you have a transparent activity that will just call finish() during its onCreate method
             * Activity in this case also provides a static method for starting itself
             */
            context!!.startActivity(Intent(context, AutoFinishTransparentActivity::class.java))
            return 1
        }
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        // Not supported
        throw UnsupportedOperationException("Unsupported")
    }

    /**
     * Will check weather or not calling pkg is authorized to talk with this provider
     *
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    private fun checkCallingPackage() {
        val callingPkg = callingPackage
        if ("org.kustom.wallpaper" == callingPkg) return
        if ("org.kustom.widget" == callingPkg) return
        if ("org.kustom.lockscreen" == callingPkg) return
        throw SecurityException("Unauthorized")
    }

    companion object {
        /**
         * Path used by Kustom to ask a 5 secs delay reset
         */
        private const val PATH_RESET_5SEC_DELAY = "reset5secs"
    }
}