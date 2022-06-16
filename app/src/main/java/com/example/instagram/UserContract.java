package com.example.instagram;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class UserContract {

    private UserContract() { }

    public static final String CONTENT_AUTHORITY = "com.example.instagram";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_User = "user";
    public static final String PATH_Post = "post";



    public static final class userEntry implements BaseColumns{

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_User;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_User;

        public final static Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_User);

        public final static String TABLE_NAME = "user";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_User_Email = "email";

        public final static String COLUMN_User_Username = "username";

        public final static String COLUMN_User_Password = "password";

        public final static String COLUMN_User_LoginState = "loginState";

        public final static String COLUMN_User_Bio = "bio";

        public final static String COLUMN_User_ProfilePic = "profilePic";

        public final static String VALUE_True = "true";

        public final static String VALUE_False = "false";

    }



    public static final class postEntry implements BaseColumns{

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_Post;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_Post;

        public final static Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_Post);

        public final static String TABLE_NAME = "post";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_User_Email = "email";

        public final static String COLUMN_PostPic = "postPic";

        public final static String COLUMN_PostCap = "postCap";

    }



}
