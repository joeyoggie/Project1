package com.example.android.project1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class LocationServicesRegistrationPrompt extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_services_registration_prompt);
    }

    public void skipLocationServicesRegistration(View view) {
        //skipped means it'll be asked for later on
        SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString("locationServicesRegistration", "skipped");
        prefsEditor.apply();

        Intent intent = new Intent(this, MainPage.class);
        startActivity(intent);
    }

    public void startLocationServicesRegistration(View view) {
        //started means it might ask again if it's still the same value and not 'registered'
        SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString("locationServicesRegistration", "started");
        prefsEditor.apply();

        Intent intent = new Intent(this, LocationServicesRegistration.class);
        startActivity(intent);
    }
}
