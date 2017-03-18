package com.havrylyuk.socialnetworks.enents;

import com.havrylyuk.socialnetworks.model.SocialProfile;

/**
 * Created by Igor Havrylyuk on 18.03.2017.
 */

public class AccessEvent {

    private SocialProfile profile;

    public AccessEvent(SocialProfile profile) {
        this.profile = profile;
    }

    public SocialProfile getProfile() {
        return profile;
    }
}
