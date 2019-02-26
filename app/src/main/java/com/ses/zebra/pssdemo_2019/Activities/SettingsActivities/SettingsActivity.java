package com.ses.zebra.pssdemo_2019.Activities.SettingsActivities;

import android.databinding.DataBindingUtil;
import android.support.v4.app.NavUtils;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;

import com.ses.zebra.pssdemo_2019.Activities.BaseActivity;
import com.ses.zebra.pssdemo_2019.Fragments.SettingsFragment;
import com.ses.zebra.pssdemo_2019.R;
import com.ses.zebra.pssdemo_2019.databinding.ActivitySettingsBinding;

public class SettingsActivity extends BaseActivity {

    // Debugging
    private static final String TAG = "SettingsActivity";

    // Constants
    private static final int DEMO_KIT_MAP = 0;
    private static final int BE_ZEC_MAP = 1;

    // Variables
    private ActivitySettingsBinding mDataBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init DataBinding
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_settings);

        // Init Title
        mDataBinding.headerLayout.headerText.setText("Settings");
        mDataBinding.headerLayout.headerIcon.setImageResource(R.drawable.ic_back);
        mDataBinding.headerLayout.headerIcon.setOnClickListener(view ->
                NavUtils.navigateUpFromSameTask(this));

        // Display Settings Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settingsFragmentContainer, new SettingsFragment())
                .commit();

        // Init SharedPreferences
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Initialise default values
        PreferenceManager.setDefaultValues(this, R.xml.configuration, false);
    }

    @Override
    protected String getInheritedTag() {
        return TAG;
    }


}
