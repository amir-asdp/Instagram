package com.example.instagram;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static com.example.instagram.MainActivity.mCurrentUserItem;
import static com.example.instagram.MainActivity.mNumberOfCurrentUserPosts;
import static com.example.instagram.MainActivity.mPostBriefAdapter;

public class ProfileFragment extends Fragment {

    View rootView;
    ScrollView scrollView;
    Button editProfileBtn;
    CircleImageView profilePic;
    TextView postNum ,followerNum ,followingNum ,username ,bio;
    ExpandableRecyclerView mBPostRecyclerView;



    public ProfileFragment() { }



    @Override
    public void onResume() {
        super.onResume();
        scrollView.fullScroll(ScrollView.FOCUS_UP);
    }



    @Override
    public void onPause() {
        super.onPause();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        scrollView = rootView.findViewById(R.id.profile_parent);
        scrollView.fullScroll(ScrollView.FOCUS_UP);
        editProfileBtn = rootView.findViewById(R.id.edit_profile_btn);
        profilePic = rootView.findViewById(R.id.profile_pic_imv);
        postNum = rootView.findViewById(R.id.post_num);
        followerNum = rootView.findViewById(R.id.follower_num);
        followingNum = rootView.findViewById(R.id.following_num);
        username = rootView.findViewById(R.id.username_prof_fragment_txv);
        bio = rootView.findViewById(R.id.bio_txv);
        mBPostRecyclerView = rootView.findViewById(R.id.brief_grid_view);



        if (mCurrentUserItem.getmProfilePicUrl().length() != 0){
            Glide.with(getContext()).load(mCurrentUserItem.getmProfilePicUrl()).placeholder(R.drawable.ic_account).into(profilePic);
        }else {
            Glide.with(getContext()).load(R.drawable.ic_account).into(profilePic);
        }
        postNum.setText(String.valueOf(mNumberOfCurrentUserPosts));
        username.setText(mCurrentUserItem.getmUsername());
        bio.setText(mCurrentUserItem.getmBio());
        if(checkInternetConnection(getContext())){
            mBPostRecyclerView.setExpandState(true);
            mBPostRecyclerView.setLayoutManager(new GridLayoutManager(getContext() , 3));
            mBPostRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mBPostRecyclerView.addItemDecoration(new DividerItemDecoration(getContext() , GridLayoutManager.HORIZONTAL));
            mBPostRecyclerView.addItemDecoration(new DividerItemDecoration(getContext() , GridLayoutManager.VERTICAL));
            mBPostRecyclerView.setAdapter(mPostBriefAdapter);
        }else {
            mBPostRecyclerView.setVisibility(View.GONE);
        }



        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext() ,EditProfileActivity.class));
            }
        });



        return rootView;
    }












    public boolean checkInternetConnection(Context context){
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toasty.warning(context, "No internet connection").show();
            return false;
        }
        return true;
    }




}
