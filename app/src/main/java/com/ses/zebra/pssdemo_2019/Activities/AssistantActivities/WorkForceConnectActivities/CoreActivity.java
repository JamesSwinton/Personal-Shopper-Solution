package com.ses.zebra.pssdemo_2019.Activities.AssistantActivities.WorkForceConnectActivities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ses.zebra.pssdemo_2019.Activities.BaseActivity;
import com.ses.zebra.pssdemo_2019.Interfaces.ShutdownCallback;
import com.slacorp.eptt.android.common.core.DebuggerObserver;
import com.slacorp.eptt.android.service.CoreBinder;
import com.slacorp.eptt.android.service.CoreCall;
import com.slacorp.eptt.android.service.CoreCallManager;
import com.slacorp.eptt.android.service.CoreService;
import com.slacorp.eptt.android.service.CoreUtil;
import com.slacorp.eptt.core.common.CallEndReason;
import com.slacorp.eptt.core.common.ErrorCode;
import com.slacorp.eptt.jcommon.Debugger;

import java.io.File;

import static com.slacorp.eptt.core.common.SessionState.IDLE;
import static com.slacorp.eptt.core.common.SessionState.UNREGISTERED;

public abstract class CoreActivity extends BaseActivity {

    // Debugging (Inherited tag implemented by all sub classes)
    private static final String MAIN_TAG = "CoreActivity";
    private final String TAG = MAIN_TAG + " - " + getInheritedTag();

    // Constants
    final protected Handler mHandler = new Handler(); // Used for UI operations within Service Callbacks

    // Variables
    private CoreBinder mCoreBinder = null;
    private CoreBoundListener mCoreBoundListener;
    private CoreUnboundListener mCoreUnboundListener = new DefaultCoreUnboundListener();

    /**
     * Callback interfaces for sub-classes to be notified when Activity is Bound to Service
     **/
    protected interface CoreBoundListener { void onBind(); }
    protected interface CoreUnboundListener { void onUnbind(); }
    protected class DefaultCoreUnboundListener implements CoreUnboundListener {
        @Override
        public void onUnbind() {}
    }

