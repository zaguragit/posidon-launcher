package posidon.launcher.tools

import android.content.Context
import android.net.Uri
import posidon.launcher.Main
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

object Settings {

    enum class Type(val string: String) {
        TEXT("text"),
        INT("int"),
        FLOAT("float"),
        BOOL("bool"),
        LIST("list"),
    }

    var isInitialized: Boolean = false
        private set
    private lateinit var ints: HashMap<String, Int>
    private lateinit var floats: HashMap<String, Float>
    private lateinit var bools: HashMap<String, Boolean>
    private lateinit var strings: HashMap<String, String>
    private lateinit var lists: HashMap<String, String>
    private lateinit var context: Context

    inline operator fun set(key: String, value: Int) {
        putNotSave(key, value)
        apply()
    }

    inline operator fun set(key: String, value: Float) {
        putNotSave(key, value)
        apply()
    }

    inline operator fun set(key: String, value: Boolean) {
        putNotSave(key, value)
        apply()
    }

    inline operator fun set(key: String, value: String?) {
        putNotSave(key, value)
        apply()
    }

    inline operator fun set(key: String, value: Array<Int>) {
        putNotSave(key, value)
        apply()
    }

    inline operator fun set(key: String, value: Array<Float>) {
        putNotSave(key, value)
        apply()
    }

    inline operator fun set(key: String, value: Array<Boolean>) {
        putNotSave(key, value)
        apply()
    }

    fun putNotSave(key: String, value: Int) { ints[key] = value }
    fun putNotSave(key: String, value: Float) { floats[key] = value }
    fun putNotSave(key: String, value: Boolean) { bools[key] = value }
    fun putNotSave(key: String, value: String?) {
        if (value == null) strings.keys.remove(key)
        else strings[key] = value
    }

    fun putNotSave(key: String, value: Array<Int>) {
        val stringBuilder = StringBuilder(Type.INT.string)
        for (i in value) stringBuilder.append(' ').append(i)
        lists[key] = stringBuilder.toString()
    }

    fun putNotSave(key: String, value: Array<Float>) {
        val stringBuilder = StringBuilder(Type.FLOAT.string)
        for (i in value) stringBuilder.append(' ').append(i)
        lists[key] = stringBuilder.toString()
    }

    fun putNotSave(key: String, value: Array<Boolean>) {
        val stringBuilder = StringBuilder(Type.BOOL.string)
        for (i in value) stringBuilder.append(' ').append(if (i) '1' else '0')
        lists[key] = stringBuilder.toString()
    }

    fun apply() = PrivateStorage.writeData(SettingsFile(ints, floats, bools, strings, lists), context, "settings")

    operator fun get(key: String, default: Int) = ints[key] ?: default
    operator fun get(key: String, default: Float) = floats[key] ?: default
    operator fun get(key: String, default: Boolean) = bools[key] ?: default
    operator fun get(key: String, default: String) = getString(key) ?: default
    fun getString(key: String) = strings[key]

    fun getInts(key: String, default: Array<Int>): Array<Int> {
        if (lists[key] == null) return default
        val stringList = lists[key]!!.split(' ')
        if (stringList[0] != Type.INT.string) return default
        return Array(stringList.size - 1) { stringList[it].toInt() }
    }

    fun getFloats(key: String, default: Array<Float>): Array<Float> {
        if (lists[key] == null) return default
        val stringList = lists[key]!!.split(' ')
        if (stringList[0] != Type.FLOAT.string) return default
        return Array(stringList.size - 1) { stringList[it].toFloat() }
    }

    fun getBools(key: String, default: Array<Boolean>): Array<Boolean> {
        if (lists[key] == null) return default
        val stringList = lists[key]!!.split(' ')
        if (stringList[0] != Type.BOOL.string) return default
        return Array(stringList.size - 1) { stringList[it] != "0" }
    }

    fun init(context: Context) {
        this.context = context
        PrivateStorage.readAny(context, "settings").let {
            if (it != null) {
                if (it is SettingsFile) {
                    ints = it.ints
                    floats = it.floats
                    bools = it.bools
                    strings = it.strings
                    lists = it.lists
                } else if (it is SettingsContent) {
                    ints = HashMap()
                    for (entry in it.ints) ints[entry.key] = entry.value.toInt()
                    floats = HashMap()
                    for (entry in it.floats) floats[entry.key] = entry.value.toFloat()
                    bools = HashMap()
                    for (entry in it.bools) bools[entry.key] = entry.value != "0"
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

    fun saveBackup() = ExternalStorage.writeData(
            SettingsFile(ints, floats, bools, strings, lists), context,
            "posidonBackup[${SimpleDateFormat("MMddHHmmss", Locale.getDefault()).format(Date())}]",
            true
    )

    fun restoreFromBackup(uri: Uri) = ExternalStorage.readAny(context, uri).let {
        if (it != null) {
            if (it is SettingsFile) {
                ints = it.ints
                floats = it.floats
                bools = it.bools
                strings = it.strings
                lists = it.lists
                Main.customized = true
                Main.shouldSetApps = true
            } else if (it is SettingsContent) {
                ints = HashMap()
                for (entry in it.ints) ints[entry.key] = entry.value.toInt()
                floats = HashMap()
                for (entry in it.floats) floats[entry.key] = entry.value.toFloat()
                bools = HashMap()
                for (entry in it.bools) bools[entry.key] = entry.value != "0"
                strings = it.strings
                lists = it.lists
                Main.customized = true
                Main.shouldSetApps = true
            }
        }
    }

    private class SettingsContent(
            val ints: HashMap<String, String>,
            val floats: HashMap<String, String>,
            val bools: HashMap<String, String>,
            val strings: HashMap<String, String>,
            val lists: HashMap<String, String>
    ) : Serializable { companion object { private const val serialVersionUID = 0 } }

    private class SettingsFile(
            val ints: HashMap<String, Int>,
            val floats: HashMap<String, Float>,
            val bools: HashMap<String, Boolean>,
            val strings: HashMap<String, String>,
            val lists: HashMap<String, String>
    ) : Serializable { companion object { private const val serialVersionUID = 0 } }
}