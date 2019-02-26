package com.ses.zebra.pssdemo_2019.Activities.AssistantActivities.WorkForceConnectActivities;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.WindowManager;

import com.ses.zebra.pssdemo_2019.Activities.MainActivities.VlcLightingActivity;
import com.ses.zebra.pssdemo_2019.Debugging.Logger;
import com.ses.zebra.pssdemo_2019.Fragments.ContactListFragment;
import com.ses.zebra.pssdemo_2019.Fragments.InCallFragment;
import com.ses.zebra.pssdemo_2019.PushToTalkButton;
import com.ses.zebra.pssdemo_2019.R;
import com.ses.zebra.pssdemo_2019.databinding.ActivityPushToTalkBinding;
import com.slacorp.eptt.android.service.CallListener;
import com.slacorp.eptt.android.service.CallManagerListener;
import com.slacorp.eptt.android.service.CoreBinder;
import com.slacorp.eptt.android.service.CoreCall;
import com.slacorp.eptt.android.service.CoreCallManager;
import com.slacorp.eptt.core.common.ContactList;
import com.slacorp.eptt.core.common.Participant;
import com.slacorp.eptt.core.common.Protocol;
import com.slacorp.eptt.jcommon.Debugger;

import java.util.List;

import static com.ses.zebra.pssdemo_2019.PushToTalkButton.PushToTalkButtonState.FLOOR_TAKEN;
import static com.ses.zebra.pssdemo_2019.PushToTalkButton.PushToTalkButtonState.NO_CONTACT_SELECTED;
import static com.ses.zebra.pssdemo_2019.PushToTalkButton.PushToTalkButtonState.PUSH_TO_TALK;
import static com.ses.zebra.pssdemo_2019.PushToTalkButton.PushToTalkButtonState.TALKING;
import static com.ses.zebra.pssdemo_2019.PushToTalkButton.PushToTalkButtonState.WAIT_FOR_FLOOR;

public class PushToTalkActivity extends CoreActivity {

    // Debugging
    private static final String TAG = "PushToTalkActivity";

    // Constants
    private static final String FROM = "from";
    private static final String BASKET_ACTIVITY = "basket-activity";
    private static final String OFFER_ACTIVITY = "offer-activity";
    private static final String SHOPPING_LIST_ACTIVITY = "shopping-list-activity";
    private static final String LOCATION_ACTIVITY = "location-activity";

    // Variables
    private ActivityPushToTalkBinding mDataBinding;
    private static String mParentActivity;

    private CoreCall mCoreCall;
    private PushToTalkButton mPushToTalkButton;
    private PushToTalkCallListener mCallListener = new PushToTalkCallListener();
    private PushToTalkCallManagerListener mCallManagerListener = new PushToTalkCallManagerListener();

    private InCallFragment mInCallFragment;
    private ContactListFragment mContactListFragment;

