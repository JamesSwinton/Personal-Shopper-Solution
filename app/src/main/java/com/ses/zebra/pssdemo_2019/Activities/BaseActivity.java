package com.ses.zebra.pssdemo_2019.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.ses.zebra.pssdemo_2019.Activities.MainActivities.SplashScreenActivity;
import com.ses.zebra.pssdemo_2019.Debugging.Logger;

public abstract class BaseActivity extends AppCompatActivity {

    // Debugging
    private static final String BASE_TAG = "BaseActivity";
    private final String TAG = BASE_TAG + " - " + getInheritedTag();

    // Constants
    final protected Handler mHandler = new Handler(); // Used for UI operations within Service Callbacks
    public static final String PREF_SELECT_CURRENCY = "currency";
    public static final String DEFAULT_CURRENCY = "£";
    public static final String PREF_EDIT_STOCK_LIST = "edit_stock_list";
    public static final String PREF_EDIT_OFFER_LIST = "edit_offer_list";
    public static final String PREF_EDIT_SHOPPING_LIST = "edit_shopping_list";
    public static final String PREF_ENABLE_MQTT = "mqtt_enabled";
    public static final String PREF_CUSTOM_MQTT_SERVER = "mqtt_use_custom_broker";
    public static final String PREF_MQTT_BROKER = "mqtt_broker";
    public static final String PREF_MQTT_USER = "mqtt_broker_user";
    public static final String PREF_MQTT_PASS = "mqtt_broker_pass";
    public static final String PREF_CURRENT_TOPIC = "mqtt_topic";
    public static final String PREF_TOPIC_IDENTIFIER = "mqtt_topic_identifier";
    public static final String PREF_SUB_TOPIC_IDENTIFIER = "mqtt_sub_topic_identifier";
    public static final String PREF_ENABLE_WFC = "wfc_enabled";
    public static final String PREF_WFC_ACTIVATION_CODE = "wfc_provisioning_code";
    public static final String PREF_ENABLE_VLC = "vlc_enabled";
    public static final String PREF_VLC_CONFIG_STRING = "vlc_config_string";
    public static final String PREF_VLC_SELECT_MAP = "vlc_map";
    public static final String PREF_ENABLE_CONTEXTUAL_VOICE = "contextual_voice_enabled";
    public static final String PREF_AI_CONFIG_STRING = "contextual_voice_config_string";

    // Variables
    public static SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init shared Pref
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Setup UI
        initImmersiveUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register BatteryReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(powerReceiver, filter);

        // init UI Settings
        initImmersiveUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(powerReceiver);
        initImmersiveUI();
    }

    @Override
    public void finish() {
        Log.i(TAG, "finish()");
        super.finish();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    protected abstract String getInheritedTag();

    /**
     * Broadcast Receivers
     * powerReceiver  -> Listens for device entering / exiting cradle by monitoring Charge
     *                     & Plug status
     */
    private BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
                Logger.i(TAG, "Device Connected to Power");
                runOnUiThread(() -> displayActivity(SplashScreenActivity.class));
            }


//            if (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) == BatteryManager.BATTERY_PLUGGED_AC) {
//                runOnUiThread(() -> displayActivity(SplashScreenActivity.class));
//            }
        }
    };

    /**
     * Inherited Utility Methods
     */

    public void displayActivity(Class<?> Activity) {
        Intent i = new Intent(this, Activity);
        startActivity(i);
        finish();
        overridePendingTransition(0, 0);
    }

    /**
     * UI Initialisation Methods
     * Called in onCreate, onResume & onPause
     * Removes all external UI components
     */

    private void initImmersiveUI() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        View decorView = setSystemUiVisibilityMode();
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
            setSystemUiVisibilityMode(); // Needed to avoid exiting immersive_sticky when keyboard is displayed
        });
    }

    private View setSystemUiVisibilityMode() {
        View decorView = getWindow().getDecorView();
        int options = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(options);
        return decorView;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            initImmersiveUI();
        }
    }
}
