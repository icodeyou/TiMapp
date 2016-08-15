package com.timappweb.timapp.rest.services;

import com.timappweb.timapp.data.entities.ApplicationRules;
import com.timappweb.timapp.data.entities.UserInvitationFeedback;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.models.EventPost;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.SpotCategory;
import com.timappweb.timapp.data.models.UserEvent;
import com.timappweb.timapp.data.models.UserFriend;
import com.timappweb.timapp.data.models.UserQuota;
import com.timappweb.timapp.data.models.Picture;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.rest.io.responses.PaginatedResponse;
import com.timappweb.timapp.rest.io.responses.RestFeedback;
import com.timappweb.timapp.sync.performers.FullTableSyncPerformer;

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
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by stephane on 8/20/2015.
 */
public interface WebServiceInterface {

    // ---------------------------------------------------------------------------------------------
    @GET("spots/reachable")
    Call<PaginatedResponse<Spot>> spotReachable(@Query("latitude") double latitude, @Query("longitude") double longitude);

    @GET("spots")
    Call<PaginatedResponse<Spot>> spots(@QueryMap Map<String, String> query);

    // ---------------------------------------------------------------------------------------------
    // Event
    @POST("places/add")
    Call<RestFeedback> addPlace(@Body Event event);


    // ---------------------------------------------------------------------------------------------
    // Event invites
    @FormUrlEncoded
    @POST("PlacesInvitations/invite/{placeId}.json")
    Call<List<UserInvitationFeedback>> sendInvite(@Path("placeId") int placeId, @Field("ids[]") List<Integer> ids);

    @GET("PlacesInvitations/accept/{inviteId}.json")
    Call<RestFeedback> acceptInvite(@Path("inviteId") int inviteId);

    @GET("PlacesInvitations/reject/{inviteId}.json")
    Call<RestFeedback> rejectInvite(@Path("inviteId") int inviteId);

    @GET("PlacesInvitations/sent/{placeId}.json")
    Call<PaginatedResponse<EventsInvitation>> invitesSent(@Path("placeId") long placeId);

    //@GET("PlacesInvitations/sent.json")
    //Call<PaginatedResponse<EventsInvitation>> invitesSent();

    @GET("PlacesInvitations/received.json")
    Call<PaginatedResponse<EventsInvitation>> inviteReceived();


    // ---------------------------------------------------------------------------------------------
    // Posts

    @GET("eventPosts/eventPosts.json")
    Call<List<EventPost>> listPosts(@QueryMap Map<String, String> conditions);

    @GET("eventPosts/view/{id}.json")
    Call<EventPost> viewPost(@Path("id") int id);

    @POST("eventPosts/add.json")
    Call<RestFeedback> addTags(@Body EventPost eventPost);

    // ---------------------------------------------------------------------------------------------
    // USER

    @POST("users/login.json")
    Call<RestFeedback>  login(@Body User user);

    @GET("users/check_token.json")
    Call<RestFeedback>  checkToken();

    @GET("users/profile/{id}.json")
    Call<User>  profile(@Path("id") long userId);

    @POST("users/facebook_login.json")
    Call<RestFeedback>  facebookLogin(@Body Map<String,String> accessToken);

    @GET("users/friends.json")
    Call<FullTableSyncPerformer.RemoteLoader.TableSyncResult<UserFriend>> friends(@QueryMap Map<String,String> options);

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

    @GET("SpotsTags/eventPost/{id}.json")
    Call<List<Tag>> loadTagsFromPost(@Path("id") int id);


    // ---------------------------------------------------------------------------------------------
    // Pictures

    @POST("pictures/upload/{placeId}.json")
    Call<RestFeedback> upload(@Path("placeId") int placeId,
                              @Body RequestBody body);

    @GET("pictures/place/{id}.json")
    Call<PaginatedResponse<Picture>> viewPicturesForPlace(@Path("id") long id);

    // ---------------------------------------------------------------------------------------------
    // Places

    /**
     * Get eventPosts for a place
     * @param id
     */
    @POST("eventPosts/place/{id}.json")
    Call<List<EventPost>> viewPostsForPlace(@Path("id") int id);

    /**
     * Get most popular tags for a place
     * @param id
     */
    @POST("tags/place/{id}.json")
    Call<List<Tag>> viewPopularTagsForPlace(@Path("id") long id);

    /**
     *
     * @param id
     */
    @POST("Places/view/{id}.json")
    Call<Event> viewPlace(@Path("id") long id);

    /**
     * Find places to display on the map
     * @param conditions
     */
    @POST("Places/populars.json")
    Call<List<Event>> bestPlaces(@QueryMap Map<String, String> conditions);
    /**
     * Adding a place
     * @param event
     * @param restFeedback
     */
    // @POST("Places/add.json")
    // void addPlace(@Body Event event, RestCallback<RestFeedback> restFeedback);


    /**
     * Used to get all event that are in a area
     * @param conditions
     */
    //@GET("Places/around_me.json")
    //Call<List<Event>> placeAroundMe(@QueryMap Map<String, String> conditions);

    /**
     * Used to get all event that are around user position
     * @param conditions"
     */
    @GET("Places/reachable.json")
    Call<List<Event>> placeReachable(@QueryMap Map<String, String> conditions);

    /**
     *
     * @param latitude
     * @param longitude
     * @return
     */
    @GET("Places/reachable.json")
    Call<List<Event>> placeReachable(@Query("latitude") double latitude, @Query("longitude") double longitude);

    // ---------------------------------------------------------------------------------------------
    // PlacesUsers
    /**
     *
     */
    @POST("PlacesUsers/coming/{placeId}.json")
    Call<RestFeedback> notifyPlaceComing(@Path("placeId") long remoteId, @Body Map<String, String> conditions);
    /**
     *
     */
    @POST("PlacesUsers/gone/{placeId}.json")
    Call<RestFeedback> notifyPlaceGone(@Path("placeId") long id, @Body Map<String, String> conditions);
    /**
     *
     */
    @POST("PlacesUsers/here/{placeId}.json")
    Call<RestFeedback> notifyPlaceHere(@Path("placeId") long id, @Body Map<String, String> conditions);

    /**
     *
     */
    @POST("PlacesUsers/cancelComing/{placeId}.json")
    Call<RestFeedback> cancelComing(@Path("placeId") long id);

    /**
     *
     */
    @POST("PlacesUsers/cancelHere/{placeId}.json")
    Call<RestFeedback> cancelHere(@Path("placeId") long id);


    @POST("PlacesUsers/place/{id}.json")
    Call<PaginatedResponse<UserEvent>> viewUsersForPlace(@Path("id") int placeId, @QueryMap Map<String, String> conditions);

    @POST("PlacesUsers/place/{id}.json")
    Call<PaginatedResponse<UserEvent>> viewUsersForPlace(@Path("id") long placeId);

    @POST("PlacesUsers/user.json")
    Call<List<UserEvent>> placeStatus();

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

    @GET("configurations/application-rules")
    Call<ApplicationRules> applicationRules();

}
