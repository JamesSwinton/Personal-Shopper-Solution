package com.ses.zebra.pssdemo_2019.Activities.MainActivities;

import android.Manifest;
import android.Manifest.permission;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.ses.zebra.pssdemo_2019.Activities.BaseActivity;
import com.ses.zebra.pssdemo_2019.Activities.SubActivities.NavigationMenuActivity;
import com.ses.zebra.pssdemo_2019.App;
import com.ses.zebra.pssdemo_2019.Debugging.Logger;
import com.ses.zebra.pssdemo_2019.EMDK.PSS;
import com.ses.zebra.pssdemo_2019.MQTT;
import com.ses.zebra.pssdemo_2019.POJOs.Config;
import com.ses.zebra.pssdemo_2019.POJOs.Geofencing.PopUpRegion;
import com.ses.zebra.pssdemo_2019.POJOs.Meta;
import com.ses.zebra.pssdemo_2019.POJOs.StockItem;
import com.ses.zebra.pssdemo_2019.R;
import com.ses.zebra.pssdemo_2019.databinding.ActivitySplashScreenBinding;
import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.personalshopper.CradleConfig;
import com.symbol.emdk.personalshopper.CradleException;
import com.symbol.emdk.personalshopper.PersonalShopper;

import org.ini4j.Wini;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashScreenActivity extends BaseActivity {

    // Debugging
    private static final String TAG = "SplashScreenActivity";

    // Constants
    private static final int ALL_PERMISSIONS = 1;
    private static final String mStockImagesPath = Environment.getExternalStorageDirectory()
        + File.separator + "PSSDemo" + File.separator + "Stock" + File.separator + "images"
        + File.separator;
    private static final String mConfigFilePath = Environment.getExternalStorageDirectory()
        + File.separator + "PSSDemo" + File.separator + "Stock" + File.separator + "config.ini";
    private static final String mMetaFilePath = Environment.getExternalStorageDirectory()
        + File.separator + "PSSDemo" + File.separator + "Stock" + File.separator + "meta.json";
    private static final String mStockFilePath = Environment.getExternalStorageDirectory()
        + File.separator + "PSSDemo" + File.separator + "Stock" + File.separator + "stock.json";
    private static final String mGeofenceFilePath = Environment.getExternalStorageDirectory()
        + File.separator + "PSSDemo" + File.separator + "Stock" + File.separator + "geofence.json";
    private static final String SETTINGS = "Settings";

    // Variables
    private Gson mGson;
    private ActivitySplashScreenBinding mDataBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Init Gson
        if (mGson == null) {
            mGson = new Gson();
        }

        // Clear Basket
        BasketActivity.mBasketList = new ArrayList<>();

        // Init DataBinding
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash_screen);

        // Display Version Number
        mDataBinding.version.setText(getAppVersion());

        // Check Runtime Permissions
        if (checkAndRequestPermissions()) {
            // Permissions were all accepted
            Logger.i(TAG, "Permissions Enabled - Starting Imports");

            // Set Serial (Needs Permission)
            if (Build.VERSION.SDK_INT >= VERSION_CODES.P) {
                App.mDeviceSerialNumber = Build.getSerial();
            } else {
                App.mDeviceSerialNumber = Build.SERIAL;
            }

            // Begin importing Meta & Stock JSON
            if (importDataFiles()) {
                // Init MQTT
                MQTT.init();

                // Debugging (Skip Splash Screen)
                if (App.DEBUGGING) {
                    checkDataFilesAndShowNavigationMenu();
                }

            } else {
                Log.e(TAG, "Importing Error. Cannot initialise MQTT");
                Toast.makeText(this, "Configuration files missing", Toast.LENGTH_LONG).show();
            }
        }

        // Setup Splash Screen
        initSplashScreen();

    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister PowerReceiver
        unregisterReceiver(powerReceiver);
    }

    @Override
    protected String getInheritedTag() {
        return TAG;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register PowerReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(powerReceiver, filter);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void initSplashScreen() {
        Log.i(TAG, "Loading Splash Screen Animation");
        Glide.with(this).load(R.raw.splash_animation_lesly)
                .into(mDataBinding.splashScreenLogo);

        // Set Listeners
        mDataBinding.splashScreenLayout.setOnClickListener(this::unlockPss);
        mDataBinding.splashScreenLayout.setOnLongClickListener(this::closeSplashScreen);
    }

    /*
     * Method Declared in XML
     * Listens to any screen-taps whilst docked
     * Clicking screen creates new PSS object & unlocks PSS from cradle
     */
    public void unlockPss(View view) {
        // Log
        Logger.i(TAG, "Screen Tapped within Cradle");
        Logger.i(TAG, "Creating PSS Instance & Unlocking Device from Cradle");

        // Create PSS -> Unlock from cradle
        new PSS();
    }

    public boolean closeSplashScreen(View view) {
        checkDataFilesAndShowNavigationMenu();
        return true;
    }

    private String getAppVersion() {
        String version = "---";
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(TAG, "PackageManagerException: " + e.getMessage(), e);
        } return version;
    }

    private boolean importDataFiles() {
        // Holder Variable
        boolean dataImported = true;

        // Import Pictures
        try {
            importStockImages();
        } catch (IOException e) {
            // Log to File & LogCat
            Logger.e(TAG, "Stock Image Import Error: " + e.getMessage(), e);
            dataImported = false;
        }

        // Start Imports
        try {
            importAndParseMetaFile();
        } catch(IOException e) {
            // Log to File & LogCat
            Logger.e(TAG, "Meta File Import Error: " + e.getMessage(), e);
            dataImported = false;
        }

        // Start Imports
        try {
            importAndParseStockFile();
        } catch(IOException e) {
            // Log to File & LogCat
            Logger.e(TAG, "Stock File Import Error: " + e.getMessage(), e);
            dataImported = false;
        }

        // Start Imports
        try {
            importGeofenceFile();
        } catch(IOException e) {
            // Log to File & LogCat
            Logger.e(TAG, "Geofence File Import Error: " + e.getMessage(), e);
            dataImported = false;
        }

        // Start Imports
        try {
            importConfigFile();
        } catch(IOException e) {
            // Log to File & LogCat
            Logger.e(TAG, "Config File Import Error: " + e.getMessage(), e);
            dataImported = false;
        }

        // Return Success / Failure
        return dataImported;
    }

    private void importStockImages() throws IOException {
        Logger.i(TAG, "Importing Stock Images...");

        // Create images folder in SD-Card if not already exist
        File imageFileDirectory = new File(mStockImagesPath);
        if (!imageFileDirectory.exists()) {
            Logger.i(TAG, "Creating images Directory...");
            boolean directoryCreated = imageFileDirectory.mkdirs();
            Logger.i(TAG, "Images directory created: " + (directoryCreated));
        }
    }

    private void importAndParseMetaFile() throws IOException {
        // Log to File && Logcat
        Logger.i(TAG, "Importing Meta File...");

        // Read metaFile from SDCard -> If not found copy from Assets Folder
        File metaFile = new File(mMetaFilePath);
        if (!metaFile.exists()) {
            metaFile = createFileFromAsset(getAssets().open("meta.json"), mMetaFilePath);
        }

        // Check if file exists or throw exception
        if (metaFile == null || !metaFile.exists()) {
            throw new FileNotFoundException("Could not find Meta File: " + mMetaFilePath);
        }

        // Import JSON from Meta File
        InputStream inputStream = new FileInputStream(metaFile);
        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);
        inputStream.close();

        // Log to File && Logcat
        Logger.i(TAG, "Importing Completed Successfully");
        Logger.i(TAG, "Parsing Meta File...");

        // Parse ConfigJSON to Meta POJO & Store in App class
        App.mMeta = mGson.fromJson(new String(buffer, "UTF-8"), Meta.class);

        // Log to File & LogCat
        Logger.i(TAG, "Parse Completed Successfully");
    }

    private void importAndParseStockFile() throws IOException {
        // Log to File && Logcat
        Logger.i(TAG, "Importing Stock File...");

        // Read stockFile from SDCard -> If not found copy from Assets Folder
        File stockFile = new File(mStockFilePath);
        if (!stockFile.exists()) {
            stockFile = createFileFromAsset(getAssets().open("stock.json"), mStockFilePath);
        }

        // Check if file exists or throw exception
        if (stockFile == null || !stockFile.exists()) {
            throw new FileNotFoundException("Could not find Stock File: " + mStockFilePath);
        }

        // Open & Read Json File
        InputStream inputStream = new FileInputStream(stockFile);
        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);
        inputStream.close();

        // Log to File && Logcat
        Logger.i(TAG, "Importing Completed Successfully");
        Logger.i(TAG, "Parsing Stock File...");

        // Convert Byte[] to JSONObject
        App.mStockItems = mGson.fromJson(new String(buffer, "UTF-8"), StockItem[].class);

        // Log to File & LogCat
        Logger.i(TAG, "Parse Completed Successfully");
    }

    private void importGeofenceFile() throws IOException {
        Logger.i(TAG, "Importing Geofence File...");

        // Read stockFile from SDCard -> If not found copy from Assets Folder
        File geofenceFile = new File(mGeofenceFilePath);
        if (!geofenceFile.exists()) {
            geofenceFile = createFileFromAsset(getAssets().open("geofence.json"), mGeofenceFilePath);
        }

        // Check if file exists or throw exception
        if (geofenceFile == null || !geofenceFile.exists()) {
            throw new FileNotFoundException("Could not find Geofence File: " + mGeofenceFilePath);
        }

        // Open & Read Json File
        InputStream inputStream = new FileInputStream(geofenceFile);
        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);
        inputStream.close();

        // Log to File && Logcat
        Logger.i(TAG, "Importing Completed Successfully");
        Logger.i(TAG, "Parsing Geofence File...");

        // Convert Byte[] to JSONObject
        App.mPopUpRegions = mGson.fromJson(new String(buffer, "UTF-8"), PopUpRegion[].class);

        // Log to File & LogCat
        Logger.i(TAG, "Parse Completed Successfully");
    }

    private void importConfigFile() throws IOException {
        // Log to File && Logcat
        Logger.i(TAG, "Importing Config File...");

        // Create new Config Object in App Class
        App.mConfig = new Config();

        // Read config file from SDCard -> If not found copy from Assets Folder
        File configFile = new File(mConfigFilePath);
        if (!configFile.exists()) {
            configFile = createFileFromAsset(getAssets().open("config.ini"), mConfigFilePath);
        }

        // Check if file exists or throw exception
        if (configFile == null || !configFile.exists()) {
            throw new FileNotFoundException("Could not find Config File: " + mConfigFilePath);
        }

        // Create Wini Object from ConfigFilePath
        Wini configIni = new Wini(configFile);

        // VLC Regions
        App.mConfig.setTopLeftPolyRegionCenterPointLat(Double.parseDouble(configIni.fetch(SETTINGS, "topLeftPolyRegionCenterPointLat")));
        App.mConfig.setTopLeftPolyRegionCenterPointLng(Double.parseDouble(configIni.fetch(SETTINGS, "topLeftPolyRegionCenterPointLng")));
        App.mConfig.setTopLeftPolyRegionLat0(Double.parseDouble(configIni.fetch(SETTINGS, "topLeftPolyRegionLat0")));
        App.mConfig.setTopLeftPolyRegionLat1(Double.parseDouble(configIni.fetch(SETTINGS, "topLeftPolyRegionLat1")));
        App.mConfig.setTopLeftPolyRegionLat2(Double.parseDouble(configIni.fetch(SETTINGS, "topLeftPolyRegionLat2")));
        App.mConfig.setTopLeftPolyRegionLat3(Double.parseDouble(configIni.fetch(SETTINGS, "topLeftPolyRegionLat3")));
        App.mConfig.setTopLeftPolyRegionLng0(Double.parseDouble(configIni.fetch(SETTINGS, "topLeftPolyRegionLng0")));
        App.mConfig.setTopLeftPolyRegionLng1(Double.parseDouble(configIni.fetch(SETTINGS, "topLeftPolyRegionLng1")));
        App.mConfig.setTopLeftPolyRegionLng2(Double.parseDouble(configIni.fetch(SETTINGS, "topLeftPolyRegionLng2")));
        App.mConfig.setTopLeftPolyRegionLng3(Double.parseDouble(configIni.fetch(SETTINGS, "topLeftPolyRegionLng3")));

        App.mConfig.setMiddleLeftPolyRegionCenterPointLat(Double.parseDouble(configIni.fetch(SETTINGS, "middleLeftPolyRegionCenterPointLat")));
        App.mConfig.setMiddleLeftPolyRegionCenterPointLng(Double.parseDouble(configIni.fetch(SETTINGS, "middleLeftPolyRegionCenterPointLng")));
        App.mConfig.setMiddleLeftPolyRegionLat0(Double.parseDouble(configIni.fetch(SETTINGS, "middleLeftPolyRegionLat0")));
        App.mConfig.setMiddleLeftPolyRegionLat1(Double.parseDouble(configIni.fetch(SETTINGS, "middleLeftPolyRegionLat1")));
        App.mConfig.setMiddleLeftPolyRegionLat2(Double.parseDouble(configIni.fetch(SETTINGS, "middleLeftPolyRegionLat2")));
        App.mConfig.setMiddleLeftPolyRegionLat3(Double.parseDouble(configIni.fetch(SETTINGS, "middleLeftPolyRegionLat3")));
        App.mConfig.setMiddleLeftPolyRegionLng0(Double.parseDouble(configIni.fetch(SETTINGS, "middleLeftPolyRegionLng0")));
        App.mConfig.setMiddleLeftPolyRegionLng1(Double.parseDouble(configIni.fetch(SETTINGS, "middleLeftPolyRegionLng1")));
        App.mConfig.setMiddleLeftPolyRegionLng2(Double.parseDouble(configIni.fetch(SETTINGS, "middleLeftPolyRegionLng2")));
        App.mConfig.setMiddleLeftPolyRegionLng3(Double.parseDouble(configIni.fetch(SETTINGS, "middleLeftPolyRegionLng3")));

        App.mConfig.setTopRightPolyRegionCenterPointLat(Double.parseDouble(configIni.fetch(SETTINGS, "topRightPolyRegionCenterPointLat")));
        App.mConfig.setTopRightPolyRegionCenterPointLng(Double.parseDouble(configIni.fetch(SETTINGS, "topRightPolyRegionCenterPointLng")));
        App.mConfig.setTopRightPolyRegionLat0(Double.parseDouble(configIni.fetch(SETTINGS, "topRightPolyRegionLat0")));
        App.mConfig.setTopRightPolyRegionLat1(Double.parseDouble(configIni.fetch(SETTINGS, "topRightPolyRegionLat1")));
        App.mConfig.setTopRightPolyRegionLat2(Double.parseDouble(configIni.fetch(SETTINGS, "topRightPolyRegionLat2")));
        App.mConfig.setTopRightPolyRegionLat3(Double.parseDouble(configIni.fetch(SETTINGS, "topRightPolyRegionLat3")));
        App.mConfig.setTopRightPolyRegionLng0(Double.parseDouble(configIni.fetch(SETTINGS, "topRightPolyRegionLng0")));
        App.mConfig.setTopRightPolyRegionLng1(Double.parseDouble(configIni.fetch(SETTINGS, "topRightPolyRegionLng1")));
        App.mConfig.setTopRightPolyRegionLng2(Double.parseDouble(configIni.fetch(SETTINGS, "topRightPolyRegionLng2")));
        App.mConfig.setTopRightPolyRegionLng3(Double.parseDouble(configIni.fetch(SETTINGS, "topRightPolyRegionLng3")));

        App.mConfig.setMiddleRightPolyRegionCenterPointLat(Double.parseDouble(configIni.fetch(SETTINGS, "middleRightPolyRegionCenterPointLat")));
        App.mConfig.setMiddleRightPolyRegionCenterPointLng(Double.parseDouble(configIni.fetch(SETTINGS, "middleRightPolyRegionCenterPointLng")));
        App.mConfig.setMiddleRightPolyRegionLat0(Double.parseDouble(configIni.fetch(SETTINGS, "middleRightPolyRegionLat0")));
        App.mConfig.setMiddleRightPolyRegionLat1(Double.parseDouble(configIni.fetch(SETTINGS, "middleRightPolyRegionLat1")));
        App.mConfig.setMiddleRightPolyRegionLat2(Double.parseDouble(configIni.fetch(SETTINGS, "middleRightPolyRegionLat2")));
        App.mConfig.setMiddleRightPolyRegionLat3(Double.parseDouble(configIni.fetch(SETTINGS, "middleRightPolyRegionLat3")));
        App.mConfig.setMiddleRightPolyRegionLng0(Double.parseDouble(configIni.fetch(SETTINGS, "middleRightPolyRegionLng0")));
        App.mConfig.setMiddleRightPolyRegionLng1(Double.parseDouble(configIni.fetch(SETTINGS, "middleRightPolyRegionLng1")));
        App.mConfig.setMiddleRightPolyRegionLng2(Double.parseDouble(configIni.fetch(SETTINGS, "middleRightPolyRegionLng2")));
        App.mConfig.setMiddleRightPolyRegionLng3(Double.parseDouble(configIni.fetch(SETTINGS, "middleRightPolyRegionLng3")));

        App.mConfig.setBottomPolyRegionCenterPointLat(Double.parseDouble(configIni.fetch(SETTINGS, "bottomPolyRegionCenterPointLat")));
        App.mConfig.setBottomPolyRegionCenterPointLng(Double.parseDouble(configIni.fetch(SETTINGS, "bottomPolyRegionCenterPointLng")));
        App.mConfig.setBottomPolyRegionLat0(Double.parseDouble(configIni.fetch(SETTINGS, "bottomPolyRegionLat0")));
        App.mConfig.setBottomPolyRegionLat1(Double.parseDouble(configIni.fetch(SETTINGS, "bottomPolyRegionLat1")));
        App.mConfig.setBottomPolyRegionLat2(Double.parseDouble(configIni.fetch(SETTINGS, "bottomPolyRegionLat2")));
        App.mConfig.setBottomPolyRegionLat3(Double.parseDouble(configIni.fetch(SETTINGS, "bottomPolyRegionLat3")));
        App.mConfig.setBottomPolyRegionLng0(Double.parseDouble(configIni.fetch(SETTINGS, "bottomPolyRegionLng0")));
        App.mConfig.setBottomPolyRegionLng1(Double.parseDouble(configIni.fetch(SETTINGS, "bottomPolyRegionLng1")));
        App.mConfig.setBottomPolyRegionLng2(Double.parseDouble(configIni.fetch(SETTINGS, "bottomPolyRegionLng2")));
        App.mConfig.setBottomPolyRegionLng3(Double.parseDouble(configIni.fetch(SETTINGS, "bottomPolyRegionLng3")));

        // Log to File && Logcat
        Logger.i(TAG, "Config file imported successfully");
    }

    private File createFileFromAsset(InputStream assetInputStream, String filePath) {
        // Log File Creation
        Logger.i(TAG, "Writing asset to file: " + filePath);

        try {
            // Create Asset Output Stream && File Output Path
            File assetFile = new File(filePath);
            OutputStream outputStream = new FileOutputStream(assetFile);

            // Copy Asset to File Path
            int length;
            byte buffer[] = new byte[1024];
            while((length = assetInputStream.read(buffer)) > 0) {
                outputStream.write(buffer,0,length);
            }

            // Close Streams
            outputStream.close();
            assetInputStream.close();

            // Log success -> Return file
            Logger.i(TAG, "Successfully created file: " + filePath);
            return assetFile;
        } catch (IOException e) {
            Logger.e(TAG, e.getMessage(), e);
            return null;
        }
    }

    /*
     * Listen for Device being removed from Charging Cradle
     * If basket is empty: show SplashScreen Activity
     * Else: Show "EndShop" Activity
     */
    private BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // If Not Plugged into cradle -> Show Navigation Activity
            if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
                Logger.i(TAG, "Device Disconnected From Power");
                runOnUiThread(() -> checkDataFilesAndShowNavigationMenu());
            }

