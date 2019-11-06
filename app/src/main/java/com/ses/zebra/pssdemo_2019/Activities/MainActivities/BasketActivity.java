package com.ses.zebra.pssdemo_2019.Activities.MainActivities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ses.zebra.pssdemo_2019.Activities.AssistantActivities.VoiceAssistantActivityV2;
import com.ses.zebra.pssdemo_2019.Activities.AssistantActivities.WorkForceConnectActivities.ChatAssistantActivity;
import com.ses.zebra.pssdemo_2019.Activities.AssistantActivities.MessageAssistantActivity;
import com.ses.zebra.pssdemo_2019.Activities.AssistantActivities.VoiceAssistantActivity;
import com.ses.zebra.pssdemo_2019.Activities.BaseActivity;
import com.ses.zebra.pssdemo_2019.Activities.SubActivities.EndShopActivity;
import com.ses.zebra.pssdemo_2019.Activities.SubActivities.ProductActivity;
import com.ses.zebra.pssdemo_2019.Activities.SettingsActivities.SettingsActivity;
import com.ses.zebra.pssdemo_2019.Adapter.BasketListAdapter;
import com.ses.zebra.pssdemo_2019.App;
import com.ses.zebra.pssdemo_2019.Debugging.Logger;
import com.ses.zebra.pssdemo_2019.Interfaces.UpdateBasketCallback;
import com.ses.zebra.pssdemo_2019.POJOs.BasketItem;
import com.ses.zebra.pssdemo_2019.POJOs.StockItem;
import com.ses.zebra.pssdemo_2019.R;
import com.ses.zebra.pssdemo_2019.databinding.ActivityBasketBinding;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.ScannerException;

import java.util.ArrayList;
import java.util.List;

public class BasketActivity extends BaseActivity implements Scanner.DataListener {

    // Debugging
    private static final String TAG = "BasketActivity";

    // Constants
    private static final String FROM = "from";
    private static final String END_SHOP = "End Shop";
    private static final String BASKET_ITEM = "Basket-Item";
    private static final String START_SCAN = "Starting Scan";
    private static final String END_SHOP_UPPERCASE = "END SHOP";
    private static final String BASKET_ACTIVITY = "basket-activity";
    private static final LinearLayout.LayoutParams noMicBasketLayout = new LinearLayout.LayoutParams
            (LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.8f);

    // Variables
    private ActivityBasketBinding mDataBinding;

    public static List<BasketItem> mBasketList = new ArrayList<>();
    private static BasketActivity mBasketActivity;
    private static BasketListAdapter mBasketListAdapter;

    private static boolean mHandsFreeMode;
    private static Sensor mProximitySensor;
    private static SensorManager mSensorManager;

    @Override
    protected String getInheritedTag() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);

        // Init Basket Activity
        mBasketActivity = this;

        // Init DataBinding
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_basket);

        // Init Title
        mDataBinding.headerLayout.headerText.setText("Zebra Basket");
        mDataBinding.headerLayout.headerIcon.setImageResource(R.drawable.ic_basket);

        // Init Help Dialog
        mDataBinding.headerLayout.helpIcon.setOnClickListener(view -> displayHelpDialog());

        // Init Proximity Sensor
