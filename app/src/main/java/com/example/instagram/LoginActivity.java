package com.example.instagram;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.internal.SignInButtonImpl;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;

import static com.example.instagram.SignupActivity.mCurrentUserPath;
import static com.example.instagram.StartupActivity.mCurrentUserUri;
import static com.example.instagram.UserContract.userEntry;

public class LoginActivity extends AppCompatActivity {

    public static final int RC_SIGN_IN = 101;
    static GoogleSignInClient mGoogleSignInClient;
    FirebaseDatabase mUserDatabase;
    DatabaseReference mUserDatabaseRef;
    FirebaseAuth mUserAuth;
    FirebaseUser mUser;
    UserItem mUserItem;
    ArrayList<String> mEmailList = new ArrayList<>();
    HashMap<String,String> Email_Password = new HashMap<>();
    ChildEventListener mUserChildEventListener;
    CustomProgressDialogClass mLoginProgressDialog;


    static EditText mEmailEditTXT;
    static EditText mPasswordEditTXT;
    static ImageView Check_green_Email;
    ImageView mPasswordVisibler;
    TextView mEmailValidation;
    TextView mGoSignup;
    Button mLoginButton;
    SignInButtonImpl mGoogleSignInBtn;
    private static UserDBHelper mDBHelper;
    boolean visibleFlag = true;



