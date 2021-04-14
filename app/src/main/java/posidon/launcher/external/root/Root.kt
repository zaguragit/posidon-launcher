package posidon.launcher.external.root

import posidon.launcher.items.App
import java.nio.charset.Charset

object Root {

    /**
     * Attempt to hibernate app
     * Basically same as [hibernatePackage]
     */
    inline fun hibernate(app: App): Boolean = hibernatePackage(app.packageName)

    /**
     * Attempt to hibernate package
     * @return true unless there was an error
     */
    inline fun hibernatePackage(packageName: String): Boolean =
        try { exec("am force-stop $packageName") }
        catch (e: Exception) { false }

    /**
     * Self-explanatory
     */
    inline fun isRootAvailable(): Boolean =
        try { exec(null) }
        catch (e: Exception) { false }

    /**
     * Tries to execute a command in root mode
     * @return true if the execution was successful
     */
    fun exec(command: String?): Boolean {
        var p: Process? = null
        try {
            p = Runtime.getRuntime().exec("su")
            val o = p.outputStream
            //put command
            if (command != null && command.trim { it <= ' ' } != "") {
                o.write("$command\n".toByteArray(UTF_8))
            }
            //exit from su command
            o.run {
                write("exit\n".toByteArray(UTF_8))
                flush()
                close()
            }
            val result = p.waitFor()
            if (result != 0) {
                throw Exception("Command execution failed $result")
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            p?.destroy()
        }
        return false
    }

    private val UTF_8 = Charset.forName("UTF-8")
}