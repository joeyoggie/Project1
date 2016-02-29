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
import android.os.AsyncTask;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Joey on 11/9/2015.
 */
public class ChatPage extends ActionBarActivity {

    //String SERVER_IP = "197.45.183.87";
    String SERVER_IP = "192.168.1.44";

    String userName;
    String name;
    String phoneNumber;

    EditText enteredRecepient;
    String recepientName;
    String recepientUserName;
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
        //new backgroundDBHelperFetchMessages().execute();
        //new backgroundDBHelper().execute();

        listAdapter = new ChatPageAdapter(this, cursor);
        listView.setAdapter(listAdapter);
        refreshCursor();

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //new backgroundDBHelperFetchMessages().execute();
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
//        //Define a projection string that specifies which columns from the database you will actually use after this query.
//        String[] projection = {DBMessagesContract.MessageEntry._ID,
//                DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER,
//                DBMessagesContract.MessageEntry.COLUMN_NAME_TIME,
//                DBMessagesContract.MessageEntry.COLUMN_NAME_CONTENT};
//        //How you want the results to be sorted in the resulting Cursor
//        String sortOrder = DBMessagesContract.MessageEntry.COLUMN_NAME_TIME + " ASC";
//        //The columns for the WHERE clause
//        //String selection = DBMessagesContract.MessageEntry.COLUMN_NAME_RECEPIENT + "=?";
//        String selection = "(" + DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER + "=?1"
//                + " AND "
//                + DBMessagesContract.MessageEntry.COLUMN_NAME_RECEPIENT + "=?2" + ")"
//                +" OR (" + DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER + "=?2"
//                +" AND "
//                + DBMessagesContract.MessageEntry.COLUMN_NAME_RECEPIENT + "=?1" + ")";
//
//        Log.d("ChatPage", selection);
//        //The values of the WHERE clause
//        //String[] selectionArgs = {"JoeyOggie"};
//        String[] selectionArgs = {"JoeyOggieTablet", "JoeyOggiePC"};
//        //The cursor object will contain the result of the query
//        if(readableMessagesDB == null)
//        {
//            new backgroundDBHelper().execute();
//        }
//
//        cursor = readableMessagesDB.query(DBMessagesContract.MessageEntry.TABLE_NAME,
//                projection,
//                selection,
//                selectionArgs,
//                null,
//                null,
//                sortOrder);
//        listAdapter.changeCursor(cursor); //use swapCursor when using CursorLoader
        //recepientUserName = "JoeyOggieTablet";
    }


//    private void insertMessageIntoDB(String[] data){
//        ContentValues values = new ContentValues();
//        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_ID,1);
//        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER, userName);
//        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_RECEPIENT, data[0]);
//        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_CONTENT, data[1]);
//        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_TIME, data[2]);
//        long newRowId;
//        newRowId = writableMessagesDB.insert(DBMessagesContract.MessageEntry.TABLE_NAME,null,values);
//    }

    private void setupActionBar() {
        getSupportActionBar().setTitle("RecepientNameHere");
        getSupportActionBar().setDisplayUseLogoEnabled(true); //Enable the Logo to be shown
        getSupportActionBar().setDisplayShowHomeEnabled(true); //Show the Logo
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Show the Up/Back arrow
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
    }

    private String getServerIP() {
        SharedPreferences tempPrefs = getSharedPreferences("com.example.android.project1.NetworkPreferences", 0);
        return tempPrefs.getString("SERVER_IP","192.168.1.44");
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
        sendButton.setEnabled(false);

        EditText message = (EditText) findViewById(R.id.textInput);
        String mText = message.getText().toString();
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
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy-HH:mm:ss");
            String timestamp = simpleDateFormat.format(date);
            Log.d("TIMESTAMP:", timestamp);

            //new backgroundDBHelperInsertMessages().execute(new String[]{recepientUserName, mText, timestamp});
            //insertMessageIntoDB(new String[]{recepientUserName, mText, timestamp});
            DBMessagesHelper.insertMessageIntoDB(userName, recepientUserName, mText, timestamp);
            //new backgroundDBHelperFetchMessages().execute(); //Mark as sent in onPostExectute below
            refreshCursor();

            //Send the message info to the server in a background thread
            downloadThread download = new downloadThread();
            //download.execute("http://192.168.1.44:8080/MyFirstServlet/AddNewMessage?senderDeviceID=" + URLEncoder.encode(deviceID) + "&recepientUserName=" + URLEncoder.encode(recepientUserName) + "&message=" + URLEncoder.encode(mText));
            download.execute("http://"+SERVER_IP+":8080/MyFirstServlet/AddNewMessage?senderDeviceID=" + URLEncoder.encode(deviceID) + "&recepientUserName=" + URLEncoder.encode(recepientUserName) + "&message=" + URLEncoder.encode(mText)+"&timestamp="+URLEncoder.encode(timestamp));
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

    //AsyncTask that will handle the HTTP connections in a background thread
    public class downloadThread extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            String result = null;
            try {
                result = downloadUrl(urls[0]);
            } catch (IOException e) {

            }
            return result;
        }

        protected void onPreExecute() {
        }

        protected void onPostExecute(String result) {
            //Re-enable the sendButton
            sendButton.setEnabled(true);
        }

        protected void onProgressUpdate(Void... value) {
        }


        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                conn.connect();
                int response = conn.getResponseCode();
                Log.d("MainActivity", "The response is: " + response);
                publishProgress();

                is = conn.getInputStream();

                int len = 500;
                String result = null;

                Reader reader = new InputStreamReader(is, "UTF-8");
                char[] buffer = new char[len];
                reader.read(buffer);
                result = new String(buffer);

                return result;

            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }//End of downloadUrl method
    }

