package com.timappweb.timapp.data.entities;

import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.data.models.User;

import java.util.List;

public interface PlaceUserInterface {

    //String getUsername();

    //String getProfilePictureUrl();

    List<Tag> getTags();

    String getTimeCreated();

    User getUser();

    int getViewType();
}
