package posidon.launcher.wall

import android.graphics.BitmapFactory
import android.os.AsyncTask
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

internal class WallLoader : AsyncTask<Unit, Unit, List<Wall>>() {

    private val text = StringBuilder()
    var listener: ((List<Wall>) -> Unit)? = null

    override fun doInBackground(vararg params: Unit): List<Wall> {
        val repo = "https://raw.githubusercontent.com/leoxshn/walls/master/"
        try {
            var line: String?
            val bufferReader = BufferedReader(InputStreamReader(URL(repo + "index.json").openStream()))
            while (bufferReader.readLine().also { line = it } != null) text.append(line)
            bufferReader.close()
            val array = JSONObject(text.toString()).getJSONArray("")
            val walls: ArrayList<Wall> = ArrayList()
            var realI = 0
            for (i in 0 until array.length()) {
                try {
                    walls.add(realI, Wall())
                    walls[realI].name = array.getJSONObject(i).getString("n")
                    walls[realI].author = array.getJSONObject(i).getString("a")
                    val d = repo + array.getJSONObject(i).getString("d")
                    walls[realI].img = BitmapFactory.decodeStream(URL("$d/thumb.jpg").openConnection().getInputStream())
                    walls[realI].url = "$d/img.png"
                    realI++
                } catch (ignore: Exception) {
                }
            }
            return walls
        } catch (e: Exception) { e.printStackTrace() }
        return emptyList()
    }

    override fun onPostExecute(walls: List<Wall>) { listener?.invoke(walls) }
}