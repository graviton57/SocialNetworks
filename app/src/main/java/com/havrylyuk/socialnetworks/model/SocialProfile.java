package com.havrylyuk.socialnetworks.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *
 * Created by Igor Havrylyuk on 17.03.2017.
 */

public class SocialProfile  {

    private SocialType socialType;
    private String userName;
    private String screenName;
    private String userEmail;
    private String userAvatar;

    public SocialProfile() {
    }

    public SocialProfile(SocialType socialType) {
        this.socialType = socialType;
    }

    public SocialProfile(SocialType socialType, String userName, String userEmail, String userAvatar) {
        this.socialType = socialType;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userAvatar = userAvatar;
    }

    public SocialType getSocialType() {
        return socialType;
    }

    public void setSocialType(SocialType socialType) {
        this.socialType = socialType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }
}
