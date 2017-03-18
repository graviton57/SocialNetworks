package com.havrylyuk.socialnetworks.util;

import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.havrylyuk.socialnetworks.model.SocialProfile;
import com.havrylyuk.socialnetworks.model.SocialType;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Igor Havrylyuk on 18.03.2017.
 */

public class FacebookRequests {

    public interface UserProfileCallback {
        void onCompleted(SocialProfile profile);
    }

    public static GraphRequest infoRequest(AccessToken accessToken, final UserProfileCallback callback){
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                SocialProfile userProfile = null;
                try {
                    String name =  jsonObject.getString("name");
                    String email = jsonObject.has("email") ? jsonObject.getString("email") : null;
                    String birthday = jsonObject.has("birthday") ? jsonObject.getString("birthday") : null;
                    String pictureUrl = jsonObject.getJSONObject("picture").
                            getJSONObject("data").getString("url");
                    userProfile = new SocialProfile(SocialType.FACEBOOK, name, email, pictureUrl);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                callback.onCompleted(userProfile);
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "picture{url},name,birthday,email");
        request.setParameters(parameters);
        return request;
    }

    public static GraphRequest postRequest(AccessToken accessToken, String message, GraphRequest.Callback callback) {
        Bundle params = new Bundle();
        params.putString("message", message);

        return new GraphRequest(
                accessToken,
                "/me/feed",
                params,
                HttpMethod.POST, callback
        );
    }
}
