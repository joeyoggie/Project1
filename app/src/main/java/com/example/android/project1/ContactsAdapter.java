package com.example.android.project1;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by fady on 3/28/2016.
 */
public class ContactsAdapter extends CursorAdapter{

    TextView phoneNumberTextView;
    TextView nameTextView;
    TextView userNameTextView;
    String phoneNo;
    String name;
    String userName;
    ImageView userProfilePictureImageView;
    Context context;

    public ContactsAdapter(Context con, Cursor c) {
        super(con, c);
        context = con;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.layout_contact_entry, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        phoneNumberTextView = (TextView) view.findViewById(R.id.contact_phone_number);
        nameTextView = (TextView) view.findViewById(R.id.contact_name);
        userNameTextView = (TextView) view.findViewById(R.id.contact_username);
        userProfilePictureImageView = (ImageView)view.findViewById(R.id.contact_profile_picture);

        phoneNo = cursor.getString(cursor.getColumnIndexOrThrow(DBContactsContract.ContactsEntry.COLUMN_NAME_PHONE_NUMBER));
        name = cursor.getString(cursor.getColumnIndexOrThrow(DBContactsContract.ContactsEntry.COLUMN_NAME_NAME));
        userName = cursor.getString(cursor.getColumnIndexOrThrow(DBContactsContract.ContactsEntry.COLUMN_NAME_USERNAME));
        //get image as well from the cursor
        phoneNumberTextView.setText(phoneNo);
        nameTextView.setText(name);
        userNameTextView.setText("(@"+userName+")");
        //userProfilePictureImageView.setImageBitmap(bitmap);

        //TODO Needs fixing as it always returns the last entry in the list view, regarldess of the clicked item
        userProfilePictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context.getApplicationContext(), UserProfile.class);
                intent.putExtra("username_key",userName);
                context.startActivity(intent);
            }
        });
    }
}
