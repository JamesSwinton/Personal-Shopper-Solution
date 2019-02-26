package com.ses.zebra.pssdemo_2019.POJOs.Assistance;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HelpMessage {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    private MessageType sender;
    private String message, deviceSerialNumber;
    private String sentTime;

    public HelpMessage(MessageType sender, String message, long sentTime, String deviceSerialNumber) {
        this.sender = sender;
        this.message = message;
        this.sentTime = convertSentTime(sentTime);
        this.deviceSerialNumber = deviceSerialNumber;
    }

    public String getSentTime() {
        return sentTime;
    }

    public void setSentTime(long sentTime) {
        this.sentTime = convertSentTime(sentTime);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MessageType getSender() {
        return sender;
    }

    public void setSender(MessageType sender) {
        this.sender = sender;
    }

    public String getDeviceSerialNumber() {
        return deviceSerialNumber;
    }

    public void setDeviceSerialNumber(String deviceSerialNumber) {
        this.deviceSerialNumber = deviceSerialNumber;
    }

    private String convertSentTime(long sentTime) {
        return sdf.format(new Date(sentTime));
    }

    public enum MessageType { SENT, RECEIVED, SYSTEM }
}
