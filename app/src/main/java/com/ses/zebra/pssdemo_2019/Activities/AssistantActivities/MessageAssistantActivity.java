package com.ses.zebra.pssdemo_2019.Activities.AssistantActivities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.philips.indoorpositioning.library.IndoorPositioning;
import com.ses.zebra.pssdemo_2019.Activities.BaseActivity;
import com.ses.zebra.pssdemo_2019.Activities.MainActivities.BasketActivity;
import com.ses.zebra.pssdemo_2019.Activities.MainActivities.OffersListActivity;
import com.ses.zebra.pssdemo_2019.Activities.MainActivities.ShoppingListActivity;
import com.ses.zebra.pssdemo_2019.Activities.MainActivities.VlcLightingActivity;
import com.ses.zebra.pssdemo_2019.Activities.SubActivities.NavigationMenuActivity;
import com.ses.zebra.pssdemo_2019.Adapter.MessageAssistantAdapter;
import com.ses.zebra.pssdemo_2019.App;
import com.ses.zebra.pssdemo_2019.Debugging.Logger;
import com.ses.zebra.pssdemo_2019.Interfaces.HelpMessageCallback;
import com.ses.zebra.pssdemo_2019.MQTT;
import com.ses.zebra.pssdemo_2019.POJOs.Assistance.HelpMessage;
import com.ses.zebra.pssdemo_2019.R;
import com.ses.zebra.pssdemo_2019.databinding.ActivityMessageAssistantBinding;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageAssistantActivity extends BaseActivity {

    // Debugging
    private static final String TAG = "MessageAssistantActivity";

    // Constants
    private static Gson mGson = new Gson();
    private static Handler mHandler = new Handler();

    private static final String FROM = "from";
    private static final String OFFER_ACTIVITY = "offer-activity";
    private static final String BASKET_ACTIVITY = "basket-activity";
    private static final String LOCATION_ACTIVITY = "location-activity";
    private static final String NO_PARENT_ACTIVITY = "no-parent-activity";
    private static final String SHOPPING_LIST_ACTIVITY = "shopping-list-activity";

    private static final String DEFAULT_TOPIC = "PSSDEMO/MQTT/DEFAULT/DEFAULT/";
    private static final String HELP_REQUEST_BROADTCAST_TOPIC = "HELP_REQUEST_BROADCAST";
    private static final String HELP_REQUEST_BROADCAST_ACCEPTED_TOPIC = "HELP_REQUEST_BROADCAST_ACCEPTED";
    private static final String HELP_REQUEST_BROADCAST_ACCEPTED_REPLY_TOPIC = "HELP_REQUEST_BROADCAST_ACCEPTED_REPLY/";
    private static final String HELP_REQUEST_PRIVATE_CHAT_TOPIC = "CHAT_ASSIST/";
    private static final String HELP_REQUEST_PRIVATE_CHAT_ENDED_TOPIC = "CHAT_ASSIST_ENDED/";


    // Variables
    private ActivityMessageAssistantBinding mDataBinding;
    private static InputMethodManager mInputManager;
    private static HelpMessage helpMessage;
    private static List<HelpMessage> mHelpMessages;
    private static MessageAssistantAdapter mMessageAssistantAdapter;
    private static AlertDialog mWaitingForAcceptanceDialog;
    private static boolean mAssociateFound;
    private static boolean mInitialMessage;
    public static String mAssociateSerial;
    private static Activity mActivity;
    private static String mParentActivity;

    private static String mCurrentTopic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_assistant);

        // Initialise MQTT
        MQTT.init();

        // Init Variables
        mActivity = this;
        mInitialMessage = true;
        mAssociateFound = false;
        mCurrentTopic = mSharedPreferences.getString(PREF_CURRENT_TOPIC, DEFAULT_TOPIC);
        mHelpMessages = new ArrayList<>();
        mInputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_message_assistant);

        // Init Chat Log
        mMessageAssistantAdapter = new MessageAssistantAdapter(mHelpMessages);
        mDataBinding.chatLogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDataBinding.chatLogRecyclerView.setAdapter(mMessageAssistantAdapter);

        // Get Parent Activity && Set Back Button Click Listener Accordingly
        initNavigation();

        // Init Sent Button Listener
        initSendMessageListeners();
    }

    @Override
    protected String getInheritedTag() {
        return TAG;
    }

    private void initNavigation() {
        // Get Parent Activity
        Intent intent = getIntent();
        mParentActivity = (intent.getExtras() != null && intent.getStringExtra(FROM) != null)
                ? getIntent().getStringExtra(FROM) : NO_PARENT_ACTIVITY;

        // Set Back Navigation Click Listener
        mDataBinding.headerIcon.setOnClickListener(view -> {
            AlertDialog.Builder confirmExitDialogBuilder = new AlertDialog.Builder(this)
                    .setTitle("Confirm Exit")
                    .setMessage("Are you sure you wish to exit this chat? This action cannot be " +
                            "undone and you will lose connection with the customer")
                    .setPositiveButton("EXIT", (dialog, i) -> {
                        // Notify Customer
                        notifyChatEnding();
                        // End Chat and Reset Subscriptions in onDestroy()
                        // Return to original activity
                        switch (mParentActivity) {
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
                            case NO_PARENT_ACTIVITY:
                            default:
                                displayActivity(BasketActivity.class);
                        }
                    })
                    .setNegativeButton("CANCEL", (dialog, i) -> dialog.dismiss());

            // Create & Show Dialog
            AlertDialog confirmExitDialog = confirmExitDialogBuilder.create();
            confirmExitDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            confirmExitDialog.show();
            confirmExitDialog.getWindow().getDecorView().setSystemUiVisibility(
                    this.getWindow().getDecorView().getSystemUiVisibility());
            confirmExitDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        });
    }

    private void notifyChatEnding() {
        // Notify Chat Ending
        try {
            MQTT.publish(mCurrentTopic + HELP_REQUEST_PRIVATE_CHAT_ENDED_TOPIC
                    + App.mDeviceSerialNumber + "/" + mAssociateSerial, new byte[0]);
        } catch (MqttException e) {
            Logger.e(TAG, "MQTT Exception :" + e.getMessage(), e);
        }
    }

    private void initSendMessageListeners() {
        // Set "Send Request" Button Listener
        mDataBinding.sendRequestButton.setOnClickListener(view -> {
            String message = mDataBinding.assistanceMessage.getText().toString().trim();
            if (messageValidated(message)) {
                buildAndSendHelpMessage(message);
            }
        });

        // Set imeOptions Send Button Listener
        mDataBinding.sendRequestButton.setOnEditorActionListener((textView, i, keyEvent) -> {
            // If triggered by an enter key, this is the event; otherwise, this is null.
            if (keyEvent != null) {
                String message = mDataBinding.assistanceMessage.getText().toString().trim();
                if (messageValidated(message)) {
                    buildAndSendHelpMessage(message);
                    return true;
                }
            }
            return false;
        });
    }

    private boolean messageValidated(String message) {
        // Check message was entered
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please enter a message...", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void buildAndSendHelpMessage(String message) {
        // Create HelpMessage
        helpMessage = new HelpMessage(HelpMessage.MessageType.SENT, message,
                System.currentTimeMillis(), App.mDeviceSerialNumber);
        // Remove Text From Send Box
        mDataBinding.assistanceMessage.setText("");
        // Close Soft Keyboard
        mInputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        // MQTT Connected? -> Publish Message -> Init ChatLog Adapter
        try {
            if (mInitialMessage) {
                // Disable Repeat Messaging
                mDataBinding.assistanceMessage.setHint("Waiting for reply...");
                mDataBinding.assistanceMessage.setEnabled(false);
                mDataBinding.sendRequestButton.setEnabled(false);

                // Publish Message
                MQTT.publishHelpMessage(mCurrentTopic + HELP_REQUEST_BROADTCAST_TOPIC,
                        helpMessage, messageCallbackHandler());

                // Show Dialog waiting for Acceptance
                mWaitingForAcceptanceDialog = new ProgressDialog.Builder(this)
                        .setTitle("Contacting Associate")
                        .setMessage("Please wait while we contact an associate")
                        .setCancelable(false)
                        .setNegativeButton("CANCEL", (dialogInterface, i) -> returnToParentActivity())
                        .setOnDismissListener(dialogInterface -> {
                            // Enable Messaging
                            mDataBinding.assistanceMessage.setHint("Enter Message");
                            mDataBinding.assistanceMessage.setEnabled(true);
                            mDataBinding.sendRequestButton.setEnabled(true);
                        })
                        .show();

                // Dismiss waiting dialog if no response within 30 seconds
                mHandler.postDelayed(() -> {
                    if (!mAssociateFound) {
                        if (mWaitingForAcceptanceDialog.isShowing()) {
                            mWaitingForAcceptanceDialog.dismiss();
                        }

                        if (!this.isFinishing()) {
                            mWaitingForAcceptanceDialog = new ProgressDialog.Builder(this)
                                    .setTitle("Couldn't find associate")
                                    .setMessage("We couldn't contact an associate. Please try again")
                                    .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                                    .show();
                        }

                        mInitialMessage = true;
                    }
                }, 30000);
            } else {
                // Publish Message
                MQTT.publishHelpMessage(mCurrentTopic + HELP_REQUEST_PRIVATE_CHAT_TOPIC +
                                App.mDeviceSerialNumber + "/" + mAssociateSerial, helpMessage,
                        messageCallbackHandler());
            }

            mInitialMessage = false;

        } catch (MqttException e) {
            Logger.e(TAG, "MQTT Exception: " + e.getMessage(), e);

            // Enable Messaging
            mDataBinding.assistanceMessage.setHint("Enter Message");
            mDataBinding.assistanceMessage.setEnabled(true);
            mDataBinding.sendRequestButton.setEnabled(true);

            // Show MQTT Re-connect dialog
            new AlertDialog.Builder(this)
                    .setTitle("Messaging Client Disconnected")
                    .setMessage("We've encountered an issue with the Messaging client. " +
                            "Would you like to re-connect?")
                    .setPositiveButton("RETRY", (dialogInterface, i) -> MQTT.init())
                    .setNegativeButton("CANCEL", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        finish();
                    })
                    .show();

        }
    }

    private void returnToParentActivity() {
        switch (mParentActivity) {
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
            case NO_PARENT_ACTIVITY:
            default:
                displayActivity(BasketActivity.class);
        }
    }

    public static void updateChatLog(HelpMessage helpMessage) {
        mHandler.post(() -> {
            mHelpMessages.add(helpMessage);
            mMessageAssistantAdapter.updateMessageList(mHelpMessages);
        });
    }

    private HelpMessageCallback messageCallbackHandler() {
        return new HelpMessageCallback() {
            @Override
            public void onSent() {
                Logger.i(TAG, "Help Message Sent");

                // Update Chat Log
                updateChatLog(helpMessage);
            }

            @Override
            public void onDelivered() {
                Logger.i(TAG, "Help Message Delivered");
            }

            @Override
            public void onReply(String reply) {
                Logger.i(TAG, "Help Message Reply Received");

                // Update Chat Log
                updateChatLog(new HelpMessage(HelpMessage.MessageType.RECEIVED, reply,
                        System.currentTimeMillis(), ""));

                // Enable Message Sending
                runOnUiThread(() -> {
                    mDataBinding.assistanceMessage.setHint("Enter message");
                    mDataBinding.assistanceMessage.setEnabled(true);
                    mDataBinding.sendRequestButton.setEnabled(true);
                });
            }

            @Override
            public void onError() {
                Logger.i(TAG, "Help Message Error");

                // Enable Message Sending
                runOnUiThread(() -> {
                    mDataBinding.assistanceMessage.setHint("Enter message");
                    mDataBinding.assistanceMessage.setEnabled(true);
                    mDataBinding.sendRequestButton.setEnabled(true);
                });
            }
        };
    }

    public static void assistanceAccepted(String acceptanceMessage) {
        // Dismiss Dialog
        if (mWaitingForAcceptanceDialog.isShowing()) {
            mWaitingForAcceptanceDialog.dismiss();
        }

        mAssociateFound = true;

        // Get Request Accepted Object
        HelpMessage requestAccepted = mGson.fromJson(acceptanceMessage, HelpMessage.class);

        // Get Serial for Topic Subscription
        mAssociateSerial = requestAccepted.getDeviceSerialNumber();

        // Update Chat Log
        updateChatLog(requestAccepted);

        // Subscribe to Chat Topic
        MQTT.subscribeToTopic(mCurrentTopic + HELP_REQUEST_PRIVATE_CHAT_TOPIC + mAssociateSerial + "/" + App.mDeviceSerialNumber);
        MQTT.subscribeToTopic(mCurrentTopic + HELP_REQUEST_PRIVATE_CHAT_ENDED_TOPIC + mAssociateSerial + "/" + App.mDeviceSerialNumber);
    }

    public static void onChatEnded() {
        mHandler.post(() ->
                new AlertDialog.Builder(mActivity)
                        .setTitle("Chad Ended")
                        .setMessage("Chat was closed by the associate")
                        .setPositiveButton("OK", (dialog, i) -> {
                            mActivity.finish();
                            mActivity = null;
                        })
                        .show());
    }

    @Override
    protected void onDestroy() {
        // Unsubscribe Topics
        MQTT.unsubscribeFromTopic(mCurrentTopic + HELP_REQUEST_PRIVATE_CHAT_TOPIC + mAssociateSerial + "/" + App.mDeviceSerialNumber);
        MQTT.unsubscribeFromTopic(mCurrentTopic + HELP_REQUEST_PRIVATE_CHAT_ENDED_TOPIC + mAssociateSerial + "/" + App.mDeviceSerialNumber);

        // Disconnect MQTT
        MQTT.disconnect(this, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Logger.i(TAG, "MQTT Disconnected");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable e) {
                Logger.e(TAG, "Failure disconnecting from MQTT: " + e.getMessage(), e);
            }
        });

        // Pass to super class
        super.onDestroy();
    }
}