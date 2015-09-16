package edu.my.rstetsenko.zenua.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.my.rstetsenko.zenua.Constants;
import edu.my.rstetsenko.zenua.R;
import edu.my.rstetsenko.zenua.Utility;
import edu.my.rstetsenko.zenua.activities.MainActivity;
import edu.my.rstetsenko.zenua.data.RateBaseColumns;
import edu.my.rstetsenko.zenua.data.RateContract;


public class ZenUaSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String OPEN_EXCHANGE_RATES_REQUEST = "http://openexchangerates.org/api/latest.json?app_id=60acbc550c654afd86eea4304cdec3f0";
    /** jsonrates.com moved to currencylayer.com    */
    private static final String CURRENCYLAYER_REQUEST = "http://apilayer.net/api/live?access_key=f240012d96396239cfbe653939eff33e&currencies=EUR,UAH,RUB";
    /** usage limit has been reached    */
    private static final int CURRENCYLAYER_ERROR_CODE = 104;
    //    private static final String PRIVATE_NBU_REQUEST= "https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=3";
    private static final String PRIVATE_CASH_REQUEST = "https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=5";
    //    private static final String PRIVATE_NON_CASH_REQUEST= "https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=11";
//    private static final String INTERBANK_REQUEST= "http://api.minfin.com.ua/mb/4d18fc9525f199ed8ba09a535fe3367b6e3c39f1/"; //MINFIN api, not works
    private static final String INTERBANK_BANKS_REQUEST = "http://api.minfin.com.ua/summary/4d18fc9525f199ed8ba09a535fe3367b6e3c39f1/"; //banks info from MINFIN
    //    private static final String CURRENCY_AUCTION_REQUEST= "http://api.minfin.com.ua/auction/info/4d18fc9525f199ed8ba09a535fe3367b6e3c39f1/"; // MINFIN Currency auction
    private static final String FINANCE_REQUEST = "http://resources.finance.ua/ua/public/currency-cash.json";

    private static final int SYNC_INTERVAL = 60 * 180;
    private static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int RATE_NOTIFICATION_ID = 3764;

    public ZenUaSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(Constants.LOG_TAG, "-----STARTING SYNC!-----");

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(prepareLinks()[0]);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                return;
            }
            parseJson(buffer.toString());

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Log.e(Constants.LOG_TAG, "Error ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(Constants.LOG_TAG, "Error closing stream", e);
                }
            }
        }

    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                //TODO add to Crashlytics
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            onAccountCreated(newAccount, context);
            if (!Utility.isConnectedToInternet(context)) {
                Toast.makeText(context, context.getString(R.string.check_internet_connection), Toast.LENGTH_LONG).show();
            }
        }
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        ZenUaSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    private String[] prepareLinks() {
        switch (Utility.getPreferredSource()) {
            case Constants.PRIVATE:
                return new String[]{PRIVATE_CASH_REQUEST};
            case Constants.MIN_FIN:
                return new String[]{INTERBANK_BANKS_REQUEST};
            case Constants.CURRENCYLAYER:
                return new String[]{CURRENCYLAYER_REQUEST};
            case Constants.OPEN_EXCHANGE_RATES:
                return new String[]{OPEN_EXCHANGE_RATES_REQUEST};
            case Constants.FINANCE:
                return new String[]{FINANCE_REQUEST};
            default:
                return null;
        }
    }

    private void parseJson(String jsonString) throws JSONException {

        //openexchangerates.org structure:
        String timestamp = "timestamp"; //long
        String rates = "rates";
        String uah = "UAH";
        String eur = "EUR";
        String rub = "RUB";

        //currencylayer.com structure:
        String success = "success";
        String quotes = "quotes";
        String usdUah = "USDUAH";
        String usdEur = "USDEUR";
        String usdRub = "USDRUB";
        String error = "error";
        String code = "code";
        String info = "info";

        //private structure:
        String buy = "buy";
        String sale = "sale";

        //minfin structure:
        String bid = "bid";
        String ask = "ask";
        String usd = "USD";

        //finance.ua structure
        String date = "date"; //UTC string
        String organizations = "organizations";
        String orgType = "orgType";
        //orgType: 1 - banks
        //orgType: 2 - exchange office
        //TODO provide choosing banks / exchange offices
        String currencies = "currencies";

        long receivedTimestamp;

        try {
            switch (Utility.getPreferredSource()) {
                case Constants.PRIVATE:
                    JSONArray privateJsonArray = new JSONArray(jsonString);
                    JSONObject rubJson = privateJsonArray.getJSONObject(0);
                    JSONObject eurJson = privateJsonArray.getJSONObject(1);
                    JSONObject usdJson = privateJsonArray.getJSONObject(2);
                    addDoubleRateRow(Constants.PRIVATE, System.currentTimeMillis(), usdJson.getDouble(buy), usdJson.getDouble(sale),
                            eurJson.getDouble(buy), eurJson.getDouble(sale), rubJson.getDouble(buy), rubJson.getDouble(sale));
                    break;

                case Constants.MIN_FIN:
                    JSONObject interBankJson = new JSONObject(jsonString);
                    JSONObject usdInterBankJson = interBankJson.getJSONObject(usd.toLowerCase());
                    JSONObject eurInterBankJson = interBankJson.getJSONObject(eur.toLowerCase());
                    JSONObject rubInterBankJson = interBankJson.getJSONObject(rub.toLowerCase());
                    addDoubleRateRow(Constants.MIN_FIN, System.currentTimeMillis(),
                            usdInterBankJson.getDouble(bid), usdInterBankJson.getDouble(ask),
                            eurInterBankJson.getDouble(bid), eurInterBankJson.getDouble(ask),
                            rubInterBankJson.getDouble(bid), rubInterBankJson.getDouble(ask));
                    break;

                case Constants.CURRENCYLAYER:
                    JSONObject wholeJson = new JSONObject(jsonString);
                    boolean isSuccessful = wholeJson.getBoolean(success);
                    if (isSuccessful) {
                        receivedTimestamp = wholeJson.getLong(timestamp) * 1000;
                        JSONObject clJson = wholeJson.getJSONObject(quotes);
                        addRateRow(Constants.CURRENCYLAYER, receivedTimestamp, clJson.getDouble(usdUah),
                                clJson.getDouble(usdUah) / clJson.getDouble(usdEur), clJson.getDouble(usdUah) / clJson.getDouble(usdRub));
                    } else {
                        JSONObject errorResult = wholeJson.getJSONObject(error);
                        if (errorResult.getInt(code) == CURRENCYLAYER_ERROR_CODE) {
                            //TODO send intent via broadcastreceiver to show some Dialog
                            Log.d(Constants.LOG_TAG, errorResult.getString(info));
                        }
                    }
                    break;

                case Constants.OPEN_EXCHANGE_RATES:
                    JSONObject openExchangeRatesWholeJson = new JSONObject(jsonString);
                    receivedTimestamp = openExchangeRatesWholeJson.getLong(timestamp) * 1000;
                    JSONObject oerJson = openExchangeRatesWholeJson.getJSONObject(rates);
                    addRateRow(Constants.OPEN_EXCHANGE_RATES, receivedTimestamp, oerJson.getDouble(uah),
                            oerJson.getDouble(uah) / oerJson.getDouble(eur), oerJson.getDouble(uah) / oerJson.getDouble(rub));
                    break;

                case Constants.FINANCE:
                    JSONObject financeWholeJson = new JSONObject(jsonString);
                    JSONArray banksOfUkraine = financeWholeJson.getJSONArray(organizations);
                    double usdRateBuy = 0;
                    double eurRateBuy = 0;
                    double rubRateBuy = 0;
                    double usdRateSell = 0;
                    double eurRateSell = 0;
                    double rubRateSell = 0;
                    int usdBankCounter = 0;
                    int eurBankCounter = 0;
                    int rubBankCounter = 0;
                    for (int i = 0; i < banksOfUkraine.length(); i++) {
                        JSONObject bank = banksOfUkraine.getJSONObject(i);
                        if (bank.getInt(orgType) == 1) {
                            JSONObject currenciesJson = bank.getJSONObject(currencies);
                            try {
                                JSONObject usdFinanceJson = currenciesJson.getJSONObject(usd);
                                usdRateBuy += usdFinanceJson.getDouble(bid);
                                usdRateSell += usdFinanceJson.getDouble(ask);
                                usdBankCounter++;
                            } catch (JSONException ex) {
                            }
                            try {
                                JSONObject eurFinanceJson = currenciesJson.getJSONObject(eur);
                                eurRateBuy += eurFinanceJson.getDouble(bid);
                                eurRateSell += eurFinanceJson.getDouble(ask);
                                eurBankCounter++;
                            } catch (JSONException ex) {
                            }
                            try {
                                JSONObject rubFinanceJson = currenciesJson.getJSONObject(rub);
                                rubRateBuy += rubFinanceJson.getDouble(bid);
                                rubRateSell += rubFinanceJson.getDouble(ask);
                                rubBankCounter++;
                            } catch (JSONException ex) {
                            }
                        }
                    }
                    if (usdBankCounter == 0) {
                        usdBankCounter = 1; //in case of incorrect json
                    }
                    if (eurBankCounter == 0) {
                        eurBankCounter = 1; //in case of incorrect json
                    }
                    if (rubBankCounter == 0) {
                        rubBankCounter = 1; //in case of incorrect json
                    }
                    String jsonUpdateDate = financeWholeJson.getString(date);
                    addDoubleRateRow(Constants.FINANCE, Utility.getTimeFromUTCDate(jsonUpdateDate),
                            usdRateBuy / usdBankCounter, usdRateSell / usdBankCounter,
                            eurRateBuy / eurBankCounter, eurRateSell / eurBankCounter,
                            rubRateBuy / rubBankCounter, rubRateSell / rubBankCounter);
                    break;
            }
            notifyNewRate();
            //TODO maybe need to clear db here?
            //Example
//            getContext().getContentResolver().delete(WeatherContract.WeatherEntry.CONTENT_URI,
//                    WeatherContract.WeatherEntry.COLUMN_DATE + " <= ?",
//                    new String[]{Long.toString(dayTime.setJulianDay(julianStartDay - 1))});
        } catch (JSONException e) {
            Log.e(Constants.LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void addRateRow(int sourceId, long updateDate, double usd, double eur, double rub) {
        if (usd <= 0 || eur <= 0 || rub <= 0) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(RateBaseColumns.COLUMN_SOURCE_ID, sourceId);
        values.put(RateBaseColumns.COLUMN_DATE, updateDate);
        values.put(RateContract.RateEntry.COLUMN_USD, usd);
        values.put(RateContract.RateEntry.COLUMN_EUR, eur);
        values.put(RateContract.RateEntry.COLUMN_RUB, rub);
        Uri resultUri = getContext().getContentResolver().insert(RateContract.RateEntry.CONTENT_URI, values);
        long id = ContentUris.parseId(resultUri);
        Log.d(Constants.LOG_TAG, "rate, added row id: " + id);
    }

    private void addDoubleRateRow(int sourceId, long updateDate, double usdBuy, double usdSell,
                                  double eurBuy, double eurSell, double rubBuy, double rubSell) {
        if (usdBuy <= 0 || usdSell <= 0 || eurBuy <= 0 ||  eurSell <= 0 || rubBuy <= 0 || rubSell <= 0) {
            return;
            //TODO send intent via broadcastreceiver to show some Dialog
        }
        ContentValues values = new ContentValues();
        values.put(RateBaseColumns.COLUMN_SOURCE_ID, sourceId);
        values.put(RateBaseColumns.COLUMN_DATE, updateDate);
        values.put(RateContract.DoubleRateEntry.COLUMN_USD_BUY, usdBuy);
        values.put(RateContract.DoubleRateEntry.COLUMN_USD_SELL, usdSell);
        values.put(RateContract.DoubleRateEntry.COLUMN_EUR_BUY, eurBuy);
        values.put(RateContract.DoubleRateEntry.COLUMN_EUR_SELL, eurSell);
        values.put(RateContract.DoubleRateEntry.COLUMN_RUB_BUY, rubBuy);
        values.put(RateContract.DoubleRateEntry.COLUMN_RUB_SELL, rubSell);

        Uri resultUri = getContext().getContentResolver().insert(RateContract.DoubleRateEntry.CONTENT_URI, values);
        long id = ContentUris.parseId(resultUri);
        Log.d(Constants.LOG_TAG, "double rate, added row id: " + id);
    }

    private void notifyNewRate() {
        Context context = getContext();
        long lastSync = Utility.getLastNotificationTimestamp();

        if (Utility.isNotificationsOn()) {
            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
                int sourceId = Utility.getPreferredSource();
                boolean isSingleRate = Constants.singleRates.contains(sourceId);
                Uri uri = isSingleRate ?
                        RateContract.RateEntry.buildRateSourceIdWithLastDate(sourceId) :
                        RateContract.DoubleRateEntry.buildDoubleRateSourceIdWithLastDate(sourceId);

                String[] projection = isSingleRate ?
                        Utility.RATE_COLUMNS : Utility.DOUBLE_RATE_COLUMNS;

                Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

                if (cursor.moveToFirst()) {
                    String contentText;
                    if (isSingleRate) {
                        contentText = Utility.prepareRateDescriptionString(
                                cursor.getDouble(Utility.COL_RATE_USD),
                                cursor.getDouble(Utility.COL_RATE_EUR),
                                cursor.getDouble(Utility.COL_RATE_RUB)
                        );
                    } else {
                        contentText = Utility.prepareDoubleRateDescriptionString(
                                cursor.getDouble(Utility.COL_DOUBLE_RATE_USD_BUY),
                                cursor.getDouble(Utility.COL_DOUBLE_RATE_USD_SELL),
                                cursor.getDouble(Utility.COL_DOUBLE_RATE_EUR_BUY),
                                cursor.getDouble(Utility.COL_DOUBLE_RATE_EUR_SELL),
                                cursor.getDouble(Utility.COL_DOUBLE_RATE_RUB_BUY),
                                cursor.getDouble(Utility.COL_DOUBLE_RATE_RUB_SELL)
                        );
                    }
                String desc = Utility.getSourceName(sourceId);
//                int iconId = Utility.getIconResourceForWeatherCondition(weatherId);
                String title = context.getString(R.string.app_name);

                    //TODO add description and\or icon of rate source. maybe custom layout?
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getContext())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(title)
//                            .setContentText(desc)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(desc +"\n" +contentText));

                    // App will be open when the user clicks on the notification.
                    Intent resultIntent = new Intent(context, MainActivity.class);

                    // The stack builder object will contain an artificial back stack for the started Activity.
                    // This ensures that navigating backward from the Activity leads out of your application to the Home screen.
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
                    mBuilder.setContentIntent(resultPendingIntent);

                    Log.d("MY_LOG", "Notification!");

                    NotificationManager mNotificationManager =
                            (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(RATE_NOTIFICATION_ID, mBuilder.build());

                    Utility.setLastNotificationTimestamp(System.currentTimeMillis());
                }
                cursor.close();
            }
        }
    }
}