    /**
     * Life Cycle Methods
     * onCreate()       -> Init DataBinding
     *                     Init PushToTalkButton
     *                     Get or Create Fragment depending on savedInstanceState
     * onStart()        -> Show Fragment (InCall / ContactList) depending on state
     * onStop()         -> De-registers Listeners
     * finish()         -> De-registers Listeners
     * onBackPressed()  -> Ends Any Calls, then returns to parent Activity
     **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_to_talk);

        // Init DataBinding
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_push_to_talk);

        // Init PushToTalkButton Class Instance -> Pass Button to Class
        mPushToTalkButton = new PushToTalkButton(this, mDataBinding.pushToTalkButton);

        // Get Parent Activity
        getParentActivity();

        // Init Back Button
        mDataBinding.headerIcon.setOnClickListener(view -> {
            // Build Dialog
            AlertDialog.Builder locationErrorDialogBuilder = new AlertDialog.Builder(this)
                    .setTitle("Ending Calls")
                    .setMessage("Please wait while we end your session...")
                    .setOnDismissListener(dialogInterface -> finish())
                    .setCancelable(false);

            // Show Dialog without showing navigation
            AlertDialog locationErrorDialog = locationErrorDialogBuilder.create();
            locationErrorDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            locationErrorDialog.show();
            locationErrorDialog.getWindow().getDecorView().setSystemUiVisibility(
                    this.getWindow().getDecorView().getSystemUiVisibility());
            locationErrorDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

            // Deregister Listeners
            deregisterListeners();

            // Shutdown Calls
            shutdown(callsEnded -> {
                // Log Shutdown Progress
                Log.i(TAG, "Shutdown Complete - Calls Ended: " + callsEnded
                        + " | Starting Parent Activity: " + mParentActivity);

                // Finish
                mDataBinding.headerIcon.postDelayed(locationErrorDialog::dismiss, 5000);
            });
        });

        // Init Fragments
        if (savedInstanceState == null) {
            Log.i(TAG, "No existing state - Creating new fragments...");

            // Create new Fragments
            mInCallFragment = new InCallFragment();
            mContactListFragment = new ContactListFragment();

            // Add mContactListFragment to FrameLayout (fragmentHolder) in this Activity
            getFragmentManager().beginTransaction()
                    .add(R.id.fragmentHolder, mContactListFragment).commit();
            return;
        }


        // Saved Instance State Found -> Get Existing Fragment from Holder
        Log.i(TAG, "Found existing state - Loading fragments...");
        Fragment fragment = getFragmentManager().findFragmentById(R.id.fragmentHolder);

        // Determine Fragment Type (Null / InCall / ContactList)
        if (fragment == null) {
            Log.e(TAG, "Fragment is null. Cannot load Fragment.");
            return;
        }

        if (fragment instanceof InCallFragment) {
            Log.i(TAG, "Successfully Loaded InCallListFragment");

            // Load InCallFragment && Assign to Member Variable -> Pass CoreCall Object
            mInCallFragment = (InCallFragment) fragment;
            mInCallFragment.setCall(mCoreCall);

            // Create new ContactListFragment
            mContactListFragment = new ContactListFragment();
            return;
        }

        if (fragment instanceof ContactListFragment) {
            Log.i(TAG, "Successfully Loaded ContactListFragment");

            // Load ContactListFragment
            mContactListFragment = (ContactListFragment) fragment;

            // Create new InCallListFragment
            mInCallFragment = new InCallFragment();
            return;
        }

        // Handle Unknown Fragment State (Shouldn't occour)
        Log.e(TAG, "Cannot Load Fragment of Unknown Type: " + fragment);
    }

    private void getParentActivity() {
        Intent productIntent = getIntent();
        if (productIntent.getExtras() != null) {
            mParentActivity = productIntent.getStringExtra(FROM);
        } else {
            mParentActivity = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()");
        showFragment();
    }

    @Override
    public void onStop() {
        Log.i(TAG, "onStop()");
        // deregisterListeners();
        super.onStop();
    }

    @Override
    public void finish() {
        Log.i(TAG, "onFinish()");
        // deregisterListeners();
        super.finish();
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed()");
        // Ends any calls that we're currently in
        // -> Closes InCallFragment
        // -> Returns to ContactListFragment
        // -> CallListener handles closing in onEnded()
        // -> Super Class finishes Activity & returns to Parent
        if (endAllCalls()) { return; }
        super.onBackPressed();
    }

    private class PushToTalkCallListener extends CallListener {

        /**
         * Log State Change
         */
        public void stateChange(int state) {
            Log.i(TAG, "Call State Changed: " + state);
        }

        /**
         * Notify InCall Fragment that Call is Updated
         */
        public void update() {
            Log.i(TAG, "Call Updated");

            // Notify InCallFragment that Call has Updated
            mHandler.post(() -> mInCallFragment.update());
        }

        /**
         * This method is called whenever a call ends (either MO or MT)
         * Sets CoreCall to Null && Displays Contact List Fragment
         *
         * @param callEndReason Reason the call ended from CallEndReason.
         * @see com.slacorp.eptt.core.common.CallEndReason
         */
        public void ended(int callEndReason) {
            Log.i(TAG, "Call Ended | Reason: " + callEndReason);
            // Set Core Null -> Display Contact List Fragment
            mCoreCall = null;
            // Show Contact List Fragment
            mHandler.post(PushToTalkActivity.this::showContactListFragment);
        }

        /**
         * No Call Present, set Button State to allow starting of call
         */
        public void floorIdle() {
            Log.i(TAG, "Floor is idle");

            // Set Button State
            setPushToTalkButtonState(PUSH_TO_TALK);
        }

        /**
         * The Floor is Granted to this participant
         *
         * @param index Indicates the location of the user in the
         *              participant list. If the user's index is
         *              unknown this will be reported as -1.
         */
        public void floorGrant(int index) {
            Log.i(TAG, "Floor granted");

            // Set Button State
            setPushToTalkButtonState(TALKING);
        }

        /**
         * The Floor is Denied to this participant
         */
        public void floorDeny() {
            Debugger.i(TAG, "Floor denied");

            // Set Button State
            setPushToTalkButtonState(PUSH_TO_TALK);
        }

        /**
         * The Floor is Revoked for this participant
         */
        public void floorRevoke() {
            Debugger.i(TAG, "Floor revoked");

            // Set Button State
            setPushToTalkButtonState(PUSH_TO_TALK);
        }

