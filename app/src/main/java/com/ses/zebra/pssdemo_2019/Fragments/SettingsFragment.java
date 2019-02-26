package com.ses.zebra.pssdemo_2019.Fragments;

import android.os.Bundle;
import android.support.v7.preference.Preference;

import com.ses.zebra.pssdemo_2019.Activities.SettingsActivities.SettingsActivity;
import com.ses.zebra.pssdemo_2019.R;
import com.takisoft.fix.support.v7.preference.EditTextPreference;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;


import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends PreferenceFragmentCompat {

    //
    private static final String TAG = "SettingsFragment";

    //
    private static final String PREF_SELECT_CURRENCY = "currency";
    private static final String PREF_EDIT_STOCK_LIST = "edit_stock_list";
    private static final String PREF_ENABLE_MQTT = "mqtt_enabled";
    private static final String PREF_CUSTOM_MQTT_SERVER = "mqtt_use_custom_broker";
    private static final String PREF_MQTT_BROKER = "mqtt_broker";
    private static final String PREF_MQTT_USER = "mqtt_broker_user";
    private static final String PREF_MQTT_PASS = "mqtt_broker_pass";
    private static final String PREF_CURRENT_TOPIC = "mqtt_topic";
    private static final String PREF_TOPIC_IDENTIFIER = "mqtt_topic_identifier";
    private static final String PREF_SUB_TOPIC_IDENTIFIER = "mqtt_sub_topic_identifier";
    private static final String PREF_ENABLE_WFC = "wfc_enabled";
    private static final String PREF_WFC_ACTIVATION_CODE = "wfc_provisioning_code";
    private static final String PREF_ENABLE_VLC = "vlc_enabled";
    private static final String PREF_VLC_CONFIG_STRING = "vlc_config_string";
    private static final String PREF_ENABLE_CONTEXTUAL_VOICE = "contextual_voice_enabled";
    private static final String PREF_AI_CONFIG_STRING = "contextual_voice_config_string";

    // Variables
    private static List<Preference> mPreferences = new ArrayList<>();
    private static Preference mPrefCurrentTopic;
    private static Preference mPrefEditStockList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.configuration, rootKey);

        // Init Preferences
        initPreferences();

        // Set Preference Click Listeners
        findPreference(PREF_EDIT_STOCK_LIST).setOnPreferenceClickListener(preference -> false);
    }

    private void initPreferences() {
        // Get Commonly Used Prefs
        mPrefCurrentTopic = findPreference(PREF_CURRENT_TOPIC);
        mPrefEditStockList = findPreference(PREF_EDIT_STOCK_LIST);

        // Add Preferences to List
        mPreferences.add(mPrefEditStockList);
        mPreferences.add(mPrefCurrentTopic);
        mPreferences.add(findPreference(PREF_SELECT_CURRENCY));
        mPreferences.add(findPreference(PREF_ENABLE_MQTT));
        mPreferences.add(findPreference(PREF_CUSTOM_MQTT_SERVER));
        mPreferences.add(findPreference(PREF_MQTT_BROKER));
        mPreferences.add(findPreference(PREF_MQTT_USER));
        mPreferences.add(findPreference(PREF_MQTT_PASS));
        mPreferences.add(findPreference(PREF_TOPIC_IDENTIFIER));
        mPreferences.add(findPreference(PREF_SUB_TOPIC_IDENTIFIER));
        mPreferences.add(findPreference(PREF_ENABLE_WFC));
        mPreferences.add(findPreference(PREF_WFC_ACTIVATION_CODE));
        mPreferences.add(findPreference(PREF_ENABLE_VLC));
        mPreferences.add(findPreference(PREF_VLC_CONFIG_STRING));
        mPreferences.add(findPreference(PREF_ENABLE_CONTEXTUAL_VOICE));
        mPreferences.add(findPreference(PREF_AI_CONFIG_STRING));

        // Set Change Listener
        for (Preference preference : mPreferences) {
            preference.setOnPreferenceChangeListener(prefChangeListener);
            if (setValueAsSummary(preference)) {
                preference.setSummary(SettingsActivity.mSharedPreferences.getString(
                        preference.getKey(), ""));
            }
        }
    }

    private Preference.OnPreferenceChangeListener prefChangeListener = (preference, newValue) -> {
        // Update MQTT Topic Preference
        if (preference.getKey().equals(PREF_TOPIC_IDENTIFIER)) {
            // Get Sub Topic Identifier
            String subTopicIdentifier = SettingsActivity.mSharedPreferences
                    .getString(PREF_SUB_TOPIC_IDENTIFIER, "DEFAULT");

            // Build New MQTT String
            String newTopicStructure = "PSSDEMO/MQTT/" + newValue.toString().trim().toUpperCase()
                    + "/" + subTopicIdentifier.trim().toUpperCase() + "/";

            // Apply
            SettingsActivity.mSharedPreferences.edit()
                    .putString(PREF_CURRENT_TOPIC, newTopicStructure).apply();

            // Set Summary
            mPrefCurrentTopic.setSummary(newTopicStructure);
        }

        if (preference.getKey().equals(PREF_SUB_TOPIC_IDENTIFIER)) {
            // Get Topic Identifier
            String topicIdentifier = SettingsActivity.mSharedPreferences
                    .getString(PREF_TOPIC_IDENTIFIER, "DEFAULT");

            // Build New MQTT String
            String newTopicStructure = "PSSDEMO/MQTT/" + topicIdentifier.trim().toUpperCase() + "/"
                    + newValue.toString().trim().toUpperCase() + "/";

            // Apply
            SettingsActivity.mSharedPreferences.edit()
                    .putString(PREF_CURRENT_TOPIC, newTopicStructure).apply();

            // Set Summary
            mPrefCurrentTopic.setSummary(newTopicStructure);
        }

        // Set Preference Summary
        if (setValueAsSummary(preference) && preference instanceof EditTextPreference) {
            preference.setSummary(String.valueOf(newValue).toUpperCase());
        }

        // Update Preference
        return true;
    };

    private static boolean setValueAsSummary(Preference preference) {
        return preference.getKey().equals(PREF_CURRENT_TOPIC)
                || preference.getKey().equals(PREF_TOPIC_IDENTIFIER)
                || preference.getKey().equals(PREF_SUB_TOPIC_IDENTIFIER)
                || preference.getKey().equals(PREF_MQTT_BROKER)
                || preference.getKey().equals(PREF_MQTT_USER)
                || preference.getKey().equals(PREF_MQTT_PASS)
                || preference.getKey().equals(PREF_WFC_ACTIVATION_CODE)
                || preference.getKey().equals(PREF_VLC_CONFIG_STRING)
                || preference.getKey().equals(PREF_AI_CONFIG_STRING);
    }

}
