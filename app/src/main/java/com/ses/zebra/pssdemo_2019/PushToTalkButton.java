package com.ses.zebra.pssdemo_2019;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;

import com.ses.zebra.pssdemo_2019.Activities.AssistantActivities.WorkForceConnectActivities.PushToTalkActivity;


/**
 * Handles Push-To-Talk Button Actions
 */

public class PushToTalkButton {

    private static final String TAG = "PushToTalkButton";

    private Button mPushToTalkButton;
    private PushToTalkActivity mPushToTalkActivity;


    /**
     * Enum containing possible Push To Talk Button States
     */
    public enum PushToTalkButtonState {
        NO_CONTACT_SELECTED, WAIT_FOR_FLOOR, PUSH_TO_TALK, FLOOR_TAKEN, TALKING
    }

    /**
     * Constructor to assign Button & Parent Activity
     *
     * Set onTouchListener on PushToTalkButton to get state (Pressed or released)
     *
     * @param parentActivity
     * @param pushToTalkButton
     */
    @SuppressLint("ClickableViewAccessibility")
    public PushToTalkButton(PushToTalkActivity parentActivity, Button pushToTalkButton) {
        // Assign Buttons
        this.mPushToTalkButton = pushToTalkButton;
        this.mPushToTalkActivity = parentActivity;

        // Set onTouchListener to get Button State (Pushed or released)
        mPushToTalkButton.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getActionMasked()) {
                // Button Pressed
                case MotionEvent.ACTION_DOWN:
                    Log.i(TAG, "Push-To-Talk button pressed.");
                    mPushToTalkActivity.buttonPressed();
                    return true;
                case MotionEvent.ACTION_UP:
                    Log.i(TAG, "Push-to-Talk button released.");
                    mPushToTalkActivity.buttonReleased();
                    return true;
                default:
                    Log.i(TAG, "Unhandled onLongClick event: " + motionEvent.getActionMasked());
                    return false;
            }
        });
    }

    /**
     *
     * @param state
     */
    public void setPushToTalkButtonState(PushToTalkButtonState state) {
        mPushToTalkActivity.getHandler().post(() -> {
            int pushToTalkButtonColour = R.color.colorAccent;
            int pushToTalkButtonText = R.string.push_to_talk;

            switch (state) {
                case NO_CONTACT_SELECTED:
                    pushToTalkButtonColour = R.color.noContactSelected;
                    pushToTalkButtonText = R.string.no_contact_selected;
                    break;
                case FLOOR_TAKEN:
                    pushToTalkButtonColour = R.color.floorTaken;
                    pushToTalkButtonText = R.string.floor_taken;
                    break;
                case WAIT_FOR_FLOOR:
                    pushToTalkButtonColour = R.color.waitForFloor;
                    pushToTalkButtonText = R.string.wait_for_floor;
                    break;
                case TALKING:
                    pushToTalkButtonColour = R.color.talking;
                    pushToTalkButtonText = R.string.talking;
                    break;
                case PUSH_TO_TALK:
                    pushToTalkButtonColour = R.color.pushToTalk;
                    pushToTalkButtonText = R.string.push_to_talk;
                    break;
            }

            mPushToTalkButton.setText(pushToTalkButtonText);
            mPushToTalkButton.setBackgroundColor(mPushToTalkActivity.getResources()
                    .getColor(pushToTalkButtonColour));
        });
    }
}
