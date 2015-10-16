package com.example.android.project1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class PrivacySettings extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_settings);
        setTitle("Privacy Settings");
    }

    public void goToBlockedContactsSettings(View view)
    {
        Intent intent = new Intent(this, BlockedContactsSettings.class);
        startActivity(intent);
    }

    public void goToStatusActivity(View view)
    {
        Intent intent = new Intent(this, StatusSettings.class);
        startActivity(intent);
    }

    public void goToProfilePictureActivity(View view)
    {
        Intent intent = new Intent(this, ProfilePictureSettings.class);
        startActivity(intent);
    }

    public void goToPhoneNumberSettings(View view)
    {
        Intent intent = new Intent(this, PhoneNumberSettings.class);
        startActivity(intent);
    }

    public void goToOnlinePresenceSettings(View view)
    {
        Intent intent = new Intent(this, OnlinePresenceSettings.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_privacy_settings, menu);
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
        if(id == R.id.action_search)
        {
            Intent intent = new Intent(this, MyProfile.class);
            startActivity(intent);
            return true;
        }

        if(id == R.id.location_services)
        {
            Intent intent = new Intent(this, LocationServices.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
