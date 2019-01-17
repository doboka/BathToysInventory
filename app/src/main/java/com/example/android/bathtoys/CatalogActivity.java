
package com.example.android.bathtoys;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.bathtoys.data.BathToyContract;
import com.example.android.bathtoys.data.BathToyContract.BathToysEntry;

import java.io.ByteArrayOutputStream;

/**
 * Displays list of bath toys that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the bath toy data loader
     */
    private static final int BATHTOY_LOADER = 0;

    /**
     * Adapter for the ListView
     */
    BathToyCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView bathToyListView = (ListView) findViewById(R.id.list);

        View emptyView = findViewById(R.id.empty_view);
        bathToyListView.setEmptyView(emptyView);

        mCursorAdapter = new BathToyCursorAdapter(this, null);
        bathToyListView.setAdapter(mCursorAdapter);

        bathToyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                Uri currentBathToyUri = ContentUris.withAppendedId(BathToysEntry.CONTENT_URI, id);

                intent.setData(currentBathToyUri);

                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(BATHTOY_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded bath toy data into the database.
     */
    private void insertBathToy() {
        byte[] imageAsByteArray;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.duck, null);
        BitmapDrawable bitmapDrawable = ((BitmapDrawable) drawable);
        Bitmap bitmap = bitmapDrawable.getBitmap();

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        imageAsByteArray = bos.toByteArray();
        ContentValues values = new ContentValues();
        values.put(BathToysEntry.COLUMN_BATHTOYS_IMAGE, imageAsByteArray);
        values.put(BathToysEntry.COLUMN_BATHTOYS_NAME, "Yellow rubber duck");
        values.put(BathToyContract.BathToysEntry.COLUMN_BATHTOYS_QUANTITY, 10);
        values.put(BathToysEntry.COLUMN_BATHTOYS_PRICE, 7);

        getContentResolver().insert(BathToyContract.BathToysEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all bath toys in the database.
     */
    private void deleteAllBathToys() {
        int rowsDeleted = getContentResolver().delete(BathToyContract.BathToysEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from bath toys database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertBathToy();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllBathToys();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                BathToyContract.BathToysEntry._ID,
                BathToysEntry.COLUMN_BATHTOYS_IMAGE,
                BathToyContract.BathToysEntry.COLUMN_BATHTOYS_NAME,
                BathToyContract.BathToysEntry.COLUMN_BATHTOYS_QUANTITY,
                BathToysEntry.COLUMN_BATHTOYS_PRICE,
        };

        return new CursorLoader(this,
                BathToysEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
