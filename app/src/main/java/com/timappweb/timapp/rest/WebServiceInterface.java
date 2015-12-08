package com.timappweb.timapp.rest;

import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.MapTag;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.rest.model.RestFeedback;

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
    void listSpots(@QueryMap Map<String, String> conditions, RestCallback<List<Post>> restCallback);

    @GET("/spots/index.json")
    List<Post> listSpots();

    @GET("/spots/view/{id}.json")
    void viewSpot(@Path("id") int id, RestCallback<Post> restCallback);

    @GET("/spots/view/{id}.json")
    Post viewSpot(@Path("id") int id);

    @GET("/users/check_token.json")
    void checkToken(Callback<RestFeedback> cb);

    @POST("/spots/add.json")
    void addSpot(@Body Post spot, RestCallback<RestFeedback> cb);

    @POST("/users/login.json")
    RestFeedback login(@Body User user);

    @POST("/SpotsTags/latest.json")
    List<MapTag> listSpotsTags();

    @POST("/SpotsTags/latest.json")
    void listSpotsTags(RestCallback<List<MapTag>> restCallback);
}
