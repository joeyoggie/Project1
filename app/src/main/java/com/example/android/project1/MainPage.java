package com.example.android.project1;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


public class MainPage extends ActionBarActivity {

    String recepientUserName;
    String recepientName;

    String userName;
    String name;
    String phoneNumber;

    DrawerLayout mDrawerLayout;
    View navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        setupActionBar();
        getLocalUserInfo();

        SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
        boolean registered = prefs.getBoolean("isRegistered", false);
        if(!registered)
        {
            Intent reg = new Intent(this, Registration.class);
            startActivity(reg);
        }

        //Drawlayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.drawerPane);

        TextView nameTextView = (TextView) findViewById(R.id.name);
        TextView userNameTextView = (TextView) findViewById(R.id.user_name);
        TextView phoneNumberTextView = (TextView) findViewById(R.id.phone_number);
        ImageView profilePictureImageView = (ImageView) findViewById(R.id.personal_profile_picture);
        nameTextView.setText(name);
        userNameTextView.setText("@"+userName);
        phoneNumberTextView.setText(phoneNumber);
        //profilePictureImageView.setImageBitmap(bitmap);
    }

    private void setupActionBar() {
        getSupportActionBar().setDisplayUseLogoEnabled(true); //Enable the Logo to be shown
        getSupportActionBar().setDisplayShowHomeEnabled(true); //Show the Logo
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Show the Up/Back arrow
        getSupportActionBar().setLogo(R.mipmap.ic_launcher); //Specify the logo image
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer); //Specify the Up/Back arrow image
    }

    private void getLocalUserInfo(){
        SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
        userName = prefs.getString("userName","Me");
        name = prefs.getString("name", "Jon Doe");
        phoneNumber = prefs.getString("phoneNumber","0000000000");
    }

    public void goToPrivacySettings(View view)
    {
        Intent intent = new Intent(this, PrivacySettings.class);
        startActivity(intent);
        if (mDrawerLayout.isDrawerOpen(navigationView)) {
            mDrawerLayout.closeDrawer(navigationView);
        }
    }

    public void goToChatPage(View view) {
        EditText customRecepientUserName = (EditText) findViewById(R.id.custom_recepient_username);
        EditText customRecepientName = (EditText) findViewById(R.id.custom_recepient_name);

        recepientName = customRecepientName.getText().toString(); //get the name of the clicked contact
        recepientUserName = customRecepientUserName.getText().toString(); //get the username of the clicked contact
        Intent intent = new Intent(this, ChatPage.class);
        intent.putExtra("recepientName",recepientName);
        intent.putExtra("recepientUserName",recepientUserName);
        startActivity(intent);
    }

    public void newMessageContactListView(View view){
        Intent intent = new Intent(this, NewMessageContactsListView.class);
        startActivity(intent);
    }


    public void goToRegistration(View view)
    {
        Intent intent = new Intent(this, Registration.class);
        startActivity(intent);
        if (mDrawerLayout.isDrawerOpen(navigationView)) {
            mDrawerLayout.closeDrawer(navigationView);
        }
    }

    public void goToSettings(View view)
    {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
        if (mDrawerLayout.isDrawerOpen(navigationView)) {
            mDrawerLayout.closeDrawer(navigationView);
        }
    }

    public void navigateToStatusFragment(View view){
        DialogFragment newFragment = new StatusFragment();
        newFragment.show(getFragmentManager(),"statusFragment");
    }

    @Override
    public void onBackPressed(){
        if (mDrawerLayout.isDrawerOpen(navigationView)) {
            mDrawerLayout.closeDrawer(navigationView);
        }
        else{
            MainPage.super.onBackPressed();
        }
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
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
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

        if(id == android.R.id.home)
        {
            //when clicked
            if (mDrawerLayout.isDrawerOpen(navigationView)) {
                mDrawerLayout.closeDrawer(navigationView);
            } else {
                mDrawerLayout.openDrawer(navigationView);
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
