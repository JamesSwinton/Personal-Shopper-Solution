package com.ses.zebra.pssdemo_2019;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
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
    public static DEVICE_TYPE mDeviceType;

    public static Handler mUiThread;

    private boolean mIsScanning = false;
    private List<DataListener> mDataListeners;
    private static BarcodeManager mBarcodeManager;

    public enum DEVICE_TYPE { PS20, MC18 }

    @Override
    public void onCreate() {
        super.onCreate();
        // Set-up Variables
        mDataListeners = new ArrayList<>();
        mUiThread = new Handler(getMainLooper());

        // Set Context
        mContext = getApplicationContext();

        // Init TTS
        TTS.init(getApplicationContext());

        // Get EMDK Manager
        EMDKManager.getEMDKManager(this, this);

        // Init Logger -> Log Device Info
        Logger.logDeviceInfo();

        // Init MC18 Vs. PS20
        mDeviceType = getDeviceType();
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
    }

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
        config.scanParams.decodeAudioFeedbackUri = "system/media/audio/notifications/decode-short.wav";
        config.scanParams.decodeHapticFeedback = true;
        config.decoderParams.code128.enabled = true;
        config.decoderParams.code39.enabled = true;
        config.decoderParams.upca.enabled = true;
        if (App.mDeviceType == DEVICE_TYPE.PS20) {
            config.readerParams.readerSpecific.imagerSpecific.digimarcDecoding = true;
        }

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

    private void startScannerRead() {
        try {
            try { Thread.sleep(100); }
            catch (InterruptedException e) { e.printStackTrace(); }
            mScanner.read();
        } catch (ScannerException e) {
            Logger.e(TAG, "ScannerException: " + e.getMessage(), e);
        }
    }

    private DEVICE_TYPE getDeviceType() {
        // Get Camera & Microphone
        boolean hasMicrophone = getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
        boolean hasCamera = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);

        // Return Device Type
        return hasCamera && hasMicrophone ? DEVICE_TYPE.PS20 : DEVICE_TYPE.MC18;
    }

}
