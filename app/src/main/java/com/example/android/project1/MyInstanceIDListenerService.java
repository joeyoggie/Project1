package com.example.android.project1;

/**
 * Created by Joey on 11/9/2015.
 */

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MyInstanceIDListenerService extends IntentService {

    String SERVER_IP;

    private static final String TAG = "MyInstanceIDListenerService";
    private static final String[] TOPICS = {"global"};

    String userName;
    String name;
    String phoneNumber;
    String deviceID;

    public MyInstanceIDListenerService()
    {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //Get the passed info from the MainActivity.java when it called the MyInstanceIDListenerService.java service
        userName = intent.getStringExtra("userName");
        name = intent.getStringExtra("name");
        phoneNumber = intent.getStringExtra("phoneNumber");
        deviceID = intent.getStringExtra("deviceID");

        //Get a registration ID for this device and send it to the server along with the user-submitted info above
        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.i("MyInstanceIDListener", "GCM Registration Token: " + token);

            SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
            if(!prefs.getBoolean("isRegistered", false)){
                //Implement this method to send the registration info to your app's servers.
                sendRegistrationToServer(token);
            }

            // Subscribe to topic channels
            subscribeTopics(token);

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            //sharedPreferences.edit().putBoolean("isRegistered", true).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d("MyInstanceIDListener", "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            //sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        //Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
        //LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }


    //Send the registration info to our server
    private void sendRegistrationToServer(String token) {

        SharedPreferences tempPrefs = getSharedPreferences("com.example.android.project1.NetworkPreferences",0);
        SERVER_IP = tempPrefs.getString("SERVER_IP", getResources().getString(R.string.server_ip_address));

        //Send the info in a background thread
        //Instantiate the RequestQueue.
        String url = "http://"+SERVER_IP+":8080/MyFirstServlet/Register?userName="+ URLEncoder.encode(userName)+"&name="+URLEncoder.encode(name)+"&phoneNumber="+URLEncoder.encode(phoneNumber)+"&deviceID="+URLEncoder.encode(deviceID)+"&regID="+URLEncoder.encode(token);
        //Request a string response from the provided URL.
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                //Send the server response to Registration activity to dismiss the progress indicator and
                //display the response to the user in contentTextView textbox
                Intent intent = new Intent("registrationCompleteIntent");
                intent.putExtra("response", response);
                intent.putExtra("userName",userName);
                intent.putExtra("name",name);
                intent.putExtra("phoneNumber", phoneNumber);
                intent.putExtra("deviceID", deviceID);
                LocalBroadcastManager.getInstance(MyInstanceIDListenerService.this).sendBroadcast(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Send the server response to Registration activity to dismiss the progress indicator and
                //display the response to the user in contentTextView textbox
                Intent intent = new Intent("registrationCompleteIntent");
                intent.putExtra("response", "Volley ERROR");
                intent.putExtra("userName",userName);
                intent.putExtra("name",name);
                intent.putExtra("phoneNumber", phoneNumber);
                intent.putExtra("deviceID", deviceID);
                LocalBroadcastManager.getInstance(MyInstanceIDListenerService.this).sendBroadcast(intent);
            }
        });
        //Add the request to the RequestQueue.
        HttpConnector.getInstance(this).addToRequestQueue(request);
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]
}

