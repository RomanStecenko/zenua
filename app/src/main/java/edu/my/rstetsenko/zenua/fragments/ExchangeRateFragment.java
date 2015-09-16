package edu.my.rstetsenko.zenua.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import edu.my.rstetsenko.zenua.Constants;
import edu.my.rstetsenko.zenua.R;
import edu.my.rstetsenko.zenua.Utility;
import edu.my.rstetsenko.zenua.activities.MainActivity;
import edu.my.rstetsenko.zenua.activities.RateProgressActivity;
import edu.my.rstetsenko.zenua.data.RateBaseColumns;
import edu.my.rstetsenko.zenua.data.RateContract;
import edu.my.rstetsenko.zenua.sync.ZenUaSyncAdapter;

public class ExchangeRateFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String SHARE_TAG = " #ZenUA";
    private static final String LINK_OF_APP_ON_PLAY_MARKET = "https://play.google.com/store/apps";
    private static final int EXCHANGE_RATE_LOADER_ID = 0;
    private static final long CONVERTER_LIMIT = 1000000000000L;

    private ShareActionProvider mShareActionProvider;

    private TextView usdTextView, eurTextView, rubTextView;
    private TextView usdRateBuy, usdRateSell, eurRateBuy, eurRateSell, rubRateBuy, rubRateSell, description;
    private TextView updateDateTextView;
    private LinearLayout buySellTitles, buySellUSD, buySellEUR, buySellRUB;
    private Button sourceButton;
    private Uri uriToSource;
    private String shareString;
    private View converterView, converterSellLayout;
    private TextView converterUsd, converterEur, converterRub, converterUsdSell, converterEurSell, converterRubSell, converterBuyTitle, converterPlaceholder;
    private EditText converterEditText;
    private boolean singleRate;
    private NumberFormat fmt;

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
        int paddingTop = rootView.getPaddingTop();
        if (ViewConfiguration.get(getActivity()).hasPermanentMenuKey()) {
            rootView.setPadding(0, paddingTop, 0, 0);
        } else {
            rootView.setPadding(0, paddingTop, 0, paddingTop);
        }
        rootView.setOnClickListener(onClickListener);
        converterView = rootView.findViewById(R.id.converter);
        if (converterView != null) {
            converterSellLayout = converterView.findViewById(R.id.converter_sell_layout);
            converterUsd = (TextView) converterView.findViewById(R.id.usd_result);
            converterEur = (TextView) converterView.findViewById(R.id.eur_result);
            converterRub = (TextView) converterView.findViewById(R.id.rub_result);
            converterUsdSell = (TextView) converterSellLayout.findViewById(R.id.usd_result_sell);
            converterEurSell = (TextView) converterSellLayout.findViewById(R.id.eur_result_sell);
            converterRubSell = (TextView) converterSellLayout.findViewById(R.id.rub_result_sell);
            converterBuyTitle = (TextView) converterView.findViewById(R.id.converter_buy_title);
            converterPlaceholder = (TextView) converterView.findViewById(R.id.converter_placeholder);
            converterEditText = (EditText) converterView.findViewById(R.id.converter_edit_text);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sourceButton.setOnClickListener(onClickListener);
        if (converterView != null) {
            fmt = NumberFormat.getNumberInstance(Locale.US);
            converterEditText.setText("1");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        getLoaderManager().initLoader(EXCHANGE_RATE_LOADER_ID, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        switchSource(Utility.getPreferredSource());
        toggleActionBar();
        if (!Utility.isConnectedToInternet(getActivity())) {
            Toast.makeText(getActivity(), getActivity().getString(R.string.check_internet_connection), Toast.LENGTH_LONG).show();
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
//        switch (item.getItemId()){
//            case R.id.action_refresh:
//                loadData();
//                return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.link_for_resource:
//                    if (uriToSource != null) {
//                        Intent goToLink = new Intent(Intent.ACTION_VIEW, uriToSource);
//                        startActivity(goToLink);
//                    }
                    startActivity(new Intent(getActivity(), RateProgressActivity.class));
                    break;
                default:
                    Utility.toggleActionBarPreference();
                    toggleActionBar();
                    break;
            }
        }
    };

    private void switchSource(int sourceNumber) {
        switch (sourceNumber) {
            case Constants.PRIVATE:
                uriToSource = Uri.parse("https://privatbank.ua");
                break;
            case Constants.MIN_FIN:
                uriToSource = Uri.parse("http://www.minfin.com.ua/currency/");
                break;
            case Constants.CURRENCYLAYER:
                uriToSource = null;
                break;
            case Constants.OPEN_EXCHANGE_RATES:
                uriToSource = null;
                break;
            case Constants.FINANCE:
                uriToSource = Uri.parse("http://finance.ua/ru/currency");
                break;
        }
        sourceButton.setText(String.format("%s %s",getString(R.string.chart_of), Utility.getSourceName(sourceNumber)));
        toggleLayout(sourceNumber);
    }

    private void toggleLayout(int sourceNumber) {
        singleRate = Constants.singleRates.contains(sourceNumber);
        if (singleRate) {
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
        if (converterView != null) {
            toggleConverterLayout();
        }
    }

    private void toggleConverterLayout() {
        if (singleRate) {
            converterPlaceholder.setVisibility(View.GONE);
            converterBuyTitle.setVisibility(View.GONE);
            converterSellLayout.setVisibility(View.GONE);
        } else {
            converterPlaceholder.setVisibility(View.INVISIBLE);
            converterBuyTitle.setVisibility(View.VISIBLE);
            converterSellLayout.setVisibility(View.VISIBLE);
        }
    }

    private void toggleActionBar() {
        if (Utility.isFullScreen()) {
            ((MainActivity)getActivity()).getSupportActionBar().hide();
        } else {
            ((MainActivity)getActivity()).getSupportActionBar().show();
        }
    }

    private void loadData(){
//        if (getActivity() != null && !isConnectedToInternet()) {
//            Toast.makeText(getActivity(), "Check your Internet connection", Toast.LENGTH_SHORT).show();
//        } else {
//            new FetchRateTask(getActivity()).execute(prepareLinks());

//            Intent intent = new Intent(getActivity(), MyService.class);
//            intent.putExtra(Constants.EXTRA_SOURCE, prepareLinks());
//            getActivity().startService(intent);

//            Intent alarmIntent = new Intent(getActivity(), MyService.AlarmReceiver.class);
//            alarmIntent.putExtra(Constants.EXTRA_SOURCE, prepareLinks());
//            PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, alarmIntent, PendingIntent.FLAG_ONE_SHOT);//getBroadcast(context, 0, i, 0);
//            AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
//            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pi);

            ZenUaSyncAdapter.syncImmediately(getActivity());
//        }
    }

    private void setRate(double usd, double eur, double rub) {
        usdTextView.setText(String.format(Locale.US, "%.2f", usd));
        eurTextView.setText(String.format(Locale.US, "%.2f", eur));
        rubTextView.setText(String.format(Locale.US, "%.2f", rub));
        shareString = Utility.prepareRateDescriptionString(usd, eur, rub);
    }

    private void setDoubleRate(double usdBuy, double usdSell, double eurBuy, double eurSell, double rubBuy, double rubSell) {
        usdRateBuy.setText(String.format(Locale.US, "%.2f", usdBuy));
        usdRateSell.setText(String.format(Locale.US, "%.2f", usdSell));
        eurRateBuy.setText(String.format(Locale.US, "%.2f", eurBuy));
        eurRateSell.setText(String.format(Locale.US, "%.2f", eurSell));
        rubRateBuy.setText(String.format(Locale.US, "%.2f", rubBuy));
        rubRateSell.setText(String.format(Locale.US, "%.2f", rubSell));
        shareString = Utility.prepareDoubleRateDescriptionString(usdBuy, usdSell, eurBuy, eurSell, rubBuy, rubSell);
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
//        shareIntent.setType("image/*");
//        Uri uri = Uri.parse("android.resource://"+getActivity().getPackageName() + "/" + R.mipmap.ic_launcher);
//        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareString + SHARE_TAG);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, LINK_OF_APP_ON_PLAY_MARKET);
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
        setDescription(Utility.getDescription(sourceId));

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(getShareIntent());
        }
        try {
            initConverterValues();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    public void onSourceChanged() {
        loadData();
        getLoaderManager().restartLoader(EXCHANGE_RATE_LOADER_ID, null, this);
    }

    private void initConverterValues() throws ParseException {
        if (converterView != null) {
            converterView.setVisibility(View.VISIBLE);
            final double usrRateValue;
            final double eurRateValue;
            final double rubRateValue;
            final double usdRateValueSell;
            final double eurRateValueSell;
            final double rubRateValueSell;

                if (singleRate) {
                    usrRateValue = fmt.parse(usdTextView.getText().toString()).doubleValue();
                    eurRateValue = fmt.parse(eurTextView.getText().toString()).doubleValue();
                    rubRateValue = fmt.parse(rubTextView.getText().toString()).doubleValue();
                    usdRateValueSell = 0;
                    eurRateValueSell = 0;
                    rubRateValueSell = 0;
                } else {
                    usrRateValue = fmt.parse(usdRateBuy.getText().toString()).doubleValue();
                    eurRateValue = fmt.parse(eurRateBuy.getText().toString()).doubleValue();
                    rubRateValue = fmt.parse(rubRateBuy.getText().toString()).doubleValue();
                    usdRateValueSell = fmt.parse(usdRateSell.getText().toString()).doubleValue();
                    eurRateValueSell = fmt.parse(eurRateSell.getText().toString()).doubleValue();
                    rubRateValueSell = fmt.parse(rubRateSell.getText().toString()).doubleValue();
                }
            converterUsd.setText(String.format(Locale.US, "%.2f", 1 / usrRateValue));
            converterEur.setText(String.format(Locale.US, "%.2f", 1 / eurRateValue));
            converterRub.setText(String.format(Locale.US, "%.2f", 1 / rubRateValue));
            if (!singleRate) {
                converterUsdSell.setText(String.format(Locale.US, "%.2f", 1 / usdRateValueSell));
                converterEurSell.setText(String.format(Locale.US, "%.2f", 1 / eurRateValueSell));
                converterRubSell.setText(String.format(Locale.US, "%.2f", 1 / rubRateValueSell));
            }
            converterEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() > 0) {
                        double value = 1;
                        try {
                            value = fmt.parse(s.toString()).doubleValue();
                            if (value <= 0) {
                                value = 1;
                            }
                            if (value > CONVERTER_LIMIT) {
                                value = CONVERTER_LIMIT;
                                converterEditText.setText(String.valueOf(CONVERTER_LIMIT));
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        converterUsd.setText(String.format(Locale.US, "%.2f", value / usrRateValue));
                        converterEur.setText(String.format(Locale.US, "%.2f", value / eurRateValue));
                        converterRub.setText(String.format(Locale.US, "%.2f", value / rubRateValue));
                        if (!singleRate) {
                            converterUsdSell.setText(String.format(Locale.US, "%.2f", value / usdRateValueSell));
                            converterEurSell.setText(String.format(Locale.US, "%.2f", value / eurRateValueSell));
                            converterRubSell.setText(String.format(Locale.US, "%.2f", value / rubRateValueSell));
                        }
                    }
                }
            });
        }
    }
}
