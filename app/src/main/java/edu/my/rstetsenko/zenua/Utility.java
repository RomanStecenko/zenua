package edu.my.rstetsenko.zenua;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Utility {

    public static int getPreferredSource(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String source = prefs.getString(context.getString(R.string.pref_source_key), context.getString(R.string.pref_source_default));
        return Integer.parseInt(source);
    }

}
