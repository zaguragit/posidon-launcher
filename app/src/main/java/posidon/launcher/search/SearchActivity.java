package posidon.launcher.search;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import posidon.launcher.BuildConfig;
import posidon.launcher.Main;
import posidon.launcher.R;
import posidon.launcher.items.App;
import posidon.launcher.tools.Loader;
import posidon.launcher.tools.Settings;
import posidon.launcher.tools.Tools;

public class SearchActivity extends AppCompatActivity {

	private final Map<String, Arithmetic> operators = new HashMap<>();

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_layout);
		Tools.applyFontSetting(SearchActivity.this);
		EditText searchTxt = findViewById(R.id.searchTxt);
		searchTxt.requestFocus();
		final GridView searchgrid = findViewById(R.id.searchgrid);
		search("", searchgrid);
		searchTxt.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) { search(s.toString(), searchgrid); }
		});
		searchTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
						(actionId == EditorInfo.IME_ACTION_DONE)) { onPause(); }
				return false;
			}
		});

		ShapeDrawable bg = new ShapeDrawable();
		float tr = Settings.getInt("searchradius", 0) * getResources().getDisplayMetrics().density;
		bg.setShape(new RoundRectShape(new float[]{tr, tr ,tr, tr, 0, 0, 0, 0}, null, null));
		bg.getPaint().setColor(Settings.getInt("searchcolor", 0x33000000));
		findViewById(R.id.searchbar).setBackground(bg);
		bg = new ShapeDrawable();
		bg.setShape(new RectShape());
		bg.getPaint().setColor(Settings.getInt("searchUiBg", 0x88000000));
		getWindow().setBackgroundDrawable(bg);
		searchTxt.setTextColor(Settings.getInt("searchtxtcolor", 0xFFFFFFFF));
		((TextView)findViewById(R.id.failtxt)).setTextColor(Settings.getInt("searchtxtcolor", 0xFFFFFFFF));
		searchTxt.setHintTextColor(Settings.getInt("searchhintcolor", 0xFFFFFFFF));
		searchTxt.setHint(Settings.getString("searchhinttxt", "Search.."));
		((ImageView)findViewById(R.id.searchIcon)).setImageTintList(ColorStateList.valueOf(Settings.getInt("searchhintcolor", 0xFFFFFFFF)));
		((ImageView)findViewById(R.id.kill)).setImageTintList(ColorStateList.valueOf(Settings.getInt("searchhintcolor", 0xFFFFFFFF)));
		findViewById(R.id.kill).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				startActivity(new Intent(SearchActivity.this, Main.class));
			}
		});
		operators.put("+", new Add());
		operators.put("plus", new Add());
		operators.put("-", new Subtract());
		operators.put("minus", new Subtract());
		operators.put("*", new Multiply());
		operators.put("x", new Multiply());
		operators.put("times", new Multiply());
		operators.put("/", new Divide());
		operators.put(":", new Divide());
		operators.put("over", new Divide());
		operators.put("&", new And());
		operators.put("and", new And());
		operators.put("|", new Or());
		operators.put("or", new Or());
	}

	boolean stillWantIP = false;
	private void search(String string, GridView grid) {
		stillWantIP = false;
		int j = 0;
		if (Main.apps != null) {
			boolean showHidden = cook(string).equals(cook("hidden")) || cook(string).equals(cook("hiddenapps"));
			if (showHidden) j++;
			for (App app : Main.apps) {
				for (String word : app.label.split(" "))
					if (cook(word).contains(cook(string)) || word.contains(string)) {
						j++;
						break;
					}
			}
			final App[] results = new App[j];
			if (j > 0) {
				findViewById(R.id.fail).setVisibility(View.GONE);
				j = 0;
				for (App app : Main.apps) {
					for (String word : app.label.split(" ")) {
						if (cook(word).contains(cook(string)) || word.contains(string)) {
							results[j] = app;
							j++;
							break;
						}
					}
				}
			}
			if (showHidden) {
				findViewById(R.id.fail).setVisibility(View.GONE);
				App app = new App();
				app.label = "Hidden apps";
				app.packageName = BuildConfig.APPLICATION_ID;
				app.name = HiddenAppsActivity.class.getName();
				app.icon = getDrawable(R.drawable.hidden_apps);
				results[j] = app;
				j++;
			}
			try {
				grid.setAdapter(new SearchAdapter(this, results));
				grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) { results[i].open(SearchActivity.this, view); }
				});
			} catch (Exception e) { e.printStackTrace(); }
		}

		try {
			String tmp = string.trim()
					.replace(" ", "")
					.replace("=", "")
					.replace("-+", "-")
					.replace("+-", "-")
					.replace("++", "+")
					.replace("--", "+");
			for (String op : operators.keySet()) tmp = tmp.replace(op, ' ' + op + ' ');
			if (tmp.charAt(1) == '-') {
				tmp = '-' + tmp.substring(3);
			}
			String[] math = tmp.toLowerCase().split(" ");
			double bufferNum = Double.valueOf(math[0]);
			for (int i = 0; i < math.length; i++) {
				try { Double.parseDouble(math[i]); } catch (Exception e) {
					bufferNum = operators.get(math[i]).apply(bufferNum, Double.valueOf(math[i+1]));
					findViewById(R.id.smartbox).setVisibility(View.VISIBLE);
					((TextView) findViewById(R.id.type)).setText(R.string.math_operation);
					((TextView) findViewById(R.id.result)).setText(tmp + " = " + bufferNum);
					findViewById(R.id.fail).setVisibility(View.GONE);
				}
			}
		} catch (Exception e) {
			if (j == 0) {
				findViewById(R.id.fail).setVisibility(View.VISIBLE);
				((TextView)findViewById(R.id.failtxt)).setText(getString(R.string.no_results_for, string));
			}
			if (string.toLowerCase().contains("ip")) {
				stillWantIP = true;
				findViewById(R.id.smartbox).setVisibility(View.VISIBLE);
				((TextView)findViewById(R.id.type)).setText(R.string.ip_address_external);
				((TextView)findViewById(R.id.result)).setText("");
				new Loader.text("https://checkip.amazonaws.com", new Loader.text.Listener() {
					@Override
					public void onFinished(String string) { if (stillWantIP) ((TextView)findViewById(R.id.result)).setText(string); }
				}).execute();
				findViewById(R.id.fail).setVisibility(View.GONE);
			} else if (string.toLowerCase().contains("pi") || string.toLowerCase().contains("π")) {
				findViewById(R.id.smartbox).setVisibility(View.VISIBLE);
				((TextView)findViewById(R.id.type)).setText(R.string.value_of_pi);
				((TextView)findViewById(R.id.result)).setText("\u03c0 = " + Math.PI);
				findViewById(R.id.fail).setVisibility(View.GONE);
			} else findViewById(R.id.smartbox).setVisibility(View.GONE);
		}
	}

	static abstract class Arithmetic { protected abstract double apply(double x, double y); }
	static class Add extends Arithmetic { public double apply(double x, double y) { return x+y; } }
	static class Subtract extends Arithmetic { public double apply(double x, double y) { return x-y; } }
	static class Multiply extends Arithmetic { public double apply(double x, double y) { return x*y; } }
	static class Divide extends Arithmetic { public double apply(double x, double y) { return x/y; } }
	static class And extends Arithmetic { public double apply(double x, double y) { return (int)x & (int)y; } }
	static class Or extends Arithmetic { public double apply(double x, double y) { return (int)x | (int)y; } }

	@Override
	protected void onPause() {
		super.onPause();
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		hideKeyboard(this);
		finish();
	}

	private String cook(String s) {
		return s.toLowerCase()
				.replace(",", "")
				.replace(".", "")
				.replace("ñ", "n")
				.replace("ck", "c")
				.replace("cc", "c")
				.replace("z", "s")
				.replace("wh", "w")
				.replace("ts", "s")
				.replace("tz", "s")
				.replace("gh", "g")
				.replace("-", "")
				.replace("_", "")
				.replace("/", "")
				.replace("&", "");
	}

	private static void hideKeyboard(Activity activity) {
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		//Find the currently focused view, so we can grab the correct window token from it.
		View view = activity.getCurrentFocus();
		//If no view currently has focus, create a new one, just so we can grab a window token from it
		if (view == null) view = new View(activity);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
}