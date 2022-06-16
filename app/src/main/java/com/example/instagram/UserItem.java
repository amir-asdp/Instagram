package com.example.instagram;

public class UserItem {

    public String mEmail, mUsername, mPassword, mBio, mProfilePicUrl;

    public UserItem (){}

    public UserItem (String email, String username, String password, String bio, String profilePicUrl){

        mEmail = email;
        mUsername= username;
        mPassword = password;
        mBio = bio;
        mProfilePicUrl = profilePicUrl;

    }

    public String getmEmail() {
        return mEmail;
    }

    public String getmUsername() {
        return mUsername;
    }

    public String getmPassword() {
        return mPassword;
    }

    public String getmBio() {
        return mBio;
    }

    public String getmProfilePicUrl() {
        return mProfilePicUrl;
    }




}
