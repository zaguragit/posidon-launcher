package posidon.launcher.tools;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.Type;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Objects;

import posidon.launcher.R;

import static android.content.Context.VIBRATOR_SERVICE;

public class Tools {

	public static int numtodp(int in, Activity activity) { return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, in, activity.getResources().getDisplayMetrics()); }

	public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
	    int width = bm.getWidth();
	    int height = bm.getHeight();
	    float scaleWidth = ((float) newWidth) / width;
	    float scaleHeight = ((float) newHeight) / height;
	    Matrix matrix = new Matrix();
	    matrix.postScale(scaleWidth, scaleHeight);
	    return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
	}

	public static Matrix getResizedMatrix(Bitmap bm, int newHeight, int newWidth) {
	    int width = bm.getWidth();
	    int height = bm.getHeight();
	    float scaleWidth = ((float) newWidth) / width;
	    float scaleHeight = ((float) newHeight) / height;
	    Matrix matrix = new Matrix();
	    matrix.postScale(scaleWidth, scaleHeight);
	    return matrix;
	}

	public static Drawable animate(Drawable d) {
		if (d instanceof AnimatedVectorDrawable && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			final AnimatedVectorDrawable avd = (AnimatedVectorDrawable) d;
			avd.registerAnimationCallback(new Animatable2.AnimationCallback() {
				@Override
				public void onAnimationEnd(Drawable drawable) {
					avd.start();
				}
			});
			avd.start();
		} else if (d instanceof AnimatedVectorDrawableCompat) {
			final AnimatedVectorDrawableCompat avd = (AnimatedVectorDrawableCompat) d;
			avd.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
				@Override
				public void onAnimationEnd(Drawable drawable) {
					avd.start();
				}
			});
			avd.start();
		} else if (d instanceof AnimationDrawable) ((AnimationDrawable) d).start();
		return d;
	}

	public static void clearAnimation(Drawable d) {
		if (d instanceof AnimatedVectorDrawable && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			final AnimatedVectorDrawable avd = (AnimatedVectorDrawable) d;
			avd.clearAnimationCallbacks();
		} else if (d instanceof AnimatedVectorDrawableCompat) {
			final AnimatedVectorDrawableCompat avd = (AnimatedVectorDrawableCompat) d;
			avd.clearAnimationCallbacks();
		} else if (d instanceof AnimationDrawable) ((AnimationDrawable) d).stop();
	}

	public static boolean isInstalled(String packageName, PackageManager packageManager) {
		boolean found = true;
		try { packageManager.getPackageInfo(packageName, 0); }
		catch (PackageManager.NameNotFoundException e) { found = false; }
		return found;
	}

	public static int getDisplayWidth(Context context) { return context.getResources().getDisplayMetrics().widthPixels; }
	public static int getDisplayHeight(Context context) { return context.getResources().getDisplayMetrics().heightPixels; }

	/*try {
		Object sbs = getSystemService("statusbar");
		Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
		Method showsb = statusbarManager.getMethod("expandNotificationsPanel");
		showsb.invoke(sbs);
	}
	catch (ClassNotFoundException e) {}
	catch (NoSuchMethodException e) {}
	catch (IllegalAccessException e) {}
	catch (InvocationTargetException e) {} */

	public static Bitmap blurBitmap(Context context, Bitmap bitmap, float r) {
		if (r > 0) { if (r > 25) r = 25;
			RenderScript rs = RenderScript.create(context);
			Allocation allocation = Allocation.createFromBitmap(rs, bitmap);
			Type t = allocation.getType();
			Allocation blurredAllocation = Allocation.createTyped(rs, t);
			ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
			blurScript.setRadius(r);
			blurScript.setInput(allocation);
			blurScript.forEach(blurredAllocation);
			blurredAllocation.copyTo(bitmap);
			allocation.destroy();
			blurredAllocation.destroy();
			blurScript.destroy();
			rs.destroy();
		} return bitmap;
	}


	public static Bitmap fastBlur(Bitmap bitmap, int radius) {
		float d = Math.max(radius, 1);
		int width = Math.round(bitmap.getWidth() / d);
		int height = Math.round(bitmap.getHeight() / d);
		bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

		bitmap = bitmap.copy(bitmap.getConfig(), true);

		if (radius < 1) return null;

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		int[] pix = new int[w * h];
		//Log.e("pix", w + " " + h + " " + pix.length);
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);

		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;

		int[] r = new int[wh];
		int[] g = new int[wh];
		int[] b = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int[] vmin = new int[Math.max(w, h)];

		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int[] dv = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) dv[i] = i / divsum;

		yw = yi = 0;

		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;

		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = radius;

			for (x = 0; x < w; x++) {
				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];
				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;
				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];
				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];
				if (y == 0) vmin[x] = Math.min(x + radius + 1, wm);
				p = pix[yw + vmin[x]];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];
				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;
				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];
				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];
				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];
				yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;
				sir = stack[i + radius];
				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];
				rbs = r1 - Math.abs(i);
				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				} if (i < hm) yp += w;
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				// Preserve alpha channel: ( 0xff000000 & pix[yi] )
				pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;
				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];
				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];
				if (x == 0) vmin[y] = Math.min(y + r1, hm) * w;
				p = x + vmin[y];
				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];
				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];
				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;
				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];
				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];
				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];
				yi += w;
			}
		}
		//Log.e("pix", w + " " + h + " " + pix.length);
		bitmap.setPixels(pix, 0, w, 0, 0, w, h);
		return bitmap;
	}






	public static Bitmap drawable2bitmap(Drawable drawable) {
		return drawable2bitmap(drawable, false);
	}

	public static Bitmap drawable2bitmap(Drawable drawable, boolean duplicateIfBitmapDrawable) {
		Bitmap bitmap;
		if (drawable instanceof BitmapDrawable) {
			BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			if(bitmapDrawable.getBitmap() != null) {
				if (duplicateIfBitmapDrawable) return Bitmap.createBitmap(bitmapDrawable.getBitmap());
				else return bitmapDrawable.getBitmap();
			}
		}
		if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0)
			bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
		else bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		try { drawable.draw(canvas); } catch (Exception ignore) {}
		return bitmap;
	}

	public static boolean canBlurWall(Context context) {
		return Settings.getBool("blur", true) && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
	}

	public static Bitmap blurredWall(Context context, float radius) { try {
			@SuppressLint("MissingPermission") Bitmap bitmap = Tools.drawable2bitmap(WallpaperManager.getInstance(context).peekFastDrawable());
			int displayWidth = context.getResources().getDisplayMetrics().widthPixels, displayHeight = context.getResources().getDisplayMetrics().heightPixels + navbarHeight;
			if (bitmap.getWidth() > radius && bitmap.getHeight() > radius) {
				if (bitmap.getHeight() / (float) bitmap.getWidth() < displayHeight / (float) displayWidth) {
					bitmap = Bitmap.createScaledBitmap(
							bitmap,
							displayHeight * bitmap.getWidth() / bitmap.getHeight(),
							displayHeight,
							false);
					bitmap = Bitmap.createBitmap(
							bitmap, 0, 0,
							displayWidth,
							displayHeight);
				} else if (bitmap.getHeight() / (float) bitmap.getWidth() > displayHeight / (float) displayWidth) {
					bitmap = Bitmap.createScaledBitmap(
							bitmap,
							displayWidth,
							displayWidth * bitmap.getHeight() / bitmap.getWidth(),
							false);
					bitmap = Bitmap.createBitmap(
							bitmap, 0, (bitmap.getHeight() - displayHeight) >> 1,
							displayWidth,
							displayHeight);
				} else bitmap = Bitmap.createScaledBitmap(bitmap, displayWidth, displayHeight, false);
				if (radius > 0) try {
					//float d = Math.max(radius, 1);
					//bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() / d), (int) (bitmap.getHeight() / d), false);
					bitmap = Tools.fastBlur(bitmap, (int)radius);
				} catch (Exception e) { e.printStackTrace(); }
			} return bitmap;
		} catch (OutOfMemoryError e) {
			Toast.makeText(context, "OutOfMemoryError: Couldn't blur wallpaper!", Toast.LENGTH_SHORT).show();
		} catch (Exception e) { e.printStackTrace(); }
		return null;
	}

	public static Bitmap centerCropWallpaper(Context context, Bitmap wallpaper) {
		int scaledWidth = context.getResources().getDisplayMetrics().heightPixels * wallpaper.getWidth() / wallpaper.getHeight();

		Bitmap scaledWallpaper = Bitmap.createScaledBitmap(
				wallpaper,
				scaledWidth,
				context.getResources().getDisplayMetrics().heightPixels,
				false);

		scaledWallpaper = Bitmap.createBitmap(
				scaledWallpaper,
				(scaledWidth - context.getResources().getDisplayMetrics().widthPixels) >> 1,
				0,
				context.getResources().getDisplayMetrics().widthPixels,
				context.getResources().getDisplayMetrics().heightPixels);

		return scaledWallpaper;
	}

	/*public static int getNavbarHeight(Context c) {
		int id = c.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
		if(id > 0 && c.getResources().getBoolean(id) && !ViewConfiguration.get(c).hasPermanentMenuKey()) {
			Resources resources = c.getResources();
			int orientation = resources.getConfiguration().orientation;
			int resourceId;
			if (isTablet(c)) resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
			else resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_width", "dimen", "android");
			if (resourceId > 0) return resources.getDimensionPixelSize(resourceId);
		}
		return 0;
	}*/

	public static int navbarHeight = 0;
	public static void updateNavbarHeight(Activity activity) {
		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int usableHeight = metrics.heightPixels;
		activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
		int realHeight = metrics.heightPixels;
		navbarHeight = (realHeight > usableHeight) ? realHeight - usableHeight : 0;
	}

	public static int getStatusBarHeight(Context c) {
		int resourceId = c.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) return c.getResources().getDimensionPixelSize(resourceId);
		return 0;
	}

	public static boolean isTablet(Context c) { return (c.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE; }

	public static void vibrate(Context context) {
		int duration = Settings.getInt("hapticfeedback", 14);
		if (duration != 0) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
				((Vibrator) context.getSystemService(VIBRATOR_SERVICE)).vibrate(
						VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE),
						new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
				);
			else ((Vibrator) context.getSystemService(VIBRATOR_SERVICE)).vibrate(duration);
		}
	}

	public static boolean isAirplaneModeOn(Context context) {
		return android.provider.Settings.System.getInt(context.getContentResolver(), android.provider.Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	public static Drawable adaptic(Context context, Drawable drawable) {
		int icShape = Settings.getInt("icshape", 4);
		if (icShape == 0) return drawable;
		else if (drawable instanceof AdaptiveIconDrawable || Settings.getBool("reshapeicons", false)) {
			Drawable[] drr = new Drawable[2];
			if (drawable instanceof AdaptiveIconDrawable) {
				AdaptiveIconDrawable aid = ((AdaptiveIconDrawable) drawable);
				drr[0] = aid.getBackground();
				drr[1] = aid.getForeground();
			} else {
				drr[0] = new ColorDrawable(0xFFFFFFFF);
				BitmapDrawable d = (BitmapDrawable) drawable;
				Bitmap b = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
				Canvas c = new Canvas(b);
				d.setBounds(c.getWidth() / 4, c.getHeight() / 4, c.getWidth() / 4 * 3, c.getHeight() / 4 * 3);
				drawable.draw(c);
				drr[1] = new BitmapDrawable(context.getResources(), b);
			}
			LayerDrawable layerDrawable = new LayerDrawable(drr);
			int width = layerDrawable.getIntrinsicWidth();
			int height = layerDrawable.getIntrinsicHeight();
			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			layerDrawable.setBounds(-canvas.getWidth() / 4, -canvas.getHeight() / 4, canvas.getWidth() / 4 * 5, canvas.getHeight() / 4 * 5);
			layerDrawable.draw(canvas);
			Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			canvas = new Canvas(outputBitmap);
			if (icShape != 3) {
				Path path = new Path();
				if (icShape == 1)
					path.addCircle((float)width / 2f + 1, (float)height / 2f + 1, Math.min(width, ((float)height / 2f)) - 2, Path.Direction.CCW);
				else if (icShape == 2)
					path.addRoundRect(2, 2, width - 2, height - 2, (float)Math.min(width, height) / 4f, (float)Math.min(width, height) / 4f, Path.Direction.CCW);
				else if (icShape == 4) {
					//Formula: (|x|)^3 + (|y|)^3 = radius^3
					int xx = 2, yy = 2, radius = (Math.min(width, height) >> 1) - 2;
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
				}
				canvas.clipPath(path);
			}
			Paint p = new Paint();
			p.setAntiAlias(true);
			p.setFilterBitmap(true);
			canvas.drawBitmap(bitmap, 0, 0, p);
			bitmap = outputBitmap;
			return new BitmapDrawable(context.getResources(), bitmap);
		} else return drawable;
	}

	public static void applyFontSetting(Activity activity) {
		switch (Settings.getString("font", "ubuntu")) {
			case "sansserif":
				activity.getTheme().applyStyle(R.style.font_sans_serif, true);
				break;
			case "posidonsans":
				activity.getTheme().applyStyle(R.style.font_posidon_sans, true);
				break;
			case "monospace":
				activity.getTheme().applyStyle(R.style.font_monospace, true);
				break;
			case "ubuntu":
				activity.getTheme().applyStyle(R.style.font_ubuntu, true);
				break;
			case "lexendDeca":
				activity.getTheme().applyStyle(R.style.font_lexend_deca, true);
				break;
			case "openDyslexic":
				activity.getTheme().applyStyle(R.style.font_open_dyslexic, true);
				break;
		}
	}


	public static void showStackTrace(Context context, Throwable t) {
		StackTraceElement[] stackTrace = t.getStackTrace();
		BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.bottomsheet);
		dialog.setContentView(R.layout.bug_report);
		Objects.requireNonNull(dialog.getWindow()).findViewById(R.id.design_bottom_sheet).setBackground(context.getDrawable(R.drawable.bottom_sheet));
		StringBuilder builder = new StringBuilder();
		for (StackTraceElement line : stackTrace) {
			builder.append("\t").append(line);
		}
		TextView stackTraceView = dialog.findViewById(R.id.txt);
		stackTraceView.setText(builder);
		dialog.show();
		Log.d("BUG", builder.toString());
	}
}
