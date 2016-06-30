package com.example.android.project1;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.instabug.library.IBGInvocationEvent;
import com.instabug.library.Instabug;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


public class MainPage extends ActionBarActivity {

    String SERVER_IP;

    String recepientUserName;
    String recepientName;

    String userName;
    String name;
    String phoneNumber;
    String deviceID;

    DrawerLayout mDrawerLayout;
    View navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        setupActionBar();
        getLocalUserInfo();

        SERVER_IP = getServerIP();

        SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
        boolean registered = prefs.getBoolean("isRegistered", false);
        if(!registered)
        {
            Intent reg = new Intent(this, Registration.class);
            startActivity(reg);
            finish();
        }

        //Drawlayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.drawerPane);

        TextView nameTextView = (TextView) findViewById(R.id.name);
        TextView userNameTextView = (TextView) findViewById(R.id.user_name);
        TextView phoneNumberTextView = (TextView) findViewById(R.id.phone_number);
        ImageView profilePictureImageView = (ImageView) findViewById(R.id.personal_profile_picture);
        nameTextView.setText(name);
        userNameTextView.setText("@"+userName);
        phoneNumberTextView.setText(phoneNumber);
        //profilePictureImageView.setImageBitmap(bitmap);

        //Initialize Instabug
        new Instabug.Builder(getApplication(), "2cf1f7a67f638f61eb13b0fc6e619bca")
                .setInvocationEvent(IBGInvocationEvent.IBGInvocationEventShake)
                .build();
    }

    private String getServerIP() {
        SharedPreferences tempPrefs = getSharedPreferences("com.example.android.project1.NetworkPreferences", 0);
        return tempPrefs.getString("SERVER_IP", getResources().getString(R.string.server_ip_address));
    }

    private void setupActionBar() {
        getSupportActionBar().setDisplayUseLogoEnabled(true); //Enable the Logo to be shown
        getSupportActionBar().setDisplayShowHomeEnabled(true); //Show the Logo
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Show the Up/Back arrow
        getSupportActionBar().setLogo(R.mipmap.ic_launcher); //Specify the logo image
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer); //Specify the Up/Back arrow image
    }

    private void getLocalUserInfo(){
        SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
        userName = prefs.getString("userName","Me");
        name = prefs.getString("name", "Jon Doe");
        phoneNumber = prefs.getString("phoneNumber","0000000000");
        //Get the unique device ID that will be stored in the database to uniquely identify this device
        deviceID = prefs.getString("deviceUUID","0");
    }

    public void goToPrivacySettings(View view)
    {
        Intent intent = new Intent(this, PrivacySettings.class);
        startActivity(intent);
        if (mDrawerLayout.isDrawerOpen(navigationView)) {
            mDrawerLayout.closeDrawer(navigationView);
        }
    }

    public void goToLocationServicesRegistrationPrompt(View view)
    {
        SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);

        if(prefs.getString("locationServicesRegistration", "notRegistered").equals("registered")) {
            Intent locationServicesRegistrationIntent = new Intent(this, LocationServicesRegistration.class);
            startActivity(locationServicesRegistrationIntent);
        }
        else{
            Intent locationServicesRegistrationPromptIntent = new Intent(this, LocationServicesRegistrationPrompt.class);
            startActivity(locationServicesRegistrationPromptIntent);
        }

        if (mDrawerLayout.isDrawerOpen(navigationView)) {
            mDrawerLayout.closeDrawer(navigationView);
        }
    }

    public void goToChatPage(View view) {
        EditText customRecepientUserName = (EditText) findViewById(R.id.custom_recepient_username);
        EditText customRecepientName = (EditText) findViewById(R.id.custom_recepient_name);

        recepientName = customRecepientName.getText().toString(); //get the name of the clicked contact
        recepientUserName = customRecepientUserName.getText().toString().trim(); //get the username of the clicked contact
        Intent intent = new Intent(this, ChatPage.class);
        intent.putExtra("recepientName",recepientName);
        intent.putExtra("recepientUserName",recepientUserName);
        startActivity(intent);
    }

    public void newMessageContactListView(View view){
        Intent intent = new Intent(this, NewMessageContactsListView.class);
        startActivity(intent);
    }


    public void goToRegistration(View view)
    {
        Intent intent = new Intent(this, Registration.class);
        startActivity(intent);
        if (mDrawerLayout.isDrawerOpen(navigationView)) {
            mDrawerLayout.closeDrawer(navigationView);
        }
    }

    public void goToSettings(View view)
    {
        Toast.makeText(MainPage.this, "No settings to change yet. Coming soon!", Toast.LENGTH_SHORT).show();

        /*Intent intent = new Intent(this, Settings.class);
        startActivity(intent);*/
        if (mDrawerLayout.isDrawerOpen(navigationView)) {
            mDrawerLayout.closeDrawer(navigationView);
        }
    }

    public void navigateToStatusFragment(View view){
        DialogFragment newFragment = new StatusFragment();
        newFragment.show(getFragmentManager(),"statusFragment");
        if (mDrawerLayout.isDrawerOpen(navigationView)) {
            mDrawerLayout.closeDrawer(navigationView);
        }
    }

    public void toggleDontDisturbMode(View view){
        Toast.makeText(MainPage.this, "This feature is not implemented yet..", Toast.LENGTH_SHORT).show();
        if (mDrawerLayout.isDrawerOpen(navigationView)) {
            mDrawerLayout.closeDrawer(navigationView);
        }
    }

    public void closeFriendsList(View view){
        Toast.makeText(MainPage.this, "This feature is not implemented yet..", Toast.LENGTH_SHORT).show();
        if (mDrawerLayout.isDrawerOpen(navigationView)) {
            mDrawerLayout.closeDrawer(navigationView);
        }
    }

    private void sendOnlineStateToServer(final String state){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String timestamp = simpleDateFormat.format(date);

        String url = "http://"+SERVER_IP+":8080/MyFirstServlet/State_change";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response!=null && response.length() >= 0) {
                    Log.d("VolleyResponse", response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error!=null && error.toString().length() >= 0) {
                    Log.d("VolleyError", error.toString());
                }
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("userName", userName);
                params.put("state", state);
                params.put("timestamp", timestamp);

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };
        //Add the request to the RequestQueue.
        HttpConnector.getInstance(this).addToRequestQueue(stringRequest);
    }

    @Override
    public void onBackPressed(){
        if (mDrawerLayout.isDrawerOpen(navigationView)) {
            mDrawerLayout.closeDrawer(navigationView);
        }
        else{
            MainPage.super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        //Send 'offline' state to server
        sendOnlineStateToServer("offline");
        super.onDestroy();
    }

    @Override
    public void onStop()
    {
        //Send 'offline' state to server
        sendOnlineStateToServer("offline");
        super.onStop();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        //Send 'online' state to server
        sendOnlineStateToServer("online");
    }

    /*@Override
    public void onPause()
    {
        //Send 'offline' state to server
        sendOnlineStateToServer("offline");
        super.onPause();
    }*/

    /*@Override
    public void onResume()
    {
        super.onResume();
        //Send 'online' state to server
        sendOnlineStateToServer("online");
    }*/


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_page, menu);
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
            Toast.makeText(MainPage.this, "No settings to change yet. Coming soon!", Toast.LENGTH_SHORT).show();
            /*Intent intent = new Intent(this, Settings.class);
            startActivity(intent);*/
            return true;
        }

        /*if(id == R.id.action_alt_profile)
        {
            Intent intent = new Intent(this, MyProfile.class);
            startActivity(intent);
            return true;
        }*/

        if(id == R.id.location_services)
        {
            Intent intent = new Intent(this, LocationServices.class);
            startActivity(intent);
            return true;
        }

        if(id == android.R.id.home)
        {
            //when clicked
            if (mDrawerLayout.isDrawerOpen(navigationView)) {
                mDrawerLayout.closeDrawer(navigationView);
            } else {
                mDrawerLayout.openDrawer(navigationView);
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
