package com.timappweb.timapp.rest.services;

import com.google.gson.JsonArray;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.models.UserQuota;
import com.timappweb.timapp.entities.Picture;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.PlacesInvitation;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.Spot;
import com.timappweb.timapp.data.models.SpotCategory;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.entities.UserPlace;
import com.timappweb.timapp.rest.PostAndPlaceRequest;
import com.timappweb.timapp.rest.model.PaginationResponse;
import com.timappweb.timapp.rest.model.RestFeedback;

import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * Created by stephane on 8/20/2015.
 */
public interface WebServiceInterface {

    // ---------------------------------------------------------------------------------------------
    @GET("spots/reachable")
    Call<PaginationResponse<Spot>> spotReachable(@QueryMap Map<String, String> conditions);

    @GET("spots")
    Call<PaginationResponse<SpotCategory>> spots();

    // ---------------------------------------------------------------------------------------------
    // Place
    @POST("places/add")
    Call<RestFeedback> addPlace(@Body Place place);

    // ---------------------------------------------------------------------------------------------
    // Place invites
    @FormUrlEncoded
    @POST("PlacesInvitations/invite/{placeId}.json")
    Call<JsonArray> sendInvite(@Path("placeId") int placeId, @Field("ids[]") List<Integer> ids);

    @GET("PlacesInvitations/accept/{inviteId}.json")
    Call<RestFeedback> acceptInvite(@Path("inviteId") int inviteId);

    @GET("PlacesInvitations/reject/{inviteId}.json")
    Call<RestFeedback> rejectInvite(@Path("inviteId") int inviteId);

    @GET("PlacesInvitations/sent/{placeId}.json")
    Call<PaginationResponse<PlacesInvitation>> invitesSent(@Path("placeId") int inviteId);

    @GET("PlacesInvitations/received.json")
    Call<PaginationResponse<PlacesInvitation>> inviteReceived();


    // ---------------------------------------------------------------------------------------------
    // Posts

    @GET("posts/posts.json")
    Call<List<Post>> listPosts(@QueryMap Map<String, String> conditions);

    @GET("posts/view/{id}.json")
    Call<Post> viewPost(@Path("id") int id);

    @POST("posts/add.json")
    Call<RestFeedback> addPost(@Body Post post);

    @POST("posts/add.json")
    Call<RestFeedback>  addPost(@Body PostAndPlaceRequest data);

    // ---------------------------------------------------------------------------------------------
    // USER

    @POST("users/login.json")
    Call<RestFeedback>  login(@Body User user);

    @GET("users/check_token.json")
    Call<RestFeedback>  checkToken();

    @GET("users/profile/{id}.json")
    Call<User>  profile(@Path("id") int username);

    @POST("users/facebook_login.json")
    Call<RestFeedback>  facebookLogin(@Body Map<String,String> accessToken);

    @GET("users/friends.json")
    Call<PaginationResponse<User>> friends();

    @POST("users/edit.json")
    Call<RestFeedback> editProfile(@Body Map<String, String> user);

    @GET("Users/logout.json")
    Call<RestFeedback> logout();

    @FormUrlEncoded
    @POST("Users/update_google_messaging_token.json")
    Call<RestFeedback> updateGoogleMessagingToken(@Field("token") String token);

    // ---------------------------------------------------------------------------------------------
    // TAGS
    @GET("Posts/trending_tags.json")
    Call<List<Tag>> trendingTags(@QueryMap Map<String, String> conditions);

    @GET("Tags/suggest/{term}.json")
    Call<List<Tag>>  suggest(@Path("term") String term);

    @GET("SpotsTags/post/{id}.json")
    Call<List<Tag>> loadTagsFromPost(@Path("id") int id);


    // ---------------------------------------------------------------------------------------------
    // Pictures

    @POST("pictures/upload/{placeId}.json")
    Call<RestFeedback> upload(@Path("placeId") int placeId,
                              @Body RequestBody body);

    @GET("pictures/place/{id}.json")
    Call<PaginationResponse<Picture>> viewPicturesForPlace(@Path("id") int id);

    // ---------------------------------------------------------------------------------------------
    // Places

    /**
     * Get posts for a place
     * @param id
     */
    @POST("posts/place/{id}.json")
    Call<List<Post>> viewPostsForPlace(@Path("id") int id);

    /**
     * Get most popular tags for a place
     * @param id
     */
    @POST("tags/place/{id}.json")
    Call<List<Tag>> viewPopularTagsForPlace(@Path("id") int id);

    /**
     *
     * @param id
     */
    @POST("Places/view/{id}.json")
    Call<Place> viewPlace(@Path("id") int id);

    /**
     * Find places to display on the map
     * @param conditions
     */
    @POST("Places/populars.json")
    Call<List<Place>> bestPlaces(@QueryMap Map<String, String> conditions);
    /**
     * Adding a place
     * @param place
     * @param restFeedback
     */
    // @POST("Places/add.json")
    // void addPlace(@Body Place place, RestCallback<RestFeedback> restFeedback);


    /**
     * Used to get all place that are in a area
     * @param conditions
     */
    //@GET("Places/around_me.json")
    //Call<List<Place>> placeAroundMe(@QueryMap Map<String, String> conditions);

    /**
     * Used to get all place that are around user position
     * @param conditions"
     */
    @GET("Places/reachable.json")
    Call<List<Place>> placeReachable(@QueryMap Map<String, String> conditions);

    // ---------------------------------------------------------------------------------------------
    // PlacesUsers
    /**
     *
     */
    @POST("PlacesUsers/coming.json")
    Call<RestFeedback> notifyPlaceComing(@Body Map<String, String> conditions);
    /**
     *
     */
    @POST("PlacesUsers/gone.json")
    Call<RestFeedback> notifyPlaceGone(@Body Map<String, String> conditions);
    /**
     *
     */
    @POST("PlacesUsers/here.json")
    Call<RestFeedback> notifyPlaceHere(@Body Map<String, String> conditions);

    @POST("PlacesUsers/place/{id}.json")
    Call<PaginationResponse<UserPlace>> viewUsersForPlace(@Path("id") int placeId, @QueryMap Map<String, String> conditions);

    // ---------------------------------------------------------------------------------------------
    // Quotas
    @GET("activity-quota/user-quotas")
    Call<List<UserQuota>> userQuotas();

    // ---------------------------------------------------------------------------------------------
    // Categories
    @GET("spot-categories")
    Call<List<SpotCategory>> spotCategories();

    @GET("categories")
    Call<List<EventCategory>> eventCategories();
}
