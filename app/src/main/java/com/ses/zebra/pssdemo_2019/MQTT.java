package com.ses.zebra.pssdemo_2019;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.ses.zebra.pssdemo_2019.Activities.AssistantActivities.MessageAssistantActivity;
import com.ses.zebra.pssdemo_2019.Debugging.Logger;
import com.ses.zebra.pssdemo_2019.EMDK.PSS;
import com.ses.zebra.pssdemo_2019.Interfaces.HelpMessageCallback;
import com.ses.zebra.pssdemo_2019.POJOs.Assistance.HelpMessage;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static com.ses.zebra.pssdemo_2019.Activities.AssistantActivities.MessageAssistantActivity.mAssociateSerial;
import static com.ses.zebra.pssdemo_2019.Activities.BaseActivity.PREF_CURRENT_TOPIC;
import static com.ses.zebra.pssdemo_2019.Activities.BaseActivity.PREF_MQTT_BROKER;
import static com.ses.zebra.pssdemo_2019.Activities.BaseActivity.PREF_MQTT_PASS;
import static com.ses.zebra.pssdemo_2019.Activities.BaseActivity.PREF_MQTT_USER;
import static com.ses.zebra.pssdemo_2019.Activities.BaseActivity.mSharedPreferences;

public class MQTT {

    // Debugging
    private static final String TAG = "MQTT";

    // Constants
    private static final int QOS_LEVEL_0 = 0;
    private static final int QOS_LEVEL_1 = 1;
    private static final int QOS_LEVEL_2 = 2;

    private static final String DEFAULT_BROKER = "ssl://52.42.13.85";
    private static final String DEFAULT_USER = "ZebraMQTTGeneralUser";
    private static final String DEFAULT_PASS = "Zebra3278!7$!*hdh737$";

    private static final String DEFAULT_TOPIC = "PSSDEMO/MQTT/DEFAULT/DEFAULT/";
    private static final String HELP_REQUEST_BROADTCAST_TOPIC = "HELP_REQUEST_BROADCAST";
    private static final String HELP_REQUEST_BROADCAST_ACCEPTED_TOPIC = "HELP_REQUEST_BROADCAST_ACCEPTED";
    private static final String HELP_REQUEST_BROADCAST_ACCEPTED_REPLY_TOPIC = "HELP_REQUEST_BROADCAST_ACCEPTED_REPLY/";
    private static final String HELP_REQUEST_PRIVATE_CHAT_TOPIC = "CHAT_ASSIST/";
    private static final String HELP_REQUEST_PRIVATE_CHAT_ENDED_TOPIC = "CHAT_ASSIST_ENDED/";

    // Variables
    private static Gson mGson;
    private static String mUnlockTopic;
    private static String mHelpRequestBroadcastAcceptedTopic;
    private static List<String> mMqttTopics;
    private static MqttAsyncClient mMqttAsyncClient;

    private static IMqttToken mHelpMessagePublishToken;
    private static HelpMessageCallback mHelpMessageCallback;

    private static String mCurrentTopic;
    private static String mMqttBroker;
    private static String mMqttUser;
    private static String mMqttPass;

    public static void init() {
        if (mMqttAsyncClient == null || !mMqttAsyncClient.isConnected()) {
            // Init Gson
            mGson = new Gson();

            // Get Current Topic
            mCurrentTopic = mSharedPreferences.getString(PREF_CURRENT_TOPIC, DEFAULT_TOPIC);

            // Get MQTT Credentials
            mMqttBroker = mSharedPreferences.getString(PREF_MQTT_BROKER, DEFAULT_BROKER);
            mMqttUser = mSharedPreferences.getString(PREF_MQTT_USER, DEFAULT_USER);
            mMqttPass = mSharedPreferences.getString(PREF_MQTT_PASS, DEFAULT_PASS);

            // Init Topics
            mUnlockTopic = App.mDeviceSerialNumber;
            mHelpRequestBroadcastAcceptedTopic = mCurrentTopic +
                    HELP_REQUEST_BROADCAST_ACCEPTED_REPLY_TOPIC + App.mDeviceSerialNumber;

            // Build Topic List
            mMqttTopics = new ArrayList<>();
            mMqttTopics.add(mHelpRequestBroadcastAcceptedTopic);
            mMqttTopics.add(mUnlockTopic);

            // Create MQTT Async Client
            try {
                mMqttAsyncClient = new MqttAsyncClient(mMqttBroker, MqttClient.generateClientId(),
                        null);

                // Set Callback Handler
                mMqttAsyncClient.setCallback(mqttCallbackHandler());

                // Set Connection Options
                MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
                mqttConnectOptions.setCleanSession(true);
                mqttConnectOptions.setAutomaticReconnect(true);
                mqttConnectOptions.setSocketFactory(getSocketFactory());
                mqttConnectOptions.setUserName(mMqttUser);
                mqttConnectOptions.setPassword(mMqttPass.toCharArray());

                // Init Connection
                mMqttAsyncClient.connect(mqttConnectOptions);
            } catch (MqttException e) {
                Logger.e(TAG, "MQTT Exception: " + e.getMessage(), e);
            } catch (NullPointerException e) {
                Logger.e(TAG, "Null Pointer Exception: " + e.getMessage(), e);
            }
        }
    }

    public static void publishHelpMessage(String topic, HelpMessage helpMessage, HelpMessageCallback callback) throws MqttException {
        // Assign callback
        mHelpMessageCallback = callback;

        // Publish Message
        mHelpMessagePublishToken = mMqttAsyncClient.publish(
                topic, mGson.toJson(helpMessage).getBytes(), QOS_LEVEL_0, false);

        // Notify Caller
        if (mHelpMessagePublishToken == null) {
            callback.onError();
        } else {
            callback.onSent();
        }
    }

