package com.ses.zebra.pssdemo_2019.Interfaces;

public interface WifiEnabledCallback {
    void onConnected();
    void onConnectionFailed();
    void onEnableFailed();
}
