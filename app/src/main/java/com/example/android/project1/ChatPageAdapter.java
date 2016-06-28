package com.example.android.project1;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Joey on 2/21/2016.
 */
public class ChatPageAdapter extends CursorAdapter {

    Context context;
    Cursor cursor;

    String sender;
    String message;
    String timestamp;
    String userName;
    Bitmap image;
    String messageState;

    SimpleDateFormat simpleDateFormat;
    SimpleDateFormat simpleDateFormatToDisplay;
    Date date;

    int senderColumnIndex;
    int messageColumnIndex;
    int timestampColumnIndex;
    int messageStateColumnIndex;

    public ChatPageAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        this.context = context;
        this.cursor = cursor;
        messageColumnIndex = cursor.getColumnIndexOrThrow(DBMessagesContract.MessageEntry.COLUMN_NAME_CONTENT);
        senderColumnIndex = cursor.getColumnIndexOrThrow(DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER);
        timestampColumnIndex = cursor.getColumnIndexOrThrow(DBMessagesContract.MessageEntry.COLUMN_NAME_TIME);
        messageStateColumnIndex = cursor.getColumnIndexOrThrow(DBMessagesContract.MessageEntry.COLUMN_NAME_MESSAGE_STATE);
        SharedPreferences prefs = context.getSharedPreferences("com.example.android.project1.RegistrationPreferences", 0);
        userName = prefs.getString("userName","Me");
        simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        simpleDateFormatToDisplay = new SimpleDateFormat("h:mm a");
        simpleDateFormatToDisplay.setTimeZone(TimeZone.getDefault());
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor){

        TextView messageText = (TextView) view.findViewById(R.id.message_text);
        TextView senderText = (TextView) view.findViewById(R.id.sender_username_box);
        TextView timeBox = (TextView) view.findViewById(R.id.time_box);


        message = cursor.getString(messageColumnIndex);
        sender = cursor.getString(senderColumnIndex);
        timestamp = cursor.getString(timestampColumnIndex);
        messageState = cursor.getString(messageStateColumnIndex);

        if(message != null){
            messageText.setText(message);
        }
        else{
            messageText.setText("null message");
        }

        senderText.setText(sender);

        if(userName.equals(sender)) {
            messageText.setGravity(Gravity.START);
            //messageText.setTextColor(Color.GREEN); //use colors temporarily, should be aligned only
        }
        else {
            messageText.setGravity(Gravity.END);
            //messageText.setTextColor(Color.RED); //use colors temporarily, should be aligned only
        }

        if(messageState.equals("unsent")){
            messageText.setTextColor(Color.RED);
        }
        else{
            messageText.setTextColor(Color.BLACK);
        }

        if(timestamp != null){
            try {
                date = simpleDateFormat.parse(timestamp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            timestamp = simpleDateFormatToDisplay.format(date);
            timeBox.setText(timestamp);
        }
        else {
            timeBox.setText("null time");
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent){
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.message_list, parent, false);
        return view;
    }
/*
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.message_list, parent, false);
            messageText = (TextView) convertView.findViewById(R.id.message_text);
        }
        return convertView;
    }
*/
}
