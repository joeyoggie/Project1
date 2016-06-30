package com.example.android.project1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.TimeZone;

import github.ankushsachdeva.emojicon.EmojiconEditText;
import github.ankushsachdeva.emojicon.EmojiconGridView;
import github.ankushsachdeva.emojicon.EmojiconsPopup;
import github.ankushsachdeva.emojicon.emoji.Emojicon;

/**
 * Created by Joey on 11/9/2015.
 */
public class ChatPage extends ActionBarActivity {

    String SERVER_IP;

    String userName;
    String name;
    String phoneNumber;
    String deviceID;

    String typingStatusSender;
    String receivedTypingStatus;
    Boolean isTyping = false;

    String recepientName;
    static String recepientUserName;

    EmojiconEditText message;
    ImageButton emojiButton;
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

    int messageID;

    EmojiconsPopup popup;

    int SELECT_FILE = 1;
    private String KEY_IMAGE = "image";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_page);
        setupActionBar();
        SERVER_IP = getServerIP();
        getLocalUserInfo();

        //Set the activity as visible
        setActivityVisibleState(true);

        message = (EmojiconEditText) findViewById(R.id.textInput);
        sendButton = (Button) findViewById(R.id.send_message_button);
        emojiButton = (ImageButton) findViewById(R.id.emoji_button);

        // Give the topmost view of your activity layout hierarchy. This will be used to measure soft keyboard height
        final View rootView = findViewById(R.id.chat_page_root_view);
        popup = new EmojiconsPopup(rootView, this);
        //Will automatically set size according to the soft keyboard size
        popup.setSizeForSoftKeyboard();

        onNewIntent(getIntent());
        listView = (ListView) findViewById(R.id.list);
        listView.setDividerHeight(0);
        dbHelper = DBMessagesHelper.getInstance(this);

        cursor = DBMessagesHelper.readMessages(userName, recepientUserName);
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

        BroadcastReceiver receiver1 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                receiveTypingStatus(intent);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver1, new IntentFilter("newTypingIntent"));


        //If the emoji popup is dismissed, change emojiButton to smiley icon
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                changeEmojiKeyboardIcon(emojiButton, R.drawable.smiley);
            }
        });

        //If the text keyboard closes, also dismiss the emoji popup
        popup.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {
            @Override
            public void onKeyboardOpen(int keyBoardHeight) {
            }
            @Override
            public void onKeyboardClose() {
                if(popup.isShowing())
                    popup.dismiss();
            }
        });

        //On emoji clicked, add it to edittext
        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                if (message == null || emojicon == null) {
                    return;
                }

                int start = message.getSelectionStart();
                int end = message.getSelectionEnd();
                if (start < 0) {
                    message.append(emojicon.getEmoji());
                } else {
                    message.getText().replace(Math.min(start, end),
                            Math.max(start, end), emojicon.getEmoji(), 0,
                            emojicon.getEmoji().length());
                }
            }
        });

        //On backspace clicked, emulate the KEYCODE_DEL key event
        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {
                KeyEvent event = new KeyEvent(
                        0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                message.dispatchKeyEvent(event);
            }
        });

        //Handle the edittext typing/notTyping states and notify the server accordingly
        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (message.getText().toString().trim().length() !=0) {
                    if (isTyping == false) {
                        sendTypingStatusToServer("typing...");
                        isTyping = true;
                    }
                }
                else if(message.getText().toString().trim().length() == 0) {
                    isTyping = false;
                    sendTypingStatusToServer(" ");
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Check if there are any unsent messages and tries to send them again
        sendUnsentMessages();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Bundle extras = intent.getExtras();
        if(extras != null) {
            if(extras.containsKey("recepientName"))
            {
                recepientName = extras.getString("recepientName");
                getSupportActionBar().setTitle(recepientName);
            }
            if(extras.containsKey("recepientUserName"))
            {
                recepientUserName = extras.getString("recepientUserName");
                if(recepientName == null || recepientName.trim().length() == 0)
                {
                    getSupportActionBar().setTitle(recepientUserName);
                }
            }
            //also check for the received recepientProfilePicture as well and display it in the ActionBar
        }
    }

    private void getLocalUserInfo(){
        SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
        userName = prefs.getString("userName","Me");
        name = prefs.getString("name", "Jon Doe");
        phoneNumber = prefs.getString("phoneNumber","0000000000");
        //Get the unique device ID that will be stored in the database to uniquely identify this device
        deviceID = prefs.getString("deviceUUID","0");
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

    private void sendUnsentMessages(){
        Cursor c = DBMessagesHelper.readUnsentMessages(userName, recepientUserName);
        String messageContent;
        String timestamp2;

        int messageColumnIndex = c.getColumnIndexOrThrow(DBMessagesContract.MessageEntry.COLUMN_NAME_CONTENT);
        int timestampIndex = c.getColumnIndexOrThrow(DBMessagesContract.MessageEntry.COLUMN_NAME_TIME);
        int messageIDColumnIndex = c.getColumnIndexOrThrow(DBMessagesContract.MessageEntry._ID);
        if(c.moveToFirst()){
            Log.d("ChatPage", "Old messages detected, sending them...!");
            do{
                messageContent = c.getString(messageColumnIndex);
                timestamp2 = c.getString(timestampIndex);
                messageID = c.getInt(messageIDColumnIndex);

                Log.d("ChatPage", "Sending old message: " + " _ID= " + messageID + ", message: " + messageContent + " @"+timestamp2);

                //Send the message info to the server in a background thread
                String url2 = "http://"+SERVER_IP+":8080/MyFirstServlet/AddNewMessage";

                HashMap<String, String> params = new HashMap<>();
                params.put("senderDeviceID", deviceID);
                params.put("recepientUserName", recepientUserName);
                params.put("messageContent", messageContent);
                params.put("timestamp", timestamp2);
                params.put("messageID", String.valueOf(messageID));
                JSONObject jsonObject = new JSONObject(params);
                Log.d("ChatPage", "JSON Message: "+jsonObject.toString());
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url2, jsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("ChatPage", "VolleyResponse" + response.toString());
                        int msgID = 0;
                        try {
                            msgID = response.getInt("messageID");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        DBMessagesHelper.updateMessage(msgID, "sent");
                        Log.d("ChatPage", "Message _ID= " + msgID + ", sent!" );
                        refreshCursor();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        /*DBMessagesHelper.updateMessage(messageID, "unsent");
                        Log.d("ChatPage", "Message _ID= " +messageID + ", not sent!" );
                        Log.d("ChatPage", "VolleyErrorResponse" + error.toString());
                        refreshCursor();*/
                    }
                });
                //Add the request to the RequestQueue.
                HttpConnector.getInstance(this).addToRequestQueue(jsonObjectRequest);
            }while (c.moveToNext());
        }
    }

    public void receiveTypingStatus(Intent i){
        Log.d("ChatPage", "Typing status received!");
        receivedTypingStatus = i.getStringExtra("receivedTypingStatusFromServer_key");
        typingStatusSender = i.getStringExtra("senderUserName");
        if(typingStatusSender.equals(recepientUserName)) {
            getSupportActionBar().setSubtitle(receivedTypingStatus);
        }
        /*Bundle bundle = getIntent().getExtras();
        if (bundle!=null){
            recievedOfTypingStatus = bundle.getString("recievedTypingStatusFromServer_key");
            senderOfTypingStatus = bundle.getString("sender_key");
            Log.d("ChatPage", recievedOfTypingStatus);
            Log.d("ChatPage", senderOfTypingStatus);
            if(senderOfTypingStatus.equals(recepientUserName))
            {
                getSupportActionBar().setSubtitle(recievedOfTypingStatus);
            }
        }*/
    }

    public void sendTypingStatusToServer(String typingStatus){

        String URL = "http://"+SERVER_IP+":8080/MyFirstServlet/UpdateTypingStatus?senderUserName="+URLEncoder.encode(userName)+"&recepientUserName="+URLEncoder.encode(recepientUserName)+"&typingStatus="+URLEncoder.encode(typingStatus);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("ChatPage", "Typing status sent to server.");
                Log.i("ChatPage", "VolleyResponse" + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ChatPage", "Error sending typing status sent to server.");
                Log.e("ChatPage", "VolleyErrorResponse"+error.toString());
            }
        });
        HttpConnector.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    public void toggleEmojis(View view){
        //If popup is not showing => emoji keyboard is not visible, we need to show it
        if(!popup.isShowing()){
            //If keyboard is visible, simply show the emoji popup
            if(popup.isKeyBoardOpen()){
                popup.showAtBottom();
                changeEmojiKeyboardIcon(emojiButton, R.drawable.ic_action_keyboard);
            }
            //else, open the text keyboard first and immediately after that show the emoji popup
            else{
                message.setFocusableInTouchMode(true);
                message.requestFocus();
                popup.showAtBottomPending();
                final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(message, InputMethodManager.SHOW_IMPLICIT);
                changeEmojiKeyboardIcon(emojiButton, R.drawable.ic_action_keyboard);
            }
        }
        //If popup is showing, simply dismiss it to show the undelying text keyboard
        else{
            popup.dismiss();
        }
    }

    public void sendMessage(View view) {
        //Hide the keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        mText = message.getText().toString();
        if(mText.trim().length() == 0)
        {
            return;
        }
        message.setText("");
        isTyping = false;

        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        timestamp = simpleDateFormat.format(date);
        Log.d("TIMESTAMP:", timestamp);

        //Send the message info to the server in a background thread
        String url2 = "http://"+SERVER_IP+":8080/MyFirstServlet/AddNewMessage";
        /*Message messageObject = new Message();
        messageObject.setMessageSenderDeviceID(deviceID);
        messageObject.setMessageRecepientUserName(recepientUserName);
        messageObject.setMessageContent(mText);
        messageObject.setTimestamp(timestamp);*/

        HashMap<String, String> params = new HashMap<>();
        params.put("senderDeviceID", deviceID);
        params.put("recepientUserName", recepientUserName);
        params.put("messageContent", mText);
        params.put("timestamp", timestamp);
        params.put("messageID", String.valueOf(0));
        JSONObject jsonObject = new JSONObject(params);
        Log.d("ChatPage", "JSON Message: "+jsonObject.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url2, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                DBMessagesHelper.insertMessageIntoDB(userName, recepientUserName, mText, timestamp, "sent");
                refreshCursor();
                Log.d("ChatPage", "VolleyResponse:" + response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                DBMessagesHelper.insertMessageIntoDB(userName, recepientUserName, mText, timestamp, "unsent");
                refreshCursor();
                Log.d("ChatPage", "VolleyErrorResponse:" + error.toString());
            }
        });
        //Add the request to the RequestQueue.
        HttpConnector.getInstance(this).addToRequestQueue(jsonObjectRequest);

        /*//Send the message info to the server in a background thread
        //Instantiate the RequestQueue.
        String url = "http://"+SERVER_IP+":8080/MyFirstServlet/AddNewMessage?senderDeviceID=" + URLEncoder.encode(deviceID) + "&recepientUserName=" + URLEncoder.encode(recepientUserName) + "&message=" + URLEncoder.encode(mText) +"&timestamp="+URLEncoder.encode(timestamp);
        //Request a string response from the provided URL.
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                DBMessagesHelper.insertMessageIntoDB(userName, recepientUserName, mText, timestamp);
                refreshCursor();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                DBMessagesHelper.insertMessageIntoDB(userName, recepientUserName, mText, timestamp);
                refreshCursor();
            }
        });
        //Add the request to the RequestQueue.
        HttpConnector.getInstance(this).addToRequestQueue(request);*/
    }

    private void changeEmojiKeyboardIcon(ImageButton iconToBeChanged, int drawableResourceId){
        iconToBeChanged.setImageResource(drawableResourceId);
    }

    public void sendScheduledMessage(View view)
    {
        DialogFragment newFragment = new PopupMessageDialog();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    private void sendOnlineStateToServer(final String state){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String timestamp2 = simpleDateFormat.format(date);

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
                params.put("timestamp", timestamp2);

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

    public void loadImagefromGallery() {
//        // Create intent to Open Image applications like Gallery, Google Photos
//        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
//                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        // Start the Intent
//        startActivityForResult(galleryIntent,RESULT_LOAD_IMG);
        //  Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMG);
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(
                Intent.createChooser(intent, "Select File"),
                SELECT_FILE);
    }

    public void sendImage(Bitmap bitmap){
        final Bitmap bm = bitmap;
        final ProgressDialog loading = ProgressDialog.show(this,"Uploading...","Please wait...",false,false);
        SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences", 0);
        String deviceID = prefs.getString("deviceUUID","0");
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        timestamp = simpleDateFormat.format(date);
        String url = "http://" + SERVER_IP + ":8080/MyFirstServlet/AddNewImage?senderDeviceID="+deviceID+"receptientUserName"+ recepientUserName+"&timestamp="+timestamp;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        loading.dismiss();
                        //Showing toast message of the response
                        Toast.makeText(ChatPage.this, s, Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                        //Showing toast
                        //Toast.makeText(ChatPage.this, volleyError.getMessage().toString(), Toast.LENGTH_LONG).
                          //      show();
                    }
                }) {
            protected Map<String, String> getParams() throws AuthFailureError
            {
                //Converting Bitmap to String
                String Image = bm.toString();
                //Getting Image Name

                //Creating parameters
                Map<String, String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put(KEY_IMAGE, Image);
                //  params.put(KEY_NAME, name);

                //returning parameters
                return params;
            }
        };
        //Adding request to the queue
        HttpConnector.getInstance(this).addToRequestQueue(stringRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        try {
//            // When an Image is picked
//            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
//                    && null != data) {
//                // Get the Image from data
//
//                Uri selectedImage = data.getData();
//                String[] filePathColumn = { MediaStore.Images.Media.DATA };
//
//                // Get the cursor
//                Cursor cursor = getContentResolver().query(selectedImage,
//                        filePathColumn, null, null, null);
//                // Move to first row
//                cursor.moveToFirst();
//
//                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//                imgDecodableString = cursor.getString(columnIndex);
//                cursor.close();
////                ImageView imgView = (ImageView) findViewById(R.id.imgView);
////                // Set the Image in ImageView after decoding the String
////                imgView.setImageBitmap(BitmapFactory
////                        .decodeFile(imgDecodableString));
//                PopUp_Window pop=new PopUp_Window();
//                pop.init();
//                //    pop.popupInit();
//                pop.setImage(BitmapFactory.decodeFile(imgDecodableString));
        //  if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && data != null && data.getData() != null) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE){
                Uri selectedImageUri = data.getData();
                String[] projection = { MediaStore.MediaColumns.DATA };
                Cursor cursor = managedQuery(selectedImageUri, projection, null, null,
                        null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();

                String selectedImagePath = cursor.getString(column_index);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(selectedImagePath, options);
                final int REQUIRED_SIZE = 200;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                        && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                Bitmap bm = BitmapFactory.decodeFile(selectedImagePath, options);
                sendImage(bm);
            }
        }


//            Uri filePath = data.getData();
//            try {
//                //Getting the Bitmap from Gallery
//                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
//                //Setting the Bitmap to ImageView
//             //   imageView.setImageBitmap(bitmap);
//                PopUp_Window pop=new PopUp_Window();
//                pop.init();
//                pop.setImage(bitmap);

//            }else{
//                Toast.makeText(this, "You haven't picked Image",
//                        Toast.LENGTH_LONG).show();
//            }
//        } catch (Exception ex) {
//            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
//                    .show();

    }

    //Use this method to set a flag during the activity's life cycle methods to indicate whether it's shown or not
    private void setActivityVisibleState(boolean isVisible){
        SharedPreferences prefs  = getSharedPreferences("com.example.android.project1.ChatPageState",0);
        SharedPreferences.Editor prefsEditor;
        prefsEditor = prefs.edit();
        prefsEditor.putBoolean("isVisible", isVisible);
        prefsEditor.apply();
    }

    @Override
    protected void onDestroy() {
        //Set the activity as invisible
        setActivityVisibleState(false);
        //Send 'offline' state to server
        sendOnlineStateToServer("offline");
        super.onDestroy();
    }

    @Override
    public void onStop()
    {
        //Set the activity as invisible
        setActivityVisibleState(false);
        //Send 'offline' state to server
        sendOnlineStateToServer("offline");
        super.onStop();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        //Set the activity as visible
        setActivityVisibleState(true);
        //Send 'online' state to server
        sendOnlineStateToServer("online");
    }

    @Override
    public void onResume(){
        super.onResume();
        //Set the activity as visible
        setActivityVisibleState(true);
        //Send 'online' state to server
        //sendOnlineStateToServer("online");
        refreshCursor();
    }

    @Override
    public void onPause()
    {
        //Set the activity as invisible
        setActivityVisibleState(false);
        //Send 'offline' state to server
        //sendOnlineStateToServer("offline");
        super.onPause();
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
        if(id==R.id.image_upload)
        {
            //  DialogFragment newFragment = new PopupMessageDialog();
            //  ViewGroup group = (ViewGroup) findViewById(R.id.imageLayout);
            // loadImagefromGallery(group);
            loadImagefromGallery();
        }
        return super.onOptionsItemSelected(item);
    }
}
