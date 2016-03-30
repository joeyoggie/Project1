package com.example.android.project1;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by fady on 3/28/2016.
 */
public class ContactsAdapter extends CursorAdapter{

    TextView phone_no;
    TextView user_name;
    String phoneNo;
    String userName;

    public ContactsAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.layout_contact_entry,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        phone_no = (TextView) view.findViewById(R.id.phone_number);
        user_name = (TextView) view.findViewById(R.id.user_name);
        phoneNo = cursor.getString(cursor.getColumnIndexOrThrow(DBContactsContract.ContactsEntry.COLUMN_NAME_PHONE_NUMBER));
        userName = cursor.getString(cursor.getColumnIndexOrThrow(DBContactsContract.ContactsEntry.COLUMN_NAME_USERNAME));
        phone_no.setText(phoneNo);
        user_name.setText(userName);
    }
}
