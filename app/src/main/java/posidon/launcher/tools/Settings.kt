package posidon.launcher.tools

import java.io.Serializable
import kotlin.collections.HashMap

object Settings {
    class SettingsFile(
            val ints: HashMap<String, Int>,
            val floats: HashMap<String, Float>,
            val bools: HashMap<String, Boolean>,
            val strings: HashMap<String, String>,
            val _lists: HashMap<String, String>
    ) : Serializable { companion object { private const val serialVersionUID = 0 } }
}