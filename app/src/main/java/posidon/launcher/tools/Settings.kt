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

    private lateinit var ints: HashMap<String, String>
    private lateinit var  floats: HashMap<String, String>
    private lateinit var bools: HashMap<String, String>
    private lateinit var strings: HashMap<String, String>
    private lateinit var lists: HashMap<String, String>
    private lateinit var context: Context

    @JvmStatic fun put(key: String, value: Int) {
        putNotSave(key, value)
        apply()
    }

    @JvmStatic fun put(key: String, value: Float) {
        putNotSave(key, value)
        apply()
    }

    @JvmStatic fun put(key: String, value: Boolean) {
        putNotSave(key, value)
        apply()
    }

    @JvmStatic fun put(key: String, value: String?) {
        putNotSave(key, value)
        apply()
    }

    @JvmStatic fun put(key: String, value: Array<Int>) {
        putNotSave(key, value)
        apply()
    }

    @JvmStatic fun put(key: String, value: Array<Float>) {
        putNotSave(key, value)
        apply()
    }

    @JvmStatic fun put(key: String, value: Array<Boolean>) {
        putNotSave(key, value)
        apply()
    }

    @JvmStatic fun putNotSave(key: String, value: Int) {
        //checkKey(key)
        ints[key] = value.toString()
    }

    @JvmStatic fun putNotSave(key: String, value: Float) {
        floats[key] = value.toString()
    }

    @JvmStatic fun putNotSave(key: String, value: Boolean) {
        //checkKey(key)
        bools[key] = if (value) "1" else "0"
    }

    @JvmStatic fun putNotSave(key: String, value: String?) {
        //checkKey(key)
        if (value == null) strings.keys.remove(key)
        else strings[key] = value
    }

    @JvmStatic fun putNotSave(key: String, value: Array<Int>) {
        //checkKey(key)
        val stringBuilder = StringBuilder(Type.INT.string)
        for (i in value) stringBuilder.append(' ').append(i)
        lists[key] = stringBuilder.toString()
    }

    @JvmStatic fun putNotSave(key: String, value: Array<Float>) {
        //checkKey(key)
        val stringBuilder = StringBuilder(Type.FLOAT.string)
        for (i in value) stringBuilder.append(' ').append(i)
        lists[key] = stringBuilder.toString()
    }

    @JvmStatic fun putNotSave(key: String, value: Array<Boolean>) {
        //checkKey(key)
        val stringBuilder = StringBuilder(Type.BOOL.string)
        for (i in value) stringBuilder.append(' ').append(if (i) '1' else '0')
        lists[key] = stringBuilder.toString()
    }

    @JvmStatic fun apply() = PrivateStorage.writeData(SettingsContent(ints, floats, bools, strings, lists), context, "settings")


    /*private fun generateText(): String {
        val stringBuilder = StringBuilder()
        for (string in ints) stringBuilder
                .append(Type.INT.string)
                .append(' ')
                .append(string.key)
                .append(' ')
                .append(string.value)
                .append('\n')
        for (string in floats) stringBuilder
                .append(Type.FLOAT.string)
                .append(' ')
                .append(string.key)
                .append(' ')
                .append(string.value)
                .append('\n')
        for (string in booleans) stringBuilder
                .append(Type.BOOL.string)
                .append(' ')
                .append(string.key)
                .append(' ')
                .append(string.value)
                .append('\n')
        for (string in strings) stringBuilder
                .append(Type.TEXT.string)
                .append(' ')
                .append(string.key)
                .append(' ')
                .append(string.value.replace("\\", "\\\\").replace("\n", "\\n"))
                .append('\n')
        for (string in lists) stringBuilder
                .append(Type.LIST.string)
                .append(' ')
                .append(string.key)
                .append(' ')
                .append(string.value)
                .append('\n')
        return stringBuilder.toString()
    }

    private fun parseText(text: String) = parseText(text.split('\n'))
    private fun parseText(lines: Iterable<String>) {
        for (line in lines) {
            val tokens = line.split(' ')
            when(tokens[0]) {
                Type.INT.string -> ints[tokens[1]] = tokens[2]
                Type.FLOAT.string -> floats[tokens[1]] = tokens[2]
                Type.BOOL.string -> booleans[tokens[1]] = tokens[2]
                Type.TEXT.string -> {
                    strings[tokens[1]] = line.substring(tokens[0].length + tokens[1].length + 2).replace(Regex.fromLiteral("(?<!\\)(\\n)"), "\n").replace("\\\\", "\\")
                    println("aaaaaaaaaaaaaaaaaaaaaa!!! " + strings[tokens[1]])
                }
                Type.LIST.string -> lists[tokens[1]] = line.substring(tokens[0].length + tokens[1].length + 2)
            }
        }
    }*/

    @JvmStatic fun getInt(key: String, default: Int) = ints[key]?.toInt() ?: default
    @JvmStatic fun getFloat(key: String, default: Float) = floats[key]?.toFloat() ?: default
    @JvmStatic fun getBool(key: String, default: Boolean) =
            if (bools[key] != null) bools[key] != "0" else default
    @JvmStatic fun getString(key: String, default: String) = getString(key) ?: default
    @JvmStatic fun getString(key: String) = strings[key]

    @JvmStatic
    fun getInts(key: String, default: Array<Int>): Array<Int> {
        if (lists[key] == null) return default
        val stringList = lists[key]!!.split(' ')
        if (stringList[0] != Type.INT.string) return default
        return Array(stringList.size - 1) { stringList[it].toInt() }
    }

    @JvmStatic
    fun getFloats(key: String, default: Array<Float>): Array<Float> {
        if (lists[key] == null) return default
        val stringList = lists[key]!!.split(' ')
        if (stringList[0] != Type.FLOAT.string) return default
        return Array(stringList.size - 1) { stringList[it].toFloat() }
    }

    @JvmStatic
    fun getBools(key: String, default: Array<Boolean>): Array<Boolean> {
        if (lists[key] == null) return default
        val stringList = lists[key]!!.split(' ')
        if (stringList[0] != Type.BOOL.string) return default
        return Array(stringList.size - 1) { stringList[it] != "0" }
    }

    private fun checkKey(key: String) {
        when {
            key.contains(' ') -> throw IllegalArgumentException(
                    "The ULB key can't have spaces! You should use '_' instead")
            key.contains('\n') -> throw IllegalArgumentException(
                    "The ULB key can't have \\n characters! " +
                            "It will cause syntax errors when reading the data")
        }
    }

    @JvmStatic
    fun init(context: Context) {
        Settings.context = context
        PrivateStorage.readAny(context, "settings").let {
            if (it != null && it is SettingsContent) {
                ints = it.ints
                floats = it.floats
                bools = it.bools
                strings = it.strings
                lists = it.lists
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
            SettingsContent(ints, floats, bools, strings, lists), context,
            "posidonBackup[${SimpleDateFormat("MMddHHmmss", Locale.getDefault()).format(Date())}]",
            true
    )

    fun restoreFromBackup(uri: Uri) = ExternalStorage.readAny(context, uri).let {
        if (it != null && it is SettingsContent) {
            ints = it.ints
            floats = it.floats
            bools = it.bools
            strings = it.strings
            lists = it.lists
            Main.customized = true
            Main.shouldSetApps = true
        }
    }

    private class SettingsContent(
        val ints: HashMap<String, String>,
        val floats: HashMap<String, String>,
        val bools: HashMap<String, String>,
        val strings: HashMap<String, String>,
        val lists: HashMap<String, String>
    ) : Serializable { companion object { private const val serialVersionUID = 0 } }
}


/*
int universal:dock_color 0235634
float universal:dock_corner_radius_all 43.3f
bool posidon:enable_blur 1
text posidon:search_bar_hint Search...
text posidon.anim:app_open_animation none
text randomlauncher:some_random_long_text blabla bla bla blabla bla blablabla
list bla:bla_bla_bla int 1 3 4 75 456 74784568 568456 345 3455 5 3 74 574 4
list bla:bla_bla float 35.5f 44.7f 3.6f
list bla:bla bool 0 1 0 0 0 1 1 0 0 0 0 0 1 1 0 1 0 1 0 0 1 1 1 0 0
*/

/*
syntax: <data type> <key> <value>
(only the value can have spaces, otherwise the data is impossible to interpret)


data types = int, float, bool, text, list
list is a space type because its value can have spaces
text is too and it's kinda like an list of words, but without declaring the list type at the beginning of the value

booleans have 1/0 instead of true/false (0 = false, everything else = true)

this syntax should use less data than SharedPreferences which to my knowledge is just xml

bool and text instead of boolean and string because it's shorter and uses less storage, and it's also more readable
in a more efficient version, [int, float, bool, text] could be replaced by shorter names like: [I, F, B, S] (S for string or T for text)
*/