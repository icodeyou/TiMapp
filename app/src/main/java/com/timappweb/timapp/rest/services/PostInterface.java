package com.timappweb.timapp.rest.services;

import com.timappweb.timapp.data.models.EventPost;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;

/**
 * Created by stephane on 6/5/2016.
 */
public interface PostInterface {

    @POST("/api/eventPosts/add.json")
    Call<EventPost> post(@Body EventPost data);

    @PUT("/api/eventPosts/edit.json")
    Call<EventPost> put(@Body EventPost data);


}
