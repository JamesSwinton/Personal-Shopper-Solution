package com.ses.zebra.pssdemo_2019.Interfaces;

public interface HelpMessageCallback {
    void onSent();
    void onDelivered();
    void onReply(String reply);
    void onError();
}