//            if (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) != BatteryManager.BATTERY_PLUGGED_AC) {
//                runOnUiThread(() -> checkDataFilesAndShowNavigationMenu());
//            }
        }
    };

    private void checkDataFilesAndShowNavigationMenu() {
        // Log to File & Logcat
        Logger.i(TAG, "PSS Removed -> Checking Data File Exist && Imported");
        // Check Data was Imported
//        if (App.mMeta == null || App.mConfig == null || App.mStockItems == null) {
//            new AlertDialog.Builder(this)
//                    .setTitle("Missing Configuration Files!")
//                    .setMessage("One or more files are missing from 'sdcard/PSSDemo/Stock/' \n\n"
//                            + "Please ensure the following files exist in the above directory: \n\n"
//                            + "meta.json \n"
//                            + "stock.json \n"
//                            + "config.ini")
//                    .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
//                    .show();
//            return;
//        }

        // Log to File & Logcat
        Logger.i(TAG, "Data Files Imported Successfully -> Starting Navigation Activity");

        // Start Navigation Menu Activity -> End SplashScreen Activity
        Intent navigationMenuActivity = new Intent(this, NavigationMenuActivity.class);
        startActivity(navigationMenuActivity);
        finish();
    }

    /*
     * Check Permissions at Runtime
     */
    private  boolean checkAndRequestPermissions() {
        // Define Permissions
        int cameraPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        int microphonePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        int locationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int readStoragePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeStoragePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPhoneStatePermission = ContextCompat.checkSelfPermission(this,
            permission.READ_PHONE_STATE);

        // Add non-granted permissions to Array for Requesting
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (microphonePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (readStoragePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (writeStoragePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (readPhoneStatePermission != PackageManager.PERMISSION_GRANTED
            && Build.VERSION.SDK_INT >= VERSION_CODES.P) {
            listPermissionsNeeded.add(permission.READ_PHONE_STATE);
        }

        // Request permissions, if required
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    ALL_PERMISSIONS);
            return false;
        } return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();

                // Initialize the map with both permissions
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);

                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                        // Permissions were all accepted -> Begin importing Meta & Stock JSON
                        importDataFiles();
                    } else {
                        Log.d(TAG, "1 Or More Permissions Not Granted");
                        // Show dialog to re-request permissions
                        showDialogOK((dialog, which) -> {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    checkAndRequestPermissions();
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    Log.i(TAG, "Permissions Not Granted - Go to settings to enable");
                                    Log.i(TAG, "Permissions Not Granted - Application Exiting");
                                    finish();
                                    break;
                            }
                        });
                    }
                }
            }
        }
    }

    private void showDialogOK(DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage("All Permissions are required to run this app")
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

}
