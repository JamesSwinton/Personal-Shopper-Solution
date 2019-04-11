package com.ses.zebra.pssdemo_2019.Activities.SettingsActivities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import com.google.gson.Gson;
import com.ses.zebra.pssdemo_2019.Activities.BaseActivity;
import com.ses.zebra.pssdemo_2019.App;
import com.ses.zebra.pssdemo_2019.Debugging.Logger;
import com.ses.zebra.pssdemo_2019.POJOs.Geofencing.GeofenceData;
import com.ses.zebra.pssdemo_2019.POJOs.Geofencing.PopUpData;
import com.ses.zebra.pssdemo_2019.POJOs.Geofencing.PopUpRegion;
import com.ses.zebra.pssdemo_2019.R;
import com.ses.zebra.pssdemo_2019.Utilities.UriHelper;
import com.ses.zebra.pssdemo_2019.databinding.ActivityGeofenceSettingsBinding;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeofenceSettingsActivity extends BaseActivity {

  // Debugging
  private static final String TAG = "GeoSettingsActivity";

  // Constants
  private static final int PICK_IMAGE = 1002;
  private static final int GEOFENCE_ACTIVITY =  1001;
  private static final String GEOFENCE_DATA = "geofence-data";
  private static final String mGeofenceFilePath = Environment.getExternalStorageDirectory()
      + File.separator + "PSSDemo" + File.separator + "Stock" + File.separator + "geofence.json";
  private static final String mPopUpImagesPath = Environment.getExternalStorageDirectory()
      + File.separator + "PSSDemo" + File.separator + "Stock" + File.separator + "geofence-images"
      + File.separator;

  // Static Variables
  private static String mRegionImage;
  private static PopUpRegion mPopUpRegion;

  // Non-Static Variables
  private ActivityGeofenceSettingsBinding mDataBinding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Init DataBinding
    mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_geofence_settings);

    // Init Back Listener
    mDataBinding.headerIcon.setOnClickListener(view -> confirmBackNavigation());

    // Init Switch Listener for Check boxes (Optional message / icon)
    initDisplayOptionsListener();

    // Init Geofence Listener
    mDataBinding.defineGeofence.setOnClickListener(view -> startCreateGeofenceActivity());

    // Init Save Listener
    mDataBinding.saveIcon.setOnClickListener(view -> createRegionPopup());

    // Init Image Listener
    mDataBinding.image.setOnClickListener(view -> selectImage());

    // Create New Region Object
    mPopUpRegion = new PopUpRegion();
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (mPopUpRegion.getGeoFenceData() != null && mPopUpRegion.getGeoFenceData().getVertexPoints().size() == 360) {
      mDataBinding.defineGeofence.setText("RE-DEFINE GEOFENCE");
      mDataBinding.defineGeofence.setBackgroundColor(getResources().getColor(R.color.zebraGreen));
    } else {
      mDataBinding.defineGeofence.setText("DEFINE GEOFENCE");
      mDataBinding.defineGeofence.setBackgroundColor(getResources().getColor(R.color.zebraRed));
    }
  }

  private void confirmBackNavigation() {
    AlertDialog.Builder confirmExitDialogBuilder = new AlertDialog.Builder(this)
        .setTitle("Confirm Exit")
        .setMessage("You are about to leave this page. All unsaved changes will be lost - " +
            "Are you sure you want to exit?")
        .setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss())
        .setPositiveButton("EXIT", (dialogInterface, i) ->
            NavUtils.navigateUpFromSameTask(this));

    // Create & Show Dialog
    AlertDialog confirmExitDialog = confirmExitDialogBuilder.create();
    confirmExitDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    confirmExitDialog.show();
    confirmExitDialog.getWindow().getDecorView().setSystemUiVisibility(
        this.getWindow().getDecorView().getSystemUiVisibility());
    confirmExitDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
  }

  private void initDisplayOptionsListener() {
    mDataBinding.displayPopUpIcon.setOnCheckedChangeListener((compoundButton, checked) ->
        mDataBinding.image.setVisibility(checked ? View.VISIBLE : View.GONE));

    mDataBinding.displayPopUpMessage.setOnCheckedChangeListener((compoundButton, checked) ->
        mDataBinding.popUpMessage.setVisibility(checked ? View.VISIBLE : View.GONE));
  }

  private void startCreateGeofenceActivity() {
    startActivityForResult(new Intent(GeofenceSettingsActivity.this,
            CreateGeofenceActivity.class), GEOFENCE_ACTIVITY);
  }

  private void selectImage() {
    Intent selectImageIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    selectImageIntent.addCategory(Intent.CATEGORY_OPENABLE);
    selectImageIntent.setType("image/jpeg");
    startActivityForResult(selectImageIntent, PICK_IMAGE);
  }

  private boolean copyImageToInternalDirectory(Uri imagePath) {
    // Create Directory
    File popUpImagesDirectory = new File(mPopUpImagesPath);
    if (!popUpImagesDirectory.exists()) {
      popUpImagesDirectory.mkdirs();
    }

    // Create File
    File newImage = new File(mPopUpImagesPath + System.currentTimeMillis() + ".jpg");
    File originalImage = new File(UriHelper.getPath(this, imagePath));
    try {
      if (!newImage.exists()) {
        newImage.createNewFile();
      }
      InputStream initialStream = new FileInputStream(originalImage);
      byte[] buffer = new byte[initialStream.available()];
      initialStream.read(buffer);
      OutputStream outStream = new FileOutputStream(newImage);
      outStream.write(buffer);

      // Save to Region
      mRegionImage = newImage.getAbsolutePath();
      return true;
    } catch (IOException e) {
      Logger.e(TAG, "IOException: " + e.getMessage(), e);
      return false;
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK) {
      if (requestCode == GEOFENCE_ACTIVITY) {
        if (data.getSerializableExtra(GEOFENCE_DATA) != null) {
          mPopUpRegion.setGeoFenceData((GeofenceData) data.getSerializableExtra(GEOFENCE_DATA));
        } else {
          Log.e(TAG, "Geofence Data not Available!");
          Toast.makeText(this, "Geofence not Created", Toast.LENGTH_SHORT).show();
        }
      } else if (requestCode == PICK_IMAGE) {
        mDataBinding.image.setImageURI(data.getData());
        copyImageToInternalDirectory(data.getData());
      }
    }
  }

  private void createRegionPopup() {
    // Validate Input
    if (!validatePopupData()) {
      return;
    }
  }

  private boolean validatePopupData() {
    // Validate Title
    if (TextUtils.isEmpty(mDataBinding.popUpTitle.getText())) {
      mDataBinding.popUpTitle.setError("Please enter a title");
      return false;
    }

    // Check Number is Int and between 3 -> 120
    try {
      if (Integer.parseInt(mDataBinding.popUpDisplayTime.getText().toString()) > 120 ||
          Integer.parseInt(mDataBinding.popUpDisplayTime.getText().toString()) < 3) {
        mDataBinding.popUpDisplayTime.setError("Please enter a integer between 3 and 120");
        return false;
      }
    } catch (NumberFormatException e) {
      mDataBinding.popUpDisplayTime.setError("Please enter a integer between 3 and 120");
      return false;
    }

    // Check Message
    if (mDataBinding.displayPopUpMessage.isChecked() &&
        TextUtils.isEmpty(mDataBinding.popUpMessage.getText())) {
      mDataBinding.popUpMessage.setError("Please enter a message");
      return false;
    }

    if (mDataBinding.displayPopUpIcon.isChecked()
        && mDataBinding.image.getDrawable() == getResources().getDrawable(R.drawable.ic_add_image)) {
      showDialog("Error!", "Please make sure to add an image.", false);
      return false;
    }

    // Check Region
    if (mPopUpRegion.getGeoFenceData() == null ||
        mPopUpRegion.getGeoFenceData().getCenterPoint() == null ||
        mPopUpRegion.getGeoFenceData().getVertexPoints() == null ||
        mPopUpRegion.getGeoFenceData().getVertexPoints().size() != 360) {
      showDialog("Error!", "Please make sure you've created a geo-fence.", false);
      return false;
    }

    // Create Region Data
    PopUpData popUpData = new PopUpData();
    popUpData.setTitle(mDataBinding.popUpTitle.getText().toString());
    popUpData.setDisplayTimeSeconds(Integer.parseInt(mDataBinding.popUpDisplayTime.getText().toString()));
    if (mDataBinding.displayPopUpMessage.isChecked()) {
      popUpData.setMessage(mDataBinding.popUpMessage.getText().toString());
    }
    if (mDataBinding.displayPopUpIcon.isChecked()) {
      popUpData.setImage(mRegionImage);
    }

    // Store Region Data
    mPopUpRegion.setPopUpData(popUpData);

    // Store In App
    commitChangesToFile(mPopUpRegion);

    // Close Activity & Return
    NavUtils.navigateUpFromSameTask(this);
    return true;
  }

  private boolean commitChangesToFile(PopUpRegion popUpRegion) {
    // New StockList
    List<PopUpRegion> popUpRegions = App.mPopUpRegions == null ? new ArrayList<>() :
        new ArrayList<>(Arrays.asList(App.mPopUpRegions));

    // Position Holder
    int popUpRegionIndex = popUpRegions.size();

    // Add new Stock Item
    popUpRegions.add(popUpRegionIndex, popUpRegion);

    // Re-create StockItems List
    App.mPopUpRegions = popUpRegions.toArray(new PopUpRegion[0]);

    // Save List to file
    return updatePopUpRegions(App.mPopUpRegions);
  }

  private boolean updatePopUpRegions(PopUpRegion[] popUpRegions) {
    // Over-write stock items file
    try {
      FileOutputStream fileOutputStream = new FileOutputStream(mGeofenceFilePath);
      fileOutputStream.write(new Gson().toJson(popUpRegions).getBytes());
      fileOutputStream.close();
      return true;
    } catch (IOException e) {
      Logger.e(TAG, "IOException: " + e.getMessage(), e);
    }

    // Error
    Toast.makeText(this, "Error saving Geofence to File", Toast.LENGTH_LONG).show();
    return false;
  }

  private void showDialog(String title, String message, boolean success) {
    // Build Dialog
    AlertDialog.Builder genericDialogBuilder = new AlertDialog.Builder(GeofenceSettingsActivity.this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("OK", null)
        .setIcon(success ? R.drawable.ic_success : R.drawable.ic_error);

    // Show Dialog without showing navigation
    displayDialogImmersive(genericDialogBuilder);
  }

  private void displayDialogImmersive(AlertDialog.Builder builder) {
    AlertDialog genericDialog = builder.create();
    genericDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    genericDialog.show();
    genericDialog.getWindow().getDecorView().setSystemUiVisibility(
        GeofenceSettingsActivity.this.getWindow().getDecorView().getSystemUiVisibility());
    genericDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
  }

  @Override
  protected String getInheritedTag() { return TAG; }
}
