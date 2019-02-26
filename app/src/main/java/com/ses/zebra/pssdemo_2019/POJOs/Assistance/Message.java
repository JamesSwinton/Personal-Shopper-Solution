package com.ses.zebra.pssdemo_2019.POJOs.Assistance;

public class Message {

    public enum MessageType { SENT, RECEIVED }

    private String message;
    private MessageType sender;

    public Message(String message, MessageType sender) {
        this.message = message;
        this.sender = sender;
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
}
