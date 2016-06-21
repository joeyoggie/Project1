package com.example.android.project1;

import android.app.Activity;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Joey on 6/16/2016.
 */
public class MapDialog extends android.app.DialogFragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {

    //Default value set to Cairo, Egypt (will be overriden on app launch anyway)
    static double latitude = 30.048865;
    static double longitude = 31.235290;

    static double selectedLatitude = 0;
    static double selectedLongitude = 0;

    //mGoogleApiClient is responsible for handling connections related to Google Play Services APIs
    private GoogleApiClient mGoogleApiClient;

    //locationRequest object is responsible for handling the location request settings (refresh period and accuracy)
    LocationRequest locationRequest;

    //REQUEST_CHECK_SETTINGS is used for the dialog that will be shown if the Location Settings need to be adjusted
    static final int REQUEST_CHECK_SETTINGS = 1;

    //mLastLocation is used to store the last known location
    Location mLastLocation;

    //integer to indicate the view of the map (0 = street, 1 = satellite)
    int mapView = 0;
    private GoogleMap mMap;
    MapFragment mapFragment;
    boolean firstLaunch = true;

    private static View view;

    //An interface to allow the fragment to communivate with the parent activity that created it,
    //which must implement this interface
    OnLocationSelectedListener mCallback;
    public interface OnLocationSelectedListener {
        void onLocationSelected(double latitude, double longitude, double latitude2, double longitude2);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnLocationSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try{
            view = inflater.inflate(R.layout.map_dialog, container, false);
        }
        catch(InflateException e){
            Log.d("MapDialog", e.toString());
        }

        //Initialize the mGoogleApiClient object if it's null, and make sure to pass LocationServices API parameter
        if(mGoogleApiClient ==  null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(com.google.android.gms.location.LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        //Build and initialize the LocationSettingsRequest/locationRequest objects and verify the device meets the required settings to get a GPS location
        buildLocationSettingsRequest();

        mapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);

        Button saveLocationButton = (Button) view.findViewById(R.id.save_location_button);
        saveLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass the location to the parent activity
                mCallback.onLocationSelected(selectedLatitude, selectedLongitude, latitude, longitude);
                getDialog().dismiss();
            }
        });

        Button changeToMapViewButton = (Button) view.findViewById(R.id.change_to_map_view_button);
        changeToMapViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView = 0;
                //mapFragment.getMapAsync(MapDialog.this);
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            }
        });

        Button changeToSatelliteViewButton = (Button) view.findViewById(R.id.change_to_satellite_view_button);
        changeToSatelliteViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView = 1;
                //mapFragment.getMapAsync(MapDialog.this);
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

            }
        });

        return view;
    }

    private void buildLocationSettingsRequest() {

        //Initialize the locationRequest object and set the preferred interval to 1 seconds, and same for fastest interval
        //Also, use the HIGH_ACCURACY parameter to get a more precise location
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
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
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                            mGoogleApiClient);
                    if (mLastLocation != null) {
                        latitude = mLastLocation.getLatitude();
                        longitude = mLastLocation.getLongitude();
                    }
                    else {
                        Toast.makeText(getActivity().getApplicationContext(),"Please make sure your GPS is turned on",Toast.LENGTH_SHORT).show();
                    }
                }
                else if(statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    //Show a dialog to change the Location Settings
                    try {
                        status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
                else if(statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                    Toast.makeText(getActivity().getApplicationContext(), "Unable to use location settings!", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Connected to Google Play services!
        // The good stuff goes here.
        //Start listening for location updates, which will be resolved in the LocationListener.onLocationChanged callback
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // This callback is important for handling errors that
        // may occur while attempting to connect with Google.
        //
        // More about this in the 'Handle Connection Failures' section.
        Toast.makeText(getActivity().getApplicationContext(), "Connection to Google Play Services failed.", Toast.LENGTH_SHORT);
    }

    @Override
    public void onLocationChanged(Location location) {
        //This is the callback that will handle the current location of the user which is updated every 5 seconds
        mLastLocation = location;
        latitude = mLastLocation.getLatitude();
        longitude = mLastLocation.getLongitude();
        if(firstLaunch){
            mapFragment.getMapAsync(this);
            firstLaunch = false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //Connect to the Google Play Services
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        //Disconnect from the Google Play Services
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.map_view)).commit();

        /*FragmentManager fragManager = this.getFragmentManager();
        Fragment fragment = fragManager.findFragmentById(R.id.map_view);
        if(fragment!=null){
            fragManager.beginTransaction().remove(fragment).commit();
        }*/

        /*Fragment fragment = getFragmentManager().findFragmentById(R.id.map_view);
        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
        ft.remove(fragment);
        ft.commit();*/
    }

    @Override
    public void onPause() {
        super.onPause();
        //Stop listening for locations to save battery
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onMapReady(GoogleMap map){
        mMap = map;
        //Change the map view depending which menu option was clicked last
        if(mapView == 0) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        if(mapView == 1){
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
        //Show the user location on the map
        mMap.setMyLocationEnabled(true);

        //Show the zoom controls
        mMap.getUiSettings().setZoomControlsEnabled(true);

        //Change the marker position when the user clicks on the map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
            Marker m;
            @Override
            public void onMapClick(LatLng point){
                //mMap.clear();
                if(m!=null) {
                    m.setPosition(new LatLng(point.latitude, point.longitude));
                }
                else{
                    m = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(point.latitude, point.longitude))
                            .snippet("Click Done if this is the correct location")
                            .title("Work location")
                /*.icon(BitmapDescriptorFactory.fromResource(R.drawable.place_a_work_icon_here))*/
                            .flat(true)
                    );
                }
                m.showInfoWindow();
                selectedLatitude = point.latitude;
                selectedLongitude = point.longitude;
            }
        });

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
        /*if(firstLaunch){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
            firstLaunch = false;
        }*/
    }
}
