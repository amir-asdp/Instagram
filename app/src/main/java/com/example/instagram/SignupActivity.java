package com.example.instagram;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

import static com.example.instagram.StartupActivity.mCurrentUserUri;
import static com.example.instagram.UserContract.userEntry.TABLE_NAME;

public class SignupActivity extends AppCompatActivity {

    FirebaseDatabase mUserDatabase;
    DatabaseReference mUserDatabaseRef;
    FirebaseAuth mUserAuth;
    FirebaseUser mUser;
    UserItem mUserItem;
    ArrayList<String> mEmailList = new ArrayList<>();
    ArrayList<String> mUsernameList = new ArrayList<>();
    ChildEventListener mUserChildEventListener;
    CustomProgressDialogClass mSignupProgressDialog;


    static EditText mEmailEditTXT;
    static EditText mUsernameEdittxt;
    static EditText mPasswordEditTXT;
    TextView mEmailValidOrNot;
    TextView mUsernameValidOrNot;
    TextView mPasswordStrongOrNot;
    static ImageView Check_green_Email;
    static ImageView Check_green_USername;
    ImageView mPasswordVisibler;
    Button mLoginButton;
    Button mSignupButton;
    int Password;
    private static UserDBHelper mDBHelper;
    public static SharedPreferences mCurrentUserPath;
    boolean visibleFlag = true;



    protected void Quit() {
        finishAffinity();
    }



