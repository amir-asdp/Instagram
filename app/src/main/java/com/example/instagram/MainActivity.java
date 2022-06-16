package com.example.instagram;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

import static com.example.instagram.SignupActivity.mCurrentUserPath;
import static com.example.instagram.StartupActivity.mCurrentUserUri;
import static com.example.instagram.UserContract.userEntry;
import static es.dmoral.toasty.Toasty.LENGTH_LONG;

public class MainActivity extends AppCompatActivity {

    public static FirebaseDatabase mUserDatabase;
    public static DatabaseReference mUserDatabaseRef;
    public static FirebaseDatabase mPostDatabase;
    public static DatabaseReference mPostDatabaseRef;
    public static FirebaseUser mCurrentUser;
    public static UserItem mCurrentUserItem;
    public static ArrayList<DataSnapshot> mAllUserItemsList = new ArrayList<>();
    public static ArrayList<DataSnapshot> mAllUsersPostList = new ArrayList<>();
    public static ArrayList<PostItem> mSearchFragmentPostList = new ArrayList<>();
    public static ArrayList<PostItem> mCurrentUserPostList = new ArrayList<>();
    public static PostBriefAdapter mPostBriefAdapter;
    public static PostDetailedAdapter mPostDetailedAdapter;
    public static SearchPostAdapter mSearchPostAdapter;
    public static int mNumberOfCurrentUserPosts;
    public static ValueEventListener mUserValueEventListener;
    public static ValueEventListener mPostValueEventListener;
    CustomProgressDialogClass mLoadingProgressDialog;


    TabLayout tabLayout;
    ViewPager viewPager;
    static UserDBHelper mDBHelper;



