package posidon.launcher.items;

import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherApps.ShortcutQuery;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import posidon.launcher.R;
import posidon.launcher.tools.Settings;
import posidon.launcher.tools.Tools;

public class App extends LauncherItem {
    public String name;
    public String packageName;
    private static HashMap<String, App> appsByName = new HashMap<>();
    private static HashMap<String, App> appsByName2 = new HashMap<>();
    public static final ArrayList<App> hidden = new ArrayList<>();

    public static App get(String component) { return appsByName.get(component); }
    public static void putInSecondMap(String component, App app) { appsByName2.put(component, app); }

    public static void swapMaps() {
        HashMap<String, App> tmp = appsByName;
        appsByName = appsByName2;
        appsByName2 = tmp;
    }

    public static void clearSecondMap() { appsByName2.clear(); }

    public void open(final Context context, final View view) {
        try {
            final Intent launchintent = new Intent(Intent.ACTION_MAIN);
            launchintent.setComponent(new ComponentName(packageName, name));
            launchintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            switch (Settings.getString("anim:app_open", "posidon")) {
                case "scale_up":
                    context.startActivity(launchintent, ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getMeasuredWidth(), view.getMeasuredHeight()).toBundle());
                    break;
                case "clip_reveal":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        context.startActivity(launchintent, ActivityOptions.makeClipRevealAnimation(view, 0, 0, view.getMeasuredWidth(), view.getMeasuredHeight()).toBundle());
                        break;
                    }
                default:
                    context.startActivity(launchintent, ActivityOptions.makeCustomAnimation(context, R.anim.appopen, R.anim.slightfadeout).toBundle());
                    break;
            }

        } catch (Exception ignore) {}
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public List<ShortcutInfo> getShortcuts(Context context) {
        ShortcutQuery shortcutQuery = new ShortcutQuery();
        shortcutQuery.setQueryFlags(ShortcutQuery.FLAG_MATCH_DYNAMIC | ShortcutQuery.FLAG_MATCH_MANIFEST | ShortcutQuery.FLAG_MATCH_PINNED);
        shortcutQuery.setPackage(packageName);
        try { return ((LauncherApps)(Objects.requireNonNull(context.getSystemService(Context.LAUNCHER_APPS_SERVICE)))).getShortcuts(shortcutQuery, Process.myUserHandle()); }
        catch (Exception e) { return Collections.emptyList(); }
    }

    public void showProperties(final Context context, int appcolor) {
        final BottomSheetDialog d = new BottomSheetDialog(context, R.style.bottomsheet);
        d.setContentView(R.layout.app_properties);
        GradientDrawable g = (GradientDrawable) context.getResources().getDrawable(R.drawable.bottom_sheet);
        g.setColor(appcolor);
        Objects.requireNonNull(d.getWindow()).findViewById(R.id.design_bottom_sheet).setBackground(g);
        ((TextView) Objects.requireNonNull(d.findViewById(R.id.appname))).setText(label);
        ((ImageView) Objects.requireNonNull(d.findViewById(R.id.iconimg))).setImageBitmap(Tools.drawable2bitmap(icon));
        try { ((TextView) Objects.requireNonNull(d.findViewById(R.id.version))).setText(context.getPackageManager().getPackageInfo(packageName, 0).versionName); }
        catch (PackageManager.NameNotFoundException ignored) {}
        if (Settings.getBool("showcomponent", false)) {
            ((TextView) Objects.requireNonNull(d.findViewById(R.id.componentname))).setText(packageName + "/" + name);
            Objects.requireNonNull(d.findViewById(R.id.component)).setVisibility(View.VISIBLE);
        }
        Objects.requireNonNull(d.findViewById(R.id.openinsettings)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setData(Uri.parse("package:" + packageName));
                context.startActivity(i, ActivityOptions.makeCustomAnimation(context, R.anim.slideup, R.anim.slightfadeout).toBundle());
            }
        });
        Objects.requireNonNull(d.findViewById(R.id.uninstallbtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.vibrate(context);
                d.dismiss();
                Intent uninstallintent = new Intent("android.intent.action.DELETE");
                uninstallintent.setData(Uri.parse("package:" + packageName));
                context.startActivity(uninstallintent);
            }
        });

        /*context.findViewById(R.id.drawergrid).animate().scaleX(0.9f).scaleY(0.9f).setInterpolator(new PathInterpolator(0.245f, 1.275f, 0.405f, 1.005f)).setDuration(300L);
        context.findViewById(R.id.desktop).animate().scaleX(0.9f).scaleY(0.9f).setInterpolator(new PathInterpolator(0.245f, 1.275f, 0.405f, 1.005f)).setDuration(300L);
        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                context.findViewById(R.id.drawergrid).animate().scaleX(1).scaleY(1).setDuration(300L);
                context.findViewById(R.id.desktop).animate().scaleX(1).scaleY(1).setDuration(300L);
                context.getWindow().setNavigationBarColor(0x0);
            }
        });*/
        d.show();
    }
}