    @Override
    protected void onResume() {
        super.onResume();
        if (checkInternetConnection(LoginActivity.this) && mUserChildEventListener != null){
            mEmailList.clear();
            Email_Password.clear();
            mUserDatabaseRef.addChildEventListener(mUserChildEventListener);
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        mEmailList.clear();
        Email_Password.clear();
        mUserDatabaseRef.removeEventListener(mUserChildEventListener);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            mLoginProgressDialog.cancel();
            mLoginProgressDialog = new CustomProgressDialogClass(LoginActivity.this, "Logging in ...");
            mLoginProgressDialog.setCancelable(false);
            mLoginProgressDialog.show();
            try {
                loginUserWithGoogle(GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class));
            } catch (ApiException e) {
                Toasty.error(this, e.getMessage()).show();
                loginUserWithGoogle( null);
            }
        } else {
            mLoginProgressDialog.cancel();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        mLoginProgressDialog = new CustomProgressDialogClass(LoginActivity.this, "Loading ...");
        mLoginProgressDialog.setCanceledOnTouchOutside(false);
        mGoogleSignInClient = GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build());
        mUserDatabase = FirebaseDatabase.getInstance();
        mUserDatabaseRef = mUserDatabase.getReference().child("users");
        mUserAuth = FirebaseAuth.getInstance();
        mUserChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                mLoginProgressDialog = new CustomProgressDialogClass(LoginActivity.this, "Loading ...");
                mLoginProgressDialog.setCancelable(false);
                mLoginProgressDialog.show();
                UserItem userItem = dataSnapshot.getValue(UserItem.class);
                mEmailList.add(userItem.getmEmail());
                Email_Password.put(userItem.getmEmail(), userItem.getmPassword());
                mLoginProgressDialog.cancel();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("*********",databaseError.getMessage() + "//" + databaseError.getDetails() );
            }
        };



        Intent intent = getIntent();
        mEmailEditTXT = findViewById(R.id.login_email);
        mPasswordEditTXT = findViewById(R.id.login_password);
        mPasswordVisibler = findViewById(R.id.password_visbler);
        mEmailValidation = findViewById(R.id.email_validation);
        Check_green_Email = findViewById(R.id.green_check_email_for_login);
        mGoSignup = findViewById(R.id.go_signup);
        mLoginButton = findViewById(R.id.login_btn);
        mGoogleSignInBtn = findViewById(R.id.google_signin_btn);
        mDBHelper = new UserDBHelper(getApplicationContext());
        mCurrentUserUri = intent.getData();
        if(mCurrentUserPath == null){
            mCurrentUserPath = getSharedPreferences("current_path" , MODE_PRIVATE);
        }



        mEmailEditTXT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email1=mEmailEditTXT.getText().toString();
                String email2=mEmailEditTXT.getText().toString().trim();

                if (!email1.contains(" ")) {
                    if (Patterns.EMAIL_ADDRESS.matcher(email2).matches()) {
                        Check_green_Email.setVisibility(View.GONE);
                        mEmailValidation.setVisibility(View.GONE);
                        mEmailValidation.setText("");
                    }
                    else if (email2.length() == 0) {
                        mEmailValidation.setText("");
                        Check_green_Email.setVisibility(View.GONE);
                        mEmailValidation.setVisibility(View.GONE);
                    }
                    else {
                        mEmailValidation.setText("Email is not valid");
                        mEmailValidation.setVisibility(View.VISIBLE);
                        Check_green_Email.setVisibility(View.GONE);
                    }
                }else {
                    mEmailValidation.setText("There is invalid character");
                    mEmailValidation.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void afterTextChanged(Editable s) { }
        });



        mPasswordVisibler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPasswordEditTXT.getText().toString().length() != 0) {
                    if (visibleFlag) {
                        mPasswordEditTXT.setInputType(InputType.TYPE_CLASS_TEXT);
                        mPasswordVisibler.setImageResource(R.drawable.ic_eye);
                        mPasswordEditTXT.setSelection(mPasswordEditTXT.getText().toString().length());
                        visibleFlag = false;
                    } else {
                        mPasswordEditTXT.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        mPasswordVisibler.setImageResource(R.drawable.ic_hide);
                        mPasswordEditTXT.setSelection(mPasswordEditTXT.getText().toString().length());
                        visibleFlag = true;
                    }
                }
            }
        });



        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailEditTXT.getText().toString();
                String password = mPasswordEditTXT.getText().toString();
                assignSqlDB(userEntry.TABLE_NAME, userEntry.COLUMN_User_Email, email);
                mLoginProgressDialog.setProgressText("Logging in ...");
                mLoginProgressDialog.setCancelable(false);
                mLoginProgressDialog.show();

                if (email.length()==0 || password.length()==0){
                    vibrate(200);
                    Toasty.error(LoginActivity.this, "All of the fields must be completed").show();
                    mLoginProgressDialog.cancel();
                }
                else if (checkInternetConnection(LoginActivity.this)){
                    if (mEmailValidation.length()==0 && isInFirebaseDB(email,password)){
                        loginUser(mEmailEditTXT.getText().toString().trim() ,mPasswordEditTXT.getText().toString().trim());
                    }
                    else{
                        vibrate(200);
                        mLoginProgressDialog.cancel();
                    }
                }else {
                    vibrate(200);
                    mLoginProgressDialog.cancel();
                }

            }
        });



        mGoogleSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInternetConnection(LoginActivity.this)){
                    mLoginProgressDialog.setCancelable(false);
                    mLoginProgressDialog.show();
                    startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
                }
            }
        });



        mGoSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this ,SignupActivity.class));
            }
        });



    }












    public static boolean assignSqlDB(String TableName, String column, String value) {
        SQLiteDatabase sqldb = LoginActivity.mDBHelper.getReadableDatabase();
        String Query = "Select * from " + TableName + " where " + column + " ='" + value+"'";
        Cursor cursor = sqldb.rawQuery(Query, null);
        cursor.moveToFirst();

        if(cursor.getCount() <= 0){
            mCurrentUserPath.edit().putString("current_path" , "").apply();
            mCurrentUserUri = userEntry.CONTENT_URI;
            cursor.close();
            return false;
        }
        else {
            mCurrentUserPath.edit().putString("current_path" , String.valueOf(cursor.getLong(0))).apply();
            mCurrentUserUri = Uri.withAppendedPath(userEntry.CONTENT_URI , mCurrentUserPath.getString("current_path" , ""));
            cursor.close();
            return true;
        }

    }



    public boolean isInFirebaseDB(String email, String password){
        if (mEmailList.contains(email)){
            if (password.equals(Email_Password.get(email))){
                return true;
            }else {
                Toasty.error(LoginActivity.this ,"The password is wrong or the account doesn't have password").show();
                return false;
            }
        }else {
            Toasty.error(LoginActivity.this ,"There is no account related to this email").show();
            return false;
        }
    }



    public boolean checkInternetConnection(Context context){
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toasty.warning(context, "No internet connection").show();
            return false;
        }
        return true;
    }



    public void loginUser (final String email , String password){

        mUserAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    boolean sqlResult = assignSqlDB(userEntry.TABLE_NAME, userEntry.COLUMN_User_Email, email);
                    int rowUpdated = 0;
                    Uri newUri = null;

                    if (sqlResult){
                        ContentValues values = new ContentValues();
                        values.put(UserContract.userEntry.COLUMN_User_LoginState, UserContract.userEntry.VALUE_True);
                        rowUpdated = getContentResolver().update(mCurrentUserUri , values , null , null);
                    }else {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        BitmapFactory.decodeResource(getResources(), R.drawable.ic_account).compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[] defaultProfilePic = baos.toByteArray();

                        ContentValues values = new ContentValues();
                        values.put(UserContract.userEntry.COLUMN_User_Email, email);
                        values.put(UserContract.userEntry.COLUMN_User_Username, "?");
                        values.put(UserContract.userEntry.COLUMN_User_Password, "");
                        values.put(UserContract.userEntry.COLUMN_User_LoginState, UserContract.userEntry.VALUE_True);
                        values.put(UserContract.userEntry.COLUMN_User_Bio, "?");
                        values.put(UserContract.userEntry.COLUMN_User_ProfilePic, defaultProfilePic);

                        newUri = LoginActivity.this.getContentResolver().insert(UserContract.userEntry.CONTENT_URI, values);

                        mCurrentUserUri = newUri;
                        mCurrentUserPath.edit().putString("current_path" , mCurrentUserUri.getLastPathSegment()).apply();
                    }

                    if (!(rowUpdated < 0) || newUri != null) {
                        mLoginProgressDialog.cancel();
                        startActivity(new Intent(LoginActivity.this ,MainActivity.class));
                        Toasty.success(LoginActivity.this, "Logged in successfully!").show();
                    }
                    else {
                        mLoginProgressDialog.cancel();
                        Toasty.error(LoginActivity.this, "Login failed").show();
                    }

                }else {
                    mLoginProgressDialog.cancel();
                    Toasty.error(LoginActivity.this, task.getException().getMessage()).show();
                    Log.e("*********",task.getException().toString() );
                }
            }
        });

    }



    public void loginUserWithGoogle(GoogleSignInAccount account){
        mUserAuth.signInWithCredential(GoogleAuthProvider.getCredential(account.getIdToken(), null))
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    mUser = LoginActivity.this.mUserAuth.getCurrentUser();

                    if (mEmailList.contains(mUser.getEmail())){
                        boolean sqlResult = assignSqlDB(userEntry.TABLE_NAME, userEntry.COLUMN_User_Email, mUser.getEmail());
                        int rowUpdated = 0;
                        Uri newUri = null;

                        if (sqlResult){
                            ContentValues values = new ContentValues();
                            values.put(UserContract.userEntry.COLUMN_User_LoginState, UserContract.userEntry.VALUE_True);
                            rowUpdated = getContentResolver().update(mCurrentUserUri , values , null , null);
                        }else {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            BitmapFactory.decodeResource(getResources(), R.drawable.ic_account).compress(Bitmap.CompressFormat.PNG, 100, baos);
                            byte[] defaultProfilePic = baos.toByteArray();

                            ContentValues values = new ContentValues();
                            values.put(UserContract.userEntry.COLUMN_User_Email, mUser.getEmail());
                            values.put(UserContract.userEntry.COLUMN_User_Username, "?");
                            values.put(UserContract.userEntry.COLUMN_User_Password, "");
                            values.put(UserContract.userEntry.COLUMN_User_LoginState, UserContract.userEntry.VALUE_True);
                            values.put(UserContract.userEntry.COLUMN_User_Bio, "?");
                            values.put(UserContract.userEntry.COLUMN_User_ProfilePic, defaultProfilePic);

                            newUri = LoginActivity.this.getContentResolver().insert(UserContract.userEntry.CONTENT_URI, values);

                            mCurrentUserUri = newUri;
                            mCurrentUserPath.edit().putString("current_path" , mCurrentUserUri.getLastPathSegment()).apply();
                        }

                        if (!(rowUpdated < 0) || newUri != null) {
                            mLoginProgressDialog.cancel();
                            startActivity(new Intent(LoginActivity.this ,MainActivity.class));
                            Toasty.success(LoginActivity.this, "Logged in successfully!").show();
                        }
                        else {
                            mLoginProgressDialog.cancel();
                            Toasty.error(LoginActivity.this, "Login failed").show();
                        }

                    }else {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        BitmapFactory.decodeResource(getResources(), R.drawable.ic_account).compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[] defaultProfilePic = baos.toByteArray();

                        ContentValues values = new ContentValues();
                        values.put(UserContract.userEntry.COLUMN_User_Email, mUser.getEmail());
                        values.put(UserContract.userEntry.COLUMN_User_Username, mUser.getDisplayName().trim());
                        values.put(UserContract.userEntry.COLUMN_User_Password, "");
                        values.put(UserContract.userEntry.COLUMN_User_LoginState, UserContract.userEntry.VALUE_True);
                        values.put(UserContract.userEntry.COLUMN_User_Bio, "");
                        values.put(UserContract.userEntry.COLUMN_User_ProfilePic, defaultProfilePic);

                        Uri newUri = LoginActivity.this.getContentResolver().insert(UserContract.userEntry.CONTENT_URI, values);

                        if (newUri == null) {
                            mLoginProgressDialog.cancel();
                            Toasty.error(LoginActivity.this, "Sign up failed").show();
                        } else {
                            mCurrentUserUri = newUri;
                            mCurrentUserPath.edit().putString("current_path" , mCurrentUserUri.getLastPathSegment()).apply();
                            mUserItem = new UserItem(mUser.getEmail(), mUser.getDisplayName(), "", "", (mUser.getPhotoUrl()==null) ? "":mUser.getPhotoUrl().toString());
                            mUserDatabaseRef.child(mUser.getUid()).setValue(mUserItem);
                            mLoginProgressDialog.cancel();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            Toasty.success(LoginActivity.this, "Signed up successfully!").show();
                        }
                    }


                }else {
                    mLoginProgressDialog.cancel();
                    Toasty.error(LoginActivity.this, task.getException().getMessage()).show();
                    Log.e("*********",task.getException().toString() );
                }
            }
        });
    }



    public void vibrate(int duration) {
        Vibrator vibs = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibs.vibrate(duration);
    }




}
