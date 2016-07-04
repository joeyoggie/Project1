package com.example.android.project1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageMultiPartRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class UserProfile extends ActionBarActivity {

    String SERVER_IP;
    TextView userNameTextView, statusTextView, phoneNumberTextView;
    ImageView profilePictureImageView;
    Button sendMessageButton, changeProfilePictureButton;
    String receivedUserName, receivedPhoneNumber, receivedStatus, receivedName, localUserName, deviceID;
    Bitmap receivedProfilePicture;

    int SELECT_PICTURE = 44;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        SERVER_IP = getServerIP();
        getLocalUserInfo();

        sendMessageButton = (Button) findViewById(R.id.send_message_button);
        userNameTextView = (TextView) findViewById(R.id.profileusername);
        statusTextView = (TextView) findViewById(R.id.profieStatus);
        phoneNumberTextView = (TextView) findViewById(R.id.profilePhoneNo);
        profilePictureImageView = (ImageView) findViewById(R.id.userpp);
        changeProfilePictureButton = (Button) findViewById(R.id.change_profile_picture_button);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            receivedUserName = bundle.getString("userName");
            receivedName = bundle.getString("name");
            receivedPhoneNumber = bundle.getString("phoneNumber");
            setupActionBar();
            if(receivedUserName.equals(localUserName)){
                sendMessageButton.setVisibility(View.GONE);
                changeProfilePictureButton.setVisibility(View.VISIBLE);
            }
            getUpdatedInfoFromServer(receivedUserName);
            downloadProfilePicture(receivedUserName);
        }
    }

    private void setupActionBar() {
        getSupportActionBar().setTitle(receivedName);
        getSupportActionBar().setDisplayUseLogoEnabled(true); //Enable the Logo to be shown
        getSupportActionBar().setDisplayShowHomeEnabled(true); //Show the Logo
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Show the Up/Back arrow
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
    }

    private void getLocalUserInfo(){
        SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
        localUserName = prefs.getString("userName","Me");
        deviceID = prefs.getString("deviceUUID","0");
    }

    private String getServerIP() {
        SharedPreferences tempPrefs = getSharedPreferences("com.example.android.project1.NetworkPreferences", 0);
        return tempPrefs.getString("SERVER_IP", getResources().getString(R.string.server_ip_address));
    }

    public void getUpdatedInfoFromServer(String userName) {
        String URL = "http://"+SERVER_IP+":8080/MyFirstServlet/GetUserInfo?userName=" + URLEncoder.encode(userName);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("UserProfile", response);
                if(response.length() >= 3){ //empty response contains just 2 empty json brackets, so we check for more than 2 chars
                    Gson gson = new Gson();
                    Type type = new TypeToken<User>(){}.getType();
                    User user = gson.fromJson(response, type);
                    receivedStatus = user.getStatus();
                    receivedPhoneNumber = user.getPhoneNumber();
                    receivedUserName = user.getUserName();
                    receivedName = user.getName();
                    //get image from received user object
                    userNameTextView.setText("@"+receivedUserName);
                    if(receivedStatus != null && receivedStatus.length() > 1) {
                        statusTextView.setText(receivedStatus);
                    }
                    else{
                        statusTextView.setText("-");
                    }
                    phoneNumberTextView.setText(receivedPhoneNumber);
                    getSupportActionBar().setTitle(receivedName);
                }
                else{
                    userNameTextView.setText("@"+receivedUserName);
                    statusTextView.setText("-");
                    phoneNumberTextView.setText(receivedPhoneNumber);
                    getSupportActionBar().setTitle(receivedName);
                    //use local image stored in sqlite database
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY", error.toString());
                userNameTextView.setText("@"+receivedUserName);
                statusTextView.setText("-");
                phoneNumberTextView.setText(receivedPhoneNumber);
                getSupportActionBar().setTitle(receivedName);
                //use local image stored in sqlite database
            }
        });
        HttpConnector.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    public void goToChatPage(View view) {
        Intent intent = new Intent(this, ChatPage.class);
        intent.putExtra("recepientName",receivedName);
        intent.putExtra("recepientUserName",receivedUserName);
        startActivity(intent);
    }

    public void changeProfilePicture(View view){
        loadImageFromGallery();
    }

    public void loadImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);

        /*Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("ChatPage", "onActivityResult");
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE){
                if(data != null){
                    String selectedImagePath = null;
                    Uri selectedImageUri = data.getData();
                    String[] projection = { MediaStore.MediaColumns.DATA };
                    //String[] projection = { MediaStore.Images.Media.DATA };
                    //Cursor cursor = managedQuery(selectedImageUri, projection, null, null, null);
                    Cursor cursor = this.getContentResolver().query(selectedImageUri, projection, null, null, null);
                    if(cursor != null){
                        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                        //int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        cursor.moveToFirst();
                        selectedImagePath = cursor.getString(column_index);
                    }
                    else{
                        selectedImagePath = selectedImageUri.getPath();
                    }

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(selectedImagePath, options);
                    final int REQUIRED_SIZE = 1000;
                    int scale = 1;
                    while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                            && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                        scale *= 2;
                    options.inSampleSize = scale;
                    options.inJustDecodeBounds = false;
                    Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath, options);
                    /*ImageView imgView = (ImageView) findViewById(R.id.test_image);
                    imgView.setImageBitmap(bitmap);*/
                    sendImage(bitmap, selectedImagePath);
                }
                else{
                    Log.d("ChatPage", "ResultCode= " + resultCode + " & RequestCode= " +requestCode);
                    Log.d("ChatPage", "Intent 'data' is null.");
                    Toast.makeText(UserProfile.this, "Image null error!", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Log.d("ChatPage", "ResultCode= " + resultCode + " & RequestCode= " +requestCode);
                Toast.makeText(UserProfile.this, "Unable to select image. \"" + "ResultCode= " + resultCode + " & RequestCode= " + requestCode, Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Log.d("ChatPage", "ResultCode= " + resultCode + " & RequestCode= " +requestCode);
            Toast.makeText(UserProfile.this, "Unable to select image. \"" + "ResultCode= " +resultCode + " & RequestCode= " + requestCode, Toast.LENGTH_SHORT).show();
        }
    }

    public void sendImage(final Bitmap imageBitmap, String imageName){
        final ProgressDialog progressDialog = ProgressDialog.show(this,"Uploading image...","Please wait...",false,false);
        progressDialog.setCanceledOnTouchOutside(false);
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timestamp = simpleDateFormat.format(date);

        String url = "http://" + SERVER_IP + ":8080/MyFirstServlet/AddNewProfilePicture?userName="+localUserName+"&timestamp="+ timestamp;

        final String boundary = "begBoundary-" + deviceID + localUserName + deviceID;
        final String mimeType = "multipart/form-data; boundary=" + boundary;
        //Convert image into multipart byte[]
        byte[] multipartBody = getMultiPartDataFromBitmap(imageBitmap, imageName);

        ByteArrayOutputStream imageByteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 0, imageByteArrayOutputStream);
        final byte[] imageByteArray = imageByteArrayOutputStream.toByteArray();

        ImageMultiPartRequest multipartRequest = new ImageMultiPartRequest(url, null, mimeType, multipartBody, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                Toast.makeText(UserProfile.this, "Image sent successfully!", Toast.LENGTH_SHORT).show();
                profilePictureImageView.setImageBitmap(imageBitmap);
                //Dismiss the progress dialog
                progressDialog.dismiss();
                Log.d("UserProfile", "Image uploaded successfully.");
                Log.d("UserProfile", "Volley respose: " + response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(UserProfile.this, "Image upload failed!\r\n" + error.toString(), Toast.LENGTH_SHORT).show();
                //Dismiss the progress dialog
                progressDialog.dismiss();
                Log.d("UserProfile", "Volley error: " + error.toString());
            }
        });
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Adding request to the queue
        HttpConnector.getInstance(this).addToRequestQueue(multipartRequest);
    }

    private byte[] getMultiPartDataFromBitmap(Bitmap imageBitmap, String imageName){
        final String twoHyphens = "--";
        final String lineEnd = "\r\n";
        final String boundary = "begBoundary-" + deviceID + localUserName + deviceID;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageData = byteArrayOutputStream.toByteArray();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            //dataOutputStream.writeBytes("Content-Type: image/png; charset=UTF-8" + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"image_file\"; filename=\""
                    + imageName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            ByteArrayInputStream fileInputStream = new ByteArrayInputStream(imageData);
            int bytesAvailable = fileInputStream.available();

            int maxBufferSize = 1024 * 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];

            //read file and write it into form...
            int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }
            dos.writeBytes(lineEnd);
            //send multipart form data necesssary after file data
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bos.toByteArray();
    }

    public void downloadProfilePicture(String userName){
        ImageDownloader dImage = new ImageDownloader();
        dImage.execute(userName);
    }

    private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... userNames){
            Bitmap profilePicture = null;
            String urlString = "http://"+SERVER_IP+":8080/MyFirstServlet/GetProfilePicture?userName=" + userNames[0];
            try {
                profilePicture = BitmapFactory.decodeStream((InputStream)new URL(urlString).getContent());

                /*ByteArrayOutputStream bos = new ByteArrayOutputStream();
                profilePicture.compress(Bitmap.CompressFormat.PNG, 100, bos);*/


                //imageData = BitmapFactory.decodeStream((InputStream)new URL(urlString[0]).getContent());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return profilePicture;
        }

        @Override
        protected void onPostExecute(Bitmap profilePicture){
            if(profilePicture != null){
                profilePictureImageView.setImageBitmap(profilePicture);
            }
            else{
                profilePictureImageView.setImageResource(R.drawable.image_error);
            }
        }
    }
}
