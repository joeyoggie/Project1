package com.example.android.project1;

import android.provider.BaseColumns;

/**
 * Created by fady on 3/27/2016.
 */
public final class DBContactsContract {
    public DBContactsContract (){}

    public static abstract class ContactsEntry implements BaseColumns {
        public static final String TABLE_NAME = "contacts";
        /*public static final String COLUMN_NAME_ID = "_ID";*/
        public static final String COLUMN_NAME_PHONE_NUMBER = "phoneNumber";
        public static final String COLUMN_NAME_USERNAME = "userName";
        public static final String COLUMN_NAME_NAME = "name";
    }

}
