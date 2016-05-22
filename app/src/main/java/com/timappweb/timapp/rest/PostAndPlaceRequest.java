package com.timappweb.timapp.rest;

import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.Post;

/**
 * TODO REMOVE
 */
public class PostAndPlaceRequest {

    @SerializedName("post")
    private Post post;

    @SerializedName("event")
    private Event event;

    public PostAndPlaceRequest(Post post, Event event) {
        this.post = post;
        this.event = event;
    }
}
