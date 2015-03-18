package edu.my.rstetsenko.zenua;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.my.rstetsenko.zenua.data.RateBaseColumns;
import edu.my.rstetsenko.zenua.data.RateContract;

public class FetchRateTask extends AsyncTask<String, Void, Void> {

    private final Context mContext;

    public FetchRateTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (params == null || params.length == 0) {
            return null;
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(params[0]);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                return null;
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
        return null;
    }

    private void parseJson(String jsonString) throws JSONException {

        //openexchangerates.org structure:
        String timestamp = "timestamp"; //long
        String rates = "rates";
        String uah = "UAH";
        String eur = "EUR";
        String rub = "RUB";

        //jsonrates.com structure:
        String utctime = "utctime"; //UTC string

        //private structure:
        String buy = "buy";
        String sale = "sale";

        //minfin structure:
        String bid = "bid";
        String ask = "ask";
        String usd = "USD";

        //finance.ua structure
        String data = "date"; //UTC string
        String organizations = "organizations";
        String orgType = "orgType";
        //orgType: 1 - banks
        //orgType: 2 - exchange office
        //TODO provide choosing banks / exchange office
        String currencies = "currencies";

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

                case Constants.JSON_RATES:
                    JSONObject wholeJson = new JSONObject(jsonString);
                    String receivedUTCTime = wholeJson.getString(utctime);
                    JSONObject ratesJson = wholeJson.getJSONObject(rates);
                    addRateRow(Constants.JSON_RATES, Utility.getTimeFromUTCDate(receivedUTCTime), ratesJson.getDouble(uah),
                            ratesJson.getDouble(uah) / ratesJson.getDouble(eur), ratesJson.getDouble(uah) / ratesJson.getDouble(rub));
                    break;

                case Constants.OPEN_EXCHANGE_RATES:
                    JSONObject openExchangeRatesWholeJson = new JSONObject(jsonString);
                    long receivedTimestamp = openExchangeRatesWholeJson.getLong(timestamp) * 1000;
                    JSONObject openExchangeRatesJson = openExchangeRatesWholeJson.getJSONObject(rates);
                    addRateRow(Constants.OPEN_EXCHANGE_RATES, receivedTimestamp, openExchangeRatesJson.getDouble(uah),
                            openExchangeRatesJson.getDouble(uah) / openExchangeRatesJson.getDouble(eur), openExchangeRatesJson.getDouble(uah) / openExchangeRatesJson.getDouble(rub));
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
                    int usdBankCrashCounter = 0; //temp
                    int eurBankCrashCounter = 0; //temp
                    int rubBankCrashCounter = 0; //temp
                    for (int i=0; i < banksOfUkraine.length(); i++) {
                        JSONObject bank = banksOfUkraine.getJSONObject(i);
                        if (bank.getInt(orgType) == 1) {
                            JSONObject currenciesJson = bank.getJSONObject(currencies);
                            try {
                                JSONObject usdFinanceJson = currenciesJson.getJSONObject(usd);
                                usdRateBuy += usdFinanceJson.getDouble(bid);
                                usdRateSell += usdFinanceJson.getDouble(ask);
                                usdBankCounter++;
                            } catch (JSONException ex) {
                                usdBankCrashCounter++;
                            }
                            try {
                                JSONObject eurFinanceJson = currenciesJson.getJSONObject(eur);
                                eurRateBuy += eurFinanceJson.getDouble(bid);
                                eurRateSell += eurFinanceJson.getDouble(ask);
                                eurBankCounter++;
                            } catch (JSONException ex) {
                                eurBankCrashCounter++;
                            }
                            try {
                                JSONObject rubFinanceJson = currenciesJson.getJSONObject(rub);
                                rubRateBuy += rubFinanceJson.getDouble(bid);
                                rubRateSell += rubFinanceJson.getDouble(ask);
                                rubBankCounter++;
                            } catch (JSONException ex) {
                                rubBankCrashCounter++;
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
                    Log.d(Constants.LOG_TAG, "USD bank crashes:" + usdBankCrashCounter); //temp
                    Log.d(Constants.LOG_TAG, "EUR bank crashes:" + eurBankCrashCounter); //temp
                    Log.d(Constants.LOG_TAG, "RUB bank crashes:" + rubBankCrashCounter); //temp
                    String jsonUpdateDate = financeWholeJson.getString(data);
                    addDoubleRateRow(Constants.FINANCE, Utility.getTimeFromUTCDate(jsonUpdateDate),
                            usdRateBuy / usdBankCounter, usdRateSell / usdBankCounter,
                            eurRateBuy / eurBankCounter, eurRateSell / eurBankCounter,
                            rubRateBuy / rubBankCounter, rubRateSell / rubBankCounter);
                    break;
            }

        } catch (JSONException e) {
            Log.e(Constants.LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }



    private void addRateRow(int sourceId, long updateDate, double usd, double eur, double rub) {
        ContentValues values = new ContentValues();
        values.put(RateBaseColumns.COLUMN_SOURCE_ID, sourceId);
        values.put(RateBaseColumns.COLUMN_DATE, updateDate);
        values.put(RateContract.RateEntry.COLUMN_USD, usd);
        values.put(RateContract.RateEntry.COLUMN_EUR, eur);
        values.put(RateContract.RateEntry.COLUMN_RUB, rub);
        Uri resultUri = mContext.getContentResolver().insert(RateContract.RateEntry.CONTENT_URI, values);
        long id = ContentUris.parseId(resultUri);
        Log.d(Constants.LOG_TAG, "rate, added row id: " + id);
    }

    private void addDoubleRateRow(int sourceId, long updateDate, double usdBuy, double usdSell,
                                  double eurBuy, double eurSell, double rubBuy, double rubSell) {
        ContentValues values = new ContentValues();
        values.put(RateBaseColumns.COLUMN_SOURCE_ID, sourceId);
        values.put(RateBaseColumns.COLUMN_DATE, updateDate);
        values.put(RateContract.DoubleRateEntry.COLUMN_USD_BUY, usdBuy);
        values.put(RateContract.DoubleRateEntry.COLUMN_USD_SELL, usdSell);
        values.put(RateContract.DoubleRateEntry.COLUMN_EUR_BUY,  eurBuy);
        values.put(RateContract.DoubleRateEntry.COLUMN_EUR_SELL, eurSell);
        values.put(RateContract.DoubleRateEntry.COLUMN_RUB_BUY, rubBuy);
        values.put(RateContract.DoubleRateEntry.COLUMN_RUB_SELL, rubSell);

        Uri resultUri = mContext.getContentResolver().insert(RateContract.DoubleRateEntry.CONTENT_URI, values);
        long id = ContentUris.parseId(resultUri);
        Log.d(Constants.LOG_TAG, "double rate, added row id: " + id);
    }

}
