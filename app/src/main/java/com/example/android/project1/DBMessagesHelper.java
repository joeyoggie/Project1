package com.example.android.project1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by Joey on 2/20/2016.
 */
public final class DBMessagesHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Messages.db";
    private static DBMessagesHelper dbHelper;
    private static SQLiteDatabase messagesDB;
    private static SQLiteDatabase readableMessagesDB;
    private static SQLiteDatabase writableMessagesDB;
    private static Cursor cursor;


    public static final String SQL_CREATE_QUERY = "CREATE TABLE " + DBMessagesContract.MessageEntry.TABLE_NAME
            + " (" + DBMessagesContract.MessageEntry._ID + " INTEGER PRIMARY KEY,"
            + DBMessagesContract.MessageEntry.COLUMN_NAME_ID + " TEXT,"
            + DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER + " TEXT,"
            + DBMessagesContract.MessageEntry.COLUMN_NAME_RECEPIENT + " TEXT,"
            + DBMessagesContract.MessageEntry.COLUMN_NAME_CONTENT + " TEXT,"
            + DBMessagesContract.MessageEntry.COLUMN_NAME_TIME + " TEXT" + ")";
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

    public static void insertMessageIntoDB(String senderUserName, String recepientUserName, String message, String timestamp){
        ContentValues values = new ContentValues();
        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_ID,1);
        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER, senderUserName);
        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_RECEPIENT, recepientUserName);
        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_CONTENT, message);
        values.put(DBMessagesContract.MessageEntry.COLUMN_NAME_TIME, timestamp);
        long newRowId;
        newRowId = writableMessagesDB.insert(DBMessagesContract.MessageEntry.TABLE_NAME,null,values);
    }

    public static Cursor readMessages(String userName, String recepientUserName)
    {
        //Define a projection string that specifies which columns from the database you will actually use after this query.
        String[] projection = {DBMessagesContract.MessageEntry._ID,
                DBMessagesContract.MessageEntry.COLUMN_NAME_SENDER,
                DBMessagesContract.MessageEntry.COLUMN_NAME_TIME,
                DBMessagesContract.MessageEntry.COLUMN_NAME_CONTENT};
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

        Log.d("ChatPage", selection);
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

    //AsynkTask that will get Writable/Readable databases in a background thread
    public class backgroundDBHelper extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... params){
            readableMessagesDB = dbHelper.getReadableDatabase();
            writableMessagesDB = dbHelper.getWritableDatabase();
            return null;
        }
    }
}
