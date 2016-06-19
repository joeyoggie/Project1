package com.example.android.project1;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TimePicker;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class PopupMessageDialog extends DialogFragment implements CustomTimePickerDialog.OnTimeSetListener {

    String SERVER_IP;
    DBMessagesHelper dbHelper;
    SQLiteDatabase readableMessagesDB;
    SQLiteDatabase writableMessagesDB;
    Cursor cursor;

    String userName;
    String name;
    String phoneNumber;

    String recepientUserName;
    String message;
    String timestamp;

    int count;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_popup_message_dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        getLocalUserInfo();
        SERVER_IP = getServerIP();
        dbHelper = DBMessagesHelper.getInstance(getActivity());
        //new backgroundDBHelper().execute();
        count = 0;

        // Create a new instance of TimePickerDialog and return it
        return new CustomTimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if(count == 0)
        {
            // Do something with the time chosen by the user
            EditText enteredScheduledMessage = (EditText) getDialog().findViewById(R.id.entered_scheduled_message);
            message = enteredScheduledMessage.getText().toString();
            //EditText enteredRecepientUserName = (EditText) getActivity().findViewById(R.id.entered_recepient);
            //String recepientUserName = enteredRecepientUserName.getText().toString();
            recepientUserName = ChatPage.recepientUserName;
            if(message.trim().length() == 0)
            {
                return;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            Log.d("CALENDAR TIME: ", calendar.toString());
            Date date = calendar.getTime();
            Log.d("DATE: ", date.toString());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
            timestamp = simpleDateFormat.format(date);
            Log.d("TIMESTAMP:", timestamp);

            //Get the unique device ID that will be stored in the database to uniquely identify this device
            SharedPreferences prefs = getActivity().getSharedPreferences("com.example.android.project1.RegistrationPreferences", 0);
            String deviceID = prefs.getString("deviceUUID", "0");

            //Check if there's an internet connection
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connMgr.getActiveNetworkInfo();

            //If there's an internet connection
            if (netInfo != null && netInfo.isConnected())
            {
                //Send the message info to the server in a background thread
                String url2 = "http://"+SERVER_IP+":8080/MyFirstServlet/AddNewMessage";

                HashMap<String, String> params = new HashMap<>();
                params.put("senderDeviceID", deviceID);
                params.put("recepientUserName", recepientUserName);
                params.put("messageContent", message);
                params.put("timestamp", timestamp);
                JSONObject jsonObject = new JSONObject(params);
                Log.d("ChatPage", "JSON: "+jsonObject.toString());
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url2, jsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //TODO: Add a flag marking the message as sent successfully
                        DBMessagesHelper.insertMessageIntoDB(userName, recepientUserName, message, timestamp);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //TODO: Add a flag marking the message as not sent
                        DBMessagesHelper.insertMessageIntoDB(userName, recepientUserName, message, timestamp);
                    }
                });
                //Add the request to the RequestQueue.
                HttpConnector.getInstance(this.getActivity()).addToRequestQueue(jsonObjectRequest);
            }
            count++;
        }
    }

    private void getLocalUserInfo(){
        SharedPreferences prefs = getActivity().getSharedPreferences("com.example.android.project1.RegistrationPreferences", 0);
        userName = prefs.getString("userName","Me");
        name = prefs.getString("name","Jon Doe");
        phoneNumber = prefs.getString("phoneNumber", "0000000000");
    }

    private String getServerIP()
    {
        SharedPreferences tempPrefs = getActivity().getSharedPreferences("com.example.android.project1.NetworkPreferences", 0);
        return tempPrefs.getString("SERVER_IP", getResources().getString(R.string.server_ip_address));
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

        return super.onOptionsItemSelected(item);
    }
}
