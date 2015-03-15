package edu.my.rstetsenko.zenua.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import edu.my.rstetsenko.zenua.data.RateContract.RateEntry;
import edu.my.rstetsenko.zenua.data.RateContract.DoubleRateEntry;

public class RateDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "rate.db";

    public RateDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_RATE_TABLE = "CREATE TABLE " + RateEntry.TABLE_NAME + " (" +
                RateEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                RateEntry.COLUMN_SOURCE_ID + " INTEGER NOT NULL, " +
                RateEntry.COLUMN_DATE + " INTEGER, " +
                RateEntry.COLUMN_USD + " REAL NOT NULL, " +
                RateEntry.COLUMN_EUR + " REAL NOT NULL, " +
                RateEntry.COLUMN_RUB + " REAL NOT NULL " +
                " );";

        final String SQL_CREATE_DOUBLE_RATE_TABLE = "CREATE TABLE " + DoubleRateEntry.TABLE_NAME + " (" +
                DoubleRateEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DoubleRateEntry.COLUMN_SOURCE_ID + " INTEGER NOT NULL, " +
                DoubleRateEntry.COLUMN_DATE + " INTEGER, " +
                DoubleRateEntry.COLUMN_USD_BUY + " REAL NOT NULL, " +
                DoubleRateEntry.COLUMN_USD_SELL + " REAL NOT NULL, " +
                DoubleRateEntry.COLUMN_EUR_BUY + " REAL NOT NULL, " +
                DoubleRateEntry.COLUMN_EUR_SELL + " REAL NOT NULL, " +
                DoubleRateEntry.COLUMN_RUB_BUY + " REAL NOT NULL " +
                DoubleRateEntry.COLUMN_RUB_SELL + " REAL NOT NULL " +
                " );";

        db.execSQL(SQL_CREATE_RATE_TABLE);
        db.execSQL(SQL_CREATE_DOUBLE_RATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RateEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DoubleRateEntry.TABLE_NAME);
        onCreate(db);
    }

}
