package com.ses.zebra.pssdemo_2019.EMDK;

import com.ses.zebra.pssdemo_2019.App;
import com.ses.zebra.pssdemo_2019.Debugging.Logger;
import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.personalshopper.CradleConfig;
import com.symbol.emdk.personalshopper.CradleException;
import com.symbol.emdk.personalshopper.CradleLedFlashInfo;
import com.symbol.emdk.personalshopper.CradleResults;
import com.symbol.emdk.personalshopper.PersonalShopper;

public class PSS {
    // Debugging
    private static final String TAG = "PSS";

    // Constants
    private static final int mOnDuration = 300; // Cradle LED on time in milliseconds
    private static final int mOffDuration = 300; // Cradle LED off time in milliseconds
    private static final int mUnlockDuration = 30;
    private static final boolean mSmoothEffect = true; // Enable or disable the smooth effect of the LED blinking

    // Variables
    private PersonalShopper mPersonalShopper;
    private CradleConfig.CradleLocation mCradleLocation;

    public CradleResults mLedsFlashed;
    public CradleResults mCradleUnlocked;

    // Get PersonalShopper instance from instance of EMDK manager in Application Class
    public PSS() {
        // Create PSS
        if (App.mEmdkManager != null) {
            // Create Personal Shopper Instance
            mPersonalShopper = (PersonalShopper)
                    App.mEmdkManager.getInstance(EMDKManager.FEATURE_TYPE.PERSONALSHOPPER);
            if (mPersonalShopper != null) {
                // Init PSS -> Set Location -> Unlock
                unlockDevice();
            } else {
                Logger.e(TAG, "PSS Object is null - Cannot unlock device",
                        new NullPointerException("PSS Object is null - Cannot unlock device"));
            }
        } else {
            Logger.e(TAG, "EMDK Manager is Null - Cannot create Personal Shopper Object",
                    new NullPointerException("EMDK Manager is Null - Cannot create Personal " +
                                             "Shopper Object"));
        }
    }

    private void unlockDevice() {
        try {
            // Enabled Cradle
            if (!mPersonalShopper.cradle.isEnabled()) {
                mPersonalShopper.cradle.enable();
            }

            //
            CradleLedFlashInfo cradleLedFlashInfo = new CradleLedFlashInfo(mOnDuration, mOffDuration,
                    mSmoothEffect);

            // Get Result of Cradle Unlock
            mCradleUnlocked = mPersonalShopper.cradle.unlock(mUnlockDuration,
                    cradleLedFlashInfo);

            // Log Result of Unlock && LED Flash
            Logger.i(TAG, mCradleUnlocked == CradleResults.SUCCESS
                    ? "Cradle Unlock Successful" : "Cradle Unlock Unsuccessful");

        } catch (CradleException e) {
            Logger.e(TAG, e.getResult().getDescription(), e);
        }
    }
}
