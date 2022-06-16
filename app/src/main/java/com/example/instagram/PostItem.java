package com.example.instagram;

public class PostItem {

    String mPostPicUrl, mCaption, mUid;
    long mDate;

    public String getmPostPicUrl() {
        return mPostPicUrl;
    }

    public String getmCaption() {
        return mCaption;
    }

    public String getmUid() {
        return mUid;
    }

    public long getmDate() {
        return mDate;
    }

    public PostItem(String postPicUrl, String uid, String caption, long date) {
        this.mPostPicUrl = postPicUrl;
        this.mCaption = caption;
        this.mUid = uid;
        this.mDate = date;
    }

    public PostItem (){}

}
