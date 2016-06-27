package com.example.android.project1;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationServicesRegistration extends AppCompatActivity implements MapDialog.OnLocationSelectedListener {

    String SERVER_IP;
    ProgressDialog progressDialog;
    //The info that will be sent to the server to add a new service provider
    double latitude;
    double longitude;
    String name;
    String userName;
    String phoneNumber;
    String job;
    String address;

    AutoCompleteTextView jobEditText;
    TextView nameTextView, userNameTextView, phoneNumberTextView, addressTextView, locationTextView;
    Button addJobButton;

    //Boolean to indicate whether the user has tried to get his/her location from the map
    Boolean userLocation = false;

    List<String> suggestedJobs;
    ArrayAdapter<String> suggestedJobsAdapter;
    /*Spinner countrySpinner, citySpinner;
    ArrayList<String> countries, cities;
    ArrayAdapter<String> countrySpinnerAdapter, citySpinnerAdapter;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_services_registration);

        SERVER_IP = getServerIP();
        getLocalUserInfo();

        //initializeSpinners();

        progressDialog = new ProgressDialog(LocationServicesRegistration.this);
        progressDialog.setMessage("Getting info from server...");
        progressDialog.setProgressStyle(progressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //initialize the views
        nameTextView = (TextView) findViewById(R.id.name_text_view);
        userNameTextView = (TextView) findViewById(R.id.username_text_view);
        phoneNumberTextView = (TextView) findViewById(R.id.phone_number_text_view);
        addressTextView = (TextView) findViewById(R.id.address_text_view);
        locationTextView = (TextView) findViewById(R.id.location_text_view);
        jobEditText = (AutoCompleteTextView) findViewById(R.id.job_category_edit_text);
        addJobButton = (Button) findViewById(R.id.add_job_button);

        nameTextView.setText(name);
        userNameTextView.setText("@"+userName);
        phoneNumberTextView.setText(phoneNumber);

        //Check if already registered, and if so, fill the views with the stored values
        SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
        if(prefs.getString("locationServicesRegistration", "notRegistered").equals("registered")) {
            jobEditText.setText(prefs.getString("job", "No job defined yet"));
            addressTextView.setText(prefs.getString("address", "No address defined yet"));
            latitude = Double.longBitsToDouble(prefs.getLong("latitude", 0));
            longitude = Double.longBitsToDouble(prefs.getLong("longitude", 0));
            locationTextView.setText(String.valueOf(latitude) + "," + String.valueOf(longitude));
            addJobButton.setText("Update job info!");
        }

        //Add some jobs to the autocomplete edittextview
        addJobsToAutoCompleteEditText();
    }

    private void getLocalUserInfo(){
        SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
        userName = prefs.getString("userName","Me");
        name = prefs.getString("name", "Jon Doe");
        phoneNumber = prefs.getString("phoneNumber","0000000000");
    }

    private String getServerIP() {
        SharedPreferences tempPrefs = getSharedPreferences("com.example.android.project1.NetworkPreferences", 0);
        return tempPrefs.getString("SERVER_IP", getResources().getString(R.string.server_ip_address));
    }

    private void addJobsToAutoCompleteEditText() {
        suggestedJobs = new ArrayList<>();
        suggestedJobsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, suggestedJobs);
        jobEditText.setAdapter(suggestedJobsAdapter);

        String url = "http://"+SERVER_IP+":8080/MyFirstServlet/GetAllServiceProviderCategories";
        //Request a response from the provided URL.
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
            Gson gson = new Gson();
            @Override
            public void onResponse(String response) {
                if (response.length() > 0){
                    suggestedJobs = gson.fromJson(response, ArrayList.class);
                    if(suggestedJobs.isEmpty() == false){
                        Log.d("LocationServicesReg", "Received jobs: " + suggestedJobs.toString());
                        suggestedJobsAdapter = new ArrayAdapter<>(LocationServicesRegistration.this, android.R.layout.simple_list_item_1, suggestedJobs);
                        jobEditText.setAdapter(suggestedJobsAdapter);
                    }
                    else{
                        Log.d("LocationServicesReg", "Received non/empty list response: "+response.toString());
                        useDefaultJobs();
                    }
                }
                else {
                    Log.d("LocationServicesReg", "Received an empty response from server.");
                    useDefaultJobs();
                }
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("LocationServicesReg", "Volley error!");
                useDefaultJobs();
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
            }
        });
        //Add the request to the RequestQueue.
        HttpConnector.getInstance(this).addToRequestQueue(request);
    }

    private void useDefaultJobs(){
        suggestedJobs.add("Mechanic");
        suggestedJobs.add("Electrician");
        suggestedJobs.add("Plumber");
        suggestedJobs.add("Doctor");
        suggestedJobs.add("Pharmacy");
        suggestedJobs.add("Police");
        suggestedJobs.add("Cook");
        suggestedJobs.add("Chef");
        suggestedJobs.add("Carpenter");
        //probably add more here to cover more use cases,
        //and keep this synced with the server as well if possible
        suggestedJobsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, suggestedJobs);
        jobEditText.setAdapter(suggestedJobsAdapter);
    }

    private void updateTextViews() {
        SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
        nameTextView.setText(name);
        userNameTextView.setText("@"+userName);
        phoneNumberTextView.setText(phoneNumber);
        jobEditText.setText(prefs.getString("job", "No job defined yet"));
        addressTextView.setText(prefs.getString("address", "No address defined yet"));
        latitude = Double.longBitsToDouble(prefs.getLong("latitude", 0));
        longitude = Double.longBitsToDouble(prefs.getLong("longitude", 0));
        locationTextView.setText(String.valueOf(latitude) + "," + String.valueOf(longitude));
    }

    /*public void initializeSpinners(){
        countrySpinner = (Spinner) findViewById(R.id.country_spinner);
        citySpinner = (Spinner) findViewById(R.id.city_spinner);

        Locale[] locales = Locale.getAvailableLocales();
        countries = new ArrayList<>();
        String country;
        final Set<String> countriesSet = new HashSet<>();
        for(Locale loc : locales) {
            country = loc.getDisplayCountry();
            if(country.length() > 0){
                countriesSet.add(country);
            }
        }
        countries.addAll(countriesSet);
        Collections.sort(countries, String.CASE_INSENSITIVE_ORDER);
        countries.add(0, "Select country");

        countrySpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, countries){
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent)
            {
                View v = null;
                // If this is the initial dummy entry, make it hidden
                if (position == 0) {
                    TextView tv = new TextView(getContext());
                    tv.setHeight(0);
                    tv.setVisibility(View.GONE);
                    v = tv;
                }
                else {
                    // Pass convertView as null to prevent reuse of special case views
                    v = super.getDropDownView(position, null, parent);
                }
                // Hide scroll bar because it appears sometimes unnecessarily, this does not prevent scrolling
                parent.setVerticalScrollBarEnabled(false);
                return v;
            }
        };
        countrySpinner.setAdapter(countrySpinnerAdapter);
        //countrySpinner.setPrompt("Select country");
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("Countries", "Selected country: " + countries.get(position));
                //Fill the citySpinner with the cities of the selected country
                if(countries.get(position).equals("Egypt")){
                    cities = new ArrayList<>();
                    cities.add("Select city");
                    cities.add("Cairo");
                    cities.add("Alexandria");
                    citySpinnerAdapter = new ArrayAdapter<String>(LocationServicesRegistration.this, android.R.layout.simple_spinner_dropdown_item, cities){
                        @Override
                        public View getDropDownView(int position, View convertView, ViewGroup parent)
                        {
                            View v = null;
                            // If this is the initial dummy entry, make it hidden
                            if (position == 0) {
                                TextView tv = new TextView(getContext());
                                tv.setHeight(0);
                                tv.setVisibility(View.GONE);
                                v = tv;
                            }
                            else {
                                // Pass convertView as null to prevent reuse of special case views
                                v = super.getDropDownView(position, null, parent);
                            }
                            // Hide scroll bar because it appears sometimes unnecessarily, this does not prevent scrolling
                            parent.setVerticalScrollBarEnabled(false);
                            return v;
                        }
                    };
                    citySpinner.setAdapter(citySpinnerAdapter);
                    //citySpinner.setPrompt("Select city");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){

            }
        });
    }*/

    public void openMapFragment(View view) {
        /*DialogFragment newFragment = new MapDialog();
        newFragment.show(getFragmentManager(), "locationPicker");*/

        DialogFragment newFragment = new MapDialog();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.add(newFragment, "locationPicker");
        ft.commit();
    }

    //Get the location the user selected from the mapfragment
    @Override
    public void onLocationSelected(double receivedLatitude, double receivedLongitude, double currentLatitude, double currentLongitude){

        if(receivedLatitude == 0 && receivedLongitude == 0) {
            Toast.makeText(LocationServicesRegistration.this, "No location selected, used current location instead.\n" +
                    "Select location from map again if you want to change your work location.", Toast.LENGTH_SHORT).show();
            latitude = currentLatitude;
            longitude = currentLongitude;
            locationTextView.setText(String.valueOf(latitude) + "," + String.valueOf(longitude));
        }
        else {
            latitude = receivedLatitude;
            longitude = receivedLongitude;
            locationTextView.setText(String.valueOf(latitude) + "," + String.valueOf(longitude));
        }

        userLocation = true;

        AddressDecoder aDecoder = new AddressDecoder();
        Location loc = new Location("");
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);
        aDecoder.execute(loc);
    }

    public void submitJobInfo(View view){

        if(userLocation){
            if(jobEditText.getText().toString().trim().length() > 0){
                job = jobEditText.getText().toString();
                //save and submit name, userName, phoneNumber, latitude, longitude, job, address

                //Send the info to the server
                sendInfoToServer();

                //save the info locally
                //registered means it won't ask again for this info
                SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
                SharedPreferences.Editor prefsEditor = prefs.edit();
                prefsEditor.putString("locationServicesRegistration", "registered");
                prefsEditor.putString("job", job);
                prefsEditor.putString("address", address);
                prefsEditor.putLong("latitude", Double.doubleToRawLongBits(latitude));
                prefsEditor.putLong("longitude", Double.doubleToRawLongBits(longitude));
                prefsEditor.apply();

                //update the textviews
                updateTextViews();
            }
            else{
                Toast.makeText(LocationServicesRegistration.this, "Please enter your job first", Toast.LENGTH_SHORT).show();
            }

        }
        else{
            Toast.makeText(LocationServicesRegistration.this, "Please select your work location first.", Toast.LENGTH_SHORT).show();
        }

    }

    public void sendInfoToServer(){
        //Send the message info to the server in a background thread
        String url = "http://"+SERVER_IP+":8080/MyFirstServlet/AddNewServiceProvider?name=" + URLEncoder.encode(name)
                + "&phoneNumber=" + URLEncoder.encode(phoneNumber)
                + "&userName=" + URLEncoder.encode(userName)
                + "&latitude=" + String.valueOf(latitude)
                + "&longitude=" + String.valueOf(longitude)
                + "&job=" + URLEncoder.encode(job)
                + "&address=" + URLEncoder.encode(address);
        //Request a string response from the provided URL.
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                //Go to MainPage automatically if this was the first time to register
                SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
                if(prefs.getBoolean("firstVisit", true)){
                    SharedPreferences.Editor prefsEditor = prefs.edit();
                    prefsEditor.putBoolean("firstVisit", false);
                    prefsEditor.apply();
                    Toast.makeText(LocationServicesRegistration.this, "Registered successfully", Toast.LENGTH_SHORT).show();
                    Intent goToMainPageIntent = new Intent(LocationServicesRegistration.this, MainPage.class);
                    startActivity(goToMainPageIntent);
                }
                Toast.makeText(LocationServicesRegistration.this, "Updated successfully", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LocationServicesRegistration.this, "Registration error", Toast.LENGTH_SHORT).show();
            }
        });
        //Add the request to the RequestQueue.
        HttpConnector.getInstance(this).addToRequestQueue(request);
    }

    public class AddressDecoder extends AsyncTask<Location,Void,String[]> {

        Geocoder geocoder = new Geocoder(LocationServicesRegistration.this, Locale.getDefault());
        String addressString = "";
        String detailedAddressString = "";

        @Override
        protected String[] doInBackground(Location... location) {
            try{
                Log.d("Address", "starting geocoding address...");
                List<Address> addresses = geocoder.getFromLocation(location[0].getLatitude(), location[0].getLongitude(), 1);
                Address address;
                detailedAddressString = "Number of addresses: " + addresses.size() + "\n";

                for(int x = 0; x < addresses.size(); x++)
                {
                    address = addresses.get(x);
                    detailedAddressString += "Address number " + x + "\n";
                    detailedAddressString += "Max index number: " + address.getMaxAddressLineIndex() +"\n";

                    for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        addressString += address.getAddressLine(i) + ", ";
                        detailedAddressString += "Line " + i + ":" + address.getAddressLine(i) + "\n";
                        publishProgress();
                    }

                    detailedAddressString += "Country: " + address.getCountryName() + "\n";
                    detailedAddressString += "City: " + address.getAddressLine(address.getMaxAddressLineIndex()-1) + "\n";
                    detailedAddressString += "Locality: " + address.getLocality() + "\n";
                    detailedAddressString += "AdminArea: " + address.getAdminArea() + "\n";
                    detailedAddressString += "SubAdminArea: " + address.getSubAdminArea() + "\n";

                    Log.d("Address", "Detailed address: " + detailedAddressString);
                    Log.d("Address", "Full address: " + addressString);
                }
            }
            catch (IOException io){
                Log.v("AddressGeocoder", "Error in reverse geocoding the address!");
            }
            return new String[]{addressString, detailedAddressString};
        }

        @Override
        protected void onPreExecute() {
            //Toast.makeText(LocationServicesRegistration.this, "Getting address...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(String[] result) {
            //result[0] is address string, result[1] is detailed address string
            //Toast.makeText(LocationServicesRegistration.this, result[1], Toast.LENGTH_LONG).show();
            address = result[0];
            addressTextView.setText(address);
        }

        @Override
        protected void onProgressUpdate (Void... values) {
            //Toast.makeText(LocationServicesRegistration.this, "Still getting address...", Toast.LENGTH_SHORT).show();
        }
    }
}
