package edu.my.rstetsenko.zenua;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.my.rstetsenko.zenua.data.RateBaseColumns;
import edu.my.rstetsenko.zenua.data.RateContract;

public class Utility {
    private static final String KEY_SOUND = "key_sound";
    private static final String KEY_FULL_SCREEN = "key_full_screen";
    private static final String KEY_LAST_NOTIFICATION = "key_last_notification";
//    private static final String KEY_NOTIFICATIONS = "key_notifications";

    public static final String[] RATE_COLUMNS = {
            RateContract.RateEntry.TABLE_NAME + "." + RateBaseColumns._ID,
            RateBaseColumns.COLUMN_SOURCE_ID,
            RateBaseColumns.COLUMN_DATE,
            RateContract.RateEntry.COLUMN_USD,
            RateContract.RateEntry.COLUMN_EUR,
            RateContract.RateEntry.COLUMN_RUB
    };

    public static final String[] DOUBLE_RATE_COLUMNS = {
            RateContract.DoubleRateEntry.TABLE_NAME + "." + RateBaseColumns._ID,
            RateBaseColumns.COLUMN_SOURCE_ID,
            RateBaseColumns.COLUMN_DATE,
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

    private static void updatePref(String key, long value) {
        SharedPreferences.Editor edit = mPrefs.edit();
        edit.putLong(key, value);
        edit.apply();
    }

    public static void toggleSound() {
        updatePref(KEY_SOUND, !isSoundOn());
    }

    public static boolean isSoundOn() {
        return mPrefs.getBoolean(KEY_SOUND, true);
    }

//    public static void toggleNotifications() {
//        updatePref(KEY_NOTIFICATIONS, !isNotificationsOn());
//    }
//
//    public static boolean isNotificationsOn() {
//        return mPrefs.getBoolean(KEY_NOTIFICATIONS, true);
//    }

    public static boolean isNotificationsOn() {
        return mPrefs.getBoolean(context.getString(R.string.pref_enable_notifications_key),
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));
    }

    public static void toggleActionBarPreference() {
        updatePref(KEY_FULL_SCREEN, !isFullScreen());
    }

    public static boolean isFullScreen() {
        return mPrefs.getBoolean(KEY_FULL_SCREEN, false);
    }

    public static void setLastNotificationTimestamp(long timestamp) {
        updatePref(KEY_LAST_NOTIFICATION, timestamp);
    }

    public static long getLastNotificationTimestamp() {
        return mPrefs.getLong(KEY_LAST_NOTIFICATION, 0);
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

    public static String getDayAndMonth(long dateInMillis) {
        Date date = new Date(dateInMillis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd");
        return dateFormat.format(date);
    }

//    public static long getTimeFromDate(String Date) {
//        Date date;
//        try {
//            date = DateFormat.getDateTimeInstance().parse(Date);
//        } catch (ParseException e) {
//            e.printStackTrace();
//            return 0;
//        }
//        return date.getTime();
//    }

    public static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateTimeInstance().format(date);
    }

    public static String getDescription(int sourceId) {
        switch (sourceId){
            case Constants.PRIVATE:
                return context.getString(R.string.private_description_cash);
            case Constants.MIN_FIN:
            case Constants.JSON_RATES:
            case Constants.OPEN_EXCHANGE_RATES:
                return "";
            case Constants.FINANCE:
                return context.getString(R.string.finance_description_average);
            default:
                return "";
        }
    }

    public static String getSourceName(int sourceId) {
        switch (sourceId){
            case Constants.PRIVATE:
                return context.getString(R.string.pref_private_label);
            case Constants.MIN_FIN:
                return context.getString(R.string.minfin_label);
            case Constants.JSON_RATES:
                return context.getString(R.string.pref_jsonrates_label);
            case Constants.OPEN_EXCHANGE_RATES:
                return context.getString(R.string.pref_openexchangerates_label);
            case Constants.FINANCE:
                return context.getString(R.string.finance_label);
            default:
                return "";
        }
    }

    public static String prepareDoubleRateDescriptionString(double usdBuy, double usdSell, double eurBuy, double eurSell, double rubBuy, double rubSell) {
        return String.format("USD: %s %6.2f %s %6.2f%nEUR: %s %6.2f %s %6.2f%nRUB: %s %6.2f %s %6.2f",
                context.getString(R.string.buy), usdBuy,
                context.getString(R.string.sell), usdSell,
                context.getString(R.string.buy), eurBuy,
                context.getString(R.string.sell), eurSell,
                context.getString(R.string.buy), rubBuy,
                context.getString(R.string.sell), rubSell
        );
    }

    public static String prepareRateDescriptionString(double usd, double eur, double rub) {
        return String.format("USD: %.2f%nEUR: %.2f%nRUB: %.2f", usd, eur, rub);
    }

    public static boolean isConnectedToInternet(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        return net != null && net.isAvailable() && net.isConnected();
    }
}
