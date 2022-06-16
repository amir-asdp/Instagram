package com.example.instagram;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import static com.example.instagram.SignupActivity.mCurrentUserPath;
import static com.example.instagram.UserContract.userEntry;

public class StartupActivity extends AppCompatActivity {

    private static int TIME_OUT = 1000;
    private static UserDBHelper mDBHelper;
    public static Uri mCurrentUserUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        mDBHelper=new UserDBHelper(getApplicationContext());
        if(mCurrentUserPath == null){
            mCurrentUserPath = getSharedPreferences("current_path" , MODE_PRIVATE);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (CheckIfDataAlreadyInDBorNot(userEntry.TABLE_NAME ,userEntry.COLUMN_User_LoginState ,userEntry.VALUE_True)) {
                    Intent i = new Intent(StartupActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }else {
                    Intent i = new Intent(StartupActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        }, TIME_OUT);
    }



    public static boolean CheckIfDataAlreadyInDBorNot(String TableName, String column, String value ) {
        SQLiteDatabase sqldb = StartupActivity.mDBHelper.getReadableDatabase();
        String Query = "Select * from " + TableName + " where " + column + " ='" + value+"'";
        Cursor cursor = sqldb.rawQuery(Query, null);

        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }

        cursor.close();
        return true;
    }


}