    @Override
    public void onBackPressed() {
        Quit();
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (checkInternetConnection(SignupActivity.this) && mUserChildEventListener != null){
            mEmailList.clear();
            mUsernameList.clear();
            mUserDatabaseRef.addChildEventListener(mUserChildEventListener);
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        mEmailList.clear();
        mUsernameList.clear();
        mUserDatabaseRef.removeEventListener(mUserChildEventListener);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);



        mUserDatabase = FirebaseDatabase.getInstance();
        mUserDatabaseRef = mUserDatabase.getReference().child("users");
        mUserAuth = FirebaseAuth.getInstance();
        mUserChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                UserItem userItem = dataSnapshot.getValue(UserItem.class);
                mEmailList.add(userItem.getmEmail());
                mUsernameList.add(userItem.getmUsername());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("*********",databaseError.getMessage() + "//" + databaseError.getDetails() );
            }
        };
        mSignupProgressDialog = new CustomProgressDialogClass(SignupActivity.this, "Loading ...");
        mSignupProgressDialog.setCanceledOnTouchOutside(false);



        mEmailEditTXT = findViewById(R.id.email);
        mUsernameEdittxt = findViewById(R.id.username);
        mPasswordEditTXT = findViewById(R.id.password);
        mEmailValidOrNot=findViewById(R.id.emailtxt);
        mUsernameValidOrNot =findViewById(R.id.usernametxt);
        mPasswordStrongOrNot = findViewById(R.id.password_strong_weak);
        Check_green_Email=findViewById(R.id.green_check_email);
        Check_green_USername=findViewById(R.id.green_check_username);
        mPasswordVisibler = findViewById(R.id.passwordvisible);
        mLoginButton = findViewById(R.id.login);
        mSignupButton = findViewById(R.id.signup_btn);
        mDBHelper=new UserDBHelper(getApplicationContext());
        Intent intent = getIntent();
        mCurrentUserUri = intent.getData();
        if(mCurrentUserPath == null){
            mCurrentUserPath = getSharedPreferences("current_path" , MODE_PRIVATE);
        }



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



        mPasswordEditTXT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = mPasswordEditTXT.getText().toString().trim();
                if (password.length() > 0) {
                    passwordStrengthChecker(mPasswordEditTXT);
                    mPasswordStrongOrNot.setVisibility(View.VISIBLE);
                }
                else {
                    mPasswordStrongOrNot.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });



        mEmailEditTXT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email1=mEmailEditTXT.getText().toString();
                String email2=mEmailEditTXT.getText().toString().trim();

                if (!email1.contains(" ")) {
                    if (Patterns.EMAIL_ADDRESS.matcher(email2).matches()) {
                        mEmailValidOrNot.setVisibility(View.GONE);
                        mEmailValidOrNot.setText("");
                    } else if (email2.length() == 0) {
                        mEmailValidOrNot.setText("");
                        mEmailValidOrNot.setVisibility(View.GONE);
                        Check_green_Email.setVisibility(View.GONE);

                    } else {
                        mEmailValidOrNot.setText("Email is not valid");
                        mEmailValidOrNot.setVisibility(View.VISIBLE);
                        Check_green_Email.setVisibility(View.GONE);
                    }
                }else {
                    mEmailValidOrNot.setText("There is invalid character");
                    mEmailValidOrNot.setVisibility(View.VISIBLE);
                    Check_green_Email.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });



        mUsernameEdittxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mUsernameEdittxt.getText().toString().contains(" ")) {
                    if (mUsernameEdittxt.getText().toString().contains("@")) {
                        mUsernameValidOrNot.setText("@ is invalid character");
                        mUsernameValidOrNot.setVisibility(View.VISIBLE);
                        Check_green_USername.setVisibility(View.GONE);
                    } else if (mUsernameEdittxt.getText().toString().trim().length() == 0) {
                        mUsernameValidOrNot.setVisibility(View.GONE);
                        mUsernameValidOrNot.setText("");
                        Check_green_USername.setVisibility(View.GONE);
                    } else {
                        mUsernameValidOrNot.setVisibility(View.GONE);
                        mUsernameValidOrNot.setText("");
                        Check_green_USername.setVisibility(View.VISIBLE);
                    }
                }else {
                    mUsernameValidOrNot.setText("There is invalid character");
                    mUsernameValidOrNot.setVisibility(View.VISIBLE);
                    Check_green_USername.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });



        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this ,LoginActivity.class));
            }
        });



        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailEditTXT.getText().toString().trim();
                String username = mUsernameEdittxt.getText().toString().trim();
                String password = mPasswordEditTXT.getText().toString().trim();
                mSignupProgressDialog.setProgressText("Signing in ...");
                mSignupProgressDialog.setCancelable(false);
                mSignupProgressDialog.show();


                if (email.length()==0|| username.length()==0|| password.length()==0){
                    vibrate(200);
                    mSignupProgressDialog.cancel();
                    Toasty.error(SignupActivity.this, "All of the fields must be completed").show();
                }
                else if (checkInternetConnection(SignupActivity.this)){
                    if (!isInFirebaseDB(email, username) && mEmailValidOrNot.length()==0 && mUsernameValidOrNot.length()==0
                            && !mPasswordStrongOrNot.getText().toString().equals("Password is too Easy")) {
                        boolean saveResult = saveUser();
                    }
                    else {
                        vibrate(200);
                        mSignupProgressDialog.cancel();
                    }
                }else {
                    vibrate(200);
                    mSignupProgressDialog.cancel();
                }
            }
        });


    }












    public long getUsersCount() {
        SQLiteDatabase db = SignupActivity.mDBHelper.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        db.close();
        return count;
    }



    public void vibrate(int duration) {
        Vibrator vibs = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibs.vibrate(duration);
    }



    private boolean saveUser() {
        final String emailString = mEmailEditTXT.getText().toString().trim();
        final String usernameString = mUsernameEdittxt.getText().toString().trim();
        final String passwordString = mPasswordEditTXT.getText().toString().trim();
        final boolean[] saveResult = {true};

        mUserAuth.createUserWithEmailAndPassword(emailString, passwordString).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    BitmapFactory.decodeResource(getResources(), R.drawable.ic_account).compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] defaultProfilePic = baos.toByteArray();

                    ContentValues values = new ContentValues();
                    values.put(UserContract.userEntry.COLUMN_User_Email, emailString);
                    values.put(UserContract.userEntry.COLUMN_User_Username, usernameString);
                    values.put(UserContract.userEntry.COLUMN_User_Password, "");
                    values.put(UserContract.userEntry.COLUMN_User_LoginState, UserContract.userEntry.VALUE_True);
                    values.put(UserContract.userEntry.COLUMN_User_Bio, "");
                    values.put(UserContract.userEntry.COLUMN_User_ProfilePic, defaultProfilePic);

                    Uri newUri = SignupActivity.this.getContentResolver().insert(UserContract.userEntry.CONTENT_URI, values);

                    if (newUri == null) {
                        mSignupProgressDialog.cancel();
                        Toasty.error(SignupActivity.this, "Sign up failed").show();
                    } else {
                        mCurrentUserUri = newUri;
                        mCurrentUserPath.edit().putString("current_path" , mCurrentUserUri.getLastPathSegment()).apply();
                        mUser = SignupActivity.this.mUserAuth.getCurrentUser();
                        mUserItem = new UserItem(emailString, usernameString, passwordString, "", "");
                        mUserDatabaseRef.child(mUser.getUid()).setValue(mUserItem);
                        mSignupProgressDialog.cancel();
                        startActivity(new Intent(SignupActivity.this, MainActivity.class));
                        Toasty.success(SignupActivity.this, "Signed up successfully!").show();
                    }

                }else {
                    saveResult[0] = false;
                    mSignupProgressDialog.cancel();
                    Toasty.error(SignupActivity.this, task.getException().getMessage()).show();
                    Log.e("*********",task.getException().toString() );
                }
            }
        });

        return saveResult[0];

    }



    public static boolean isInSqlDB(String TableName, String column, String value ) {
        SQLiteDatabase sqldb = SignupActivity.mDBHelper.getReadableDatabase();
        String Query = "Select * from " + TableName + " where " + column + " ='" + value+"'";
        Cursor cursor = sqldb.rawQuery(Query, null);

        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }

        cursor.close();
        return true;
    }



    public boolean isInFirebaseDB(String email, String username){
        if (mEmailList.contains(email)){
            Toasty.error(SignupActivity.this ,"The Email is already used by another account").show();
            return true;
        }else if (mUsernameList.contains(username)){
            Toasty.error(SignupActivity.this ,"The Username is already used by another account").show();
            return true;
        }
        return false;
    }



    public boolean checkInternetConnection(Context context){
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toasty.warning(context, "No internet connection").show();
            return false;
        }
        return true;
    }



    public void passwordStrengthChecker(EditText s) {
        int len = s.getText().toString().length();
        if (!s.getText().toString().contains(" ")) {
            if (len < 8) {
                mPasswordStrongOrNot.setText("Password is too Easy");
                mPasswordStrongOrNot.setTextColor(getResources().getColor(R.color.red));
                Password = 0;
            } else if (len < 15) {
                mPasswordStrongOrNot.setText("Password is strong");
                mPasswordStrongOrNot.setTextColor(getResources().getColor(R.color.blue));
                Password = 2;
            } else {
                mPasswordStrongOrNot.setText("Password is the strongest");
                mPasswordStrongOrNot.setTextColor(getResources().getColor(R.color.green));
                Password = 3;
            }

            if (len == 30) {
                mPasswordStrongOrNot.setText("Password Max Length Reached");
                mPasswordStrongOrNot.setTextColor(getResources().getColor(R.color.green));
                Password = 4;
            }
        }else {
            mPasswordStrongOrNot.setText("There is extra space");
            mPasswordStrongOrNot.setTextColor(getResources().getColor(R.color.red));
        }
    }


}
