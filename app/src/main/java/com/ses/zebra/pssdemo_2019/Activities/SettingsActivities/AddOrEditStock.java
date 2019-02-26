package com.ses.zebra.pssdemo_2019.Activities.SettingsActivities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.ses.zebra.pssdemo_2019.Activities.BaseActivity;
import com.ses.zebra.pssdemo_2019.App;
import com.ses.zebra.pssdemo_2019.Debugging.Logger;
import com.ses.zebra.pssdemo_2019.POJOs.Meta;
import com.ses.zebra.pssdemo_2019.POJOs.StockItem;
import com.ses.zebra.pssdemo_2019.POJOs.Sub.NutritionalInfoValues;
import com.ses.zebra.pssdemo_2019.R;
import com.ses.zebra.pssdemo_2019.Utilities.UriHelper;
import com.ses.zebra.pssdemo_2019.databinding.ActivityAddOrEditStockBinding;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.ScannerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AddOrEditStock extends BaseActivity implements AdapterView.OnItemSelectedListener,
        Scanner.DataListener {

    // Debugging
    private static final String TAG = "AddOrEditStock";

    // Constants
    private static final int PICK_IMAGE = 0;
    private static final Gson mGson = new Gson();
    private static final String EDIT_STOCK_EXTRA = "stock-item-to-edit";
    private static final String mStockImagesPath = Environment.getExternalStorageDirectory()
            + File.separator + "PSSDemo" + File.separator + "Stock" + File.separator + "images"
            + File.separator;
    private static final String mMetaFilePath = Environment.getExternalStorageDirectory()
            + File.separator + "PSSDemo" + File.separator + "Stock" + File.separator + "meta.json";
    private static final String mStockFilePath = Environment.getExternalStorageDirectory()
            + File.separator + "PSSDemo" + File.separator + "Stock" + File.separator + "stock.json";

    // Variables
    private boolean mEditMode;
    private StockItem mStockItem;
    private String mOriginalBarcode;
    private Integer mSpinnerDiscountPosition;
    private ActivityAddOrEditStockBinding mDataBinding;
    private String mSaveErrorExplanation = "Unknown Error";

    private Uri mSelectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init DataBinding
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_or_edit_stock);

        // Get whether we're adding new item, or editing existing
        mEditMode = isEditMode();

        // Create Stock Item if required
        if (!mEditMode) { mStockItem = new StockItem(); }

        // Init Form Listeners
        initListeners();

        // Init Title
        mDataBinding.headerLayout.headerText.setText(mEditMode ? "Edit Stock Item" : "Create Stock Item");
        mDataBinding.headerLayout.headerIcon.setImageResource(R.drawable.ic_back);
        mDataBinding.headerLayout.helpIcon.setImageResource(R.drawable.ic_save);
        mDataBinding.headerLayout.helpIcon.setOnClickListener(view -> saveStockItem());
        mDataBinding.headerLayout.headerIcon.setOnClickListener(view -> confirmBackNavigation());

        // Populate fields if in edit mode
        if (mEditMode) {
            populateFieldsWithExistingData();
        } else {
            mDataBinding.productImage.setImageDrawable(getResources()
                    .getDrawable(R.drawable.ic_add_image));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Enable Scanner
        mDataBinding.productPrice.postDelayed(this::enableScanner, 100);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Disable Scanner
        disableScanner();
    }

    @Override
    protected String getInheritedTag() {
        return TAG;
    }

    private void enableScanner() {
        final Scanner.DataListener dataListener = this;
        try {
            ((App) getApplicationContext()).enableScanner(dataListener);
        } catch (ScannerException e) {
            Log.e(TAG, "ScannerException: " + e.getMessage());
        }
    }

    private void disableScanner() {
        try {
            ((App) getApplicationContext()).disableScanner(this);
        } catch (ScannerException e) {
            Log.e(TAG, "ScannerException: " + e.getMessage());
        }
    }

    private boolean isEditMode() {
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().get(EDIT_STOCK_EXTRA) != null) {
                mStockItem = (StockItem) getIntent().getExtras().getSerializable(EDIT_STOCK_EXTRA);

                // Save barcode for reference when committing changes
                mOriginalBarcode = mStockItem == null ? "" : mStockItem.getBarcode();
                return true;
            }
        } return false;
    }

    private void confirmBackNavigation() {
        AlertDialog.Builder confirmExitdialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Confirm Exit")
                .setMessage("You are about to leave this page. All unsaved changes will be lost - " +
                        "Are you sure you want to exit?")
                .setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton("EXIT", (dialogInterface, i) ->
                        NavUtils.navigateUpFromSameTask(this));

        // Create & Show Dialog
        AlertDialog confirmExitDialog = confirmExitdialogBuilder.create();
        confirmExitDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        confirmExitDialog.show();
        confirmExitDialog.getWindow().getDecorView().setSystemUiVisibility(
                this.getWindow().getDecorView().getSystemUiVisibility());
        confirmExitDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    private void saveStockItem() {
        // Validate Field Input
        if (!validateInput()) { return; }

        // Create / Edit StockItem
        applyValuesToStockItem();

        // Write StockItem to file
        if (!commitChangesToFile()) {
            showSaveErrorDialog();
            return;
        }

        // Update Meta Image in Meta
        addImageToMeta();
        addItemToOffers();
        addItemToShoppingList();

        if (!updateMetaFile(App.mMeta)) {
            showSaveErrorDialog();
            return;
        }

        // Copy image to directory
        if (mSelectedImageUri != null) {
            if (!copyImageToInternalDirectory()) {
                showSaveErrorDialog();
                return;
            }
        }

        // Return to StockActivity
        NavUtils.navigateUpFromSameTask(this);
    }

    private void addImageToMeta() {
        // Create copy of array
        List<Meta.StockImage> stockImages = App.mMeta.getStockImages() == null ? new ArrayList<>()
                : App.mMeta.getStockImages();

        // Check if image already exists
        for (Meta.StockImage stockImage : stockImages) {
            if (stockImage.getBarcode().equals(mStockItem.getBarcode())) {
                return;
            }
        }

        // Create Stock Image
        Meta.StockImage stockImage = new Meta.StockImage();

        // Update Values
        stockImage.setBarcode(mStockItem.getBarcode());
        stockImage.setImageTag(mStockItem.getBarcode() + ".jpg");

        // Add to array
        stockImages.add(stockImage);

        // Add Stock Image to Meta
        App.mMeta.setStockImages(stockImages);
    }

    private void addItemToOffers() {
        // Create copy of array
        List<Meta.Offer> offers = App.mMeta.getOffers() == null ? new ArrayList<>()
                : App.mMeta.getOffers();

        // Check if product has offer
        if (mStockItem.getDiscount() == 0) {
            // holder array in case offer need to be removed
            List<Meta.Offer> offersToRemove = new ArrayList<>();

            // No offer -> check if product was previously in offer list
            for (Meta.Offer offer : offers) {
                if (offer.getBarcode().equals(mStockItem.getBarcode())) {
                    // Offer exists, remove from offer list
                    offersToRemove.add(offer);
                }
            }

            // remove Offer
            offers.removeAll(offersToRemove);

            // Update Meta
            App.mMeta.setOffers(offers);
            return;
        }

        // Check if offer already exists
        for (Meta.Offer offer : offers) {
            if (offer.getBarcode().equals(mStockItem.getBarcode())) {
                if (offer.getDiscount() == mStockItem.getDiscount()) {
                    return;
                }
            }
        }

        // Create Offer
        Meta.Offer offer = new Meta.Offer();

        // Update Values
        offer.setBarcode(mStockItem.getBarcode());
        offer.setDescription(mStockItem.getDescription());
        offer.setDiscount(mStockItem.getDiscount());
        offer.setImageTag(mStockItem.getBarcode() + ".jpg");
        offer.setOffer(getDiscountAsText());

        // Add to array
        offers.add(offer);

        // Add Offers to Meta
        App.mMeta.setOffers(offers);
    }

    private void addItemToShoppingList() {
        // Create copy of array
        List<List<Meta.ShoppingList>> shoppingLists = App.mMeta.getShoppingLists() == null
                ? new ArrayList<>() : App.mMeta.getShoppingLists();

        // Check item should be included
        if (!mDataBinding.productShoppingList.isChecked()) {
            // holder array in case offer need to be removed
            int arrayToRemoveFrom = 0;
            List<Meta.ShoppingList> shoppingListItemsToRemove = new ArrayList<>();

            for (List<Meta.ShoppingList> shoppingList : shoppingLists) {
                for (Meta.ShoppingList shoppingListItem : shoppingList) {
                    if (shoppingListItem.getBarcode().equals(mStockItem.getBarcode())) {
                        arrayToRemoveFrom = shoppingLists.indexOf(shoppingList);
                        shoppingListItemsToRemove.add(shoppingListItem);
                        break;
                    }
                }
            }

            // remove Offer
            shoppingLists.get(arrayToRemoveFrom).removeAll(shoppingListItemsToRemove);

            // Update Meta
            App.mMeta.setShoppingLists(shoppingLists);
            return;
        }

        // Check if item already exists in any list
        for (List<Meta.ShoppingList> shoppingList : shoppingLists) {
            for (Meta.ShoppingList shoppingListItem : shoppingList) {
                if (shoppingListItem.getBarcode().equals(mStockItem.getBarcode())) {
                    return;
                }
            }
        }

        // Create Stock Image
        Meta.ShoppingList shoppingListItem = new Meta.ShoppingList();

        // Update Values
        shoppingListItem.setBarcode(mStockItem.getBarcode());
        shoppingListItem.setDescription(mStockItem.getDescription());
        shoppingListItem.setPrice(mStockItem.getPrice());

        // Add to array
        if (shoppingLists.size() <= 0) {
            List<Meta.ShoppingList> randomShoppingList = new ArrayList<>();
            randomShoppingList.add(shoppingListItem);
            shoppingLists.add(randomShoppingList);
        } else {
            int randomArray = new Random().nextInt(shoppingLists.size());
            List<Meta.ShoppingList> randomShoppingList = shoppingLists.get(randomArray);
            randomShoppingList.add(shoppingListItem);
            shoppingLists.set(randomArray, randomShoppingList);
        }

        // Add Stock Image to Meta
        App.mMeta.setShoppingLists(shoppingLists);
    }

    private String getDiscountAsText() {
        if (mStockItem.getDiscount() == -1.50) {
            return "One fifty off!";
        } else if (mStockItem.getDiscount() == 0.1) {
            return "10% Off!";
        } else if (mStockItem.getDiscount() == 0.15) {
            return "15% Off!";
        } else if (mStockItem.getDiscount() == 0.2) {
            return "20% Off!";
        } else if (mStockItem.getDiscount() == 0.3) {
            return "30% Off!";
        } else if (mStockItem.getDiscount() == 0.5) {
            return "50% Off!";
        } return "";
    }

    private boolean validateInput() {
        // Holder variables
        boolean validInput = true;

        if (mDataBinding.productBarcode.getText().toString().equals("")) {
            validInput = false;
            mDataBinding.productBarcode.setError("Enter a barcode");
        }

        if (mDataBinding.productDescription.getText().toString().equals("")) {
            validInput = false;
            mDataBinding.productDescription.setError("Enter product name");
        }

        if (mDataBinding.productPrice.getText().toString().equals("")) {
            validInput = false;
            mDataBinding.productPrice.setError("Enter price");
        }

        if (mDataBinding.productSize.getText().toString().equals("")) {
            validInput = false;
            mDataBinding.productSize.setError("Enter size in grams");
        }

        if (mSpinnerDiscountPosition == null) {
            validInput = false;
            ((TextView) mDataBinding.productDiscount.getSelectedView()).setError("Select discount");
        }

        return validInput;
    }

    private void applyValuesToStockItem() {
        // Clean Barcode
        String barcode = mDataBinding.productBarcode.getText().toString()
                .replace("\\", "")
                .replace("/", "")
                .trim();

        // Set Values
        mStockItem.setBarcode(barcode);
        mStockItem.setDescription(mDataBinding.productDescription.getText().toString());
        mStockItem.setSize(Double.parseDouble(mDataBinding.productSize.getText().toString()));
        mStockItem.setPrice(Double.parseDouble(mDataBinding.productPrice.getText().toString()));
        mStockItem.setDiscount(getDiscountFromSpinner());

        // Create Random Ingredients
        List<String> ingredients = new ArrayList<>(Arrays.asList(mStockItem.getIngredientsList()));
        Collections.shuffle(ingredients);
        mStockItem.setIngredients(new ArrayList<>(ingredients.subList(0,
                new Random().nextInt(ingredients.size() - 4) + 4)));
    }

    private void showSaveErrorDialog() {
        AlertDialog.Builder errorSavingDialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Error Saving Stock Item")
                .setMessage("There was an error saving this item:\n\n" + mSaveErrorExplanation
                            + "\n\nPlease try again.")
                .setIcon(R.drawable.ic_error)
                .setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton("RETRY", (dialogInterface, i) -> saveStockItem());

        // Create & Show Dialog
        AlertDialog errorSavingDialog = errorSavingDialogBuilder.create();
        errorSavingDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        errorSavingDialog.show();
        errorSavingDialog.getWindow().getDecorView().setSystemUiVisibility(
                this.getWindow().getDecorView().getSystemUiVisibility());
        errorSavingDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    private double getDiscountFromSpinner() {
        // Get Discount from spinner
        switch ((String) mDataBinding.productDiscount.getItemAtPosition(mSpinnerDiscountPosition)) {
            case "-1.50": return -1.50;
            case "10%": return 0.1;
            case "15%": return 0.15;
            case "20%": return 0.2;
            case "30%": return 0.3;
            case "50%": return 0.5;
            default: return 0;
        }
    }

    private boolean commitChangesToFile() {
        // New StockList
        List<StockItem> stockItems = App.mStockItems == null ? new ArrayList<>() :
                new ArrayList<>(Arrays.asList(App.mStockItems));

        // Position Holder
        int stockItemIndex = stockItems.size();

        // Temp list to store items to remove (avoid ConcurrentModificationException)
        List<StockItem> itemsToRemove = new ArrayList<>();

        // Get Stock Item
        for (StockItem stockItem : stockItems) {
            // Trying to add product with existing barcode
            if (!mStockItem.getBarcode().equals(mOriginalBarcode)
                    && stockItem.getBarcode().equals(mStockItem.getBarcode())) {
                mSaveErrorExplanation = "A product with this barcode already exists";
                return false;
            }

            // Get Current Item from Iterator
            if (stockItem.getBarcode().equals(mOriginalBarcode)) {
                // Save Place
                stockItemIndex = stockItems.indexOf(stockItem);

                // Add Stock Item to itemsToRemoveList (Ready for removal outside loop)
                itemsToRemove.add(stockItem);

                // Exit for loop
                break;
            }
        }

        // Remove Item from StockList
        stockItems.removeAll(itemsToRemove);

        // Add new Stock Item
        stockItems.add(stockItemIndex, mStockItem);

        // Re-create StockItems List
        App.mStockItems = stockItems.toArray(new StockItem[0]);

        // Save List to file
        return updateStockItems(App.mStockItems);
    }

    private boolean updateStockItems(StockItem[] stockItems) {
        // Over-write stock items file
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(mStockFilePath);
            fileOutputStream.write(mGson.toJson(stockItems).getBytes());
            fileOutputStream.close();
            return true;
        } catch (IOException e) {
            mSaveErrorExplanation = "Memory too low to save changes to file";
            Logger.e(TAG, "IOException: " + e.getMessage(), e);
        }

        // Error
        return false;
    }

    private boolean updateMetaFile(Meta meta) {
        // Over-write stock items file
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(mMetaFilePath);
            fileOutputStream.write(mGson.toJson(meta).getBytes());
            fileOutputStream.close();
            return true;
        } catch (IOException e) {
            mSaveErrorExplanation = "Memory too low to save changes to file";
            Logger.e(TAG, "IOException: " + e.getMessage(), e);
        }

        // Error
        return false;
    }

    private void initListeners() {
        // Init Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.discounts, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDataBinding.productDiscount.setAdapter(adapter);
        mDataBinding.productDiscount.setOnItemSelectedListener(this);

        // Init Icon Listener
        mDataBinding.productImage.setOnClickListener(view -> selectImage());
    }

    private void populateFieldsWithExistingData() {
        mDataBinding.productBarcode.setText(mStockItem.getBarcode());
        mDataBinding.productDescription.setText(mStockItem.getDescription());
        mDataBinding.productPrice.setText(String.valueOf(mStockItem.getPrice()));
        mDataBinding.productSize.setText(String.valueOf(mStockItem.getSize()));
        // Set discount
        if (mStockItem.getDiscount() == -1.50){
            mDataBinding.productDiscount.setSelection(1);
        } else if (mStockItem.getDiscount() == 0.10) {
            mDataBinding.productDiscount.setSelection(2);
        } else if (mStockItem.getDiscount() == 0.15) {
            mDataBinding.productDiscount.setSelection(3);
        } else if (mStockItem.getDiscount() == 0.2) {
            mDataBinding.productDiscount.setSelection(4);
        } else if (mStockItem.getDiscount() == 0.3) {
            mDataBinding.productDiscount.setSelection(5);
        } else if (mStockItem.getDiscount() == 0.5){
            mDataBinding.productDiscount.setSelection(6);
        } else {
            mDataBinding.productDiscount.setSelection(0);
        }
        // Check if product is in shopping list
        List<List<Meta.ShoppingList>> shoppingLists = App.mMeta.getShoppingLists();
        for (List<Meta.ShoppingList> shoppingList : shoppingLists) {
            for (Meta.ShoppingList shoppingListItem : shoppingList) {
                if (shoppingListItem.getBarcode().equals(mStockItem.getBarcode())) {
                    mDataBinding.productShoppingList.setChecked(true);
                }
            }
        }
        // Display Image
        if (App.mMeta != null && App.mMeta.getStockImages() != null) {
            for (Meta.StockImage stockImage : App.mMeta.getStockImages()) {
                if (stockImage.getBarcode().equalsIgnoreCase(mStockItem.getBarcode())) {
                    File imageFile = new File(App.mStockImagesPath + stockImage.getImageTag());
                    if (imageFile.exists()) {
                        Bitmap stockImageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                        mDataBinding.productImage.setImageBitmap(stockImageBitmap);
                    } else {
                        mDataBinding.productImage.setImageDrawable(getResources()
                                .getDrawable(R.drawable.ic_add_image));
                    }
                }
            }
        }
    }

    private void selectImage() {
        Intent selectImageIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        selectImageIntent.addCategory(Intent.CATEGORY_OPENABLE);
        selectImageIntent.setType("image/jpeg");
        startActivityForResult(selectImageIntent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                mSelectedImageUri = resultData.getData();
                mDataBinding.productImage.setImageURI(mSelectedImageUri);
            }
        }
    }

    private boolean copyImageToInternalDirectory() {
        File newImage = new File(mStockImagesPath + mStockItem.getBarcode() + ".jpg");
        File originalImage = new File(UriHelper.getPath(this, mSelectedImageUri));
        try {
            if (!newImage.exists()) {
                newImage.createNewFile();
            }
            InputStream initialStream = new FileInputStream(originalImage);
            byte[] buffer = new byte[initialStream.available()];
            initialStream.read(buffer);
            OutputStream outStream = new FileOutputStream(newImage);
            outStream.write(buffer);
            return true;
        } catch (IOException e) {
            mSaveErrorExplanation = e.getMessage();
            Logger.e(TAG, "IOException: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> spinner, View view, int pos, long l) {
        // Save Spinner Position
        mSpinnerDiscountPosition = pos;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // Required empty Callback
    }

    @Override
    public void onData(ScanDataCollection scanDataCollection) {
        // Get Scanner Data as []
        ScanDataCollection.ScanData[] scannedData = scanDataCollection.getScanData().toArray(
                new ScanDataCollection.ScanData[0]);

        // If not End shop -> Parse Barcode
        new getBarcode(this).execute(scannedData);
    }

    public static class getBarcode extends AsyncTask<ScanDataCollection.ScanData, Void, String> {

        private WeakReference<AddOrEditStock> activityWeakReference;

        getBarcode(AddOrEditStock activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected String doInBackground(ScanDataCollection.ScanData... scanDataArray) {
            return scanDataArray[0].getData();
        }

        @Override
        protected void onPostExecute(String barcode) {
            // Get context
            AddOrEditStock activityContext = activityWeakReference.get();
            if (activityContext == null || activityContext.isFinishing()) return;

            // Set Barcode
            activityContext.mDataBinding.productBarcode.setText(barcode == null ? "" : barcode);
        }
    }
}
