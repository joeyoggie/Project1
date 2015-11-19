package com.example.android.project1;

/**
 * Created by Joey on 11/9/2015.
 */
public class MessageData {
    private String message;
    //Use a DateTime object instead of a String
    private String time;
    //Determine whether message is sent or received (to align it left or right accordingly)
    private boolean sent;

    public MessageData(String message) {
        this.message = message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
}
