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

    String phoneNo;
    String name;
    String userName;
    Context context;

    public ContactsAdapter(Context con, Cursor c) {
        super(con, c);
        context = con;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_contact_entry, parent, false);

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.phoneNumberTextView = (TextView) view.findViewById(R.id.contact_phone_number);
        viewHolder.nameTextView = (TextView) view.findViewById(R.id.contact_name);
        viewHolder.userNameTextView = (TextView) view.findViewById(R.id.contact_username);
        viewHolder.userProfilePictureImageView = (ImageView)view.findViewById(R.id.contact_profile_picture);

        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        phoneNo = cursor.getString(cursor.getColumnIndexOrThrow(DBContactsContract.ContactsEntry.COLUMN_NAME_PHONE_NUMBER));
        name = cursor.getString(cursor.getColumnIndexOrThrow(DBContactsContract.ContactsEntry.COLUMN_NAME_NAME));
        userName = cursor.getString(cursor.getColumnIndexOrThrow(DBContactsContract.ContactsEntry.COLUMN_NAME_USERNAME));
        //get image as well from the cursor
        viewHolder.phoneNumberTextView.setText(phoneNo);
        viewHolder.nameTextView.setText(name);
        viewHolder.userNameTextView.setText("(@"+userName+")");
        //viewHolder.userProfilePictureImageView.setImageBitmap(bitmap);

        //use the tag to determine which row is selected in the onclicklistener below
        viewHolder.tag = cursor.getPosition();

        viewHolder.userProfilePictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) viewHolder.tag;
                cursor.moveToPosition(position);
                phoneNo = cursor.getString(cursor.getColumnIndexOrThrow(DBContactsContract.ContactsEntry.COLUMN_NAME_PHONE_NUMBER));
                name = cursor.getString(cursor.getColumnIndexOrThrow(DBContactsContract.ContactsEntry.COLUMN_NAME_NAME));
                userName = cursor.getString(cursor.getColumnIndexOrThrow(DBContactsContract.ContactsEntry.COLUMN_NAME_USERNAME));

                Intent intent = new Intent(context.getApplicationContext(), UserProfile.class);
                intent.putExtra("userName",userName);
                intent.putExtra("name", name);
                intent.putExtra("phoneNumber", phoneNo);
                context.startActivity(intent);
            }
        });
    }

    public static class ViewHolder{
        TextView phoneNumberTextView;
        TextView nameTextView;
        TextView userNameTextView;
        ImageView userProfilePictureImageView;
        int tag;
    }
}
