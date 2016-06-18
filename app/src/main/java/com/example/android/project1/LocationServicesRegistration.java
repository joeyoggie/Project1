package com.example.android.project1;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
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

    Spinner countrySpinner, citySpinner;
    ArrayList<String> countries, cities;
    ArrayAdapter<String> countrySpinnerAdapter, citySpinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_services_registration);

        SERVER_IP = getServerIP();
        getLocalUserInfo();

        //initializeSpinners();

        jobEditText = (AutoCompleteTextView) findViewById(R.id.job_category_edit_text);
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

        AddressDecoder aDecoder = new AddressDecoder();
        Location loc = new Location("");
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);
        aDecoder.execute(loc);
    }

    public void submitJobInfo(View view){
        job = jobEditText.getText().toString();

        //submit name, userName, phoneNumber, job, latitude, longitude, address to the server
        String test = "Name: " + name + "\n"
                + "Username: " + userName + "\n"
                + "Phone number:" + phoneNumber + "\n"
                + "Job: " + job + "\n"
                + "Location: " + latitude + "," + longitude + "\n"
                + "Address: " + address;
        Toast.makeText(LocationServicesRegistration.this, test, Toast.LENGTH_SHORT).show();

        //TODO save the registered info and upload them to the server
        //when finished
        //registered means it won't ask again for this info
        /*SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString("locationServicesRegistration", "registered");
        //add other registered info
        prefsEditor.apply();*/
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
            //Toast.makeText(LocationServicesRegistration.this, result, Toast.LENGTH_LONG).show();
            TextView addressTextView = (TextView) findViewById(R.id.address_text_view);
            addressTextView.setText("Address: " + result[0]);
            TextView detailedAddressTextView = (TextView) findViewById(R.id.detailed_address_text_view);
            detailedAddressTextView.setText("Detailed info:" + "\n" + result[1]);
            address = addressString;
        }

        @Override
        protected void onProgressUpdate (Void... values) {
            //Toast.makeText(LocationServicesRegistration.this, "Still getting address...", Toast.LENGTH_SHORT).show();
        }
    }
}
