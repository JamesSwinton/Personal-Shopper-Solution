package com.ses.zebra.pssdemo_2019.Activities.MainActivities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.ses.zebra.pssdemo_2019.Activities.BaseActivity;
import com.ses.zebra.pssdemo_2019.Activities.SubActivities.ProductActivity;
import com.ses.zebra.pssdemo_2019.Adapter.OffersListAdapter;
import com.ses.zebra.pssdemo_2019.Interfaces.ViewOfferProductCallback;
import com.ses.zebra.pssdemo_2019.R;
import com.ses.zebra.pssdemo_2019.databinding.ActivityOffersListBinding;

public class OffersListActivity extends BaseActivity {

    // Debugging
    private static final String TAG = "OffersListActivity";

    // Constants
    private static final String FROM = "from";
    private static final String OFFER_ACTIVITY = "offer-activity";
    private static final String OFFER_ITEM = "Offer-Item";

    // Variables
    private ActivityOffersListBinding mDataBinding;
    private OffersListAdapter mOffersListAdapter;

    @Override
    protected String getInheritedTag() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers_list);

        // Init DataBinding
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_offers_list);

        // Init Title
        mDataBinding.headerLayout.headerText.setText("Offers List");
        mDataBinding.headerLayout.headerIcon.setImageResource(R.drawable.ic_offers);

        // Init Navigation
        initBottomNavBar();

        // Init Shopping List Adapter
        mOffersListAdapter = new OffersListAdapter(this, callbackHandler());
        mDataBinding.offersListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDataBinding.offersListRecyclerView.setAdapter(mOffersListAdapter);
    }

    private void initBottomNavBar() {
        // Update Current "Tab" colour
        setCurrentTab();

        // Get VLC enabled from Pref
        boolean vlcEnabled = mSharedPreferences.getBoolean(PREF_ENABLE_VLC, false);

        // Init Listeners
        mDataBinding.bottomNavLayout.basketLayout.setOnClickListener(view ->
                displayActivity(BasketActivity.class));
        mDataBinding.bottomNavLayout.shoppingListLayout.setOnClickListener(view ->
                displayActivity(ShoppingListActivity.class));
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
        for (Drawable drawable : mDataBinding.bottomNavLayout.offersText.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(
                        mDataBinding.bottomNavLayout.offersLayout.getContext(), R.color.white),
                        PorterDuff.Mode.SRC_IN));
            }
        }

        // Update Background Colour
        mDataBinding.bottomNavLayout.offersLayout.setBackgroundColor(
                getResources().getColor(R.color.zebraBlue));

        // Update Text Colour
        mDataBinding.bottomNavLayout.offersText.setTextColor(Color.WHITE);
    }

    private ViewOfferProductCallback callbackHandler() {
        return offer -> {
            Intent viewProduct = new Intent(OffersListActivity.this,
                    ProductActivity.class);
            viewProduct.putExtra(FROM, OFFER_ACTIVITY);
            viewProduct.putExtra(OFFER_ITEM, offer);
            startActivity(viewProduct);
        };
    }
}
