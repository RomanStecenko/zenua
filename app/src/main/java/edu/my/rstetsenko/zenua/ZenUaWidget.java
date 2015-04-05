package edu.my.rstetsenko.zenua;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Arrays;
import edu.my.rstetsenko.zenua.services.WidgetUpdateService;

public class ZenUaWidget extends AppWidgetProvider {
    static final String LOG_TAG = ZenUaWidget.class.getSimpleName();

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d(LOG_TAG, "onEnabled");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        ComponentName thisWidget = new ComponentName(context, ZenUaWidget.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        Intent intent = new Intent(context.getApplicationContext(), WidgetUpdateService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
        context.startService(intent);

        Log.d(LOG_TAG, "onUpdate " + Arrays.toString(appWidgetIds));

//        int sourceId = Utility.getPreferredSource();
//        boolean singleRate = Constants.singleRates.contains(sourceId);
//        Uri uri = singleRate ?
//                RateContract.RateEntry.buildRateSourceIdWithLastDate(sourceId):
//                RateContract.DoubleRateEntry.buildDoubleRateSourceIdWithLastDate(sourceId);
//
//        String[] projection = singleRate ?
//                Utility.RATE_COLUMNS : Utility.DOUBLE_RATE_COLUMNS;
//
//        String sortOrder = RateBaseColumns.COLUMN_DATE + " DESC";
//        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, sortOrder);
//        for (int id : appWidgetIds) {
//            updateWidget(context, appWidgetManager, cursor, id, singleRate);
//        }
//        cursor.close();
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.d(LOG_TAG, "onDeleted " + Arrays.toString(appWidgetIds));
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d(LOG_TAG, "onDisabled");
    }
}
