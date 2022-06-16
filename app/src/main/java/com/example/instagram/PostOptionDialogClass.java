package com.example.instagram;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import static com.example.instagram.MainActivity.mPostDatabaseRef;


public class PostOptionDialogClass extends Dialog implements View.OnClickListener {

    Context mContext;
    String mAction;
    PostItem mEditingPost;
    String mEditingPostKey;
    long mCurrentPostId;
    Uri mCurrentPostUri;
    String mCurrentCaption;
    String mNewCaption;

    public PostOptionDialogClass(@NonNull Context context , String action , PostItem editingPost , String editingPostKey) {
        super(context);
        mContext = context;
        mAction = action;
        mEditingPost = editingPost;
        mEditingPostKey = editingPostKey;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (mAction.equals("edit_caption")){
            setContentView(R.layout.dialog_edit_caption);
            mCurrentCaption = mEditingPost.getmCaption();
            mNewCaption = mCurrentCaption;
            final EditText editCaptionEditTxt = findViewById(R.id.edit_caption_input);
            TextView okTxv = findViewById(R.id.ok_post_edit);
            TextView cancelTxv = findViewById(R.id.cancel_post_edit);

            editCaptionEditTxt.setText(mCurrentCaption);
            editCaptionEditTxt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    mNewCaption = editCaptionEditTxt.getText().toString();
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });

            okTxv.setOnClickListener(this);
            cancelTxv.setOnClickListener(this);
        }else {
            setContentView(R.layout.dialog_delete_post);
            TextView yesTxv = findViewById(R.id.yes_delete_post);
            TextView noTxv = findViewById(R.id.no_delete_post);
            yesTxv.setOnClickListener(this);
            noTxv.setOnClickListener(this);
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View v) {

        switch (mAction) {


            case "edit_caption":
                switch (v.getId()){
                    case R.id.ok_post_edit:
                        mPostDatabaseRef.child(mEditingPostKey).child("mCaption").setValue(mNewCaption);
                        break;
                    case R.id.cancel_post_edit:
                        dismiss();
                        break;
                }
                break;


            case "delete_post":
                switch (v.getId()){
                    case R.id.yes_delete_post:
                        mPostDatabaseRef.child(mEditingPostKey).removeValue();
                        break;
                    case R.id.no_delete_post:
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