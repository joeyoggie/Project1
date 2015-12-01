package com.example.android.project1;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Joey on 11/22/2015.
 */

//AsyncTask that will handle the HTTP connections in a background thread
public class HttpConnector extends AsyncTask<String, Void, String>{

    protected String doInBackground(String... urls) {
        String result = null;
        try {
            result = downloadUrl(urls[0]);
        } catch (IOException e) {

        }
        return result;
    }

    protected void onPreExecute() {
    }

    protected void onPostExecute(String result) {
    }

    protected void onProgressUpdate(Void... value) {
    }

    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //conn.setReadTimeout(10000);
            //conn.setConnectTimeout(15000);
            //conn.setRequestMethod("GET");
            //conn.setDoInput(true);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(0);

            //conn.connect();


            MessageData message = new MessageData();
            JSONObject json = new JSONObject();
            try {
                json.put("messageObject",message);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //convert the json object to a string that will be written to OutputStream object
            json.toString();

            int response = conn.getResponseCode();
            Log.d("MainActivity", "The response is: " + response);
            publishProgress();

            is = conn.getInputStream();

            int len = 500;
            String result = null;

            Reader reader = new InputStreamReader(is, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            result = new String(buffer);

            return result;

        } finally {
            if (is != null) {
                is.close();
            }
        }
    }//End of downloadUrl method
}