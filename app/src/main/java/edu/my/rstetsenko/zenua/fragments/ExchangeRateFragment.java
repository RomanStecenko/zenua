package edu.my.rstetsenko.zenua.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.my.rstetsenko.zenua.Constants;
import edu.my.rstetsenko.zenua.R;
import edu.my.rstetsenko.zenua.Utility;
import edu.my.rstetsenko.zenua.data.RateContract;

public class ExchangeRateFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String OPEN_EXCHANGE_RATES_REQUEST = "http://openexchangerates.org/api/latest.json?app_id=60acbc550c654afd86eea4304cdec3f0";
    private static final String JSON_RATES_REQUEST= "http://jsonrates.com/get/?%20base=USD&apiKey=jr-878f7938dc3db294f030a675358a2ed9";
    private static final String PRIVATE_NBU_REQUEST= "https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=3";
    private static final String PRIVATE_CASH_REQUEST= "https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=5";
    private static final String PRIVATE_NON_CASH_REQUEST= "https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=11";
    private static final String INTERBANK_REQUEST= "http://api.minfin.com.ua/mb/4d18fc9525f199ed8ba09a535fe3367b6e3c39f1/"; //MINFIN api, not works
    private static final String INTERBANK_BANKS_REQUEST= "http://api.minfin.com.ua/summary/4d18fc9525f199ed8ba09a535fe3367b6e3c39f1/"; //banks info from MINFIN
    private static final String CURRENCY_AUCTION_REQUEST= "http://api.minfin.com.ua/auction/info/4d18fc9525f199ed8ba09a535fe3367b6e3c39f1/"; // MINFIN Currency auction
    private static final String FINANCE_REQUEST= "http://resources.finance.ua/ua/public/currency-cash.json";

    private static final String SHARE_TAG = " #ZenUA";
    private static final int EXCHANGE_RATE_LOADER_ID = 0;

    private ShareActionProvider mShareActionProvider;

    private static final int PRIVATE = 0;
    private static final int MIN_FIN = 1;
    private static final int JSON_RATES = 2;
    private static final int OPEN_EXCHANGE_RATES = 3;
    private static final int FINANCE = 4;

    private TextView usdTextView;
    private TextView eurTextView;
    private TextView rubTextView;
    private TextView usdRateBuy, usdRateSell, eurRateBuy, eurRateSell, rubRateBuy, rubRateSell, description;
    private TextView updateDateTextView;
    private LinearLayout buySellTitles, buySellUSD, buySellEUR, buySellRUB;
    private Button sourceButton;
    private int currentSource;
    private Uri uriToSource;
    private long updateDate;
    private boolean isSingleRate = true;
    private String shareString;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.exchange_rate_fragment, container, false);
        usdTextView = (TextView) rootView.findViewById(R.id.usd_rate);
        eurTextView = (TextView) rootView.findViewById(R.id.eur_rate);
        rubTextView = (TextView) rootView.findViewById(R.id.rub_rate);

        updateDateTextView = (TextView) rootView.findViewById(R.id.updated);
        description = (TextView) rootView.findViewById(R.id.description);
        sourceButton = (Button) rootView.findViewById(R.id.link_for_resource);

        buySellTitles = (LinearLayout) rootView.findViewById(R.id.buy_sell_titles);

        buySellUSD = (LinearLayout) rootView.findViewById(R.id.buy_sell_usd);
        usdRateBuy = (TextView) buySellUSD.findViewById(R.id.usd_rate_buy);
        usdRateSell = (TextView) buySellUSD.findViewById(R.id.usd_rate_sell);

        buySellEUR = (LinearLayout) rootView.findViewById(R.id.buy_sell_eur);
        eurRateBuy = (TextView) buySellEUR.findViewById(R.id.eur_rate_buy);
        eurRateSell = (TextView) buySellEUR.findViewById(R.id.eur_rate_sell);

        buySellRUB = (LinearLayout) rootView.findViewById(R.id.buy_sell_rub);
        rubRateBuy = (TextView) buySellRUB.findViewById(R.id.rub_rate_buy);
        rubRateSell = (TextView) buySellRUB.findViewById(R.id.rub_rate_sell);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sourceButton.setOnClickListener(onClickListener);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(EXCHANGE_RATE_LOADER_ID, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        switchSource(Utility.getPreferredSource());
        toggleLayout();
        loadData();
    }

    private void toggleLayout() {
        //TODO simplify select of correct table!
        if (isSingleRate) {
            usdTextView.setVisibility(View.VISIBLE);
            eurTextView.setVisibility(View.VISIBLE);
            rubTextView.setVisibility(View.VISIBLE);
            buySellUSD.setVisibility(View.GONE);
            buySellEUR.setVisibility(View.GONE);
            buySellRUB.setVisibility(View.GONE);
            buySellTitles.setVisibility(View.GONE);
        } else {
            usdTextView.setVisibility(View.GONE);
            eurTextView.setVisibility(View.GONE);
            rubTextView.setVisibility(View.GONE);
            buySellUSD.setVisibility(View.VISIBLE);
            buySellEUR.setVisibility(View.VISIBLE);
            buySellRUB.setVisibility(View.VISIBLE);
            buySellTitles.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.exchange_rate_fragment_menu, menu);
        MenuItem shareItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        if (shareString != null) {
            mShareActionProvider.setShareIntent(getShareIntent());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_refresh:
                loadData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.link_for_resource:
                    if (uriToSource != null) {
                        Intent goToLink = new Intent(Intent.ACTION_VIEW, uriToSource);
                        startActivity(goToLink);
                    }
                    break;
            }
        }
    };

    private void switchSource(int sourceNumber) {
        currentSource = sourceNumber;
        switch (currentSource){
            case PRIVATE:
                isSingleRate = false;
                uriToSource = Uri.parse("https://privatbank.ua");
                sourceButton.setText(getString(R.string.pref_private_label));
                break;
            case MIN_FIN:
                isSingleRate = false;
                uriToSource = Uri.parse("http://www.minfin.com.ua/currency/");
                sourceButton.setText(getString(R.string.minfin_label));
                break;
            case JSON_RATES:
                isSingleRate = true;
                uriToSource = null;
                sourceButton.setText(getString(R.string.pref_jsonrates_label));
                break;
            case OPEN_EXCHANGE_RATES:
                isSingleRate = true;
                uriToSource = null;
                sourceButton.setText(getString(R.string.pref_openexchangerates_label));
                break;
            case FINANCE:
                isSingleRate = false;
                uriToSource = Uri.parse("http://finance.ua/ru/currency");
                sourceButton.setText(getString(R.string.finance_label));
                break;
        }
    }

    private String[] prepareLinks(){
        switch (currentSource){
            case PRIVATE:
                return new String[]{PRIVATE_CASH_REQUEST};
            case MIN_FIN:
                return new String[]{INTERBANK_BANKS_REQUEST};
            case JSON_RATES:
                return new String[]{JSON_RATES_REQUEST};
            case OPEN_EXCHANGE_RATES:
                return new String[]{OPEN_EXCHANGE_RATES_REQUEST};
            case FINANCE:
                return new String[]{FINANCE_REQUEST};
            default:
                return null;
        }
    }

    private void loadData(){
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
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
                    return buffer.toString();

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(Constants.LOG_TAG, "Error ", e);
                }  finally {
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

            @Override
            protected void onPostExecute(String s) {
                if (s != null) {
                    try {
                        parseJson(s);
                    } catch (JSONException e) {
                        Log.e(Constants.LOG_TAG, e.getMessage(), e);
                        e.printStackTrace();
                    }
                } else if (getActivity() != null && !isConnectedToInternet()) {
                    Toast.makeText(getActivity(), "Check your Internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(prepareLinks());
    }

    private void parseJson(String jsonString) throws JSONException {

        //openexchangerates.org structure:
        String timestamp = "timestamp";
        String rates = "rates";
        //inside 'rates'
        String uah = "UAH";
        String eur = "EUR";
        String rub = "RUB";
        //USD is always BASE
        String usd = "USD";

        //jsonrates.com structure:
        String utctime = "utctime";

        //private structure:
        String buy = "buy";
        String sale = "sale";

        //minfin structure:
        String bid = "bid";
        String ask = "ask";

        //finance.ua structure
        String data = "date"; //UTC
        String organizations = "organizations";
        String orgType = "orgType";
        //orgType: 1 - banks
        //orgType: 2 - exchange office
        //TODO provide choosing banks / exchange office
        String currencies = "currencies";
        //ask
        //bid


        double usdRate = 0;
        double eurRate = 0;
        double rubRate = 0;

        try {
            switch (currentSource) {
                //TODO replace updating UI to inserting into DB via ContentProvider (ContentResolver)
                case PRIVATE:
                    JSONArray privateJsonArray = new JSONArray(jsonString);
                    JSONObject rubJson = privateJsonArray.getJSONObject(0);
                    JSONObject eurJson = privateJsonArray.getJSONObject(1);
                    JSONObject usdJson = privateJsonArray.getJSONObject(2);
                    setBuySaleRates(
                            usdJson.getDouble(buy),
                            usdJson.getDouble(sale),
                            eurJson.getDouble(buy),
                            eurJson.getDouble(sale),
                            rubJson.getDouble(buy),
                            rubJson.getDouble(sale)
                            );
                    updateDate = System.currentTimeMillis();
                    setUpdateDate(formatDate(updateDate));
                    setDescription(getString(R.string.private_description_cash));
                    break;

                case MIN_FIN:
                    JSONObject interBankJson = new JSONObject(jsonString);
                    JSONObject usdInterBankJson = interBankJson.getJSONObject(usd.toLowerCase());
                    JSONObject eurInterBankJson = interBankJson.getJSONObject(eur.toLowerCase());
                    JSONObject rubInterBankJson = interBankJson.getJSONObject(rub.toLowerCase());
                    setBuySaleRates(
                            usdInterBankJson.getDouble(bid),
                            usdInterBankJson.getDouble(ask),
                            eurInterBankJson.getDouble(bid),
                            eurInterBankJson.getDouble(ask),
                            rubInterBankJson.getDouble(bid),
                            rubInterBankJson.getDouble(ask)
                    );
                    updateDate = System.currentTimeMillis();
                    setUpdateDate(formatDate(updateDate));
                    setDescription("");
                    break;

                case JSON_RATES:
                    JSONObject wholeJson = new JSONObject(jsonString);
                    String receivedUTCTime = wholeJson.getString(utctime);
                    JSONObject ratesJson = wholeJson.getJSONObject(rates);
                    usdRate = ratesJson.getDouble(uah);
                    eurRate = ratesJson.getDouble(eur);
                    rubRate = ratesJson.getDouble(rub);
                    setRates(usdRate, usdRate / eurRate, usdRate / rubRate);
                    setUpdateDate(formatUTCDate((receivedUTCTime)));
                    updateDate = Utility.getTimeFromUTCDate(receivedUTCTime);
                    setDescription("");
                    break;

                case OPEN_EXCHANGE_RATES:
                    JSONObject openExchangeRatesWholeJson = new JSONObject(jsonString);
                    long receivedTimestamp = openExchangeRatesWholeJson.getLong(timestamp) * 1000;
                    JSONObject openExchangeRatesJson = openExchangeRatesWholeJson.getJSONObject(rates);
                    usdRate = openExchangeRatesJson.getDouble(uah);
                    eurRate = openExchangeRatesJson.getDouble(eur);
                    rubRate = openExchangeRatesJson.getDouble(rub);
                    setRates(usdRate, usdRate / eurRate, usdRate / rubRate);
                    setUpdateDate(formatDate(receivedTimestamp));
                    updateDate = receivedTimestamp;
                    setDescription("");
                    break;

                case FINANCE:
                    JSONObject financeWholeJson = new JSONObject(jsonString);
                    JSONArray banksOfUkraine = financeWholeJson.getJSONArray(organizations);
                    double usdRateSell = 0;
                    double eurRateSell = 0;
                    double rubRateSell = 0;
                    int banksCounter = 0;
                    for (int i=0; i < banksOfUkraine.length(); i++) {
                        JSONObject bank = banksOfUkraine.getJSONObject(i);
                        if (bank.getInt(orgType) == 1) {
                            banksCounter++;
                            JSONObject currenciesJson = bank.getJSONObject(currencies);
                            JSONObject usdFinanceJson = currenciesJson.getJSONObject(usd);
                            JSONObject eurFinanceJson = currenciesJson.getJSONObject(eur);
                            JSONObject rubFinanceJson = currenciesJson.getJSONObject(rub);
                            usdRate += usdFinanceJson.getDouble(bid);
                            usdRateSell += usdFinanceJson.getDouble(ask);
                            eurRate += eurFinanceJson.getDouble(bid);
                            eurRateSell += eurFinanceJson.getDouble(ask);
                            rubRate += rubFinanceJson.getDouble(bid);
                            rubRateSell += rubFinanceJson.getDouble(ask);
                        }
                    }
                    if (banksCounter == 0) {
                        banksCounter = 1; //in case of incorrect json
                    }
                    setBuySaleRates(
                            usdRate/banksCounter,
                            usdRateSell/banksCounter,
                            eurRate/banksCounter,
                            eurRateSell/banksCounter,
                            rubRate/banksCounter,
                            rubRateSell/banksCounter
                    );
                    String jsonUpdateDate = financeWholeJson.getString(data);
                    setUpdateDate(formatUTCDate(jsonUpdateDate));
                    updateDate = Utility.getTimeFromUTCDate(jsonUpdateDate);
                    setDescription(getString(R.string.finance_description_average));
                    break;
            }

        } catch (JSONException e) {
            Log.e(Constants.LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void setRates(double usd, double eur, double rub) {
        usdTextView.setText(String.format("%.2f", usd));
        eurTextView.setText(String.format("%.2f", eur));
        rubTextView.setText(String.format("%.2f", rub));
        shareString = String.format("USD: %.2f EUR: %.2f RUB: %.2f", usd, eur, rub);
    }

    private void setBuySaleRates(double usdBuy, double usdSell, double eurBuy, double eurSell, double rubBuy, double rubSell) {
        usdRateBuy.setText(String.format("%.2f", usdBuy));
        usdRateSell.setText(String.format("%.2f", usdSell));
        eurRateBuy.setText(String.format("%.2f", eurBuy));
        eurRateSell.setText(String.format("%.2f", eurSell));
        rubRateBuy.setText(String.format("%.2f", rubBuy));
        rubRateSell.setText(String.format("%.2f", rubSell));
        shareString = String.format("USD: %s-%.2f, %s-%.2f EUR: %s-%.2f, %s-%.2f RUB: %s-%.2f, %s-%.2f",
                getString(R.string.buy), usdBuy,
                getString(R.string.sell), usdSell,
                getString(R.string.buy), eurBuy,
                getString(R.string.sell), eurSell,
                getString(R.string.buy), rubBuy,
                getString(R.string.sell), rubSell
        );
    }

    public static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateTimeInstance().format(date);
    }

    public static String formatUTCDate(String UTCDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Date date;
        try {
            date = dateFormat.parse(UTCDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        return DateFormat.getDateTimeInstance().format(date);
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        return net != null && net.isAvailable() && net.isConnected();
    }

    private void setDescription(String descriptionStr) {
        if (descriptionStr.length() < 1) {
            description.setVisibility(View.GONE);
        } else {
            description.setVisibility(View.VISIBLE);
            description.setText(descriptionStr);
        }
    }

    private void setUpdateDate(String updateDate) {
        if (updateDate.length() < 1) {
            updateDateTextView.setVisibility(View.GONE);
        } else {
            updateDateTextView.setVisibility(View.VISIBLE);
            updateDateTextView.setText(updateDate);
        }
    }

    private Intent getShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareString + SHARE_TAG);
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        int sourceId = Utility.getPreferredSource();
//        Uri uri;
//        String[] projection;
//        switch (sourceId) {
//            case JSON_RATES:
//            case OPEN_EXCHANGE_RATES:
//                uri = RateContract.RateEntry.buildRateSourceIdWithDate(sourceId, updateDate);
//                projection =  Utility.RATE_COLUMNS;
//                break;
//            default:
//                uri = RateContract.DoubleRateEntry.buildDoubleRateSourceIdWithDate(sourceId, updateDate);
//                projection = Utility.DOUBLE_RATE_COLUMNS;
//                break;
//        }
        //TODO simplify select of correct table!
        Uri uri = isSingleRate ?
                RateContract.RateEntry.buildRateSourceIdWithDate(sourceId, updateDate) :
                RateContract.DoubleRateEntry.buildDoubleRateSourceIdWithDate(sourceId, updateDate);

        String[] projection = isSingleRate ?
                Utility.RATE_COLUMNS : Utility.DOUBLE_RATE_COLUMNS;

        return new CursorLoader(
                getActivity(),
                uri,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }
        setUpdateDate(formatDate(data.getLong(Utility.COL_DATE)));
        if (isSingleRate) {
            setRates(
                    data.getDouble(Utility.COL_RATE_USD),
                    data.getDouble(Utility.COL_RATE_EUR),
                    data.getDouble(Utility.COL_RATE_RUB)
            );
        } else {
            setBuySaleRates(
                    data.getDouble(Utility.COL_DOUBLE_RATE_USD_BUY),
                    data.getDouble(Utility.COL_DOUBLE_RATE_USD_SELL),
                    data.getDouble(Utility.COL_DOUBLE_RATE_EUR_BUY),
                    data.getDouble(Utility.COL_DOUBLE_RATE_EUR_SELL),
                    data.getDouble(Utility.COL_DOUBLE_RATE_RUB_BUY),
                    data.getDouble(Utility.COL_DOUBLE_RATE_RUB_SELL)
            );
        }

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(getShareIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}
