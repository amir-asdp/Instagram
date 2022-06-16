package com.example.instagram;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.CONNECTIVITY_SERVICE;

public class AddMediaFragment extends Fragment {

    FirebaseDatabase mPostDatabase;
    DatabaseReference mPostDatabaseRef;
    FirebaseStorage mPostStorage;
    StorageReference mPostStorageRef;
    FirebaseUser mCurrentUser;
    PostItem mNewPost;
    CustomProgressDialogClass mUploadProgressDialog;


    ImageView mAddPostImg;
    TextView mAddPostTxt;
    Fragment mThisFragment;
    CropImage.ActivityResult mResultImage;
    Uri mImageUri;
    SetPostDialogClass mSetPostDialog;



    public AddMediaFragment() { }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK){
            Uri imageUri = CropImage.getPickImageResultUri(getContext() , data);
            if (CropImage.isReadExternalStoragePermissionsRequired(getContext() , imageUri)){
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
                mImageUri = mResultImage.getUri();
                mSetPostDialog = new SetPostDialogClass(getContext() ,mImageUri);
                mSetPostDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mSetPostDialog.show();
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = mResultImage.getError();
                Log.e("*********", error.getMessage());
            }

        }

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_media, container, false);

        mPostDatabase = FirebaseDatabase.getInstance();
        mPostDatabaseRef = mPostDatabase.getReference().child("posts");
        mPostStorage = FirebaseStorage.getInstance();
        mPostStorageRef = mPostStorage.getReference().child("posts");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();


        mAddPostImg = rootView.findViewById(R.id.add_post_btn);
        mAddPostTxt = rootView.findViewById(R.id.add_post_txv);
        mThisFragment = AddMediaFragment.this;

        mAddPostImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInternetConnection(getContext())){
                    CropImage.startPickImageActivity(getContext() , mThisFragment);
                }
            }
        });

        return rootView;
    }












    public class SetPostDialogClass extends Dialog implements android.view.View.OnClickListener {
        TextView mOkDialogBtn , mCancelDialogBtn;
        EditText mCaptionInput;
        ImageView mPostCheckImage;
        Context mContext;
        Uri mPhotoUri;


        public SetPostDialogClass(@NonNull Context context ,Uri photoUri) {
            super(context);
            mContext = context;
            mPhotoUri = photoUri;

        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_set_post);
            mOkDialogBtn = findViewById(R.id.ok_post);
            mCancelDialogBtn = findViewById(R.id.cancel_post);
            mCaptionInput = findViewById(R.id.caption_input);
            mPostCheckImage = findViewById(R.id.post_check_imv);
            mPostCheckImage.setImageURI(mImageUri);
            mOkDialogBtn.setOnClickListener(this);
            mCancelDialogBtn.setOnClickListener(this);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ok_post:
                    if (checkInternetConnection(mContext)){
                        mUploadProgressDialog = new CustomProgressDialogClass(getContext(), "Uploading ...");
                        mUploadProgressDialog.setCancelable(false);
                        mUploadProgressDialog.show();
                        savePost(mPhotoUri, mCaptionInput.getText().toString());
                    }
                    break;
                case R.id.cancel_post:
                    dismiss();
                    break;
                default:
                    break;
            }
            dismiss();
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public void savePost(Uri imageUri , final String caption){

        String fileName = fileNameMaker(imageUri.getLastPathSegment());
        mPostStorageRef.child(fileName).putFile(imageUri).addOnSuccessListener(mThisFragment.getActivity(), new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(mThisFragment.getActivity(), new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        mNewPost = new PostItem(uri.toString(), mCurrentUser.getUid(), caption, System.currentTimeMillis()*-1);
                        mPostDatabaseRef.push().setValue(mNewPost);
                        mUploadProgressDialog.cancel();
                    }
                });
            }
        });

    }



    private void startCrop(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(getContext() , mThisFragment);
    }



    public boolean checkInternetConnection(Context context){
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toasty.warning(context, "No internet connection").show();
            return false;
        }
        return true;
    }



    public String fileNameMaker (String input){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateAndTime = sdf.format(new Date());
        String fileName = "(" + currentDateAndTime + ")" + input;

        return fileName;
    }



    static class Utils {

        public Bitmap getImage(byte[] image) {
            return BitmapFactory.decodeByteArray(image, 0, image.length);
        }


        public static byte[] getBytes(Bitmap bitmap , InputStream inputStream) throws IOException {

            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG , 100000 , byteBuffer);
            int bufferSize = 2048;
            byte[] buffer = new byte[bufferSize];
            int len = 0;

            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        }


    }




}
