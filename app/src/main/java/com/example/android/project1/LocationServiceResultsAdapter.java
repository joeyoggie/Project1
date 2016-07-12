package com.example.android.project1;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by fady on 4/1/2016.
 */
public class LocationServiceResultsAdapter extends ArrayAdapter {

    Activity activity ;
    List <ServiceProvider> serviceProviders;
    ViewHolder vHolder = null;

    public LocationServiceResultsAdapter(Activity activity, List sProviders){
        super(activity, R.layout.locationservicesitems, sProviders);
        this.activity = activity;
        this.serviceProviders = sProviders;
    }
    @Override
    public Object getItem(int position) {
        return serviceProviders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.locationservicesitems, null);
            vHolder = new ViewHolder();
            vHolder.nameTextView = (TextView)rowView.findViewById(R.id.name_text_view);
            vHolder.ratingTextVew = (TextView)rowView.findViewById(R.id.rating_text_view);
            vHolder.addressTextView = (TextView)rowView.findViewById(R.id.address_text_view);
            vHolder.profilePictureImageView = (ImageView)rowView.findViewById(R.id.profile_picture);
            vHolder.callButton = (Button) rowView.findViewById(R.id.call_button);
            vHolder.contactButton = (Button) rowView.findViewById(R.id.contact_button);
            vHolder.mapButton = (Button) rowView.findViewById(R.id.map_button);
            rowView.setTag(vHolder);
        }
        else{
            vHolder = (ViewHolder) rowView.getTag();
        }

        vHolder.nameTextView.setText(serviceProviders.get(position).getName());
        vHolder.addressTextView.setText(serviceProviders.get(position).getAddress());
        if(serviceProviders.get(position).getRating() == 0){
            vHolder.ratingTextVew.setText("Be the first to rate this person!");
        }
        else{
            vHolder.ratingTextVew.setText(String.valueOf(serviceProviders.get(position).getRating()) + "/5");
        }
        //vHolder.profilePictureImageView.setImageBitmap(bitmap);

        //use the tag to determine which row is selected in the onclicklistener below
        vHolder.tag = position;
        vHolder.contactButton.setTag(vHolder);
        vHolder.callButton.setTag(vHolder);
        vHolder.mapButton.setTag(vHolder);

        vHolder.contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vHolder = (ViewHolder) v.getTag();
                int pos = (int) vHolder.tag;
                String recepientUserName = serviceProviders.get(pos).getUserName();
                String recepientName = serviceProviders.get(pos).getName();
                if(recepientUserName != null && recepientUserName.length() > 0){
                    Intent chatPageIntent = new Intent(activity, ChatPage.class);
                    chatPageIntent.putExtra("recepientName",recepientName);
                    chatPageIntent.putExtra("recepientUserName",recepientUserName);
                    activity.startActivity(chatPageIntent);
                }
                else{
                    Toast.makeText(activity, "Contact not using OnTime. You should try calling instead.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        vHolder.callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vHolder = (ViewHolder) v.getTag();
                int pos = (int) vHolder.tag;
                String phoneNumber = serviceProviders.get(pos).getPhoneNumber();
                if(phoneNumber != null && phoneNumber.length() > 0){
                    Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+phoneNumber));
                    activity.startActivity(callIntent);
                }
                else{
                    Toast.makeText(activity, "No phone number found.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        vHolder.mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vHolder = (ViewHolder) v.getTag();
                int pos = (int) vHolder.tag;
                Double latitude = serviceProviders.get(pos).getLatitude();
                Double longitude = serviceProviders.get(pos).getLongitude();
                String name = serviceProviders.get(pos).getName();
                if(latitude == 0 || longitude == 0){
                    Toast.makeText(activity, "Location not found. You should call to get more info.", Toast.LENGTH_SHORT).show();
                }
                else{
                    String uriString = "geo:" + latitude + "," + longitude
                            + "?q=" + latitude + "," + longitude + "(" + name + ")";
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
                    activity.startActivity(mapIntent);
                }
            }
        });

        return rowView;
    }

    public static class ViewHolder{
        TextView nameTextView, ratingTextVew, addressTextView;
        ImageView profilePictureImageView;
        Button callButton, contactButton, mapButton;
        int tag;
    }
}
