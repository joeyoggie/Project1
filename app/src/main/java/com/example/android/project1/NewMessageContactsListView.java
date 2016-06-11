package com.example.android.project1;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SearchView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NewMessageContactsListView extends ActionBarActivity implements SearchView.OnQueryTextListener{

    String SERVER_IP;
    ListView contacts_list_view;
    ContactsAdapter contactsAdapter;
    Cursor cursor;

    ProgressDialog dialog;

    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message_contacts_list_view);

        SERVER_IP = getServerIP();

        contacts_list_view = (ListView) findViewById(R.id.list);

        //Initialize the contacts database helper
        DBContactsHelper dbContactsHelper = DBContactsHelper.getInstance(this);
        //Read the local contacts stored in the local database
        cursor = DBContactsHelper.readContacts();
        contactsAdapter = new ContactsAdapter(this, cursor);
        contacts_list_view.setAdapter(contactsAdapter);
        contactsAdapter.changeCursor(cursor);
        contacts_list_view.setTextFilterEnabled(true);

        //Set the adapter's FilterQueryProvider which will allow filtering the list
        contactsAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                String s = constraint.toString();
                return DBContactsHelper.readFilteredContacts(s);
            }
        });

        //Set the onItemClickListener to open the ChatPage or UserProfile for the clicked contact
        contacts_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cursor.moveToPosition(position);
                String readable_phone_no = cursor.getString(cursor.getColumnIndexOrThrow(DBContactsContract.ContactsEntry.COLUMN_NAME_PHONE_NUMBER));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DBContactsContract.ContactsEntry.COLUMN_NAME_NAME));
                String userName = cursor.getString(cursor.getColumnIndexOrThrow(DBContactsContract.ContactsEntry.COLUMN_NAME_USERNAME));

                Intent intent = new Intent(NewMessageContactsListView.this, ChatPage.class);
                intent.putExtra("phoneNumber", readable_phone_no);
                intent.putExtra("recepientName", name);
                intent.putExtra("recepientUserName", userName);
                //intent.putExtra("recepientProfilePicture", profilePicture);
                startActivity(intent);
                /*if(view.getId() == R.id.profile_picture) {
                    Log.d("NewMessageCon","PROFILE PICTURE");
                    Intent intent = new Intent(NewMessageContactsListView.this, UserProfile.class);
                    intent.putExtra("username_key",userName);
                    startActivity(intent);
                }
                else{
                    Log.d("NewMessageCon","NOT PROFILE PICTURE");
                    Intent intent = new Intent(NewMessageContactsListView.this, ChatPage.class);
                    intent.putExtra("phoneNumber", readable_phone_no);
                    intent.putExtra("recepientUserName", userName);
                    //intent.putExtra("recepientName", recepientName);
                    //intent.putExtra("recepientProfilePicture", profilePicture);
                    startActivity(intent);
                }*/
            }
        });
        refreshContactsFromServer();
    }

    private String getServerIP() {
        SharedPreferences tempPrefs = getSharedPreferences("com.example.android.project1.NetworkPreferences", 0);
        return tempPrefs.getString("SERVER_IP", getResources().getString(R.string.server_ip_address));
    }

    private void setupSearchView()
    {
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setQueryHint("Search here");
    }

    @Override
    public boolean onQueryTextChange(String newText)
    {
        Log.d("CONTACTS QUERY STRING", newText);
        if (TextUtils.isEmpty(newText.toLowerCase())) {
            contacts_list_view.clearTextFilter();
        } else {
            Log.d("CONTACTS","onQueryTextChange not empty");
            contacts_list_view.setFilterText(newText.toLowerCase());
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        Log.d("CONTACTS","onQueryTextSubmit YES");
        contacts_list_view.clearTextFilter();
        return false;
    }

    public void refreshContactsFromServer() {
        ArrayList<String> localPhoneNumbers = read_from_content_providers();
        sendNumbersToServer(localPhoneNumbers);
    }

    public void refreshContacts(){
        dialog = new ProgressDialog(this);
        dialog.setMessage("Refreshing local contacts...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        refreshContactsFromServer();
    }

    public ArrayList<String> read_from_content_providers (){
        String[] columns_array = new String[]{ ContactsContract.CommonDataKinds.Phone.NUMBER}; //this array contains no of returned columns
        //  selection_criteria= new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?"};//selection clause
        //  selection_arguments=new String[]{""};
        // selection_arguments[0]=user_input;
        ContentResolver contentResolver = getApplication().getContentResolver(); //content resolver is used to determine which content provider that we want to access
        Cursor c = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, columns_array, null, null, null);
        int column_index = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        //String column_name = cursor.getColumnName(column_index);
        ArrayList<String> values_of_phone_numbers = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                values_of_phone_numbers.add(c.getString(column_index));
            }while (c.moveToNext());
        }
        c.close();

        return values_of_phone_numbers;
    }

    private void sendNumbersToServer(ArrayList<String> phoneNumbers)
    {
        //Send the data to the server in a background thread
        String url = "http://"+SERVER_IP+":8080/MyFirstServlet/CheckRegisteredContacts";
        JSONArray jsonArray = new JSONArray(phoneNumbers);
        //Request a response from the provided URL.
        JsonArrayRequest requestArray = new JsonArrayRequest(Request.Method.POST, url, jsonArray, new Response.Listener<JSONArray>(){
            Gson gson = new Gson();
            ArrayList<Contact> receivedContacts = new ArrayList<>();
            @Override
            public void onResponse(JSONArray response) {
                if(response.length() > 0) {
                    Type collectionType = new TypeToken<List<Contact>>(){}.getType();
                    //receivedNumbers = gson.fromJson(response.toString(), ArrayList.class);
                    receivedContacts = gson.fromJson(response.toString(), collectionType);
                    if(receivedContacts.isEmpty() == false) {
                        DBContactsHelper.insertContactIntoDataBase(receivedContacts);
                        Log.d("ContactsListView",receivedContacts.toString());
                        cursor = DBContactsHelper.readContacts();
                        contactsAdapter.changeCursor(cursor);
                    }
                    else
                        Log.d("ContactsListView", "No numbers returned.");
                    if(dialog != null && dialog.isShowing()){
                        dialog.dismiss();
                    }
                }
                else {
                    Log.d("ContactsListView", "Receieved an emtpy JSON string");
                    if(dialog != null && dialog.isShowing()){
                        dialog.dismiss();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ContactsListView", "Volley error!");
                if(dialog != null && dialog.isShowing()){
                    dialog.dismiss();
                }
            }
        });
        //Add the request to the RequestQueue.
        HttpConnector.getInstance(this).addToRequestQueue(requestArray);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_message_contacts_list_view, menu);
        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        setupSearchView();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            refreshContacts();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
