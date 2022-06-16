package com.example.instagram;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.instagram.MainActivity.mPostDetailedAdapter;
import static com.example.instagram.UserContract.postEntry;

public class HomeFragment extends Fragment {


    public HomeFragment() {

    }

    RecyclerView mDPostRecycleView;
    static View mRootView;
    private static UserDBHelper mDBHelper;
    Cursor mPostCursor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_home, container, false);
        mDPostRecycleView = mRootView.findViewById(R.id.home_recycler_view);
        mDBHelper = new UserDBHelper(getContext());

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String queryStatement = "Select * from " + postEntry.TABLE_NAME + " ORDER BY " + postEntry._ID + " DESC";
        Cursor cursor = db.rawQuery(queryStatement , null);
        cursor.moveToFirst();
        mPostCursor = cursor;


        mDPostRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        mDPostRecycleView.setItemAnimator(new DefaultItemAnimator());
        mDPostRecycleView.setAdapter(mPostDetailedAdapter);


        return mRootView;
    }



}
