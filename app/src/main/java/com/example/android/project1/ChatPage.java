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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    String typingStatusSender;
    String receivedTypingStatus;
    Boolean isTyping = false;

    EditText enteredRecepient;
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

    EmojiconsPopup popup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_page);
        setupActionBar();
        SERVER_IP = getServerIP();
        getLocalUserInfo();

        enteredRecepient = (EditText) findViewById(R.id.entered_recepient);
        enteredRecepient.setVisibility(View.GONE);
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
                Log.i("VOLLEY", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ChatPage", "Error sending typing status sent to server.");
                Log.e("VOLLEY", error.toString());
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
        //Disable the sendButton temporarily

        mText = message.getText().toString();
        if(mText.trim().length() == 0)
        {
            return;
        }
        message.setText("");
        isTyping = false;

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

    private void changeEmojiKeyboardIcon(ImageButton iconToBeChanged, int drawableResourceId){
        iconToBeChanged.setImageResource(drawableResourceId);
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
