package com.example.instagram;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import static com.example.instagram.UserDBHelper.LOG_TAG;

public class UserProvider extends ContentProvider {

    private UserDBHelper mDbHelper;
    private static final int USERS = 100;
    private static final int USERS_ID = 101;
    private static final int POSTS = 200;
    private static final int POSTS_ID = 201;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(UserContract.CONTENT_AUTHORITY, UserContract.PATH_User, USERS);
        sUriMatcher.addURI(UserContract.CONTENT_AUTHORITY, UserContract.PATH_User + "/#", USERS_ID);
        sUriMatcher.addURI(UserContract.CONTENT_AUTHORITY, UserContract.PATH_Post, POSTS);
        sUriMatcher.addURI(UserContract.CONTENT_AUTHORITY, UserContract.PATH_Post + "/#", POSTS_ID);
    }



    public UserProvider() {
        super();
    }



    @Override
    public boolean onCreate() {
        mDbHelper = new UserDBHelper(getContext());
        return true;
    }



    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case POSTS:
                cursor = database.query(UserContract.postEntry.TABLE_NAME, projection, selection + "=?", selectionArgs, null, null, sortOrder);
                break;
            case POSTS_ID:
                selection = UserContract.postEntry._ID;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(UserContract.postEntry.TABLE_NAME, projection, selection + "=?", selectionArgs, null, null, sortOrder);
                break;
            case USERS:
                cursor = database.query(UserContract.userEntry.TABLE_NAME, projection, selection + "=?", selectionArgs, null, null, sortOrder);
                break;
            case USERS_ID:
                selection = UserContract.userEntry._ID;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(UserContract.userEntry.TABLE_NAME, projection, selection + "=?", selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }



    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case POSTS:
                return UserContract.postEntry.CONTENT_LIST_TYPE;
            case POSTS_ID:
                return UserContract.postEntry.CONTENT_ITEM_TYPE;
            case USERS:
                return UserContract.userEntry.CONTENT_LIST_TYPE;
            case USERS_ID:
                return UserContract.userEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }



    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case POSTS:
                return insertPost(uri, contentValues);
            case USERS:
                return insertUser(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }



    private Uri insertPost(Uri uri , ContentValues values){

        byte[] photo = values.getAsByteArray(UserContract.postEntry.COLUMN_PostPic);
        if (photo == null) {
            throw new IllegalArgumentException("User requires a photo");
        }

        String email = values.getAsString(UserContract.postEntry.COLUMN_User_Email);
        if (email == null) {
            throw new IllegalArgumentException("User requires a email");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(UserContract.postEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }



    private Uri insertUser(Uri uri, ContentValues values) {

        String email = values.getAsString(UserContract.userEntry.COLUMN_User_Email);
        if (email == null) {
            throw new IllegalArgumentException("User requires a email");
        }

        String username = values.getAsString(UserContract.userEntry.COLUMN_User_Username);
        if (username == null) {
            throw new IllegalArgumentException("User requires a username");
        }

        String password = values.getAsString(UserContract.userEntry.COLUMN_User_Password);
        if (password == null) {
            throw new IllegalArgumentException("User requires a password");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(UserContract.userEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }



    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case POSTS:
                rowsDeleted = database.delete(UserContract.postEntry.TABLE_NAME, selection + "=?", selectionArgs);
                break;
            case POSTS_ID:
                selection = UserContract.postEntry._ID ;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(UserContract.postEntry.TABLE_NAME, selection + "=?", selectionArgs);
                break;
            case USERS:
                rowsDeleted = database.delete(UserContract.userEntry.TABLE_NAME, selection + "=?", selectionArgs);
                break;
            case USERS_ID:
                selection = UserContract.userEntry._ID ;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(UserContract.userEntry.TABLE_NAME, selection + "=?", selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }



    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case POSTS:
                return updateCaption(uri, contentValues, selection + "=?", selectionArgs);
            case POSTS_ID:
                selection = UserContract.postEntry._ID;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateCaption(uri, contentValues, selection + "=?", selectionArgs);
            case USERS:
                return updateUser(uri, contentValues, selection + "=?", selectionArgs);
            case USERS_ID:
                selection = UserContract.userEntry._ID;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateUser(uri, contentValues, selection + "=?", selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }



    private int updateCaption(Uri uri, ContentValues values, String selection, String[] selectionArgs){

        if (values.containsKey(UserContract.postEntry.COLUMN_User_Email)) {
            String caption = values.getAsString(UserContract.postEntry.COLUMN_PostCap);
            if (caption == null) {
                throw new IllegalArgumentException("User requires a new caption");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(UserContract.postEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }



    private int updateUser(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(UserContract.userEntry.COLUMN_User_Email)) {
            String name = values.getAsString(UserContract.userEntry.COLUMN_User_Email);
            if (name == null) {
                throw new IllegalArgumentException("User requires an email");
            }
        }

        if (values.containsKey(UserContract.userEntry.COLUMN_User_Username)) {
            String Username = values.getAsString(UserContract.userEntry.COLUMN_User_Username);
            if (Username == null ) {
                throw new IllegalArgumentException("User requires a username");
            }
        }

        if (values.containsKey(UserContract.userEntry.COLUMN_User_Password)) {
            String Password = values.getAsString(UserContract.userEntry.COLUMN_User_Password);
            if (Password == null ) {
                throw new IllegalArgumentException("User requires a password");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(UserContract.userEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}
