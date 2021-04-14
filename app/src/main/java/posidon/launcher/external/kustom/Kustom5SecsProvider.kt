package posidon.launcher.external.kustom

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import posidon.launcher.external.AutoFinishTransparentActivity

class Kustom5SecsProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun query(u: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?) =
            throw UnsupportedOperationException("Unsupported")

    override fun getType(u: Uri) = null // Not supported
    override fun insert(u: Uri, v: ContentValues?) = throw UnsupportedOperationException("Unsupported")

    override fun delete(u: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        if (PATH_RESET_5SEC_DELAY == u.lastPathSegment) {
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

    override fun update(u: Uri, v: ContentValues?, selection: String?, selectionArgs: Array<String>?) = throw UnsupportedOperationException("Unsupported")

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