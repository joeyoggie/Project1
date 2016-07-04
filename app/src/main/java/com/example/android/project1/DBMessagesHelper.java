package com.example.android.project1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

/**
 * Created by Joey on 2/20/2016.
 */
//TODO: Get the readable/writable databases in a background thread
//TODO (DONE, needs testing)save the images in a file and only store its path in the database because cursor.configureWIndowSize has a limit of 1MB
public final class DBMessagesHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 14;
    public static final String DATABASE_NAME = "Messages.db";
    private static DBMessagesHelper dbHelper;
    private static SQLiteDatabase messagesDB;
    private static SQLiteDatabase readableMessagesDB;
    private static SQLiteDatabase writableMessagesDB;
    private static Cursor cursor;


    public static final String SQL_CREATE_QUERY = "CREATE TABLE " + DBMessagesContract.MessageEntry.TABLE_NAME
            + " (" + DBMessagesContract.MessageEntry._ID + " INTEGER PRIMARY KEY, "
            /*+ DBMessagesContract.MessageEntry.COLUMN_NAME_ID + " INTEGER AUTOINCREMENT,"*/
            + DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER + " TEXT, "
            + DBMessagesContract.MessageEntry.COLUMN_NAME_RECEPIENT + " TEXT, "
            + DBMessagesContract.MessageEntry.COLUMN_NAME_CONTENT + " TEXT, "
            + DBMessagesContract.MessageEntry.COLUMN_NAME_TIME + " TEXT, "
            + DBMessagesContract.MessageEntry.COLUMN_NAME_MESSAGE_STATE + " TEXT,"
            + DBMessagesContract.MessageEntry.COLUMN_NAME_MESSAGE_TYPE + " TEXT, "
            + DBMessagesContract.MessageEntry.COLUMN_NAME_IMAGE_ID + " TEXT, "
            + DBMessagesContract.MessageEntry.COLUMN_NAME_IMAGE_PATH + " TEXT" + ")";

    public static final String SQL_DELETE_QUERY = "DROP TABLE IF EXISTS " + DBMessagesContract.MessageEntry.TABLE_NAME;

    public static synchronized DBMessagesHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (dbHelper == null) {
            dbHelper = new DBMessagesHelper(context.getApplicationContext());
            getReadableWritableDatabases();
        }
        return dbHelper;
    }
    private DBMessagesHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_QUERY);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_QUERY);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private static void getReadableWritableDatabases()
    {
        readableMessagesDB = dbHelper.getReadableDatabase();
        writableMessagesDB = dbHelper.getWritableDatabase();
    }

    public static long insertMessageIntoDB(String senderUserName, String recepientUserName, String message, String timestamp, String state){
        ContentValues values = new ContentValues();
        //values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_ID,1);
        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER, senderUserName);
        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_RECEPIENT, recepientUserName);
        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_CONTENT, message);
        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_TIME, timestamp);
        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_MESSAGE_STATE, state);
        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_MESSAGE_TYPE, "text");
        long messageID = writableMessagesDB.insert(DBMessagesContract.MessageEntry.TABLE_NAME,null,values);
        return messageID;
    }

    public static Cursor readMessages(String userName, String recepientUserName)
    {
        //Define a projection string that specifies which columns from the database you will actually use after this query.
        String[] projection = {DBMessagesContract.MessageEntry._ID,
                /*DBMessagesContract.MessageEntry.COLUMN_NAME_ID,*/
                DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER,
                DBMessagesContract.MessageEntry.COLUMN_NAME_TIME,
                DBMessagesContract.MessageEntry.COLUMN_NAME_CONTENT,
                DBMessagesContract.MessageEntry.COLUMN_NAME_MESSAGE_STATE,
                DBMessagesContract.MessageEntry.COLUMN_NAME_MESSAGE_TYPE,
                DBMessagesContract.MessageEntry.COLUMN_NAME_IMAGE_ID,
                DBMessagesContract.MessageEntry.COLUMN_NAME_IMAGE_PATH};
        //How you want the results to be sorted in the resulting Cursor
        String sortOrder = DBMessagesContract.MessageEntry.COLUMN_NAME_TIME + " ASC";
        //The columns for the WHERE clause
        //String selection = DBMessagesContract.MessageEntry.COLUMN_NAME_RECEPIENT + "=?";
        String selection = "(" + DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER + "=?1"
                + " AND "
                + DBMessagesContract.MessageEntry.COLUMN_NAME_RECEPIENT + "=?2" + ")"
                +" OR (" + DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER + "=?2"
                +" AND "
                + DBMessagesContract.MessageEntry.COLUMN_NAME_RECEPIENT + "=?1" + ")";

        //The values of the WHERE clause
        //String[] selectionArgs = {"JoeyOggie"};
        String[] selectionArgs = {userName, recepientUserName};
        //The cursor object will contain the result of the query
        cursor = readableMessagesDB.query(DBMessagesContract.MessageEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        return cursor;
    }

    public static Cursor readUnsentTextMessages(String userName, String recepientUserName)
    {
        //Define a projection string that specifies which columns from the database you will actually use after this query.
        String[] projection = {DBMessagesContract.MessageEntry._ID,
                /*DBMessagesContract.MessageEntry.COLUMN_NAME_ID,*/
                DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER,
                DBMessagesContract.MessageEntry.COLUMN_NAME_TIME,
                DBMessagesContract.MessageEntry.COLUMN_NAME_CONTENT,
                DBMessagesContract.MessageEntry.COLUMN_NAME_MESSAGE_STATE,
                DBMessagesContract.MessageEntry.COLUMN_NAME_MESSAGE_TYPE};
        //How you want the results to be sorted in the resulting Cursor
        String sortOrder = DBMessagesContract.MessageEntry.COLUMN_NAME_TIME + " ASC";
        //The columns for the WHERE clause
        //String selection = DBMessagesContract.MessageEntry.COLUMN_NAME_RECEPIENT + "=?";
        String selection = "(" + DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER + "=?1"
                + " AND "
                + DBMessagesContract.MessageEntry.COLUMN_NAME_RECEPIENT + "=?2" + ")"
                + " AND (" + DBMessagesContract.MessageEntry.COLUMN_NAME_MESSAGE_TYPE + " = 'text')"
                + " AND (" + DBMessagesContract.MessageEntry.COLUMN_NAME_MESSAGE_STATE + " = 'unsent'"
                + ")";

        //The values of the WHERE clause
        String[] selectionArgs = {userName, recepientUserName};
        //The cursor object will contain the result of the query
        cursor = readableMessagesDB.query(DBMessagesContract.MessageEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        return cursor;
    }

    public static void updateTextMessage(long messageID, String newState){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMessagesContract.MessageEntry.COLUMN_NAME_MESSAGE_STATE, newState);
        long newRowId = writableMessagesDB.update(DBMessagesContract.MessageEntry.TABLE_NAME, contentValues, "_ID="+messageID, null);
    }

    public static long insertImageIntoDB(String senderUserName, String recepientUserName, String remoteImageID, String imagePath, String timestamp, String state){
        ContentValues values = new ContentValues();
        //values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_ID,1);
        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER, senderUserName);
        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_RECEPIENT, recepientUserName);
        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_IMAGE_ID, remoteImageID);
        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_IMAGE_PATH, imagePath);
        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_TIME, timestamp);
        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_MESSAGE_STATE, state);
        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_MESSAGE_TYPE, "image");
        long imageID = writableMessagesDB.insert(DBMessagesContract.MessageEntry.TABLE_NAME,null,values);
        return imageID;
    }

    public static Cursor readUnsentImageMessages(String userName, String recepientUserName)
    {
        //Define a projection string that specifies which columns from the database you will actually use after this query.
        String[] projection = {DBMessagesContract.MessageEntry._ID,
                /*DBMessagesContract.MessageEntry.COLUMN_NAME_ID,*/
                DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER,
                DBMessagesContract.MessageEntry.COLUMN_NAME_TIME,
                DBMessagesContract.MessageEntry.COLUMN_NAME_IMAGE_PATH,
                DBMessagesContract.MessageEntry.COLUMN_NAME_MESSAGE_STATE,
                DBMessagesContract.MessageEntry.COLUMN_NAME_MESSAGE_TYPE};
        //How you want the results to be sorted in the resulting Cursor
        String sortOrder = DBMessagesContract.MessageEntry.COLUMN_NAME_TIME + " ASC";
        //The columns for the WHERE clause
        //String selection = DBMessagesContract.MessageEntry.COLUMN_NAME_RECEPIENT + "=?";
        String selection = "(" + DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER + "=?1"
                + " AND "
                + DBMessagesContract.MessageEntry.COLUMN_NAME_RECEPIENT + "=?2" + ")"
                + " AND (" + DBMessagesContract.MessageEntry.COLUMN_NAME_MESSAGE_TYPE + " = 'image'"
                + " AND (" + DBMessagesContract.MessageEntry.COLUMN_NAME_MESSAGE_STATE + " = 'unsent'"
                + " )";

        //The values of the WHERE clause
        String[] selectionArgs = {userName, recepientUserName};
        //The cursor object will contain the result of the query
        cursor = readableMessagesDB.query(DBMessagesContract.MessageEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        return cursor;
    }

    public static void updateImageState(long imageID, String newState){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBMessagesContract.MessageEntry.COLUMN_NAME_MESSAGE_STATE, newState);
        long newRowId = writableMessagesDB.update(DBMessagesContract.MessageEntry.TABLE_NAME, contentValues, "_ID="+imageID, null);
    }

    //AsynkTask that will get Writable/Readable databases in a background thread
    public class backgroundDBHelper extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... params){
            readableMessagesDB = dbHelper.getReadableDatabase();
            writableMessagesDB = dbHelper.getWritableDatabase();
            return null;
        }
    }
}
