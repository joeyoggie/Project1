package com.example.android.project1;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.UUID;


public class Registration extends ActionBarActivity {

    String SERVER_IP;
    TextView contentTextView;

    EditText enteredUserName;
    EditText enteredName;
    EditText enteredPhoneNumber;
    TextView userNameErrorTextView;
    TextView nameErrorTextView;
    TextView phoneNumberErrorTextView;
    String deviceID;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        contentTextView = (TextView) findViewById(R.id.content_text_view);

        enteredUserName = (EditText) findViewById(R.id.entered_username);
        enteredName = (EditText) findViewById(R.id.entered_name);
        enteredPhoneNumber = (EditText) findViewById(R.id.entered_phone_number);

        userNameErrorTextView = (TextView) findViewById(R.id.userName_error_message);
        nameErrorTextView = (TextView) findViewById(R.id.name_error_message);
        phoneNumberErrorTextView = (TextView) findViewById(R.id.phone_number_error_message);
        userNameErrorTextView.setVisibility(View.GONE);
        nameErrorTextView.setVisibility(View.GONE);
        phoneNumberErrorTextView.setVisibility(View.GONE);

        SERVER_IP = getServerIP();
        deviceID = getDeviceID();

        //BroadcastReceiver that will be waiting for calls from the onPostExecute() method in MyInstanceIDListenerService.java
        //to dismiss the progress indicator and update the contentTextView textbox with the response
        //from the server after sending the registration info to the server
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
                String response = intent.getStringExtra("response");
                if(dialog != null) {dialog.dismiss();}
                if(response!=null && response.contains("successfully"))
                {
                    //Save a boolean file indicating that the registration was successful, to prevent the Registration.java activity
                    //from being re-launched at app launch again
                    SharedPreferences.Editor prefsEditor = prefs.edit();
                    prefsEditor.putBoolean("isRegistered", true);
                    //Save the registered info as well
                    prefsEditor.putString("userName",intent.getStringExtra("userName"));
                    prefsEditor.putString("name",intent.getStringExtra("name"));
                    prefsEditor.putString("phoneNumber", intent.getStringExtra("phoneNumber"));
                    prefsEditor.apply();
                    contentTextView.setText("Registration successful! Click next to continue.");
                    contentTextView.setTextColor(getResources().getColor(R.color.colorAccent));
                    Button finishRegistration = (Button) findViewById(R.id.finish_registration_button);
                    finishRegistration.setVisibility(View.VISIBLE);
                    Button submitInfo = (Button) findViewById(R.id.submit_info_button);
                    submitInfo.setVisibility(View.GONE);
                }
                else if(response!=null && response.contains("phoneNumber"))
                {
                    //Save a boolean file indicating that the registration was unsuccessful, so that the Registration.java activity
                    //is re-launched at next app-launch
                    SharedPreferences.Editor prefsEditor = prefs.edit();
                    prefsEditor.putBoolean("isRegistered", false);
                    prefsEditor.apply();
                    contentTextView.setText(response);
                    contentTextView.setTextColor(Color.RED);
                }
                else if(response!=null && response.contains("userName"))
                {
                    //Save a boolean file indicating that the registration was unsuccessful, so that the Registration.java activity
                    //is re-launched at next app-launch
                    SharedPreferences.Editor prefsEditor = prefs.edit();
                    prefsEditor.putBoolean("isRegistered", false);
                    prefsEditor.apply();
                    contentTextView.setText(response);
                    contentTextView.setTextColor(Color.RED);
                }
                else
                {
                    contentTextView.setText("Couldn't connect to server. Please try again later.");
                    contentTextView.setTextColor(Color.RED);
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("registrationCompleteIntent"));
    }

    public String getServerIP(){
        SharedPreferences tempPrefs = getSharedPreferences("com.example.android.project1.NetworkPreferences",0);
        return tempPrefs.getString("SERVER_IP", getResources().getString(R.string.server_ip_address));
    }

    private String getDeviceID(){
        //Get a unique device ID (IMEI, Secure.ANDROID_ID or a randomly-generated UUID) that will be stored
        //in the server database to uniquely identify this device
        if(!(Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID).equals("9774d56d682e549c"))) {
            //Use the 64-bit Android_ID (changed on factory resets)
            deviceID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
            SharedPreferences.Editor prefsEditor = prefs.edit();
            prefsEditor.putString("deviceUUID", deviceID);
            prefsEditor.apply();
            Log.d("Registration","Device ID: "+deviceID);
            return deviceID;
        }
        else {
            TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
            if(tm.getDeviceId() != null && !tm.getDeviceId().equals("000000000000000")) {
                //Use the IMEI number (persistent through factory resets)
                deviceID = tm.getDeviceId();
                SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
                SharedPreferences.Editor prefsEditor = prefs.edit();
                prefsEditor.putString("deviceUUID", deviceID);
                prefsEditor.apply();
                Log.d("Registration","Device ID: "+deviceID);
                return deviceID;
            }
            else {
                //Use a randomly-generated UUID and save it on the device for later usage (changed on app re-installation)
                deviceID = UUID.randomUUID().toString();
                SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
                SharedPreferences.Editor prefsEditor = prefs.edit();
                prefsEditor.putString("deviceUUID", deviceID);
                prefsEditor.apply();
                Log.d("Registration", "Device ID: " + deviceID);
                return deviceID;
            }
        }
    }

    //Submit the info to the server (register a new device)
    public void submitInfo(View view) {
        //Hide the keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        //Check for internet connectivity first
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();

        //A boolean variable to indicate if there's an error in any submitted info and prevent sending them to the server
        boolean error = false;

        //If there's internet connection
        if (netInfo != null && netInfo.isConnected()) {
            //Get the info from the UI (textboxes)
            String userName = enteredUserName.getText().toString();
            String name = enteredName.getText().toString();
            String phoneNumber = enteredPhoneNumber.getText().toString();

            //Check the user-submitted data to make sure it's in the correct format
            if(userName.trim().length() == 0)
            {
                userNameErrorTextView.setText("Please enter a username!");
                userNameErrorTextView.setVisibility(View.VISIBLE);
                error = true;
            }
            else if(userName==null || userName.contains("\n") || userName.contains(" "))
            {
                userNameErrorTextView.setText("Please enter a valid username");
                userNameErrorTextView.setVisibility(View.VISIBLE);
                error = true;
            }
            else
            {
                userNameErrorTextView.setVisibility(View.GONE);
            }

            if(name.trim().length() == 0)
            {
                nameErrorTextView.setText("Please enter a name!");
                nameErrorTextView.setVisibility(View.VISIBLE);
                error = true;
            }
            else
            {
                nameErrorTextView.setVisibility(View.GONE);
            }

            if(phoneNumber.trim().length() == 0)
            {
                phoneNumberErrorTextView.setText("Please enter a phone number!");
                phoneNumberErrorTextView.setVisibility(View.VISIBLE);
                error = true;
            }
            else if(phoneNumber.contains(" "))
            {
                phoneNumberErrorTextView.setText("Please enter a valid phone number");
                phoneNumberErrorTextView.setVisibility(View.VISIBLE);
                error = true;
            }
            else
            {
                phoneNumberErrorTextView.setVisibility(View.GONE);
            }

            if(error)
            {
                return;
            }

            SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences", 0);
            deviceID = prefs.getString("deviceUUID","0");

            //Display a progress circle indicator
            dialog = new ProgressDialog(Registration.this);
            dialog.setMessage("Sending registration info to server...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            //Pass the info to the MyInstanceIDListenerService.java service that will send the info to the server along
            //with the registration ID token
            Intent reg = new Intent(this, MyInstanceIDListenerService.class);
            reg.putExtra("userName", userName);
            reg.putExtra("name", name);
            reg.putExtra("phoneNumber", phoneNumber);
            reg.putExtra("deviceID", deviceID);
            startService(reg);
        }
        //If there's no internet connection
        else {
            contentTextView.setText("No internet connection!");
            contentTextView.setTextColor(Color.RED);
        }
    }

    //Get the user info from the server (using the device ID)
    public void getInfo(View view) {
        //Check for internet connectivity first
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();

        SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences", 0);
        deviceID = prefs.getString("deviceUUID","0");

        //If there's an internet connection
        if (netInfo != null && netInfo.isConnected()) {
            Log.d("Registration", "IP Address: "+SERVER_IP);

            //Display a progress circle indicator
            final ProgressDialog dialog;
            dialog = new ProgressDialog(Registration.this);
            dialog.setMessage("Getting user info from server...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            //Get the info from the server in a background thread
            //Instantiate the RequestQueue.
            //RequestQueue queue = HttpConnector.getInstance(this.getApplicationContext()).getRequestQueue();
            String url = SERVER_IP + "/MyFirstServlet/GetInfo?deviceID=" + deviceID;
            HttpsTrustManager.allowAllSSL();
            //Request a string response from the provided URL.
            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
                @Override
                public void onResponse(String response) {
                    //Update the contentTextView with the response from the server
                    contentTextView.setText("Server response is: "+ response);
                    contentTextView.setTextColor(Color.BLACK);
                    dialog.dismiss();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //Update the contentTextView, indicating a failure in connection
                    contentTextView.setText("Couldn't connect to server. Please try again later.");
                    contentTextView.setTextColor(Color.RED);
                    dialog.dismiss();
                }
            });
            //Add the request to the RequestQueue.
            HttpConnector.getInstance(this).addToRequestQueue(request);
            //queue.add(request);
        }
        //If there's no internet connection
        else {
            contentTextView.setText("No internet connection!");
            contentTextView.setTextColor(Color.RED);
        }
    }

    public void goToLocationServicesRegistrationPrompt(View view)
    {
        Intent intent = new Intent(this, LocationServicesRegistrationPrompt.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_registration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, com.example.android.project1.Settings.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
