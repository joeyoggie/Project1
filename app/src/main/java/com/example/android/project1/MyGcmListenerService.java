package com.example.android.project1;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by Joey on 11/9/2015.
 */
public class MyGcmListenerService extends GcmListenerService {

    public MyGcmListenerService() {
    }

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        //Get the data  that was sent from the GCM server
        String message = data.getString("message");
        String sender = data.getString("sender");
        String timestamp = data.getString("timestamp");
        String recepient = data.getString("recepient");

        Log.d("MyGcmListenerService", "From: " + from);
        Log.d("MyGcmListenerService", "Sender: " + sender);
        Log.d("MyGcmListenerService", "Recepient: " + recepient);
        Log.d("MyGcmListenerService", "Message: " + message);
        Log.d("MyGcmListenerService", "Timestamp: " + timestamp);

        if (from.startsWith("/topics/")) {
            //message received from some topic.
        } else {
            //normal downstream message.
        }

        //Pass the message that was received to the ListView
        //refreshListView(message);
        //Store the message that was received in a local SQLite database
        //new backgroundDBHelperInsertMessages().execute(new String[]{sender, recepient, message, timestamp});
        DBMessagesHelper.getInstance(getApplicationContext());
        DBMessagesHelper.insertMessageIntoDB(sender, recepient, message,timestamp);
        notifyListView();
        //Show a notification
        sendNotification(sender, message);
    }

    private void notifyListView()
    {
        Intent intent = new Intent("newMessageIntent");
        LocalBroadcastManager.getInstance(MyGcmListenerService.this).sendBroadcast(intent);
    }

//    private void refreshListView(String message)
//    {
//        Intent intent = new Intent("newMessageIntent");
//        intent.putExtra("message", message);
//        LocalBroadcastManager.getInstance(MyGcmListenerService.this).sendBroadcast(intent);
//    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String sender, String message) {
        //Create a pending intent that will launch when the notification is pressed
        Intent intent = new Intent(this, ChatPage.class);
        Bundle extras = new Bundle();
        extras.putString("message", message);
        extras.putString("recepientUserName", sender);
        intent.putExtras(extras);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.powered_by_google_dark)
                .setContentTitle("Message from "+sender)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

//    //AsyncTask that will perform DB operations in a background thread
//    public class backgroundDBHelperInsertMessages extends AsyncTask<String[], Void, Void> {
//        DBMessagesHelper dbHelper;
//        SQLiteDatabase messagesDB;
//
//        protected Void doInBackground(String[]... params) {
//            dbHelper = DBMessagesHelper.getInstance(getApplicationContext());
//            messagesDB = dbHelper.getWritableDatabase();
//            ContentValues values = new ContentValues();
//            values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_ID,1);
//            values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER,params[0][0]);
//            values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_RECEPIENT,params[0][1]);
//            values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_CONTENT, params[0][2]);
//            values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_TIME,params[0][3]);
//            long newRowId;
//            newRowId = messagesDB.insert(DBMessagesContract.MessageEntry.TABLE_NAME,null,values);
//            return null;
//        }
//        protected void onPostExecute(Void v)
//        {
//            refreshListView("message");
//        }
//    }
}
