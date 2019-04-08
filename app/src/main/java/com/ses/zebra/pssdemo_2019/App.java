package com.ses.zebra.pssdemo_2019;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ses.zebra.pssdemo_2019.Debugging.Logger;
import com.ses.zebra.pssdemo_2019.POJOs.Config;
import com.ses.zebra.pssdemo_2019.POJOs.Geofencing.PopUpRegion;
import com.ses.zebra.pssdemo_2019.POJOs.Meta;
import com.ses.zebra.pssdemo_2019.POJOs.StockItem;
import com.ses.zebra.pssdemo_2019.POJOs.Sub.CalcNutrient;
import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.ProfileManager;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.Scanner.DataListener;
import com.symbol.emdk.barcode.Scanner.StatusListener;
import com.symbol.emdk.barcode.ScannerConfig;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.StatusData;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * Called at Application Start-Up.
 * All Initialisation is done here, including EMDKManager, Scanner etc...
 */

public class App extends Application implements EMDKListener, StatusListener, DataListener {

    // Debugging
    public static final boolean DEBUGGING = false;
    private static final String TAG = "Application Class";

    // Constants
    public static final String mStockImagesPath = Environment.getExternalStorageDirectory()
            + File.separator + "PSSDemo" + File.separator + "Stock" + File.separator + "images"
            + File.separator;

    // Variables
    public static EMDKManager mEmdkManager;
    public static ProfileManager mProfileManager;

    public static Meta mMeta;
    public static Context mContext;
    public static Scanner mScanner;
    public static PopUpRegion[] mPopUpRegions;
    public static StockItem[] mStockItems;
    public static String mDeviceSerialNumber;
    public static Config mConfig = new Config();

    public static Handler mUiThread;

    private boolean mIsScanning = false;
    private List<DataListener> mDataListeners;
    private static BarcodeManager mBarcodeManager;

    public static String[] mIngredientsList = {
            "Carbonated Water",
            "Citric Acid",
            "Erythritol",
            "Sodium Citrate",
            "Taurine",
            "Panax Ginseng Extract",
            "L-Carnitine",
            "L-Tartrate",
            "Sucralose",
            "Sobric Acid",
            "Benzoic Acid",
            "Salt",
            "Guarana Extract",
            "Glucuronolactone",
            "Caffeine",
            "Acesulfame K",
            "Aspartame",
            "Inositol",
            "Xanthan Gum",
            "Niacinamide",
            "Calcium Pantothenate",
            "Pyridoxine HCL",
            "Vitamin B12",
            "Natural & Artificial Flavors",
            "Colors",
            "Vegetable Oil (Corn, Canola, Sunflower and/or Soybean Oil)",
            "Sugar",
            "Monosodium Glutamine",
            "Fructose",
            "Sodium Diacetate",
            "Soy Sauce (Soybean, Wheat, Salt)",
            "Onion Powder",
            "Maltodextrin (Made from corn)",
            "Hydrolyzed Soy Protein",
            "Garlic Powder",
            "Torula Yeast",
            "Malic Acid",
            "Extract of Paprike",
            "Spices",
            "Caramel Color",
            "Disodium Inosinate",
            "Disodium Guanylate",
            "Dextrose",
            "Natural Flavor",
            "Corn Meal",
            "Vegetable Oil (Contains One or More of the Following: Corn, Cottonseed, Sunflower, or Canola Oil)",
            "Whey",
            "Cornstarch",
            "Corn Flour",
            "Calcium Carbonate",
            "Buttermilk",
            "Cheddar Cheese (Cultured Milk, Salt, Enzymes)",
            "Monosodium Glutamate (Flavor Enhancer)",
            "Artificial Color",
            "Calcium and Sodium Caseinates (Milk Derived)",
            "Butter Oil",
            "Yellow 6 Lake",
            "Lactic Acid",
            "Yellow 5 Lake",
            "Natural Flavors",
            "Invert Sugar",
            "Corn Syrup",
            "Modified Corn Starch",
            "White Mineral Oil",
            "Natural and artificial flavourings",
            "Red 40",
            "Carnauba Wax"
    };

    @Override
    public void onCreate() {
        super.onCreate();
        // Set-up Variables
        mDataListeners = new ArrayList<>();
        mUiThread = new Handler(getMainLooper());

        // Set Context
        mContext = getApplicationContext();

        // Set Serial Number
        mDeviceSerialNumber = Build.SERIAL;

        // Init TTS
        TTS.init(getApplicationContext());

        // Get EMDK Manager
        EMDKManager.getEMDKManager(this, this);

        // Init Logger -> Log Device Info
        Logger.logDeviceInfo();
    }

    /*
     * Called to notify the client when the EMDKManager object has been opened and its ready to use.
     */
    @Override
    public void onOpened(EMDKManager emdkManager) {
        // Log EMDK Open
        Logger.i(TAG, "EMDK: Open");

        // Init EMDK Manager
        mEmdkManager = emdkManager;

        // Init Barcode Manager
        Logger.i(TAG, "Init Barcode Manager");
        mBarcodeManager = (BarcodeManager)
                mEmdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);

