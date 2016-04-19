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
    List <ServiceProvider> objs;
    String phone_no, longitude, latitude, service_category, user_name;
    TextView person_name,rating,address;
    ImageView user_pp;
    public LocationServiceResultsAdapter(Activity activity,List objs){
        super(activity,R.layout.locationservicesitems,objs);
        this.activity=activity;
        this.objs=objs;
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
            person_name = (TextView)rowView.findViewById(R.id.name_text_view);
            rating = (TextView)rowView.findViewById(R.id.rating_text_view);
            address = (TextView)rowView.findViewById(R.id.location_text_view);
            user_pp = (ImageView)rowView.findViewById(R.id.profile_picture);
        }

        person_name.setText(objs.get(position).getUserName());
        address.setText(objs.get(position).getAddress());
        rating.setText(String.valueOf(objs.get(position).getRating()) + "/5");
        return rowView;
    }
}
