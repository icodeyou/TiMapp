package com.timappweb.timapp.rest;

import com.timappweb.timapp.entities.RestFeedback;
import com.timappweb.timapp.entities.Spot;
import com.timappweb.timapp.entities.SpotsTag;
import com.timappweb.timapp.entities.User;

import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.QueryMap;

/**
 * Created by stephane on 8/20/2015.
 */
public interface WebServiceInterface {

    @GET("/spots/index.json")
    void listSpots(@QueryMap Map<String, String> conditions, RestCallback<List<Spot>> restCallback);

    @GET("/spots/index.json")
    List<Spot> listSpots();

    @GET("/spots/view/{id}.json")
    void viewSpot(@Path("id") int id, RestCallback<Spot> restCallback);

    @GET("/spots/view/{id}.json")
    Spot viewSpot(@Path("id") int id);

    @GET("/users/check_token.json")
    void checkToken(Callback<RestFeedback> cb);

    @POST("/spots/add.json")
    void addSpot(@Body Spot spot, Callback<RestFeedback> cb);

    @GET("/spots/stop_broadcast.json")
    void stopBroadcast(Callback<RestFeedback> cb);

    @POST("/users/login.json")
    RestFeedback login(@Body User user);

    @POST("/SpotsTags/latest.json")
    List<SpotsTag> listSpotsTags();

    @POST("/SpotsTags/latest.json")
    void listSpotsTags(RestCallback<List<SpotsTag>> restCallback);
}
