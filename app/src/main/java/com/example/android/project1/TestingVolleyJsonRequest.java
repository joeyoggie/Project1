package com.example.android.project1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TestingVolleyJsonRequest extends AppCompatActivity {

    ArrayList array;
    TextView responseTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing_volley_json_request);

        responseTextView = (TextView) findViewById(R.id.response_dynamic_text_view);


    }

    public void sendToServer(View view) {
        responseTextView.setText("Waiting for response...");

        //Send the data to the server in a background thread
        //Instantiate the RequestQueue.
        String url = "http://192.168.1.44:8080/MyFirstServlet/TestingVolley";
        //Request a response from the provided URL.
        StringRequest request1 = new StringRequest(url, new Response.Listener<String>(){
            Gson gson = new Gson();
            ArrayList<String> numbers;
            int i;
            @Override
            public void onResponse(String response) {
                if(response.length() > 0) {
                    numbers = gson.fromJson(response.toString(), ArrayList.class);
                    responseTextView.setText(numbers.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseTextView.setText("Volley error!");
            }
        });

        StringRequest sr = new StringRequest(Request.Method.POST, url , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.d(TAG, response.toString());
                responseTextView.setText(response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //VolleyLog.d(TAG, "Error: " + error.getMessage());
                //Log.d(TAG, "" + error.getMessage() + "," + error.toString());
                responseTextView.setText("Volley error!");
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                params.put("first_param", "28");
                params.put("second_param", "1");

                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String, String>();
                headers.put("Content-Type","application/x-www-form-urlencoded");
                headers.put("abc", "value");
                return headers;
            }
        };

        ArrayList<String> phoneNumbers = new ArrayList<>();
        phoneNumbers.add("01099824282");
        phoneNumbers.add("01000977474");
        phoneNumbers.add("02");
        JSONArray jsonArray = new JSONArray(phoneNumbers);

        JsonArrayRequest requestArray = new JsonArrayRequest(Request.Method.POST, url, jsonArray, new Response.Listener<JSONArray>(){
            Gson gson = new Gson();
            ArrayList<String> receivedNumbers;
            int i;
            @Override
            public void onResponse(JSONArray response) {
                if(response.length() > 0) {
                    receivedNumbers = gson.fromJson(response.toString(), ArrayList.class);
                    if(receivedNumbers.isEmpty() == false)
                        responseTextView.setText(receivedNumbers.toString());
                    else
                        responseTextView.setText("No numbers returned.");
                }
                else {
                    responseTextView.setText("Received an empty JSON string.");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseTextView.setText("Volley error!");
            }
        }){
            /*@Override
            public byte[] getBody() {
                Map<String, String> params = new HashMap<>();
                try {
                    params.put("tempParameter", "BEDANNN");
                    return params.toString() == null ? null : params.toString().getBytes(PROTOCOL_CHARSET);
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                            params.toString(), PROTOCOL_CHARSET);
                    return null;
                }
            }*/
            /*@Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("tempParameter", "BEDANNN");
                return params;
            }*/
            /*@Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //params.put("Content-Type", "application/json; charset=utf-8");
                params.put("tempHeader", jsonArray.toString());
                return params;
            }*/

        };

        //Add the request to the RequestQueue.
        HttpConnector.getInstance(this).addToRequestQueue(requestArray);
    }
}
