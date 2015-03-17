package edu.my.rstetsenko.zenua;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.my.rstetsenko.zenua.data.RateBaseColumns;
import edu.my.rstetsenko.zenua.data.RateContract;

public class Utility {
    private static final String KEY_SOUND = "key_sound";

    public static final String[] RATE_COLUMNS = {
            RateContract.RateEntry.TABLE_NAME + "." + RateBaseColumns._ID,
            RateBaseColumns.COLUMN_DATE,
            RateBaseColumns.COLUMN_SOURCE_ID,
            RateContract.RateEntry.COLUMN_USD,
            RateContract.RateEntry.COLUMN_EUR,
            RateContract.RateEntry.COLUMN_RUB
    };

    public static final String[] DOUBLE_RATE_COLUMNS = {
            RateContract.DoubleRateEntry.TABLE_NAME + "." + RateBaseColumns._ID,
            RateBaseColumns.COLUMN_DATE,
            RateBaseColumns.COLUMN_SOURCE_ID,
            RateContract.DoubleRateEntry.COLUMN_USD_BUY,
            RateContract.DoubleRateEntry.COLUMN_USD_SELL,
            RateContract.DoubleRateEntry.COLUMN_EUR_BUY,
            RateContract.DoubleRateEntry.COLUMN_EUR_SELL,
            RateContract.DoubleRateEntry.COLUMN_RUB_BUY,
            RateContract.DoubleRateEntry.COLUMN_RUB_SELL
    };

    public static final int COL_ID = 0;
    public static final int COL_SOURCE_ID = 1;
    public static final int COL_DATE = 2;
    public static final int COL_RATE_USD = 3;
    public static final int COL_RATE_EUR = 4;
    public static final int COL_RATE_RUB = 5;
    public static final int COL_DOUBLE_RATE_USD_BUY = 3;
    public static final int COL_DOUBLE_RATE_USD_SELL = 4;
    public static final int COL_DOUBLE_RATE_EUR_BUY = 5;
    public static final int COL_DOUBLE_RATE_EUR_SELL = 6;
    public static final int COL_DOUBLE_RATE_RUB_BUY = 7;
    public static final int COL_DOUBLE_RATE_RUB_SELL = 8;

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

    public static long getTimeFromUTCDate(String UTCDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Date date;
        try {
            date = dateFormat.parse(UTCDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
        return date.getTime();
    }
}
