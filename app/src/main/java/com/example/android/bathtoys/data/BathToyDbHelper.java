
package com.example.android.bathtoys.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database helper for Bath Toy app. Manages database creation and version management.
 */
public class BathToyDbHelper extends SQLiteOpenHelper {

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "bathtoys.db";

    /**
     * Database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link BathToyDbHelper}.
     *
     * @param context of the app
     */
    public BathToyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_BATHTOYS_TABLE = "CREATE TABLE " + BathToyContract.BathToysEntry.TABLE_NAME + " ("
                + BathToyContract.BathToysEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BathToyContract.BathToysEntry.COLUMN_BATHTOYS_IMAGE + " BLOB, "
                + BathToyContract.BathToysEntry.COLUMN_BATHTOYS_NAME + " TEXT NOT NULL, "
                + BathToyContract.BathToysEntry.COLUMN_BATHTOYS_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + BathToyContract.BathToysEntry.COLUMN_BATHTOYS_PRICE + " INTEGER NOT NULL DEFAULT 0);";

        db.execSQL(SQL_CREATE_BATHTOYS_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}