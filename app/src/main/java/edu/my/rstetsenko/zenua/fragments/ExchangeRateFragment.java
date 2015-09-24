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
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import edu.my.rstetsenko.zenua.Constants;
import edu.my.rstetsenko.zenua.R;
import edu.my.rstetsenko.zenua.Utility;
import edu.my.rstetsenko.zenua.activities.MainActivity;
import edu.my.rstetsenko.zenua.activities.RateProgressActivity;
import edu.my.rstetsenko.zenua.data.RateBaseColumns;
import edu.my.rstetsenko.zenua.data.RateContract;

public class ExchangeRateFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static ExchangeRateFragment newInstance(int sourceId) {
        Bundle args = new Bundle();
        args.putInt(ARG_SOURCE_ID, sourceId);
        ExchangeRateFragment fragment = new ExchangeRateFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private static final String ARG_SOURCE_ID = "arg_source_id";
    private static final String SHARE_TAG = " #ZenUA";
    private static final String LINK_OF_APP_ON_PLAY_MARKET = "https://play.google.com/store/apps";

    private ShareActionProvider mShareActionProvider;
    private TextView usdTextView, eurTextView, rubTextView;
    private TextView usdRateBuy, usdRateSell, eurRateBuy, eurRateSell, rubRateBuy, rubRateSell, description;
    private TextView updateDateTextView;
    private LinearLayout buySellTitles, buySellUSD, buySellEUR, buySellRUB;
    private Button sourceButton;
    private String shareString;
    private int mSourceId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSourceId = getArguments().getInt(ARG_SOURCE_ID);
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
        int paddingTop = rootView.getPaddingTop();
        if (ViewConfiguration.get(getActivity()).hasPermanentMenuKey()) {
            rootView.setPadding(0, paddingTop, 0, 0);
        } else {
            rootView.setPadding(0, paddingTop, 0, paddingTop);
        }
        toggleLayout();
        rootView.setOnClickListener(onClickListener);
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
        setHasOptionsMenu(true);
        getLoaderManager().initLoader(mSourceId, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        toggleActionBar();
        if (!Utility.isConnectedToInternet(getActivity())) {
            Toast.makeText(getActivity(),
                    getActivity().getString(R.string.check_internet_connection),
                    Toast.LENGTH_LONG).show();
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

    private void toggleLayout() {
        sourceButton.setText(String.format("%s %s",getString(R.string.chart_of),
                Utility.getSourceName(mSourceId)));
        boolean singleRate = Constants.singleRates.contains(mSourceId);
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
    }

    private void toggleActionBar() {
        ActionBar actionBar = ((MainActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            if (Utility.isFullScreen()) {
                actionBar.hide();
            } else {
                actionBar.show();
            }
        }
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
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareString + SHARE_TAG);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, LINK_OF_APP_ON_PLAY_MARKET);
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = Constants.singleRates.contains(mSourceId) ?
                RateContract.RateEntry.buildRateSourceIdWithLastDate(mSourceId):
                RateContract.DoubleRateEntry.buildDoubleRateSourceIdWithLastDate(mSourceId);

        String[] projection = Constants.singleRates.contains(mSourceId) ?
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
            //TODO check if TextViews are empty and show Message
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
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

//    public void updateFragmentInfo() {
////        loadData();
//        getLoaderManager().restartLoader(EXCHANGE_RATE_LOADER_ID, null, this);
//    }
//
//    private void loadData(){
//        ZenUaSyncAdapter.syncImmediately(getActivity());
//    }
}
