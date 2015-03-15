package edu.my.rstetsenko.zenua;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Utility {
    private static final String KEY_SOUND = "key_sound";

    private static SharedPreferences mPrefs;
    private static Context context;

    private Utility() {
    }

    public static void init(Context applicationContext) {
        context = applicationContext;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(applicationContext);
    }

    private static void updatePref(String key, boolean value) {
        SharedPreferences.Editor edit = mPrefs.edit();
        edit.putBoolean(key, value);
        edit.apply();
    }

    public static void toggleSound() {
        updatePref(KEY_SOUND, !isSoundOn());
    }

    public static boolean isSoundOn() {
        return mPrefs.getBoolean(KEY_SOUND, true);
    }

    public static int getPreferredSource() {
        String source = mPrefs.getString(context.getString(R.string.pref_source_key), context.getString(R.string.pref_source_default));
        return Integer.parseInt(source);
    }
}
