package posidon.launcher.tools

import android.content.Context
import android.net.Uri
import android.os.Environment
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
        }
        catch (e: Exception) {
            e.printStackTrace()
            if (feedbackPopup) Toast.makeText(context, "error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }


    fun readAny(context: Context, uri: Uri): Any? {
        var data: Any? = null
        try { data = ObjectInputStream(context.contentResolver.openInputStream(uri)).readObject() }
        catch (e: Exception) { e.printStackTrace() }
        return data
    }
}