    @Override
    protected void onResume() {
        super.onResume();
        mCurrentUserPostList.clear();
        if (checkInternetConnection(MainActivity.this)){
            mUserDatabaseRef.addValueEventListener(mUserValueEventListener);
            mPostDatabaseRef.orderByChild("mDate").addValueEventListener(mPostValueEventListener);
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        mUserDatabaseRef.removeEventListener(mUserValueEventListener);
        mPostDatabaseRef.removeEventListener(mPostValueEventListener);
        mCurrentUserPostList.clear();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mUserDatabase = FirebaseDatabase.getInstance();
        mUserDatabaseRef = mUserDatabase.getReference().child("users");
        mPostDatabase = FirebaseDatabase.getInstance();
        mPostDatabaseRef = mPostDatabase.getReference().child("posts");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mPostBriefAdapter = new PostBriefAdapter(MainActivity.this, mCurrentUserPostList);
        mPostDetailedAdapter = new PostDetailedAdapter(MainActivity.this, mAllUsersPostList, mAllUserItemsList);
        mSearchPostAdapter = new SearchPostAdapter(MainActivity.this, mSearchFragmentPostList);


        if (checkInternetConnection(MainActivity.this)){
            mLoadingProgressDialog = new CustomProgressDialogClass(MainActivity.this, "Loading ...");
            mLoadingProgressDialog.setCancelable(false);
            mLoadingProgressDialog.show();
            mUserValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mAllUserItemsList.clear();
                    for (DataSnapshot user : dataSnapshot.getChildren()){
                        mAllUserItemsList.add(user);
                        mPostDetailedAdapter.notifyDataSetChanged();
                        if (user.getKey().equals(mCurrentUser.getUid())){
                            mCurrentUserItem = user.getValue(UserItem.class);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mPostValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mCurrentUserPostList.clear();
                    mAllUsersPostList.clear();
                    mSearchFragmentPostList.clear();
                    if (dataSnapshot.getValue() != null){
                        for (DataSnapshot post : dataSnapshot.getChildren()){
                            mAllUsersPostList.add(post);
                            mPostDetailedAdapter.notifyDataSetChanged();
                            mSearchFragmentPostList.add(post.getValue(PostItem.class));
                            mSearchPostAdapter.notifyDataSetChanged();
                            if (post.getValue(PostItem.class).getmUid().equals(mCurrentUser.getUid())){
                                mCurrentUserPostList.add(post.getValue(PostItem.class));
                                mPostBriefAdapter.notifyDataSetChanged();
                            }
                        }
                        mNumberOfCurrentUserPosts = mCurrentUserPostList.size();
                    }
                    mLoadingProgressDialog.cancel();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
        }


        mCurrentUserUri = Uri.withAppendedPath(userEntry.CONTENT_URI , mCurrentUserPath.getString("current_path" , ""));
        mDBHelper = new UserDBHelper(getApplicationContext());
        tabLayout=findViewById(R.id.tablayout);
        viewPager=findViewById(R.id.viewpager);
        ViewPagerAdapter viewPagerAdapter=new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        if(mCurrentUserPath == null){
            mCurrentUserPath = getSharedPreferences("current_path" , MODE_PRIVATE);
        }



        tabLayout.getTabAt(0).setIcon(R.drawable.home_fill);
        tabLayout.getTabAt(1).setIcon(R.drawable.search_normal);
        tabLayout.getTabAt(2).setIcon(R.drawable.add_empty);
        tabLayout.getTabAt(3).setIcon(R.drawable.profile_empty);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position==0){
                    tabLayout.getTabAt(0).setIcon(R.drawable.home_fill);
                    tabLayout.getTabAt(1).setIcon(R.drawable.search_normal);
                    tabLayout.getTabAt(2).setIcon(R.drawable.add_empty);
                    tabLayout.getTabAt(3).setIcon(R.drawable.profile_empty);
                }
                else if (position==1) {
                    tabLayout.getTabAt(0).setIcon(R.drawable.home_empty);
                    tabLayout.getTabAt(1).setIcon(R.drawable.search_focused);
                    tabLayout.getTabAt(2).setIcon(R.drawable.add_empty);
                    tabLayout.getTabAt(3).setIcon(R.drawable.profile_empty);
                }
                else if (position==2) {
                    tabLayout.getTabAt(0).setIcon(R.drawable.home_empty);
                    tabLayout.getTabAt(1).setIcon(R.drawable.search_normal);
                    tabLayout.getTabAt(2).setIcon(R.drawable.add_fill);
                    tabLayout.getTabAt(3).setIcon(R.drawable.profile_empty);
                }
                else {
                    tabLayout.getTabAt(0).setIcon(R.drawable.home_empty);
                    tabLayout.getTabAt(1).setIcon(R.drawable.search_normal);
                    tabLayout.getTabAt(2).setIcon(R.drawable.add_empty);
                    tabLayout.getTabAt(3).setIcon(R.drawable.profile_fill);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main ,menu);
        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        MainActivity mainActivity = MainActivity.this;
        switch (item.getItemId()){
            case R.id.logout_btn:
               AccountOptionDialogClass logoutDialog = new AccountOptionDialogClass(MainActivity.this , mainActivity ,
                       "Do you want to logout ?" ,"Yes" ,"No" ,"logout");
                logoutDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
               logoutDialog.show();
                break;

            case R.id.delete_account_btn:
                AccountOptionDialogClass deleteAccountDialog = new AccountOptionDialogClass(MainActivity.this , mainActivity ,
                        "Do you really want to delete your account ?!" ,"Yes" ,"No" ,"delete_account");
                deleteAccountDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                deleteAccountDialog.show();
                break;

            case R.id.about_btn:
                Toasty.info(MainActivity.this, "\n  Developed By :\n  Amirmohammad Asadpour\n\n  Email :\n  ossoft.app@gmail.com\n", LENGTH_LONG).show();

        }

        return super.onOptionsItemSelected(item);
    }



    public boolean checkInternetConnection(Context context){
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toasty.warning(context, "No internet connection").show();
            return false;
        }
        return true;
    }



    @Override
    public void onBackPressed() {
        Quit();
    }



    protected  void Quit() {
        finishAffinity();
    }



}
