package com.example.android.project1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainPage extends ActionBarActivity {

    String recepientUserName;
    String recepientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
        boolean registered = prefs.getBoolean("isRegistered",false);
        if(!registered)
        {
            Intent reg = new Intent(this, Registration.class);
            startActivity(reg);
        }
    }

    public void goToPrivacySettings(View view)
    {
        Intent intent = new Intent(this, PrivacySettings.class);
        startActivity(intent);
    }

    public void goToChatPage(View view) {
        recepientName = "Youssef Wagieh"; //get the name of the clicked contact
        recepientUserName = "JoeyOggiePC"; //get the username of the clicked contact
        Intent intent = new Intent(this, ChatPage.class);
        intent.putExtra("recepientName",recepientName);
        intent.putExtra("recepientUserName",recepientUserName);
        startActivity(intent);
    }

    public void selectContacts(View view)
    {
        Intent intent = new Intent(this, ContactsListView.class);
        startActivity(intent);
    }

    public void goToRegistration(View view)
    {
        Intent intent = new Intent(this, Registration.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_page, menu);
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

        if(id == R.id.action_alt_profile)
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
