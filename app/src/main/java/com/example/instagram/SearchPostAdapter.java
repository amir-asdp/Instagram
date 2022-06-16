package com.example.instagram;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchPostAdapter extends RecyclerView.Adapter<SearchPostAdapter.SearchPostViewHolder> {

    Context mContext;
    ArrayList<PostItem> mPostList;

    public SearchPostAdapter(Context mContext, ArrayList<PostItem> mPostList) {
        this.mContext = mContext;
        this.mPostList = mPostList;
    }



    @NonNull
    @Override
    public SearchPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemRootView = LayoutInflater.from(mContext).inflate(R.layout.item_post_in_search ,parent ,false);
        return new SearchPostViewHolder(itemRootView);
    }



    @Override
    public void onBindViewHolder(@NonNull SearchPostViewHolder holder, int position) {
        holder.bind(mContext, mPostList, position);
    }



    @Override
    public int getItemCount() {
        return (mPostList.size()%12 == 0) ? mPostList.size()/12 : (mPostList.size()/12) + 1;
    }



    static class SearchPostViewHolder extends RecyclerView.ViewHolder{

        ImageView mPostGrid1, mPostGrid2, mPostGrid3, mPostGrid4, mPostGrid5, mPostGrid6, mPostGrid7, mPostGrid8, mPostGrid9, mPostGrid10, mPostGrid11, mPostGrid12;
        HashMap<Integer,ImageView> mViewMap = new HashMap<>();

        public SearchPostViewHolder(@NonNull View itemView) {
            super(itemView);
            mPostGrid1 = itemView.findViewById(R.id.grid_1);
            mPostGrid2 = itemView.findViewById(R.id.grid_2);
            mPostGrid3 = itemView.findViewById(R.id.grid_3);
            mPostGrid4 = itemView.findViewById(R.id.grid_4);
            mPostGrid5 = itemView.findViewById(R.id.grid_5);
            mPostGrid6 = itemView.findViewById(R.id.grid_6);
            mPostGrid7 = itemView.findViewById(R.id.grid_7);
            mPostGrid8 = itemView.findViewById(R.id.grid_8);
            mPostGrid9 = itemView.findViewById(R.id.grid_9);
            mPostGrid10 = itemView.findViewById(R.id.grid_10);
            mPostGrid11 = itemView.findViewById(R.id.grid_11);
            mPostGrid12 = itemView.findViewById(R.id.grid_12);

            mViewMap.clear();
            mViewMap.put(0, mPostGrid1);
            mViewMap.put(1, mPostGrid2);
            mViewMap.put(2, mPostGrid3);
            mViewMap.put(3, mPostGrid4);
            mViewMap.put(4, mPostGrid5);
            mViewMap.put(5, mPostGrid6);
            mViewMap.put(6, mPostGrid7);
            mViewMap.put(7, mPostGrid8);
            mViewMap.put(8, mPostGrid9);
            mViewMap.put(9, mPostGrid10);
            mViewMap.put(10, mPostGrid11);
            mViewMap.put(11, mPostGrid12);
        }

        public void bind(Context context, ArrayList<PostItem> postList, int position){
            for (int i=position*12 ; i<(position+1)*12 ; i++){
                if (i < postList.size()){
                    PostItem postItem = postList.get(i);
                    if (postItem != null){
                        Glide.with(context).load(postItem.getmPostPicUrl()).placeholder(R.drawable.loading_placeholder).centerCrop().into(mViewMap.get(i%12));
                    }
                }
            }
        }

    }


}
