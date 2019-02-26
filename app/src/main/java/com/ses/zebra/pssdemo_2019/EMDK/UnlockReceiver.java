package com.ses.zebra.pssdemo_2019.EMDK;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ses.zebra.pssdemo_2019.Debugging.Logger;

public class UnlockReceiver extends BroadcastReceiver {

    // Debugging
    private static final String TAG = "UnlockReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Log
        Logger.i(TAG, "onReceive - Intent Action: " + intent.getAction());

        // Init PSS & Unlock Cradle
        if (intent.getExtras() != null) {
            new PSS();
        }
    }
}
