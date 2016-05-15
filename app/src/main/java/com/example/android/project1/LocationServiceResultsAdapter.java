package com.example.android.project1;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by fady on 4/1/2016.
 */
public class LocationServiceResultsAdapter extends ArrayAdapter {

    Activity activity ;
    List <ServiceProvider> serviceProviders;
    String phone_no, longitude, latitude, service_category, user_name;
    TextView nameTextView, ratingTextVew, addressTextView;
    ImageView profilePictureImageView;

    public LocationServiceResultsAdapter(Activity activity, List sProviders){
        super(activity,R.layout.locationservicesitems, sProviders);
        this.activity = activity;
        this.serviceProviders = sProviders;
    }
    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.locationservicesitems, null);
            nameTextView = (TextView)rowView.findViewById(R.id.name_text_view);
            ratingTextVew = (TextView)rowView.findViewById(R.id.rating_text_view);
            addressTextView = (TextView)rowView.findViewById(R.id.location_text_view);
            profilePictureImageView = (ImageView)rowView.findViewById(R.id.profile_picture);
        }

        nameTextView.setText(serviceProviders.get(position).getUserName());
        addressTextView.setText(serviceProviders.get(position).getAddress());
        ratingTextVew.setText(String.valueOf(serviceProviders.get(position).getRating()) + "/5");
        //profilePictureImageView.setImageBitmap(bitmap);
        return rowView;
    }
}
