package posidon.launcher.storage

import android.content.Context
import android.net.Uri
import posidon.launcher.Global
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

    val stringKeys get() = strings.keys
    private lateinit var ints: HashMap<String, Int>
    private lateinit var floats: HashMap<String, Float>
    private lateinit var bools: HashMap<String, Boolean>
    private lateinit var strings: HashMap<String, String>
    private lateinit var lists: HashMap<String, ArrayList<String>>
    private lateinit var context: WeakReference<Context>

    private var isInitialized: Boolean = false

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


    private val lock = ReentrantLock()

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

    fun apply() { thread(block = ::applyNow) }
    fun applyNow() {
        lock.lock()
        PrivateStorage.writeData(SettingsFile(ints, floats, bools, strings, lists), context.get() ?: Tools.appContext!!, "settings")
        lock.unlock()
    }

    inline operator fun get(key: String, default: Int): Int = getInt(key) ?: default
    inline operator fun get(key: String, default: Float): Float = getFloat(key) ?: default
    inline operator fun get(key: String, default: Boolean): Boolean = getBoolean(key) ?: default
    inline operator fun get(key: String, default: String): String = getString(key) ?: default


    fun getInt(key: String): Int? {
        lock.lock()
        return ints[key].also { lock.unlock() }
    }

    fun getFloat(key: String): Float? {
        lock.lock()
        return floats[key].also { lock.unlock() }
    }

    fun getBoolean(key: String): Boolean? {
        lock.lock()
        return bools[key].also { lock.unlock() }
    }

    fun getString(key: String): String? {
        lock.lock()
        return (strings[key]).also { lock.unlock() }
    }

    fun getStrings(key: String): ArrayList<String>? {
        lock.lock()
        return lists[key].also { lock.unlock() }
    }

    inline fun getStringsOrSetEmpty(key: String): ArrayList<String> = getStringsOrSet(key) { ArrayList() }

    fun getStringsOrSet(key: String, def: () -> ArrayList<String>): ArrayList<String> {
        lock.lock()
        return (lists[key] ?: def().also { lists[key] = it }).also { lock.unlock() }
    }


    fun init(context: Context) {
        lock.lock()
        if (!isInitialized) {
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
            isInitialized = true
        }
        lock.unlock()
    }

    fun saveBackup() = ExternalStorage.writeDataOutsideScope(
            SettingsFile(ints, floats, bools, strings, lists), context.get()!!,
            "${SimpleDateFormat("'home_'MMMd-HHmmss", Locale.getDefault()).format(Date())}.plb", true)

    fun restoreFromBackup(uri: Uri) = ExternalStorage.readAny(context.get()!!, uri)?.let {
        if (it is SettingsFile) {
            ints = it.ints
            floats = it.floats
            bools = it.bools
            strings = it.strings
            lists = it.lists
            Global.customized = true
            Global.shouldSetApps = true
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