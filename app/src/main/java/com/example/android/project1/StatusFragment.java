package com.example.android.project1;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.net.URLEncoder;

/**
 * Created by fady on 5/9/2016.
 */
public class StatusFragment extends DialogFragment {

    String SERVER_IP;
    EditText statusEditText;
    Button postStatusButton;
    Button cancelButton;
    String status;
    static String statusUserName;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status, container);

        getLocalUserInfo();
        SERVER_IP = getServerIP();

        statusEditText = (EditText) view.findViewById(R.id.status_box);
        postStatusButton = (Button) view.findViewById(R.id.post_button);
        cancelButton = (Button) view.findViewById(R.id.cancel_button);
        postStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status = statusEditText.getText().toString();
                if(status.trim().length() > 0)
                    sendStatusToServer();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                getDialog().dismiss();
            }
        });

        return view;
    }

    private String getServerIP() {
        SharedPreferences tempPrefs = getActivity().getSharedPreferences("com.example.android.project1.NetworkPreferences", 0);
        return tempPrefs.getString("SERVER_IP", getResources().getString(R.string.server_ip_address));
    }

    private void getLocalUserInfo(){
        SharedPreferences prefs = getActivity().getSharedPreferences("com.example.android.project1.RegistrationPreferences", 0);
        statusUserName = prefs.getString("userName","Me");
    }

    public void sendStatusToServer() {
        String URL =  "http://"+SERVER_IP+":8080/MyFirstServlet/UpdateStatus?userName=" + URLEncoder.encode(statusUserName) + "&status=" + URLEncoder.encode(status);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("StatusFragment", "Status sent to server.");
                Log.i("VOLLEY", response);
                getDialog().dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("StatusFragment", "Error sending status to server.");
                Log.e("VOLLEY", error.toString());
            }
        });
        HttpConnector.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);
    }
}