//    //AsynkTask that will get Writable/Readable databases in a background thread
//    public class backgroundDBHelper extends AsyncTask<Void, Void, Void>{
//
//        protected Void doInBackground(Void... params){
//            readableMessagesDB = dbHelper.getReadableDatabase();
//            writableMessagesDB = dbHelper.getWritableDatabase();
//            //Update the listView adapter to refresh the list and show the data that's in the new/updated cursor
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    refreshCursor();
//                }
//            });
//            return null;
//        }
//    }

//    //AsyncTask that will perform DB operations in a background thread
//    public class backgroundDBHelperFetchMessages extends AsyncTask<Void, Void, Void> {
//
//        protected Void doInBackground(Void... params) {
//            messagesDB = dbHelper.getReadableDatabase();
//            //Define a projection string that specifies which columns from the database you will actually use after this query.
//            String[] projection = {DBMessagesContract.MessageEntry._ID,
//                    DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER,
//                    DBMessagesContract.MessageEntry.COLUMN_NAME_TIME,
//                    DBMessagesContract.MessageEntry.COLUMN_NAME_CONTENT};
//            //How you want the results to be sorted in the resulting Cursor
//            String sortOrder = DBMessagesContract.MessageEntry.COLUMN_NAME_TIME + " ASC";
//            //The columns for the WHERE clause
//            //String selection = DBMessagesContract.MessageEntry.COLUMN_NAME_RECEPIENT + "=?";
//            String selection = "";
//            //The values of the WHERE clause
//            //String[] selectionArgs = {"JoeyOggie"};
//            String[] selectionArgs = {};
//            //The cursor object will contain the result of the query
//            cursor = messagesDB.query(DBMessagesContract.MessageEntry.TABLE_NAME,
//                    projection,
//                    selection,
//                    selectionArgs,
//                    null,
//                    null,
//                    sortOrder);
//            //Update the listView adapter to refresh the list and show the data that's in the new/updated cursor
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    listAdapter.changeCursor(cursor);
//                }
//            });
//            return null;
//        }
//    }

//    //AsyncTask that will perform DB operations in a background thread
//    public class backgroundDBHelperInsertMessages extends AsyncTask<String[], Void, Void> {
//
//        protected Void doInBackground(String[]... params) {
//            messagesDB = dbHelper.getWritableDatabase();
//            ContentValues values = new ContentValues();
//            values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_ID,1);
//            values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER,"Me");//Get the correct senderUserName from SharedPrefs
//            values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_RECEPIENT,params[0][0]);
//            values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_CONTENT, params[0][1]);
//            values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_TIME,params[0][2]);
//            long newRowId;
//            newRowId = messagesDB.insert(DBMessagesContract.MessageEntry.TABLE_NAME,null,values);
//            //Update the listView adapter to refresh the list and show the data that's in the new/updated cursor
//            //Not working! You need to re-query the DB and use getReadableDatabase to update the Cursor object itself
//            //or just use new backgroundDBMessagesHelperFetchMessages().execute();
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    listAdapter.changeCursor(cursor);
//                }
//            });
//            return null;
//        }
//    }
}
