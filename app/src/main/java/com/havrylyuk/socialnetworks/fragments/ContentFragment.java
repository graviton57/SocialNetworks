package com.havrylyuk.socialnetworks.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.havrylyuk.socialnetworks.R;
import com.havrylyuk.socialnetworks.model.SocialType;
import com.havrylyuk.socialnetworks.util.Utility;

/**
 *
 * Created by Igor Havrylyuk on 17.03.2017.
 */

public class ContentFragment extends Fragment {

    public static final String CONTENT_TAG = "com.havrylyuk.socialnetworks.content_tag";
    public static final String ARG_SOCIAL_ID = "extra_arg_sosial_id";

    private ImageView socialLogo;
    private SocialType socialType;

    public ContentFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_content, container, false);
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab_share);
        if (fab != null) {
            fab.setVisibility(View.VISIBLE);
        }
        socialLogo = (ImageView) rootView.findViewById(R.id.social_logo);
        if (getArguments() != null) {
            socialType = SocialType.values()[getArguments().getInt(ARG_SOCIAL_ID)];
        }
        switch (socialType) {
            case FACEBOOK:
                setSocialLogo(R.drawable.facebook);
                break;
            case TWITTER:
                setSocialLogo(R.drawable.twitter);
                break;
            case GOOGLE:
                setSocialLogo(R.drawable.google);
                break;
            default:
                setSocialLogo(R.drawable.user);
        }
        return rootView;
    }

    public void setSocialLogo(int imageId) {
        if (socialLogo != null) {
            Utility.load(imageId, socialLogo);
        }
    }

}
