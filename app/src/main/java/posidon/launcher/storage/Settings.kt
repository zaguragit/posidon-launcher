package posidon.launcher.storage

import android.content.Context
import android.net.Uri
import posidon.launcher.Main
import posidon.launcher.tools.Tools
import java.io.Serializable
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.thread

object Settings {

    private lateinit var ints: HashMap<String, Int>
    private lateinit var floats: HashMap<String, Float>
    private lateinit var bools: HashMap<String, Boolean>
    private lateinit var strings: HashMap<String, String>
    private lateinit var lists: HashMap<String, ArrayList<String>>
    private lateinit var context: WeakReference<Context>

    var isInitialized: Boolean = false
        private set

    inline operator fun set(key: String, value: Int) {
        putNotSave(key, value); apply()
    }

    inline operator fun set(key: String, value: Float) {
        putNotSave(key, value); apply()
    }

    inline operator fun set(key: String, value: Boolean) {
        putNotSave(key, value); apply()
    }

    inline operator fun set(key: String, value: String?) {
        putNotSave(key, value); apply()
    }

    inline operator fun set(key: String, value: ArrayList<String>) {
        putNotSave(key, value); apply()
    }


    val lock = ReentrantLock()

    fun putNotSave(key: String, value: Int) {
        lock.lock()
        ints[key] = value
        lock.unlock()
    }

    fun putNotSave(key: String, value: Float) {
        lock.lock()
        floats[key] = value
        lock.unlock()
    }

    fun putNotSave(key: String, value: Boolean) {
        lock.lock()
        bools[key] = value
        lock.unlock()
    }

    fun putNotSave(key: String, value: String?) {
        lock.lock()
        if (value == null) strings.keys.remove(key)
        else strings[key] = value
        lock.unlock()
    }

    fun putNotSave(key: String, value: ArrayList<String>) {
        lock.lock()
        lists[key] = value
        lock.unlock()
    }

    fun apply() { thread {
        lock.lock()
        PrivateStorage.writeData(SettingsFile(ints, floats, bools, strings, lists), context.get() ?: Tools.publicContext!!, "settings")
        lock.unlock()
    }}

    operator fun get(key: String, default: Int) = ints[key] ?: default
    operator fun get(key: String, default: Float) = floats[key] ?: default
    operator fun get(key: String, default: Boolean) = bools[key] ?: default
    operator fun get(key: String, default: String) = getString(key) ?: default
    fun getString(key: String) = strings[key]
    fun getStrings(key: String) = lists[key] ?: ArrayList<String>().also { lists[key] = it }


    fun init(context: Context) {
        Settings.context = WeakReference(context)
        PrivateStorage.readAny(context, "settings").let {
            if (it != null) {
                if (it is SettingsFile) {
                    ints = it.ints
                    floats = it.floats
                    bools = it.bools
                    strings = it.strings
                    lists = it.lists
                }
            } else {
                ints = HashMap()
                floats = HashMap()
                bools = HashMap()
                strings = HashMap()
                lists = HashMap()
            }
        }
    }

    fun saveBackup() = ExternalStorage.writeDataOutsideScope(
            SettingsFile(ints, floats, bools, strings, lists), context.get()!!,
            "${SimpleDateFormat("MMMd-HHmmss", Locale.getDefault()).format(Date())}.plb", true)

    fun restoreFromBackup(uri: Uri) = ExternalStorage.readAny(context.get()!!, uri)?.let {
        if (it is SettingsFile) {
            ints = it.ints
            floats = it.floats
            bools = it.bools
            strings = it.strings
            lists = it.lists
            Main.customized = true
            Main.shouldSetApps = true
        }
    }

    private class SettingsFile(
        val ints: HashMap<String, Int>,
        val floats: HashMap<String, Float>,
        val bools: HashMap<String, Boolean>,
        val strings: HashMap<String, String>,
        val lists: HashMap<String, ArrayList<String>>
    ) : Serializable { companion object { private const val serialVersionUID = 0 } }
}