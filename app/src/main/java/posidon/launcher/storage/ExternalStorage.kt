package posidon.launcher.storage

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.*


object ExternalStorage {

    inline fun write(context: Context, name: String, fn: (OutputStream, String) -> Unit) {
        val dir: File? = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val file = File(dir, name)
        FileOutputStream(file).use {
            fn(it, file.absolutePath)
        }
    }

    inline fun writeOutsideScope(context: Context, name: String, fn: (OutputStream, String) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, name)
                put(MediaStore.Downloads.RELATIVE_PATH, "Download")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val uri = context.contentResolver.insert(collection, values)!!

            context.contentResolver.openOutputStream(uri).use {
                fn(it!!, "Download/$name")
            }

            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)
        } else write(context, name, fn)
    }

    fun writeDataOutsideScope(data: Serializable, context: Context, name: String, feedbackPopup: Boolean) {
        writeOutsideScope(context, name) { o, path ->
            val out = ObjectOutputStream(o)
            try {
                out.writeObject(data)
                if (feedbackPopup) Toast.makeText(context, "Saved: $path", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                if (feedbackPopup) Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun readAny(context: Context, uri: Uri): Any? {
        var data: Any? = null
        try { data = ObjectInputStream(context.contentResolver.openInputStream(uri)).readObject() }
        catch (e: Exception) { e.printStackTrace() }
        return data
    }

    private const val FILE_REQUEST_CODE = 0xf113

    fun pickFile(activity: Activity, mime: String) {
        val intent = Intent()
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = mime
        activity.startActivityForResult(intent, FILE_REQUEST_CODE)
    }

    fun onActivityResultPickFile(activity: Activity, requestCode: Int, data: Intent?, fn: (InputStream?) -> Unit) {
        if (requestCode == FILE_REQUEST_CODE) {
            fn(try { activity.contentResolver.openInputStream(data!!.data!!)!! } catch (e: Exception) { null })
        }
    }
}