    public static void publish(String topic, byte[] message) throws MqttException {
        Log.i(TAG, "Publishing MQTT Message | Topic: " + topic + " | Payload: " + message);
        // Publish Message
        mHelpMessagePublishToken = mMqttAsyncClient.publish(topic, message, QOS_LEVEL_0, false);
    }

    public static void subscribeToTopic(String topic) {
        try {
            Logger.i(TAG, "Subscribing to topic: " + topic);
            mMqttAsyncClient.subscribe(topic, 0, null, mqttPublishCallbackHandler());
        } catch (MqttException e) {
            Logger.e(TAG, "MQTT Topic Subscription Error: " + e.getMessage(), e);
        }
    }

    public static void unsubscribeFromTopic(String topic) {
        try {
            Logger.i(TAG, "Subscribing to topic: " + topic);
            mMqttAsyncClient.unsubscribe(topic);
        } catch (MqttException e) {
            Logger.e(TAG, "MQTT Topic Subscription Error: " + e.getMessage(), e);
        }
    }

    private static void unsubscribeFromAllTopics() {
        try {
            for (String topic : mMqttTopics) {
                Logger.i(TAG, "Un-subscribing to topic: " + topic);
                mMqttAsyncClient.unsubscribe(topic);
            }
        } catch (MqttException e) {
            Logger.e(TAG, "MQTT Topic Un-subscription Error: " + e.getMessage(), e);
        }
    }

    private static void subscribeToAllTopics() {
        try {
            for (String topic : mMqttTopics) {
                Logger.i(TAG, "Subscribing to topic: " + topic);
                mMqttAsyncClient.subscribe(topic, QOS_LEVEL_0, null, mqttPublishCallbackHandler());
            }
        } catch (MqttException e) {
            Logger.e(TAG, "MQTT Topic Subscription Error: " + e.getMessage(), e);
        }
    }

    private static IMqttActionListener mqttPublishCallbackHandler() {
        return new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Logger.i(TAG,"Successfully Subscribed!");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable e) {
                Logger.e(TAG, "Failed to Subscribe: " + e.getMessage(), e);
            }
        };
    }

    private static MqttCallbackExtended mqttCallbackHandler() {
        return new MqttCallbackExtended() {
            /*
             * Called when the connection to the server is completed successfully.
             */
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                // Log connection Details
                Logger.i(TAG, "MQTT Connection Complete | Reconnect: " + reconnect
                        + " | URI: " + serverURI);

                // Subscribe to Topics
                subscribeToAllTopics();
            }

            /*
             * This method is called when the connection to the server is lost.
             */
            @Override
            public void connectionLost(Throwable cause) {
                Logger.e(TAG, "MQTT Client Disconnected - " + cause.getMessage(), cause);
            }

            /*
             * Called when delivery for a message has been completed,
             * and all acknowledgments have been received.
             */
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Logger.i(TAG, "MQTT Message Delivery Complete - " + token.toString());

                // Handle Help Message Delivery
                if (token.equals(mHelpMessagePublishToken)) {
                    mHelpMessageCallback.onDelivered();
                }
            }

            /*
             * This method is called when a message arrives from the server.
             */
            @Override
            public void messageArrived(String topic, MqttMessage message) {
                Logger.i(TAG, "MQTT Message Arrived | ID: " + message.getId() + "| Topic: " + topic
                        + " | Payload: " + new String(message.getPayload()));

                // Handle Message
                if (topic.equals(mUnlockTopic)) {
                    Log.i(TAG, "Unlock Command Received");
                    new PSS();
                    return;
                }

                if (topic.equals(mHelpRequestBroadcastAcceptedTopic)) {
                    MessageAssistantActivity.assistanceAccepted(new String(message.getPayload()));
                    return;
                }

                if (topic.equals(mCurrentTopic + HELP_REQUEST_PRIVATE_CHAT_TOPIC + mAssociateSerial
                        + "/" + App.mDeviceSerialNumber)) {
                    MessageAssistantActivity.updateChatLog(
                            mGson.fromJson(new String(message.getPayload()), HelpMessage.class));
                    return;
                }

                if (topic.equals(mCurrentTopic + HELP_REQUEST_PRIVATE_CHAT_ENDED_TOPIC
                        + mAssociateSerial + "/" + App.mDeviceSerialNumber)) {
                    MessageAssistantActivity.onChatEnded();
                    return;
                }
            }
        };
    }

    private static SSLSocketFactory getSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, new TrustManager[]{ getVeryTrustingTrustManager() },
                    new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            Logger.e(TAG, "NoSuchAlgorithmException: " + e.getMessage(), e);
        } catch (KeyManagementException e) {
            Logger.e(TAG, "KeyManagementException: " + e.getMessage(), e);
        } return null;
    }

    private static TrustManager getVeryTrustingTrustManager() {
        return new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                Logger.i(TAG, "Returning Accepted Issuers");
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                Logger.i(TAG, "Checking Clients Are Trusted");
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                Logger.i(TAG, "Checking Server Is Trusted");
            }
        };
    }

    public static void disconnect(Context cx, IMqttActionListener callback) {
        try {
            if (mMqttAsyncClient.isConnected()) {
                mMqttAsyncClient.disconnect(cx, callback);
            }
        } catch (Exception e) {
            Logger.e(TAG, "MQTT Exception: " + e.getMessage(), e);
        }
    }
}
