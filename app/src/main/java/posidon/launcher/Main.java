package posidon.launcher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import posidon.launcher.feed.news.FeedAdapter;
import posidon.launcher.feed.news.FeedItem;
import posidon.launcher.feed.news.FeedLoader;
import posidon.launcher.feed.notifications.NotificationAdapter;
import posidon.launcher.feed.notifications.NotificationService;
import posidon.launcher.feed.notifications.SwipeToDeleteCallback;
import posidon.launcher.items.App;
import posidon.launcher.items.DrawerAdapter;
import posidon.launcher.items.Folder;
import posidon.launcher.items.ItemLongPress;
import posidon.launcher.items.LauncherItem;
import posidon.launcher.items.Shortcut;
import posidon.launcher.search.SearchActivity;
import posidon.launcher.tools.ColorTools;
import posidon.launcher.tools.Settings;
import posidon.launcher.tools.Sort;
import posidon.launcher.tools.ThemeTools;
import posidon.launcher.tools.Tools;
import posidon.launcher.tutorial.WelcomeActivity;
import posidon.launcher.view.ResizableLayout;
import static android.widget.ListPopupWindow.WRAP_CONTENT;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN;

public class Main extends AppCompatActivity {

	public static final int REQUEST_PICK_APPWIDGET = 0;
	public static final int REQUEST_CREATE_APPWIDGET = 1;
	public static final int REQUEST_BIND_WIDGET = 2;

	public static boolean shouldSetApps = false;
	public static boolean customized = false;

	public static App[] apps;
	public static int accentColor = 0xff1155ff;
	public static AppChangeReceiver receiver;
	public static Methods methods;

	@RequiresApi(api = Build.VERSION_CODES.M)
	public static LauncherApps launcherApps;

	private GridView drawerGrid;
	private View searchBar;
	private PowerManager powerManager;
	private NestedScrollView desktop;
	private RecyclerView feedRecycler;
	private RecyclerView notifications;
	private LayerDrawable blurBg;
	private AppWidgetManager widgetManager;
	private AppWidgetHost widgetHost;
	private AppWidgetHostView hostView;
	private ResizableLayout widgetLayout;
	private int dockHeight;
	private BottomSheetBehavior behavior;

