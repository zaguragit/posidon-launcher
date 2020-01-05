package posidon.launcher.tools

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.preference.PreferenceManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


object Settings {
    private var settings: SharedPreferences? = null
    @JvmStatic
    fun init(context: Context?) {
        settings = PreferenceManager.getDefaultSharedPreferences(context)
    }

    @JvmStatic
    fun putString(key: String, value: String?) {
        settings!!.edit().putString(key, value).apply()
    }

    @JvmStatic
    fun putInt(key: String, value: Int) {
        settings!!.edit().putInt(key, value).apply()
    }

    fun putFloat(key: String, value: Float) {
        settings!!.edit().putFloat(key, value).apply()
    }

    fun putBool(key: String?, value: Boolean) {
        settings!!.edit().putBoolean(key, value).apply()
    }

    @JvmStatic
    fun getString(key: String, def: String?): String? {
        return settings!!.getString(key, def)
    }

    @JvmStatic
    fun getInt(key: String, def: Int): Int {
        return settings!!.getInt(key, def)
    }

    @JvmStatic
    fun getFloat(key: String, def: Float): Float {
        return settings!!.getFloat(key, def)
    }

    @JvmStatic
    fun getBool(key: String, def: Boolean): Boolean {
        return settings!!.getBoolean(key, def)
    }

    @JvmStatic
    fun saveImage(key: String, bitmap: Bitmap) {
        try {
            FileOutputStream("config/savedImages/$key").use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
        catch (e: IOException) { e.printStackTrace() }
    }

    @JvmStatic
    fun loadImage(key: String): Bitmap? {
        return try {
            val f = File("config/savedImages", "$key.png")
            BitmapFactory.decodeStream(FileInputStream(f))
        }
        catch (e: IOException) { e.printStackTrace(); null }
    }

    @JvmStatic
    fun deleteImage(key: String) {

    }
}