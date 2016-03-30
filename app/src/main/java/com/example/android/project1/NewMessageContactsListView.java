package com.example.android.project1;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

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

public class NewMessageContactsListView extends AppCompatActivity {

    ListView contacts_list;
    ContactsAdapter contactsAdapter;
    Cursor cursor;
    ArrayList<String> receivedNumbers = new ArrayList<>();
    ArrayList<Contact> receivedContacts = new ArrayList<>();

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message_contacts_list_view);

        contacts_list = (ListView) findViewById(R.id.list);
        DBContactsHelper dbContactsHelper = DBContactsHelper.getInstance(getApplicationContext());
        cursor = DBContactsHelper.readContacts();
        contactsAdapter = new ContactsAdapter(this, cursor);
        contacts_list.setAdapter(contactsAdapter);
        contactsAdapter.changeCursor(cursor);
        contacts_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                cursor.moveToPosition(position);
                String readable_phone_no = cursor.getString(cursor.getColumnIndexOrThrow(DBContactsContract.ContactsEntry.COLUMN_NAME_PHONE_NUMBER));
                String userName = cursor.getString(cursor.getColumnIndexOrThrow(DBContactsContract.ContactsEntry.COLUMN_NAME_USERNAME));

                Intent intent = new Intent(NewMessageContactsListView.this, ChatPage.class);
                intent.putExtra("phoneNumber", readable_phone_no);
                intent.putExtra("recepientUserName", userName);
                //intent.putExtra("recepientName", recepientName);
                //intent.putExtra("recepientProfilePicture", profilePicture);
                startActivity(intent);
            }
        });
    }

    public void refreshContactsFromServe(View view){
        dialog = new ProgressDialog(this);
        dialog.setMessage("Refreshing local contacts...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        read_from_content_providers();
        cursor = DBContactsHelper.readContacts();
        contactsAdapter.changeCursor(cursor);
    }

    public void read_from_content_providers (){
        String[] columns_array = new String[]{ ContactsContract.CommonDataKinds.Phone.NUMBER}; //this array contains no of returned columns
        //  selection_criteria= new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?"};//selection clause
        //  selection_arguments=new String[]{""};
        // selection_arguments[0]=user_input;
        ContentResolver contentResolver = getApplication().getContentResolver(); //content resolver is used to determine which content provider that we want to access
        cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, columns_array, null, null, null);
        int column_index = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        //String column_name = cursor.getColumnName(column_index);
        ArrayList<String> values_of_phone_numbers = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                values_of_phone_numbers.add(cursor.getString(column_index));
            }while (cursor.moveToNext());
        }
        cursor.close();

        sendNumbersToServer(values_of_phone_numbers);
    }

    private void sendNumbersToServer(ArrayList<String> phoneNumbers)
    {
        //Send the data to the server in a background thread
        String url = "http://192.168.1.44:8080/MyFirstServlet/TestingVolley";
        JSONArray jsonArray = new JSONArray(phoneNumbers);
        //Request a response from the provided URL.
        JsonArrayRequest requestArray = new JsonArrayRequest(Request.Method.POST, url, jsonArray, new Response.Listener<JSONArray>(){
            Gson gson = new Gson();
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
                    dialog.dismiss();
                }
                else {
                    Log.d("ContactsListView", "Receieved an emtpy JSON string");
                    dialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ContactsListView", "Volley error!");
                dialog.dismiss();
            }
        });
        //Add the request to the RequestQueue.
        HttpConnector.getInstance(this).addToRequestQueue(requestArray);
    }
}
