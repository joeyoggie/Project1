package com.example.android.project1;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public ChatPageAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        this.context = context;
        this.cursor = cursor;
        SharedPreferences prefs = context.getSharedPreferences("com.example.android.project1.RegistrationPreferences", 0);
        userName = prefs.getString("userName","Me");

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor){
        message = cursor.getString(cursor.getColumnIndexOrThrow(DBMessagesContract.MessageEntry.COLUMN_NAME_CONTENT));
        sender = cursor.getString(cursor.getColumnIndexOrThrow(DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER));
        timestamp = cursor.getString(cursor.getColumnIndexOrThrow(DBMessagesContract.MessageEntry.COLUMN_NAME_TIME));

        TextView messageText = (TextView) view.findViewById(R.id.message_text);
        TextView senderText = (TextView) view.findViewById(R.id.sender_username_box);
        TextView timeBox = (TextView) view.findViewById(R.id.time_box);

        messageText.setText(message);
        senderText.setText(sender);
        if(userName.equals(sender))
        {
            messageText.setGravity(Gravity.START);
            //messageText.setTextColor(Color.GREEN); //use colors temporarily, should be aligned only
        }
        else
        {
            messageText.setGravity(Gravity.END);
            //messageText.setTextColor(Color.RED); //use colors temporarily, should be aligned only
        }
        SimpleDateFormat simpleDateFormatToDisplay = new SimpleDateFormat("h:mm a");
        SimpleDateFormat simpleDateFormatInDB = new SimpleDateFormat("dd/MM/yy-HH:mm:ss");
        Date date = null;
        try {
            date = simpleDateFormatInDB.parse(timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        timestamp = simpleDateFormatToDisplay.format(date);
        timeBox.setText(timestamp);
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
