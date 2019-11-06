package com.ses.zebra.pssdemo_2019.Activities.SettingsActivities;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.SizeF;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import com.philips.indoormaps.logic.MapFragment;
import com.philips.indoormaps.map.Annotation;
import com.philips.indoormaps.map.Location;
import com.philips.indoormaps.map.Map;
import com.philips.indoormaps.map.OnAnnotationTouchListener;
import com.philips.indoormaps.map.OnMapReadyCallback;
import com.philips.indoormaps.map.OnMapStatusChangedListener;
import com.philips.indoormaps.map.OnMapTouchListener;
import com.philips.indoormaps.map.UserLocationStatus;
import com.philips.indoorpositioning.library.IndoorPositioning;
import com.ses.zebra.pssdemo_2019.Activities.BaseActivity;
import com.ses.zebra.pssdemo_2019.App;
import com.ses.zebra.pssdemo_2019.Debugging.Logger;
import com.ses.zebra.pssdemo_2019.Fragments.NoMapFragment;
import com.ses.zebra.pssdemo_2019.Interfaces.WifiEnabledCallback;
import com.ses.zebra.pssdemo_2019.POJOs.Geofencing.GeofenceData;
import com.ses.zebra.pssdemo_2019.POJOs.Geofencing.VertexPoint;
import com.ses.zebra.pssdemo_2019.R;
import com.ses.zebra.pssdemo_2019.Utilities.GeofenceHelper;
import com.ses.zebra.pssdemo_2019.databinding.ActivityCreateGeofenceBinding;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CreateGeofenceActivity extends BaseActivity {

  // Debugging
  private static final String TAG = "CreateGeofenceActivity";

  // Constants
  private static final Handler mHandler = new Handler();

  private static final SizeF mAnnotationBitmapSize = new SizeF(0.01f, 0.01f);

  private static final String GEOFENCE_DATA = "geofence-data";
  private static final String MAP_FILE_PATH = Environment.getExternalStorageDirectory()
      + File.separator + "PSSDemo" + File.separator + "Stock" + File.separator + "map.bin";

  // Static Variables
  private Map mIndoorMap;
  private GeofenceData mGeofenceData;

  private Bitmap mRegionBitmap;
  private Bitmap mAnnotationBitmap;

  private WifiManager mWifiManager;
  private int mNumOfEnabledChecks = 0;
  private int mNumOfConnectedChecks = 0;
  private final int mMaxNumOfChecks = 100;
  private final int mDelayBetweenChecks = 100;
  private ConnectivityManager mConnectivityManager;

  // Non-Static Variables
  private ActivityCreateGeofenceBinding mDataBinding;

  private MapFragment mMapFragment;
  private IndoorPositioning mIndoorPositioning;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_create_geofence);

    // Init Objects
    mMapFragment = new MapFragment();
    mGeofenceData = new GeofenceData();
    mRegionBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.region);
    mAnnotationBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_annotation);
    mDataBinding.headerIcon.setOnClickListener(view -> confirmBackNavigation());

    // Init Save Listener
    mDataBinding.confirm.setOnClickListener(view -> returnGeofenceObjectAndExit());

    // Setup Indoor Positioning
    initialiseIndoorPositioning();

    // Load Map
    loadMap();
  }

  @Override
  protected void onResume() {
    super.onResume();
    // Register Indoor Positioning Listener
    if (mIndoorPositioning != null) {
      mIndoorPositioning.register(indoorPositioningListener, mHandler);

      // Start Indoor Positioning
      mIndoorPositioning.start();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    // Stop Indoor Position & Unregister Listener
    if (mIndoorPositioning != null) {
      if (mIndoorPositioning.isRunning()) {
        mIndoorPositioning.stop();
      }
      mIndoorPositioning.unregister();
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

  private void initialiseIndoorPositioning() {
    String configString = getConfig();
    Logger.i(TAG, "Init VLC with config string: " + configString);
    mIndoorPositioning = new IndoorPositioning(this);
    mIndoorPositioning.setConfiguration(getConfig());
    mIndoorPositioning.setMode(IndoorPositioning.IndoorPositioningMode.DEFAULT);
    mIndoorPositioning.setHeadingOrientation(IndoorPositioning.IndoorPositioningHeadingOrientation.PORTRAIT);
  }

  private String getConfig() {
    // Get Config String
    String configString = mSharedPreferences.getString(PREF_VLC_CONFIG_STRING,
        getString(R.string.app_configuration_eu));

    // Validate Config String
    try {
      Base64.decode(configString, Base64.DEFAULT);
    } catch (Exception e) {
      Logger.e(TAG, e.getMessage(), e);
      return getString(R.string.app_configuration_eu);
    }

    // Return Config
    return configString;
  }

  private void loadMap() {
    // Show Progress Bar
    mDataBinding.mapContainer.setVisibility(View.GONE);
    mDataBinding.mapProgress.setVisibility(View.VISIBLE);

    // Get Map
    String mapFilePath = new File(MAP_FILE_PATH).exists() ? MAP_FILE_PATH : null;

    // Remove Progress Bar
    mDataBinding.mapContainer.setVisibility(View.VISIBLE);
    mDataBinding.mapProgress.setVisibility(View.GONE);

    if (mapFilePath == null) {
      // Notify
      Logger.i(TAG, "Error loading Map File");
      Toast.makeText(this, "Could not load Map File", Toast.LENGTH_LONG).show();
      // Replace fragment with Error Fragment
      getSupportFragmentManager()
          .beginTransaction().replace(R.id.map_container, NoMapFragment.newInstance())
          .commit();
      return;
    }

    // Load Map
    mMapFragment.loadMap(mapFilePath, onMapReadyCallback);

    // Show Map Fragment
    getSupportFragmentManager().beginTransaction().replace(R.id.map_container, mMapFragment)
        .commit();
  }

  private OnMapReadyCallback onMapReadyCallback = new OnMapReadyCallback() {
    @Override
    public void onMapReady(Map map) {
      Logger.i(TAG, "Map Ready");
      mIndoorMap = com.philips.indoormaps.map.Map.getInstance();
      mIndoorMap.setOnMapTouchListener(onMapTouchListener);
      mIndoorMap.setOnMapStatusChangedListener(onMapStatusChangedListener);
      mIndoorMap.setOnAnnotationTouchListener(onMapAnnotationTouchListener);
      mIndoorMap.setStyle("{\"userLocationColor\":\"#007CB0\", \"routeLineColor\":\"#007CB0\", " +
              "\"floorSelectionColor\":\"#007CB0\"}");

      // Remove Existing Annotation
      for (Annotation annotation : mIndoorMap.getAnnotations(0)) {
        mIndoorMap.removeAnnotation(annotation);
      }
    }
  };

  private OnAnnotationTouchListener onMapAnnotationTouchListener = new OnAnnotationTouchListener() {
    @Override
    public void onAnnotationTouch(Annotation annotation) {
      Log.i(TAG, "onMapAnnotationTouchListener: Annotation Touched: " + annotation.getId() +
              " | Latitude: " + annotation.getLocation().getLatitude() + " | Longitude: " +
              annotation.getLocation().getLongitude());
    }
  };

  private OnMapTouchListener onMapTouchListener = new OnMapTouchListener() {
    @Override
    public void onMapTouched(Location location) {
      // Log Map Touch
      Log.i(TAG, "onMapAnnotationTouchListener: Map Touched - Longitude: " +
              location.getLongitude() + " | Latitude: " + location.getLatitude() +
              " | Level: " + location.getFloorLevel());

      // Remove Existing Geofence Annotation
      for (Annotation annotation : mIndoorMap.getAnnotations(0)) {
        mIndoorMap.removeAnnotation(annotation);
      }

      // Get & Set Geofence Data
      mGeofenceData = GeofenceHelper.createCircularGeofence(
              location.getLatitude(),
              location.getLongitude(),
              Double.parseDouble(mDataBinding.regionSize.getText().toString())
      );

      // Create Annotations
      List<Annotation> vertexAnnotations = new ArrayList<>();
      for (VertexPoint vertexPoint : mGeofenceData.getVertexPoints()) {
        // Create Location from Vertex
        Location vertexLocation = new Location(vertexPoint.getLongitude(), vertexPoint.getLatitude(),
                location.getFloorLevel());
        // Create Annotation & Add to List
        vertexAnnotations.add(new Annotation(vertexLocation, mAnnotationBitmap, false,
                mAnnotationBitmapSize, String.valueOf(mGeofenceData.getCenterPoint().getLatitude())));
      }

      // Draw Vertex Annotations
      mIndoorMap.addAnnotations(vertexAnnotations);
    }
  };

  private void returnGeofenceObjectAndExit() {
    // Validate Geofence
    if (!validateGeofenceData()) {
      showDialog("Error!", "Please make sure you create a Geofence!", false);
      return;
    }

    // Return Data
    Intent geofenceData = new Intent();
    geofenceData.putExtra(GEOFENCE_DATA, mGeofenceData);
    setResult(RESULT_OK, geofenceData);
    finish();
  }

  private boolean validateGeofenceData() {
    if (mGeofenceData.getVertexPoints() == null) {
      return false;
    }

    if (mGeofenceData.getVertexPoints().size() != 360) {
      return false;
    }

    if (mGeofenceData.getCenterPoint() == null) {
      return false;
    }

    return true;
  }

  /**
   * Used to display map tracking toggle button on MapFragment
   *
   * @return Instance of OnMapStatusChangedListener
   */

  private OnMapStatusChangedListener onMapStatusChangedListener = new OnMapStatusChangedListener() {
    @Override
    public void onUserLocationStatusChanged(UserLocationStatus userLocationStatus) { }
    @Override
    public void onRouteDistanceChanged(float v) { }
    @Override
    public void onRouteErrorNoCrossFloor(int i) { }
    @Override
    public void onMultiPointDistancesChanged(List<java.util.Map<String, Object>> list) { }
  };

  /**
   * Callback method for receiving and handling Location data from VLC SDK
   */
  private IndoorPositioning.Listener indoorPositioningListener = new IndoorPositioning.Listener() {
    @Override
    public void didUpdateHeading(java.util.Map<String, Object> heading) {
      // Assign Variables from Response
      Float mHeadingDegrees = (Float) heading.get(IndoorPositioning.Listener.HEADING_DEGREES);
      Float mHeadingAccuracy = (Float) heading.get(IndoorPositioning.Listener.HEADING_ACCURACY);
      Float mHeadingArbitraryNorthDegrees = (Float) heading.get(
          IndoorPositioning.Listener.HEADING_ARBITRARY_NORTH_DEGREES);

      // Update User Heading
      if (mIndoorMap != null && !App.DEBUGGING) {
        mIndoorMap.setUserHeading(mHeadingDegrees, mHeadingAccuracy, mHeadingArbitraryNorthDegrees);
      }
    }

    @Override
    public void didUpdateLocation(java.util.Map<String, Object> location) {
      // Get Location Values
      Double mLatitude = (Double) location.get(IndoorPositioning.Listener.LOCATION_LATITUDE);
      Double mLongitude = (Double) location.get(IndoorPositioning.Listener.LOCATION_LONGITUDE);
      Double mAltitude = (Double) location.get(IndoorPositioning.Listener.LOCATION_ALTITUDE);

      // Update User Location
      if (mIndoorMap != null && !App.DEBUGGING) {
        mIndoorMap.setUserLocation(new Location(mLongitude, mLatitude), 0);
      }
    }

    @Override
    public void didFailWithError(Error error) {
      // Log Error
      Logger.e(TAG, "VLC Location Error: " + error.toString(),
          new Exception(error.toString()));

      // Check for Connection Failed due to WiFi
      if (error.equals(Error.CONNECTION_FAILED)) {
        handleConnectionFailedError();
        return;
      }

      // Show Error Dialog
      showDialog("Locationing Error!",
          "VLC Locationing encountered an error: \n\n" + error.toString(), false);
    }
  };

  /**
   * Helper methods for Notifying & Fixing WiFi issues when running VLC
   */
  private void handleConnectionFailedError() {
    // Check WiFi Enabled
    if (!wifiEnabled()) {
      // WiFi not enabled -> Show Dialog with option to enable & re-try
      AlertDialog.Builder locationErrorDialogBuilder = new AlertDialog.Builder(this)
          .setTitle("Locationing Error!")
          .setIcon(R.drawable.ic_error)
          .setMessage("VLC Connection Failed due to WiFi being disabled.")
          .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss())
          .setPositiveButton("ENABLE & RETRY", (dialogInterface, i) -> {
            enableWifi(new WifiEnabledCallback() {
              @Override
              public void onConnected() {
                // Restart Positioning
                restartPositioning();
                // Display Result (Success / Failure)
                if (mIndoorPositioning != null && mIndoorPositioning.isRunning()) {
                  // Display dialog
                  showDialog("Locationing Enabled!",
                      "You have connected to the Locationing Service!",
                      true);
                }
              }

              @Override
              public void onConnectionFailed() {
                // Show Error Dialog
                showDialog("Locationing Error!",
                    "VLC Connection failed as you are not connected to a " +
                        "valid network. Please manually connect to a valid " +
                        "Wifi network and try again.", false);
              }

              @Override
              public void onEnableFailed() {
                // Show Error Dialog
                showDialog("Locationing Error!",
                    "There was an error whilst attempting to enable " +
                        "WiFi, please manually enable WiFi and try again.",
                    false);
              }
            });

            // Dismiss this Dialog
            dialogInterface.dismiss();
          });

      // Show Dialog without showing navigation
      displayDialogImmersive(locationErrorDialogBuilder);
      return;
    }

    // Check WiFi Connected
    if (!wifiConnected()) {
      // Show Dialog
      showDialog("Locationing Error!",
          "VLC Connection failed as you are not connected to a valid network. " +
              "Please manually connect to a valid Wifi network and try again.",
          false);
      return;
    }

    // WiFi is valid, show generic connection failed
    showDialog("Locationing Error!",
        "VLC Locationing encountered an error: \n\n CONNECTION_FAILED",
        false);
  }

  private boolean wifiEnabled() {
    if (mWifiManager == null) {
      mWifiManager = (WifiManager) getApplicationContext().
          getSystemService(Context.WIFI_SERVICE);
    }
    return mWifiManager.isWifiEnabled();
  }

  private boolean wifiConnected() {
    // Init Connectivity Manager
    if (mConnectivityManager == null) {
      mConnectivityManager = (ConnectivityManager)
          getSystemService(CONNECTIVITY_SERVICE);
    }
    // Get Network Info
    NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
    // Return State of Network
    return networkInfo != null && networkInfo.isConnected();
  }

  private void enableWifi(WifiEnabledCallback callback) {
    // Build Progress Dialog View
    View wifiProgressDialog = getLayoutInflater().inflate(
        R.layout.dialog_layout_enable_wifi_progress, null);

    // Build Dialog
    AlertDialog.Builder wifiProgressDialogBuilder = new AlertDialog.Builder(this)
        .setTitle("WiFi Assistant")
        .setView(wifiProgressDialog)
        .setCancelable(false);

    // Create & Show Dialog
    AlertDialog wifiDialog = wifiProgressDialogBuilder.create();
    wifiDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    wifiDialog.show();
    wifiDialog.getWindow().getDecorView().setSystemUiVisibility(
        this.getWindow().getDecorView().getSystemUiVisibility());
    wifiDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

    // Get WifiManager
    if (mWifiManager == null) {
      mWifiManager = (WifiManager) getApplicationContext().
          getSystemService(Context.WIFI_SERVICE);
    }

    // Enabled Wifi
    mWifiManager.setWifiEnabled(true);

    // Reset Holders
    mNumOfConnectedChecks = 0;
    mNumOfEnabledChecks = 0;

    // Poll Wifi one a second for 10 seconds to check status
    mHandler.postDelayed(new Runnable(){
      public void run(){
        // Update Checks Variable
        mNumOfEnabledChecks++;

        // Check Wifi State
        if (mWifiManager.isWifiEnabled()) {
          // Wait a few seconds to connect after enablement
          mHandler.postDelayed(() -> {
            // Update Checks Variable
            mNumOfConnectedChecks++;

            // Check Connection
            if (wifiConnected()) {
              wifiDialog.dismiss();
              callback.onConnected();
              return;
            }

            // Retry
            // If less than 10 checks, check again
            if (mNumOfConnectedChecks <= mMaxNumOfChecks) {
              mHandler.postDelayed(this, mDelayBetweenChecks);
            } else {
              wifiDialog.dismiss();
              callback.onConnectionFailed();
            }

          }, mDelayBetweenChecks);
          return;
        }

        // If less than 10 checks, check again
        if (mNumOfEnabledChecks <= mMaxNumOfChecks) {
          mHandler.postDelayed(this, mDelayBetweenChecks);
        } else {
          wifiDialog.dismiss();
          callback.onEnableFailed();
        }
      }
    }, mDelayBetweenChecks);
  }

  private void restartPositioning() {
    // Stop Indoor Position & Unregister Listener
    if (mIndoorPositioning != null) {
      if (mIndoorPositioning.isRunning()) {
        mIndoorPositioning.stop();
      }
      mIndoorPositioning.unregister();
    }

    // Register Indoor Positioning Listener
    if (mIndoorPositioning != null) {
      mIndoorPositioning.register(indoorPositioningListener, mHandler);

      // Start Indoor Positioning
      mIndoorPositioning.start();
    }
  }

  private void showDialog(String title, String message, boolean success) {
    // Build Dialog
    AlertDialog.Builder genericDialogBuilder = new AlertDialog.Builder(CreateGeofenceActivity.this)
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
        CreateGeofenceActivity.this.getWindow().getDecorView().getSystemUiVisibility());
    genericDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
  }

  @Override
  protected String getInheritedTag() {
    return TAG;
  }
}
