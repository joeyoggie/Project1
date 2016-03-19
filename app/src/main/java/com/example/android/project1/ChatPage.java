package com.example.android.project1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Joey on 11/9/2015.
 */
public class ChatPage extends ActionBarActivity {

    String SERVER_IP;

    String userName;
    String name;
    String phoneNumber;

    EditText enteredRecepient;
    String recepientName;
    static String recepientUserName;

    String mText;
    String timestamp;
    Button sendButton;

    ChatPageAdapter listAdapter;
    ListView listView;

    SQLiteDatabase messagesDB;
    SQLiteDatabase readableMessagesDB;
    SQLiteDatabase writableMessagesDB;
    DBMessagesHelper dbHelper;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_page);
        setupActionBar();
        SERVER_IP = getServerIP();
        getLocalUserInfo();

        enteredRecepient = (EditText) findViewById(R.id.entered_recepient);
        enteredRecepient.setVisibility(View.GONE);
        sendButton = (Button) findViewById(R.id.send_message_button);

        onNewIntent(getIntent());
        listView = (ListView) findViewById(R.id.list);

        dbHelper = DBMessagesHelper.getInstance(this);

        listAdapter = new ChatPageAdapter(this, cursor);
        listView.setAdapter(listAdapter);
        refreshCursor();

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshCursor();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("newMessageIntent"));
    }
    private void getLocalUserInfo(){
        SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
        userName = prefs.getString("userName","Me");
        name = prefs.getString("name", "Jon Doe");
        phoneNumber = prefs.getString("phoneNumber","0000000000");
    }

    private void refreshCursor(){
        cursor = DBMessagesHelper.readMessages(userName, recepientUserName);
        listAdapter.changeCursor(cursor);
    }

    private void setupActionBar() {
        getSupportActionBar().setTitle("RecepientNameHere");
        getSupportActionBar().setDisplayUseLogoEnabled(true); //Enable the Logo to be shown
        getSupportActionBar().setDisplayShowHomeEnabled(true); //Show the Logo
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Show the Up/Back arrow
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
    }

    private String getServerIP() {
        SharedPreferences tempPrefs = getSharedPreferences("com.example.android.project1.NetworkPreferences", 0);
        return tempPrefs.getString("SERVER_IP", getResources().getString(R.string.server_ip_address));
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Bundle extras = intent.getExtras();
        if(extras != null) {
            if(extras.containsKey("message")) {
                //setContentView(R.layout.activity_chat_page);
                //Extract the extra-data in the Notification
                String msg = extras.getString("message");
            }
            if(extras.containsKey("recepientName"))
            {
                recepientName = extras.getString("recepientName");
                getSupportActionBar().setTitle(recepientName);
            }
            if(extras.containsKey("recepientUserName"))
            {
                recepientUserName = extras.getString("recepientUserName");
                if(recepientName == null)
                {
                    getSupportActionBar().setTitle(recepientUserName);
                }
            }
            //also check for the received recepientProfilePicture as well and display it in the acionbar
        }
    }

    public void sendMessage(View view) {
        //Hide the keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        //Disable the sendButton temporarily

        EditText message = (EditText) findViewById(R.id.textInput);
        mText = message.getText().toString();
        if(mText.trim().length() == 0)
        {
            return;
        }
        message.setText("");

        //Get the unique device ID that will be stored in the database to uniquely identify this device
        SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences", 0);
        String deviceID = prefs.getString("deviceUUID","0");

        //Check if there's an internet connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();

        //If there's an internet connection
        if (netInfo != null && netInfo.isConnected()) {
            //Get the value of the textfields from the UI
            //recepientUserName = enteredRecepient.getText().toString();

            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
            timestamp = simpleDateFormat.format(date);
            Log.d("TIMESTAMP:", timestamp);

            //Send the message info to the server in a background thread
            //Instantiate the RequestQueue.
            String url = "http://"+SERVER_IP+":8080/MyFirstServlet/AddNewMessage?senderDeviceID=" + URLEncoder.encode(deviceID) + "&recepientUserName=" + URLEncoder.encode(recepientUserName) + "&message=" + URLEncoder.encode(mText)+"&timestamp="+URLEncoder.encode(timestamp);
            //Request a string response from the provided URL.
            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
                @Override
                public void onResponse(String response) {
                    //TODO: Add a flag marking the message as sent successfully
                    DBMessagesHelper.insertMessageIntoDB(userName, recepientUserName, mText, timestamp);
                    refreshCursor();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //TODO: Add a flag marking the message as not sent
                    DBMessagesHelper.insertMessageIntoDB(userName, recepientUserName, mText, timestamp);
                    refreshCursor();
                }
            });
            //Add the request to the RequestQueue.
            HttpConnector.getInstance(this).addToRequestQueue(request);
        }
    }
    public void sendScheduledMessage(View view)
    {
        DialogFragment newFragment = new PopupMessageDialog();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //dbHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat_page, menu);
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
            return true;
        }
        if(id == R.id.schedule_message_button)
        {
            DialogFragment newFragment = new PopupMessageDialog();
            newFragment.show(getSupportFragmentManager(), "timePicker");
        }
        return super.onOptionsItemSelected(item);
    }
}
