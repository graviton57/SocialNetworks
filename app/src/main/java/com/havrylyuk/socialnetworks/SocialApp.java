package com.havrylyuk.socialnetworks;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.facebook.FacebookSdk;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;



/**
 * Created by Igor Havrylyuk on 17.03.2017.
 */

public class SocialApp extends MultiDexApplication {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Picasso picasso = new Picasso.Builder(this).build();
        Picasso.setSingletonInstance(picasso);
        FacebookSdk.sdkInitialize(this);
        TwitterAuthConfig authConfig =
                new TwitterAuthConfig(BuildConfig.TWITTER_KEY, BuildConfig.TWITTER_KEY_SECRET);
        Fabric.with(getApplicationContext(), new Twitter(authConfig));
    }
}
