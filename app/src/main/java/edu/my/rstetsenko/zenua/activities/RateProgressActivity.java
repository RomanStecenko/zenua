package edu.my.rstetsenko.zenua.activities;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import edu.my.rstetsenko.zenua.Constants;
import edu.my.rstetsenko.zenua.R;
import edu.my.rstetsenko.zenua.Utility;
import edu.my.rstetsenko.zenua.data.RateBaseColumns;
import edu.my.rstetsenko.zenua.data.RateContract;

public class RateProgressActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int RATE_PROGRESS_LOADER_ID = 11;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private static final int SINGLE_RATE_CHARTS = 3;
    private static final int DOUBLE_RATE_CHARTS = 6;


    private LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_progress);
        //TODO clean code; optimization; move description below chart; legend in two lines.
        int sourceId = Utility.getPreferredSource();
        String description = Utility.getSourceName(sourceId);
        mChart = (LineChart) findViewById(R.id.chart);
        mChart.setDescription(description);
        mChart.setNoDataTextDescription("Need Internet connection to sync with source");
        mChart.setDrawGridBackground(false);
        mChart.setHighlightEnabled(true);
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setPinchZoom(false);
        getSupportLoaderManager().initLoader(RATE_PROGRESS_LOADER_ID, null, this);
    }

