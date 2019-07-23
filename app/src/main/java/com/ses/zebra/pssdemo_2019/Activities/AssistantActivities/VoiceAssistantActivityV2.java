package com.ses.zebra.pssdemo_2019.Activities.AssistantActivities;

import ai.api.model.AIError;
import ai.api.model.AIResponse;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Toast;
import com.ses.zebra.pssdemo_2019.Activities.BaseActivity;
import com.ses.zebra.pssdemo_2019.Activities.MainActivities.BasketActivity;
import com.ses.zebra.pssdemo_2019.Activities.MainActivities.OffersListActivity;
import com.ses.zebra.pssdemo_2019.Activities.MainActivities.ShoppingListActivity;
import com.ses.zebra.pssdemo_2019.Activities.MainActivities.VlcLightingActivity;
import com.ses.zebra.pssdemo_2019.Debugging.Logger;
import com.ses.zebra.pssdemo_2019.R;
import com.ses.zebra.pssdemo_2019.TTS;
import com.ses.zebra.pssdemo_2019.databinding.ActivityVoiceAssistantV2Binding;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class VoiceAssistantActivityV2 extends BaseActivity {

  // Debugging
  private static final String TAG = "VoiceAssistantActivity";

  // Constants
  private static final int VOICE_RECOGNITION_INTENT = 100;

  private static final String CUSTOM_TEXT_IDENTIFIER = "<b>You: </b>";
  private static final String ASSISTANT_TEXT_IDENTIFIER = "<b>zAssistant: </b>";

  private static final String FROM = "from";
  private static final String BASKET_ACTIVITY = "basket-activity";
  private static final String OFFER_ACTIVITY = "offer-activity";
  private static final String SHOPPING_LIST_ACTIVITY = "shopping-list-activity";
  private static final String LOCATION_ACTIVITY = "location-activity";

  // Variables
  private ActivityVoiceAssistantV2Binding mDataBinding;

  private static String mParentActivity;

  private List<String> mBeansResponses = new ArrayList<>();
  private List<String> mPointsResponses = new ArrayList<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_voice_assistant_v2);

    // Init DataBinding
    mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_voice_assistant_v2);

    // Init Scrolling Chatlog
    mDataBinding.voiceAssistantChatLog.setMovementMethod(new ScrollingMovementMethod());

    // Get Parent Activity
    getParentActivity();

    // Init Canned Responses
    mBeansResponses = Arrays.asList(getResources().getStringArray(R.array.beans_responses));
    mPointsResponses =Arrays.asList(getResources().getStringArray(R.array.points_responses));

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
    initVoiceAssistantButton();
  }

  private void initVoiceAssistantButton() {
    // Log
    Logger.i(TAG, "Initialising Voice Assistant Button");

    // Init Voice Assistant Button
    mDataBinding.contextualVoiceButton.setOnClickListener(view -> startVoiceRecognition());
  }

  private void startVoiceRecognition() {
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "What can I help with?");
    try {
      startActivityForResult(intent, VOICE_RECOGNITION_INTENT);
    } catch (ActivityNotFoundException a) {
      Toast.makeText(getApplicationContext(), "Sorry! Your device doesn't support speech input",
          Toast.LENGTH_LONG).show();
    }
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
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    switch (requestCode) {
      case VOICE_RECOGNITION_INTENT: {
        if (resultCode == RESULT_OK && data != null) {
          // List of Results
          List<String> speechResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

          // Log Response Info
          Log.d(TAG, "AIResponse Result Successful");
          Logger.i(TAG, "Speech: " + speechResult.get(0));

          // Build Request & Response Strings
          String request = speechResult.get(0);
          String result = "";
          if (request.contains("beans")) {
            result = mBeansResponses.get(new Random().nextInt(mBeansResponses.size()));
          } else if (request.contains("points")) {
            result = mPointsResponses.get(new Random().nextInt(mPointsResponses.size()));
          } else {
            result = "I'm sorry, I didn't catch that.";
          }

          // Vocalise Result
          TTS.speak(result);

          // Log Speech to TextView
          mDataBinding.voiceAssistantChatLog.append(Html.fromHtml(CUSTOM_TEXT_IDENTIFIER + request + "<br>"));
          mDataBinding.voiceAssistantChatLog.append(Html.fromHtml(ASSISTANT_TEXT_IDENTIFIER + result + "<br><br>"));
        }
        break;
      }

    }
  }

  @Override
  protected String getInheritedTag() {
    return TAG;
  }
}
