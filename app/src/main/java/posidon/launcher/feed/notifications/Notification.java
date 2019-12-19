package posidon.launcher.feed.notifications;

import android.app.PendingIntent;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

public class Notification {
	public final CharSequence title;
	public final CharSequence text;
	public final boolean isSummary;
	public final Drawable icon;
	public final android.app.Notification.Action[] actions;
	public final CharSequence style;
	public final String key;

	private final PendingIntent contentIntent;

	public Notification(CharSequence title, CharSequence text, boolean isSummary, Drawable icon, android.app.Notification.Action[] actions, PendingIntent contentIntent, @Nullable CharSequence style, String key) {
		this.title = title;
		this.text = text;
		this.isSummary = isSummary;
		this.icon = icon;
		this.actions = actions;
		this.contentIntent = contentIntent;
		this.style = style == null ? null : style.subSequence(25, style.length());
		this.key = key;
	}

	public void open() { try { contentIntent.send(); } catch (Exception ignore) {} }
}
