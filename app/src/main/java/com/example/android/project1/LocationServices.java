package com.example.android.project1;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;
import java.util.List;


public class LocationServices extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    ListView job_list_view;
    JobsAdapter adapter;
    List<JobContent> list_jobs;
    EditText filter_edit_box;
    String longiude;
    String latitude;
    JobContent jobContent0, jobContent1, jobContent2, jobContent3, jobContent4, jobContent5, jobContent6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_services);
        setTitle("Location Services");

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(com.google.android.gms.location.LocationServices.API)
                    .build();
        }
        addJobs();

    }
    private void addJobs()
    {
        job_list_view=(ListView)findViewById(R.id.listview);
        filter_edit_box= (EditText)findViewById(R.id.filter);
        list_jobs= new ArrayList<>();
        adapter= new JobsAdapter(this,list_jobs);
        job_list_view.setAdapter(adapter);

        jobContent0 = new JobContent("Mechanic");
        list_jobs.add(jobContent0);

        jobContent1 = new JobContent("Doctor");
        list_jobs.add(jobContent1);

        jobContent2 = new JobContent("Electrician");
        list_jobs.add(jobContent2);

        jobContent3 = new JobContent("Pharmacy");
        list_jobs.add(jobContent3);

        jobContent4 = new JobContent("Police");
        list_jobs.add(jobContent4);

        jobContent5 = new JobContent("Coiffure");
        list_jobs.add(jobContent5);

        //jobContent6 = new JobContent("");
        //list_jobs.add(jobContent6);

        adapter.notifyDataSetChanged();

        job_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(LocationServices.this, LocationServicesResultsList.class);
                String clickeditem = list_jobs.get(position).getJob_name();
                intent.putExtra("key_clicked_item",clickeditem);
                intent.putExtra("key_longitude",longiude);
                intent.putExtra("key_latitude",latitude);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }
    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint){
        //Connected to Google Play services!
        //The good stuff goes here.
        mLocationRequest = new LocationRequest();
        mLocationRequest.setFastestInterval(60000);
        mLocationRequest.setInterval(3600000);
        //mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                com.google.android.gms.location.LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates states = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        mLastLocation = com.google.android.gms.location.LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        if (mLastLocation != null) {
                            //Save the long/lat in a string and send them to the LocationServicesResultsList activity
                            Toast.makeText(LocationServices.this, mLastLocation.getLongitude() + "," + mLastLocation.getLatitude(), Toast.LENGTH_LONG).show();
                            longiude= String.valueOf(mLastLocation.getLongitude());
                            latitude=String.valueOf(mLastLocation.getLatitude());
                        }
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    LocationServices.this,
                                    1);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    public void goToResultsPage(View view) {
        //Get the clicked service, and send it along with the location to the server
        Intent intent = new Intent(this, LocationServicesResultsList.class);
        startActivity(intent);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_location_services, menu);
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
