package edu.my.rstetsenko.zenua.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
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

import edu.my.rstetsenko.zenua.Constants;
import edu.my.rstetsenko.zenua.FetchRateTask;
import edu.my.rstetsenko.zenua.R;
import edu.my.rstetsenko.zenua.Utility;
import edu.my.rstetsenko.zenua.data.RateBaseColumns;
import edu.my.rstetsenko.zenua.data.RateContract;

public class ExchangeRateFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String OPEN_EXCHANGE_RATES_REQUEST = "http://openexchangerates.org/api/latest.json?app_id=60acbc550c654afd86eea4304cdec3f0";
    private static final String JSON_RATES_REQUEST= "http://jsonrates.com/get/?%20base=USD&apiKey=jr-878f7938dc3db294f030a675358a2ed9";
//    private static final String PRIVATE_NBU_REQUEST= "https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=3";
    private static final String PRIVATE_CASH_REQUEST= "https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=5";
//    private static final String PRIVATE_NON_CASH_REQUEST= "https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=11";
//    private static final String INTERBANK_REQUEST= "http://api.minfin.com.ua/mb/4d18fc9525f199ed8ba09a535fe3367b6e3c39f1/"; //MINFIN api, not works
    private static final String INTERBANK_BANKS_REQUEST= "http://api.minfin.com.ua/summary/4d18fc9525f199ed8ba09a535fe3367b6e3c39f1/"; //banks info from MINFIN
//    private static final String CURRENCY_AUCTION_REQUEST= "http://api.minfin.com.ua/auction/info/4d18fc9525f199ed8ba09a535fe3367b6e3c39f1/"; // MINFIN Currency auction
    private static final String FINANCE_REQUEST= "http://resources.finance.ua/ua/public/currency-cash.json";

    private static final String SHARE_TAG = " #ZenUA";
    private static final int EXCHANGE_RATE_LOADER_ID = 0;

    private ShareActionProvider mShareActionProvider;

    private TextView usdTextView;
    private TextView eurTextView;
    private TextView rubTextView;
    private TextView usdRateBuy, usdRateSell, eurRateBuy, eurRateSell, rubRateBuy, rubRateSell, description;
    private TextView updateDateTextView;
    private LinearLayout buySellTitles, buySellUSD, buySellEUR, buySellRUB;
    private Button sourceButton;
    private int currentSource;
    private Uri uriToSource;
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
    }

    private void toggleLayout() {
        if (Constants.singleRates.contains(currentSource)) {
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
            case Constants.PRIVATE:
                uriToSource = Uri.parse("https://privatbank.ua");
                sourceButton.setText(getString(R.string.pref_private_label));
                break;
            case Constants.MIN_FIN:
                uriToSource = Uri.parse("http://www.minfin.com.ua/currency/");
                sourceButton.setText(getString(R.string.minfin_label));
                break;
            case Constants.JSON_RATES:
                uriToSource = null;
                sourceButton.setText(getString(R.string.pref_jsonrates_label));
                break;
            case Constants.OPEN_EXCHANGE_RATES:
                uriToSource = null;
                sourceButton.setText(getString(R.string.pref_openexchangerates_label));
                break;
            case Constants.FINANCE:
                uriToSource = Uri.parse("http://finance.ua/ru/currency");
                sourceButton.setText(getString(R.string.finance_label));
                break;
        }
    }

    private String[] prepareLinks(){
        switch (currentSource){
            case Constants.PRIVATE:
                return new String[]{PRIVATE_CASH_REQUEST};
            case Constants.MIN_FIN:
                return new String[]{INTERBANK_BANKS_REQUEST};
            case Constants.JSON_RATES:
                return new String[]{JSON_RATES_REQUEST};
            case Constants.OPEN_EXCHANGE_RATES:
                return new String[]{OPEN_EXCHANGE_RATES_REQUEST};
            case Constants.FINANCE:
                return new String[]{FINANCE_REQUEST};
            default:
                return null;
        }
    }

    private void loadData(){
        if (getActivity() != null && !isConnectedToInternet()) {
            Toast.makeText(getActivity(), "Check your Internet connection", Toast.LENGTH_SHORT).show();
        } else {
            new FetchRateTask(getActivity()).execute(prepareLinks());
        }
    }

    private void setRate(double usd, double eur, double rub) {
        usdTextView.setText(String.format("%.2f", usd));
        eurTextView.setText(String.format("%.2f", eur));
        rubTextView.setText(String.format("%.2f", rub));
        shareString = String.format("USD: %.2f EUR: %.2f RUB: %.2f", usd, eur, rub);
    }

    private void setDoubleRate(double usdBuy, double usdSell, double eurBuy, double eurSell, double rubBuy, double rubSell) {
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
        Uri uri = Constants.singleRates.contains(sourceId) ?
                RateContract.RateEntry.buildRateSourceIdWithLastDate(sourceId):
                RateContract.DoubleRateEntry.buildDoubleRateSourceIdWithLastDate(sourceId);

        String[] projection = Constants.singleRates.contains(sourceId) ?
                Utility.RATE_COLUMNS : Utility.DOUBLE_RATE_COLUMNS;

        String sortOrder = RateBaseColumns.COLUMN_DATE + " DESC";

        return new CursorLoader(
                getActivity(),
                uri,
                projection,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }
        setUpdateDate(Utility.formatDate(data.getLong(Utility.COL_DATE)));
        int sourceId = data.getInt(Utility.COL_SOURCE_ID);
        if (Constants.singleRates.contains(sourceId)) {
            setRate(
                    data.getDouble(Utility.COL_RATE_USD),
                    data.getDouble(Utility.COL_RATE_EUR),
                    data.getDouble(Utility.COL_RATE_RUB)
            );
        } else {
            setDoubleRate(
                    data.getDouble(Utility.COL_DOUBLE_RATE_USD_BUY),
                    data.getDouble(Utility.COL_DOUBLE_RATE_USD_SELL),
                    data.getDouble(Utility.COL_DOUBLE_RATE_EUR_BUY),
                    data.getDouble(Utility.COL_DOUBLE_RATE_EUR_SELL),
                    data.getDouble(Utility.COL_DOUBLE_RATE_RUB_BUY),
                    data.getDouble(Utility.COL_DOUBLE_RATE_RUB_SELL)
            );
        }

        switch (sourceId){
            case Constants.PRIVATE:
                setDescription(getString(R.string.private_description_cash));
                break;
            case Constants.MIN_FIN:
            case Constants.JSON_RATES:
            case Constants.OPEN_EXCHANGE_RATES:
                setDescription("");
                break;
            case Constants.FINANCE:
                setDescription(getString(R.string.finance_description_average));
                break;
        }

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(getShareIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    public void onSourceChanged() {
        loadData();
        getLoaderManager().restartLoader(EXCHANGE_RATE_LOADER_ID, null, this);
    }
}
