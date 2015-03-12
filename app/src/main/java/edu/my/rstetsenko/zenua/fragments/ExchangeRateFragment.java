package edu.my.rstetsenko.zenua.fragments;

import android.app.Notification;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.my.rstetsenko.zenua.Constants;
import edu.my.rstetsenko.zenua.R;

public class ExchangeRateFragment extends Fragment {
    private static final String OPEN_EXCHANGE_RATES = "http://openexchangerates.org/api/latest.json?app_id=60acbc550c654afd86eea4304cdec3f0";

    private TextView usdTextView;
    private TextView eurTextView;
    private TextView rubTextView;
    private TextView updateTextView;
    private Button sourceButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        updateTextView = (TextView) rootView.findViewById(R.id.updated);
        sourceButton = (Button) rootView.findViewById(R.id.link_for_resource);
        sourceButton.setText(Constants.OPEN_EXCHANGE_RATES);
//        String locationSetting = Utility.getPreferredLocation(getActivity());
//        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
//        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis());
//        Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri, null, null, null, sortOrder);
//        forecastAdapter = new ForecastAdapter(getActivity(), cur, 0);
//        forecastAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, new ArrayList<String>());
//        ListView listView = (ListView) rootView.findViewById(R.id.listView_forecast);
//        listView.setAdapter(forecastAdapter);
//        listView.setOnItemClickListener(onItemClickListener);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        sourceButton.setOnClickListener(onClickListener);
        super.onViewCreated(view, savedInstanceState);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.link_for_resource:
                    Intent goToLink = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.OPEN_EXCHANGE_RATES_LINK));
                    startActivity(goToLink);
                    break;
            }
        }
    };

    private void loadData(){
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
//                if (params.length == 0) {
//                    return null;
//                }

                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                try {
                    //http://openexchangerates.org/api/latest.json?app_id=60acbc550c654afd86eea4304cdec3f0

//                    final String BASE_URL = "http://openexchangerates.org/api/latest.json?";
//                    final String QUERY_PARAM = "app_id";
//                    final String KEY_PARAM = "60acbc550c654afd86eea4304cdec3f0";
//
//                    Uri builtUri = Uri.parse(BASE_URL).buildUpon()
//                            .appendQueryParameter(QUERY_PARAM, KEY_PARAM).build();

                    URL url = new URL(OPEN_EXCHANGE_RATES);

                    urlConnection = (HttpURLConnection) url.openConnection();
//                    urlConnection.setRequestMethod("GET");
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
                        // Stream was empty.  No point in parsing.
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
        }.execute();
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        String location = prefs.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
//        String temperatureUnits = prefs.getString(getString(R.string.pref_temperature_units_key),getString(R.string.pref_temperature_units_default));
//        new FetchWeatherTask(getActivity()).execute(location, temperatureUnits);
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

        try {
            //openexchangerates.org structure:
            JSONObject wholeJson = new JSONObject(jsonString);

            long receivedTimestamp = wholeJson.getLong(timestamp) * 1000; //need *1000 when use openexchangerates!

            JSONObject ratesJson = wholeJson.getJSONObject(rates);
            
            double uahRate = ratesJson.getDouble(uah);
            double eurRate = ratesJson.getDouble(eur);
            double rubRate = ratesJson.getDouble(rub);
            setRates(
                    String.format("%.2f",uahRate),
                    String.format("%.2f",uahRate/eurRate),
                    String.format("%.2f",uahRate/rubRate)
            );
            updateTextView.setText(formatDate(receivedTimestamp));
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

    private boolean isConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        return net != null && net.isAvailable() && net.isConnected();
    }

}
