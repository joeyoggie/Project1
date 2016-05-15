package com.example.android.project1;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LocationServicesResultsList extends AppCompatActivity {

    String SERVER_IP;
    ProgressDialog progressDialog;
    String latitude, longitude, serviceCategory;
    List<ServiceProvider> receivedServiceProviders;
    LocationServiceResultsAdapter locationServiceResultsAdapter;
    ListView resultListView;

    TextView numberOfResponses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_services_results_list);

        SERVER_IP = getServerIP();

        numberOfResponses = (TextView) findViewById(R.id.number_of_results);

        progressDialog = new ProgressDialog(LocationServicesResultsList.this);
        progressDialog.setMessage("Performing your request...");
        progressDialog.setProgressStyle(progressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            latitude = bundle.getString("userLatitude");
            longitude = bundle.getString("userLongitude");
            serviceCategory = bundle.getString("clickedCategory");
        }

        getServiceProviders(latitude, longitude, serviceCategory);
    }

    private String getServerIP() {
        SharedPreferences tempPrefs = getSharedPreferences("com.example.android.project1.NetworkPreferences", 0);
        return tempPrefs.getString("SERVER_IP", getResources().getString(R.string.server_ip_address));
    }

    public void getServiceProviders(String latitude, String longitude, String serviceCategory) {

        String url = "http://"+SERVER_IP+":8080/MyFirstServlet/GetLocalServices?serviceCategory="+serviceCategory+"&userLongitude="+longitude+"&userLatitude="+latitude;

        JSONArray jsonArray = new JSONArray();
        //Request a response from the provided URL.
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
            Gson gson = new Gson();
            @Override
            public void onResponse(String response) {
                if (response.length()>0){
                    receivedServiceProviders= new ArrayList<>();
                    resultListView = (ListView) findViewById(R.id.results_list);
                    Type type = new TypeToken<List<ServiceProvider>>(){}.getType();
                    receivedServiceProviders= gson.fromJson(response.toString(), type);
                    if(receivedServiceProviders.isEmpty() == false){
                        locationServiceResultsAdapter = new LocationServiceResultsAdapter(LocationServicesResultsList.this, receivedServiceProviders);
                        resultListView.setAdapter(locationServiceResultsAdapter);
                        //locationServiceResultsAdapter.notifyDataSetChanged();
                        Log.d("RECEIVED DATA", String.valueOf(receivedServiceProviders.size()));
                        numberOfResponses.setText("Found " + receivedServiceProviders.size() + " results.");
                        progressDialog.dismiss();
                    }
                    else{
                        Log.d("RECEIVED DATA", response.toString());
                        numberOfResponses.setText("Found " + 0 + " results.");
                        progressDialog.dismiss();
                    }
                }
                else {
                    Log.d("RECEIVED DATA", "Received an empty response from server.");
                    numberOfResponses.setText("Found " + 0 + " results.");
                    progressDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ContactsListView", "Volley error!");
                numberOfResponses.setText("Internet connection error.");
                progressDialog.dismiss();
            }
        });
        //Add the request to the RequestQueue.
        HttpConnector.getInstance(this).addToRequestQueue(request);

        /*
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, url, jsonArray, new Response.Listener<JSONArray>() {
            Gson gson = new Gson();
            @Override
            public void onResponse(JSONArray response) {
                if (response.length()>0){
                    receivedServiceProviders= new ArrayList<>();
                    resultListView = (ListView) findViewById(R.id.results_list);
                    Type type = new TypeToken<List<ServiceProvider>>(){}.getType();
                    receivedServiceProviders= gson.fromJson(response.toString(), type);
                    if(receivedServiceProviders.isEmpty() == false){
                        locationServiceResultsAdapter = new LocationServiceResultsAdapter(LocationServicesResultsList.this, receivedServiceProviders);
                        resultListView.setAdapter(locationServiceResultsAdapter);
                        //locationServiceResultsAdapter.notifyDataSetChanged();
                        Log.d("RECEIVED DATA", String.valueOf(receivedServiceProviders.size()));
                        numberOfResponses.setText("Found " + receivedServiceProviders.size() + " results.");
                        progressDialog.dismiss();
                    }
                    else{
                        Log.d("RECEIVED DATA", response.toString());
                        numberOfResponses.setText("Found " + 0 + " results.");
                        progressDialog.dismiss();
                    }
                }
                else {
                    Log.d("RECEIVED DATA", "Received an empty response from server.");
                    numberOfResponses.setText("Found " + 0 + " results.");
                    progressDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ContactsListView", "Volley error!");
                numberOfResponses.setText("Internet connection error.");
                progressDialog.dismiss();
            }
        });
        HttpConnector.getInstance(this).addToRequestQueue(jsonArrayRequest);
*/
    }
}
