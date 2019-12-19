package posidon.launcher.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {
    private static SharedPreferences settings;

    public static void init(Context context) { settings = PreferenceManager.getDefaultSharedPreferences(context); }

    public static void putString(String key, String value) { settings.edit().putString(key, value).apply(); }
    public static void putInt(String key, int value) { settings.edit().putInt(key, value).apply(); }
    public static void putFloat(String key, float value) { settings.edit().putFloat(key, value).apply(); }
    public static void putBool(String key, boolean value) { settings.edit().putBoolean(key, value).apply(); }

    public static String getString(String key, String def) { return settings.getString(key, def); }
    public static int getInt(String key, int def) { return settings.getInt(key, def); }
    public static float getFloat(String key, float def) { return settings.getFloat(key, def); }
    public static boolean getBool(String key, boolean def) { return settings.getBoolean(key, def); }
}
