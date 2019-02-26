package com.ses.zebra.pssdemo_2019.Activities.AssistantActivities.WorkForceConnectActivities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.ses.zebra.pssdemo_2019.Activities.MainActivities.BasketActivity;
import com.ses.zebra.pssdemo_2019.Activities.MainActivities.OffersListActivity;
import com.ses.zebra.pssdemo_2019.Activities.MainActivities.ShoppingListActivity;
import com.ses.zebra.pssdemo_2019.Activities.MainActivities.VlcLightingActivity;
import com.ses.zebra.pssdemo_2019.App;
import com.ses.zebra.pssdemo_2019.R;
import com.ses.zebra.pssdemo_2019.Utilities.EnumToText;
import com.ses.zebra.pssdemo_2019.databinding.ActivityCallAssistantBinding;
import com.slacorp.eptt.android.service.CoreBinder;
import com.slacorp.eptt.core.common.ErrorCode;
import com.slacorp.eptt.core.common.SessionState;

public class ChatAssistantActivity extends CoreActivity {

    // Debugging
    private static final String TAG = "ChatAssistantActivity";

    // Constants
    private static final String ACTIVATION_CODE_1 = "ALHWNQUFXL";

    private static final String FROM = "from";
    private static final String BASKET_ACTIVITY = "basket-activity";
    private static final String OFFER_ACTIVITY = "offer-activity";
    private static final String SHOPPING_LIST_ACTIVITY = "shopping-list-activity";
    private static final String LOCATION_ACTIVITY = "location-activity";

    private CoreListener mCoreListener = new CoreListener();

    // Variables
    private String mActivationCode;
    private String mParentActivity;
    private ActivityCallAssistantBinding mDataBinding;
    private boolean isCoreProvisioned; // True when Core is Provisioned && We've started an activity

    /**
     * LifeCycleMethods
     * onCreate() -> No Functionality
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_assistant);

        // Init DataBinding
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_call_assistant);

        // Get Parent Activity
        getParentActivity();

        // Get Activation Code
        mActivationCode = mSharedPreferences.getString(PREF_WFC_ACTIVATION_CODE,
                getString(R.string.default_wfc_activation_code));

        // Init Back Button
        mDataBinding.headerIcon.setOnClickListener(view -> {
            // Shutdown Calls
            shutdown(callsEnded -> {
                // Log Shutdown Progress
                Log.i(TAG, "Shutdown Complete - Calls Ended: " + callsEnded
                        + " | Starting Parent Activity: " + mParentActivity);

                // Return to Parent
                switch(mParentActivity) {
                    case BASKET_ACTIVITY:
                        displayActivity(BasketActivity.class);
                        break;
                    case OFFER_ACTIVITY:
                        displayActivity(OffersListActivity.class);
                        break;
                    case SHOPPING_LIST_ACTIVITY:
                        displayActivity(ShoppingListActivity.class);
                        break;
                    case LOCATION_ACTIVITY:
                        displayActivity(VlcLightingActivity.class);
                        break;
                    default:
                        displayActivity(BasketActivity.class);
                }
            });
        });
    }

    private void getParentActivity() {
        Intent productIntent = getIntent();
        if (productIntent.getExtras() != null) {
            mParentActivity = productIntent.getStringExtra(FROM);
        } else {
            mParentActivity = null;
        }
    }

    /**
     * Core Listener Class
     * Listens for changes in Core State
     * error()              -> Catches CoreBinder in Error State
     * sessionStateChange() -> Notifies when CoreBinder Session State has changed
     **/