//                Legend l = mChart.getLegend();
//
//                // modify the legend ...
//                l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
//                l.setFormSize(10f);
//                l.setForm(Legend.LegendForm.CIRCLE);
//                l.setTextColor(Color.BLACK);
//
//                XLabels xl = mChart.getXLabels();
//                xl.setTextColor(Color.BLACK);
//
//                YLabels yl = mChart.getYLabels();
//                yl.setTextColor(Color.BLACK);

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        int sourceId = Utility.getPreferredSource();
        Uri uri = Constants.singleRates.contains(sourceId) ?
                RateContract.RateEntry.buildRateSourceId(sourceId):
                RateContract.DoubleRateEntry.buildDoubleRateSourceId(sourceId);

        String[] projection = Constants.singleRates.contains(sourceId) ?
                Utility.RATE_COLUMNS : Utility.DOUBLE_RATE_COLUMNS;

        String sortOrder = RateBaseColumns.COLUMN_DATE + " ASC";

        return new CursorLoader(
                this,
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
            Log.d(LOG_TAG, "Cursor is EMPTY!");
            return;
        }
        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<LineDataSet> dataSets = new ArrayList<>();

        ArrayList<Entry> usdValues = new ArrayList<>();
        ArrayList<Entry> eurValues = new ArrayList<>();
        ArrayList<Entry> rubValues = new ArrayList<>();

        ArrayList<Entry> usdBuyValues = new ArrayList<>();
        ArrayList<Entry> usdSellValues = new ArrayList<>();
        ArrayList<Entry> eurBuyValues = new ArrayList<>();
        ArrayList<Entry> eurSellValues = new ArrayList<>();
        ArrayList<Entry> rubBuyValues = new ArrayList<>();
        ArrayList<Entry> rubSellValues = new ArrayList<>();

        int i = 0;
        int sourceId = data.getInt(Utility.COL_SOURCE_ID);
        boolean singleRate = Constants.singleRates.contains(sourceId);
        do {
                xVals.add(Utility.getDayAndMonth(data.getLong(Utility.COL_DATE)));
                if (singleRate) {
                    usdValues.add(new Entry((float)data.getDouble(Utility.COL_RATE_USD), i));
                    eurValues.add(new Entry((float)data.getDouble(Utility.COL_RATE_EUR), i));
                    rubValues.add(new Entry((float)data.getDouble(Utility.COL_RATE_RUB), i));
                } else {
                    usdBuyValues.add(new Entry((float)data.getDouble(Utility.COL_DOUBLE_RATE_USD_BUY), i));
                    usdSellValues.add(new Entry((float)data.getDouble(Utility.COL_DOUBLE_RATE_USD_SELL), i));
                    eurBuyValues.add(new Entry((float)data.getDouble(Utility.COL_DOUBLE_RATE_EUR_BUY), i));
                    eurSellValues.add(new Entry((float)data.getDouble(Utility.COL_DOUBLE_RATE_EUR_SELL), i));
                    rubBuyValues.add(new Entry((float)data.getDouble(Utility.COL_DOUBLE_RATE_RUB_BUY), i));
                    rubSellValues.add(new Entry((float)data.getDouble(Utility.COL_DOUBLE_RATE_RUB_SELL), i));
                }
                i++;
            } while (data.moveToNext());
        if (singleRate) {
            LineDataSet usdDataSet = new LineDataSet(usdValues, getString(R.string.usd));
            LineDataSet eurDataSet = new LineDataSet(eurValues, getString(R.string.eur));
            LineDataSet rubDataSet = new LineDataSet(rubValues, getString(R.string.rub));
            setDataSetSettings(usdDataSet, android.R.color.holo_blue_bright, android.R.color.holo_blue_dark);
            setDataSetSettings(eurDataSet, android.R.color.holo_green_light, android.R.color.holo_green_dark);
            setDataSetSettings(rubDataSet, android.R.color.holo_red_light, android.R.color.holo_red_dark);
            dataSets.add(usdDataSet);
            dataSets.add(eurDataSet);
            dataSets.add(rubDataSet);
        } else {
            LineDataSet usdBuyDataSet =  new LineDataSet(usdBuyValues, String.format("%s %s",  getString(R.string.usd), getString(R.string.buy)));
            LineDataSet usdSellDataSet = new LineDataSet(usdSellValues, String.format("%s %s", getString(R.string.usd), getString(R.string.sell)));
            LineDataSet eurBuyDataSet =  new LineDataSet(eurBuyValues, String.format("%s %s",  getString(R.string.eur), getString(R.string.buy)));
            LineDataSet eurSellDataSet = new LineDataSet(eurSellValues, String.format("%s %s", getString(R.string.eur), getString(R.string.sell)));
            LineDataSet rubBuyDataSet =  new LineDataSet(rubBuyValues, String.format("%s %s",  getString(R.string.rub), getString(R.string.buy)));
            LineDataSet rubSellDataSet = new LineDataSet(rubSellValues, String.format("%s %s", getString(R.string.rub), getString(R.string.sell)));
            setDataSetSettings(usdBuyDataSet, android.R.color.holo_blue_bright, android.R.color.holo_blue_dark);
            setDataSetSettings(usdSellDataSet, android.R.color.holo_green_light, android.R.color.holo_green_dark);
            setDataSetSettings(eurBuyDataSet, android.R.color.holo_red_light, android.R.color.holo_red_dark);
            setDataSetSettings(eurSellDataSet, android.R.color.secondary_text_light, android.R.color.black);
            setDataSetSettings(rubBuyDataSet, android.R.color.holo_orange_light, android.R.color.holo_orange_dark);
            setDataSetSettings(rubSellDataSet, android.R.color.holo_purple, android.R.color.holo_purple);
            dataSets.add(usdBuyDataSet);
            dataSets.add(usdSellDataSet);
            dataSets.add(eurBuyDataSet);
            dataSets.add(eurSellDataSet);
            dataSets.add(rubBuyDataSet);
            dataSets.add(rubSellDataSet);
        }

        LineData lineData = new LineData(xVals, dataSets);
        mChart.setData(lineData);
        mChart.invalidate();
        mChart.animateX(3000);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    private void setDataSetSettings(LineDataSet set, int colorId, int highLightColorId){
        set.setColor(getResources().getColor(colorId));
        set.setCircleColor(getResources().getColor(colorId));
        set.setLineWidth(2f);
        set.setCircleSize(4f);
        set.setHighLightColor(getResources().getColor(highLightColorId));
    }
}
