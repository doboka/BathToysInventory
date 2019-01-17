
package com.example.android.bathtoys;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.bathtoys.data.BathToyContract;
import com.example.android.bathtoys.data.BathToyContract.BathToysEntry;

/**
 * {@link BathToyCursorAdapter} is an adapter for a list view
 * that uses a {@link Cursor} of bath toy data as its data source. This adapter knows
 * how to create list items for each row of bath toy data in the {@link Cursor}.
 */
public class BathToyCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link BathToyCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public BathToyCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the bath toy data (in the current row pointed to by cursor) to the given
     * list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        ImageView itemImageView = (ImageView) view.findViewById(R.id.item_image);
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);

        int idColumnIndex = cursor.getColumnIndex(BathToysEntry._ID);
        int imageColumnIndex = cursor.getColumnIndex(BathToysEntry.COLUMN_BATHTOYS_IMAGE);
        int nameColumnIndex = cursor.getColumnIndex(BathToysEntry.COLUMN_BATHTOYS_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(BathToyContract.BathToysEntry.COLUMN_BATHTOYS_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(BathToysEntry.COLUMN_BATHTOYS_PRICE);

        final int bathToyId = cursor.getInt(idColumnIndex);
        byte[] bathToyImage = cursor.getBlob(imageColumnIndex);
        String bathToyName = cursor.getString(nameColumnIndex);
        final int bathToyQuantity = cursor.getInt(quantityColumnIndex);
        String bathToyPrice = cursor.getString(priceColumnIndex);

        nameTextView.setText(bathToyName);
        quantityTextView.setText(bathToyQuantity + " piece(s)");
        priceTextView.setText(bathToyPrice + " $");
        try {
            if (bathToyImage == null || bathToyImage.length == 0) {
                itemImageView.setImageResource(R.drawable.clean);
            } else {
                Bitmap bathToyBitmap = BitmapFactory.decodeByteArray(bathToyImage, 0, bathToyImage.length);
                itemImageView.setImageBitmap(bathToyBitmap);
            }
        } catch (Exception e) {
            Log.w("BathToyCursorAdapter", "Problem with image", e);
        }
        Button saleButton = (Button) view.findViewById(R.id.sell_item);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri currentBathToyUri = ContentUris.withAppendedId(BathToysEntry.CONTENT_URI, bathToyId);
                sellItem(context, bathToyQuantity, currentBathToyUri);
            }
        });
    }

    private void sellItem(Context context, int quantity, Uri uriBathToy) {
        if (quantity == 0) {
            Log.v("BathToyCursorAdpter", "quantity cannot be reduced");
        } else {
            int newQuantity = quantity - 1;
            Log.v("BathToyCursorAdpter", "new quantity is " + newQuantity);

            ContentValues values = new ContentValues();
            values.put(BathToysEntry.COLUMN_BATHTOYS_QUANTITY, newQuantity);
            int rowsAffected = context.getContentResolver().update(uriBathToy, values, null, null);

            if (rowsAffected == 0) {
                Log.v("BathToyCursorAdapter", "no rows changed");
            } else {
                Log.v("BathToyCursorAdpter", "rows changed = " + rowsAffected);
            }
        }
    }
}
