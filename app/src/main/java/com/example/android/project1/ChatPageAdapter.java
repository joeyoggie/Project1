package com.example.android.project1;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Joey on 2/21/2016.
 */
//TODO listview stutters, maybe compress images and find an alternative for toggling the imageview/textview
public class ChatPageAdapter extends CursorAdapter {

    Context context;
    Cursor cursor;

    String sender;
    String message;
    String timestamp;
    String userName;
    Bitmap image;
    String messageState;
    String messageType;

    SimpleDateFormat simpleDateFormat;
    SimpleDateFormat simpleDateFormatToDisplay;
    Date date;

    int senderColumnIndex;
    int messageColumnIndex;
    int timestampColumnIndex;
    int messageStateColumnIndex;
    int messageTypeColumnIndex;
    int imageBlobColumnIndex;

    public ChatPageAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        this.context = context;
        this.cursor = cursor;
        messageColumnIndex = cursor.getColumnIndexOrThrow(DBMessagesContract.MessageEntry.COLUMN_NAME_CONTENT);
        senderColumnIndex = cursor.getColumnIndexOrThrow(DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER);
        timestampColumnIndex = cursor.getColumnIndexOrThrow(DBMessagesContract.MessageEntry.COLUMN_NAME_TIME);
        messageStateColumnIndex = cursor.getColumnIndexOrThrow(DBMessagesContract.MessageEntry.COLUMN_NAME_MESSAGE_STATE);
        messageTypeColumnIndex = cursor.getColumnIndexOrThrow(DBMessagesContract.MessageEntry.COLUMN_NAME_MESSAGE_TYPE);
        imageBlobColumnIndex = cursor.getColumnIndexOrThrow(DBMessagesContract.MessageEntry.COLUMN_NAME_IMAGE_BLOB);

        SharedPreferences prefs = context.getSharedPreferences("com.example.android.project1.RegistrationPreferences", 0);
        userName = prefs.getString("userName","Me");
        simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        simpleDateFormatToDisplay = new SimpleDateFormat("h:mm a");
        simpleDateFormatToDisplay.setTimeZone(TimeZone.getDefault());
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent){
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.message_list, parent, false);

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.messageView = (RelativeLayout) view.findViewById(R.id.message_view);
        viewHolder.messageText = (TextView) view.findViewById(R.id.message_text);
        viewHolder.senderText = (TextView) view.findViewById(R.id.sender_username_box);
        viewHolder.timeBox = (TextView) view.findViewById(R.id.time_box);
        viewHolder.messageImage = (ImageView) view.findViewById(R.id.message_image);

        //set up the view parameters programmatically
        /*viewHolder.messageViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        viewHolder.messageViewParams.setMargins(4,4,4,4);
        viewHolder.messageView.setLayoutParams(viewHolder.messageViewParams);

        viewHolder.senderTextParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        viewHolder.senderTextParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        viewHolder.senderTextParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, R.id.message_view);
        viewHolder.senderText.setLayoutParams(viewHolder.senderTextParams);

        viewHolder.messageTextParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        //viewHolder.messageTextParams.setMargins(4,0,0,0);
        viewHolder.messageTextParams.addRule(RelativeLayout.BELOW, R.id.sender_username_box);
        viewHolder.messageTextParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
        viewHolder.messageTextParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        viewHolder.messageText.setLayoutParams(viewHolder.messageTextParams);

        viewHolder.timeBoxParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        //viewHolder.timeBoxParams.setMargins(4,4,0,0);
        viewHolder.timeBoxParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
        viewHolder.timeBoxParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        viewHolder.timeBoxParams.addRule(RelativeLayout.RIGHT_OF, R.id.message_text);
        viewHolder.timeBox.setLayoutParams(viewHolder.timeBoxParams);*/

        viewHolder.messageViewParams = (LinearLayout.LayoutParams) viewHolder.messageView.getLayoutParams();
        viewHolder.senderText.setVisibility(View.GONE);

        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor){

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        sender = cursor.getString(senderColumnIndex);
        timestamp = cursor.getString(timestampColumnIndex);
        messageState = cursor.getString(messageStateColumnIndex);

        //Check type of message, text or image
        messageType = cursor.getString(messageTypeColumnIndex);
        if(messageType.equals("text")){
            viewHolder.messageText.setVisibility(View.VISIBLE);
            message = cursor.getString(messageColumnIndex);
            if(message != null){
                viewHolder.messageText.setText(message);
            }
            else{
                viewHolder.messageText.setText("null message");
            }
            viewHolder.messageImage.setVisibility(View.INVISIBLE);
        }
        else if(messageType.equals("image")){
            viewHolder.messageImage.setVisibility(View.VISIBLE);
            viewHolder.messageText.setText("Loading picture...");
            byte[] imageByteArray = cursor.getBlob(imageBlobColumnIndex);
            image = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
            viewHolder.messageImage.setImageBitmap(image);
            viewHolder.messageText.setVisibility(View.INVISIBLE);
        }

        viewHolder.senderText.setText(sender);

        if(userName.equals(sender)) {
            viewHolder.messageViewParams.gravity = Gravity.RIGHT;

            //viewHolder.senderText.setVisibility(View.GONE);
        }
        else {
            viewHolder.messageViewParams.gravity = Gravity.LEFT;

            //viewHolder.senderText.setVisibility(View.VISIBLE);
        }

        if(messageState.equals("unsent")){
            viewHolder.messageText.setTextColor(Color.LTGRAY);
            //TODO Mark viewHolder.messageImage as grey or something
        }
        else{
            viewHolder.messageText.setTextColor(Color.BLACK);
            //TODO Leave the viewHolder.messageImage as it normally should be
        }

        if(timestamp != null){
            try {
                date = simpleDateFormat.parse(timestamp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            timestamp = simpleDateFormatToDisplay.format(date);
            viewHolder.timeBox.setText(timestamp);
        }
        else {
            viewHolder.timeBox.setText("null time");
        }
    }

    static class ViewHolder {
        RelativeLayout messageView;
        TextView messageText;
        TextView senderText;
        TextView timeBox;
        ImageView messageImage;
        LinearLayout.LayoutParams messageViewParams;
        /*RelativeLayout.LayoutParams timeBoxParams;
        RelativeLayout.LayoutParams messageTextParams;
        RelativeLayout.LayoutParams senderTextParams;*/
    }
}
