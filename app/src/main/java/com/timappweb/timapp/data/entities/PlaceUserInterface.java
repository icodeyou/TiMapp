package com.timappweb.timapp.data.entities;

import java.util.List;

public interface PlaceUserInterface {

    //String getUsername();

    //String getProfilePictureUrl();

    List<Tag> getTags();

    String getTimeCreated();

    User getUser();

    int getViewType();
}
