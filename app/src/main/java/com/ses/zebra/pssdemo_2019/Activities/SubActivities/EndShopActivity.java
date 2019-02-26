package com.ses.zebra.pssdemo_2019.Activities.SubActivities;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.zxing.WriterException;
import com.ses.zebra.pssdemo_2019.Activities.BaseActivity;
import com.ses.zebra.pssdemo_2019.Activities.MainActivities.BasketActivity;
import com.ses.zebra.pssdemo_2019.Activities.MainActivities.SplashScreenActivity;
import com.ses.zebra.pssdemo_2019.App;
import com.ses.zebra.pssdemo_2019.Debugging.Logger;
import com.ses.zebra.pssdemo_2019.POJOs.BasketItem;
import com.ses.zebra.pssdemo_2019.R;
import com.ses.zebra.pssdemo_2019.databinding.ActivityEndShopBinding;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class EndShopActivity extends BaseActivity {

    // Debugging
    private static final String TAG = "EndShopActivity";

    // Variables
    private ActivityEndShopBinding mDataBinding;

    @Override
    protected String getInheritedTag() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_shop);

        // Init DataBinding
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_end_shop);

        // Load Display End Shop Barcode
        displayEndShopBarcode();

        // Init Exit Button
        mDataBinding.exitButton.setOnClickListener(view -> {
            // Display Splash Activity
            displayActivity(NavigationMenuActivity.class);
        });
    }

    private void displayEndShopBarcode() {
        // Log to File
        Logger.i(TAG, "Initialising End Shop");

        // Convert basket to String
        StringBuilder basket = new StringBuilder();
        for (BasketItem basketItem : BasketActivity.mBasketList) {
            basket.append(basketItem.getQuantity());
            basket.append(",");
            basket.append(basketItem.getBarcode());
            basket.append(";");
        } basket.deleteCharAt(basket.length() - 1);
        basket.append("|");

        // Generate QR Code
        try {
            // Init Encoder
            QRGEncoder qrCodeEncoder = new QRGEncoder(basket.toString(), null,
                    QRGContents.Type.TEXT, 250);
            // Getting QR-Code as Bitmap
            Bitmap qrCode = qrCodeEncoder.encodeAsBitmap();
            // Display QR Code
            mDataBinding.endShopBarcode.setImageBitmap(qrCode);
        } catch (WriterException e) {
            Logger.e(TAG, "Writer Exception: " + e.getMessage(), e);
        }
    }
}
