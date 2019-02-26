package com.ses.zebra.pssdemo_2019.Activities.SettingsActivities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.ses.zebra.pssdemo_2019.Activities.BaseActivity;
import com.ses.zebra.pssdemo_2019.Adapter.StockListAdapter;
import com.ses.zebra.pssdemo_2019.App;
import com.ses.zebra.pssdemo_2019.Debugging.Logger;
import com.ses.zebra.pssdemo_2019.Interfaces.EditOrDeleteStockItemCallback;
import com.ses.zebra.pssdemo_2019.POJOs.Meta;
import com.ses.zebra.pssdemo_2019.POJOs.StockItem;
import com.ses.zebra.pssdemo_2019.R;
import com.ses.zebra.pssdemo_2019.databinding.ActivityStockListBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StockListActivity extends BaseActivity {

    // Debugging
    private static final String TAG = "StockListActivity";

    // Constants
    private static final Gson mGson = new Gson();
    private static final String EDIT_STOCK_EXTRA = "stock-item-to-edit";
    private static final String mMetaFilePath = Environment.getExternalStorageDirectory()
            + File.separator + "PSSDemo" + File.separator + "Stock" + File.separator + "meta.json";
    private static final String mStockFilePath = Environment.getExternalStorageDirectory()
            + File.separator + "PSSDemo" + File.separator + "Stock" + File.separator + "stock.json";

    // Variables
    private View customLayout;
    private LinearLayout editStock;
    private LinearLayout deleteStock;

    private StockListAdapter mStockListAdapter;
    private AlertDialog editOrDeleteStockDialog;
    private ActivityStockListBinding mDataBinding;

    @Override
    protected String getInheritedTag() { return TAG; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init DataBinding
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_stock_list);

        // Init Title
        mDataBinding.headerLayout.headerText.setText("Configure Stock");
        mDataBinding.headerLayout.headerIcon.setImageResource(R.drawable.ic_back);
        mDataBinding.headerLayout.headerIcon.setOnClickListener(view ->
                NavUtils.navigateUpFromSameTask(this));

        // Init Add Stock Click Listener
        mDataBinding.addStockButton.setOnClickListener(view -> startActivity(new Intent(
                this, AddOrEditStock.class)));

        // Init Stock List Adapter
        mStockListAdapter = new StockListAdapter(stockItemCallback);
        mDataBinding.stockListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDataBinding.stockListRecyclerView.setAdapter(mStockListAdapter);

        // Get Dialog Views
        customLayout = getLayoutInflater().inflate(R.layout.dialog_layout_edit_or_delete_stock,
                null);
        editStock = customLayout.findViewById(R.id.editStockContainer);
        deleteStock = customLayout.findViewById(R.id.deleteStockContainer);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reload Recycler View
        mStockListAdapter.refreshList();
    }

    // Show Edit or Delete Dialog
    // Handle Stock Item Long-Click
    private EditOrDeleteStockItemCallback stockItemCallback = this::displayEditOrDeleteDialog;

    private void displayEditOrDeleteDialog(StockItem stockItem) {
        // Edit Stock Click Listener -> Start Edit Stock Activity
        editStock.setOnClickListener(view -> {
            editOrDeleteStockDialog.dismiss();
            startEditStockActivity(stockItem);
        });

        // Delete Stock Click Listener
        deleteStock.setOnClickListener(view -> {
            editOrDeleteStockDialog.dismiss();
            displayConfirmDeleteDialog(stockItem);
        });

        // Show Dialog if already created
        if (editOrDeleteStockDialog != null) {
            // Create & Show Dialog
            this.editOrDeleteStockDialog.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            this.editOrDeleteStockDialog.show();
            this.editOrDeleteStockDialog.getWindow().getDecorView().setSystemUiVisibility(
                    this.getWindow().getDecorView().getSystemUiVisibility());
            this.editOrDeleteStockDialog.getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            return;
        }

        // Build Dialog
        AlertDialog.Builder editOrDeleteStockDialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Choose Action")
                .setView(customLayout)
                .setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());

        // Create & Show Dialog
        this.editOrDeleteStockDialog = editOrDeleteStockDialogBuilder.create();
        this.editOrDeleteStockDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        this.editOrDeleteStockDialog.show();
        this.editOrDeleteStockDialog.getWindow().getDecorView().setSystemUiVisibility(
                this.getWindow().getDecorView().getSystemUiVisibility());
        this.editOrDeleteStockDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    private void displayConfirmDeleteDialog(StockItem stockItem) {
        AlertDialog.Builder confirmDeleteDialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("You are about to delete: '" + stockItem.getDescription() + "' from the " +
                        "stock list. This action cannot be done - are you sure you wish to proceed?")
                .setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton("DELETE", (dialogInterface, i) -> deleteStockItem(stockItem));

        // Create & Show Dialog
        AlertDialog confirmDeleteDialog = confirmDeleteDialogBuilder.create();
        confirmDeleteDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        confirmDeleteDialog.show();
        confirmDeleteDialog.getWindow().getDecorView().setSystemUiVisibility(
                this.getWindow().getDecorView().getSystemUiVisibility());
        confirmDeleteDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    private void deleteStockItem(StockItem stockItemToDelete) {
        // Create Copy of Stock / Image / Offer / Shopping List (In array as easier to delete)
        ArrayList<StockItem> tempStockItemList = App.mStockItems == null
                ? new ArrayList<>() : new ArrayList<>(Arrays.asList(App.mStockItems));

        if (App.mMeta == null) {
            App.mMeta = new Meta();
        }

        List<Meta.StockImage> tempStockImageList;
        if (App.mMeta.getStockImages() == null) {
            tempStockImageList = new ArrayList<>();
        } else {
            tempStockImageList = App.mMeta.getStockImages();
        }

        List<Meta.Offer> tempOffersList;
        if (App.mMeta.getOffers() == null) {
            tempOffersList = new ArrayList<>();
        } else {
            tempOffersList = App.mMeta.getOffers();
        }

        List<List<Meta.ShoppingList>> tempShoppingLists;
        if (App.mMeta.getShoppingLists() == null) {
            tempShoppingLists = new ArrayList<>();
        } else {
            tempShoppingLists = App.mMeta.getShoppingLists();
        }

        // Temp list to store items / images to remove (avoid ConcurrentModificationException)
        List<StockItem> itemsToRemove = new ArrayList<>();
        List<Meta.StockImage> imagesToRemove = new ArrayList<>();
        List<Meta.Offer> offersToRemove = new ArrayList<>();
        List<Meta.ShoppingList> shoppingListItemsToRemove = new ArrayList<>();

        // Loop through StockItems to find item to remove
        for (StockItem stockItem : tempStockItemList) {
            // Get Current Item from Iterator
            if (stockItem.getBarcode().equals(stockItemToDelete.getBarcode())) {
                // Add Stock Item to itemsToRemoveList (Ready for removal outside loop)
                itemsToRemove.add(stockItem);

                // Exit for loop
                break;
            }
        }

        // Loop through StockImages to find item to remove
        for (Meta.StockImage stockImage : tempStockImageList) {
            if (stockImage.getBarcode().equals(stockItemToDelete.getBarcode())) {
                // Add Stock Item to itemsToRemoveList (Ready for removal outside loop)
                imagesToRemove.add(stockImage);

                // Exit for loop
                break;
            }
        }

        // Loop through offers to find items to remove
        for (Meta.Offer offer : tempOffersList) {
            if (offer.getBarcode().equals(stockItemToDelete.getBarcode())) {
                offersToRemove.add(offer);
            }
        }

        // Loop through shopping list to find items to remove
        int arrayToRemoveFrom = 0;
        for (List<Meta.ShoppingList> shoppingList : tempShoppingLists) {
            for (Meta.ShoppingList shoppingListItem : shoppingList) {
                if (shoppingListItem.getBarcode().equals(stockItemToDelete.getBarcode())) {
                    arrayToRemoveFrom = tempShoppingLists.indexOf(shoppingList);
                    shoppingListItemsToRemove.add(shoppingListItem);
                    break;
                }
            }
        }

        // Remove all StockItems / Stock Images as required
        tempStockItemList.removeAll(itemsToRemove);
        tempStockImageList.removeAll(imagesToRemove);
        tempOffersList.removeAll(offersToRemove);
        tempShoppingLists.get(arrayToRemoveFrom).removeAll(shoppingListItemsToRemove);

        // Re-create stock items array
        App.mMeta.setOffers(tempOffersList);
        App.mMeta.setStockImages(tempStockImageList);
        App.mMeta.setShoppingLists(tempShoppingLists);
        App.mStockItems = tempStockItemList.toArray(new StockItem[0]);

        // Write new StockItems / StockImages to file
        updateMeta(App.mMeta);
        updateStockItems(App.mStockItems);
    }

    private void updateStockItems(StockItem[] stockItems) {
        // Over-write stock items file
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(mStockFilePath);
            fileOutputStream.write(mGson.toJson(stockItems).getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            Logger.e(TAG, "IOException: " + e.getMessage(), e);
        }

        // Reload Recycler View
        mStockListAdapter.refreshList();
    }

    private void updateMeta(Meta meta) {
        // Over-write stock items file
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(mMetaFilePath);
            fileOutputStream.write(mGson.toJson(meta).getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            Logger.e(TAG, "IOException: " + e.getMessage(), e);
        }
    }

    private void startEditStockActivity(StockItem stockItem) {
        Intent editStockActivity = new Intent(this, AddOrEditStock.class);
        editStockActivity.putExtra(EDIT_STOCK_EXTRA, stockItem);
        startActivity(editStockActivity);
    }

}