	private ProgressBar batteryBar;
	private final BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
		@Override public void onReceive(Context context, Intent intent) {
			batteryBar.setProgress(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0));
		}
	};


	@SuppressLint("ClickableViewAccessibility") @Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Settings.init(this);
		if (Settings.getBool("init", true)) {
			startActivity(new Intent(this, WelcomeActivity.class));
			finish();
		}
		accentColor = Settings.getInt("accent", 0x1155ff) | 0xff000000;
		setContentView(R.layout.main);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
			launcherApps = getSystemService(LauncherApps.class);

		batteryBar = findViewById(R.id.battery);
		registerReceiver(batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		methods = new Methods() {
			@Override public void setDock() {
				final BottomSheetBehavior behavior = BottomSheetBehavior.from(findViewById(R.id.drawer));
				int appSize = 0;
				switch (Settings.getInt("dockicsize", 1)) {
					case 0: appSize = (int) (getResources().getDisplayMetrics().density * 64); break;
					case 1: appSize = (int) (getResources().getDisplayMetrics().density * 74); break;
					case 2: appSize = (int) (getResources().getDisplayMetrics().density * 84); break;
				}

				final String[] data = Settings.getString("dock", "").split("\n");
				final GridLayout container = findViewById(R.id.dockContainer);
				container.removeAllViews();
				int columnCount = Settings.getInt("dockIconCount", 5);
				int rowCount = Settings.getInt("dockRowCount", 1);
				boolean showLabels = Settings.getBool("dockLabelsEnabled", false);
				container.setColumnCount(columnCount);
				container.setRowCount(rowCount);
				appSize = Math.min(appSize, (int)((Tools.getDisplayWidth(Main.this) - 32 * getResources().getDisplayMetrics().density) / columnCount));
				int i = 0;
				for (; i < data.length && i < columnCount * rowCount; i++) {
					String string = data[i];
					final View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.drawer_item, null);
					ImageView img = view.findViewById(R.id.iconimg);
					img.getLayoutParams().height = appSize;
					img.getLayoutParams().width = appSize;
					if (data[i].startsWith("folder(") && data[i].endsWith(")")) {
						final Folder folder = new Folder(Main.this, data[i]);
						img.setImageDrawable(folder.icon);
						if (showLabels) {
							((TextView) view.findViewById(R.id.icontxt)).setText(folder.label);
							((TextView) view.findViewById(R.id.icontxt)).setTextColor(Settings.getInt("dockLabelColor", -0x11111112));
						}
						else view.findViewById(R.id.icontxt).setVisibility(View.GONE);
						final int finalI = i, finalAppSize = appSize, bgColor = Settings.getInt("folderBG", 0xdd111213);
						final float r = Settings.getInt("folderCornerRadius", 18) * getResources().getDisplayMetrics().density;
						final boolean labelsEnabled = Settings.getBool("folderLabelsEnabled", false);
						view.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								View content = LayoutInflater.from(Main.this).inflate(R.layout.folder_layout, null);
								final PopupWindow popupWindow = new PopupWindow(content, WRAP_CONTENT, WRAP_CONTENT, true);
								popupWindow.setBackgroundDrawable(new ColorDrawable(0x0));
								GridLayout container = content.findViewById(R.id.container);
								container.setColumnCount(Settings.getInt("folderColumns", 3));
								List<App> appList = folder.apps;
								for (int i1 = 0, appListSize = appList.size(); i1 < appListSize; i1++) {
									final App app = appList.get(i1);
									if (app == null) {
										folder.apps.remove(i1);
										data[finalI] = folder.toString();
										Settings.putString("dock", TextUtils.join("\n", data));
									} else {
										View appIcon = LayoutInflater.from(getApplicationContext()).inflate(R.layout.drawer_item, null);
										final ImageView icon = appIcon.findViewById(R.id.iconimg);
										icon.getLayoutParams().height = finalAppSize;
										icon.getLayoutParams().width = finalAppSize;
										icon.setImageDrawable(app.icon);
										if (labelsEnabled) {
											TextView iconTxt = appIcon.findViewById(R.id.icontxt);
											iconTxt.setText(app.label);
											iconTxt.setTextColor(Settings.getInt("folder:label_color", 0xddffffff));
										} else appIcon.findViewById(R.id.icontxt).setVisibility(View.GONE);
										appIcon.setOnClickListener(new View.OnClickListener() {
											@Override
											public void onClick(View view) {
												app.open(Main.this, view);
												popupWindow.dismiss();
											}
										});
										appIcon.setOnLongClickListener(ItemLongPress.insideFolder(Main.this, app, finalI, view, i1, popupWindow));
										container.addView(appIcon);
									}
								}

								ShapeDrawable bg = new ShapeDrawable();
								bg.setShape(new RoundRectShape(new float[]{r, r, r, r, r, r, r, r}, null, null));
								bg.getPaint().setColor(bgColor);
								content.findViewById(R.id.bg).setBackground(bg);

								int[] location = new int[2];
								view.getLocationInWindow(location);
								int gravity = (location[0] > Tools.getDisplayWidth(Main.this) / 2) ? Gravity.END : Gravity.START;
								int x = (location[0] > Tools.getDisplayWidth(Main.this) / 2) ? Tools.getDisplayWidth(Main.this) - location[0] - view.getMeasuredWidth() : location[0];
								popupWindow.showAtLocation(
										view, Gravity.BOTTOM | gravity, x,
										(int)(-view.getY() + view.getHeight() * Settings.getInt("dockRowCount", 1) + Tools.navbarHeight + (Settings.getInt("dockbottompadding", 10) + 12) * getResources().getDisplayMetrics().density)
								);
							}
						});
						view.setOnLongClickListener(ItemLongPress.folder(Main.this, folder, i));
					} else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && data[i].startsWith("shortcut:")) {
						final Shortcut shortcut = new Shortcut(string);
						if (!showLabels) view.findViewById(R.id.icontxt).setVisibility(View.GONE);
						if (Tools.isInstalled(shortcut.getPackageName(), getPackageManager())) {
							if (showLabels) {
								((TextView) view.findViewById(R.id.icontxt)).setText(shortcut.label);
								((TextView) view.findViewById(R.id.icontxt)).setTextColor(Settings.getInt("dockLabelColor", -0x11111112));
							}
							img.setImageDrawable(shortcut.icon);
							view.setOnClickListener(new View.OnClickListener() {
								@Override public void onClick(View view) {
									shortcut.open(Main.this, view);
								}
							});
							//view.setOnLongClickListener(ItemLongPress.dock(Main.this, app, i));
						} else {
							data[i] = "";
							Settings.putString("dock", TextUtils.join("\n", data));
						}
					} else {
						final App app = App.get(string);
                        if (!showLabels) view.findViewById(R.id.icontxt).setVisibility(View.GONE);
						if (app == null) {
							data[i] = "";
							Settings.putString("dock", TextUtils.join("\n", data));
						} else {
							if (showLabels) {
								((TextView) view.findViewById(R.id.icontxt)).setText(app.label);
								((TextView) view.findViewById(R.id.icontxt)).setTextColor(Settings.getInt("dockLabelColor", -0x11111112));
							}
							img.setImageDrawable(app.icon);
							view.setOnClickListener(new View.OnClickListener() {
								@Override public void onClick(View view) {
									app.open(Main.this, view);
								}
							});
							view.setOnLongClickListener(ItemLongPress.dock(Main.this, app, i));
						}
					}
					container.addView(view);
				}

				for (; i < columnCount * rowCount; i++) {
					final View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.drawer_item, null);
					ImageView img = view.findViewById(R.id.iconimg);
					img.getLayoutParams().height = appSize;
					img.getLayoutParams().width = appSize;
					if (!showLabels) view.findViewById(R.id.icontxt).setVisibility(View.GONE);
					container.addView(view);
				}

				int containerHeight = (int)(appSize * rowCount + getResources().getDisplayMetrics().density * (Settings.getBool("dockLabelsEnabled", false) ? 18 * rowCount : 0));
				if (Settings.getBool("docksearchbarenabled", false) && !Tools.isTablet(Main.this)) dockHeight = (int)(containerHeight + getResources().getDisplayMetrics().density * 84);
				else dockHeight = (int)(containerHeight + getResources().getDisplayMetrics().density * 14);
				container.getLayoutParams().height = containerHeight;
				behavior.setPeekHeight((int)(dockHeight + Tools.navbarHeight + Settings.getInt("dockbottompadding", 10) * getResources().getDisplayMetrics().density));
				DisplayMetrics metrics = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
				findViewById(R.id.drawercontent).getLayoutParams().height = metrics.heightPixels;
				((FrameLayout.LayoutParams) findViewById(R.id.homeView).getLayoutParams()).topMargin = -dockHeight;
				if (Settings.getBool("feed:show_behind_dock", false)) {
					((CoordinatorLayout.LayoutParams) desktop.getLayoutParams()).setMargins(0, dockHeight, 0, 0);
					findViewById(R.id.desktopContent).setPadding(0, 0, 0, (int) (dockHeight + Tools.navbarHeight + Settings.getInt("dockbottompadding", 10) * getResources().getDisplayMetrics().density));

				} else {
					((CoordinatorLayout.LayoutParams) desktop.getLayoutParams()).setMargins(0, dockHeight, 0, (int) (dockHeight + Tools.navbarHeight + (Settings.getInt("dockbottompadding", 10) - 18) * getResources().getDisplayMetrics().density));
					findViewById(R.id.desktopContent).setPadding(0, (int)(6 * getResources().getDisplayMetrics().density), 0, (int)(24 * getResources().getDisplayMetrics().density));
				}
				//desktop.setPadding(0, dockHeight, 0, (int) (dockHeight + Tools.navbarHeight + (Settings.getInt("dockbottompadding", 10) - 18) * getResources().getDisplayMetrics().density));
				((CoordinatorLayout.LayoutParams) findViewById(R.id.blur).getLayoutParams()).topMargin = dockHeight;


				getWindow().getDecorView().findViewById(android.R.id.content).setOnDragListener(new View.OnDragListener() {
					@Override
					public boolean onDrag(View v, DragEvent event) {
						switch (event.getAction()) {
							case DragEvent.ACTION_DRAG_LOCATION:
								Object[] objs = (Object[]) event.getLocalState();
								View icon = (View) objs[1];
								int[] location = new int[2];
								icon.getLocationOnScreen(location);
								float x = Math.abs(event.getX() - location[0] - icon.getWidth()/2f), y = Math.abs(event.getY() - location[1] - icon.getHeight()/2f);
								if (x > icon.getWidth()/2f || y > icon.getHeight()/2f) {
									((PopupWindow)objs[2]).dismiss();
									behavior.setState(STATE_COLLAPSED);
								}
								break;
							case DragEvent.ACTION_DRAG_STARTED:
								((View)((Object[]) event.getLocalState())[1]).setVisibility(View.INVISIBLE);
								break;
							case DragEvent.ACTION_DRAG_ENDED:
								objs = (Object[]) event.getLocalState();
								((View)objs[1]).setVisibility(View.VISIBLE);
								((PopupWindow)objs[2]).setFocusable(true);
								((PopupWindow)objs[2]).update();
								break;
							case DragEvent.ACTION_DROP:
								((View)((Object[]) event.getLocalState())[1]).setVisibility(View.VISIBLE);
								if (behavior.getState() != STATE_EXPANDED) {
									if (event.getY() > Tools.getDisplayHeight(Main.this) - dockHeight) {
										LauncherItem item = (LauncherItem) ((Object[]) event.getLocalState())[0];
										if (item instanceof App) {
											App app = (App) item;
											location = new int[2];
											for (int i = 0; i < container.getChildCount(); i++) {
												container.getChildAt(i).getLocationOnScreen(location);
												float threshHold = Math.min(container.getChildAt(i).getHeight() / 3, 100 * getResources().getDisplayMetrics().density);
												if (Math.abs(location[0] - (event.getX() - container.getChildAt(i).getHeight() / 2f)) < threshHold && Math.abs(location[1] - (event.getY() - container.getChildAt(i).getHeight() / 2f)) < threshHold) {
													String[] data = Settings.getString("dock", "").split("\n");
													if (data.length <= i)
														data = Arrays.copyOf(data, i + 1);
													if (data[i] == null || data[i].equals("") || data[i].equals("null")) {
														data[i] = app.packageName + "/" + app.name;
														Settings.putString("dock", TextUtils.join("\n", data));
													} else {
														if (data[i].startsWith("folder(") && data[i].endsWith(")"))
															data[i] = "folder(" + data[i].substring(7, data[i].length() - 1) + "¬" + app.packageName + "/" + app.name + ")";
														else data[i] = "folder(" + "folder¬" + data[i] + "¬" + app.packageName + "/" + app.name + ")";
														Settings.putString("dock", TextUtils.join("\n", data));
													}
													break;
												}
											}
										} else if (item instanceof Folder) {
											Folder folder = (Folder) item;
											location = new int[2];
											for (int i = 0; i < container.getChildCount(); i++) {
												container.getChildAt(i).getLocationOnScreen(location);
												float threshHold = Math.min(container.getChildAt(i).getHeight() / 3, 100 * getResources().getDisplayMetrics().density);
												if (Math.abs(location[0] - (event.getX() - container.getChildAt(i).getHeight() / 2f)) < threshHold && Math.abs(location[1] - (event.getY() - container.getChildAt(i).getHeight() / 2f)) < threshHold) {
													String[] data = Settings.getString("dock", "").split("\n");
													if (data.length <= i)
														data = Arrays.copyOf(data, i + 1);
													if (data[i] == null || data[i].equals("") || data[i].equals("null")) {
														data[i] = folder.toString();
														Settings.putString("dock", TextUtils.join("\n", data));
													} else {
														String folderContent = folder.toString().substring(7, folder.toString().length() - 1);
														if (data[i].startsWith("folder(") && data[i].endsWith(")")) {
															folderContent = folderContent.substring(folderContent.indexOf('¬') + 1);
															data[i] = "folder(" + data[i].substring(7, data[i].length() - 1) + "¬" + folderContent + ")";
														} else data[i] = "folder(" + folderContent + "¬" + data[i] + ")";
														Settings.putString("dock", TextUtils.join("\n", data));
													}
													break;
												}
											}
										}
									}
									setDock();
								}
								break;
						}
						return true;
					}
				});
			}
			@Override public void setCustomizations() {
				Tools.applyFontSetting(Main.this);

				if (Settings.getBool("hidestatus", false)) getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

				ShapeDrawable bg = new ShapeDrawable();
				float tr = Settings.getInt("dockradius", 30) * getResources().getDisplayMetrics().density;
				bg.setShape(new RoundRectShape(new float[]{tr, tr ,tr, tr, 0, 0, 0, 0}, null, null));
				bg.getPaint().setColor(Settings.getInt("dockcolor", 0x88000000));
				findViewById(R.id.drawer).setBackground(bg);

				if (Settings.getBool("drawersearchbarenabled", true)) {
					drawerGrid.setPadding(0, Tools.getStatusBarHeight(Main.this), 0, Tools.navbarHeight + (int)(56 * getResources().getDisplayMetrics().density));
					searchBar.setPadding(0, 0, 0, Tools.navbarHeight);
					searchBar.setVisibility(View.VISIBLE);
					bg = new ShapeDrawable();
					tr = Settings.getInt("searchradius", 0) * getResources().getDisplayMetrics().density;
					bg.setShape(new RoundRectShape(new float[]{tr, tr ,tr, tr, 0, 0, 0, 0}, null, null));
					bg.getPaint().setColor(Settings.getInt("searchcolor", 0x33000000));
					searchBar.setBackground(bg);
					TextView t = findViewById(R.id.searchTxt);
					t.setTextColor(Settings.getInt("searchhintcolor", 0xFFFFFFFF));
					t.setText(Settings.getString("searchhinttxt", "Search.."));
					((ImageView)findViewById(R.id.searchIcon)).setImageTintList(new ColorStateList(new int[][]{new int[]{0}}, new int[]{Settings.getInt("searchhintcolor", 0xFFFFFFFF)}));
				} else {
					searchBar.setVisibility(View.GONE);
					drawerGrid.setPadding(0, Tools.getStatusBarHeight(Main.this), 0, Tools.navbarHeight + (int)(12 * getResources().getDisplayMetrics().density));
				}

				if (Settings.getBool("docksearchbarenabled", false)) {
					findViewById(R.id.docksearchbar).setVisibility(View.VISIBLE);
					findViewById(R.id.battery).setVisibility(View.VISIBLE);
					bg = new ShapeDrawable();
					tr = Settings.getInt("docksearchradius", 30) * getResources().getDisplayMetrics().density;
					bg.setShape(new RoundRectShape(new float[]{tr, tr ,tr, tr, tr, tr, tr, tr}, null, null));
					bg.getPaint().setColor(Settings.getInt("docksearchcolor", 0xDDFFFFFF));
					findViewById(R.id.docksearchbar).setBackground(bg);
					TextView t = findViewById(R.id.docksearchtxt);
					t.setTextColor(Settings.getInt("docksearchtxtcolor", 0xff000000));
					t.setText(Settings.getString("searchhinttxt", "Search.."));
					((ImageView)findViewById(R.id.docksearchic)).setImageTintList(new ColorStateList(new int[][]{new int[]{0}}, new int[]{Settings.getInt("docksearchtxtcolor", 0xff000000)}));
					((ImageView)findViewById(R.id.docksearchic)).setImageTintMode(PorterDuff.Mode.MULTIPLY);
					((ProgressBar)findViewById(R.id.battery)).setProgressTintList(new ColorStateList(new int[][]{new int[]{0}}, new int[]{Settings.getInt("docksearchtxtcolor", 0xff000000)}));
					((ProgressBar)findViewById(R.id.battery)).setIndeterminateTintMode(PorterDuff.Mode.MULTIPLY);
					((ProgressBar)findViewById(R.id.battery)).setProgressBackgroundTintList(new ColorStateList(new int[][]{new int[]{0}}, new int[]{Settings.getInt("docksearchtxtcolor", 0xff000000)}));
					((ProgressBar)findViewById(R.id.battery)).setProgressBackgroundTintMode(PorterDuff.Mode.MULTIPLY);
					((LayerDrawable)((ProgressBar)findViewById(R.id.battery)).getProgressDrawable()).getDrawable(3).setTint(ColorTools.useDarkText(Settings.getInt("docksearchtxtcolor", 0xff000000)) ? 0xdd000000 : 0xeeffffff);
				} else {
					findViewById(R.id.docksearchbar).setVisibility(View.GONE);
					findViewById(R.id.battery).setVisibility(View.GONE);
				}

				drawerGrid.setNumColumns(Settings.getInt("numcolumns", 4));
				drawerGrid.setVerticalSpacing((int)(getResources().getDisplayMetrics().density * Settings.getInt("verticalspacing", 12)));
				feedRecycler.setVisibility(Settings.getBool("feedenabled", true) ? View.VISIBLE : View.GONE);
				int marginX = (int)(Settings.getInt("feed:card_margin_x", 16) * getResources().getDisplayMetrics().density);
				((LinearLayout.LayoutParams)feedRecycler.getLayoutParams()).setMargins(marginX, 0, marginX, 0);
				((LinearLayout.LayoutParams)findViewById(R.id.parentNotification).getLayoutParams()).leftMargin = marginX;
				((LinearLayout.LayoutParams)findViewById(R.id.parentNotification).getLayoutParams()).rightMargin = marginX;
				if (Settings.getBool("hidefeed", false)) {
					feedRecycler.setTranslationX(findViewById(R.id.homeView).getWidth());
					feedRecycler.setAlpha(0);
					desktop.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
						@Override
						public void onScrollChange(NestedScrollView v, int x, int y, int oldX, int oldY) {
							float a = 6 * getResources().getDisplayMetrics().density;
							if (y > a) {
								feedRecycler.setTranslationX(0);
								feedRecycler.setAlpha(1);
							} else if (y < a && oldY >= a) {
								feedRecycler.setTranslationX(findViewById(R.id.homeView).getWidth());
								feedRecycler.setAlpha(0);
							}
							if (!LauncherMenu.isActive) {
								if (y + desktop.getHeight() < findViewById(R.id.desktopContent).getHeight() - dockHeight) {
									int distance = oldY - y;
									if ((y < a || distance > a) && behavior.getState() == STATE_HIDDEN) {
										behavior.setState(STATE_COLLAPSED);
										behavior.setHideable(false);
									} else if (distance < -a && behavior.getState() == STATE_COLLAPSED) {
										behavior.setHideable(true);
										behavior.setState(STATE_HIDDEN);
									}
								} else if (behavior.getState() == STATE_HIDDEN) {
									behavior.setState(STATE_COLLAPSED);
									behavior.setHideable(false);
								}
							}
						}
					});
				} else {
					feedRecycler.setTranslationX(0);
					feedRecycler.setAlpha(1);
					desktop.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
						@Override
						public void onScrollChange(NestedScrollView v, int x, int y, int oldX, int oldY) {
							if (!LauncherMenu.isActive) {
								if (y + desktop.getHeight() < findViewById(R.id.desktopContent).getHeight() - dockHeight) {
									float a = 6 * getResources().getDisplayMetrics().density;
									int distance = oldY - y;
									if ((y < a || distance > a) && behavior.getState() == STATE_HIDDEN) {
										behavior.setState(STATE_COLLAPSED);
										behavior.setHideable(false);
									} else if (distance < -a && behavior.getState() == STATE_COLLAPSED) {
										behavior.setHideable(true);
										behavior.setState(STATE_HIDDEN);
									}
								} else if (behavior.getState() == STATE_HIDDEN) {
									behavior.setState(STATE_COLLAPSED);
									behavior.setHideable(false);
								}
							}
						}
					});
				}

				if (!Settings.getBool("hidestatus", false))
					desktop.setPadding(0, (int)(Tools.getStatusBarHeight(Main.this) - 12 * getResources().getDisplayMetrics().density), 0, 0);

				if (shouldSetApps) new AppLoader(Main.this, onAppLoaderEnd).execute();
				else if (apps != null) {
					drawerGrid.setAdapter(new DrawerAdapter(Main.this, apps));
					setDock();
				}
				shouldSetApps = false;
				customized = false;

				final ShapeDrawable notificationBackground = new ShapeDrawable();
				float r = getResources().getDisplayMetrics().density * Settings.getInt("feed:card_radius", 15);
				notificationBackground.setShape(new RoundRectShape(new float[]{r, r, r, r, r, r, r, r}, null, null));
				notificationBackground.getPaint().setColor(Settings.getInt("notificationbgcolor", -0x1));
				findViewById(R.id.parentNotification).setBackground(notificationBackground);
				TextView parentNotificationTitle = findViewById(R.id.parentNotificationTitle);
				parentNotificationTitle.setTextColor(Settings.getInt("notificationtitlecolor", -0xeeeded));
				ImageView parentNotificationBtn = findViewById(R.id.parentNotificationBtn);
				parentNotificationBtn.setImageTintList(ColorStateList.valueOf(ColorTools.useDarkText(accentColor) ? 0xff000000 : 0xffffffff));
				parentNotificationBtn.setBackgroundTintList(ColorStateList.valueOf(accentColor));
				parentNotificationBtn.setImageTintList(ColorStateList.valueOf(accentColor));
				parentNotificationBtn.setBackgroundTintList(ColorStateList.valueOf(accentColor & 0x00ffffff | 0x33000000));

				if (Settings.getBool("collapseNotifications", false) && NotificationService.notificationsAmount > 1) {
					notifications.setVisibility(View.GONE);
					findViewById(R.id.arrowUp).setVisibility(View.GONE);
					findViewById(R.id.parentNotification).setVisibility(View.VISIBLE);
					findViewById(R.id.parentNotification).getBackground().setAlpha(255);
				} else {
					notifications.setVisibility(View.VISIBLE);
					findViewById(R.id.parentNotification).setVisibility(View.GONE);
				}
			}
		};

		powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		desktop = findViewById(R.id.desktop);
		desktop.setNestedScrollingEnabled(false);
		desktop.setSmoothScrollingEnabled(false);

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addDataScheme("package");
		receiver = new AppChangeReceiver();
		registerReceiver(receiver, filter);

		Tools.updateNavbarHeight(Main.this);

		drawerGrid = findViewById(R.id.drawergrid);
		searchBar = findViewById(R.id.searchbar);
		drawerGrid.setOnTouchListener(new View.OnTouchListener() {
			@Override public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN && drawerGrid.canScrollVertically(-1))
					drawerGrid.requestDisallowInterceptTouchEvent(true);
				return false;
			}
		});

		behavior = BottomSheetBehavior.from(findViewById(R.id.drawer));
		behavior.setState(STATE_COLLAPSED);
		behavior.setHideable(false);
		final int[] things = new int[5];
		final float[] radii = new float[8];
		behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override public void onStateChanged(@NonNull View bottomSheet, int newState) {
				if (newState == STATE_COLLAPSED)
					drawerGrid.smoothScrollToPositionFromTop(0, 0, 0);
				things[0] = Settings.getInt("blurLayers", 1);
				things[1] = Settings.getInt("dockradius", 30);
				things[2] = Settings.getInt("drawercolor", 0x88000000);
				things[3] = Settings.getInt("dockcolor", 0x88000000);
				things[4] = Tools.canBlurWall(Main.this) ? 1 : 0;

				float tr = things[1] * getResources().getDisplayMetrics().density;
				radii[0] = tr;
				radii[1] = tr;
				radii[2] = tr;
				radii[3] = tr;
			}
			@Override public void onSlide(@NonNull View bottomSheet, float slideOffset) {
				float inverseOffset = 1 - slideOffset;
				drawerGrid.setAlpha(slideOffset);
				desktop.setAlpha(inverseOffset);
				if (slideOffset >= 0) {
					ShapeDrawable bg = (ShapeDrawable) findViewById(R.id.drawer).getBackground();
					bg.getPaint().setColor(ColorTools.blendColors(things[2], things[3], slideOffset));
					bg.setShape(new RoundRectShape(radii, null, null));
					if (things[4] == 1) {
						int repetitive = (int)(slideOffset * 255) * things[0];
						for (int i = 0; i < things[0]; i++)
							blurBg.getDrawable(i).setAlpha(Math.min(repetitive - (i << 8) + i, 255));
					}
					desktop.setTranslationY(-200 * slideOffset);
				} else if (!Settings.getBool("feed:show_behind_dock", false)) {
					((CoordinatorLayout.LayoutParams) desktop.getLayoutParams()).bottomMargin =
							(int) ((1 + slideOffset) * (dockHeight + Tools.navbarHeight +
									(Settings.getInt("dockbottompadding", 10) - 18) *
									getResources().getDisplayMetrics().density
							));
					desktop.setLayoutParams(desktop.getLayoutParams());
				} findViewById(R.id.realdock).setAlpha(inverseOffset);
			}
		});

		//setApps(drawerGrid);
		new AppLoader(Main.this, onAppLoaderEnd).execute();
		feedRecycler = findViewById(R.id.feedrecycler);
		feedRecycler.setLayoutManager(new LinearLayoutManager(Main.this));
		feedRecycler.setNestedScrollingEnabled(false);
		if (Settings.getBool("feedenabled", true)) {
			new FeedLoader(new FeedLoader.Listener() {
				@Override
				public void onFinished(@NotNull List<FeedItem> feedModels) {
					feedRecycler.setAdapter(new FeedAdapter(feedModels, Main.this, getWindow()));
				}
			}).execute((Void) null);
			feedRecycler.setVisibility(View.VISIBLE);
		} else feedRecycler.setVisibility(View.GONE);

		NotificationService.contextReference = new WeakReference<Context>(this);
		notifications = findViewById(R.id.notifications);
		notifications.setNestedScrollingEnabled(false);
		notifications.setLayoutManager(new LinearLayoutManager(this));
		ItemTouchHelper touchHelper = new ItemTouchHelper(new SwipeToDeleteCallback());
		touchHelper.attachToRecyclerView(notifications);

		final TextView parentNotificationTitle = findViewById(R.id.parentNotificationTitle);
		findViewById(R.id.parentNotification).setOnLongClickListener(new LauncherMenu(Main.this, getWindow()));
		findViewById(R.id.parentNotification).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				if (notifications.getVisibility() == View.VISIBLE) {
					desktop.scrollTo(0, 0);
					notifications.setVisibility(View.GONE);
					findViewById(R.id.parentNotification).getBackground().setAlpha(255);
					findViewById(R.id.arrowUp).setVisibility(View.GONE);
				} else {
					notifications.setVisibility(View.VISIBLE);
					findViewById(R.id.parentNotification).getBackground().setAlpha(127);
					findViewById(R.id.arrowUp).setVisibility(View.VISIBLE);
				}
			}
		});

		try {
			NotificationService.listener = new NotificationService.Listener() {
				@Override
				public void onUpdate() { try {
					Main.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (Settings.getBool("collapseNotifications", false)) {
								if (NotificationService.notificationsAmount > 1) {
									findViewById(R.id.parentNotification).setVisibility(View.VISIBLE);
									parentNotificationTitle.setText(getResources().getString(
											R.string.num_notifications,
											NotificationService.notificationsAmount
									));
									if (notifications.getVisibility() == View.VISIBLE) {
										findViewById(R.id.parentNotification).getBackground().setAlpha(127);
										findViewById(R.id.arrowUp).setVisibility(View.VISIBLE);
									} else {
										findViewById(R.id.parentNotification).getBackground().setAlpha(255);
										findViewById(R.id.arrowUp).setVisibility(View.GONE);
									}
								} else {
									findViewById(R.id.parentNotification).setVisibility(View.GONE);
									notifications.setVisibility(View.VISIBLE);
								}
							}
							notifications.setAdapter(new NotificationAdapter(Main.this, getWindow()));
						}
					});
				} catch (Exception e) { e.printStackTrace(); } }
			};
			startService(new Intent(this, NotificationService.class));
		} catch (Exception ignore) {}

		widgetLayout = findViewById(R.id.widgets);
		widgetLayout.getLayoutParams().height = Settings.getInt("widgetHeight", ViewGroup.LayoutParams.WRAP_CONTENT);
		widgetLayout.setLayoutParams(widgetLayout.getLayoutParams());
		widgetLayout.setOnLongClickListener(new View.OnLongClickListener() {
			@Override public boolean onLongClick(View v) {
				widgetLayout.setResizing(true);
				return true;
			}
		});
		widgetLayout.setOnResizeListener(new ResizableLayout.OnResizeListener() {
			@Override public void onStop(int newHeight) { Settings.putInt("widgetHeight", newHeight); }
			@Override public void onCrossPress() { deleteWidget(); }
			@Override public void onUpdate(int newHeight) {
				widgetLayout.getLayoutParams().height = newHeight;
				widgetLayout.setLayoutParams(widgetLayout.getLayoutParams());
			}
		});
		widgetManager = AppWidgetManager.getInstance(this);
		widgetHost = new AppWidgetHost(this, 0xe1d9e15);
		widgetHost.startListening();
		createWidget();

		final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(
				Main.this,
				new LauncherMenu.PinchListener(Main.this, getWindow())
		);
		findViewById(R.id.homeView).setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP && behavior.getState() == STATE_COLLAPSED)
					WallpaperManager.getInstance(Main.this).sendWallpaperCommand(
							v.getWindowToken(),
							WallpaperManager.COMMAND_TAP,
							(int)event.getX(),
							(int)event.getY(),
							0, null
					);
				return false;
			}
		});
		findViewById(R.id.desktop).setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				scaleGestureDetector.onTouchEvent(event);
				return false;
			}
		});

		if (Settings.getBool("mnmlstatus", false))
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
					View.SYSTEM_UI_FLAG_LOW_PROFILE
		); else getWindow().getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
				View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
		);
		getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
				WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
		);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			ArrayList<Rect> list = new ArrayList<>();
			list.add(new Rect(0, 0, Tools.getDisplayWidth(this), Tools.getDisplayHeight(this)));
			findViewById(R.id.homeView).setSystemGestureExclusionRects(list);
		}

		methods.setCustomizations();

		blurBg = new LayerDrawable(new Drawable[] {
				new ColorDrawable(0x0),
				new ColorDrawable(0x0),
				new ColorDrawable(0x0),
				new ColorDrawable(0x0)
		}); ((ImageView) findViewById(R.id.blur)).setImageDrawable(blurBg);


		System.gc();
	}

	void selectWidget() {
		int appWidgetId = widgetHost.allocateAppWidgetId();
		Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
		pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		ArrayList<? extends Parcelable> customInfo = new ArrayList<>();
		pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo);
		ArrayList<? extends Parcelable> customExtras = new ArrayList<>();
		pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras);
		startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK ) {
			if (requestCode == REQUEST_PICK_APPWIDGET) {
				Bundle extras = data.getExtras();
				if (extras != null) {
					int id = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
					AppWidgetProviderInfo widgetInfo = widgetManager.getAppWidgetInfo(id);
					if (widgetInfo.configure != null) {
						Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
						intent.setComponent(widgetInfo.configure);
						intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
						startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
					} else createWidget(data);
				}
			}
			else if (requestCode == REQUEST_CREATE_APPWIDGET) createWidget(data);
			//else if (requestCode == REQUEST_BIND_WIDGET) createWidget();
		} else if (resultCode == RESULT_CANCELED && data != null) {
			int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			if (appWidgetId != -1) widgetHost.deleteAppWidgetId(appWidgetId);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void deleteWidget() {
		widgetHost.deleteAppWidgetId(hostView.getAppWidgetId());
		widgetLayout.removeView(hostView);
		widgetLayout.setVisibility(View.GONE);
		Settings.putString("widget", "");
	}

	public void createWidget(Intent data) {
		widgetLayout.setVisibility(View.VISIBLE);
		try {
			widgetHost.deleteAppWidgetId(hostView.getAppWidgetId());
			widgetLayout.removeView(hostView);
		} catch (Exception e) { e.printStackTrace(); } try {
			int id = Objects.requireNonNull(data.getExtras())
					.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			AppWidgetProviderInfo providerInfo = widgetManager.getAppWidgetInfo(id);
			hostView = widgetHost.createView(getApplicationContext(), id, providerInfo);
			widgetLayout.addView(hostView);
			View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					widgetLayout.performLongClick();
					return true;
				}
			}; for (int i = 0; i < hostView.getChildCount(); i++)
				hostView.getChildAt(i).setOnLongClickListener(onLongClickListener);
			if (!widgetManager.bindAppWidgetIdIfAllowed(id, providerInfo.provider)) {
				Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
				intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
				intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, providerInfo.provider);
				startActivityForResult(intent, REQUEST_BIND_WIDGET);
			}
			Settings.putString("widget",
					providerInfo.provider.getPackageName() + "/" +
					providerInfo.provider.getClassName() + "/" + id
			);
		} catch (Exception e) { e.printStackTrace(); }
	}

	public void createWidget() {
		String str = Settings.getString("widget", "posidon.launcher/posidon.launcher.external.widgets.ClockWidget");
		if (!str.equals("")) {
			String[] s = str.split("/");
			String packageName = s[0];
			String className;
			try { className = s[1]; }
			catch (ArrayIndexOutOfBoundsException ignore) { return; }

			AppWidgetProviderInfo providerInfo = null;
			List<AppWidgetProviderInfo> appWidgetInfos = widgetManager.getInstalledProviders();
			boolean widgetIsFound = false;
			for (int j = 0; j < appWidgetInfos.size(); j++) {
				if (appWidgetInfos.get(j).provider.getPackageName().equals(packageName) && appWidgetInfos.get(j).provider.getClassName().equals(className)) {
					providerInfo = appWidgetInfos.get(j);
					widgetIsFound = true;
					break;
				}
			}

			if (!widgetIsFound) return;
			int id;
			try { id = Integer.parseInt(s[2]); }
			catch (ArrayIndexOutOfBoundsException e) {
				id = widgetHost.allocateAppWidgetId();

				if (!widgetManager.bindAppWidgetIdIfAllowed(id, providerInfo.provider)) {
					// Request permission - https://stackoverflow.com/a/44351320/1816603
					Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
					intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
					intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, providerInfo.provider);
					startActivityForResult(intent, REQUEST_BIND_WIDGET);
				}
			}
			hostView = widgetHost.createView(getApplicationContext(), id, providerInfo);
			hostView.setAppWidget(id, providerInfo);
			widgetLayout.addView(hostView);
			View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					widgetLayout.performLongClick();
					return true;
				}
			}; for (int i = 0; i < hostView.getChildCount(); i++) hostView.getChildAt(i).setOnLongClickListener(onLongClickListener);
		} else widgetLayout.setVisibility(View.GONE);
	}

	@Override public void onConfigurationChanged(@NonNull Configuration newConfig) { super.onConfigurationChanged(newConfig); onUpdate(); methods.setDock(); }
	@Override protected void onResume() {
		super.onResume();
		widgetHost.startListening();
		overridePendingTransition(R.anim.home_enter, R.anim.appexit);
		onUpdate();
	}

	private void onUpdate() {
		int tmp = Tools.navbarHeight;
		Tools.updateNavbarHeight(this);
		if (Settings.getBool("feedenabled", true)) new FeedLoader(new FeedLoader.Listener() {
			@Override public void onFinished(@NotNull List<FeedItem> feedModels) {
				feedRecycler.setAdapter(new FeedAdapter(feedModels, Main.this, getWindow()));
			}
		}).execute((Void) null);
		if (Tools.canBlurWall(this)) {
			final int blurLayers = Settings.getInt("blurLayers", 1);
			final float radius = Settings.getFloat("blurradius", 15);
			final boolean shouldHide = behavior.getState() == STATE_COLLAPSED || behavior.getState() == BottomSheetBehavior.STATE_HIDDEN;
			new Thread(new Runnable() {
				@Override public void run() {
					for (int i = 0; i < blurLayers; i++) {
						BitmapDrawable bd = new BitmapDrawable(getResources(),
								Tools.blurredWall(Main.this, radius / blurLayers * (i + 1))
						);
						if (shouldHide) bd.setAlpha(0);
						blurBg.setId(i, i);
						blurBg.setDrawableByLayerId(i, bd);
					}
				}
			}).start();
		}
		if (tmp != Tools.navbarHeight || customized) {
			methods.setCustomizations();
			try { Objects.requireNonNull(notifications.getAdapter()).notifyDataSetChanged(); }
			catch (Exception e) { e.printStackTrace(); }
		} else if (apps != null && !powerManager.isPowerSaveMode() && Settings.getBool("animatedicons", true))
			for (App app : apps) Tools.animate(app.icon);
		System.gc();
	}

	@Override protected void onPause() {
		super.onPause();
		if (LauncherMenu.isActive) LauncherMenu.dialog.dismiss();
		if (behavior.getState() != STATE_COLLAPSED) behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
		desktop.scrollTo(0, 0);
		widgetHost.stopListening();
		if (Settings.getBool("collapseNotifications", false) && NotificationService.notificationsAmount > 1) {
			notifications.setVisibility(View.GONE);
			findViewById(R.id.arrowUp).setVisibility(View.GONE);
			findViewById(R.id.parentNotification).getBackground().setAlpha(255);
		}
		System.gc();
	}

	@Override protected void onStop() {
		super.onStop();
		widgetHost.stopListening();
	}

	@Override protected void onDestroy() {
		try { unregisterReceiver(receiver); } catch (Exception ignore) {}
		unregisterReceiver(batteryInfoReceiver);
		super.onDestroy();
	}

	@Override public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			try {
				startService(new Intent(this, NotificationService.class));
			} catch (Exception ignore) {}
			if (Settings.getBool("mnmlstatus", false))
				getWindow().getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
						View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
						View.SYSTEM_UI_FLAG_LOW_PROFILE
				);
			if (shouldSetApps)  new AppLoader(Main.this, onAppLoaderEnd).execute(); //setApps(drawerGrid);
		} else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
	}

	public class AppChangeReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) { new AppLoader(Main.this, onAppLoaderEnd).execute(); }
    }

	@Override public void onBackPressed() {
		if (behavior.getState() == STATE_EXPANDED) behavior.setState(STATE_COLLAPSED);
		else if (widgetLayout.getResizing()) widgetLayout.setResizing(false);
	}

	public void openSearch(View view) {
		startActivity(
				new Intent(this, SearchActivity.class),
				ActivityOptions.makeCustomAnimation(this, R.anim.fadein, R.anim.fadeout).toBundle()
		);
	}

	public interface Methods {
		void setDock();
		void setCustomizations();
	}

	private static class AppLoader extends AsyncTask<Void, Void, Void> {

		private App[] tmpApps;
		private final WeakReference<Activity> context;
		private AsyncEndListener endListener;

		private AppLoader(Activity context, AsyncEndListener endListener) {
			this.context = new WeakReference<>(context);
			this.endListener = endListener;
		}

		@Override protected Void doInBackground(Void[] objects) {
			App.hidden.clear();
			PackageManager packageManager = context.get().getPackageManager();
			int skippedapps = 0;
			List<ResolveInfo> pacslist = packageManager.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0);
			tmpApps = new App[pacslist.size()];

			final int ICONSIZE = Tools.numtodp(65, context.get());
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
				try { themeRes = packageManager.getResourcesForApplication(iconpackName); }
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
				if (intresiconback != 0)
					back = BitmapFactory.decodeResource(themeRes, intresiconback, uniformOptions);
				if (intresiconmask != 0)
					mask = BitmapFactory.decodeResource(themeRes, intresiconmask, uniformOptions);
				if (intresiconfront != 0)
					front = BitmapFactory.decodeResource(themeRes, intresiconfront, uniformOptions);
			}

			for (int i = 0; i < pacslist.size(); i++) {
					App app = new App();
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
						try {
							app.icon = Tools.adaptic(context.get(),
									packageManager.getActivityIcon(new ComponentName(
											pacslist.get(i).activityInfo.packageName,
											pacslist.get(i).activityInfo.name)
									)
							);
						} catch (Exception e) {
							app.icon = pacslist.get(i).loadIcon(packageManager);
							e.printStackTrace();
						}
					} else app.icon = pacslist.get(i).loadIcon(packageManager);
					app.packageName = pacslist.get(i).activityInfo.packageName;
					app.name = pacslist.get(i).activityInfo.name;
					app.label = Settings.getString(app.packageName + "/" + app.name + "?label", pacslist.get(i).loadLabel(packageManager).toString());

					intres = 0;
					iconResource = ThemeTools.getResourceName(themeRes, iconpackName, "ComponentInfo{" + app.packageName + "/" + app.name + "}");
					if (iconResource != null) intres = Objects.requireNonNull(themeRes).getIdentifier(iconResource, "drawable", iconpackName);
					if (intres != 0) try {
						app.icon = themeRes.getDrawable(intres);
						try { if (!((PowerManager) context.get().getSystemService(Context.POWER_SERVICE)).isPowerSaveMode() && Settings.getBool("animatedicons", true)) Tools.animate(app.icon); }
						catch (Exception ignore) {}
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) app.icon = Tools.adaptic(context.get(), app.icon);
					} catch (Exception e) { e.printStackTrace(); } else try {
						orig = Bitmap.createBitmap(app.icon.getIntrinsicWidth(), app.icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
						app.icon.setBounds(0, 0, app.icon.getIntrinsicWidth(), app.icon.getIntrinsicHeight());
						app.icon.draw(new Canvas(orig));
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
						app.icon = new BitmapDrawable(context.get().getResources(), scaledBitmap);
					} catch (Exception e) { e.printStackTrace(); }
					App.putInSecondMap(app.packageName + "/" + app.name, app);
					if (Settings.getBool(pacslist.get(i).activityInfo.packageName + "/" + pacslist.get(i).activityInfo.name + "?hidden", false)) {
						skippedapps++;
						App.hidden.add(app);
					} else tmpApps[i - skippedapps] = app;
			}
			tmpApps = Arrays.copyOf(tmpApps, tmpApps.length - skippedapps);
			if (Settings.getInt("sortAlgorithm", 1) == 1) Sort.colorSort(tmpApps);
			else Sort.labelSort(tmpApps);
			return null;
		}

		@Override protected void onPostExecute(Void v) {
			Main.apps = tmpApps;
			App.swapMaps();
			App.clearSecondMap();
			endListener.onEnd();
		}

		public interface AsyncEndListener {
			void onEnd();
		}
	}

	private AppLoader.AsyncEndListener onAppLoaderEnd = new AppLoader.AsyncEndListener() {
		@Override
		public void onEnd() {
			drawerGrid.setAdapter(new DrawerAdapter(Main.this, Main.apps));
			drawerGrid.setOnItemLongClickListener(ItemLongPress.drawer(Main.this));
			drawerGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> av, View v, int i, long id) {
					Main.apps[i].open(Main.this, v);
				}
			});
			methods.setDock();
		}
	};
}