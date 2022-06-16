package com.example.instagram;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

import static com.example.instagram.MainActivity.mCurrentUser;

public class PostDetailedAdapter extends RecyclerView.Adapter<PostDetailedAdapter.PostDetailedViewHolder> {

    static Context mContext;
    ArrayList<DataSnapshot> mPostsList;
    ArrayList<DataSnapshot> mUsersList;

    public PostDetailedAdapter(Context mContext, ArrayList<DataSnapshot> mPostsList, ArrayList<DataSnapshot> mUsersList) {
        this.mContext = mContext;
        this.mPostsList = mPostsList;
        this.mUsersList = mUsersList;
    }






    @NonNull
    @Override
    public PostDetailedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemRootView = LayoutInflater.from(mContext).inflate(R.layout.item_post_detailed ,parent ,false);
        return new PostDetailedViewHolder(itemRootView);
    }



    @Override
    public void onBindViewHolder(@NonNull PostDetailedViewHolder holder, int position) {

        if (mPostsList!=null && mPostsList.size()!=0 && mUsersList!=null && mUsersList.size()!=0){
            PostItem mPostItem = mPostsList.get(position).getValue(PostItem.class);
            UserItem mUserOfPost = null;
            for (DataSnapshot userOfPost : mUsersList){
                if (userOfPost.getKey().equals(mPostItem.getmUid())){
                    mUserOfPost = userOfPost.getValue(UserItem.class);
                }
            }

            holder.mPostOptionImv.setTag(position);
            holder.bind(mPostsList, mPostItem, mUserOfPost);
        }

    }



    @Override
    public int getItemCount() {
        return mPostsList.size();
    }






    static class PostDetailedViewHolder extends RecyclerView.ViewHolder{

        CircleImageView mPostProfilePic;
        ImageView mPostOptionImv,mLikeImv,mCommentImv,mSendImv,mSaveImv,mPostPicImv;
        TextView mPostUsername,mCaptionTxv;
        View mDivider1,mDivider2;
        int currentTag;

        public PostDetailedViewHolder(@NonNull View itemView) {
            super(itemView);
            mPostProfilePic = itemView.findViewById(R.id.profile_pic_in_post_imv);
            mPostOptionImv = itemView.findViewById(R.id.post_option_btn);
            mLikeImv = itemView.findViewById(R.id.like_btn);
            mCommentImv = itemView.findViewById(R.id.comment_btn);
            mSendImv = itemView.findViewById(R.id.send_btn);
            mSaveImv = itemView.findViewById(R.id.save_btn);
            mPostPicImv = itemView.findViewById(R.id.post_imv);
            mPostUsername = itemView.findViewById(R.id.post_username);
            mCaptionTxv = itemView.findViewById(R.id.caption_txv);
            mDivider1 = itemView.findViewById(R.id.divider1);
            mDivider2 = itemView.findViewById(R.id.post_divider);
        }


        public void bind(final ArrayList<DataSnapshot> postList, final PostItem postItem, UserItem userOfPost){

            if (userOfPost.getmProfilePicUrl().length() != 0){
                Glide.with(mContext).load(userOfPost.getmProfilePicUrl()).placeholder(R.drawable.loading_placeholder).into(mPostProfilePic);
            }else {
                Glide.with(mContext).load(R.drawable.ic_account).into(mPostProfilePic);
            }
            mPostUsername.setText(userOfPost.getmUsername());
            Glide.with(mContext).load(postItem.mPostPicUrl).placeholder(R.drawable.loading_placeholder).fitCenter().into(mPostPicImv);
            mCaptionTxv.setText(postItem.getmCaption());

            final PopupMenu postPopup = new PopupMenu(mContext , mPostOptionImv);
            postPopup.inflate(R.menu.post_popup_menu);



            mPostOptionImv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentTag = (int) v.getTag();
                    if (postList.get(currentTag).getValue(PostItem.class).getmUid().equals(mCurrentUser.getUid())){
                        postPopup.show();
                    }else {
                        Toasty.warning(PostDetailedAdapter.mContext, "This is not your post\nYou can't do any action", Toasty.LENGTH_LONG).show();
                    }
                }
            });



            postPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    PostItem editingPost = postItem;
                    String editingPostKey = postList.get(currentTag).getKey();

                    switch (item.getItemId()){
                        case R.id.edit_caption_btn:
                            PostOptionDialogClass editCaptionDialog = new PostOptionDialogClass(mContext ,
                                    "edit_caption" ,editingPost ,editingPostKey);
                            editCaptionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            editCaptionDialog.show();
                            break;

                        case R.id.delete_post_btn:
                            PostOptionDialogClass deletePostDialog = new PostOptionDialogClass(mContext ,
                                    "delete_post" ,editingPost ,editingPostKey);
                            deletePostDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            deletePostDialog.show();
                            break;

                    }
                    return false;
                }
            });



        }


    }






    static class Utils {

        public static Bitmap getImage(byte[] image) {
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
