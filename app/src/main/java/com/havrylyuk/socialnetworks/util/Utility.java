package com.havrylyuk.socialnetworks.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Igor Havrylyuk on 18.03.2017.
 */

public class Utility {

    public static void load(int resId, ImageView imageView) {
        Picasso.with(imageView.getContext())
                .load(resId)
                .noFade()
                .into(imageView);
    }

    public static void loadCircle(@NonNull String url, ImageView imageView) {
        Picasso.with(imageView.getContext())
                .load(url)
                .transform(new CircleTransform())
                .noFade()
                .into(imageView);
    }

    public static void facebookHashKey(Context context){
        // Add code to print out the key hash
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    "com.havrylyuk.socialnetworks", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("Facebbok","KeyHash:"+ Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            //ignore
        } catch (NoSuchAlgorithmException e) {
            //ignore this
        }
    }
}
