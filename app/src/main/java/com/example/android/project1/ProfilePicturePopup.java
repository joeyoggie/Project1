package com.example.android.project1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import java.io.File;

/**
 * Created by Joey on 7/9/2016.
 */
public class ProfilePicturePopup extends PopupWindow {

    Context context;
    View rootView;
    View anchorView;
    PopupWindow window;
    String userName, localUserName;
    Bitmap profilePicture;
    String imagePath;

    public ProfilePicturePopup(Context context, View anchorView, String userName){
        this.context = context;
        this.anchorView = anchorView;
        this.userName = userName;
        getLocalUserInfo();
        showPopup();
    }

    private void showPopup(){
        LayoutInflater layoutInflater
                = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        rootView = layoutInflater.inflate(R.layout.profile_picture_popup_layout, null);
        window = new PopupWindow(rootView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        window.showAtLocation(anchorView, 0, 0, 0);
        ImageButton shareButton = (ImageButton) rootView.findViewById(R.id.share_profile_picture_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imagePath != null && imagePath.length() > 0){
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+imagePath));
                    shareIntent.setType("image/png");
                    context.startActivity(Intent.createChooser(shareIntent, "Send image to"));
                }
                else {
                    Log.d("UserProfile", "Unable to share image, no image found!");
                }
            }
        });
        if(!localUserName.equals(userName)){
            //This is someone else's picture, hide the "edit" icon
            ImageButton editButton = (ImageButton) rootView.findViewById(R.id.change_profile_picture_button);
            editButton.setVisibility(View.GONE);
        }
        loadImage();
    }

    private void getLocalUserInfo(){
        SharedPreferences prefs = context.getSharedPreferences("com.example.android.project1.RegistrationPreferences",0);
        localUserName = prefs.getString("userName","Me");
    }

    private void loadImage(){
        ImageView profilePictureImageView = (ImageView) rootView.findViewById(R.id.profile_picture);
        //Load the bitmap from the external storage, until the new one is downloaded
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)){
            File dataDir = new File(Environment.getExternalStorageDirectory(), "OnTime");
            if(!dataDir.exists()){
                dataDir.mkdirs();
            }
            File profilePictureDataDir = new File(Environment.getExternalStorageDirectory()+"/OnTime", "ProfilePictures");
            if(!profilePictureDataDir.exists()){
                profilePictureDataDir.mkdirs();
            }
            String fileName = userName;
            fileName = fileName.replaceAll("/", "-").replaceAll(":", "-");
            imagePath = profilePictureDataDir + "/" + fileName + ".png";
            Log.d("UserProfile", "Loading image: " + imagePath);
            profilePicture = BitmapFactory.decodeFile(imagePath);
            if(profilePicture != null){
                profilePictureImageView.setImageBitmap(profilePicture);
            }
        }
    }

    public boolean isVisible() {
        return window.isShowing();
    }
    public void hide(){
        if(window != null && window.isShowing()){
            window.dismiss();
        }
    }
}
