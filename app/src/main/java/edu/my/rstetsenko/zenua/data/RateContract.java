package edu.my.rstetsenko.zenua.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;

public class RateContract {
    public static final String CONTENT_AUTHORITY = "edu.my.rstetsenko.zenua";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_RATE = "rate";
    public static final String PATH_DOUBLE_RATE = "double_rate";

    public static final class RateEntry implements RateBaseColumns {
        public static final String TABLE_NAME = "rate";
        public static final String COLUMN_USD = "usd";
        public static final String COLUMN_EUR = "eur";
        public static final String COLUMN_RUB = "rub";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_RATE).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RATE;
        public static final String CONTENT_ITEM_TYPE =  ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RATE;

        public static Uri buildRateUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class DoubleRateEntry implements RateBaseColumns {
        public static final String TABLE_NAME = "double_rate";
        public static final String COLUMN_USD_BUY = "usd_buy";
        public static final String COLUMN_USD_SELL = "usd_sell";
        public static final String COLUMN_EUR_BUY = "eur_buy";
        public static final String COLUMN_EUR_SELL = "eur_sell";
        public static final String COLUMN_RUB_BUY = "rub_buy";
        public static final String COLUMN_RUB_SELL = "rub_sell";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_DOUBLE_RATE).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DOUBLE_RATE;
        public static final String CONTENT_ITEM_TYPE =  ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DOUBLE_RATE;

        public static Uri buildDoubleRateUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        //TODO make URI methods if they are needed
    }

    public static int getSourceIdFromUri(Uri uri) {
        return Integer.parseInt(uri.getPathSegments().get(1));
    }

    public static long getDateFromUri(Uri uri) {
        return Long.parseLong(uri.getPathSegments().get(2));
    }

    public static long getStartDateFromUri(Uri uri) {
        String dateString = uri.getQueryParameter(RateBaseColumns.COLUMN_DATE);
        if (null != dateString && dateString.length() > 0)
            return Long.parseLong(dateString);
        else
            return 0;
    }
}
