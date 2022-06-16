package com.example.instagram;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import es.dmoral.toasty.Toasty;

import static com.example.instagram.LoginActivity.mGoogleSignInClient;
import static com.example.instagram.MainActivity.mAllUsersPostList;
import static com.example.instagram.MainActivity.mDBHelper;
import static com.example.instagram.MainActivity.mPostDatabaseRef;
import static com.example.instagram.SignupActivity.mCurrentUserPath;
import static com.example.instagram.StartupActivity.mCurrentUserUri;
import static com.example.instagram.UserContract.postEntry;


public class AccountOptionDialogClass extends Dialog implements android.view.View.OnClickListener {

    Context mContext;
    Activity mActivity;
    DatabaseReference mCurrentUserReference;
    FirebaseUser mCurrentUser;
    String mMessage;
    String mAgreeTxt;
    String mDisAgreeTxt;
    String mAction;

    public AccountOptionDialogClass(@NonNull Context context , Activity activity, String message , String agreeTxt , String disAgreeTxt , String action) {
        super(context);
        mContext = context;
        mActivity = activity;
        mMessage = message;
        mAgreeTxt =agreeTxt;
        mDisAgreeTxt = disAgreeTxt;
        mAction = action;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_account_option);
        TextView mMessageTxv = findViewById(R.id.dialog_message);      mMessageTxv.setText(mMessage);
        TextView mAgreeTxtTxv = findViewById(R.id.agree_action);       mAgreeTxtTxv.setText(mAgreeTxt);
        TextView mDisAgreeTxtTxv = findViewById(R.id.disagree_action); mDisAgreeTxtTxv.setText(mDisAgreeTxt);
        mAgreeTxtTxv.setOnClickListener(this);
        mDisAgreeTxtTxv.setOnClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View v) {

        SQLiteDatabase readableDB = mDBHelper.getReadableDatabase();
        long currentUserId = Long.parseLong(mCurrentUserUri.getLastPathSegment());
        String queryStatement = "Select * from " + UserContract.userEntry.TABLE_NAME + " where " + UserContract.userEntry._ID + " ='" + currentUserId+"'";
        Cursor cursor = readableDB.rawQuery(queryStatement, null);
        cursor.moveToFirst();
        final String currentUserEmail = cursor.getString(cursor.getColumnIndex(UserContract.userEntry.COLUMN_User_Email));
        cursor.close();

        mCurrentUserReference = FirebaseDatabase.getInstance().getReference().child("users");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        switch (mAction) {


            case "logout":
                switch (v.getId()){
                    case R.id.agree_action:
                        ContentValues values = new ContentValues();
                        values.put(UserContract.userEntry.COLUMN_User_LoginState, UserContract.userEntry.VALUE_False);
                        int rowUpdated = mContext.getContentResolver().update(mCurrentUserUri , values , null , null);
                        mCurrentUserPath.edit().putString("current_path" , "").apply();
                        mCurrentUserUri = UserContract.userEntry.CONTENT_URI;

                        FirebaseAuth.getInstance().signOut();
                        if (mGoogleSignInClient != null){
                            mGoogleSignInClient.signOut();
                        }

                        mContext.startActivity(new Intent(mContext , LoginActivity.class));
                        mActivity.finishAffinity();
                        break;
                    case R.id.disagree_action:
                        dismiss();
                        break;
                }
                break;


            case "delete_account":
                switch (v.getId()){
                    case R.id.agree_action:
                        if (mGoogleSignInClient != null){
                            mGoogleSignInClient.signOut();
                        }
                        mCurrentUser.delete().addOnCompleteListener(mActivity, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    int userRowDeleted = mContext.getContentResolver().delete(mCurrentUserUri ,null ,null);
                                    int postRowDeleted = mContext.getContentResolver().delete(postEntry.CONTENT_URI ,postEntry.COLUMN_User_Email ,new String[]{currentUserEmail});
                                    mCurrentUserPath.edit().putString("current_path" , "").apply();
                                    mCurrentUserUri = UserContract.userEntry.CONTENT_URI;

                                    for (DataSnapshot deletingPost : mAllUsersPostList){
                                        if (deletingPost.getValue(PostItem.class).getmUid().equals(mCurrentUser.getUid())){
                                            mPostDatabaseRef.child(deletingPost.getKey()).removeValue();
                                        }
                                    }
                                    mCurrentUserReference.child(mCurrentUser.getUid()).removeValue();

                                    mContext.startActivity(new Intent(mContext , LoginActivity.class));
                                    mActivity.finishAffinity();
                                }else {
                                    Log.e("**********", task.getException().getMessage());
                                    Toasty.error(mContext, "Delete account failed").show();
                                }
                            }
                        });
                        break;
                    case R.id.disagree_action:
                        dismiss();
                        break;
                }
                break;


            default:
                break;

        }

        dismiss();
    }


}