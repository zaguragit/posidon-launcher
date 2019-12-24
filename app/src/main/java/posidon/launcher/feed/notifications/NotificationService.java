package posidon.launcher.feed.notifications;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import posidon.launcher.tools.Settings;
import posidon.launcher.tools.Tools;

public class NotificationService extends NotificationListenerService {

	private static ArrayList<ArrayList<Notification>> notificationGroups = new ArrayList<>();
	public static Listener listener;
	public static WeakReference<Context> contextReference;
	public static int notificationsAmount;

	private static boolean updating;

	public static ArrayList<ArrayList<Notification>> groups() { return notificationGroups; }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		SwipeToDeleteCallback.swipeListener = new SwipeToDeleteCallback.SwipeListener() {
			@Override
			public void onSwipe(RecyclerView.ViewHolder viewHolder, int direction) {
				try {
					int pos = viewHolder.getAdapterPosition();
					ArrayList<Notification> group = notificationGroups.get(pos);
					for (Notification notification : group)
						cancelNotification(notification.key);
					group.clear();
					notificationGroups.remove(pos);
				} catch (Exception e) { e.printStackTrace(); }
				onUpdate();
			}
		};
		onUpdate();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override public void onNotificationPosted(StatusBarNotification sbn) { onUpdate(); }
	@Override public void onNotificationRemoved(StatusBarNotification sbn) { onUpdate(); }
	@Override public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap, int reason) { onUpdate(); }
	@Override public void onNotificationChannelModified(String pkg, UserHandle user, NotificationChannel channel, int modificationType) { onUpdate(); }
	@Override public void onNotificationChannelGroupModified(String pkg, UserHandle user, NotificationChannelGroup group, int modificationType) { onUpdate(); }
	@Override public void onNotificationRankingUpdate(RankingMap rankingMap) { onUpdate(); }

	private void onUpdate() { if (!updating) new NotificationLoader(getActiveNotifications()).start(); }

	public interface Listener { void onUpdate(); }

	private static class NotificationLoader extends Thread {

		private final StatusBarNotification[] notifications;

		NotificationLoader(StatusBarNotification[] notifications) { this.notifications = notifications; }

		@Override
		public void run() {
			updating = true;
			ArrayList<ArrayList<Notification>> groups = new ArrayList<>();
			int i = 0;
			int notificationsAmount2 = 0;
			try {
				if (notifications != null) while (i < notifications.length) {
					ArrayList<Notification> group = new ArrayList<>();
					//if (notifications[i].getPackageName().equals("android")) i++;
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && notifications[i].isGroup() &&
							(notifications[i].getNotification().flags & android.app.Notification.FLAG_GROUP_SUMMARY) ==
							android.app.Notification.FLAG_GROUP_SUMMARY) {
						String key = notifications[i].getGroupKey();
						Bundle last = null;
						Bundle extras;
						while (i < notifications.length && notifications[i].isGroup() && notifications[i].getGroupKey().equals(key) && notifications[i].getGroupKey() != null) {
							extras = notifications[i].getNotification().extras;
							if (last == null ||
									extras.getCharSequence(android.app.Notification.EXTRA_TITLE)    !=
									last.getCharSequence(android.app.Notification.EXTRA_TITLE)      ||
									extras.getCharSequence(android.app.Notification.EXTRA_TEXT)     !=
									last.getCharSequence(android.app.Notification.EXTRA_TEXT)       ||
									extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT) !=
									last.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT)) {
								group.add(formatNotification(notifications[i]));
								if ((notifications[i].getNotification().flags & android.app.Notification.FLAG_GROUP_SUMMARY) != android.app.Notification.FLAG_GROUP_SUMMARY)
									notificationsAmount2++;
							}
							last = extras;
							i++;
						}
					} else {
						group.add(formatNotification(notifications[i]));
						notificationsAmount2++;
						i++;
					}
					groups.add(group);
				}
			} catch (Exception e) { e.printStackTrace(); }
			notificationGroups.clear();
			notificationGroups = groups;
			notificationsAmount = notificationsAmount2;
			if (listener != null) listener.onUpdate();
			updating = false;
		}

		private Notification formatNotification(StatusBarNotification notification) {
			Bundle extras = notification.getNotification().extras;
			boolean isSummary = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && (notification.getNotification().flags & android.app.Notification.FLAG_GROUP_SUMMARY) == android.app.Notification.FLAG_GROUP_SUMMARY;

			CharSequence title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE);
			if (title == null || title.toString().replace(" ", "") == "") {
				try { title = contextReference.get().getPackageManager().getApplicationLabel(contextReference.get().getPackageManager().getApplicationInfo(notification.getPackageName(), 0)); }
				catch (Exception e) { e.printStackTrace(); }
			}

			Drawable icon = null;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
				try { icon = notification.getNotification().getLargeIcon().loadDrawable(contextReference.get()); }
				catch (Exception ignore) {}
			if (icon == null) try {
				icon = contextReference.get().createPackageContext(notification.getPackageName(), 0).getResources().getDrawable(notification.getNotification().icon);
				Tools.animate(icon);
				ColorStateList colorlist = ColorStateList.valueOf((notification.getNotification().color == Settings.getInt("notificationbgcolor", -0x1) || notification.getNotification().color == 0)
						? Settings.getInt("notificationtitlecolor", -0xeeeded) : notification.getNotification().color);
				icon.setTintList(colorlist);
			} catch (Exception e) { e.printStackTrace(); }

			CharSequence text = extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT);
			if (text == null || isSummary) text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT);

			return new Notification(
					title, text, isSummary, icon,
					notification.getNotification().actions,
					notification.getNotification().contentIntent,
					extras.getCharSequence(android.app.Notification.EXTRA_TEMPLATE),
					notification.getKey()
			);
		}
	}
}
