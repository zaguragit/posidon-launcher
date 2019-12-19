package posidon.launcher.items;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

import posidon.launcher.tools.Settings;
import posidon.launcher.tools.Tools;

public class Folder extends LauncherItem {
    public final List<App> apps = new ArrayList<>();
    public String label;

    public Folder(Context context, String string) {
        String[] a = string.substring(7, string.length() - 1).split("¬");
        label = a[0];
        for (int i = 1; i < a.length; i++) {
            App app = App.get(a[i]);
            if (app != null) apps.add(app);
        }
        icon = new BitmapDrawable(context.getResources(), icon(context));
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (App  app : apps) if (app != null) sb.append("¬").append(app.packageName).append("/").append(app.name);
        return "folder(" + label + sb.toString() + ")";
    }

    private Bitmap icon(Context context) {
        try {
            int previewApps = Math.min(apps.size(), 4);
            Drawable[] drr = new Drawable[previewApps + 1];
            drr[0] = new ColorDrawable(Settings.getInt("folderBG", 0xdd111213));

            for (int i = 0; i < previewApps; i++) {
                drr[i + 1] = new BitmapDrawable(context.getResources(), Tools.drawable2bitmap(apps.get(i).icon));
            }

            LayerDrawable layerDrawable = new LayerDrawable(drr);
            int width = layerDrawable.getIntrinsicWidth();
            int height = layerDrawable.getIntrinsicHeight();
            int paddingNear = width / 6, paddingFar = width / 12 * 7, paddingMedium = (paddingFar + paddingNear) / 2;

            switch (previewApps) {
                case 1:
                    layerDrawable.setLayerInset(1, paddingMedium, paddingMedium, paddingMedium, paddingMedium);
                    break;
                case 2:
                    layerDrawable.setLayerInset(1, paddingNear, paddingMedium, paddingFar, paddingMedium);
                    layerDrawable.setLayerInset(2, paddingFar, paddingMedium, paddingNear, paddingMedium);
                    break;
                case 3:
                    layerDrawable.setLayerInset(1, paddingNear, paddingNear, paddingFar, paddingFar);
                    layerDrawable.setLayerInset(2, paddingFar, paddingNear, paddingNear, paddingFar);
                    layerDrawable.setLayerInset(3, paddingMedium, paddingFar, paddingMedium, paddingNear);
                    break;
                default:
                    layerDrawable.setLayerInset(1, paddingNear, paddingNear, paddingFar, paddingFar);
                    layerDrawable.setLayerInset(2, paddingFar, paddingNear, paddingNear, paddingFar);
                    layerDrawable.setLayerInset(3, paddingNear, paddingFar, paddingFar, paddingNear);
                    layerDrawable.setLayerInset(4, paddingFar, paddingFar, paddingNear, paddingNear);
                    break;
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            layerDrawable.setBounds(0, 0, width, height);
            layerDrawable.draw(canvas);
            Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(outputBitmap);
            if (Settings.getInt("icshape", 4) != 3) {
                final Path path = new Path();
                switch (Settings.getInt("icshape", 4)) {
                    case 1:
                        path.addCircle((float) width / 2f + 1, (float) height / 2f + 1, Math.min(width, ((float) height / 2f)) - 2, Path.Direction.CCW);
                        break;
                    case 2:
                        path.addRoundRect(2, 2, width - 2, height - 2, (float) Math.min(width, height) / 4f, (float) Math.min(width, height) / 4f, Path.Direction.CCW);
                        break;
                    case 0:
                    case 4:
                        //Formula: (|x|)^3 + (|y|)^3 = radius^3
                        int xx = 2, yy = 2, radius = Math.min(width, height) / 2 - 2;
                        final double radiusToPow = radius * radius * radius;
                        path.moveTo(-radius, 0);
                        for (int x = -radius; x <= radius; x++)
                            path.lineTo(x, ((float) Math.cbrt(radiusToPow - Math.abs(x * x * x))));
                        for (int x = radius; x >= -radius; x--)
                            path.lineTo(x, ((float) -Math.cbrt(radiusToPow - Math.abs(x * x * x))));
                        path.close();

                        Matrix matrix = new Matrix();
                        matrix.postTranslate(xx + radius, yy + radius);
                        path.transform(matrix);
                        break;
                }
                canvas.clipPath(path);
            }
            Paint p = new Paint();
            p.setAntiAlias(true);
            p.setFilterBitmap(true);
            canvas.drawBitmap(bitmap, 0, 0, p);
            bitmap = outputBitmap;
            return bitmap;
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public void clear() {
        apps.clear();
    }
}
