package edu.my.rstetsenko.zenua.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class RateProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private RateDbHelper mOpenHelper;

    private static final int RATE = 10;
    private static final int RATE_WITH_SOURCE = 11;
    private static final int RATE_WITH_SOURCE_AND_DATE = 12;
    private static final int DOUBLE_RATE = 20;
    private static final int DOUBLE_RATE_WITH_SOURCE = 21;
    private static final int DOUBLE_RATE_WITH_SOURCE_AND_DATE = 22;

    //for RATE
    private static final String rateSourceIdSelection =
            RateContract.RateEntry.TABLE_NAME+
                    "." + RateBaseColumns.COLUMN_SOURCE_ID + " = ? ";
    private static final String rateSourceIdWithStartDateSelection =
            RateContract.RateEntry.TABLE_NAME+
                    "." + RateBaseColumns.COLUMN_SOURCE_ID + " = ? AND " +
                    RateBaseColumns.COLUMN_DATE + " >= ? ";

    private static final String rateSourceIdAndDaySelection =
            RateContract.RateEntry.TABLE_NAME +
                    "." + RateBaseColumns.COLUMN_SOURCE_ID + " = ? AND " +
                    RateBaseColumns.COLUMN_DATE + " = ? ";

    //for DOUBLE_RATE
    private static final String doubleRateSourceIdSelection =
            RateContract.DoubleRateEntry.TABLE_NAME+
                    "." + RateBaseColumns.COLUMN_SOURCE_ID + " = ? ";
    private static final String doubleRateSourceIdWithStartDateSelection =
            RateContract.DoubleRateEntry.TABLE_NAME+
                    "." + RateBaseColumns.COLUMN_SOURCE_ID + " = ? AND " +
                    RateBaseColumns.COLUMN_DATE + " >= ? ";

    private static final String doubleRateSourceIdAndDaySelection =
            RateContract.DoubleRateEntry.TABLE_NAME +
                    "." + RateBaseColumns.COLUMN_SOURCE_ID + " = ? AND " +
                    RateBaseColumns.COLUMN_DATE + " = ? ";

    private Cursor getRateBySource(Uri uri, String[] projection, String sortOrder) {
        int sourceId = RateContract.getSourceIdFromUri(uri);
        long startDate = RateContract.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == 0) {
            selection = rateSourceIdSelection;
            selectionArgs = new String[]{String.valueOf(sourceId)};
        } else {
            selectionArgs = new String[]{String.valueOf(sourceId), Long.toString(startDate)};
            selection = rateSourceIdWithStartDateSelection;
        }

        return mOpenHelper.getReadableDatabase().query(
                RateContract.RateEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getRateBySourceAndDate(Uri uri, String[] projection, String sortOrder) {
        int sourceId = RateContract.getSourceIdFromUri(uri);
        long date = RateContract.getDateFromUri(uri);

        return mOpenHelper.getReadableDatabase().query(
                RateContract.RateEntry.TABLE_NAME,
                projection,
                rateSourceIdAndDaySelection,
                new String[]{String.valueOf(sourceId), Long.toString(date)},
                null,
                null,
                sortOrder
        );
    }

    //TODO I made "query" method for Rate table, need to make for Double Rate table, or think how to join this queries

    static UriMatcher buildUriMatcher() {
        UriMatcher myUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        myUriMatcher.addURI(RateContract.CONTENT_AUTHORITY, RateContract.PATH_RATE, RATE);
        myUriMatcher.addURI(RateContract.CONTENT_AUTHORITY, RateContract.PATH_RATE + "/#", RATE_WITH_SOURCE);
        myUriMatcher.addURI(RateContract.CONTENT_AUTHORITY, RateContract.PATH_RATE + "/#/#", RATE_WITH_SOURCE_AND_DATE);
        myUriMatcher.addURI(RateContract.CONTENT_AUTHORITY, RateContract.PATH_DOUBLE_RATE, DOUBLE_RATE);
        myUriMatcher.addURI(RateContract.CONTENT_AUTHORITY, RateContract.PATH_DOUBLE_RATE + "/#", DOUBLE_RATE_WITH_SOURCE);
        myUriMatcher.addURI(RateContract.CONTENT_AUTHORITY, RateContract.PATH_DOUBLE_RATE + "/#/#", DOUBLE_RATE_WITH_SOURCE_AND_DATE);
        return myUriMatcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new RateDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case RATE:
                return RateContract.RateEntry.CONTENT_TYPE;
            case RATE_WITH_SOURCE:
                return RateContract.RateEntry.CONTENT_TYPE;
            case RATE_WITH_SOURCE_AND_DATE:
                return RateContract.RateEntry.CONTENT_ITEM_TYPE;
            case DOUBLE_RATE:
                return RateContract.DoubleRateEntry.CONTENT_TYPE;
            case DOUBLE_RATE_WITH_SOURCE:
                return RateContract.DoubleRateEntry.CONTENT_TYPE;
            case DOUBLE_RATE_WITH_SOURCE_AND_DATE:
                return RateContract.DoubleRateEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // in case using tests
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
//    @Override
//    @TargetApi(11)
//    public void shutdown() {
//        mOpenHelper.close();
//        super.shutdown();
//    }
}
