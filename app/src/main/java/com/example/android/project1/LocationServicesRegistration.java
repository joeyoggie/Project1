package com.example.android.project1;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
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

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationServicesRegistration extends AppCompatActivity implements MapDialog.OnLocationSelectedListener {

    String SERVER_IP;
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
            jobEditText.setText(prefs.getString("job", "No job defined"));
            addressTextView.setText(prefs.getString("address", "No address defined"));
            latitude = Double.longBitsToDouble(prefs.getLong("latitude", 0));
            longitude = Double.longBitsToDouble(prefs.getLong("longitude", 0));
            locationTextView.setText(String.valueOf(latitude) + "," + String.valueOf(longitude));
            addJobButton.setText("Update job info!");
        }

        ArrayList<String> sugestedJobs = new ArrayList<>();
        //TODO get all the available jobs from the server
        sugestedJobs.add("Mechanic");
        sugestedJobs.add("Electrician");
        sugestedJobs.add("Plumber");
        sugestedJobs.add("Doctor");
        sugestedJobs.add("Pharmacy");
        sugestedJobs.add("Police");
        sugestedJobs.add("Cook/Chef");
        sugestedJobs.add("Carpenter");

        ArrayAdapter<String> suggestedJobsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sugestedJobs);
        jobEditText.setAdapter(suggestedJobsAdapter);
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

    private void updateTextViews() {
        SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
        nameTextView.setText(name);
        userNameTextView.setText("@"+userName);
        phoneNumberTextView.setText(phoneNumber);
        jobEditText.setText(prefs.getString("job", "No job defined"));
        addressTextView.setText(prefs.getString("address", "No address defined"));
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
    public void onLocationSelected(double receivedLatitude, double receivedLongitude){
        Toast.makeText(LocationServicesRegistration.this, String.valueOf(receivedLatitude)+","+String.valueOf(receivedLongitude), Toast.LENGTH_LONG).show();

        latitude = receivedLatitude;
        longitude = receivedLongitude;
        locationTextView.setText(String.valueOf(latitude) + "," + String.valueOf(longitude));

        AddressDecoder aDecoder = new AddressDecoder();
        Location loc = new Location("");
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);
        aDecoder.execute(loc);
    }

    public void submitJobInfo(View view){
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
                Toast.makeText(LocationServicesRegistration.this, "Registered successfully", Toast.LENGTH_SHORT).show();
                //Go to MainPage automatically if this was the first time to register
                SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
                if(prefs.getBoolean("firstVisit", true)){
                    SharedPreferences.Editor prefsEditor = prefs.edit();
                    prefsEditor.putBoolean("firstVisit", false);
                    prefsEditor.apply();
                    Intent goToMainPageIntent = new Intent(LocationServicesRegistration.this, MainPage.class);
                    startActivity(goToMainPageIntent);
                }
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
