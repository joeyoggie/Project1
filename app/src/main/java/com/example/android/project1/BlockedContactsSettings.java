package com.example.android.project1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class BlockedContactsSettings extends ActionBarActivity {

    View contactInfo;
    //ArrayList<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_contacts_settings);
        setTitle("Blocked Contacts");

        contactInfo = findViewById(R.id.blocked_contact_info);
        contactInfo.setVisibility(View.INVISIBLE);

/*
        //generate list
        list = new ArrayList<String>();

        //instantiate custom adapter
        ContactItem adapter = new ContactItem(list, this);

        //handle listview and assign adapter
        ListView lView = (ListView)findViewById(R.id.list_view);
        lView.setAdapter(adapter);
*/
    }

    public void addToList(View view)
    {
        TextView enteredName = (TextView) findViewById(R.id.enteredName);
        String name = enteredName.getText().toString();
        //list.add(name);
        enteredName.setText("");

        TextView contactName = (TextView) findViewById(R.id.contact_name);
        contactName.setText(name);
        contactInfo.setVisibility(View.VISIBLE);
    }

    public void removeFromList(View view)
    {
        contactInfo.setVisibility(View.INVISIBLE);
    }

    public void selectContacts(View view)
    {
        Intent intent = new Intent(this, ContactsListView.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_blocked_contacts_settings, menu);
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

        return super.onOptionsItemSelected(item);
    }
}
