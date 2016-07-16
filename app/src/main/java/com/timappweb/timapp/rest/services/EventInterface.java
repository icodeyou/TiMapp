package com.timappweb.timapp.rest.services;

import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.rest.model.PaginatedResponse;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * Created by stephane on 6/5/2016.
 */
public interface EventInterface {

    @POST("/api/places/add.json")
    Call<Event> post(@Body Event data);

    @PUT("/api/places/edit.json")
    Call<Event> put(@Body Event data);


}