//        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//        mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
//        if (mProximitySensor == null) {
//            // Disable Hands-Free Mode
//            Logger.i(TAG, "Proximity Sensor not Available on this Device");
//            mDataBinding.handsFreeButton.setEnabled(false);
//        } else {
//            // Init Hands-Free Mode Listener
//            initHandsFreeListener();
//        }

        mDataBinding.headerLayout.helpIcon.setOnLongClickListener(view -> {
            Intent settingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(settingsActivity);
            return false;
        });

        // Init Navigation Listeners
        initBottomNavBar();

        // Init Basket RecyclerView & Adapter
        mBasketListAdapter = new BasketListAdapter(mBasketList, basketCallbackHandler());
        mDataBinding.basketListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDataBinding.basketListRecyclerView.setAdapter(mBasketListAdapter);

        // Update Basket Total
        updateBasketTotal();
    }

    private void displayHelpDialog() {
        // Get Custom Dialog Layout
        View customLayout = getLayoutInflater().inflate(R.layout.dialog_layout_get_help, null);

        // Get Layouts
        LinearLayout voiceAssistant = customLayout.findViewById(R.id.voiceAssistantContainer);
        LinearLayout messageAssistant = customLayout.findViewById(R.id.messageAssistantContainer);
        LinearLayout callAssistant = customLayout.findViewById(R.id.callAssistantContainer);

        // Enable / Disable features based on Preferences
        boolean hasMicrophone = getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
        boolean contextualVoiceEnabled = mSharedPreferences.getBoolean(PREF_ENABLE_CONTEXTUAL_VOICE, false);
        boolean mqttEnabled = mSharedPreferences.getBoolean(PREF_ENABLE_MQTT, false);
        boolean wfcEnabled = mSharedPreferences.getBoolean(PREF_ENABLE_WFC, false);

        // Voice Assistant Click Listener
        if (contextualVoiceEnabled && hasMicrophone) {
            voiceAssistant.setOnClickListener(view -> displayVoiceActivity());
        } else {
            // Disable Voice Assistant
            for (int i = 0; i < voiceAssistant.getChildCount(); i++) {
                View child = voiceAssistant.getChildAt(i);
                child.setEnabled(false);
            }

            // Set Toast Message
            voiceAssistant.setOnClickListener(view -> Toast.makeText(this,
                hasMicrophone ? "Voice Assistant disabled in settings"
                    : "You need a microphone to use voice assistant", Toast.LENGTH_LONG).show());
        }

        // MQTT Click Listener
        if (mqttEnabled) {
            messageAssistant.setOnClickListener(view -> displayMessageActivity());
        } else {
            // Disable MQTT
            for (int i = 0; i < messageAssistant.getChildCount(); i++) {
                View child = messageAssistant.getChildAt(i);
                child.setEnabled(false);
            }

            // Set Toast Message
            messageAssistant.setOnClickListener(view -> Toast.makeText(this,
                    "MQTT Disabled in Settings", Toast.LENGTH_LONG).show());
        }

        // WFC Click Listener
        if (wfcEnabled && hasMicrophone) {
            callAssistant.setOnClickListener(view -> displayChatActivity());
        } else {
            // Disable MQTT
            for (int i = 0; i < callAssistant.getChildCount(); i++) {
                View child = callAssistant.getChildAt(i);
                child.setEnabled(false);
            }

            // Set Toast Message
            callAssistant.setOnClickListener(view -> Toast.makeText(this,
                    "WFC disabled in settings", Toast.LENGTH_LONG).show());
        }

        // Build Dialog
        AlertDialog.Builder helpDialogBuilder = new AlertDialog.Builder(this)
                .setTitle("How can we assist?")
                .setView(customLayout)
                .setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());

        // Create & Show Dialog
        AlertDialog helpDialog = helpDialogBuilder.create();
        helpDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        helpDialog.show();
        helpDialog.getWindow().getDecorView().setSystemUiVisibility(
                this.getWindow().getDecorView().getSystemUiVisibility());
        helpDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    private void displayVoiceActivity() {
        Intent voiceActivity = new Intent(BasketActivity.this, VoiceAssistantActivityV2.class);
        voiceActivity.putExtra(FROM, BASKET_ACTIVITY);
        startActivity(voiceActivity);
    }

    private void displayMessageActivity() {
        Intent messageActivity = new Intent(BasketActivity.this, MessageAssistantActivity.class);
        messageActivity.putExtra(FROM, BASKET_ACTIVITY);
        startActivity(messageActivity);
    }

    private void displayChatActivity() {
        Intent chatActivity = new Intent(BasketActivity.this, ChatAssistantActivity.class);
        chatActivity.putExtra(FROM, BASKET_ACTIVITY);
        startActivity(chatActivity);
    }

    private UpdateBasketCallback basketCallbackHandler() {
        return new UpdateBasketCallback() {
            @Override
            public void minusQuantity(BasketItem basketItem) {
                List<BasketItem> itemsToRemove = new ArrayList<>();
                for (BasketItem existingBasketItem : mBasketList) {
                    if (existingBasketItem.equals(basketItem)) {
                        // If quantity = 1, remove item, else reduce Quantity
                        if (existingBasketItem.getQuantity() == 1) {
                            itemsToRemove.add(existingBasketItem);
                        } else {
                            existingBasketItem.setQuantity(existingBasketItem.getQuantity() - 1);
                        }
                    }
                }
                mBasketList.removeAll(itemsToRemove);
                updateBasketTotal();
                mBasketListAdapter.notifyDataSetChanged();
            }

            @Override
            public void addQuantity(BasketItem basketItem) {
                for (BasketItem existingBasketItem : mBasketList) {
                    if (existingBasketItem.equals(basketItem)) {
                        existingBasketItem.setQuantity(existingBasketItem.getQuantity() + 1);
                        updateBasketTotal();
                        mBasketListAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void viewProduct(BasketItem basketItem) {
                Intent viewProduct = new Intent(BasketActivity.this,
                                                ProductActivity.class);
                viewProduct.putExtra(FROM, BASKET_ACTIVITY);
                viewProduct.putExtra(BASKET_ITEM, basketItem);
                startActivity(viewProduct);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Enable Scanner
        mDataBinding.basketListRecyclerView.postDelayed(this::enableScanner, 100);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Disable Scanner
        disableScanner();
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

    @Override
    public void onData(ScanDataCollection scanDataCollection) {
        // Get Scanner Data as []
        ScanDataCollection.ScanData[] scannedData = scanDataCollection.getScanData().toArray(
                new ScanDataCollection.ScanData[scanDataCollection.getScanData().size()]);

        // Debugging
        for (ScanDataCollection.ScanData scanData : scannedData) {
            Log.i(TAG, "Label Type: " + scanData.getLabelType().name());
            Log.i(TAG, "Barcode: " + scanData.getData());
            Log.i(TAG, "Label Type: " + scanData.getLabelType().toString());
        }

        // Check if "End Shop" code was scanned
        for (ScanDataCollection.ScanData scanData : scannedData) {
            if (scanData.getData().equals(END_SHOP) || scanData.getData().equals(END_SHOP_UPPERCASE)) {
                if (BasketActivity.mBasketList != null &&BasketActivity. mBasketList.size() > 0) {
                    displayActivity(EndShopActivity.class);
                } else {
                    Toast.makeText(this, "Basket is empty, cannot end shop",
                            Toast.LENGTH_LONG).show();
                } return;
            }
        }

        // If not End shop -> Parse Barcode
        new getProductsFromBarcode().execute(scannedData);
    }

    public static class getProductsFromBarcode extends AsyncTask<ScanDataCollection.ScanData, Void, StockItem> {
        @Override
        protected StockItem doInBackground(ScanDataCollection.ScanData... scanDataArray) {
            for (ScanDataCollection.ScanData scanData : scanDataArray) {
                Logger.i(TAG, "Search for barcode: " + scanData.getData());
                if (App.mStockItems != null) {
                    for (StockItem stockItem : App.mStockItems) {
                        if (stockItem.getBarcode().equals(scanData.getData())) {
                            return stockItem;
                        }
                    }
                }
            } return null;
        }

        @Override
        protected void onPostExecute(StockItem stockItem) {
            // Handle product not found
            if (stockItem == null) {
                Log.e(TAG, "Product does not exist in Stock List");
                mBasketActivity.showScanDialog(false, 0.0);
                return;
            }

            // Show Dialog
            mBasketActivity.showScanDialog(true, stockItem.getDiscount());

            // Update Item if Already in Basket
            boolean itemInBasket = false;
            for (BasketItem basketItem : mBasketList) {
                if (basketItem.getBarcode().equals(stockItem.getBarcode())) {
                    // Update Holder
                    itemInBasket = true;
                    // Update Quantity
                    basketItem.setQuantity(basketItem.getQuantity() + 1);
                    // Update Discounts
                    basketItem.setPrice(stockItem.getDiscount() == 0
                            ? stockItem.getPrice()
                            : calculateDiscount(stockItem.getPrice(), stockItem.getDiscount(),
                                                basketItem.getQuantity()));
                    // Update Basket List
                    mBasketActivity.updateBasketTotal();
                    mBasketListAdapter.notifyDataSetChanged();
                    mBasketActivity.mDataBinding.basketListRecyclerView.scrollToPosition(
                            mBasketList.size() -1);
                }
            }

            // Create new Basket Item if not already in Basket
            if (!itemInBasket) {
                // Create new Basket Item
                BasketItem basketItem = new BasketItem();
                // Set Properties
                basketItem.setQuantity(1);
                basketItem.setSize(stockItem.getSize());
                basketItem.setBarcode(stockItem.getBarcode());
                basketItem.setDiscount(stockItem.getDiscount());
                basketItem.setDescription(stockItem.getDescription());
                basketItem.setPrice(stockItem.getDiscount() == 0
                        ? stockItem.getPrice()
                        : calculateDiscount(stockItem.getPrice(), stockItem.getDiscount(),
                        basketItem.getQuantity()));
                basketItem.setIngredients(stockItem.getIngredients());
                basketItem.setAllergenAdvice(stockItem.getAllergenAdvice());
                basketItem.setNutritionalInfoValues(stockItem.getNutritionalInfoValues());
                // Add To Basket
                mBasketList.add(basketItem);
                // Update Basket List
                mBasketActivity.updateBasketTotal();
                mBasketListAdapter.notifyDataSetChanged();
                mBasketActivity.mDataBinding.basketListRecyclerView.scrollToPosition(
                        mBasketList.size() -1);
            }
        }
    }

    private void showScanDialog(boolean itemFound, double discount) {
        // Set Text & Icon based on Discount
        if (itemFound) {
            // Init Inflater & Inflate fullscreen Frame Layout
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            FrameLayout successfulScanPopup =
                    (FrameLayout) inflater.inflate(R.layout.dialog_layout_item_found, null);
            // Get ImageView from Fullscreen Layout -> Change tick to discount as required
            ImageView discountImage = successfulScanPopup.findViewById(R.id.tick);
            if (discount == 241) {
                discountImage.setImageResource(R.drawable.twoforone);
            }else if (discount == -1.50){
                discountImage.setImageResource(R.drawable.onefiftyoff);
            }else if (discount == 0.10) {
                discountImage.setImageResource(R.drawable.tenpercentoff);
            }else if (discount == 0.15) {
                discountImage.setImageResource(R.drawable.fifteenpercentoff);
            }else if (discount == 0.2) {
                discountImage.setImageResource(R.drawable.twentypercentoff);
            }else if (discount == 0.3) {
                discountImage.setImageResource(R.drawable.thirtypercentoff);
            }else if ( discount == 0.5){
                discountImage.setImageResource(R.drawable.fiftypercentoff);
            }

            // Fullscreen Layout Params
            FrameLayout.LayoutParams fullscreenParams = new FrameLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            // Add View
            addContentView(successfulScanPopup, fullscreenParams);
            // Get RootView -> Remove after 1sec
            successfulScanPopup.postDelayed(() -> {
                ViewGroup rootView = findViewById(android.R.id.content);
                rootView.removeView(successfulScanPopup);
            }, 1000);
        } else {
            // Build Dialog View
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            View alertDialogLayout = getLayoutInflater().inflate(R.layout.dialog_layout_no_item_found, null);
            alertDialogBuilder.setView(alertDialogLayout);

            //
            ImageView alertDialogIcon = alertDialogLayout.findViewById(R.id.scanIcon);
            TextView alertDialogText = alertDialogLayout.findViewById(R.id.scanText);

            alertDialogIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_cross));
            alertDialogText.setText("Item Not Found!");

            // Do not allow Dismiss
            alertDialogBuilder.setCancelable(false);

            // Show Dialog without showing navigation
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            alertDialog.show();
            alertDialog.getWindow().getDecorView().setSystemUiVisibility(
                    this.getWindow().getDecorView().getSystemUiVisibility());
            alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

            // Dismiss Dialog after 2 seconds
            alertDialogLayout.postDelayed(alertDialog::dismiss, 2000);
        }
    }

    private void showEndShopDialog(Bitmap endShopQrCode) {
        // Build Dialog View
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        View alertDialogLayout = getLayoutInflater().inflate(R.layout.dialog_layout_end_shop,
                null);
        alertDialogBuilder.setView(alertDialogLayout);

        // Set Dismiss Listener
        alertDialogBuilder.setPositiveButton("Done", (dialog, which) -> dialog.dismiss());

        // Show QR Code
        ImageView alertDialogIcon = alertDialogLayout.findViewById(R.id.endShopQrCodeImage);
        alertDialogIcon.setImageBitmap(endShopQrCode);

        // Show Dialog without showing navigation
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        alertDialog.show();
        alertDialog.getWindow().getDecorView().setSystemUiVisibility(
                this.getWindow().getDecorView().getSystemUiVisibility());
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    /*
     * If the discount is < 0 then it's just that amount off the price
     * If the discount is 0 to 1 then its a percentage
     * if its 241... then its two for one
     * */
    private static double calculateDiscount(double stockPrice, double stockDiscount,
                                            int basketQuantity) {
        // Amount off price
        if (stockDiscount < 0) {
            return stockPrice + stockDiscount;
        }

        // Percentage off price
        if (0 < stockDiscount && stockDiscount < 1) {
            return (stockPrice - (stockPrice * stockDiscount));
        }

        // 2 for 1 Deal
        if (stockDiscount == 241) {
            return basketQuantity % 2 == 0 ? stockPrice / 2 : stockPrice + (stockPrice / 2);
        }

        // Unhandled cases (shouldn't be any)
        return stockPrice;
    }

    private void updateBasketTotal() {
        double basketTotal = 0.00;
        for (BasketItem basketItem : mBasketList) {
            basketTotal += (basketItem.getQuantity() * basketItem.getPrice());
        }
        TextView basketTotalTextView = findViewById(R.id.total);
        basketTotalTextView.setText(mSharedPreferences.getString(PREF_SELECT_CURRENCY, DEFAULT_CURRENCY)+ String.format("%.2f", basketTotal));
    }

    private void initBottomNavBar() {
        // Update Current "Tab" colour
        setCurrentTab();

        // Get VLC enabled from Pref
        boolean vlcEnabled = mSharedPreferences.getBoolean(PREF_ENABLE_VLC, false);

        // Init Listeners
        mDataBinding.bottomNavLayout.shoppingListLayout.setOnClickListener(view ->
                displayActivity(ShoppingListActivity.class));
        mDataBinding.bottomNavLayout.offersLayout.setOnClickListener(view ->
                displayActivity(OffersListActivity.class));
        // Only Allow navigation to VLC tab is Camera is found
        if (vlcEnabled) {
            mDataBinding.bottomNavLayout.vlcLayout.setOnClickListener(view -> {
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
                    // Allow Navigation
                    displayActivity(VlcLightingActivity.class);
                } else {
                    // Build Dialog
                    AlertDialog.Builder noCameraDialogBuilder = new AlertDialog.Builder(this)
                            .setTitle("No Front Camera Found")
                            .setMessage("VLC Locationing Capabilities require a front-facing camera")
                            .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());

                    // Show Dialog without showing navigation
                    AlertDialog noCameraDialog = noCameraDialogBuilder.create();
                    noCameraDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                    noCameraDialog.show();
                    noCameraDialog.getWindow().getDecorView().setSystemUiVisibility(
                            this.getWindow().getDecorView().getSystemUiVisibility());
                    noCameraDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                }
            });
        } else {
            // Disable VLC
            for (int i = 0; i < mDataBinding.bottomNavLayout.vlcLayout.getChildCount(); i++) {
                View child = mDataBinding.bottomNavLayout.vlcLayout.getChildAt(i);
                child.setEnabled(false);
            }

            // Set Toast Message
            mDataBinding.bottomNavLayout.vlcLayout.setOnClickListener(view -> Toast.makeText(this,
                    "VLC Disabled in Settings",
                    Toast.LENGTH_LONG).show());
        }

    }

    private void setCurrentTab() {
        // Update Drawable Colour
        for (Drawable drawable : mDataBinding.bottomNavLayout.basketText.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(
                        mDataBinding.bottomNavLayout.basketText.getContext(), R.color.white),
                        PorterDuff.Mode.SRC_IN));
            }
        }

        // Update Background Colour
        mDataBinding.bottomNavLayout.basketLayout.setBackgroundColor(
                getResources().getColor(R.color.zebraBlue));

        // Update Text Colour
        mDataBinding.bottomNavLayout.basketText.setTextColor(Color.WHITE);
    }

    private void startSoftScan() {
        if (App.mScanner != null) {
            try {
                // Cancel Pending Read
                if (App.mScanner.isReadPending()) {
                   App.mScanner.cancelRead();
                }
                 // Set Soft Trigger Type && Start Scan
                App.mScanner.triggerType = Scanner.TriggerType.SOFT_ONCE;
                App.mScanner.read();
            } catch (ScannerException e) {
                Log.e(TAG, "ScannerException: " + e.getMessage());
            }
        }
    }

    private void initNoMicUI() {
        // Alert User
        AlertDialog.Builder noMicDialogBuilder = new AlertDialog.Builder(this)
                .setTitle("No Microphone Found")
                .setMessage("Contextual Voice services require a camera")
                .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());

        // Show Dialog without showing navigation
        AlertDialog noMicDialog = noMicDialogBuilder.create();
        noMicDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        noMicDialog.show();
        noMicDialog.getWindow().getDecorView().setSystemUiVisibility(
                this.getWindow().getDecorView().getSystemUiVisibility());
        noMicDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

//        // Remove Contextual Voice
//        mDataBinding.contextualVoiceLayout.setVisibility(View.GONE);
//        mDataBinding.contextualVoiceDivider.setVisibility(View.GONE);

        // Alter Weightings
        mDataBinding.basketListRecyclerView.setLayoutParams(noMicBasketLayout);
    }
}
