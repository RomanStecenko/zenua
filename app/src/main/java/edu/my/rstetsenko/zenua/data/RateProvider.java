package edu.my.rstetsenko.zenua.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

public class RateProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private RateDbHelper mOpenHelper;

    private static final int RATE = 10;
    private static final int RATE_WITH_SOURCE = 11;
    private static final int RATE_WITH_SOURCE_AND_DATE = 12;
    private static final int DOUBLE_RATE = 20;
    private static final int DOUBLE_RATE_WITH_SOURCE = 21;
    private static final int DOUBLE_RATE_WITH_SOURCE_AND_DATE = 22;

    private static final String additionForSourceIdSelection = "." + RateBaseColumns.COLUMN_SOURCE_ID + " = ? ";

    private static final String additionForSourceIdWithStartDateSelection =
            "." + RateBaseColumns.COLUMN_SOURCE_ID + " = ? AND " +
                    RateBaseColumns.COLUMN_DATE + " >= ? ";

    private static final String additionForSourceIdAndDaySelection =
                    "." + RateBaseColumns.COLUMN_SOURCE_ID + " = ? AND " +
                    RateBaseColumns.COLUMN_DATE + " = ? ";

    private Cursor getRateBySource(Uri uri, String[] projection, String sortOrder) {
        int sourceId = RateContract.getSourceIdFromUri(uri);
        long startDate = RateContract.getStartDateFromUri(uri);
        String tableName = RateContract.getTableNameFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == 0) {
            selection = tableName + additionForSourceIdSelection;
            selectionArgs = new String[]{String.valueOf(sourceId)};
        } else {
            selectionArgs = new String[]{String.valueOf(sourceId), Long.toString(startDate)};
            selection = tableName + additionForSourceIdWithStartDateSelection;
        }

        return mOpenHelper.getReadableDatabase().query(
                tableName,
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
        String tableName = RateContract.getTableNameFromUri(uri);

        return mOpenHelper.getReadableDatabase().query(
                tableName,
                projection,
                tableName + additionForSourceIdAndDaySelection,
                new String[]{String.valueOf(sourceId), Long.toString(date)},
                null,
                null,
                sortOrder
        );
    }

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
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "rate/#/#" "double_rate/#/#"
            case RATE_WITH_SOURCE_AND_DATE:
            case DOUBLE_RATE_WITH_SOURCE_AND_DATE: {
                retCursor = getRateBySourceAndDate(uri, projection, sortOrder);
                break;
            }
            // "rate/#" "double_rate/#"
            case RATE_WITH_SOURCE:
            case DOUBLE_RATE_WITH_SOURCE: {
                retCursor = getRateBySource(uri, projection, sortOrder);
                break;
            }
            // "rate"
            case RATE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        RateContract.RateEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "double_rate"
            case DOUBLE_RATE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        RateContract.DoubleRateEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case RATE: {
                long _id = db.insert(RateContract.RateEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = RateContract.RateEntry.buildRateUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case DOUBLE_RATE: {
                long _id = db.insert(RateContract.DoubleRateEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = RateContract.DoubleRateEntry.buildDoubleRateUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int affectedRows;
        if (null == selection) selection = "1";
        switch (match) {
            case RATE: {
                affectedRows = db.delete(RateContract.RateEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case DOUBLE_RATE: {
                affectedRows = db.delete(RateContract.DoubleRateEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (affectedRows != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return affectedRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int affectedRows;
        switch (match) {
            case RATE: {
                affectedRows = db.update(RateContract.RateEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case DOUBLE_RATE: {
                affectedRows = db.update(RateContract.DoubleRateEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (affectedRows != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return affectedRows;
    }

    @Override
    public int bulkInsert(Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case RATE:
                db.beginTransaction();
                int returnRateCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(RateContract.RateEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnRateCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnRateCount;
            case DOUBLE_RATE:
                db.beginTransaction();
                int returnDoubleRateCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(RateContract.DoubleRateEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnDoubleRateCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnDoubleRateCount;
            default:
                return super.bulkInsert(uri, values);
        }
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