    @SuppressLint("SetTextI18n")
    private class CoreListener extends com.slacorp.eptt.android.service.CoreListener {
        @Override
        public void error(final int error, String extra) {
            // Log Error
            Log.e(TAG, "CoreBinder Error - " + error + ", " + extra);
            // Handle Error
            mHandler.post(() -> {
                switch (error) {
                    case ErrorCode.AUTH_FAILURE :
                    case ErrorCode.CONFIG_UPDATE_FAILURE_NO_DATA :
                        Log.e(TAG, "Error Provisioning CoreBinder - Stopping Core");

                        // Update UI
                        mDataBinding.provisioningText.setText("This Activation code(" +
                                mActivationCode + ") has already been registered");
                        mDataBinding.provisioningProgressBar.setVisibility(View.GONE);

                        // Get Core binder -> Stop Core
                        CoreBinder core = getCoreBinder();
                        if (core != null) { core.stopCore(); }
                        break;
                    case ErrorCode.CONFIG_UPDATE_FAILURE_USER_NOT_FOUND:
                        mDataBinding.provisioningText.setText("We could not find an account with " +
                                "this Activation Code (" + mActivationCode + ")");
                        mDataBinding.provisioningProgressBar.setVisibility(View.GONE);
                        break;
                    case ErrorCode.FATAL_ERROR:
                        mDataBinding.provisioningText.setText("We encountered a error, please try again");
                        mDataBinding.provisioningProgressBar.setVisibility(View.GONE);
                        break;
                    case ErrorCode.NETWORK_FAILURE:
                    case ErrorCode.NETWORK_OFFLINE:
                    case ErrorCode.NETWORK_TIMEOUT:
                        mDataBinding.provisioningText.setText("There was an error due to the network, please try again");
                        mDataBinding.provisioningProgressBar.setVisibility(View.GONE);
                        break;
                    case ErrorCode.STARTUP_FAILURE:
                        break;
                    default:
                        mDataBinding.provisioningText.setText("We encountered an error (" + error +
                                " - " + (extra == null ? "" : extra)+ "), please try again");
                        mDataBinding.provisioningProgressBar.setVisibility(View.GONE);
                        break;
                }
            });
        }

        @Override
        public void sessionStateChange(final int state) {
            mHandler.post(() -> {
                // Log State
                Log.i(TAG, "Session state: " + EnumToText.getSessionStateAsString(state));
                switch (state) {
                    // IDLE State = CoreBinder is successfully Provisioned and we can use it
                    case SessionState.IDLE:
                        if (!isCoreProvisioned) { startPushToTalkActivity(); }
                        break;
                    default:
                        break;
                }
            });
        }
    }

    /**
     * Inherited Methods
     * getInheritedTag()  -> Returns this TAG to Parent class for debugging
     * coreServiceBound() -> Attaches mCoreListener to CoreBinder
     *                       Provisions CoreBinder with ACTIVATION_CODE
     */
    @Override
    protected String getInheritedTag() { return TAG; }

    @Override
    protected void coreServiceBound(CoreBinder coreBinder) {
        // Attach CoreListener to CoreBinder
        coreBinder.addCoreListener(mCoreListener);
        // Provision CoreBinder with ACTIVATION_CODE
        // provisionCore(coreBinder, ACTIVATION_CODE_1);
        provisionCore(coreBinder, mActivationCode);
    }

    /**
     * Start (Provision) the Core Binder.
     * This is different than starting and binding the core service, which is done by the
     * CoreActivity base class. If the core provisions successfully, our CoreListener will get a
     * notification that we've entered the IDLE state.  Otherwise it'll get an error notification.
     */
    private void provisionCore(CoreBinder core, String activationCode) {
        Log.i(TAG, "Provisioning CoreBinder with Activation Code: " + activationCode);
        core.startCore(activationCode);
    }

    /**
     * Helper method to start PushToTalkActivity
     **/
    private void startPushToTalkActivity() {
        // Log Step
        Log.i(TAG, "Core Provisioned -> Starting Push To Talk Activity");
        // Set Core Provisioned True
        isCoreProvisioned = true;
        // Start Push To Talk Activity
        Intent startPushToTalkActivity = new Intent(ChatAssistantActivity.this,
                PushToTalkActivity.class);
        startPushToTalkActivity.putExtra(FROM, mParentActivity);
        startActivity(startPushToTalkActivity);
        // Finish && Exit this Activity
        finish();
    }
}
