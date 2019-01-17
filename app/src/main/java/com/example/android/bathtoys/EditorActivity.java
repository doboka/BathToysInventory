
package com.example.android.bathtoys;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.bathtoys.data.BathToyContract;
import com.example.android.bathtoys.data.BathToyContract.BathToysEntry;

import java.io.ByteArrayOutputStream;

/**
 * Allows user to create a new bath toy or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the bath toy data loader
     */
    private static final int EXISTING_BATHTOY_LOADER = 0;

    private Uri mCurrentBathToyUri;

    private EditText mNameEditText;

    private EditText mQuantityEditText;

    private EditText mPriceEditText;

    private ImageView mImageView;

    private Button mPlusButton;

    private Button mMinusButton;

    private Button mOrderButton;

    private boolean mBathToyHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mBathToyHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBathToyHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentBathToyUri = intent.getData();

        if (mCurrentBathToyUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_bath_toy));

            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_bath_toy));

            getLoaderManager().initLoader(EXISTING_BATHTOY_LOADER, null, this);
        }

        mNameEditText = (EditText) findViewById(R.id.edit_bathtoy_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_bathtoy_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_bathtoy_price);
        mImageView = (ImageView) findViewById(R.id.bathtoy_image_view);

        mPlusButton = (Button) findViewById(R.id.plus_button);
        mMinusButton = (Button) findViewById(R.id.minus_button);
        mOrderButton = (Button) findViewById(R.id.order_button);

        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mImageView.setOnTouchListener(mTouchListener);
        mPlusButton.setOnTouchListener(mTouchListener);
        mMinusButton.setOnTouchListener(mTouchListener);

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, 1);
                }
            }
        });

        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantityString = mQuantityEditText.getText().toString().trim();
                int quantity = 0;
                if (quantityString != null && quantityString.length() != 0) {
                    quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                }
                quantity++;
                mQuantityEditText.setText(Integer.toString(quantity));
            }
        });

        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = 0;
                String quantityString = mQuantityEditText.getText().toString().trim();
                if (quantityString == null || quantityString.length() == 0) {
                    quantity = 0;
                } else {
                    quantity = Integer.parseInt(quantityString);
                    if (quantity == 0) {
                        Toast.makeText(getApplicationContext(), getString(R.string.quantity_negative),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        quantity = quantity - 1;
                    }
                }
                mQuantityEditText.setText(Integer.toString(quantity));
            }
        });

        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameEmail = mNameEditText.getText().toString().trim();
                String quantityEmail = mQuantityEditText.getText().toString().trim();

                String message = getString(R.string.email_message) +
                        "\n" + nameEmail +
                        " - " + quantityEmail;

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.email_subject));
                intent.putExtra(Intent.EXTRA_TEXT, message);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            mImageView.setImageBitmap(photo);
        }
    }

    /**
     * Get user input from editor and save pet into database.
     */
    private void saveBathtoy() {

        byte[] imageByte = getPictureAsByteArray();

        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        if (mCurrentBathToyUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(priceString)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(BathToysEntry.COLUMN_BATHTOYS_IMAGE, imageByte);
        values.put(BathToyContract.BathToysEntry.COLUMN_BATHTOYS_NAME, nameString);
        values.put(BathToysEntry.COLUMN_BATHTOYS_QUANTITY, quantityString);
        values.put(BathToysEntry.COLUMN_BATHTOYS_PRICE, priceString);
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(BathToysEntry.COLUMN_BATHTOYS_PRICE, price);

        if (mCurrentBathToyUri == null) {
            Uri newUri = getContentResolver().insert(BathToysEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_bath_toy_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_bath_toy_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentBathToyUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_bath_toy_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_bath_toy_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private byte[] getPictureAsByteArray() {
        byte[] imageByte = new byte[0];
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Drawable imageImage = mImageView.getDrawable();
            BitmapDrawable bitmapDrawable = ((BitmapDrawable) imageImage);
            Bitmap bitmap = bitmapDrawable.getBitmap();

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            imageByte = bos.toByteArray();
        } catch (Exception ex) {
            //Something wrong happened, we will return the empty byte array
            Log.w("EditorActivity", ex);
        }
        return imageByte;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentBathToyUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveBathtoy();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mBathToyHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        if (!mBathToyHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                BathToysEntry._ID,
                BathToysEntry.COLUMN_BATHTOYS_IMAGE,
                BathToysEntry.COLUMN_BATHTOYS_NAME,
                BathToysEntry.COLUMN_BATHTOYS_QUANTITY,
                BathToysEntry.COLUMN_BATHTOYS_PRICE};

        return new CursorLoader(this,
                mCurrentBathToyUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int imageColumnIndex = cursor.getColumnIndex(BathToysEntry.COLUMN_BATHTOYS_IMAGE);
            int nameColumnIndex = cursor.getColumnIndex(BathToysEntry.COLUMN_BATHTOYS_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(BathToysEntry.COLUMN_BATHTOYS_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(BathToysEntry.COLUMN_BATHTOYS_PRICE);

            byte[] image = cursor.getBlob(imageColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);

            Bitmap petBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            if (image == null || image.length == 0) {
                mImageView.setImageResource(R.drawable.clean);
            } else {
                mImageView.setImageBitmap(petBitmap);
            }
            mNameEditText.setText(name);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Integer.toString(price));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this bath toy.
     */
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteBathtoy();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the bath toy in the database.
     */
    private void deleteBathtoy() {
        if (mCurrentBathToyUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentBathToyUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_bath_toy_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_bath_toy_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }
}