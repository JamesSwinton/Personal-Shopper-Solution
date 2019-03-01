package com.ses.zebra.pssdemo_2019.Activities.MainActivities;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.util.SizeF;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.philips.indoormaps.logic.MapFragment;
import com.philips.indoormaps.map.Annotation;
import com.philips.indoormaps.map.Location;
import com.philips.indoormaps.map.Map;
import com.philips.indoormaps.map.OnAnnotationTouchListener;
import com.philips.indoormaps.map.OnMapReadyCallback;
import com.philips.indoormaps.map.OnMapStatusChangedListener;
import com.philips.indoormaps.map.OnMapTouchListener;

import com.philips.indoormaps.map.PolyRegion;
import com.philips.indoormaps.map.Region;
import com.philips.indoormaps.map.RegionMonitor;

import com.philips.indoormaps.map.UserLocationStatus;
import com.philips.indoorpositioning.library.IndoorPositioning;
import com.ses.zebra.pssdemo_2019.Activities.BaseActivity;
import com.ses.zebra.pssdemo_2019.App;
import com.ses.zebra.pssdemo_2019.Debugging.Logger;
import com.ses.zebra.pssdemo_2019.Fragments.NoMapFragment;
import com.ses.zebra.pssdemo_2019.Interfaces.WifiEnabledCallback;
import com.ses.zebra.pssdemo_2019.R;
import com.ses.zebra.pssdemo_2019.databinding.ActivityVlcLightingBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VlcLightingActivity extends BaseActivity {

    // Debugging
    private static final String TAG = "VlcLightingActivity";

    // Constants
    private static final String MAP = "map.bin";
    private static final Handler mHandler = new Handler();
    private static final String MAP_FILE_PATH = Environment.getExternalStorageDirectory()
            + File.separator + "PSSDemo" + File.separator + "Stock" + File.separator + MAP;

    // Variables
    private ActivityVlcLightingBinding mDataBinding;
    private IndoorPositioning mIndoorPositioning;
    private IndoorPositioning.Listener.ExpectedAccuracyLevel mExpectedAccuracyLevel;

    public Float mHorizontalAccuracy, mAltitudeAccuracy,  mHeadingAccuracy;
    public Float mHeadingDegrees, mHeadingArbitraryNorthDegrees;
    public double mLatitude, mLongitude, mAltitude;

    private static Map mIndoorMap;
    private static Bitmap regionBitmap;
    private static Bitmap annotationBitmap;

    private static final int mDelayBetweenChecks = 100;
    private static final int mMaxNumOfChecks = 100;
    private static int mNumOfConnectedChecks = 0;
    private static int mNumOfEnabledChecks = 0;
    private static WifiManager mWifiManager;
    private static ConnectivityManager mConnectivityManager;

    // Regions
    private MapFragment mapFragment;
    private static AlertDialog discountDialog;
    private static RegionMonitor mRegionMonitor;
    private static final List<Location> regionAnnotations = new ArrayList<>();

    private static final int TOP_LEFT_REGION = 0;
    private static final Location topLeftPolyRegionCenterPoint = new Location(App.mConfig.getTopLeftPolyRegionCenterPointLng(), App.mConfig.getTopLeftPolyRegionCenterPointLat(), 0);
    private static final Location[] topLeftPolyRegion = {
            new Location(App.mConfig.getTopLeftPolyRegionLng0(), App.mConfig.getTopLeftPolyRegionLat0(), 0),
            new Location(App.mConfig.getTopLeftPolyRegionLng1(), App.mConfig.getTopLeftPolyRegionLat1(), 0),
            new Location(App.mConfig.getTopLeftPolyRegionLng2(), App.mConfig.getTopLeftPolyRegionLat2(), 0),
            new Location(App.mConfig.getTopLeftPolyRegionLng3(), App.mConfig.getTopLeftPolyRegionLat3(), 0)
    };

    private static final int MIDDLE_LEFT_REGION = 1;
    private static final Location middleLeftPolyRegionCenterPoint = new Location(App.mConfig.getMiddleLeftPolyRegionCenterPointLng(), App.mConfig.getMiddleLeftPolyRegionCenterPointLat(), 0);
    private static final Location[] middleLeftPolyRegion = {
            new Location(App.mConfig.getMiddleLeftPolyRegionLng0(), App.mConfig.getMiddleLeftPolyRegionLat0(), 0),
            new Location(App.mConfig.getMiddleLeftPolyRegionLng1(), App.mConfig.getMiddleLeftPolyRegionLat1(), 0),
            new Location(App.mConfig.getMiddleLeftPolyRegionLng2(), App.mConfig.getMiddleLeftPolyRegionLat2(), 0),
            new Location(App.mConfig.getMiddleLeftPolyRegionLng3(), App.mConfig.getMiddleLeftPolyRegionLat3(), 0)
    };

    private static final int TOP_RIGHT_REGION = 2;
    private static final Location topRightPolyRegionCenterPoint = new Location(App.mConfig.getTopRightPolyRegionCenterPointLng(), App.mConfig.getTopRightPolyRegionCenterPointLat(), 0);
    private static final Location[] topRightPolyRegion = {
            new Location(App.mConfig.getTopRightPolyRegionLng0(), App.mConfig.getTopRightPolyRegionLat0(), 0),
            new Location(App.mConfig.getTopRightPolyRegionLng1(), App.mConfig.getTopRightPolyRegionLat1(), 0),
            new Location(App.mConfig.getTopRightPolyRegionLng2(), App.mConfig.getTopRightPolyRegionLat2(), 0),
            new Location(App.mConfig.getTopRightPolyRegionLng3(), App.mConfig.getTopRightPolyRegionLat3(), 0)
    };

    private static final int MIDDLE_RIGHT_REGION = 3;
    private static final Location middleRightPolyRegionCenterPoint = new Location(App.mConfig.getMiddleRightPolyRegionCenterPointLng(), App.mConfig.getMiddleRightPolyRegionCenterPointLat(), 0);
    private static final Location[] middleRightPolyRegion = {
            new Location(App.mConfig.getMiddleRightPolyRegionLng0(), App.mConfig.getMiddleRightPolyRegionLat0(), 0),
            new Location(App.mConfig.getMiddleRightPolyRegionLng1(), App.mConfig.getMiddleRightPolyRegionLat1(), 0),
            new Location(App.mConfig.getMiddleRightPolyRegionLng2(), App.mConfig.getMiddleRightPolyRegionLat2(), 0),
            new Location(App.mConfig.getMiddleRightPolyRegionLng3(), App.mConfig.getMiddleRightPolyRegionLat3(), 0)
    };

    private static final int BOTTOM_REGION = 4;
    private static final Location bottomPolyRegionCenterPoint = new Location(App.mConfig.getBottomPolyRegionCenterPointLng(), App.mConfig.getBottomPolyRegionCenterPointLat(), 0);
    private static final Location[] bottomPolyRegion = {
            new Location(App.mConfig.getBottomPolyRegionLng0(), App.mConfig.getBottomPolyRegionLat0(), 0),
            new Location(App.mConfig.getBottomPolyRegionLng1(), App.mConfig.getBottomPolyRegionLat1(), 0),
            new Location(App.mConfig.getBottomPolyRegionLng2(), App.mConfig.getBottomPolyRegionLat2(), 0),
            new Location(App.mConfig.getBottomPolyRegionLng3(), App.mConfig.getBottomPolyRegionLat3(), 0)
    };

    private static int locationFound;

    @Override
    protected String getInheritedTag() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vlc_lighting);

        // Debugging
        locationFound = 0;

        // Init DataBinding && Buttons && Variables
        mapFragment = new MapFragment();
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_vlc_lighting);
        regionBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.region);
        annotationBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_annotation);

        // Init Title && Nav Bar
        initBottomNavBar();
        mDataBinding.headerLayout.headerText.setText("Zebra Locationing");
        mDataBinding.headerLayout.headerIcon.setImageResource(R.drawable.ic_vlc_location);

        // Init Indoor Positioning
        String configString = getConfig();
        Logger.i(TAG, "Init VLC with config string: " + configString);
        mIndoorPositioning = new IndoorPositioning(getApplicationContext());
        mIndoorPositioning.setConfiguration(getConfig());
        mIndoorPositioning.setHeadingOrientation(IndoorPositioning.IndoorPositioningHeadingOrientation.PORTRAIT);
        mIndoorPositioning.setMode(IndoorPositioning.IndoorPositioningMode.DEFAULT);

        // Load Map
        loadMap();
    }

    private String getConfig() {
        try {
            String configString = mSharedPreferences.getString(PREF_VLC_CONFIG_STRING,
                getString(R.string.app_configuration_eu));
            Base64.decode(configString, Base64.DEFAULT);
            return configString;
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage(), e);
            return getString(R.string.app_configuration_eu);
        }
    }

    private void initDiscountRegions() {
        // Create RegionMonitor to hold all regions && region listener
        mRegionMonitor = new RegionMonitor();
        mRegionMonitor.setTriggerTime(0.5f);
        mRegionMonitor.setRegionMonitorListener(regionMonitorListener());

        // Add all Center Points to Array
        regionAnnotations.add(bottomPolyRegionCenterPoint);
        regionAnnotations.add(topLeftPolyRegionCenterPoint);
        regionAnnotations.add(topRightPolyRegionCenterPoint);
        regionAnnotations.add(middleLeftPolyRegionCenterPoint);
        regionAnnotations.add(middleRightPolyRegionCenterPoint);

        // Add all Polygons to Array
        List<Location[]> locationRegions = new ArrayList<>();
        locationRegions.add(topLeftPolyRegion);
        locationRegions.add(middleLeftPolyRegion);
        locationRegions.add(topRightPolyRegion);
        locationRegions.add(middleRightPolyRegion);
        locationRegions.add(bottomPolyRegion);

        // Add regions to RegionMonitor
        mRegionMonitor.addRegion(new PolyRegion(topLeftPolyRegion, 0, TOP_LEFT_REGION));
        mRegionMonitor.addRegion(new PolyRegion(middleLeftPolyRegion, 0, MIDDLE_LEFT_REGION));
        mRegionMonitor.addRegion(new PolyRegion(topRightPolyRegion, 0, TOP_RIGHT_REGION));
        mRegionMonitor.addRegion(new PolyRegion(middleRightPolyRegion, 0, MIDDLE_RIGHT_REGION));
        mRegionMonitor.addRegion(new PolyRegion(bottomPolyRegion, 0, BOTTOM_REGION));

        // Draw Region over CenterPoint
        for (Location location : regionAnnotations) {
            mIndoorMap.addAnnotation(new Annotation(location, regionBitmap, false,
                    new SizeF(1.0f, 1.0f), "Test"));
        }

        // Draw points at each Polygon point
        for (Location[] locations : locationRegions) {
            for (Location location : locations) {
                mIndoorMap.addAnnotation(new Annotation(location, annotationBitmap, false,
                        new SizeF(0.1f, 0.1f), "Test"));
            }
        }
    }

    private RegionMonitor.RegionMonitorListener regionMonitorListener() {
        return new RegionMonitor.RegionMonitorListener() {
            @Override
            public void onEnterRegion(Region region) {
                Logger.i(TAG, "Entered Region: " + region.getId());
                showDiscountByRegion(region);
            }

            @Override
            public void onLeaveRegion(Region region) {
                Logger.i(TAG, "Exited Region: " + region.getId());
                if (discountDialog != null && discountDialog.isShowing()) {
                    discountDialog.dismiss();
                }
            }
        };
    }

    private void showDiscountByRegion(Region region) {
        // Build View
        View discountDialogView = getLayoutInflater().inflate(R.layout.dialog_layout_region_discount, null);
        ImageView productImage = discountDialogView.findViewById(R.id.productImage);
        TextView discountText = discountDialogView.findViewById(R.id.discountText);

        // Customise View
        switch (region.getId()) {
            case TOP_LEFT_REGION:
                productImage.setImageDrawable(getDrawable(R.drawable.discount_image_tositos));
                discountText.setText("50% off!");
                break;
            case TOP_RIGHT_REGION:
                productImage.setImageDrawable(getDrawable(R.drawable.discount_item_boogiedown));
                discountText.setText("50% off!");
                break;
            case MIDDLE_LEFT_REGION:
                productImage.setImageDrawable(getDrawable(R.drawable.discount_item_barqs));
                discountText.setText("15% off!");
                break;
            case MIDDLE_RIGHT_REGION:
                productImage.setImageDrawable(getDrawable(R.drawable.discount_item_redbull));
                discountText.setText("20% off!");
                break;
            case BOTTOM_REGION:
                productImage.setImageDrawable(getDrawable(R.drawable.discount_item_coconut));
                discountText.setText("30% off!");
                break;
        }

        // Build Dialog
        AlertDialog.Builder helpDialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Discount Found!")
                .setView(discountDialogView)
                .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());

        // Create & Show Dialog
        discountDialog = helpDialogBuilder.create();
        discountDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        discountDialog.show();
        discountDialog.getWindow().getDecorView().setSystemUiVisibility(
                this.getWindow().getDecorView().getSystemUiVisibility());
        discountDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
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

    /*
     Map Methods
     */
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
            Toast.makeText(VlcLightingActivity.this, "Could not load Map File",
                    Toast.LENGTH_LONG).show();
            // Replace fragment with Error Fragment
            getSupportFragmentManager()
                    .beginTransaction().replace(R.id.map_container, NoMapFragment.newInstance())
                    .commit();
            return;
        }

        // Load Map
        mapFragment.loadMap(mapFilePath, onMapReadyCallback());

        // Show Map Fragment
        getSupportFragmentManager()
                .beginTransaction().replace(R.id.map_container, mapFragment)
                .commit();
    }

    private OnMapReadyCallback onMapReadyCallback() {
        return map -> {
            Logger.i(TAG, "Map Ready");
            mIndoorMap = map;
            mIndoorMap.setOnMapTouchListener(onMapTouchListener());
            mIndoorMap.setOnMapStatusChangedListener(onMapStatusChangedListener());
            mIndoorMap.setOnAnnotationTouchListener(onMapAnnotationTouchListener());
            mIndoorMap.setStyle("{\"userLocationColor\":\"#007CB0\", \"routeLineColor\":\"#007CB0\", " +
                    "\"floorSelectionColor\":\"#007CB0\"}");

            // Init Discount Regions
            initDiscountRegions();
        };
    }

    private OnAnnotationTouchListener onMapAnnotationTouchListener() {
        return annotation -> {
            // Debugging
            if (App.DEBUGGING) {
                Location location = annotation.getLocation();
                mIndoorMap.setUserLocation(location, 0);
                mRegionMonitor.setUserLocation(location.getLongitude(), location.getLatitude(), location.getFloorLevel(), 0);
            }
        };
    }

    private OnMapTouchListener onMapTouchListener() {
        return location -> {
            // Log Map Touch
            Log.i(TAG, "Map Touch Detected - Longitude: " + location.getLongitude()
                    + " | Latitude: " + location.getLatitude()
                    + " | Level: " + location.getFloorLevel());

            // Debugging
            if (App.DEBUGGING) {
                mIndoorMap.setUserLocation(location, 0);
                mRegionMonitor.setUserLocation(location.getLongitude(), location.getLatitude(), location.getFloorLevel(), 0);
            }
        };
    }

    private OnMapStatusChangedListener onMapStatusChangedListener() {
        return new OnMapStatusChangedListener() {
            @Override
            public void onUserLocationStatusChanged(UserLocationStatus userLocationStatus) { }
            @Override
            public void onRouteDistanceChanged(float v) { }
            @Override
            public void onRouteErrorNoCrossFloor(int i) { }
            @Override
            public void onMultiPointDistancesChanged(List<java.util.Map<String, Object>> list) { }
        };
    }

    /*
     Locationing Methods
     */

    private IndoorPositioning.Listener indoorPositioningListener = new IndoorPositioning.Listener() {
        @Override
        public void didUpdateHeading(java.util.Map<String, Object> heading) {
            // Assign Variables from Response
            mHeadingDegrees = (Float) heading.get(IndoorPositioning.Listener.HEADING_DEGREES);
            mHeadingAccuracy = (Float) heading.get(IndoorPositioning.Listener.HEADING_ACCURACY);
            mHeadingArbitraryNorthDegrees = (Float) heading.get(
                    IndoorPositioning.Listener.HEADING_ARBITRARY_NORTH_DEGREES);

            // Update Heading Value
            mDataBinding.heading.setText("Heading: \n" + String.format("%.1f", mHeadingDegrees));

            // Update User Heading
            if (mIndoorMap != null && !App.DEBUGGING) {
                mIndoorMap.setUserHeading(mHeadingDegrees, mHeadingAccuracy, mHeadingArbitraryNorthDegrees);
            }
        }

        @Override
        public void didUpdateLocation(java.util.Map<String, Object> location) {
            // Get Location Values
            mLatitude = (Double) location.get(IndoorPositioning.Listener.LOCATION_LATITUDE);
            mLongitude = (Double) location.get(IndoorPositioning.Listener.LOCATION_LONGITUDE);
            mAltitude = (Double) location.get(IndoorPositioning.Listener.LOCATION_ALTITUDE);

            // Get Accuracy Values
            mHorizontalAccuracy = (Float) location.get(
                    IndoorPositioning.Listener.LOCATION_HORIZONTAL_ACCURACY);
            mAltitudeAccuracy = (Float) location.get(
                    IndoorPositioning.Listener.LOCATION_VERTICAL_ACCURACY);
            mExpectedAccuracyLevel = ExpectedAccuracyLevel.fromInteger((Integer)
                    location.get(IndoorPositioning.Listener.LOCATION_EXPECTED_ACCURACY_LEVEL));

            // Set Values to UI
            mDataBinding.latitude.setText("Latitude: \n" + String.valueOf(mLatitude));
            mDataBinding.longitude.setText("Longitude: \n" + String.valueOf(mLongitude));

            if (locationFound < 50) {
                Logger.i(TAG, "Location Update - Longitude: " + mLongitude
                        + " | Latitude: " + mLatitude
                        + " | Accuracy: " + mHorizontalAccuracy);

                locationFound++;
            }

            // Update User Location
            if (mIndoorMap != null && !App.DEBUGGING) {
                mIndoorMap.setUserLocation(new Location(mLongitude, mLatitude), mHorizontalAccuracy);
                mRegionMonitor.setUserLocation(mLongitude, mLatitude, 0, mHorizontalAccuracy);
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
                    "VLC Locationing encountered an error: \n\n" + error.toString(),
                    false);
        }
    };

    private void handleConnectionFailedError() {
        // Check WiFi Enabled
        if (!wifiEnabled()) {
            // WiFi not enabled -> Show Dialog with option to enable & re-try
            AlertDialog.Builder locationErrorDialogBuilder = new AlertDialog.Builder(VlcLightingActivity.this)
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
        AlertDialog.Builder genericDialogBuilder = new AlertDialog.Builder(VlcLightingActivity.this)
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
                VlcLightingActivity.this.getWindow().getDecorView().getSystemUiVisibility());
        genericDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    /*
     Bottom navigation utility methods
     */

    private void initBottomNavBar() {
        // Update Current "Tab" colour
        setCurrentTab();

        // Init Listeners
        mDataBinding.bottomNavLayout.basketLayout.setOnClickListener(view ->
                displayActivity(BasketActivity.class));
        mDataBinding.bottomNavLayout.shoppingListLayout.setOnClickListener(view ->
                displayActivity(ShoppingListActivity.class));
        mDataBinding.bottomNavLayout.offersLayout.setOnClickListener(view ->
                displayActivity(OffersListActivity.class));
    }

    private void setCurrentTab() {
        // Update Drawable Colour
        for (Drawable drawable : mDataBinding.bottomNavLayout.vlcText.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(
                        mDataBinding.bottomNavLayout.vlcText.getContext(), R.color.white),
                        PorterDuff.Mode.SRC_IN));
            }
        }

        // Update Background Colour
        mDataBinding.bottomNavLayout.vlcLayout.setBackgroundColor(
                getResources().getColor(R.color.zebraBlue));

        // Update Text Colour
        mDataBinding.bottomNavLayout.vlcText.setTextColor(Color.WHITE);
    }

}