        /**
         * The Floor has been Taken by another participant.
         * NOTE: The participant index will indicate which position is talking.
         * The name may be changed if the server sent a new name in
         * the FloorTaken message.
         *
         * @param participant      Indicates which participant has the floor
         * @param overridePossible Set to true if this user can override the
         *                         floor, false otherwise.
         */
        public void floorTaken(Participant participant, boolean overridePossible) {
            Debugger.i(TAG, "Floor taken by " + participant.name);

            // Set Button State
            setPushToTalkButtonState(FLOOR_TAKEN);
        }
    }

    /**
     * Class for Listening to Call Starting or Call Errors
     * call() -> Called when Call is started
     *        -> Assign Call Object to Member Variable
     *        -> Attach Call Listener
     *        -> Show in Call Fragment
     **/
    private class PushToTalkCallManagerListener extends CallManagerListener {

        @Override
        public void call(CoreCall call) {
            Log.i(TAG, "Call Started - " + call.getTitle());

            // Set CoreCall && assign listener
            mCoreCall = call;
            mCoreCall.setListener(mCallListener);

            // Show InCallFragment
            showInCallFragment();
        }

        @Override
        public void error(int index, int error, String extra) {
            Log.e(TAG, "Call Manager Error: " + error + " | Index: " + index
                    + " | Extra Detail: " + extra);
        }

    }

    /**
     * Inherited Methods
     * getInheritedTag()  -> Returns this TAG to Parent class for debugging
     * coreServiceBound() -> Notifies Fragment that Core has been Bound to Activity
     *                       Displays Fragment (Call or Contacts, depending on state)
     */
    @Override
    protected String getInheritedTag() { return TAG; }

    @Override
    protected void coreServiceBound(CoreBinder coreBinder) {
        Log.i(TAG, "Core Service Bound");

        if (mContactListFragment != null) {
            mContactListFragment.coreServiceBound(coreBinder);
        } else {
            Log.w(TAG, "ContactListFragment not Initialised. Cannot notify CoreBound");
        }

        // Show correct Fragment
        showFragment();
    }

    /**
     * PushToTalkButton Callback Methods
     * buttonPressed()  -> Gets CoreBinder && CoreCallManager -> Performs Null Checks
     *                  -> Gets Currently Selected Contact -> Performs Null Check
     *                  -> Initiates Call with Current Contact
     * buttonReleased() -> Gets CoreBinder && CoreCallManager -> Performs Null Checks
     *                  -> Gets Currently Selected Contact -> Performs Null Check
     *                  -> Notifies CoreCallManager that PTTButton was Released
     */

    public void buttonPressed() {
        Log.i(TAG, "PushToTalkButton Pressed");

        // Get CoreBinder Instance
        CoreBinder coreBinder = getCoreBinder();

        // Check CoreBinder is not Null
        if (coreBinder == null) {
            Log.e(TAG, "CoreBinder is Null. Call cannot be initiated");
            return;
        }

        // CoreBinder is not null -> Get CallCoreManager Instance
        CoreCallManager coreCallManager = coreBinder.getCallManager();

        // Check CoreCallManager is not Null
        if (coreCallManager == null) {
            Log.e(TAG, "CoreCallManager is Null. Call cannot be initiated");
            return;
        }

        // Check if we're currently in a call.
        // If we are -> Set button state to WAIT_FOR_FLOOR.
        // mPushToTalkCallListener will be notified when floor is granted
        if (coreCallManager.getInCall()) {
            Log.i(TAG, "In Call -> Waiting for Floor");
            coreCallManager.pttPressed();
            return;
        }

        // Not In Call -> Get Selected Contact
        ContactList.Entry contact = mContactListFragment.getSelectedContact();

        // Check Contact is not Null
        if (contact == null) {
            Log.e(TAG, "No Contact Selected. Call cannot be initiated");
            setPushToTalkButtonState(NO_CONTACT_SELECTED);
            return;
        }

        // Contact Selected -> Start Call
        // N.B. Call can be initiated with multiple contacts, we're only passing 1
        Log.i(TAG, "Starting call to: " + contact.username);
        coreCallManager.originateAdhocCall(Protocol.CALL_TYPE_BARGE, contact);
        setPushToTalkButtonState(WAIT_FOR_FLOOR);
        coreCallManager.pttPressed();
    }

    public void buttonReleased() {
        Log.i(TAG, "PushToTalkButton Released");

        // Reset PushToTalk Button State
        setPushToTalkButtonState(PUSH_TO_TALK);

        // Get CoreBinder Instance
        CoreBinder coreBinder = getCoreBinder();

        // Check CoreBinder is not Null
        if (coreBinder == null) {
            Log.e(TAG, "CoreBinder is Null. Call cannot notify CallManager of PTT State");
            return;
        }

        // CoreBinder is not null -> Get CallCoreManager Instance
        CoreCallManager coreCallManager = coreBinder.getCallManager();

        // Check CoreCallManager is not Null
        if (coreCallManager == null) {
            Log.e(TAG, "CoreCallManager is Null. Call cannot notify CallManager of PTT State");
            return;
        }

        // Notify CallManager
        Log.i(TAG, "CoreCallManager Notified");
        coreCallManager.pttReleased();
    }

    /**
     * Call Registering & De-registering Utility Methods
     * Called when during showFragment() && finish() + onStop()
     *
     * registerListeners()      -> Assigns Listener to each Active Call
     * deregisterListeners()    -> Removes Listener from each Active Call
     */

    private boolean registerListeners() {
        Log.i(TAG, "Registering Listeners");

        // Reset CoreCall Object
        mCoreCall = null;

        // Get Core Object
        CoreBinder coreBinder = getCoreBinder();

        // Check CoreBinder for Null
        if (coreBinder == null) {
            Log.e(TAG, "CoreBinder is Null. Cannot Register CallManagerListeners.");
        } else {
            // Get CoreCallManager
            CoreCallManager coreCallManager = coreBinder.getCallManager();
            // Check CoreCallManager for Null
            if (coreCallManager == null) {
                Log.e(TAG, "CoreManager is Null. Cannot Register CallManagerListeners.");
            } else {
                // Add CallManagerListener if not already added (there can be multiple)
                if (!coreCallManager.hasListener(mCallManagerListener)) {
                    coreCallManager.addListener(mCallManagerListener);
                }

                // Get All Active Calls -> Add Listener to each call (Only 1 active call in this instance)
                List<CoreCall> callList = coreCallManager.getActiveCalls();
                if (callList.size() > 0) {
                    mCoreCall = callList.get(0);
                    mCoreCall.setListener(mCallListener);
                    Log.i(TAG, "Registering CallListener for call: " + mCoreCall);
                }
            }
        }

        // Add Call to Fragment
        if (mInCallFragment != null) { mInCallFragment.setCall(mCoreCall); }

        // Return true if mCoreCall listener was set
        return !(mCoreCall == null);
    }

    private void deregisterListeners() {
        Log.i(TAG, "De-registering Listeners");

        // Get Core Object
        CoreBinder coreBinder = getCoreBinder();

        // Check CoreBinder for Null
        if (coreBinder == null) {
            Log.e(TAG, "CoreBinder is Null. Cannot De-register CallManagerListeners.");
        } else {
            // Log Core Binder Access
            Log.i(TAG, "De-registering Listeners - Getting CoreCallManager");
            // Get CoreCallManager
            CoreCallManager coreCallManager = coreBinder.getCallManager();
            // Check CoreCallManager for Null
            if (coreCallManager == null) {
                Log.e(TAG, "CoreManager is Null. Cannot De-register CallManagerListeners.");
            } else {
                // Log CoreCallManager Access
                Log.i(TAG, "De-registering Listeners - Removing listeners from CoreCallManager");

                // Remove Any Listeners from CallManager
                if (coreCallManager.hasListener(mCallManagerListener) ) {
                    coreCallManager.removeListener(mCallManagerListener);
                }

                // Remove listener from each Active Call
                for (CoreCall coreCall : coreCallManager.getActiveCalls()) {
                    coreCall.setListener(null);
                }
            }
        }
    }

    /**
     * Fragment Utility Methods
     * showFragment()           -> Determines which fragment to show (InCall/ContactList)
     * showInCallFragment()     -> Shows InCallFragment
     * showCallListFragment()   -> Shows CallListFragment
     */

    private void showFragment() {
        Log.i(TAG, "Determine Fragment to show...");

        // Get Current Fragment from FragmentHolder
        Fragment fragment = getFragmentManager().findFragmentById(R.id.fragmentHolder);
        Debugger.i(TAG, "showFragment(), fragment found in contact_list_frame: " + fragment);

        // Register Listeners to determine if we're in-call or not
        boolean inCall = registerListeners();

        // If In-Call, replace ContactListFragment with InCallFragment if not already showing
        // Else If not In-Call, replace InCallFragment with ContactListFragment if not already showing
        if (inCall && !(fragment instanceof InCallFragment)) {
            showInCallFragment();
        } else if (!inCall && !(fragment instanceof ContactListFragment)) {
            showContactListFragment();
        }
    }

    private void showInCallFragment() {
        Log.i(TAG, "Initialising InCallFragment");

        // Display Fragment
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentHolder, mInCallFragment);
        transaction.commit();
    }

    private void showContactListFragment() {
        Log.i(TAG, "Initialising ContactListFragment");

        // Display Fragment
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentHolder, mContactListFragment);
        transaction.commit();
    }

    /**
     * Utility Methods
     *
     * setPushToTalkButtonState() -> Calls setPushToTalkButtonState in PushToTalkButton and sets the state of the button
     */

    private void setPushToTalkButtonState(PushToTalkButton.PushToTalkButtonState state) {
        mHandler.post(() -> mPushToTalkButton.setPushToTalkButtonState(state));
    }
}
