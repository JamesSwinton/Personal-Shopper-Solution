package com.ses.zebra.pssdemo_2019.Activities.SubActivities;

import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.ses.zebra.pssdemo_2019.Activities.BaseActivity;
import com.ses.zebra.pssdemo_2019.Activities.MainActivities.BasketActivity;
import com.ses.zebra.pssdemo_2019.Activities.MainActivities.OffersListActivity;
import com.ses.zebra.pssdemo_2019.Activities.MainActivities.ShoppingListActivity;
import com.ses.zebra.pssdemo_2019.Activities.MainActivities.VlcLightingActivity;
import com.ses.zebra.pssdemo_2019.R;
import com.ses.zebra.pssdemo_2019.databinding.ActivityNavigationMenuBinding;

import java.util.ArrayList;

public class NavigationMenuActivity extends BaseActivity {

    // Debugging
    private static final String TAG = "NavigationMenuActivity";

    // Constants

    // Variables
    ActivityNavigationMenuBinding mDataBinding;

    @Override
    protected String getInheritedTag() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_menu);

        // Init DataBinding && Toolbar
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_navigation_menu);

        // Clear Basket
        BasketActivity.mBasketList = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get VLC enabled from Pref
        boolean vlcEnabled = mSharedPreferences.getBoolean(PREF_ENABLE_VLC, false);

        // Set Navigation Click Listeners
        mDataBinding.basketCard.setOnClickListener(view -> displayActivity(BasketActivity.class));
        mDataBinding.listCard.setOnClickListener(view -> displayActivity(ShoppingListActivity.class));
        mDataBinding.offersCard.setOnClickListener(view -> displayActivity(OffersListActivity.class));
        // Only Allow navigation to VLC tab is Camera is found
        if (vlcEnabled) {
            mDataBinding.vlcCard.setOnClickListener(view -> {
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
            for (int i = 0; i < mDataBinding.vlcCardLayout.getChildCount(); i++) {
                View child = mDataBinding.vlcCardLayout.getChildAt(i);
                child.setEnabled(false);
            }

            // Set Toast Message
            mDataBinding.vlcCard.setOnClickListener(view -> Toast.makeText(this,
                    "VLC Disabled in Settings",
                    Toast.LENGTH_LONG).show());
        }
    }
}
