package com.example.android.project1;

/**
 * Created by Joey on 11/9/2015.
 */
public class MessageData {
    private String senderDeviceID;
    private String recepientUserName;
    private String message;
    private String timestamp;
    //Determine whether message is sent or received (to align it left or right accordingly)
    private boolean sent;

    public MessageData(String message) {
        this.message = message;
    }
    public MessageData()
    {
        this.senderDeviceID = null;
        this.recepientUserName = null;
        this.message = null;
        this.timestamp = null;
        this.sent = false;
    }
    public MessageData(String senderDeviceID, String recepientUserName, String message, String timestamp, boolean sent)
    {
        this.senderDeviceID = senderDeviceID;
        this.recepientUserName = recepientUserName;
        this.message = message;
        this.timestamp = timestamp;
        this.sent = sent;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }

    public void setSenderDeviceID(String senderDeviceID) {
        this.senderDeviceID = senderDeviceID;
    }
    public String getSenderDeviceID() {
        return senderDeviceID;
    }

    public void setRecepientUserName(String recepientUserName) {
        this.recepientUserName = recepientUserName;
    }
    public String getRecepientUserName() {
        return recepientUserName;
    }

    public boolean isSent() {
        return sent;
    }
    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public String getTimeOfMessage() {
        return timestamp;
    }
    public void setTimeOfMessage(String timestamp) {
        this.timestamp = timestamp;
    }
}
