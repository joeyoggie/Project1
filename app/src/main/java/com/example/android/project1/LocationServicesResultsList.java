package com.example.android.project1;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LocationServicesResultsList extends AppCompatActivity {

    ProgressDialog progressDialog;
    String latitude, longtitude, job;
    List<String> list_of_first_requests;
    List <ServiceProvider> recieved_list ;
    LocationServiceResultsAdapter locationServiceResultsAdapter ;
    ListView list_result;

    TextView numberOfResponses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_services_results_list);

        numberOfResponses = (TextView) findViewById(R.id.number_of_results);

        progressDialog = new ProgressDialog(LocationServicesResultsList.this);
        progressDialog.setMessage("Performing your request...");
        progressDialog.setProgressStyle(progressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        list_of_first_requests = new ArrayList<>();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            latitude = bundle.getString("key_latitude");
            longtitude = bundle.getString("key_longitude");
            job = bundle.getString("key_clicked_item");

            list_of_first_requests.add(latitude);
            list_of_first_requests.add(longtitude);
            list_of_first_requests.add(job);
        }
        send_request_to_server(list_of_first_requests);
    }

    public void send_request_to_server(List<String> list_of_requests) {

        String url = "http://192.168.1.44:8080/MyFirstServlet/GetLocalServices?serviceCategory="+list_of_requests.get(2)+"&userLongitude="+list_of_requests.get(1)+"&userLatitude="+list_of_requests.get(0);

        JSONArray jsonArray = new JSONArray(list_of_requests);
        //Request a response from the provided URL.
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST,url,jsonArray, new Response.Listener<JSONArray>() {
            Gson gson = new Gson();
            @Override
            public void onResponse(JSONArray response) {
                if (response.length()>0){
                    recieved_list= new ArrayList<>();
                    list_result = (ListView)findViewById(R.id.list_result);
                    Type type = new TypeToken<List<ServiceProvider>>(){}.getType();
                    recieved_list= gson.fromJson(response.toString(), type);
                    if(recieved_list.isEmpty() == false){
                        locationServiceResultsAdapter = new LocationServiceResultsAdapter(LocationServicesResultsList.this,recieved_list);
                        list_result.setAdapter(locationServiceResultsAdapter);
                        //locationServiceResultsAdapter.notifyDataSetChanged();
                        Log.d("RECEIVED DATA", String.valueOf(recieved_list.size()));
                        numberOfResponses.setText("Found " + recieved_list.size() + " results.");
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
    }
}
