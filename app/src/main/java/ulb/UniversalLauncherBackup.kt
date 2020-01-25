
/*
  This is meant to be implemented in more
  launchers, so that migrating from one
  launcher to another is easier for the users.
*/

package ulb

object UniversalLauncherBackup {

    ////SOME UNIVERSAL SETTINGS (launchers don't have to support them, but if they have those settings, they should use the universal names instead of custom ones)
    const val APP_DRAWER_COLOR = "ulb:app_drawer_color"
    const val APP_DRAWER_COLUMNS = "ulb:app_drawer_columns"
    const val APP_DRAWER_LABEL_COLOR = "ulb:app_drawer_label_color"
    const val APP_DRAWER_LABELS_ENABLED = "ulb:app_drawer_labels_enabled"
    const val DOCK_COLOR = "ulb:dock_color"
    const val DOCK_COLUMNS = "ulb:dock_columns"
    const val DOCK_ROWS = "ulb:dock_rows"
    const val DOCK_CORNER_RADIUS_TOP_RIGHT = "ulb:dock_corner_radius_top_right"
    const val DOCK_CORNER_RADIUS_TOP_LEFT = "ulb:dock_corner_radius_top_left"
    const val DOCK_CORNER_RADIUS_BOTTOM_RIGHT = "ulb:dock_corner_radius_bottom_right"
    const val DOCK_CORNER_RADIUS_BOTTOM_LEFT = "ulb:dock_corner_radius_bottom_left"
    const val DOCK_CORNER_RADIUS_ALL = "ulb:dock_corner_radius_all"
    const val DOCK_LABEL_COLOR = "ulb:dock_label_color"
    const val DOCK_LABELS_ENABLED = "ulb:dock_labels_enabled"
    const val ICON_PACKS = "ulb:icon_packs"
    // naming style -> <"universal"/appName/appName.category>:<parameter>


    public class Builder {
        private val backup: Backup = Backup()

        public fun put(key: String, value: Int): Builder {
            checkKey(key)
            backup.ints[key] = value.toString()
            return this
        }

        public fun put(key: String, value: Float): Builder {
            checkKey(key)
            backup.floats[key] = value.toString()
            return this
        }

        public fun put(key: String, value: Boolean): Builder {
            checkKey(key)
            backup.booleans[key] = if (value) "1" else "0"
            return this
        }

        public fun put(key: String, value: String): Builder {
            checkKey(key)
            backup.strings[key] = value
            return this
        }

        public fun put(key: String, value: Array<Int>): Builder {
            checkKey(key)
            val stringBuilder = StringBuilder("int")
            for (i in value) stringBuilder.append(' ').append(i)
            backup.lists[key] = stringBuilder.toString()
            return this
        }

        public fun put(key: String, value: Array<Float>): Builder {
            checkKey(key)
            val stringBuilder = StringBuilder("float")
            for (i in value) stringBuilder.append(' ').append(i)
            backup.lists[key] = stringBuilder.toString()
            return this
        }

        public fun put(key: String, value: Array<Boolean>): Builder {
            checkKey(key)
            val stringBuilder = StringBuilder("bool")
            for (i in value) stringBuilder.append(' ').append(if (i) '1' else '0')
            backup.lists[key] = stringBuilder.toString()
            return this
        }

        public fun getBackup(): Backup { return backup }
    }


    public class Backup {

        val ints: HashMap<String, String> = HashMap()
        val floats: HashMap<String, String> = HashMap()
        val booleans: HashMap<String, String> = HashMap()
        val strings: HashMap<String, String> = HashMap()
        val lists: HashMap<String, String> = HashMap()

        public fun generateText(): String {
            val stringBuilder = StringBuilder()
            for (string in ints) stringBuilder
                    .append("int ")
                    .append(string.key)
                    .append(' ')
                    .append(string.value)
                    .append('\n')
            for (string in floats) stringBuilder
                    .append("float ")
                    .append(string.key)
                    .append(' ')
                    .append(string.value)
                    .append('\n')
            for (string in booleans) stringBuilder
                    .append("bool ")
                    .append(string.key)
                    .append(' ')
                    .append(string.value)
                    .append('\n')
            for (string in strings) {
                if (string.value.contains('\n')) throw RuntimeException("Saved strings must not contain new-line characters (they're not supported as of now)")
                stringBuilder
                        .append("text ")
                        .append(string.key)
                        .append(' ')
                        .append(string.value)
                        .append('\n')
            }
            for (string in strings) stringBuilder
                    .append("list ")
                    .append(string.key)
                    .append(' ')
                    .append(string.value)
                    .append('\n')
            return stringBuilder.toString()
        }

        public fun parseText(text: String) {
            val lines = text.split('\n')
            for (line in lines) {
                val tokens = line.split(' ')
                when(tokens[0]) {
                    "int" -> ints[tokens[1]] = tokens[2]
                    "float" -> floats[tokens[1]] = tokens[2]
                    "bool" -> booleans[tokens[1]] = tokens[2]
                    "text" -> strings[tokens[1]] = line.substring(tokens[0].length + tokens[1].length + 2)
                    "list" -> lists[tokens[1]] = line.substring(tokens[0].length + tokens[1].length + 2)
                }
            }
        }

        public fun getInt(key: String, default: Int): Int {
            checkKey(key)
            return if (ints[key] != null) ints[key]!!.toInt() else default
        }

        public fun getFloat(key: String, default: Float): Float {
            checkKey(key)
            return if (floats[key] != null) floats[key]!!.toFloat() else default
        }

        public fun getBool(key: String, default: Boolean): Boolean {
            checkKey(key)
            return if (booleans[key] != null) booleans[key] != "0" else default
        }

        public fun getString(key: String, default: String): String {
            return if (getString(key) != null) getString(key)!! else default
        }

        public fun getString(key: String): String? {
            checkKey(key)
            return strings[key]
        }

        public fun getStrings(key: String, default: Array<Int>): Array<Int> {
            checkKey(key)
            if (lists[key] == null) return default
            val stringList = lists[key]!!.split(' ')
            if (stringList[0] != "int") return default
            return Array(stringList.size - 1) { stringList[it].toInt() }
        }

        public fun getFloats(key: String, default: Array<Float>): Array<Float> {
            checkKey(key)
            if (lists[key] == null) return default
            val stringList = lists[key]!!.split(' ')
            if (stringList[0] != "float") return default
            return Array(stringList.size - 1) { stringList[it].toFloat() }
        }

        public fun getBools(key: String, default: Array<Boolean>): Array<Boolean> {
            checkKey(key)
            if (lists[key] == null) return default
            val stringList = lists[key]!!.split(' ')
            if (stringList[0] != "bool") return default
            return Array(stringList.size - 1) { stringList[it] != "0" }
        }
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