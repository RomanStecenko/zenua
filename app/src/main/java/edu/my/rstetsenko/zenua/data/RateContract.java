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

        public static Uri buildRateSourceId(int sourceId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(sourceId)).build();
        }

        public static Uri buildRateSourceIdWithLastDate(int sourceId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(sourceId))
                    .appendQueryParameter(COLUMN_DATE, "true").build();
        }

        public static Uri buildRateSourceIdWithDate(int sourceId, long date) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(sourceId))
                    .appendPath(Long.toString(date)).build();
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

        public static Uri buildDoubleRateSourceId(int sourceId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(sourceId)).build();
        }

        public static Uri buildDoubleRateSourceIdWithLastDate(int sourceId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(sourceId))
                    .appendQueryParameter(COLUMN_DATE, "true").build();
        }

        public static Uri buildDoubleRateSourceIdWithDate(int sourceId, long date) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(sourceId))
                    .appendPath(Long.toString(date)).build();
        }
    }

    public static String getTableNameFromUri(Uri uri) {
        return uri.getPathSegments().get(0);
    }

    public static int getSourceIdFromUri(Uri uri) {
        return Integer.parseInt(uri.getPathSegments().get(1));
    }

    public static long getDateFromUri(Uri uri) {
        return Long.parseLong(uri.getPathSegments().get(2));
    }

    public static boolean withLastDateInUri(Uri uri) {
        String withLastDate = uri.getQueryParameter(RateBaseColumns.COLUMN_DATE);
        return null != withLastDate && withLastDate.length() > 0 && Boolean.parseBoolean(withLastDate);
    }
}
