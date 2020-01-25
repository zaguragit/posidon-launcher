package posidon.launcher.tools;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Objects;

import posidon.launcher.Main;
import posidon.launcher.R;

public class ColorTools {

	public static int blendColors(int color1, int color2, float ratio) {
		if (ratio > 1) ratio = 1;
		else if (ratio < 0) ratio = 0;
		final float inverseRatio = 1f - ratio;
		float a = (Color.alpha(color1) * ratio) + (Color.alpha(color2) * inverseRatio);
		float r = (Color.red(color1) * ratio) + (Color.red(color2) * inverseRatio);
		float g = (Color.green(color1) * ratio) + (Color.green(color2) * inverseRatio);
		float b = (Color.blue(color1) * ratio) + (Color.blue(color2) * inverseRatio);
		return Color.argb((int) a, (int) r, (int) g, (int) b);
	}

	public static boolean useDarkText(int bg) {
		return Color.green(bg) > 240 || (Color.green(bg) > 200 && Color.red(bg) > 120);
	}

	public static Drawable colorcircle(int color) {
		GradientDrawable d = new GradientDrawable();
		d.setShape(GradientDrawable.OVAL);
		d.setColor(color);
		d.setStroke(1, 0xff000000);
		return d;
	}

	public static void pickColor(final Context context, final String settingskey, int defaultcolor) {
		final BottomSheetDialog d = new BottomSheetDialog(context, R.style.bottomsheet);
		d.setContentView(R.layout.color_picker);
		Objects.requireNonNull(d.getWindow()).findViewById(R.id.design_bottom_sheet).setBackgroundResource(R.drawable.bottom_sheet);
		final EditText txt = d.findViewById(R.id.hextxt);
		Objects.requireNonNull(txt).addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			@Override
			public void afterTextChanged(Editable s) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try { Objects.requireNonNull(d.findViewById(R.id.bgColorPrev)).setBackgroundColor((int)Long.parseLong(s.toString(), 16)); }
				catch (NumberFormatException ignore) {}
			}
		});
		txt.setText(Integer.toHexString(Settings.getInt(settingskey, defaultcolor)));
		txt.setTextColor(useDarkText(Settings.getInt(settingskey, defaultcolor)) ? 0xff252627 : 0xffffffff);
		Objects.requireNonNull(d.findViewById(R.id.ok)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				d.dismiss();
				try { Settings.put(settingskey, (int)Long.parseLong(txt.getText().toString(), 16)); }
				catch (NumberFormatException e) { Toast.makeText(context, "That's not a color.", Toast.LENGTH_SHORT).show(); }
			}
		});
		final SeekBar alpha = d.findViewById(R.id.alpha);
		final SeekBar red = d.findViewById(R.id.red);
		final SeekBar green = d.findViewById(R.id.green);
		final SeekBar blue = d.findViewById(R.id.blue);
		StringBuilder hex = new StringBuilder(txt.getText().toString());
		while (hex.length()!=8) hex.insert(0, 0);
		Objects.requireNonNull(alpha).setProgress((int)Long.parseLong(hex.substring(0, 2), 16));
		Objects.requireNonNull(red).setProgress((int)Long.parseLong(hex.substring(2, 4), 16));
		Objects.requireNonNull(green).setProgress((int)Long.parseLong(hex.substring(4, 6), 16));
		Objects.requireNonNull(blue).setProgress((int)Long.parseLong(hex.substring(6, 8), 16));
		alpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				int color = progress*256*256*256+red.getProgress()*256*256+green.getProgress()*256+blue.getProgress();
				txt.setText(Integer.toHexString(color));
				txt.setTextColor(useDarkText(color) ? 0xff252627 : 0xffffffff);
			}
		});
		red.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				int color = alpha.getProgress()*256*256*256+progress*256*256+green.getProgress()*256+blue.getProgress();
				txt.setText(Integer.toHexString(color));
				txt.setTextColor(useDarkText(color) ? 0xff252627 : 0xffffffff);
			}
		});
		green.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				int color = alpha.getProgress()*256*256*256+red.getProgress()*256*256+progress*256+blue.getProgress();
				txt.setText(Integer.toHexString(color));
				txt.setTextColor(useDarkText(color) ? 0xff252627 : 0xffffffff);
			}
		});
		blue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				int color = alpha.getProgress()*256*256*256+red.getProgress()*256*256+green.getProgress()*256+progress;
				txt.setText(Integer.toHexString(color));
				txt.setTextColor(useDarkText(color) ? 0xff252627 : 0xffffffff);
			}
		});
		System.gc();
		d.show();
	}

	public static void pickColorNoAlpha(final Context context, final String settingskey, final int defaultcolor) {
		final BottomSheetDialog d = new BottomSheetDialog(context, R.style.bottomsheet);
		d.setContentView(R.layout.color_picker);
		Objects.requireNonNull(d.getWindow()).findViewById(R.id.design_bottom_sheet).setBackgroundResource(R.drawable.bottom_sheet);
		final EditText txt = d.findViewById(R.id.hextxt);
		assert txt != null;
		txt.setFilters(new InputFilter[] {new InputFilter.LengthFilter(6)});
		txt.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			@Override
			public void afterTextChanged(Editable s) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try { Objects.requireNonNull(d.findViewById(R.id.bgColorPrev)).setBackgroundColor((int)Long.parseLong("ff"+s.toString(), 16)); }
				catch (NumberFormatException ignored) {}
			}
		});
		txt.setText(Integer.toHexString(Settings.getInt(settingskey, defaultcolor)));
		txt.setTextColor(useDarkText(Settings.getInt(settingskey, defaultcolor)) ? 0xff252627 : 0xffffffff);
		Objects.requireNonNull(d.findViewById(R.id.ok)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				d.dismiss();
				try { Settings.put(settingskey, (int)Long.parseLong(txt.getText().toString(), 16)); }
				catch (NumberFormatException e) { Toast.makeText(context, "That's not a color.", Toast.LENGTH_SHORT).show(); }
			}
		});
		Objects.requireNonNull(d.findViewById(R.id.alpha)).setVisibility(View.GONE);
		final SeekBar red = d.findViewById(R.id.red);
		final SeekBar green = d.findViewById(R.id.green);
		final SeekBar blue = d.findViewById(R.id.blue);
		StringBuilder hex = new StringBuilder(txt.getText().toString());
		while (hex.length() != 8) hex.insert(0, 0);
		Objects.requireNonNull(red).setProgress((int)Long.parseLong(hex.substring(2, 4), 16));
		Objects.requireNonNull(green).setProgress((int)Long.parseLong(hex.substring(4, 6), 16));
		Objects.requireNonNull(blue).setProgress((int)Long.parseLong(hex.substring(6, 8), 16));
		Objects.requireNonNull(red).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				int color = progress * 256 * 256 + Objects.requireNonNull(green).getProgress() * 256 + Objects.requireNonNull(blue).getProgress();
				StringBuilder hex = new StringBuilder(Integer.toHexString(color));
				while (hex.length() != 6) hex.insert(0, 0);
				txt.setText(hex.toString());
				txt.setTextColor(useDarkText(color) ? 0xff252627 : 0xffffffff);
			}
		});
		Objects.requireNonNull(green).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				int color = red.getProgress() * 256 * 256 + progress * 256 + Objects.requireNonNull(blue).getProgress();
				StringBuilder hex = new StringBuilder(Integer.toHexString(color));
				while (hex.length() != 6) hex.insert(0, 0);
				txt.setText(hex.toString());
				txt.setTextColor(useDarkText(color) ? 0xff252627 : 0xffffffff);
			}
		});
		Objects.requireNonNull(blue).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				int color = red.getProgress() * 256 * 256 + green.getProgress() * 256 + progress;
				StringBuilder hex = new StringBuilder(Integer.toHexString(color));
				while (hex.length() != 6) hex.insert(0, 0);
				txt.setText(hex.toString());
				txt.setTextColor(useDarkText(color) ? 0xff252627 : 0xffffffff);
			}
		});
		System.gc();
		d.show();
	}

	public static void pickWallColor(final Context context) {
		final BottomSheetDialog d = new BottomSheetDialog(context, R.style.bottomsheet);
		d.setContentView(R.layout.color_picker);
		Objects.requireNonNull(d.getWindow()).findViewById(R.id.design_bottom_sheet).setBackgroundResource(R.drawable.bottom_sheet);
		final EditText txt = d.findViewById(R.id.hextxt);
		assert txt != null;
		txt.setFilters(new InputFilter[] {new InputFilter.LengthFilter(6)});
		txt.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			@Override
			public void afterTextChanged(Editable s) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try { Objects.requireNonNull(d.findViewById(R.id.bgColorPrev)).setBackgroundColor((int)Long.parseLong("ff"+s.toString(), 16)); }
				catch (NumberFormatException ignore) {}
			}
		});
		txt.setText("000000");
		Objects.requireNonNull(d.findViewById(R.id.ok)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				d.dismiss();
				try { WallpaperManager myWallpaperManager = WallpaperManager.getInstance(context);
					Bitmap c = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
					c.eraseColor((int)Long.parseLong("ff"+txt.getText().toString(), 16));
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
						if(myWallpaperManager.isWallpaperSupported()){
							try { myWallpaperManager.setBitmap(c); }
							catch (Exception ignore) {}
						} else Toast.makeText(context, "For some reason wallpapers are not supported.", Toast.LENGTH_LONG).show();
					} else {
						try { myWallpaperManager.setBitmap(c); }
						catch (Exception ignore) {}
					}
					context.startActivity(new Intent(context, Main.class)); }
				catch (NumberFormatException e) { Toast.makeText(context, "That's not a color.", Toast.LENGTH_SHORT).show(); }
			}
		});
		Objects.requireNonNull(d.findViewById(R.id.alpha)).setVisibility(View.GONE);
		final SeekBar red = d.findViewById(R.id.red);
		final SeekBar green = d.findViewById(R.id.green);
		final SeekBar blue = d.findViewById(R.id.blue);
		Objects.requireNonNull(red).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				int color = progress * 256 * 256 + Objects.requireNonNull(green).getProgress() * 256 + Objects.requireNonNull(blue).getProgress();
				StringBuilder hex = new StringBuilder(Integer.toHexString(color));
				while (hex.length() != 6) hex.insert(0, 0);
				txt.setText(hex.toString());
				txt.setTextColor(useDarkText(color) ? 0xff252627 : 0xffffffff);
			}
		});
		Objects.requireNonNull(green).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				int color = red.getProgress() * 256 * 256 + progress * 256 + Objects.requireNonNull(blue).getProgress();
				StringBuilder hex = new StringBuilder(Integer.toHexString(color));
				while (hex.length() != 6) hex.insert(0, 0);
				txt.setText(hex.toString());
				txt.setTextColor(useDarkText(color) ? 0xff252627 : 0xffffffff);
			}
		});
		Objects.requireNonNull(blue).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				int color = red.getProgress() * 256 * 256 + green.getProgress() * 256 + progress;
				StringBuilder hex = new StringBuilder(Integer.toHexString(color));
				while (hex.length() != 6) hex.insert(0, 0);
				txt.setText(hex.toString());
				txt.setTextColor(useDarkText(color) ? 0xff252627 : 0xffffffff);
			}
		});
		System.gc();
		d.show();
	}
}
