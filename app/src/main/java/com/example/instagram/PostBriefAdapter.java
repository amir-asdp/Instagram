package com.example.instagram;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class PostBriefAdapter extends RecyclerView.Adapter<PostBriefAdapter.PostBriefViewHolder> {

    Context mContext;
    ArrayList<PostItem> mPostList;



    protected PostBriefAdapter (Context context , ArrayList<PostItem> postList){
        mContext = context;
        mPostList = postList;
    }



    @NonNull
    @Override
    public PostBriefViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemRootView = LayoutInflater.from(mContext).inflate(R.layout.item_post_brief ,parent ,false);
        GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) itemRootView.getLayoutParams();
        lp.height = parent.getMeasuredWidth() / 3;
        itemRootView.setLayoutParams(lp);
        return new PostBriefViewHolder(itemRootView);
    }



    @Override
    public void onBindViewHolder(@NonNull PostBriefViewHolder holder, int position) {
        Uri uri = Uri.parse(mPostList.get(position).getmPostPicUrl());
        holder.bind(mContext, uri);
    }



    @Override
    public int getItemCount() {
        return mPostList.size();
    }



    static class PostBriefViewHolder extends RecyclerView.ViewHolder{

        ImageView mPostPicImv;

        public PostBriefViewHolder(@NonNull View itemView) {
            super(itemView);
            mPostPicImv = itemView.findViewById(R.id.post_imv_brief);
        }

        public void bind(Context context, Uri uri){
            Glide.with(context).load(uri).placeholder(R.drawable.loading_placeholder).into(mPostPicImv);
        }

    }


}


