package posidon.launcher.storage

import android.content.Context
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

object PrivateStorage {

    fun writeData(data: Serializable, context: Context, path: String) {
        try {
            val fos = context.openFileOutput(path, Context.MODE_PRIVATE)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(data)
            oos.close()
        } catch (e: IOException) { e.printStackTrace() }
    }

    fun readAny(context: Context, path: String): Any? {
        var data: Any? = null
        try {
            val fis = context.openFileInput(path)
            val ois = ObjectInputStream(fis)
            data = ois.readObject()
        }
        catch (e: IOException) {}
        catch (e: ClassNotFoundException) { e.printStackTrace() }
        return data
    }

    fun readText(context: Context, path: String): String? {
        var data: String? = null
        try {
            val fis = context.openFileInput(path)
            val ois = ObjectInputStream(fis)
            data = ois.readObject() as String?
        }
        catch (e: IOException) {}
        catch (e: ClassNotFoundException) { e.printStackTrace() }
        return data
    }

    fun writeData(data: String, context: Context, path: String) {
        try {
            val fos = context.openFileOutput(path, Context.MODE_PRIVATE)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(data)
            oos.close()
        } catch (e: IOException) { e.printStackTrace() }
    }
}