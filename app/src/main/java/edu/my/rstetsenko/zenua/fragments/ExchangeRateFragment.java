package edu.my.rstetsenko.zenua.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
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

public class ExchangeRateFragment extends Fragment {
    private static final String OPEN_EXCHANGE_RATES_REQUEST = "http://openexchangerates.org/api/latest.json?app_id=60acbc550c654afd86eea4304cdec3f0";
    private static final String JSON_RATES_REQUEST= "http://jsonrates.com/get/?%20base=USD&apiKey=jr-878f7938dc3db294f030a675358a2ed9";
    private static final String PRIVATE_NBU_REQUEST= "https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=3";
    private static final String PRIVATE_CASH_REQUEST= "https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=5";
    private static final String PRIVATE_NON_CASH_REQUEST= "https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=11";
    private static final String KEY_CURRENT_SOURCE = "key_current_source";

    private static final int PRIVATE = 0;
    private static final int MIN_FIN = 1;
    private static final int JSON_RATES = 2;
    private static final int OPEN_EXCHANGE_RATES = 3;
    private static final int FINANCE = 4;

    private TextView usdTextView;
    private TextView eurTextView;
    private TextView rubTextView;
    private TextView updateDateTextView;
    private Button sourceButton;
    private int currentSource;
    private Uri uriToSource;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        switchSource(Utility.getPreferredSource(getActivity()));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.exchange_rate_fragment_menu, menu);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.exchange_rate_fragment, container, false);
        usdTextView = (TextView) rootView.findViewById(R.id.usd_rate);
        eurTextView = (TextView) rootView.findViewById(R.id.eur_rate);
        rubTextView = (TextView) rootView.findViewById(R.id.rub_rate);
        updateDateTextView = (TextView) rootView.findViewById(R.id.updated);
        sourceButton = (Button) rootView.findViewById(R.id.link_for_resource);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        sourceButton.setOnClickListener(onClickListener);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            currentSource = savedInstanceState.getInt(KEY_CURRENT_SOURCE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_SOURCE, currentSource);
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
                uriToSource = Uri.parse("https://privatbank.ua");
                sourceButton.setText(getString(R.string.pref_private_label));
                break;
            case MIN_FIN:
                uriToSource = Uri.parse("http://www.minfin.com.ua/currency/");
                sourceButton.setText(getString(R.string.minfin_label));
                break;
            case JSON_RATES:
                uriToSource = null;
                sourceButton.setText(getString(R.string.pref_jsonrates_label));
                break;
            case OPEN_EXCHANGE_RATES:
                uriToSource = null;
                sourceButton.setText(getString(R.string.pref_openexchangerates_label));
                break;
            case FINANCE:
                uriToSource = Uri.parse("http://finance.ua/ru/currency");
                sourceButton.setText(getString(R.string.finance_label));
                break;
        }
    }

    private String[] prepareLinks(){
        switch (currentSource){
            case PRIVATE:
                return null;
            case MIN_FIN:
                return null;
            case JSON_RATES:
                return new String[]{JSON_RATES_REQUEST};
            case OPEN_EXCHANGE_RATES:
                return new String[]{OPEN_EXCHANGE_RATES_REQUEST};
            case FINANCE:
                return null;
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
//        String usd = "USD";

        //jsonrates.com structure:
        String utctime = "utctime";

        double uahRate;
        double eurRate;
        double rubRate;

        try {
            switch (currentSource) {
                case PRIVATE:
                    updateDateTextView.setText("");
                    break;

                case MIN_FIN:
                    updateDateTextView.setText("");
                    break;

                case JSON_RATES:
                    JSONObject wholeJson = new JSONObject(jsonString);
                    String receivedUTCTime = wholeJson.getString(utctime);
                    JSONObject ratesJson = wholeJson.getJSONObject(rates);
                    uahRate = ratesJson.getDouble(uah);
                    eurRate = ratesJson.getDouble(eur);
                    rubRate = ratesJson.getDouble(rub);
                    setRates(
                            String.format("%.2f", uahRate),
                            String.format("%.2f", uahRate / eurRate),
                            String.format("%.2f", uahRate / rubRate)
                    );
                    updateDateTextView.setText(formatUTCDate((receivedUTCTime)));
                    break;

                case OPEN_EXCHANGE_RATES:
                    JSONObject openExchangeRatesWholeJson = new JSONObject(jsonString);
                    long receivedTimestamp = openExchangeRatesWholeJson.getLong(timestamp) * 1000;
                    JSONObject openExchangeRatesJson = openExchangeRatesWholeJson.getJSONObject(rates);
                    uahRate = openExchangeRatesJson.getDouble(uah);
                    eurRate = openExchangeRatesJson.getDouble(eur);
                    rubRate = openExchangeRatesJson.getDouble(rub);
                    setRates(
                            String.format("%.2f", uahRate),
                            String.format("%.2f", uahRate / eurRate),
                            String.format("%.2f", uahRate / rubRate)
                    );
                    updateDateTextView.setText(formatDate(receivedTimestamp));
                    break;

                case FINANCE:
                    updateDateTextView.setText("");
                    break;
            }

        } catch (JSONException e) {
            Log.e(Constants.LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void setRates(String usd, String eur, String rub) {
        usdTextView.setText(usd);
        eurTextView.setText(eur);
        rubTextView.setText(rub);
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

}
