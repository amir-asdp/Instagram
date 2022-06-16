package com.example.instagram;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.instagram.MainActivity.mSearchPostAdapter;

public class SearchFragment extends Fragment {

    public SearchFragment() { }

    View mRootView;
    RecyclerView mSearchRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_search, container, false);
        mSearchRecyclerView = mRootView.findViewById(R.id.search_recycler_view);

        mSearchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mSearchRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mSearchRecyclerView.setAdapter(mSearchPostAdapter);

        return mRootView;
    }

}
