package posidon.launcher.desktop;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;

import androidx.fragment.app.FragmentActivity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import posidon.launcher.Main;
import posidon.launcher.R;
import posidon.launcher.items.App;
import posidon.launcher.tools.Settings;
import posidon.launcher.tools.Sort;
import posidon.launcher.tools.ThemeTools;
import posidon.launcher.tools.Tools;

public class DesktopMode extends FragmentActivity {

	private PopupWindow window;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.desktop);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		ImageView menuBtn = findViewById(R.id.menuBtn);
		Tools.animate(menuBtn.getDrawable());
		setApps();
	}

	public void showMenu(View view) { startActivity(new Intent(this, AppList.class)); }

	private void setApps() {
		int skippedapps = 0;
		List<ResolveInfo> pacslist = getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0);
		Main.apps = new App[pacslist.size()];

		final int ICONSIZE = Tools.numtodp(65, this);
		Resources themeRes = null;
		String iconpackName = Settings.getString("iconpack", "system");
		String iconResource;
		int intres;
		int intresiconback = 0;
		int intresiconfront = 0;
		int intresiconmask = 0;
		float scaleFactor;
		Paint p = new Paint(Paint.FILTER_BITMAP_FLAG);
		p.setAntiAlias(true);
		Paint origP = new Paint(Paint.FILTER_BITMAP_FLAG);
		origP.setAntiAlias(true);
		Paint maskp= new Paint(Paint.FILTER_BITMAP_FLAG);
		maskp.setAntiAlias(true);
		maskp.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
		if (iconpackName.compareTo("") != 0) {
			try { themeRes = getPackageManager().getResourcesForApplication(iconpackName); }
			catch(Exception ignore) {}
			if (themeRes != null) {
				String[] backAndMaskAndFront = ThemeTools.getIconBackAndMaskResourceName(themeRes, iconpackName);
				if (backAndMaskAndFront[0] != null) intresiconback = themeRes.getIdentifier(backAndMaskAndFront[0],"drawable", iconpackName);
				if (backAndMaskAndFront[1] != null) intresiconmask = themeRes.getIdentifier(backAndMaskAndFront[1],"drawable", iconpackName);
				if (backAndMaskAndFront[2] != null) intresiconfront = themeRes.getIdentifier(backAndMaskAndFront[2],"drawable", iconpackName);
			}
		}
		BitmapFactory.Options uniformOptions = new BitmapFactory.Options();
		uniformOptions.inScaled = false;
		Canvas origCanv;
		Canvas canvas;
		scaleFactor = ThemeTools.getScaleFactor(themeRes, iconpackName);
		Bitmap back = null;
		Bitmap mask = null;
		Bitmap front = null;
		Bitmap scaledBitmap;
		Bitmap scaledOrig;
		Bitmap orig;

		if (iconpackName.compareTo("") != 0 && themeRes != null) {
			if (intresiconback != 0) back = BitmapFactory.decodeResource(themeRes, intresiconback, uniformOptions);
			if (intresiconmask != 0) mask = BitmapFactory.decodeResource(themeRes, intresiconmask, uniformOptions);
			if (intresiconfront != 0) front = BitmapFactory.decodeResource(themeRes, intresiconfront, uniformOptions);
		}

		for (int i = 0; i < pacslist.size(); i++) {
			if (Settings.getBool(pacslist.get(i).activityInfo.packageName + "/" + pacslist.get(i).activityInfo.name + "?hidden", false)) skippedapps++;
			else {
				Main.apps[i-skippedapps] = new App();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					try { Main.apps[i-skippedapps].icon = Tools.adaptic(DesktopMode.this, getPackageManager().getActivityIcon(new ComponentName(pacslist.get(i).activityInfo.packageName, pacslist.get(i).activityInfo.name))); }
					catch (Exception e) { e.printStackTrace(); }
				} else Main.apps[i-skippedapps].icon = pacslist.get(i).loadIcon(getPackageManager());
				Main.apps[i-skippedapps].packageName = pacslist.get(i).activityInfo.packageName;
				Main.apps[i-skippedapps].name = pacslist.get(i).activityInfo.name;
				Main.apps[i-skippedapps].label = Settings.getString(Main.apps[i-skippedapps].packageName + "/" + Main.apps[i-skippedapps].name + "?label", pacslist.get(i).loadLabel(getPackageManager()).toString());

				intres = 0;
				iconResource = ThemeTools.getResourceName(themeRes, iconpackName, "ComponentInfo{" + Main.apps[i - skippedapps].packageName + "/" + Main.apps[i - skippedapps].name + "}");
				if (iconResource != null) intres = Objects.requireNonNull(themeRes).getIdentifier(iconResource, "drawable", iconpackName);
				if (intres != 0) try {
					//Do NOT add the theme parameter to getDrawable()
					Main.apps[i - skippedapps].icon = themeRes.getDrawable(intres);
					try { if (!((PowerManager) getSystemService(Context.POWER_SERVICE)).isPowerSaveMode() && Settings.getBool("animatedicons", true)) Tools.animate(Main.apps[i - skippedapps].icon); }
					catch (Exception ignore) {}
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Main.apps[i - skippedapps].icon = Tools.adaptic(DesktopMode.this, Main.apps[i - skippedapps].icon);
				} catch (Exception e) { e.printStackTrace(); } else {
					orig = Bitmap.createBitmap(Main.apps[i - skippedapps].icon.getIntrinsicWidth(), Main.apps[i - skippedapps].icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
					Main.apps[i - skippedapps].icon.setBounds(0, 0, Main.apps[i - skippedapps].icon.getIntrinsicWidth(), Main.apps[i - skippedapps].icon.getIntrinsicHeight());
					Main.apps[i - skippedapps].icon.draw(new Canvas(orig));
					scaledOrig = Bitmap.createBitmap(ICONSIZE, ICONSIZE, Bitmap.Config.ARGB_8888);
					scaledBitmap = Bitmap.createBitmap(ICONSIZE, ICONSIZE, Bitmap.Config.ARGB_8888);
					canvas = new Canvas(scaledBitmap);
					if (back != null) canvas.drawBitmap(back, Tools.getResizedMatrix(back, ICONSIZE, ICONSIZE), p);
					origCanv = new Canvas(scaledOrig);
					orig = Tools.getResizedBitmap(orig, ((int) (ICONSIZE * scaleFactor)), ((int) (ICONSIZE * scaleFactor)));
					origCanv.drawBitmap(orig, scaledOrig.getWidth() - (orig.getWidth() / 2f) - scaledOrig.getWidth() / 2f, scaledOrig.getWidth() - (orig.getWidth() / 2f) - scaledOrig.getWidth() / 2f, origP);
					if (mask != null) origCanv.drawBitmap(mask, Tools.getResizedMatrix(mask, ICONSIZE, ICONSIZE), maskp);
					if (back != null) canvas.drawBitmap(Tools.getResizedBitmap(scaledOrig, ICONSIZE, ICONSIZE), 0, 0, p);
					else canvas.drawBitmap(Tools.getResizedBitmap(scaledOrig, ICONSIZE, ICONSIZE), 0, 0, p);
					if (front != null) canvas.drawBitmap(front, Tools.getResizedMatrix(front, ICONSIZE, ICONSIZE), p);
					Main.apps[i - skippedapps].icon = new BitmapDrawable(getResources(), scaledBitmap);
				}
			}
		}
		Main.apps = Arrays.copyOf(Main.apps, Main.apps.length - skippedapps);
		if (Settings.getInt("sortAlgorithm", 1) == 1) Sort.colorSort(Main.apps);
		else Sort.labelSort(Main.apps);
	}

}