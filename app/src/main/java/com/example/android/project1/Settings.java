package com.example.android.project1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class Settings extends ActionBarActivity implements AdapterView.OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Spinner spinner = (Spinner) findViewById(R.id.ip_addresses_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.ip_addresses_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }


    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        if(pos == 0)
        {
            //Static IP (197.45.183.87)
            SharedPreferences prefs = getSharedPreferences("com.example.android.project1.NetworkPreferences",0);
            SharedPreferences.Editor prefsEditor = prefs.edit();
            prefsEditor.putString("SERVER_IP", getResources().getString(R.string.server_ip_address));
            prefsEditor.apply();
            Log.d("Settings", "IP set to: " + getResources().getString(R.string.server_ip_address));
        }
        else if(pos == 1)
        {
            //Localhost IP (192.168.1.44)
            SharedPreferences prefs = getSharedPreferences("com.example.android.project1.NetworkPreferences",0);
            SharedPreferences.Editor prefsEditor = prefs.edit();
            prefsEditor.putString("SERVER_IP",getResources().getString(R.string.server_ip_address_local));
            prefsEditor.apply();
            Log.d("Settings", "IP set to: " + getResources().getString(R.string.server_ip_address_local));
        }
        else if(pos == 2)
        {
            //Custom IP
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
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
            //Launch advanced settings activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
