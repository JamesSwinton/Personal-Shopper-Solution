package com.ses.zebra.pssdemo_2019.Activities.SettingsActivities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import com.google.gson.Gson;
import com.ses.zebra.pssdemo_2019.Activities.BaseActivity;
import com.ses.zebra.pssdemo_2019.Adapter.GeofenceListAdapter;
import com.ses.zebra.pssdemo_2019.Adapter.StockListAdapter;
import com.ses.zebra.pssdemo_2019.App;
import com.ses.zebra.pssdemo_2019.Debugging.Logger;
import com.ses.zebra.pssdemo_2019.Interfaces.EditOrDeleteGeofenceCallback;
import com.ses.zebra.pssdemo_2019.Interfaces.EditOrDeleteStockItemCallback;
import com.ses.zebra.pssdemo_2019.POJOs.Geofencing.PopUpRegion;
import com.ses.zebra.pssdemo_2019.POJOs.Meta;
import com.ses.zebra.pssdemo_2019.POJOs.Meta.ShoppingList;
import com.ses.zebra.pssdemo_2019.POJOs.StockItem;
import com.ses.zebra.pssdemo_2019.R;
import com.ses.zebra.pssdemo_2019.databinding.ActivityGeofenceListBinding;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeofenceListActivity extends BaseActivity {

  // Debugging
  private static final String TAG = "GeofenceListActivity";

  // Constants
  private static final String mGeofenceFilePath = Environment.getExternalStorageDirectory()
      + File.separator + "PSSDemo" + File.separator + "Stock" + File.separator + "geofence.json";

  // Static Variables

  // Non-Static Variables
  private ActivityGeofenceListBinding mDataBinding;

  private GeofenceListAdapter mGeofenceListAdapter;

  private View customLayout;
  private LinearLayout editStock;
  private LinearLayout deleteStock;
  private AlertDialog editOrDeleteStockDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_geofence_list);

    mDataBinding.headerIcon.setOnClickListener(view ->
        NavUtils.navigateUpFromSameTask(this));

    // Init Add Geofence Click Listener
    mDataBinding.addGeofenceButton.setOnClickListener(view -> startActivity(new Intent(
        this, GeofenceSettingsActivity.class)));

    // Init Stock List Adapter
    mGeofenceListAdapter = new GeofenceListAdapter(stockItemCallback);
    mDataBinding.geofenceListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    mDataBinding.geofenceListRecyclerView.setAdapter(mGeofenceListAdapter);

    // Get Dialog Views
    customLayout = getLayoutInflater().inflate(R.layout.dialog_layout_edit_or_delete_stock,
        null);
    editStock = customLayout.findViewById(R.id.editStockContainer);
    deleteStock = customLayout.findViewById(R.id.deleteStockContainer);
  }

  private EditOrDeleteGeofenceCallback stockItemCallback = this::displayConfirmDeleteDialog;

  private void displayEditOrDeleteDialog(PopUpRegion popUpRegion) {
    // Edit Stock Click Listener -> Start Edit Stock Activity
    editStock.setOnClickListener(view -> {
      editOrDeleteStockDialog.dismiss();
      // startEditStockActivity(stockItem);
    });

    // Delete Stock Click Listener
    deleteStock.setOnClickListener(view -> {
      editOrDeleteStockDialog.dismiss();
      displayConfirmDeleteDialog(popUpRegion);
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

  private void displayConfirmDeleteDialog(PopUpRegion popUpRegion) {
    AlertDialog.Builder confirmDeleteDialogBuilder = new AlertDialog.Builder(this)
        .setTitle("Confirm Deletion")
        .setMessage("You are about to delete: '" + popUpRegion.getPopUpData().getTitle() + "' from the " +
            "geofence list. This action cannot be done - are you sure you wish to proceed?")
        .setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss())
        .setPositiveButton("DELETE", (dialogInterface, i) -> deleteStockItem(popUpRegion));

    // Create & Show Dialog
    AlertDialog confirmDeleteDialog = confirmDeleteDialogBuilder.create();
    confirmDeleteDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    confirmDeleteDialog.show();
    confirmDeleteDialog.getWindow().getDecorView().setSystemUiVisibility(
        this.getWindow().getDecorView().getSystemUiVisibility());
    confirmDeleteDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
  }

  private void deleteStockItem(PopUpRegion popUpRegionToDelete) {
    // Create Copy of Stock / Image / Offer / Shopping List (In array as easier to delete)
    List<PopUpRegion> tempPopUpRegionList = App.mPopUpRegions == null
        ? new ArrayList<>() : new ArrayList<>(Arrays.asList(App.mPopUpRegions));

    // Temp list to store items / images to remove (avoid ConcurrentModificationException)
    List<PopUpRegion> itemsToRemove = new ArrayList<>();

    // Loop through StockItems to find item to remove
    for (PopUpRegion popUpRegion : tempPopUpRegionList) {
      // Get Current Item from Iterator
      if (popUpRegion.getId() == popUpRegionToDelete.getId()) {
        // Add Stock Item to itemsToRemoveList (Ready for removal outside loop)
        itemsToRemove.add(popUpRegion);
        // Exit for loop
        break;
      }
    }

    // Remove all StockItems / Stock Images as required
    tempPopUpRegionList.removeAll(itemsToRemove);

    // Re-create stock items array
    App.mPopUpRegions = tempPopUpRegionList.toArray(new PopUpRegion[0]);

    // Write new StockItems / StockImages to file
    updatePopUpRegions(App.mPopUpRegions);
  }

  private void updatePopUpRegions(PopUpRegion[] popUpRegions) {
    // Over-write stock items file
    try {
      FileOutputStream fileOutputStream = new FileOutputStream(mGeofenceFilePath);
      fileOutputStream.write(new Gson().toJson(popUpRegions).getBytes());
      fileOutputStream.close();
    } catch (IOException e) {
      Logger.e(TAG, "IOException: " + e.getMessage(), e);
    }

    // Reload Recycler View
    mGeofenceListAdapter.refreshList();
  }

  @Override
  protected String getInheritedTag() {
    return TAG;
  }
}
