package posidon.launcher.storage

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable


internal object ExternalStorage {

    fun writeData(data: Serializable, context: Context, name: String, feedbackPopup: Boolean) {
        val dir: File? = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val file = File(dir, name)
        try {
            ObjectOutputStream(file.outputStream()).writeObject(data)
            if (feedbackPopup) Toast.makeText(context, "saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            if (feedbackPopup) Toast.makeText(context, "error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun writeDataOutsideScope(data: Serializable, context: Context, name: String, feedbackPopup: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, name)
                put(MediaStore.Downloads.RELATIVE_PATH, "Download")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val imageUri = context.contentResolver.insert(collection, values)!!

            context.contentResolver.openOutputStream(imageUri).use {
                try {
                    ObjectOutputStream(it).writeObject(data)
                    if (feedbackPopup) Toast.makeText(context, "Saved: Download/$name", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (feedbackPopup) Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }

            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            context.contentResolver.update(imageUri, values, null, null)
        } else writeData(data, context, name, feedbackPopup)
    }

    fun readAny(context: Context, uri: Uri): Any? {
        var data: Any? = null
        try { data = ObjectInputStream(context.contentResolver.openInputStream(uri)).readObject() }
        catch (e: Exception) { e.printStackTrace() }
        return data
    }
}