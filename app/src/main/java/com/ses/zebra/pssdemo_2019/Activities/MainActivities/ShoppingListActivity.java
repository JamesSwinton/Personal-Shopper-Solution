package com.ses.zebra.pssdemo_2019.Activities.MainActivities;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.ses.zebra.pssdemo_2019.Activities.BaseActivity;
import com.ses.zebra.pssdemo_2019.Adapter.ShoppingListAdapter;
import com.ses.zebra.pssdemo_2019.R;
import com.ses.zebra.pssdemo_2019.databinding.ActivityShoppingListBinding;

public class ShoppingListActivity extends BaseActivity {

    // Debugging
    private static final String TAG = "ShoppingListActivity";

    // Constants

    // Variables
    private ShoppingListAdapter mShoppingListAdapter;
    private ActivityShoppingListBinding mDataBinding;

    @Override
    protected String getInheritedTag() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        // Init DataBinding
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_shopping_list);

        // Init Title
        mDataBinding.headerLayout.headerText.setText("Shopping List");
        mDataBinding.headerLayout.headerIcon.setImageResource(R.drawable.ic_shopping_list);

        // Init Navigation
        initBottomNavBar();

        // Init Shopping List Adapter
        mShoppingListAdapter = new ShoppingListAdapter(this);
        mDataBinding.shoppingListExpandableListView.setAdapter(mShoppingListAdapter);
    }

    private void initBottomNavBar() {
        // Update Current "Tab" colour
        setCurrentTab();

        // Get VLC enabled from Pref
        boolean vlcEnabled = mSharedPreferences.getBoolean(PREF_ENABLE_VLC, false);

        // Init Listeners
        mDataBinding.bottomNavLayout.basketLayout.setOnClickListener(view ->
                displayActivity(BasketActivity.class));
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
        for (Drawable drawable : mDataBinding.bottomNavLayout.shoppingListText.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(
                        mDataBinding.bottomNavLayout.shoppingListText.getContext(), R.color.white),
                        PorterDuff.Mode.SRC_IN));
            }
        }

        // Update Background Colour
        mDataBinding.bottomNavLayout.shoppingListLayout.setBackgroundColor(
                getResources().getColor(R.color.zebraBlue));

        // Update Text Colour
        mDataBinding.bottomNavLayout.shoppingListText.setTextColor(Color.WHITE);
    }
}