        // Init Scanner
        try {
            initScanner();
        } catch (ScannerException e) {
            Logger.e(TAG, "ScannerException: " + e.getMessage(), e);
        }

//        // Init Profile Manager
//        mProfileManager = (ProfileManager) emdkManager.getInstance(EMDKManager.FEATURE_TYPE.PROFILE);
//
//        // Apply Profile
//        applyProfile(false);
    }

//    private void applyProfile(boolean proximityOn) {
//        // Get an instance of VersionManager
//        VersionManager versionManager = (VersionManager) mEmdkManager.getInstance(EMDKManager.FEATURE_TYPE.VERSION);
//        Log.i(TAG, "MX Version: " + versionManager.getVersion(VersionManager.VERSION_TYPE.MX));
//
//        // Apply Profile
//        String profileName = proximityOn ? PROFILE_PROXIMITY_ON : PROFILE_PROXIMITY_OFF;
//        EMDKResults applyProfileResults = mProfileManager.processProfile(profileName,
//                ProfileManager.PROFILE_FLAG.SET, (String[]) null);
//
//        // Check the return status of processProfile
//        if (applyProfileResults.statusCode == EMDKResults.STATUS_CODE.CHECK_XML) {
//            Log.i(TAG, "Applying '" + profileName + "' was successful");
//        } else {
//            Log.i(TAG, "Applying '" + profileName + "' failed");
//        }
//
//    }

    void initScanner() throws ScannerException {
        Logger.i(TAG, "Init Scanner");
        // Init Scanner
        mScanner = mBarcodeManager.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT);
        // Set Scanner Listeners
        mScanner.addDataListener(this);
        mScanner.addStatusListener(this);
        // Enable Scanner if needed
        if (mIsScanning) {
            enableScanner(null);
        }
    }

    public void enableScanner(DataListener dataListener) throws ScannerException {
        Logger.i(TAG, "Enable Scanner");

        // Add DataListener to List if Exists
        if (dataListener != null && !mDataListeners.contains(dataListener)) {
            mDataListeners.add(dataListener);
        }

        // Enable Scanner
        mScanner.enable();
        mIsScanning = true;

        // Build & Set Scanner Meta (Can only be done after Scanner is Enabled)
        ScannerConfig config = mScanner.getConfig();
        config.readerParams.readerSpecific.imagerSpecific.pickList = ScannerConfig.PickList.ENABLED;
        config.readerParams.readerSpecific.imagerSpecific.digimarcDecoding = true;
        config.scanParams.decodeAudioFeedbackUri = "system/media/audio/notifications/decode-short.wav";
        config.scanParams.decodeHapticFeedback = true;
        config.decoderParams.code128.enabled = true;
        config.decoderParams.code39.enabled = true;
        config.decoderParams.upca.enabled = true;
        mScanner.setConfig(config);
    }

    public void disableScanner(DataListener dataListener) throws ScannerException {
        Logger.i(TAG, "Disable Scanner");

        // Remove DataListener from List if Exists
        if (mDataListeners.contains(dataListener)) {
            mDataListeners.remove(dataListener);
            mScanner.removeDataListener(dataListener);
        }

        // Disable Scanner
        mScanner.disable();
        mIsScanning = false;
    }

    /*
     * Notifies user upon a abrupt closing of EMDKManager.
     */
    @Override
    public void onClosed() {
        // Log EMDK Closed
        Logger.i(TAG, "EMDK: Closed");

        // Release EMDK Manager
        if (mEmdkManager != null) {
            mEmdkManager.release();
            mEmdkManager = null;
        }
    }

    /*
     * This is the callback method upon data availability.
     */
    @Override
    public void onData(ScanDataCollection scanDataCollection) {
        mUiThread.post(() -> {
            // Handle Data
            for (DataListener dataListener : mDataListeners) {
                dataListener.onData(scanDataCollection);
            }

            // Restart Scanner
            if (mScanner != null) {
                try {
                    if (!mScanner.isReadPending()) mScanner.read();
                } catch (ScannerException e) {
                    Logger.e(TAG, "ScannerException: " + e.getMessage(), e);
                }
            }
        });
    }

    /*
     * This is the callback method upon scan status event occurs.
     */
    @Override
    public void onStatus(StatusData statusData) {
        switch (statusData.getState()) {
            case IDLE:
                try {
                    try { Thread.sleep(100); }
                    catch (InterruptedException e) { e.printStackTrace(); }
                    mScanner.read();
                } catch (ScannerException e) {
                    Logger.e(TAG, "ScannerException: " + e.getMessage(), e);
                }
                break;
            case WAITING:
                Logger.i(TAG, "Scanner waiting...");
                break;
            case SCANNING:
                Logger.i(TAG, "Scanner scanning...");
                break;
            case DISABLED:
                Logger.i(TAG, "Scanner Disabled...");
                break;
            case ERROR:
                Logger.i(TAG, "Scanner Error!");
                break;
            default:
                break;
        }
    }
}
