package posidon.launcher.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class Loader {

    public static class text extends AsyncTask<Void, Void, Void> {
        private final String url;
        private final Listener listener;

        public text(String url, Listener listener) {
            this.url = url;
            this.listener = listener;
        }

        @Override
        protected Void doInBackground (Void...voids){
            try {
                StringBuilder builder = new StringBuilder();
                String buffer;
                BufferedReader bufferReader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
                while ((buffer = bufferReader.readLine()) != null) builder.append(buffer);
                bufferReader.close();
                listener.onFinished(builder.toString());
            } catch (Exception e) { e.printStackTrace(); }
            return null;
        }

        public interface Listener { void onFinished(String string); }
    }

    public static class bitmap extends AsyncTask<Void, Void, Void> {
        private final String url;
        private final Listener listener;
        private Bitmap img = null;
        private int width, height;
        private final boolean scaleIfSmaller;

        public static final int AUTO = -1;

        public bitmap(String url, Listener listener) { this(url, listener, AUTO, AUTO); }
        public bitmap(String url, Listener listener, int width, int height) { this(url, listener, width, height, true); }
        public bitmap(String url, Listener listener, int width, int height, boolean scaleIfSmaller) {
            this.url = url;
            this.listener = listener;
            this.width = width;
            this.height = height;
            this.scaleIfSmaller = scaleIfSmaller;
        }

        @Override
        protected Void doInBackground (Void...voids){
            try {
                InputStream in = new URL(url).openConnection().getInputStream();
                Bitmap tmp = BitmapFactory.decodeStream(in);
                in.close();
                if (width == AUTO && height == AUTO) img = tmp;
                else if (!scaleIfSmaller && (width > tmp.getWidth() || height > tmp.getHeight()) && (width > tmp.getWidth() && height == AUTO) || (height > tmp.getHeight() && width == AUTO)) img = tmp;
                else {
                    if (width == AUTO) width = height * tmp.getWidth() / tmp.getHeight();
                    else if (height == AUTO) height = width * tmp.getHeight() / tmp.getWidth();
                    img = Bitmap.createScaledBitmap(tmp, width, height, true);
                }
            }
            catch (FileNotFoundException ignore) {}
            catch (Exception e) { e.printStackTrace(); }
            catch (OutOfMemoryError e) {
                img.recycle();
                img = null;
                System.gc();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) { if (img != null) listener.onFinished(img); }
        public interface Listener { void onFinished(Bitmap img); }
    }
}
