package posidon.launcher.wall;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

class WallLoader extends AsyncTask<Void, Void, List<Wall>> {

    private final StringBuilder text = new StringBuilder();
    private AsyncTaskListener listener;

    @Override
    protected List<Wall> doInBackground(Void... params) {
        String repo = "https://raw.githubusercontent.com/leoxshn/walls/master/";
        try {
            String line;
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(new URL(repo + "index.json").openStream()));
            while ((line = bufferReader.readLine()) != null) text.append(line);
            bufferReader.close();

            JSONArray array = new JSONObject(text.toString()).getJSONArray("");
            List<Wall> walls = new ArrayList<>();
            int realI = 0;
            for (int i = 0; i < array.length(); i++) {
                try {
                    walls.add(realI, new Wall());
                    walls.get(realI).name = array.getJSONObject(i).getString("n");
                    walls.get(realI).author = array.getJSONObject(i).getString("a");
                    String d = repo + array.getJSONObject(i).getString("d");
                    walls.get(realI).img = BitmapFactory.decodeStream(new URL(d + "/thumb.jpg").openConnection().getInputStream());
                    walls.get(realI).url = d + "/img.png";
                    realI++;
                } catch (Exception ignore) {}
            }
            return walls;
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    void setListener(AsyncTaskListener listener) { this.listener = listener; }
    public interface AsyncTaskListener { void onAsyncTaskFinished(List<Wall> walls);}

    @Override
    protected void onPostExecute(List<Wall> walls) { if (listener != null) listener.onAsyncTaskFinished(walls); }
}