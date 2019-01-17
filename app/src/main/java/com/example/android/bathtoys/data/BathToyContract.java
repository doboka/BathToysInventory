
package com.example.android.bathtoys.data;

import android.net.Uri;
import android.content.ContentResolver;
import android.provider.BaseColumns;

/**
 * API Contract for the Bath Toy app.
 */
public final class BathToyContract {

    /**
     * The "Content authority" is a name for the entire content provider.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.bathtoys";
    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * Possible path (appended to base content URI for possible URI's)
     */
    public static final String PATH_BATHTOYS = "bathtoys";

    private BathToyContract() {
    }

    /**
     * Inner class that defines constant values for the bath toy database table.
     * Each entry in the table represents a single bath toy.
     */
    public static final class BathToysEntry implements BaseColumns {

        /**
         * The content URI to access the bath toy data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BATHTOYS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of bath toys.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BATHTOYS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single bath toy.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BATHTOYS;

        /**
         * Name of database table for bath toys
         */
        public final static String TABLE_NAME = "bathtoys";

        /**
         * Unique ID number for the bath toy (only for use in the database table).
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Image for the bath toy.
         */
        public final static String COLUMN_BATHTOYS_IMAGE = "image";

        /**
         * Name of the bath toy.
         */
        public final static String COLUMN_BATHTOYS_NAME = "name";


        /**
         * Quantity of the bath toy.
         */
        public final static String COLUMN_BATHTOYS_QUANTITY = "quantity";

        /**
         * Price of the bath toy.
         */
        public final static String COLUMN_BATHTOYS_PRICE = "price";

    }
}

