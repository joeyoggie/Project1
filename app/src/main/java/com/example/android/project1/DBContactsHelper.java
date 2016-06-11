package com.example.android.project1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import java.util.List;

/**
 * Created by fady on 3/27/2016.
 */
//TODO: Get the readable/writable databases in a background thread
//TODO: Add an image column to the database
public final class DBContactsHelper extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "Contacts.db";
    private static final int DATABASE_VERSION = 14;
    private static DBContactsHelper dbHelper;
    private static SQLiteDatabase readableContactsDB;
    private static SQLiteDatabase writableContactsDB;
    private static Cursor cursor;

    public static final String SQL_CREATE_QUERY = "CREATE TABLE " + DBContactsContract.ContactsEntry.TABLE_NAME + " ( " +
            DBContactsContract.ContactsEntry._ID + " INTEGER PRIMARY KEY," +
            DBContactsContract.ContactsEntry.COLUMN_NAME_NAME + " TEXT," +
            DBContactsContract.ContactsEntry.COLUMN_NAME_PHONE_NUMBER + " TEXT," +
            DBContactsContract.ContactsEntry.COLUMN_NAME_USERNAME + " TEXT" + ")";

    /*public static final String SQL_CREATE_QUERY = "CREATE TABLE " + DBContactsContract.ContactsEntry.TABLE_NAME + " ( " +
            DBContactsContract.ContactsEntry._ID + " INTEGER," +
            DBContactsContract.ContactsEntry.COLUMN_NAME_PHONE_NUMBER + " TEXT," +
            DBContactsContract.ContactsEntry.COLUMN_NAME_USERNAME + " TEXT," +
            "PRIMARY KEY ("+ DBContactsContract.ContactsEntry._ID + ","+ DBContactsContract.ContactsEntry.COLUMN_NAME_PHONE_NUMBER + ")" + ")";
*/
    public static final String SQL_DELETE_QUERY=  "DROP TABLE IF EXISTS " + DBContactsContract.ContactsEntry.TABLE_NAME;

    private DBContactsHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DBContactsHelper getInstance(Context context){

        if (dbHelper == null){
            dbHelper = new DBContactsHelper(context.getApplicationContext());
            getReadableWritableDatabases();
        }
        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(SQL_DELETE_QUERY);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    public static void getReadableWritableDatabases(){
        readableContactsDB=dbHelper.getReadableDatabase();
        writableContactsDB=dbHelper.getWritableDatabase();
    }

    public static void insertContactIntoDataBase(List<Contact> list_of_recieved_contacts){
        ContentValues contentValues;
        Contact tempContact;
        writableContactsDB.beginTransaction();
        for(int i = 0; i < list_of_recieved_contacts.size(); i++)
        {
            contentValues = new ContentValues();
            tempContact = list_of_recieved_contacts.get(i);
            contentValues.put(DBContactsContract.ContactsEntry.COLUMN_NAME_PHONE_NUMBER, tempContact.getPhoneNumber());
            contentValues.put(DBContactsContract.ContactsEntry.COLUMN_NAME_USERNAME, tempContact.getUserName());
            contentValues.put(DBContactsContract.ContactsEntry.COLUMN_NAME_NAME, tempContact.getName());
            /*if( readableContactsDB.query(DBContactsContract.ContactsEntry.TABLE_NAME,
                    new String[] {DBContactsContract.ContactsEntry.COLUMN_NAME_PHONE_NUMBER},
                    DBContactsContract.ContactsEntry.COLUMN_NAME_PHONE_NUMBER + "=?",
                    new String[] {phoneNumber},
                    null,null,
                    DBContactsContract.ContactsEntry.COLUMN_NAME_ID + " ASC").getCount() < 0)
            {
                long newRowId = writableContactsDB.insert(
                        DBContactsContract.ContactsEntry.TABLE_NAME,
                        null,
                        contentValues);
            }
            else{
                long newRowId = writableContactsDB.update(DBContactsContract.ContactsEntry.TABLE_NAME,contentValues,"phoneNumber=?",new String[] {tempContact.getPhoneNumber()});
            }*/
            long newRowId = writableContactsDB.update(DBContactsContract.ContactsEntry.TABLE_NAME,contentValues,"phoneNumber=?",new String[] {tempContact.getPhoneNumber()});
            if( newRowId <= 0) {
                writableContactsDB.insert(
                        DBContactsContract.ContactsEntry.TABLE_NAME,
                        null,
                        contentValues);
            }

            //long newRowId = writableContactsDB.insertWithOnConflict(DBContactsContract.ContactsEntry.TABLE_NAME,null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);
        }
        writableContactsDB.setTransactionSuccessful();
        writableContactsDB.endTransaction();
    }

    public static Cursor readContacts()
    {
        //Define a projection string that specifies which columns from the database you will actually use after this query.
        String[] projection = {DBContactsContract.ContactsEntry._ID,
                DBContactsContract.ContactsEntry .COLUMN_NAME_USERNAME,
                DBContactsContract.ContactsEntry.COLUMN_NAME_PHONE_NUMBER,
                DBContactsContract.ContactsEntry.COLUMN_NAME_NAME};
        //How you want the results to be sorted in the resulting Cursor
        String sortOrder = DBContactsContract.ContactsEntry.COLUMN_NAME_ID + " ASC";
        //The columns for the WHERE clause
        String selection = DBContactsContract.ContactsEntry.COLUMN_NAME_PHONE_NUMBER;

        //The values of the WHERE clause
        String[] selectionArgs = {};
        //The cursor object will contain the result of the query
        cursor = readableContactsDB.query(DBContactsContract.ContactsEntry.TABLE_NAME,
                projection,
                null, /*selection*/
                selectionArgs,
                null,
                null,
                sortOrder);
        return cursor;
    }

    public static Cursor readFilteredContacts(String constraint)
    {
        //Define a projection string that specifies which columns from the database you will actually use after this query.
        String[] projection = {DBContactsContract.ContactsEntry._ID,
                DBContactsContract.ContactsEntry .COLUMN_NAME_USERNAME,
                DBContactsContract.ContactsEntry.COLUMN_NAME_PHONE_NUMBER,
                DBContactsContract.ContactsEntry.COLUMN_NAME_NAME};
        //How you want the results to be sorted in the resulting Cursor
        String sortOrder = DBContactsContract.ContactsEntry.COLUMN_NAME_ID + " ASC";
        //The columns for the WHERE clause
        String selection = DBContactsContract.ContactsEntry.COLUMN_NAME_NAME + " LIKE ?";

        //The values of the WHERE clause
        String[] selectionArgs = {"%"+constraint+"%"};
        //The cursor object will contain the result of the query
        cursor = readableContactsDB.query(DBContactsContract.ContactsEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        return cursor;
    }

    public class backgroundDBHelper extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... params){
            readableContactsDB= dbHelper.getReadableDatabase();
            writableContactsDB = dbHelper.getWritableDatabase();
            return null;
        }
    }

}