    /**
     * Interface for monitoring the state of an application service
     * Calls above interfaces when service is connected / disconnect from activity
     **/
    protected ServiceConnection serviceConnectionListener = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Log Connection
            Log.i(TAG, "Service Connected to CoreActivity");
            // Set CoreBinder member Variable
            mCoreBinder = (CoreBinder) service;
            // Notify Sub-Classes
            mCoreBoundListener.onBind();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Log Disconnection
            Log.i(TAG, "Service disconnected from CoreActivity");
            // Remove CoreBinder Variable
            mCoreBinder = null;
            // Notify Sub-Classes
            mCoreUnboundListener.onUnbind();
        }
    };

    /**
     * Runnable to start ESChat Service - Service should start on Boot (via manifest) this method
     * will start the Service manually if something fails
     **/
    protected final Runnable startCoreService = new Runnable() {
        @Override
        public void run() {
            // Log Start Core
            Log.i(TAG, "StartCore Running...");

            // Listen for Bind
            mCoreBoundListener = () -> {
                // Log CoreBind
                Log.i(TAG, "CoreBound within startCoreService");
                // Initialise Core Binder (Check State -> Force registration)
                initialiseCoreBinder();
                // Notify Core Service is Bound
                coreServiceBound(mCoreBinder);
            };

            /*
             * Start Service via Intent
             */

            // Create Intent
            Intent serviceIntent = new Intent(getApplicationContext(), CoreService.class);
            // Get Intent Info
            if (getIntent() != null && getIntent().getAction() != null
                    && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
                serviceIntent.setAction(getIntent().getAction());
                serviceIntent.setData(getIntent().getData());
            }
            // Start Service -> Assign to ComponentName Variable to check Service Started
            ComponentName serviceComponent = startService(serviceIntent);
            Log.i(TAG, "Service Started - " + (serviceComponent == null ? "False" : "True"));

            /*
             * Bind Service
             */

            // Bind Service -> Attach Service Connection Listener to be notified when Service is bound
            boolean serviceBound = bindService(serviceIntent, serviceConnectionListener,
                                               Context.BIND_AUTO_CREATE);
            // Check BindService executed successfully
            Log.i(TAG, "Service Bound - " + (serviceBound));
        }
    };

    /**
     * Methods inherited by sub-classes
     * getCoreBinder()    -> Returns mCoreBinder to sub-class (Must check for null as mCoreBinder
     *                       is only valid when the Service is Bound to this Activity)
     * getHandler()       -> Returns Handler to allow for accessing the UI thread
     * getInheritedTag()  -> Allows this class to include the debug TAG of sub-classes
     * coreServiceBound() -> Notifies Sub-classes that override this method that CoreBinder has Bound
     **/

    @Nullable
    protected CoreBinder getCoreBinder() { return mCoreBinder; }
    public Handler getHandler() { return mHandler; }
    protected abstract String getInheritedTag();
    protected void coreServiceBound(CoreBinder coreBinder) {}

    /**
     * Life Cycle Methods
     * onCreate()  -> Initialises writing Logs to File
     * onResume()  -> Checks if Service is Bound. Starts or Initialises Service appropriately
     * onDestroy() -> Unbinds Service
     * finish()    -> Unbinds Service
     **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Log Version
        Log.i(TAG, "Starting WFC! Version: " + com.slacorp.eptt.core.common.Build.VERSION);

        if (Debugger.getInstance() == null) {
            File appRootDir = CoreUtil.getRootDirectory(this);
            Debugger.create(appRootDir.toString(), new DebuggerObserver());

            Debugger.i(TAG, "Debug log started - Directory: " + appRootDir.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if Service is Bound
        if (mCoreBinder != null) {
            Log.i(TAG, "Service Bound -> Initialising Service");
            if (mCoreBinder.getLastError() == ErrorCode.FATAL_ERROR) {
                finish();
                return;
            } else {
                initialiseCoreBinder();
            }
        // Service not Bound -> Start Service via Runnable
        } else {
            // Post the coreStarter to run later, so we don't hinder the GUI thread.
            Log.i(TAG, "Service not Bound -> Starting Service");
            mHandler.postDelayed(startCoreService, 100);
        }
    }

    @Override
    public void finish() {
        Debugger.i(TAG, "finish()");
        cleanup();
        super.finish();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy()");
        // cleanup();
        super.onDestroy();
    }

    private void cleanup() {
        // Unbind Core Service
        if (mCoreBinder != null) {
            Log.i(TAG, "Unbind from core service");
            unbindService(serviceConnectionListener);
            mCoreBinder = null;
        }

        // Flush the debugger to file.
        Debugger debugger = Debugger.getInstance();
        if (debugger != null) {
            debugger.flush();
        }
    }

    /**
     * CoreBinder Initialisation Method
     */

    private void initialiseCoreBinder() {
        // Check if mCoreBinder is Bound to Service
        if (mCoreBinder == null) {
            Log.i(TAG, "Core Binder not Bound to Service");
            return;
        }

        // Check if mCoreBinder is in FATAL_ERROR state
        if (mCoreBinder.getLastError() == ErrorCode.FATAL_ERROR) {
            Log.i(TAG, "Service encountered fatal error");
            finish();
            return;
        }

        // Get State -> Register CoreBinder
        int sessionState = mCoreBinder.getSessionState();
        switch(sessionState) {
            case IDLE:
            case UNREGISTERED:
                if (mCoreBinder.isCoreRunning()) { mCoreBinder.forceRegister(); }
                break;
            default:
                Log.i(TAG, "CoreBinder is in unhandled Session State");
                break;
        }
    }

    /**
     * Utility Methods for handling exiting the App
     * shutdown()    -> Triggers endAllCalls(), destroysCore && sets mCoreBinder to Null
     * endAllCalls() -> Loops through all Active Calls && Ends each one
     **/

    protected void shutdown(ShutdownCallback shutdownCallback) {
        // End Calls
        boolean callsEnded = endAllCalls();
        // Destroy the core object, but leave the service running.
        if (mCoreBinder != null) {
            // Unbind Service -> Leave running in background -> Attach listener to be notified
            unbindService(serviceConnectionListener);
            // Destroy Core -> Leave service running (@Param False = leave service running)
            mCoreBinder.destroyCore(true);
            mCoreBinder = null;
        }
        // Notify Calling Class
        shutdownCallback.onShutdownComplete(callsEnded);
    }

    protected boolean endAllCalls() {
        boolean callEnded = false;
        // Check CoreBinder is Running & Bound
        if (mCoreBinder != null) {
            // Get Call Manager
            CoreCallManager callManager = mCoreBinder.getCallManager();
            if (callManager != null) {
                // Loop through all calls -> End each one
                for (CoreCall call : callManager.getActiveCalls()) {
                    call.endCall(CallEndReason.USER_INITIATED);
                    callEnded = true;
                }
            }
        } return callEnded;
    }

    public void displayActivity(Class<?> Activity) {
        Intent i = new Intent(this, Activity);
        startActivity(i);
        finish();
        overridePendingTransition(0, 0);
    }
}
