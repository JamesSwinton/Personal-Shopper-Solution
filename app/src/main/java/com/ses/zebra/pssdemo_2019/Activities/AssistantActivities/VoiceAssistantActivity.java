package com.ses.zebra.pssdemo_2019.Activities.AssistantActivities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;

import com.ses.zebra.pssdemo_2019.Activities.BaseActivity;
import com.ses.zebra.pssdemo_2019.Activities.MainActivities.BasketActivity;
import com.ses.zebra.pssdemo_2019.Activities.MainActivities.OffersListActivity;
import com.ses.zebra.pssdemo_2019.Activities.MainActivities.ShoppingListActivity;
import com.ses.zebra.pssdemo_2019.Activities.MainActivities.VlcLightingActivity;
import com.ses.zebra.pssdemo_2019.Debugging.Logger;
import com.ses.zebra.pssdemo_2019.R;
import com.ses.zebra.pssdemo_2019.TTS;
import com.ses.zebra.pssdemo_2019.databinding.ActivityVoiceAssistantBinding;

import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.ui.AIButton;

public class VoiceAssistantActivity extends BaseActivity implements AIButton.AIButtonListener {

    // Debugging
    private static final String TAG = "VoiceAssistantActivity";

    // Constants

    private static final String FROM = "from";
    private static final String BASKET_ACTIVITY = "basket-activity";
    private static final String OFFER_ACTIVITY = "offer-activity";
    private static final String SHOPPING_LIST_ACTIVITY = "shopping-list-activity";
    private static final String LOCATION_ACTIVITY = "location-activity";

    // Variables
    private ActivityVoiceAssistantBinding mDataBinding;
    private AIConfiguration mAiConfig;
    private AIDataService mAiDataService;

    private static String mParentActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_assistant);

        // Init DataBinding
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_voice_assistant);

        // Init Scrolling Chatlog
        mDataBinding.voiceAssistantChatLog.setMovementMethod(new ScrollingMovementMethod());

        // Get Parent Activity
        getParentActivity();

        // Set back button listener
        mDataBinding.headerIcon.setOnClickListener(view -> {
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

        // Init Dialog Flow
        initDialogFlow();
    }

    private void initDialogFlow() {
        // Get Access token
        String aiAccessToken = mSharedPreferences.getString(PREF_AI_CONFIG_STRING,
                getString(R.string.default_ai_access_token));

        // Log
        Logger.i(TAG, "Initialising DialogFlow with access token: " + aiAccessToken);

        // Init Ai Button
        mAiConfig = new AIConfiguration(aiAccessToken,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        mDataBinding.contextualVoiceButton.initialize(mAiConfig);
        mDataBinding.contextualVoiceButton.setResultsListener(this);
        // Init AI Data Service
        mAiDataService = new AIDataService(this, mAiConfig);
    }

    private void getParentActivity() {
        Intent productIntent = getIntent();
        if (productIntent.getExtras() != null) {
            mParentActivity = productIntent.getStringExtra(FROM);
        } else {
            mParentActivity = null;
        }
    }

    /*
     * DialogFlow Methods
     */
    @Override
    public void onResult(AIResponse response) {
        runOnUiThread(() -> {
            // Log Response Info
            Log.d(TAG, "AIResponse Result Successful");
            Logger.i(TAG, "Status code: " + response.getStatus().getCode());
            Logger.i(TAG, "Resolved query: " + response.getResult().getResolvedQuery());
            Logger.i(TAG, "Action: " + response.getResult().getAction());
            Logger.i(TAG, "Speech: " + response.getResult().getFulfillment().getSpeech());

            // Get Speech
            String speech = response.getResult().getFulfillment().getSpeech();

            // Vocalise Result
            TTS.speak(speech);

            String customerTextIdentifier = "<b>You: </b>";
            String assistantTextIdentifier = "<b>zAssistant: </b>";

            // Log Speech to TextView
            mDataBinding.voiceAssistantChatLog.append(Html.fromHtml(customerTextIdentifier + response.getResult().getResolvedQuery() + "<br>"));
            mDataBinding.voiceAssistantChatLog.append(Html.fromHtml(assistantTextIdentifier + speech + "<br><br>"));
        });
    }

    @Override
    public void onError(AIError error) {
        runOnUiThread(() -> {
            // Log Error
            Log.e(TAG, "AI Error: " + error.getMessage());

            //
            String errorTextIdentifier = "<font color='red'><b>Error: </b></font><i>";

            // Log Speech to TextView
            mDataBinding.voiceAssistantChatLog.append(Html.fromHtml(errorTextIdentifier + error.getMessage() + "</i><br><br>"));
        });
    }

    @Override
    public void onCancelled() {
        runOnUiThread(() -> Log.e(TAG, "AI Cancelled"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause AI Listener, if visible
        if (mDataBinding.contextualVoiceButton.getVisibility() == View.VISIBLE) {
            mDataBinding.contextualVoiceButton.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume AI Listener, if visible
        if (mDataBinding.contextualVoiceButton.getVisibility() == View.VISIBLE) {
            mDataBinding.contextualVoiceButton.resume();
        }
    }

    @Override
    protected String getInheritedTag() {
        return TAG;
    }
}
