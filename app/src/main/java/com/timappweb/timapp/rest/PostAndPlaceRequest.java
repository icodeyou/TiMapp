package com.timappweb.timapp.rest;

import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.data.models.Post;

/**
 * TODO REMOVE
 */
public class PostAndPlaceRequest {

    @SerializedName("post")
    private Post post;

    @SerializedName("place")
    private Place place;

    public PostAndPlaceRequest(Post post, Place place) {
        this.post = post;
        this.place = place;
    }
}
