package com.havrylyuk.socialnetworks.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.PlusShare;
import com.havrylyuk.socialnetworks.R;
import com.havrylyuk.socialnetworks.enents.AccessEvent;
import com.havrylyuk.socialnetworks.fragments.ContentFragment;
import com.havrylyuk.socialnetworks.fragments.LoginFragment;
import com.havrylyuk.socialnetworks.model.SocialProfile;
import com.havrylyuk.socialnetworks.util.Utility;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener ,
        NavigationView.OnNavigationItemSelectedListener{

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private SocialProfile registeredUser;
    private ImageView userAvatar;
    private TextView userName;
    private TextView userEmail;
    private GoogleApiClient googleApiClient;
    private Uri selectedImageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initShareButton();
        EventBus.getDefault().register(this);
        googleInitialize();
        //Utility.facebookHashKey(getApplicationContext());
        initDrawer();
        switchToLoginFragment();
    }

    private void initShareButton() {
        FloatingActionButton share = (FloatingActionButton) findViewById(R.id.fab_share);
        if (share != null) {
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (registeredUser != null) {
                        String message = getString(R.string.content_text);
                        selectedImageUri = Uri.parse(registeredUser.getUserAvatar());
                        switch (registeredUser.getSocialType()) {
                            case FACEBOOK:
                                facebookShare(message, selectedImageUri);
                                break;
                            case TWITTER:
                                twitterShare(message, selectedImageUri);
                                break;
                            case GOOGLE:
                                googleShare(message, selectedImageUri);
                        }
                    }
                }
            });
        }
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        if(googleApiClient != null && googleApiClient.isConnected()){
            googleApiClient.stopAutoManage(this);
            googleApiClient.disconnect();
        }
        super.onDestroy();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AccessEvent event) {
        this.registeredUser = event.getProfile();
        if (registeredUser != null) {
            updateUserInfo(registeredUser);
            Log.d(LOG_TAG," name="+event.getProfile().getUserName()+" email="+event.getProfile().getUserEmail());
            switchToContentFragment();
        }
    }

    private void initDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        userAvatar = (ImageView) header.findViewById(R.id.menu_user_image);
        userName = (TextView) header.findViewById(R.id.menu_user_name);
        userEmail = (TextView) header.findViewById(R.id.menu_user_email);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "MainActivity onResult request: " + requestCode + " result: " + resultCode + " data: " + data);
        Fragment fragment = getFragmentManager().findFragmentByTag(LoginFragment.LOGIN_TAG);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_logout) {
            logOut();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void updateUserInfo(SocialProfile profile) {
        if (userEmail != null) {
            if (!TextUtils.isEmpty(profile.getUserEmail()))
            userEmail.setText(profile.getUserEmail());
        } else {
            userEmail.setText(profile.getScreenName());
        }
        if (userAvatar != null) {
            Log.d(LOG_TAG,"updateUserInfo image="+profile.getUserAvatar());
            Utility.loadCircle(profile.getUserAvatar(), userAvatar);
        }
        if (userName != null) {
            userName.setText(profile.getUserName());
        }
    }

    private void logOut() {
        if (registeredUser != null) {
            Log.d(LOG_TAG, "MainActivity LogOut socialType: " + registeredUser.getSocialType().ordinal());
            switch (registeredUser.getSocialType()) {
                case FACEBOOK:
                    LoginManager.getInstance().logOut();
                    switchToLoginFragment();
                    break;
                case TWITTER:
                    Twitter.getSessionManager().clearActiveSession();
                    switchToLoginFragment();
                    break;
                case GOOGLE:
                    Auth.GoogleSignInApi.signOut(googleApiClient)
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                Log.d(LOG_TAG, "Google logout status: " + status);
                                switchToLoginFragment();
                            }
                        });
                    break;
            }
        } else Toast.makeText(this,"Unregistered!",Toast.LENGTH_SHORT).show();

    }

    public void switchToLoginFragment() {
        Fragment frag = getFragmentManager().findFragmentByTag(ContentFragment.CONTENT_TAG);
        FragmentTransaction fTrans = getFragmentManager().beginTransaction();
        Fragment mainFragment = new LoginFragment();
        if (frag != null) {
            fTrans.replace(R.id.fragment_container, mainFragment, LoginFragment.LOGIN_TAG);
        } else {
            fTrans.add(R.id.fragment_container, mainFragment, LoginFragment.LOGIN_TAG);
        }
        fTrans.commit();
    }

    private void switchToContentFragment() {
        if (registeredUser != null) {
            Bundle args = new Bundle();
            args.putInt(ContentFragment.ARG_SOCIAL_ID, registeredUser.getSocialType().ordinal());
            Fragment contentFragment = new ContentFragment();
            contentFragment.setArguments(args);
            FragmentTransaction fTrans = getFragmentManager().beginTransaction();
            fTrans.replace(R.id.fragment_container, contentFragment, ContentFragment.CONTENT_TAG);
            fTrans.commit();
        }
    }

    private void googleInitialize() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
            googleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this , null)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
    }

    private void facebookShare(String message, Uri photo){
        if (photo != null) {
            SharePhoto sharePhoto = new SharePhoto.Builder()
                    .setImageUrl(photo)
                    .setCaption(message)
                    .build();
            SharePhotoContent content = new SharePhotoContent.Builder()
                    .addPhoto(sharePhoto)
                    .build();
            ShareDialog.show(this, content);
        }
    }

    private void twitterShare(String _message, Uri selectedImageUri) {
        TweetComposer.Builder builder = new TweetComposer.Builder(this)
                .text(_message);
        if (selectedImageUri != null) {
            builder.image(selectedImageUri);
        }
        builder.show();
    }

    private void googleShare(String _message, Uri selectedImageUri) {
        String mime = getContentResolver().getType(selectedImageUri);
        Log.d(LOG_TAG, "Share mime: " + mime);
        Intent shareIntent = new PlusShare.Builder(this)
                .setType(mime)
                .setText(_message)
                .setStream(selectedImageUri)
                .getIntent();
        startActivityForResult(shareIntent, 0);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(LOG_TAG, "Google connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_TAG, "Google connection suspended " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(LOG_TAG, "Google connection failed");
    }
}
