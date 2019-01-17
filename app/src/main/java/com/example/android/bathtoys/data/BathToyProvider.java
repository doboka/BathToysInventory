
package com.example.android.bathtoys.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.bathtoys.data.BathToyContract.BathToysEntry;

/**
 * {@link ContentProvider} for Bath Toy app.
 */
public class BathToyProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = BathToyProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the bath toys table
     */
    private static final int BATHTOYS = 100;

    /**
     * URI matcher code for the content URI for a single bath toy in the bath toys table
     */
    private static final int BATHTOY_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(BathToyContract.CONTENT_AUTHORITY, BathToyContract.PATH_BATHTOYS, BATHTOYS);

        sUriMatcher.addURI(BathToyContract.CONTENT_AUTHORITY, BathToyContract.PATH_BATHTOYS + "/#", BATHTOY_ID);
    }

    /**
     * Database helper object
     */
    private BathToyDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new BathToyDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case BATHTOYS:
                cursor = database.query(BathToysEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case BATHTOY_ID:
                selection = BathToyContract.BathToysEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(BathToysEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BATHTOYS:
                return insertBathToy(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a bath toy into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertBathToy(Uri uri, ContentValues values) {
        byte[] image = values.getAsByteArray(BathToysEntry.COLUMN_BATHTOYS_IMAGE);
        if (image == null) {
            throw new IllegalArgumentException("Bath toy requires a picture");
        }

        String name = values.getAsString(BathToysEntry.COLUMN_BATHTOYS_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Bath toy requires a name");
        }

        String quantity = values.getAsString(BathToysEntry.COLUMN_BATHTOYS_QUANTITY);
        if (quantity == null) {
            throw new IllegalArgumentException("Bath toy requires a quantity");
        }

        Integer weight = values.getAsInteger(BathToysEntry.COLUMN_BATHTOYS_PRICE);
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Bath toy requires valid price");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(BathToyContract.BathToysEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BATHTOYS:
                return updateBathToy(uri, contentValues, selection, selectionArgs);
            case BATHTOY_ID:
                selection = BathToyContract.BathToysEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBathToy(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update bath toys in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments.
     * Return the number of rows that were successfully updated.
     */
    private int updateBathToy(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(BathToysEntry.COLUMN_BATHTOYS_IMAGE)) {
            byte[] image = values.getAsByteArray(BathToysEntry.COLUMN_BATHTOYS_IMAGE);
            if (image == null) {
                throw new IllegalArgumentException("Bath toy requires a picture");
            }
        }

        if (values.containsKey(BathToysEntry.COLUMN_BATHTOYS_NAME)) {
            String name = values.getAsString(BathToysEntry.COLUMN_BATHTOYS_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Bath toy requires a name");
            }
        }

        if (values.containsKey(BathToysEntry.COLUMN_BATHTOYS_QUANTITY)) {
            Integer quantity = values.getAsInteger(BathToysEntry.COLUMN_BATHTOYS_QUANTITY);
            if (quantity == null) {
                throw new IllegalArgumentException("Bath toy requires quantity");
            }
        }

        if (values.containsKey(BathToysEntry.COLUMN_BATHTOYS_PRICE)) {
            Integer weight = values.getAsInteger(BathToysEntry.COLUMN_BATHTOYS_PRICE);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Bath toy requires price");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(BathToysEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BATHTOYS:
                rowsDeleted = database.delete(BathToysEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BATHTOY_ID:
                selection = BathToysEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(BathToysEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BATHTOYS:
                return BathToysEntry.CONTENT_LIST_TYPE;
            case BATHTOY_ID:
                return BathToyContract.BathToysEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
