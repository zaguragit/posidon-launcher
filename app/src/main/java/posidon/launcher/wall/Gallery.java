package posidon.launcher.wall;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Objects;

import posidon.launcher.R;
import posidon.launcher.tools.ColorTools;
import posidon.launcher.tools.Settings;
import posidon.launcher.tools.Tools;

public class Gallery extends AppCompatActivity {

    public static List<Wall> walls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Settings.init(this);
        Tools.applyFontSetting(this);
        setContentView(R.layout.wall_gallery);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        int sidepadding = (int)(28 * getResources().getDisplayMetrics().density);
        int gridsidepadding = (int)(12 * getResources().getDisplayMetrics().density);
        int toolbarHeight = Tools.navbarHeight + (int)(64 * getResources().getDisplayMetrics().density);
        findViewById(R.id.toolbar).setPadding(sidepadding, 0, sidepadding, Tools.navbarHeight);
        findViewById(R.id.toolbar).getLayoutParams().height = toolbarHeight;
        findViewById(R.id.gallery).setPadding(gridsidepadding, Tools.getStatusBarHeight(this), gridsidepadding, toolbarHeight);
        findViewById(R.id.pickwallbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        pickwall();
                    } else {
                        new AlertDialog.Builder(Gallery.this).setTitle("Permission needed").setMessage("posidon launcher needs the permission to read storage to access the images on your device")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) { requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 2); }
                                })
                                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); }
                                })
                                .setIcon(getDrawable(R.drawable.ic_files)).show();
                    }
                } else Toast.makeText(Gallery.this, "Please, grant the 'read external files' permission in settings", Toast.LENGTH_LONG).show();
            }
        });
        findViewById(R.id.colorwallbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { ColorTools.pickWallColor(Gallery.this); }
        });
        final ImageView loading = findViewById(R.id.loading);
        Tools.animate(loading.getDrawable());
        final GridView gallery = findViewById(R.id.gallery);
        WallLoader l = new WallLoader();
        l.setListener(new WallLoader.AsyncTaskListener() {
            @Override
            public void onAsyncTaskFinished(List<Wall> walls) {
                Gallery.walls = walls;
                if (loading.getDrawable() instanceof AnimatedVectorDrawable && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ((AnimatedVectorDrawable)loading.getDrawable()).clearAnimationCallbacks();
                else Tools.clearAnimation(loading.getDrawable());
                loading.setVisibility(View.GONE);
                if (walls != null) {
                    gallery.setAdapter(new WallAdapter(Gallery.this));
                    gallery.setOnItemClickListener(new GalleryItemClickListener(Gallery.this));
                } else findViewById(R.id.fail).setVisibility(View.VISIBLE);
            }
        });
        l.execute();
    }

    private void pickwall() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
        System.gc();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) pickwall();
            else Toast.makeText(this, "No permission, no wallpapers ;(", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                WallActivity.img = null;
                try {
                    WallActivity.img = BitmapFactory.decodeStream(getBaseContext().getContentResolver().openInputStream(Objects.requireNonNull(data.getData())));}
                catch (FileNotFoundException e) {e.printStackTrace();}
                startActivity(new Intent(this, WallActivity.class));
                System.gc();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        overridePendingTransition(R.anim.fadein, R.anim.slidedown);
        super.onPause();
    }
}
