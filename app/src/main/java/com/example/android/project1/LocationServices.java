package com.example.android.project1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class LocationServices extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, SearchView.OnQueryTextListener{

    String SERVER_IP;

    //mGoogleApiClient is responsible for handling connections related to Google Play Services APIs
    GoogleApiClient mGoogleApiClient;

    //mLastLocation is used to store the last known location
    Location mLastLocation;

    //locationRequest object is responsible for handling the location request settings (refresh period and accuracy)
    LocationRequest locationRequest;

    //REQUEST_CHECK_SETTINGS is used for the dialog that will be shown if the Location Settings need to be adjusted
    static final int REQUEST_CHECK_SETTINGS = 1;

    ListView jobsListView;
    JobsAdapter adapter;
    List<JobContent> jobsList;
    String longitude;
    String latitude;

    private SearchView mSearchView;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_services);
        setTitle("Location Services");

        SERVER_IP = getServerIP();

        progressDialog = new ProgressDialog(LocationServices.this);
        progressDialog.setMessage("Getting info from server...");
        progressDialog.setProgressStyle(progressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //Initialize the mGoogleApiClient object if it's null, and make sure to pass LocationServices API parameter
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(com.google.android.gms.location.LocationServices.API)
                    .build();
        }

        //Build and initialize the LocationSettingsRequest/locationRequest objects and verify the device meets the required settings to get a GPS location
        buildLocationSettingsRequest();

        addJobs();

    }

    private String getServerIP() {
        SharedPreferences tempPrefs = getSharedPreferences("com.example.android.project1.NetworkPreferences", 0);
        return tempPrefs.getString("SERVER_IP", getResources().getString(R.string.server_ip_address));
    }

    private void buildLocationSettingsRequest() {

        //Initialize the locationRequest object and set the preferred interval to 1 seconds, and same for fastest interval
        //Also, use the HIGH_ACCURACY parameter to get a more precise location
        locationRequest = new LocationRequest();
        locationRequest.setInterval(3600000);
        locationRequest.setFastestInterval(60000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //Check whether the device's settings are enough to get a GPS position
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        final PendingResult<LocationSettingsResult> result =
                com.google.android.gms.location.LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates locationStates = result.getLocationSettingsStates();
                final int statusCode = status.getStatusCode();
                if(statusCode == LocationSettingsStatusCodes.SUCCESS)
                {
                    //Get the last known GPS location
                    mLastLocation = com.google.android.gms.location.LocationServices.FusedLocationApi.getLastLocation(
                            mGoogleApiClient);
                    if (mLastLocation != null) {
                        latitude = String.valueOf(mLastLocation.getLatitude());
                        longitude = String.valueOf(mLastLocation.getLongitude());
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"Please make sure your GPS is turned on and try again",Toast.LENGTH_SHORT).show();
                    }
                }
                else if(statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    //Show a dialog to change the Location Settings
                    try {
                        status.startResolutionForResult(LocationServices.this, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
                else if(statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                    Toast.makeText(LocationServices.this, "Unable to use location settings!", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private void addJobs()
    {
        jobsListView = (ListView) findViewById(R.id.listview);
        jobsList = new ArrayList<>();

        jobsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(LocationServices.this, LocationServicesResultsList.class);
                String clickedCategory = jobsList.get(position).getJobName();
                intent.putExtra("clickedCategory", clickedCategory);
                intent.putExtra("userLongitude", longitude);
                intent.putExtra("userLatitude", latitude);
                startActivity(intent);
            }
        });

        String url = "http://"+SERVER_IP+":8080/MyFirstServlet/GetAllServiceProviderCategories";
        //Request a response from the provided URL.
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
            Gson gson = new Gson();
            List<String> serverJobs = new ArrayList<>();
            @Override
            public void onResponse(String response) {
                if (response.length() > 0){
                    serverJobs = gson.fromJson(response, ArrayList.class);
                    if(serverJobs.isEmpty() == false){
                        Log.d("LocationServices", "Received jobs: " + serverJobs.toString());
                        int serverJobsSize = serverJobs.size();
                        for(int i = 0; i < serverJobsSize; i++) {
                            jobsList.add(new JobContent(serverJobs.get(i)));
                        }
                        Collections.sort(jobsList);
                        adapter = new JobsAdapter(LocationServices.this, jobsList);
                        jobsListView.setAdapter(adapter);
                        jobsListView.setTextFilterEnabled(true);
                    }
                    else{
                        Log.d("LocationServices", "Received non/empty list response: "+response.toString());
                        useDefaultJobs();
                    }
                }
                else {
                    Log.d("LocationServices", "Received an empty response from server.");
                    useDefaultJobs();
                }
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("LocationServices", "Volley error!");
                useDefaultJobs();
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
            }
        });
        //Add the request to the RequestQueue.
        HttpConnector.getInstance(this).addToRequestQueue(request);
    }

    private void useDefaultJobs() {
        jobsList.add(new JobContent("Mechanic"));
        jobsList.add(new JobContent("Electrician"));
        jobsList.add(new JobContent("Plumber"));
        jobsList.add(new JobContent("Doctor"));
        jobsList.add(new JobContent("Pharmacy"));
        jobsList.add(new JobContent("Police"));
        jobsList.add(new JobContent("Cook"));
        jobsList.add(new JobContent("Chef"));
        jobsList.add(new JobContent("Carpenter"));
        //probably add more here to cover more use cases,
        //and keep this synced with the server as well if possible
        Collections.sort(jobsList);
        adapter = new JobsAdapter(this, jobsList);
        jobsListView.setAdapter(adapter);
        jobsListView.setTextFilterEnabled(true);    }

    private void setupSearchView()
    {
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setQueryHint("Search here");
    }

    @Override
    public boolean onQueryTextChange(String newText)
    {
        Log.d("JOBS QUERY STRING", newText);
        if (TextUtils.isEmpty(newText.toLowerCase())) {
            jobsListView.clearTextFilter();
        } else {
            Log.d("JOBS","onQueryTextChange not empty");
            jobsListView.setFilterText(newText.toLowerCase());
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        Log.d("JOBS","onQueryTextSubmit YES");
        jobsListView.clearTextFilter();
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Connect to the Google Play Services
        mGoogleApiClient.connect();
    }
    @Override
    protected void onStop() {
        //Disconnect from the Google Play Services
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Stop listening for locations to save battery
        com.google.android.gms.location.LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle connectionHint){
        //Connected to Google Play services!
        //The good stuff goes here.
        //Start listening for location updates, which will be resolved in the LocationListener.onLocationChanged callback
        com.google.android.gms.location.LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int cause){
        //The connection has been interrupted.
        //Disable any UI components that depend on Google APIs until onConnected() is called.
        Toast.makeText(LocationServices.this, String.valueOf(cause), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result){
        // This callback is important for handling errors that
        // may occur while attempting to connect with Google.
        // More about this in the 'Handle Connection Failures' section.
        Toast.makeText(getApplicationContext(), "Connection to Google Play Services failed.", Toast.LENGTH_SHORT);
    }

    @Override
    public void onLocationChanged(Location location) {
        //This is the callback that will handle the current location of the user which is updated every 5 seconds
        mLastLocation = location;
        latitude = String.valueOf(mLastLocation.getLatitude());
        longitude = String.valueOf(mLastLocation.getLongitude());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_location_services, menu);
        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        setupSearchView();
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
