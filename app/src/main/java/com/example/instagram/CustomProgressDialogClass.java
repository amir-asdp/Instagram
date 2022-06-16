package com.example.instagram;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class CustomProgressDialogClass extends Dialog {

    Context mContext;
    String mProgressText;

    public void setProgressText(String progressText) {
        this.mProgressText = progressText;
    }

    public CustomProgressDialogClass(@NonNull Context context, String progressText) {
        super(context);
        mContext =context;
        mProgressText = progressText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_custom_progress);
        TextView progressTxv = findViewById(R.id.progress_txt_dialog);
        ProgressBar progressBar = findViewById(R.id.progress_bar_dialog);
        progressTxv.setText(mProgressText);
    }



}
