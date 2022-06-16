package com.example.instagram;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import static com.example.instagram.UserContract.userEntry;
import static com.example.instagram.UserContract.postEntry;

public class UserDBHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = UserDBHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 1;



    public UserDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_USER_TABLE = "CREATE TABLE " + userEntry.TABLE_NAME + " ("
                + userEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + userEntry.COLUMN_User_Email + " TEXT NOT NULL , "
                + userEntry.COLUMN_User_Username + " TEXT NOT NULL , "
                + userEntry.COLUMN_User_Password + " TEXT NOT NULL ,"
                + userEntry.COLUMN_User_LoginState + " TEXT NOT NULL ,"
                + userEntry.COLUMN_User_Bio + " TEXT ,"
                + userEntry.COLUMN_User_ProfilePic + " BLOB NOT NULL );";


        String SQL_CREATE_POST_TABLE = "CREATE TABLE " + postEntry.TABLE_NAME + " ("
                + postEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + postEntry.COLUMN_User_Email + " TEXT NOT NULL , "
                + postEntry.COLUMN_PostCap + " TEXT ,"
                + postEntry.COLUMN_PostPic + " BLOB );";


        db.execSQL(SQL_CREATE_USER_TABLE);
        db.execSQL(SQL_CREATE_POST_TABLE);

    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }


}
