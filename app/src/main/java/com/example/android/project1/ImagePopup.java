package com.example.android.project1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

/**
 * Created by Joey on 7/12/2016.
 */
public class ImagePopup {

    Context context;
    View rootView;
    View anchorView;
    PopupWindow window;
    Bitmap picture;
    String imagePath;

    public ImagePopup(Context context, View anchorView, String imagePath){
        this.context = context;
        this.anchorView = anchorView;
        this.imagePath = imagePath;
        showPopup();
    }

    private void showPopup(){
        LayoutInflater layoutInflater
                = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        rootView = layoutInflater.inflate(R.layout.image_popup_layout, null);
        window = new PopupWindow(rootView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        window.showAtLocation(anchorView, 0, 0, 0);
        ImageButton shareButton = (ImageButton) rootView.findViewById(R.id.share_image_button);
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
        loadImage();
    }

    private void loadImage(){
        ImageView imageView = (ImageView) rootView.findViewById(R.id.picture_image_view);
        //Load the bitmap from the external storage, until the new one is downloaded
        //String imagePath;
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)){
            Log.d("ImagePopup", "Loading image: " + imagePath);
            picture = BitmapFactory.decodeFile(imagePath);
            if(imageView != null){
                imageView.setImageBitmap(picture);
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
