package com.havrylyuk.socialnetworks.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.havrylyuk.socialnetworks.R;
import com.havrylyuk.socialnetworks.activity.MainActivity;
import com.havrylyuk.socialnetworks.enents.AccessEvent;
import com.havrylyuk.socialnetworks.model.SocialProfile;
import com.havrylyuk.socialnetworks.model.SocialType;
import com.havrylyuk.socialnetworks.util.FacebookRequests;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;

import retrofit2.Call;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 *
 * Created by Igor Havrylyuk on 17.03.2017.
 */

public class LoginFragment extends Fragment  {

    public static final String LOGIN_TAG = "com.havrylyuk.socialnetworks.login_tag";
    public static final String LOG_TAG = LoginFragment.class.getSimpleName();

    //twitter
    private TwitterLoginButton twitterLoginButton;
    //facebook
    private CallbackManager fbCallbackManager;
    //google
    private static final int GOOGLE_SIGN_IN_REQUEST = 9001;

    public LoginFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab_share);
        if (fab != null) {
            fab.setVisibility(View.GONE);
        }
        googleInitialize(rootView);
        facebookInitialize(rootView);
        twitterInitialize(rootView);
        return rootView;
    }

    private void facebookInitialize(View view) {
        FacebookSdk.sdkInitialize(getApplicationContext(), new FacebookSdk.InitializeCallback() {
            @Override
            public void onInitialized() {
                Log.d("tag", "MainActivity facebook sdk initialize");
                if (AccessToken.getCurrentAccessToken() != null) {
                    getUserFromFacebook();
                }
            }
        });
        fbCallbackManager = CallbackManager.Factory.create();
        LoginButton facebookLoginButton = (LoginButton) view.findViewById(R.id.facebook_login_button);
        FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("tag", "LoginFragment mFacebookLoginButton onSuccess token: " + AccessToken.getCurrentAccessToken().getToken());
                getUserFromFacebook();
            }

            @Override
            public void onCancel() {
                Log.d(LOG_TAG, "Facebook Login Cancel");
            }

            @Override
            public void onError(FacebookException e) {
                Log.e(LOG_TAG, "Facebook Login Error "+e.getMessage());
            }

        };
        if (facebookLoginButton !=null){
            facebookLoginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_friends"));
            facebookLoginButton.registerCallback(fbCallbackManager, callback);
        } 
    }

    private void googleInitialize(View view) {
        SignInButton googleLoginButton = (SignInButton) view.findViewById(R.id.google_login_button);
        if (googleLoginButton != null) {
            googleLoginButton.setSize(SignInButton.SIZE_STANDARD);
            googleLoginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent signInIntent =
                            Auth.GoogleSignInApi.getSignInIntent(((MainActivity)getActivity()).getGoogleApiClient());
                    startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST);
                }
            });
            googleLoginButton.setSize(SignInButton.SIZE_STANDARD);
        }

    }

    private void twitterInitialize(View view) {
        TwitterSession twitterSession = Twitter.getSessionManager().getActiveSession();
        Log.d(LOG_TAG, "Twitter Initialize session: " + twitterSession);
        if (twitterSession != null) {
            Log.d(LOG_TAG, "Twitter Send  user data to activity");
            getUserFromTwitter(twitterSession);
        }
        twitterLoginButton = (TwitterLoginButton) view.findViewById(R.id.twitter_login_button);
        if (twitterLoginButton != null) {
            twitterLoginButton.setCallback(new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> result) {
                    Log.d(LOG_TAG, "TwitterInit login success");
                    TwitterSession session = Twitter.getSessionManager().getActiveSession();
                    getUserFromTwitter(session);
                }
                @Override
                public void failure(TwitterException exception) {
                    Log.d(LOG_TAG, "TwitterInit  login failure", exception);
                }
            });
        }
    }

    public void getUserFromFacebook() {
        FacebookRequests.infoRequest(AccessToken.getCurrentAccessToken(), new FacebookRequests.UserProfileCallback() {
            @Override
            public void onCompleted(SocialProfile profile) {
                EventBus.getDefault().post(new AccessEvent(profile));
            }
        }).executeAsync();
    }

    private void getUserFromTwitter(TwitterSession session) {
        Call<User> userResult = Twitter.getApiClient(session).getAccountService().verifyCredentials(true, false);
        userResult.enqueue(new Callback<User>() {
            @Override
            public void failure(TwitterException e) {
                Log.d(LOG_TAG, "Twitter profile read failure", e);
            }
            @Override
            public void success(Result<User> userResult) {
                User twitterUser = userResult.data;
                SocialProfile userProfile = new SocialProfile();
                try {
                    userProfile.setUserAvatar(twitterUser.profileImageUrl);
                    userProfile.setUserName(twitterUser.name);
                    userProfile.setSocialType(SocialType.TWITTER);
                    userProfile.setUserEmail(twitterUser.email);
                    userProfile.setScreenName(twitterUser.screenName);
                    EventBus.getDefault().post(new AccessEvent(userProfile));//magic
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getUserFromGoogle(GoogleSignInAccount acct) {
        Log.d(LOG_TAG, "Google account Name: " + acct.getDisplayName() +
                ", email: " + acct.getEmail() + ", photo: " + acct.getPhotoUrl());
        SocialProfile socialProfile = new SocialProfile(SocialType.GOOGLE);
        socialProfile.setUserAvatar(acct.getPhotoUrl().toString());
        socialProfile.setUserEmail(acct.getEmail());
        socialProfile.setUserName(acct.getDisplayName());
        EventBus.getDefault().post(new AccessEvent(socialProfile));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "LoginFragment onResult request: " + requestCode + " result: " + resultCode + " data: " + data);
        fbCallbackManager.onActivityResult(requestCode, resultCode, data);
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN_REQUEST) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                googleSignInResult(result);
        }
    }

    private void googleSignInResult(GoogleSignInResult result) {
        Log.d(LOG_TAG, "googleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            getUserFromGoogle(result.getSignInAccount());
        } else {
            Log.d(LOG_TAG, "Google Signed out, show unauthenticated UI." + result.getStatus().toString());
        }
    }

}
