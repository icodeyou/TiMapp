package com.timappweb.timapp.rest;

import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.MapTag;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.rest.model.RestFeedback;

import java.util.ArrayList;
import java.util.HashMap;
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

    // ---------------------------------------------------------------------------------------------
    // Posts

    @GET("/posts/posts.json")
    void listPosts(@QueryMap Map<String, String> conditions, RestCallback<List<Post>> restCallback);

    @GET("/posts/view/{id}.json")
    void viewPost(@Path("id") int id, RestCallback<Post> restCallback);

    @GET("/posts/view/{id}.json")
    Post viewPost(@Path("id") int id);

    @POST("/posts/add.json")
    void addPost(@Body Post post, RestCallback<RestFeedback> cb);

    // ---------------------------------------------------------------------------------------------
    // USER

    @POST("/users/login.json")
    RestFeedback login(@Body User user);

    @GET("/users/check_token.json")
    void checkToken(Callback<RestFeedback> cb);

    @GET("/users/profile/{username}.json")
    void profile(@Path("username") String username, RestCallback<User> restCallback);

    // ---------------------------------------------------------------------------------------------
    // TAGS
    @GET("/Posts/trending_tags.json")
    void trendingTags(@QueryMap Map<String, String> conditions, RestCallback<List<Tag>> restCallback);

    @GET("/Tags/suggest/{term}.json")
    void suggest(@Path("term") String term, RestCallback<List<Tag>> restCallback);

    @GET("/Tags/suggest/{term}.json")
    List<Tag>  suggest(@Path("term") String term);

    @GET("/SpotsTags/post/{id}.json")
    void loadTagsFromPost(@Path("id") int id, RestCallback<ArrayList<Tag>> restCallback);

    // ---------------------------------------------------------------------------------------------
    // Places

    /**
     * Find places to display on the map
     * @param conditions
     * @param callback
     */
    @POST("/Places/populars.json")
    void bestPlaces(@QueryMap Map<String, String> conditions, RestCallback<List<Place>>  callback);

    /**
     * Adding a place
     * @param place
     * @param restFeedback
     */
    @POST("/Places/add.json")
    void addPlace(@Body Place place, RestCallback<RestFeedback> restFeedback);

    /**
     * Used to get all place that are in a area
     * @param conditions
     * @param callback
     */
    @GET("/Places/around_me.json")
    void placeAroundMe(@QueryMap Map<String, String> conditions, RestCallback<List<Place>>  callback);

    /**
     * Used to get all place that are around user position
     * @param conditions
     * @param restCallback
     */
    @GET("/Places/reachable.json")
    void placeReachable(@QueryMap Map<String, String> conditions, RestCallback<List<Place>> restCallback);
}
