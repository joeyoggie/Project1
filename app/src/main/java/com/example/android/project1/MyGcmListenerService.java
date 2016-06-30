package com.example.android.project1;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

        if(data.containsKey("message")) {
            //Get the message data that was sent from the GCM server
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

            //Store the message that was received in a local SQLite database
            DBMessagesHelper.getInstance(getApplicationContext());
            DBMessagesHelper.insertMessageIntoDB(sender, recepient, message, timestamp, "received");
            //Refresh the ChatPage's listview, in case it was already visible
            refreshListView();

            //Show a notification, only if the activity is not visible
            SharedPreferences prefs  = getSharedPreferences("com.example.android.project1.ChatPageState",0);
            Log.d("MyGcmListenerService", "ChatPage visibility is " + prefs.getBoolean("isVisible", false));
            if(!prefs.getBoolean("isVisible", false)){
                sendNotification(sender, message);
            }
        }
        else if (data.containsKey("typingStatus")){
            String receivedTypingStatusFromServer = data.getString("typingStatus");
            String senderUserName = data.getString("senderUserName");
            Log.d("MyGcmListenerService", senderUserName);
            Log.d("MyGcmListenerService", receivedTypingStatusFromServer);
            forwardTypingStatus(senderUserName, receivedTypingStatusFromServer);
        }

    }

    private void refreshListView()
    {
        Intent intent = new Intent("newMessageIntent");
        LocalBroadcastManager.getInstance(MyGcmListenerService.this).sendBroadcast(intent);
    }

    private void forwardTypingStatus(String sender, String typingStatus){
        Intent intent = new Intent("newTypingIntent");
        intent.putExtra("receivedTypingStatusFromServer_key", typingStatus);
        intent.putExtra("senderUserName",sender);
        LocalBroadcastManager.getInstance(MyGcmListenerService.this).sendBroadcast(intent);
    }

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
}
