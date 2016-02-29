package com.example.android.project1;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TimePicker;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class PopupMessageDialog extends DialogFragment implements CustomTimePickerDialog.OnTimeSetListener {

    //String SERVER_IP = "197.45.183.87";
    String SERVER_IP = "192.168.1.44";
    DBMessagesHelper dbHelper;
    SQLiteDatabase readableMessagesDB;
    SQLiteDatabase writableMessagesDB;
    Cursor cursor;

    String userName;
    String name;
    String phoneNumber;

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
            String message = enteredScheduledMessage.getText().toString();
            EditText enteredRecepientUserName = (EditText) getActivity().findViewById(R.id.entered_recepient);
            String recepientUserName = enteredRecepientUserName.getText().toString();
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
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy-HH:mm:ss");
            String timestamp = simpleDateFormat.format(date);
            Log.d("TIMESTAMP:", timestamp);

            SharedPreferences tempPrefs = getActivity().getSharedPreferences("com.example.android.project1.NetworkPreferences", 0);
            SERVER_IP = tempPrefs.getString("SERVER_IP","192.168.1.44");

            //Get the unique device ID that will be stored in the database to uniquely identify this device
            SharedPreferences prefs = getActivity().getSharedPreferences("com.example.android.project1.RegistrationPreferences", 0);
            String deviceID = prefs.getString("deviceUUID", "0");

            //Check if there's an internet connection
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connMgr.getActiveNetworkInfo();

            //If there's an internet connection
            if (netInfo != null && netInfo.isConnected())
            {
                //new backgroundDBHelperInsertMessages().execute(new String[]{recepientUserName, mText, timestamp});
                //insertMessageIntoDB(new String[]{recepientUserName, message, timestamp});
                //new backgroundDBHelperFetchMessages().execute(); //Mark as sent in onPostExectute below
                DBMessagesHelper.insertMessageIntoDB(userName, recepientUserName, message, timestamp);

                //Send the message info to the server in a background thread
                downloadThread download = new downloadThread();
                //download.execute("http://192.168.1.44:8080/MyFirstServlet/AddNewMessage?senderDeviceID=" + URLEncoder.encode(deviceID) + "&recepientUserName=" + URLEncoder.encode(recepientUserName) + "&message=" + URLEncoder.encode(mText));
                download.execute("http://"+SERVER_IP+":8080/MyFirstServlet/AddNewMessage?senderDeviceID=" + URLEncoder.encode(deviceID) + "&recepientUserName=" + URLEncoder.encode(recepientUserName) + "&message=" + URLEncoder.encode(message)+"&timestamp="+URLEncoder.encode(timestamp));
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

//    private void insertMessageIntoDB(String[] data){
//        ContentValues values = new ContentValues();
//        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_ID,1);
//        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER,userName);
//        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_RECEPIENT, data[0]);
//        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_CONTENT, data[1]);
//        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_TIME, data[2]);
//        long newRowId;
//        newRowId = writableMessagesDB.insert(DBMessagesContract.MessageEntry.TABLE_NAME,null,values);
//    }


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
//            return null;
//        }
//    }
}
