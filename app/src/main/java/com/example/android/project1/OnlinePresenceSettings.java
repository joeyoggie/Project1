package com.example.android.project1;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;


public class OnlinePresenceSettings extends ActionBarActivity implements AdapterView.OnItemSelectedListener{

    View manualSelection;
    TextView currentlySelected;
    View contactInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_presence_settings);
        setTitle("Online Presence Privacy Settings");

        Spinner dropdown = (Spinner) findViewById(R.id.online_state_spinner);
        String[] items = new String[]{"Everyone", "My contacts", "Close friends", "Select from contacts"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        manualSelection =  findViewById(R.id.manualSelection);
        manualSelection.setVisibility(View.INVISIBLE);
        currentlySelected = (TextView) findViewById(R.id.currently_selected);
        currentlySelected.setVisibility(View.INVISIBLE);

        dropdown.setOnItemSelectedListener(this);

        contactInfo = findViewById(R.id.contact_info);
        contactInfo.setVisibility(View.INVISIBLE);
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
    {
        switch (position)
        {
            case 0:
                manualSelection.setVisibility(View.INVISIBLE);
                currentlySelected.setVisibility(View.INVISIBLE);
                break;
            case 1:
                manualSelection.setVisibility(View.INVISIBLE);
                currentlySelected.setVisibility(View.INVISIBLE);
                break;
            case 2:
                manualSelection.setVisibility(View.INVISIBLE);
                currentlySelected.setVisibility(View.INVISIBLE);
                break;
            case 3:
                manualSelection.setVisibility(View.VISIBLE);
                currentlySelected.setVisibility(View.VISIBLE);
                currentlySelected.setText("Currently selected: *Selected contacts listed here*");
                break;
        }
    }

    public void onNothingSelected(AdapterView<?> parent)
    {

    }

    public void addToList(View view)
    {
        TextView enteredName = (TextView) findViewById(R.id.enteredName);
        String name = enteredName.getText().toString();
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
        getMenuInflater().inflate(R.menu.menu_online_presence_settings, menu);
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
