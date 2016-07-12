package com.example.android.project1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageMultiPartRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DrawingFragment extends DialogFragment {


    private DrawingView drawView;
    private ImageButton currPaint, drawBtn, eraseBtn, /*newBtn, saveBtn,*/ opacityBtn;
    private Button newBtn, saveBtn, cancelBtn;
    //sizes
    private float smallBrush, mediumBrush, largeBrush;
    Bitmap imageBitmap;

    String SERVER_IP;
    String deviceID,userName, recepientUserName;
    String imagePath;
    long imageID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_drawing, container, false);

        SERVER_IP = getServerIP();
        getLocalUserInfo();
        recepientUserName = ChatPage.recepientUserName;

        try{

            drawView = (DrawingView) view.findViewById(R.id.drawing);
            LinearLayout paintLayout = (LinearLayout) view.findViewById(R.id.paint_colors);
            currPaint = (ImageButton) paintLayout.getChildAt(0);
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            //sizes from dimensions[
            smallBrush = getResources().getInteger(R.integer.small_size);
            mediumBrush = getResources().getInteger(R.integer.medium_size);
            largeBrush = getResources().getInteger(R.integer.large_size);

            ImageButton red = (ImageButton) view.findViewById(R.id.red);
            red.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    drawView.setErase(false);
                    drawView.setPaintAlpha(100);
                    drawView.setBrushSize(drawView.getLastBrushSize());

                    if(view!=currPaint){
                        ImageButton imgView = (ImageButton)view;
                        String color = view.getTag().toString();
                        drawView.setColor(color);
                        //update ui
                        imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
                        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
                        currPaint=(ImageButton)view;
                    }}
            });

            //draw button
            drawBtn = (ImageButton) view.findViewById(R.id.draw_btn);
            drawBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog brushDialog = new Dialog(getActivity());
                    brushDialog.setTitle("Brush size:");
                    brushDialog.setContentView(R.layout.brush_chooser);
                    //listen for clicks on size buttons
                    ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
                    smallBtn.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            drawView.setErase(false);
                            drawView.setBrushSize(smallBrush);
                            drawView.setLastBrushSize(smallBrush);
                            brushDialog.dismiss();
                        }});
                    ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
                    mediumBtn.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            drawView.setErase(false);
                            drawView.setBrushSize(mediumBrush);
                            drawView.setLastBrushSize(mediumBrush);
                            brushDialog.dismiss();
                        }
                    });
                    ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
                    largeBtn.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            drawView.setErase(false);
                            drawView.setBrushSize(largeBrush);
                            drawView.setLastBrushSize(largeBrush);
                            brushDialog.dismiss();
                        }
                    });
                    //show and wait for user interaction
                    brushDialog.show();
                }
            });

            //set initial size
            drawView.setBrushSize(mediumBrush);

            //erase button
            eraseBtn = (ImageButton) view.findViewById(R.id.erase_btn);
            eraseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog brushDialog = new Dialog(getActivity());
                    brushDialog.setTitle("Eraser size:");
                    brushDialog.setContentView(R.layout.brush_chooser);
                    //size buttons
                    ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
                    smallBtn.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            drawView.setErase(true);
                            drawView.setBrushSize(smallBrush);
                            brushDialog.dismiss();
                        }
                    });
                    ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
                    mediumBtn.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            drawView.setErase(true);
                            drawView.setBrushSize(mediumBrush);
                            brushDialog.dismiss();
                        }
                    });
                    ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
                    largeBtn.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            drawView.setErase(true);
                            drawView.setBrushSize(largeBrush);
                            brushDialog.dismiss();
                        }
                    });
                    brushDialog.show();
                }
            });

            //new button
            newBtn = (Button) view.findViewById(R.id.new_btn);
            newBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder newDialog = new AlertDialog.Builder(getActivity());
                    newDialog.setTitle("New drawing");
                    newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
                    newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which){
                            drawView.startNew();
                            dialog.dismiss();
                        }
                    });
                    newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which){
                            dialog.cancel();
                        }
                    });
                    newDialog.show();
                }
            });

            //save button
            saveBtn = (Button) view.findViewById(R.id.save_btn);
            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    drawView.setDrawingCacheEnabled(true);
                    imageBitmap = Bitmap.createBitmap(drawView.getDrawingCache());
                    sendImage(imageBitmap, "drawing");
                }

            });

            //cancel button
            cancelBtn = (Button) view.findViewById(R.id.cancel_button);
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDialog().dismiss();
                }
            });

            //opacity
            opacityBtn = (ImageButton) view.findViewById(R.id.opacity_btn);
            opacityBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //launch opacity chooser
                    final Dialog seekDialog = new Dialog(getActivity());
                    seekDialog.setTitle("Opacity level:");
                    seekDialog.setContentView(R.layout.opacity_chooser);
                    //get ui elements
                    final TextView seekTxt = (TextView)seekDialog.findViewById(R.id.opq_txt);
                    final SeekBar seekOpq = (SeekBar)seekDialog.findViewById(R.id.opacity_seek);
                    //set max
                    seekOpq.setMax(100);
                    //show current level
                    int currLevel = drawView.getPaintAlpha();
                    seekTxt.setText(currLevel+"%");
                    seekOpq.setProgress(currLevel);
                    //update as user interacts
                    seekOpq.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            seekTxt.setText(Integer.toString(progress)+"%");
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {}

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {}

                    });
                    //listen for clicks on ok
                    Button opqBtn = (Button)seekDialog.findViewById(R.id.opq_ok);
                    opqBtn.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            drawView.setPaintAlpha(seekOpq.getProgress());
                            seekDialog.dismiss();
                        }
                    });
                    //show dialog
                    seekDialog.show();
                }

            });

        }catch (NullPointerException e){
            Toast unsavedToast = Toast.makeText(getActivity().getApplicationContext(),
                    "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
            unsavedToast.show();
        }
        return view;
    }

    private String getServerIP() {
        SharedPreferences tempPrefs = getActivity().getSharedPreferences("com.example.android.project1.NetworkPreferences", 0);
        return tempPrefs.getString("SERVER_IP", getResources().getString(R.string.server_ip_address));
    }

    private void getLocalUserInfo(){
        SharedPreferences prefs = getActivity().getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
        userName = prefs.getString("userName","Me");
        deviceID = prefs.getString("deviceUUID","0");
    }

    public void sendImage(Bitmap imageBitmap, String imageName){
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(),"Uploading image...","Please wait...",false,false);
        progressDialog.setCanceledOnTouchOutside(false);
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timestamp = simpleDateFormat.format(date);

        final String boundary = "begBoundary-" + deviceID + userName + deviceID;
        final String mimeType = "multipart/form-data; boundary=" + boundary;
        //Convert image into multipart byte[]
        byte[] multipartBody = getMultiPartDataFromBitmap(imageBitmap, imageName);

        /*ByteArrayOutputStream imageByteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 0, imageByteArrayOutputStream);
        final byte[] imageByteArray = imageByteArrayOutputStream.toByteArray();*/

        //Save the bitmap on the external storage
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)){
            File dataDir = new File(Environment.getExternalStorageDirectory(), "OnTime");
            if(!dataDir.exists()){
                dataDir.mkdirs();
            }
            File sentDataDir = new File(Environment.getExternalStorageDirectory()+"/OnTime", "Drawings");
            if(!sentDataDir.exists()){
                sentDataDir.mkdirs();
            }
            String fileName = timestamp;
            fileName = fileName.replaceAll("/", "-");
            imagePath = sentDataDir + "/" + fileName + ".png";
            try{
                FileOutputStream outputStream = new FileOutputStream(imagePath);
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
                imageID = DBMessagesHelper.insertImageIntoDB(userName, recepientUserName, "0", imagePath, timestamp, "unsent");
                Intent intent = new Intent("newMessageIntent");
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

        String url = SERVER_IP + "/MyFirstServlet/AddNewImage?senderDeviceID="+deviceID+"&recepientUserName="+ recepientUserName + "&imageID=" + imageID +"&timestamp="+timestamp;
        HttpsTrustManager.allowAllSSL();
        ImageMultiPartRequest multipartRequest = new ImageMultiPartRequest(url, null, mimeType, multipartBody, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                Toast.makeText(getActivity(), "Image sent successfully!", Toast.LENGTH_SHORT).show();
                //Dismiss the progress dialog
                progressDialog.dismiss();
                Log.d("ChatPage", "Drawing sent successfully.");
                Log.d("ChatPage", "Volley response: " + response.toString());
                String imageIDString = new String(response.data);
                DBMessagesHelper.updateImageState(Long.parseLong(imageIDString), "sent");
                Intent intent = new Intent("newMessageIntent");
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "Drawing upload failed!\r\n" + error.toString(), Toast.LENGTH_SHORT).show();
                //Dismiss the progress dialog
                progressDialog.dismiss();
                Log.d("ChatPage", "Volley error: " + error.toString());
            }
        });
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Adding request to the queue
        HttpConnector.getInstance(getActivity()).addToRequestQueue(multipartRequest);
    }

    private byte[] getMultiPartDataFromBitmap(Bitmap imageBitmap, String imageName){
        final String twoHyphens = "--";
        final String lineEnd = "\r\n";
        final String boundary = "begBoundary-" + deviceID + userName + deviceID;

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
}
