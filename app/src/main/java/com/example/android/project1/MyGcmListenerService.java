package com.example.android.project1;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by Joey on 11/9/2015.
 */
public class MyGcmListenerService extends GcmListenerService {

    public MyGcmListenerService() {
    }

    String SERVER_IP;
    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {

        SERVER_IP = getServerIP();
        if(data.containsKey("message")) {
            //Get the message data that was sent from the GCM server
            String message = data.getString("message");
            String sender = data.getString("sender");
            String timestamp = data.getString("timestamp");
            String recepient = data.getString("recepient");

            Log.d("MyGcmListenerService", "Received a new message!");
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
            forwardTypingStatus(senderUserName, receivedTypingStatusFromServer);
        }
        else if (data.containsKey("imageID")){
            //Get the image data that was sent from the GCM server
            String imageID = data.getString("imageID");
            String sender = data.getString("senderUserName");
            String timestamp = data.getString("timestamp");
            String recepient = data.getString("recepientUserName");

            Log.d("MyGcmListenerService", "Received a new image!");
            Log.d("MyGcmListenerService", "From: " + from);
            Log.d("MyGcmListenerService", "Sender: " + sender);
            Log.d("MyGcmListenerService", "Recepient: " + recepient);
            Log.d("MyGcmListenerService", "ImageID: " + imageID);
            Log.d("MyGcmListenerService", "Timestamp: " + timestamp);

            //Store the image that was received in a local SQLite database
            ImageInfo imageInfoObject = new ImageInfo();
            imageInfoObject.setSenderUserName(sender);
            imageInfoObject.setRecepientUserName(recepient);
            imageInfoObject.setImageID(Long.parseLong(imageID));
            imageInfoObject.setTimestamp(timestamp);
            downloadImage(imageInfoObject);

            //Show a notification, only if the activity is not visible
            SharedPreferences prefs  = getSharedPreferences("com.example.android.project1.ChatPageState",0);
            Log.d("MyGcmListenerService", "ChatPage visibility is " + prefs.getBoolean("isVisible", false));
            if(!prefs.getBoolean("isVisible", false)){
                sendImageNotification(sender, imageID);
            }
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

    private String getServerIP() {
        SharedPreferences tempPrefs = getSharedPreferences("com.example.android.project1.NetworkPreferences", 0);
        return tempPrefs.getString("SERVER_IP", getResources().getString(R.string.server_ip_address));
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
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //also removed launchMode=singleInstance from manifest
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

    private void sendImageNotification(String sender, String message) {
        //Create a pending intent that will launch when the notification is pressed
        Intent intent = new Intent(this, ChatPage.class);
        Bundle extras = new Bundle();
        extras.putString("imageID", message);
        extras.putString("recepientUserName", sender);
        intent.putExtras(extras);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.powered_by_google_dark)
                .setContentTitle("New image from "+sender)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    public void downloadImage(ImageInfo imageInfoObject){
        ImageDownloader dImage = new ImageDownloader();
        dImage.execute(imageInfoObject);
    }

    private class ImageDownloader extends AsyncTask<ImageInfo, Void, ImageInfo> {

        @Override
        protected ImageInfo doInBackground(ImageInfo... imgObject){
            byte[] imageData = null;
            ImageInfo imageInfoObject = new ImageInfo();
            String urlString = "http://"+SERVER_IP+":8080/MyFirstServlet/GetImage?imageID=" + imgObject[0].getImageID();
            try {
                /*HttpURLConnection conn;
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream inputStream = conn.getInputStream();
                *//*int maxLength = conn.getContentLength();
                imageData = new byte[maxLength];
                int bytesRead = 0;
                while(bytesRead < maxLength){
                    int n = inputStream.read(imageData, bytesRead, maxLength - bytesRead);
                    bytesRead += n;
                }*//*

                *//*ByteArrayInputStream baos = new ByteArrayInputStream(imageData);
                inputStream.read(baos);*//*

                inputStream.close();*/
                Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(urlString).getContent());

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                imageData = bos.toByteArray();

                imageInfoObject.setSenderUserName(imgObject[0].getSenderUserName());
                imageInfoObject.setRecepientUserName(imgObject[0].getRecepientUserName());
                imageInfoObject.setImageID(imgObject[0].getImageID());
                imageInfoObject.setTimestamp(imgObject[0].getTimestamp());
                imageInfoObject.setImageData(imageData);

                //imageData = BitmapFactory.decodeStream((InputStream)new URL(urlString[0]).getContent());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return imageInfoObject;
        }

        @Override
        protected void onPostExecute(ImageInfo imageInfoObject){
            DBMessagesHelper.getInstance(getApplicationContext());
            DBMessagesHelper.insertImageIntoDB(imageInfoObject.getSenderUserName(),
                    imageInfoObject.getRecepientUserName(),
                    String.valueOf(imageInfoObject.getImageID()),
                    imageInfoObject.getImageData(),
                    imageInfoObject.getTimestamp(),
                    "received");
            //Refresh the ChatPage's listview, in case it was already visible
            refreshListView();
        }
    }
}
