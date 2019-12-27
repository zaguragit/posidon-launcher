package posidon.launcher.wall;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.Objects;

import posidon.launcher.R;
import posidon.launcher.tools.Loader;
import posidon.launcher.tools.Tools;

public class WallActivity extends AppCompatActivity {

    public static Bitmap img;
    private ImageView loading;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.applyFontSetting(this);
        setContentView(R.layout.wall_preview);
        loading = findViewById(R.id.loading);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if (img.getHeight() / img.getWidth() < getResources().getDisplayMetrics().heightPixels / getResources().getDisplayMetrics().widthPixels)
            img = Tools.centerCropWallpaper(WallActivity.this, img);
        ((ImageView)findViewById(R.id.theimg)).setImageBitmap(img);
        final Bundle extras = getIntent().getExtras();
        if (extras == null) {
            loading.setVisibility(View.GONE);
            findViewById(R.id.downloadbtn).setVisibility(View.GONE);
        } else {
            Tools.animate(loading.getDrawable());
            index = extras.getInt("index");
            new Loader.bitmap(Gallery.walls.get(index).url, new Loader.bitmap.Listener() {
                @Override
                public void onFinished(Bitmap img) {
                    if (img.getHeight() / img.getWidth() < getResources().getDisplayMetrics().heightPixels / getResources().getDisplayMetrics().widthPixels)
                        img = Tools.centerCropWallpaper(WallActivity.this, img);
                    //else img = Bitmap.createBitmap(img, 0, 0, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().widthPixels/img.getWidth()*img.getHeight());
                    ((ImageView)findViewById(R.id.theimg)).setImageBitmap(img);
                    WallActivity.img = img;
                    if (loading.getDrawable() instanceof AnimatedVectorDrawable) {
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ((AnimatedVectorDrawable)loading.getDrawable()).clearAnimationCallbacks();
                        else ((AnimatedVectorDrawable)loading.getDrawable()).stop();
                    } else ((AnimatedVectorDrawableCompat) loading.getDrawable()).clearAnimationCallbacks();
                    loading.setVisibility(View.GONE);
                }
            }).execute();
            findViewById(R.id.downloadbtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { saveBitmap(img, Gallery.walls.get(index).name); }
            });
            findViewById(R.id.downloadbtn).setBackground(btnBG());
            try {
                ((TextView) findViewById(R.id.nametxt)).setText(Gallery.walls.get(index).name);
                ((TextView)findViewById(R.id.authortxt)).setText(Gallery.walls.get(index).author);
            } catch (Exception ignore) {}
        }
        int bottompadding = Tools.navbarHeight;
        if (bottompadding == 0) bottompadding = (int)(20 * getResources().getDisplayMetrics().density);
        findViewById(R.id.bottomstuff).setPadding(0,0, 0, bottompadding);
        findViewById(R.id.applybtn).setBackground(btnBG());
        findViewById(R.id.applybtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.bottomstuff).animate().alpha(0);
                final BottomSheetDialog dialog = new BottomSheetDialog(WallActivity.this, R.style.bottomsheet);
                dialog.setContentView(R.layout.wall_apply_dialog);
                Objects.requireNonNull(dialog.getWindow()).findViewById(R.id.design_bottom_sheet).setBackgroundColor(0x0);
                dialog.findViewById(R.id.home).setBackground(dialogBtnBG());
                dialog.findViewById(R.id.home).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) { new SetWall(img, new WeakReference<Context>(WallActivity.this), 0).execute(); dialog.dismiss(); }
                });
                dialog.findViewById(R.id.lock).setBackground(dialogBtnBG());
                dialog.findViewById(R.id.lock).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) { new SetWall(img, new WeakReference<Context>(WallActivity.this), 1).execute(); dialog.dismiss(); }
                });
                dialog.findViewById(R.id.both).setBackground(dialogBtnBG());
                dialog.findViewById(R.id.both).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) { new SetWall(img, new WeakReference<Context>(WallActivity.this), 2).execute(); dialog.dismiss(); }
                });
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        findViewById(R.id.bottomstuff).animate().alpha(1);
                    }
                });
                dialog.show();
            }
        });
        System.gc();
    }

    @Override
    protected void onPause() {
        overridePendingTransition(R.anim.slideup, R.anim.slidedown);
        super.onPause();
    }

    private ShapeDrawable btnBG() {
        float r = 24 * getResources().getDisplayMetrics().density;
        ShapeDrawable out = new ShapeDrawable(new RoundRectShape(new float[]{r, r, r, r, r, r, r, r}, null, null));
        out.getPaint().setColor(Palette.from(img).generate().getVibrantColor(0xff252627));
        return out;
    }

    private ShapeDrawable dialogBtnBG() {
        float r = 24 * getResources().getDisplayMetrics().density;
        ShapeDrawable out = new ShapeDrawable(new RoundRectShape(new float[]{r, r, r, r, r, r, r, r}, null, null));
        out.getPaint().setColor(Palette.from(img).generate().getDarkMutedColor(Palette.from(img).generate().getDominantColor(0xff111213)));
        return out;
    }

    private ShapeDrawable dialogBG() {
        float r = 40 * getResources().getDisplayMetrics().density;
        ShapeDrawable out = new ShapeDrawable(new RoundRectShape(new float[]{r, r, r, r, r, r, r, r}, null, null));
        out.getPaint().setColor(Palette.from(img).generate().getDarkMutedColor(Palette.from(img).generate().getDominantColor(0xff111213)));
        return out;
    }

    private void saveBitmap(Bitmap bitmap, String name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
            File direct = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ? getExternalFilesDir("Wallpapers") : new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Wallpapers");
            if (direct != null && (direct.exists() || direct.mkdirs())) {
                File file = new File(direct,name.replace(' ', '_') + ".png");
                if (file.exists()) Snackbar.make(findViewById(R.id.nametxt), "Already Saved: " + file.getAbsolutePath(), Snackbar.LENGTH_SHORT).show();
                else try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                    Snackbar.make(findViewById(R.id.nametxt), "Saved: " + file.getAbsolutePath(), Snackbar.LENGTH_SHORT).show();
                } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) saveBitmap(img, Gallery.walls.get(index).name);
    }
}
