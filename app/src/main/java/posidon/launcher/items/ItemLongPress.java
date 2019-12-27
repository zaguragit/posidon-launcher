package posidon.launcher.items;

import android.content.ClipData;
import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

import posidon.launcher.Main;
import posidon.launcher.R;
import posidon.launcher.tools.ColorTools;
import posidon.launcher.tools.Settings;
import posidon.launcher.tools.Tools;

import static androidx.appcompat.widget.ListPopupWindow.WRAP_CONTENT;

public class ItemLongPress {
	interface methods {
		void onRemove(View v);
		void onEdit(View v);
	}

	public static PopupWindow currentPopup = null;

	private static PopupWindow popupWindow(final Context context, final methods methods, boolean showRemove, LauncherItem item) {
		Tools.vibrate(context);

		final View content;
		int color, txtColor;

		if (item instanceof App) {
			final App app = (App) item;
			color = Palette.from(Tools.drawable2bitmap(app.icon)).generate().getDominantColor(0xFF252627);
			final float[] hsv = new float[3];
			Color.colorToHSV(color, hsv);
			if (hsv[1] < 0.2f) {
				color = Palette.from(Tools.drawable2bitmap(app.icon)).generate().getVibrantColor(0xFF252627);
				Color.colorToHSV(color, hsv);
			} txtColor = ColorTools.useDarkText(color) ? 0xFF111213 : 0xFFFFFFFF;

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && !app.getShortcuts(context).isEmpty()) {
				List<ShortcutInfo> shortcuts = app.getShortcuts(context);
				content = LayoutInflater.from(context).inflate(R.layout.app_long_press_menu_w_shortcuts, null);
				RecyclerView recyclerView = content.findViewById(R.id.shortcuts);
				recyclerView.setNestedScrollingEnabled(false);
				recyclerView.setLayoutManager(new LinearLayoutManager(context));
				recyclerView.setAdapter(new ShortcutAdapter(context, shortcuts, txtColor));
				ShapeDrawable bg = new ShapeDrawable();
				float r = 18 * context.getResources().getDisplayMetrics().density;
				bg.setShape(new RoundRectShape(new float[]{r, r, r, r, 0, 0, 0, 0}, null, null));
				bg.getPaint().setColor(0x33000000);
				recyclerView.setBackground(bg);
			} else content = LayoutInflater.from(context).inflate(R.layout.app_long_press_menu, null);

			final PopupWindow window = new PopupWindow(content, WRAP_CONTENT, WRAP_CONTENT, true);
			currentPopup = window;
			window.setOnDismissListener(new PopupWindow.OnDismissListener() {
				@Override
				public void onDismiss() {
					currentPopup = null;
				}
			});
			window.setBackgroundDrawable(new ColorDrawable(0x0));

			View appinfobtn = content.findViewById(R.id.appinfobtn);
			appinfobtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					window.dismiss();
					app.showProperties(context, Color.HSVToColor(new float[]{hsv[0], hsv[1], (hsv[2] > 0.5f) ? 0.5f : hsv[2]}));
				}
			});


			TextView title = content.findViewById(R.id.title);
			View removeBtn = content.findViewById(R.id.removeBtn);
			View editBtn = content.findViewById(R.id.editbtn);

			ShapeDrawable bg = new ShapeDrawable();
			float r = 18 * context.getResources().getDisplayMetrics().density;
			bg.setShape(new RoundRectShape(new float[]{r, r, r, r, r, r, r, r}, null, null));
			bg.getPaint().setColor(color);
			content.findViewById(R.id.bg).setBackground(bg);

			title.setText(item.label);
			title.setTextColor(txtColor);

			if (removeBtn instanceof TextView) {
				((TextView) removeBtn).setTextColor(txtColor);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
					((TextView) removeBtn).setCompoundDrawableTintList(ColorStateList.valueOf(txtColor));
				((TextView) appinfobtn).setTextColor(txtColor);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
					((TextView) appinfobtn).setCompoundDrawableTintList(ColorStateList.valueOf(txtColor));
				((TextView) editBtn).setTextColor(txtColor);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
					((TextView) editBtn).setCompoundDrawableTintList(ColorStateList.valueOf(txtColor));
			} else {
				((ImageView) removeBtn).setImageTintList(ColorStateList.valueOf(txtColor));
				((ImageView) appinfobtn).setImageTintList(ColorStateList.valueOf(txtColor));
				((ImageView) editBtn).setImageTintList(ColorStateList.valueOf(txtColor));
			}

			if (!showRemove) removeBtn.setVisibility(View.GONE);
			else {
				removeBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						window.dismiss();
						methods.onRemove(v);
					}
				});
			}

			editBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					window.dismiss();
					methods.onEdit(v);
				}
			});
			return window;
		} else {
			color = 0xFF252627;
			txtColor = 0xFFFFFFFF;

			content = LayoutInflater.from(context).inflate(R.layout.app_long_press_menu, null);
			final PopupWindow window = new PopupWindow(content, WRAP_CONTENT, WRAP_CONTENT, true);
			window.setBackgroundDrawable(new ColorDrawable(0x0));

			content.findViewById(R.id.appinfobtn).setVisibility(View.GONE);
			content.findViewById(R.id.title).setVisibility(View.GONE);

			View removeBtn = content.findViewById(R.id.removeBtn);
			View editbtn = content.findViewById(R.id.editbtn);

			ShapeDrawable bg = new ShapeDrawable();
			float r = 18 * context.getResources().getDisplayMetrics().density;
			bg.setShape(new RoundRectShape(new float[]{r, r, r, r, r, r, r, r}, null, null));
			bg.getPaint().setColor(color);
			content.findViewById(R.id.bg).setBackground(bg);

			((TextView) removeBtn).setTextColor(txtColor);
			((TextView) editbtn).setTextColor(txtColor);

			removeBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					window.dismiss();
					methods.onRemove(v);
				}
			});

			editbtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					window.dismiss();
					methods.onEdit(v);
				}
			});
			return window;
		}
	}




	public static View.OnLongClickListener dock(final Context context, final App app, final int i) {
		return new View.OnLongClickListener() {
			@Override public boolean onLongClick(View view) {
				if (currentPopup == null) {
					int[] location = new int[2];
					final View icon = view.findViewById(R.id.iconimg);
					icon.getLocationInWindow(location);
					int gravity = (location[0] > Tools.getDisplayWidth(context) / 2) ? Gravity.END : Gravity.START;
					int x = (location[0] > Tools.getDisplayWidth(context) / 2) ? Tools.getDisplayWidth(context) - location[0] - view.getMeasuredWidth() : location[0];
					final PopupWindow popupWindow = popupWindow(context, new methods() {
						@Override public void onRemove(View v) {
							String[] data = Settings.getString("dock", "").split("\n");
							if (data.length <= i) data = Arrays.copyOf(data, i + 1);
							data[i] = "";
							Settings.putString("dock", TextUtils.join("\n", data));
							Main.methods.setDock();
						}
						@Override public void onEdit(View v) {
							final View editContent = LayoutInflater.from(context).inflate(R.layout.app_edit_menu, null);
							final PopupWindow editWindow = new PopupWindow(editContent, WRAP_CONTENT, WRAP_CONTENT, true);
							final EditText editLabel = editContent.findViewById(R.id.editlabel);
							editLabel.setText(Settings.getString(app.packageName + "/" + app.name + "?label", app.label));
							editWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
								@Override
								public void onDismiss() {
									Settings.putString(app.packageName + "/" + app.name + "?label", editLabel.getText().toString().replace('\n', ' ').replace('¬', ' '));
									Main.shouldSetApps = true;
									Main.methods.setDock();
								}
							});
							editWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
						}
					}, true, app);
					popupWindow.setFocusable(false);
					View.DragShadowBuilder myShadow = new View.DragShadowBuilder(icon);
					ClipData clipData = ClipData.newPlainText("", "");
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
						icon.startDragAndDrop(clipData, myShadow, new Object[]{app, view, popupWindow}, 0);
					else icon.startDrag(clipData, myShadow, new Object[]{app, view, popupWindow}, 0);
					String[] data = Settings.getString("dock", "").split("\n");
					if (data.length <= i) data = Arrays.copyOf(data, i + 1);
					data[i] = "";
					Settings.putString("dock", TextUtils.join("\n", data));
					popupWindow.showAtLocation(
							view, Gravity.BOTTOM | gravity, x,
							(int)(-view.getY() + view.getHeight() * Settings.getInt("dockRowCount", 1) + Tools.navbarHeight + (Settings.getInt("dockbottompadding", 10) + 12) * context.getResources().getDisplayMetrics().density)
					);
				} return true;
			}
		};
	}

	public static View.OnLongClickListener insideFolder(final Context context, final App app, final int i, final View v, final int folderIndex, final PopupWindow folderWindow) {
		return new View.OnLongClickListener() {
			@Override public boolean onLongClick(View view) {
				if (currentPopup == null) {
					int[] location = new int[2];
					view.getLocationOnScreen(location);
					int gravity = (location[0] > Tools.getDisplayWidth(context) / 2) ? Gravity.END : Gravity.START;
					int x = (location[0] > Tools.getDisplayWidth(context) / 2) ? Tools.getDisplayWidth(context) - location[0] - view.getMeasuredWidth() : location[0];
					popupWindow(context, new methods() {
						@Override public void onRemove(View v) {
							folderWindow.dismiss();
							String[] data = Settings.getString("dock", "").split("\n");
							if (data.length <= i) data = Arrays.copyOf(data, i + 1);
							Folder f = new Folder(context, data[i]);
							f.apps.remove(folderIndex);
							data[i] = (f.apps.size() == 1) ? f.apps.get(0).packageName + "/" + f.apps.get(0).name : f.toString();
							Settings.putString("dock", TextUtils.join("\n", data));
							Main.methods.setDock();
						}
						@Override public void onEdit(View v) {
							final View editContent = LayoutInflater.from(context).inflate(R.layout.app_edit_menu, null);
							final PopupWindow editWindow = new PopupWindow(editContent, WRAP_CONTENT, WRAP_CONTENT, true);
							final EditText editLabel = editContent.findViewById(R.id.editlabel);
							editLabel.setText(Settings.getString(app.packageName + "/" + app.name + "?label", app.label));
							editWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
								@Override
								public void onDismiss() {
									Settings.putString(app.packageName + "/" + app.name + "?label", editLabel.getText().toString().replace('\n', ' ').replace('¬', ' '));
									Main.shouldSetApps = true;
									Main.methods.setDock();
								}
							});
							editWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
						}
					}, true, app).showAtLocation(v, Gravity.BOTTOM | gravity, x, Tools.getDisplayHeight(context) - location[1] + Tools.navbarHeight);
				} return true;
			}
		};
	}

	public static AdapterView.OnItemLongClickListener drawer(final Context context) {
		return new AdapterView.OnItemLongClickListener() {
			@Override public boolean onItemLongClick(AdapterView<?> adapterView, final View view, int position, long id) { if (currentPopup == null) try {
				final App app = Main.apps[position];
				final View icon = view.findViewById(R.id.iconimg);
				final int[] location = new int[2];
				final PopupWindow popupWindow = popupWindow(context, new methods() {
					@Override public void onRemove(View v) {}
					@Override public void onEdit(View v) {
						final View editContent = LayoutInflater.from(context).inflate(R.layout.app_edit_menu, null);
						final PopupWindow editWindow = new PopupWindow(editContent, WRAP_CONTENT, WRAP_CONTENT, true);
						final EditText editLabel = editContent.findViewById(R.id.editlabel);
						editLabel.setText(Settings.getString(app.packageName + "/" + app.name + "?label", app.label));
						editWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
							@Override
							public void onDismiss() {
								Settings.putString(app.packageName + "/" + app.name + "?label", editLabel.getText().toString().replace('\n', ' ').replace('¬', ' '));
								Main.shouldSetApps = true;
								Main.methods.setDock();
							}
						});
						editWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
					}
				}, false, app);
				popupWindow.setFocusable(false);
				icon.getLocationInWindow(location);
				View.DragShadowBuilder myShadow = new View.DragShadowBuilder(icon);
				ClipData data = ClipData.newPlainText("", "");
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
					icon.startDragAndDrop(data, myShadow, new Object[]{app, view, popupWindow}, 0);
				else icon.startDrag(data, myShadow, new Object[]{app, view, popupWindow}, 0);
				int gravity = (location[0] > Tools.getDisplayWidth(context) / 2) ? Gravity.END : Gravity.START;
				int x = (location[0] > Tools.getDisplayWidth(context) / 2) ? Tools.getDisplayWidth(context) - location[0] - icon.getMeasuredWidth() : location[0];
				if (location[1] < Tools.getDisplayHeight(context) / 2f) popupWindow.showAtLocation(icon, Gravity.TOP | gravity, x, location[1] + icon.getMeasuredHeight());
				else popupWindow.showAtLocation(
						icon, Gravity.BOTTOM | gravity, x,
						context.getResources().getDisplayMetrics().heightPixels - location[1] + (int)(4 * context.getResources().getDisplayMetrics().density)  + Tools.navbarHeight
				);
			} catch (Exception ignore) {} return true; }
		};
	}

	public static View.OnLongClickListener folder(final Context context, final Folder folder, final int i) {
		return new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				if (currentPopup == null) {
					int[] location = new int[2];
					final View icon = view.findViewById(R.id.iconimg);
					icon.getLocationInWindow(location);
					int gravity = (location[0] > Tools.getDisplayWidth(context) / 2) ? Gravity.END : Gravity.START;
					int x = (location[0] > Tools.getDisplayWidth(context) / 2) ? Tools.getDisplayWidth(context) - location[0] - view.getMeasuredWidth() : location[0];
					final PopupWindow popupWindow = popupWindow(context, new methods() {
						@Override public void onRemove(View v) {
							String[] data = Settings.getString("dock", "").split("\n");
							if (data.length <= i) data = Arrays.copyOf(data, i + 1);
							data[i] = "";
							Settings.putString("dock", TextUtils.join("\n", data));
							Main.methods.setDock();
						}

						@Override public void onEdit(View v) {
							final View editContent = LayoutInflater.from(context).inflate(R.layout.app_edit_menu, null);
							final PopupWindow editWindow = new PopupWindow(editContent, WRAP_CONTENT, WRAP_CONTENT, true);
							final EditText editLabel = editContent.findViewById(R.id.editlabel);
							editLabel.setText(folder.label);
							editWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
								@Override
								public void onDismiss() {
									folder.label = editLabel.getText().toString().replace('\n', ' ').replace('¬', ' ');
									String[] data = Settings.getString("dock", "").split("\n");
									data[i] = folder.toString();
									Settings.putString("dock", TextUtils.join("\n", data));
									Main.methods.setDock();
								}
							});
							editWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
						}
					}, true, folder);

					View.DragShadowBuilder myShadow = new View.DragShadowBuilder(icon);
					ClipData clipData = ClipData.newPlainText("", "");
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
						icon.startDragAndDrop(clipData, myShadow, new Object[]{folder, view, popupWindow}, 0);
					else icon.startDrag(clipData, myShadow, new Object[]{folder, view, popupWindow}, 0);
					String[] data = Settings.getString("dock", "").split("\n");
					if (data.length <= i) data = Arrays.copyOf(data, i + 1);
					data[i] = "";
					Settings.putString("dock", TextUtils.join("\n", data));

					popupWindow.showAtLocation(
							view, Gravity.BOTTOM | gravity, x,
							(int)(-view.getY() + view.getHeight() * Settings.getInt("dockRowCount", 1) + Tools.navbarHeight + (Settings.getInt("dockbottompadding", 10) + 12) * context.getResources().getDisplayMetrics().density)
					);
				} return true;
			}
		};
	}
}
