package com.example.android.project1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URLEncoder;

public class UserProfile extends ActionBarActivity {

    String SERVER_IP;
    TextView profileUserName,profileStatus1,profilePhoneNo1;
    String recievedUserName;
    String recievedPhoneNo,recievedStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        SERVER_IP = getServerIP();

        profileUserName=(TextView)findViewById(R.id.profileusername);
        profileStatus1=(TextView)findViewById(R.id.profieStatus);
        profilePhoneNo1=(TextView)findViewById(R.id.profilePhoneNo);


        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            recievedUserName= bundle.getString("username_key");
            setupActionBar();
            send_username_to_Server();
        }
    }

    private void setupActionBar() {
        getSupportActionBar().setTitle(recievedUserName);
        getSupportActionBar().setDisplayUseLogoEnabled(true); //Enable the Logo to be shown
        getSupportActionBar().setDisplayShowHomeEnabled(true); //Show the Logo
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Show the Up/Back arrow
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
    }

    private String getServerIP() {
        SharedPreferences tempPrefs = getSharedPreferences("com.example.android.project1.NetworkPreferences", 0);
        return tempPrefs.getString("SERVER_IP", getResources().getString(R.string.server_ip_address));
    }

    public void send_username_to_Server() {

        String URL = "http://"+SERVER_IP+":8080/MyFirstServlet/GetUserInfo?userName=" + URLEncoder.encode(recievedUserName);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("UserProfile", response);
                Gson gson = new Gson();
                Type type = new TypeToken<User>(){}.getType();
                User user = gson.fromJson(response, type);
                recievedStatus = user.getStatus();
                recievedPhoneNo = user.getPhoneNumber();
                profileUserName.setText(recievedUserName);
                profileStatus1.setText(recievedStatus);
                profilePhoneNo1.setText(recievedPhoneNo);
                getSupportActionBar().setTitle(user.getName());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY", error.toString());
            }
        });
        HttpConnector.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }
}
