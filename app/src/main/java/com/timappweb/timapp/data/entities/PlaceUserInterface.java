package com.timappweb.timapp.data.entities;

import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.data.models.User;

import java.util.List;

// TODO remove if not used anymore
public interface PlaceUserInterface {

    //String getUsername();

    //String getProfilePictureUrl();

    List<Tag> getTags();

    String getTimeCreated();

    User getUser();

}
