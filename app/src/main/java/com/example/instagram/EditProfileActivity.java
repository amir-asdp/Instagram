package com.example.instagram;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

import static com.example.instagram.MainActivity.mCurrentUser;
import static com.example.instagram.MainActivity.mCurrentUserItem;
import static com.example.instagram.StartupActivity.mCurrentUserUri;
import static com.example.instagram.UserContract.userEntry;

public class EditProfileActivity extends AppCompatActivity {

    FirebaseDatabase mUserDatabase;
    DatabaseReference mUserDatabaseRef;
    FirebaseStorage mProfilePicStorage;
    StorageReference mProfilePicStorageRef;
    ArrayList<String> mUsernameList = new ArrayList<>();
    ChildEventListener mUserChildEventListener;
    CustomProgressDialogClass mSaveProgressDialog;


    CircleImageView mProfilePic;
    TextView mChangeProfilePic, mEditUserNameValidation;
    EditText mUserNameEditTxt, mBioEditTxt;
    Button mSaveEditionBtn;
    CropImage.ActivityResult mResultImage;
    Uri mNewImageUri;
    SetProfilePicDialogClass mSetProfilePicDialog;



    @Override
    protected void onResume() {
        super.onResume();
        if (checkInternetConnection(EditProfileActivity.this) && mUserChildEventListener != null){
            mUsernameList.clear();
            mUserDatabaseRef.addChildEventListener(mUserChildEventListener);
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        mUsernameList.clear();
        mUserDatabaseRef.removeEventListener(mUserChildEventListener);
        mNewImageUri = null;
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK){
            Uri imageUri = CropImage.getPickImageResultUri(EditProfileActivity.this ,data);
            if (CropImage.isReadExternalStoragePermissionsRequired(EditProfileActivity.this , imageUri)){
                Uri uri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE} , 0);
            }
            else {
                startCrop(imageUri);
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            mResultImage = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                mNewImageUri = mResultImage.getUri();
                mSetProfilePicDialog = new SetProfilePicDialogClass(this);
                mSetProfilePicDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mSetProfilePicDialog.show();
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = mResultImage.getError();
                Log.e("*********", error.getMessage());
            }

        }

    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        mUserDatabase = FirebaseDatabase.getInstance();
        mUserDatabaseRef = mUserDatabase.getReference().child("users");
        mProfilePicStorage = FirebaseStorage.getInstance();
        mProfilePicStorageRef = mProfilePicStorage.getReference().child("profilePics");
        mProfilePicStorageRef.getDownloadUrl();
        mUserChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                UserItem userItem = dataSnapshot.getValue(UserItem.class);
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


        mProfilePic = findViewById(R.id.profile_pic_imv2);
        mChangeProfilePic = findViewById(R.id.change_profile);
        mEditUserNameValidation = findViewById(R.id.edit_username_validation);
        mUserNameEditTxt = findViewById(R.id.edit_username);
        mBioEditTxt = findViewById(R.id.edit_bio);
        mSaveEditionBtn = findViewById(R.id.save_profile_btn);



        mUserNameEditTxt.setText(mCurrentUserItem.getmUsername());
        mBioEditTxt.setText(mCurrentUserItem.getmBio());
        if (mCurrentUserItem.getmProfilePicUrl().length() != 0){
            Glide.with(EditProfileActivity.this).load(mCurrentUserItem.getmProfilePicUrl()).placeholder(R.drawable.ic_account).into(mProfilePic);
        }else {
            Glide.with(EditProfileActivity.this).load(R.drawable.ic_account).into(mProfilePic);
        }



        mUserNameEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mUserNameEditTxt.getText().toString().contains(" ")) {
                    if (mUserNameEditTxt.getText().toString().contains("@")) {
                        mEditUserNameValidation.setText("@ is invalid character");
                        mEditUserNameValidation.setVisibility(View.VISIBLE);
                    } else if (mUserNameEditTxt.getText().toString().trim().length() == 0) {
                        mEditUserNameValidation.setVisibility(View.GONE);
                        mEditUserNameValidation.setText("");
                    } else {
                        mEditUserNameValidation.setVisibility(View.GONE);
                        mEditUserNameValidation.setText("");
                    }
                }else {
                    mEditUserNameValidation.setText("There is invalid character");
                    mEditUserNameValidation.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void afterTextChanged(Editable s) { }
        });



        mChangeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.startPickImageActivity(EditProfileActivity.this);
            }
        });



        mSaveEditionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUserNameEditTxt.getText().toString().trim();
                String bio = mBioEditTxt.getText().toString();
                mSaveProgressDialog = new CustomProgressDialogClass(EditProfileActivity.this, "Saving changes ...");
                mSaveProgressDialog.setCancelable(false);
                mSaveProgressDialog.show();

                if (TextUtils.isEmpty(username)){
                    vibrate(200);
                    mSaveProgressDialog.cancel();
                    Toasty.error(EditProfileActivity.this, "Username can't be empty").show();
                }
                else {
                    if (checkInternetConnection(EditProfileActivity.this)){
                        if (TextUtils.isEmpty(mEditUserNameValidation.getText().toString().trim())) {
                            if (isInFirebaseDB(username) && username.equals(mCurrentUserItem.mUsername)){
                                saveProfileChanges(mCurrentUserUri ,mNewImageUri ,username ,bio);
                            }else if (!isInFirebaseDB(username)){
                                saveProfileChanges(mCurrentUserUri ,mNewImageUri ,username ,bio);
                            }else {
                                vibrate(200);
                                mSaveProgressDialog.cancel();
                                Toasty.error(EditProfileActivity.this ,"The Username is already used by another account").show();
                            }
                        }else {
                            vibrate(200);
                            mSaveProgressDialog.cancel();
                            Toasty.error(EditProfileActivity.this ,"Invalid character").show();
                        }
                    } else {
                        vibrate(200);
                        mSaveProgressDialog.cancel();
                    }
                }
            }
        });

    }






    public void vibrate(int duration) {
        Vibrator vibs = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibs.vibrate(duration);
    }



    public boolean checkInternetConnection(Context context){
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toasty.warning(context, "No internet connection").show();
            return false;
        }
        return true;
    }



    public boolean isInFirebaseDB(String username){
        if (mUsernameList.contains(username)){
            return true;
        }
        return false;
    }



    private void startCrop(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(EditProfileActivity.this);
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public void saveProfileChanges(final Uri currentUserUri , Uri imageUri , final String username , final String bio){

        final boolean[] profilePicResult = {false};
        final boolean[] usernameResult = {false};
        final boolean[] bioResult = {false};

        if (imageUri != null){
            String fileName = fileNameMaker(imageUri.getLastPathSegment());
            mProfilePicStorageRef.child(fileName).putFile(imageUri).addOnSuccessListener(EditProfileActivity.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(EditProfileActivity.this, new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            mUserDatabaseRef.child(mCurrentUser.getUid()).child("mProfilePicUrl").setValue(uri.toString());
                            mUserDatabaseRef.child(mCurrentUser.getUid()).child("mUsername").setValue(username);
                            mUserDatabaseRef.child(mCurrentUser.getUid()).child("mBio").setValue(bio);
                            ContentValues values = new ContentValues();
                            values.put(userEntry.COLUMN_User_Username ,username);
                            values.put(userEntry.COLUMN_User_Bio ,bio);
                            int rowUpdated = getContentResolver().update(currentUserUri , values , null , null);
                            mSaveProgressDialog.cancel();
                            Toasty.success(EditProfileActivity.this, "Changes saved successfully").show();
                            startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
                        }
                    });
                }
            });
        }else {
            mUserDatabaseRef.child(mCurrentUser.getUid()).child("mUsername").setValue(username);
            mUserDatabaseRef.child(mCurrentUser.getUid()).child("mBio").setValue(bio);
            ContentValues values = new ContentValues();
            values.put(userEntry.COLUMN_User_Username ,username);
            values.put(userEntry.COLUMN_User_Bio ,bio);
            int rowUpdated = getContentResolver().update(currentUserUri , values , null , null);
            mSaveProgressDialog.cancel();
            Toasty.success(EditProfileActivity.this, "Changes saved successfully").show();
            startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
        }

    }



    public String fileNameMaker (String input){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateAndTime = sdf.format(new Date());
        String fileName = "(" + currentDateAndTime + ")" + input;

        return fileName;
    }









    public class SetProfilePicDialogClass extends Dialog implements android.view.View.OnClickListener {
        TextView mOkDialogBtn , mCancelDialogBtn;
        ImageView mProfilePicCheckImage;


        public SetProfilePicDialogClass(@NonNull Context context ) {
            super(context);

        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_set_profile_pic);
            mOkDialogBtn = findViewById(R.id.ok_profile_pic);
            mCancelDialogBtn = findViewById(R.id.cancel_profile_pic);
            mProfilePicCheckImage = findViewById(R.id.profile_pic_check_imv);
            mProfilePicCheckImage.setImageURI(mNewImageUri);
            mOkDialogBtn.setOnClickListener(this);
            mCancelDialogBtn.setOnClickListener(this);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ok_profile_pic:

                    break;
                case R.id.cancel_profile_pic:
                    mNewImageUri = null;
                    dismiss();
                    break;
                default:
                    break;
            }
            dismiss();
        }
    }



}
