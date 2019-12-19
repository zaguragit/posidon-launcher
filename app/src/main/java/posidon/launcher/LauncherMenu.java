package posidon.launcher;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.Gravity;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.animation.PathInterpolator;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.Objects;

import posidon.launcher.customizations.Customizations;
import posidon.launcher.tools.Settings;
import posidon.launcher.tools.Tools;
import posidon.launcher.wall.Gallery;

public class LauncherMenu implements View.OnLongClickListener {

    private final Context context;
    private final Window window;
    static boolean isActive = false;
    public static Dialog dialog;

    public LauncherMenu(Context context, Window window) {
        this.context = context;
        this.window = window;
    }

    @Override
    public boolean onLongClick(View v) {
        if (!isActive) menu(context, window);
        return true;
    }

    static class PinchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private final Context context;
        private final Window window;

        PinchListener(Context context, Window window) {
            this.context = context;
            this.window = window;
        }

        @Override public boolean onScale(ScaleGestureDetector detector) { return true; }
        @Override public void onScaleEnd(ScaleGestureDetector detector) { if (!isActive) { menu(context, window); }}
    }

    private static void menu(final Context context, final Window window) {
        isActive = true;
        Tools.vibrate(context);
        final View homescreen = window.getDecorView().findViewById(android.R.id.content);
        final View page = homescreen.findViewById(R.id.desktop);
        page.animate().scaleX(0.65f).scaleY(0.65f).translationY(page.getHeight() * -0.05f).setInterpolator(new PathInterpolator(0.245f, 1.275f, 0.405f, 1.005f)).setDuration(450L);
        final BottomSheetBehavior behavior = BottomSheetBehavior.from(homescreen.findViewById(R.id.drawer));
        behavior.setHideable(true);
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        dialog = new Dialog(context, R.style.longpressmenusheet);
        dialog.setContentView(R.layout.menu);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        Objects.requireNonNull(dialog.findViewById(R.id.custombtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, Customizations.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                context.startActivity(i, ActivityOptions.makeCustomAnimation(context, R.anim.slideup, R.anim.slightfadeout).toBundle());
                dialog.dismiss();
            }
        });
        Objects.requireNonNull(dialog.findViewById(R.id.wallbtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, Gallery.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                context.startActivity(i, ActivityOptions.makeCustomAnimation(context, R.anim.slideup, R.anim.slightfadeout).toBundle());
                dialog.dismiss();
            }
        });
        Objects.requireNonNull(dialog.findViewById(R.id.widgetpickerbtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Main)context).selectWidget();
                dialog.dismiss();
            }
        });
        page.setBackgroundResource(R.drawable.page);
        if (Tools.canBlurWall(context)) window.setBackgroundDrawable(new LayerDrawable(new Drawable[]{new BitmapDrawable(context.getResources(), Tools.blurredWall(context, Settings.getFloat("blurradius", 15))), context.getDrawable(R.drawable.black_gradient)}));
        else window.setBackgroundDrawableResource(R.drawable.black_gradient);
        homescreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { dialog.dismiss(); }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) { exit(homescreen, window, behavior); }
        });
        dialog.show();
        ArrayList<Rect> list = new ArrayList<>();
        homescreen.setSystemGestureExclusionRects(list);
    }

    private static void exit(final View homescreen, Window window, BottomSheetBehavior behavior) {
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        final View page = homescreen.findViewById(R.id.desktop);
        page.animate().scaleX(1).scaleY(1).translationY(0).setDuration(400L);
        page.setBackgroundColor(0x0);
        window.setBackgroundDrawableResource(android.R.color.transparent);
        behavior.setHideable(false);
        ArrayList<Rect> list = new ArrayList<>();
        list.add(new Rect(0, 0, Tools.getDisplayWidth(homescreen.getContext()), Tools.getDisplayHeight(homescreen.getContext())));
        homescreen.setSystemGestureExclusionRects(list);
        isActive = false;
    }
}
