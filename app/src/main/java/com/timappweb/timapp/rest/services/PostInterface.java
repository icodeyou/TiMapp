package com.timappweb.timapp.rest.services;

import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.Post;
import com.timappweb.timapp.data.models.SyncBaseModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;

/**
 * Created by stephane on 6/5/2016.
 */
public interface PostInterface {

    @POST("/api/posts/add.json")
    Call<Post> post(@Body Post data);

    @PUT("/api/posts/edit.json")
    Call<Post> put(@Body Post data);


}
