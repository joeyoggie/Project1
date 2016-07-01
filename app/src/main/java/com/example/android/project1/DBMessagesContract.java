package com.example.android.project1;

import android.provider.BaseColumns;

/**
 * Created by Joey on 2/20/2016.
 */
public final class DBMessagesContract {

    public DBMessagesContract(){}

    public static abstract class MessageEntry implements BaseColumns {

        public static final String TABLE_NAME = "messages";
        //public static final String COLUMN_NAME_ID = "messageID";
        public static final String COLUMN_NAME_SENDER = "senderUserName";
        public static final String COLUMN_NAME_RECEPIENT = "recepientUserName";
        public static final String COLUMN_NAME_CONTENT = "message";
        public static final String COLUMN_NAME_TIME = "timestamp";
        public static final String COLUMN_NAME_MESSAGE_STATE = "messageStatus";
        public static final String COLUMN_NAME_MESSAGE_TYPE = "messageType";
        public static final String COLUMN_NAME_IMAGE_ID = "imageID";
        public static final String COLUMN_NAME_IMAGE_BLOB = "imageBlob";
    }
}