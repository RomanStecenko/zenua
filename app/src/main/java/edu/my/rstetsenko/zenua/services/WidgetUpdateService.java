package edu.my.rstetsenko.zenua.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import edu.my.rstetsenko.zenua.Constants;
import edu.my.rstetsenko.zenua.R;
import edu.my.rstetsenko.zenua.Utility;
import edu.my.rstetsenko.zenua.activities.MainActivity;
import edu.my.rstetsenko.zenua.data.RateBaseColumns;
import edu.my.rstetsenko.zenua.data.RateContract;

public class WidgetUpdateService extends IntentService {
    private static final String LOG_TAG = WidgetUpdateService.class.getSimpleName();

    public WidgetUpdateService() {
        super("WidgetUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());

        int sourceId = Utility.getPreferredSource();
        boolean singleRate = Constants.singleRates.contains(sourceId);
        Uri uri = singleRate ?
                RateContract.RateEntry.buildRateSourceIdWithLastDate(sourceId):
                RateContract.DoubleRateEntry.buildDoubleRateSourceIdWithLastDate(sourceId);

        String[] projection = singleRate ?
                Utility.RATE_COLUMNS : Utility.DOUBLE_RATE_COLUMNS;

        String sortOrder = RateBaseColumns.COLUMN_DATE + " DESC";
        Cursor cursor = getContentResolver().query(uri, projection, null, null, sortOrder);
        for (int id : appWidgetIds) {
            updateWidget(this, appWidgetManager, cursor, id, singleRate);
        }
        cursor.close();
    }

    private static void updateWidget(Context context, AppWidgetManager appWidgetManager,
                                     Cursor cursor, int widgetID, boolean singleRate) {
        Log.d(LOG_TAG, "updateWidget " + widgetID);
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_land);
        widgetView.setOnClickPendingIntent(R.id.widget_root_view, pendingIntent);
        toggleWidgetLayout(widgetView, singleRate);

        if (!cursor.moveToFirst()) {
            return;
        }

        if (singleRate) {
            setRate(
                    widgetView,
                    cursor.getDouble(Utility.COL_RATE_USD),
                    cursor.getDouble(Utility.COL_RATE_EUR),
                    cursor.getDouble(Utility.COL_RATE_RUB)
            );
        } else {
            setDoubleRate(
                    widgetView,
                    cursor.getDouble(Utility.COL_DOUBLE_RATE_USD_BUY),
                    cursor.getDouble(Utility.COL_DOUBLE_RATE_USD_SELL),
                    cursor.getDouble(Utility.COL_DOUBLE_RATE_EUR_BUY),
                    cursor.getDouble(Utility.COL_DOUBLE_RATE_EUR_SELL),
                    cursor.getDouble(Utility.COL_DOUBLE_RATE_RUB_BUY),
                    cursor.getDouble(Utility.COL_DOUBLE_RATE_RUB_SELL)
            );
        }
        appWidgetManager.updateAppWidget(widgetID, widgetView);
    }

    private static void setRate(RemoteViews remoteViews, double usd, double eur, double rub) {
        remoteViews.setTextViewText(R.id.usd_rate, String.format("%.2f", usd));
        remoteViews.setTextViewText(R.id.eur_rate, String.format("%.2f", eur));
        remoteViews.setTextViewText(R.id.rub_rate, String.format("%.2f", rub));
    }

    private static void setDoubleRate(RemoteViews remoteViews, double usdBuy, double usdSell, double eurBuy, double eurSell, double rubBuy, double rubSell) {
        remoteViews.setTextViewText(R.id.usd_rate_buy, String.format("%.2f", usdBuy));
        remoteViews.setTextViewText(R.id.usd_rate_sell, String.format("%.2f", usdSell));
        remoteViews.setTextViewText(R.id.eur_rate_buy, String.format("%.2f", eurBuy));
        remoteViews.setTextViewText(R.id.eur_rate_sell, String.format("%.2f", eurSell));
        remoteViews.setTextViewText(R.id.rub_rate_buy, String.format("%.2f", rubBuy));
        remoteViews.setTextViewText(R.id.rub_rate_sell, String.format("%.2f", rubSell));
    }

    private static void toggleWidgetLayout(RemoteViews remoteViews, boolean singleRate) {
        if (singleRate) {
            remoteViews.setViewVisibility(R.id.usd_rate, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.eur_rate, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.rub_rate, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.buy_sell_usd, View.GONE);
            remoteViews.setViewVisibility(R.id.buy_sell_eur, View.GONE);
            remoteViews.setViewVisibility(R.id.buy_sell_rub, View.GONE);
            remoteViews.setViewVisibility(R.id.buy_sell_titles, View.GONE);
        } else {
            remoteViews.setViewVisibility(R.id.usd_rate, View.GONE);
            remoteViews.setViewVisibility(R.id.eur_rate, View.GONE);
            remoteViews.setViewVisibility(R.id.rub_rate, View.GONE);
            remoteViews.setViewVisibility(R.id.buy_sell_usd, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.buy_sell_eur, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.buy_sell_rub, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.buy_sell_titles, View.VISIBLE);
        }
    }
}
