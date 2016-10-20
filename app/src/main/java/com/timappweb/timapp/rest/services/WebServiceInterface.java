package com.timappweb.timapp.rest.services;

import com.google.gson.JsonObject;
import com.timappweb.timapp.data.entities.ApplicationRules;
import com.timappweb.timapp.data.entities.UserInvitationFeedback;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.entities.EventPeopleStats;
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
import com.timappweb.timapp.rest.io.responses.EventPointResponse;
import com.timappweb.timapp.rest.io.responses.PaginatedResponse;
import com.timappweb.timapp.rest.io.responses.RestFeedback;
import com.timappweb.timapp.rest.io.responses.ResponseSyncWrapper;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
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
    @GET("spots")
    Call<PaginatedResponse<Spot>> spots(@QueryMap Map<String, String> query);

    // ---------------------------------------------------------------------------------------------
    //  EVENTS

    @POST("events/add")
    Call<JsonObject> addPlace(@Body MultipartBody data);

    @POST("events/add")
    Call<JsonObject> addPlace(@Body JsonObject data);

    @GET("events/view/{id}")
    Call<Event> viewPlace(@Path("id") long id);

    @GET("events/points/{id}")
    Call<EventPointResponse> viewPointsPlace(@Path("id") long id);

    @GET("events/populars")
    Call<List<Event>> bestEvents(@QueryMap Map<String, String> conditions);

    //@GET("events/around_me")
    //Call<List<Event>> eventAroundMe(@QueryMap Map<String, String> conditions);

    @GET("events/index")
    Call<PaginatedResponse<Event>> places(@QueryMap Map<String, String> conditions);


    @POST("events/background_picture/{id}")
    Call<RestFeedback> setBackgroundPicture(@Path("id") long eventId, @Body Map<String,String> accessToken);


    // ---------------------------------------------------------------------------------------------
    // Event invites

    @FormUrlEncoded
    @POST("PlacesInvitations/invite/{eventId}")
    Call<List<UserInvitationFeedback>> sendInvite(@Path("eventId") int eventId, @Field("ids[]") List<Long> ids);

    @GET("PlacesInvitations/accept/{inviteId}")
    Call<RestFeedback> acceptInvite(@Path("inviteId") int inviteId);

    @GET("PlacesInvitations/reject/{inviteId}")
    Call<RestFeedback> rejectInvite(@Path("inviteId") int inviteId);

    @GET("PlacesInvitations/sent/{eventId}")
    Call<PaginatedResponse<EventsInvitation>> invitesSent(@Path("eventId") long eventId);

    //@GET("PlacesInvitations/sent")
    //Call<PaginatedResponse<EventsInvitation>> invitesSent();

    @GET("PlacesInvitations/received")
    Call<ResponseSyncWrapper<EventsInvitation>> inviteReceived(@QueryMap Map<String,String> options);


    // ---------------------------------------------------------------------------------------------
    // Posts

    @GET("eventPosts/eventPosts")
    Call<List<EventPost>> listPosts(@QueryMap Map<String, String> conditions);

    @POST("eventPosts/add")
    Call<RestFeedback> addTags(@Body EventPost eventPost);

    // ---------------------------------------------------------------------------------------------
    // USER

    @POST("users/localLogin")
    Call<RestFeedback>  login(@Body User user);

    @GET("users/check_token")
    Call<RestFeedback>  checkToken();

    @GET("users/profile/{id}")
    Call<User>  profile(@Path("id") long userId);

    @POST("users/facebook_login")
    Call<JsonObject>  facebookLogin(@Body JsonObject payload);

    @GET("UserFriends/index")
    Call<ResponseSyncWrapper<UserFriend>> friends(@QueryMap Map<String,String> options);

    @POST("UserFriends/request_sync/{providerId}")
    Call<Void> requestSyncFriends(@Path("providerId") String providerId, @Body JsonObject body);

    @POST("users/edit")
    Call<User> editProfile(@Body JsonObject user);

    @GET("Users/logout")
    Call<RestFeedback> logout();

    @FormUrlEncoded
    @POST("Users/update_google_messaging_token")
    Call<Object> updateGoogleMessagingToken(@Field("token") String token);

    // ---------------------------------------------------------------------------------------------
    // TAGS
    @GET("Posts/trending_tags")
    Call<List<Tag>> trendingTags(@QueryMap Map<String, String> conditions);

    @GET("Tags/suggest/{term}")
    Call<List<Tag>>  suggest(@Path("term") String term);

    @GET("SpotsTags/eventPost/{id}")
    Call<List<Tag>> loadTagsFromPost(@Path("id") int id);


    // ---------------------------------------------------------------------------------------------
    // Pictures

    @GET("pictures/event/{id}")
    Call<ResponseSyncWrapper<Picture>> viewPicturesForPlace(@Path("id") long id, @QueryMap Map<String, String> options);

    // ---------------------------------------------------------------------------------------------
    // Places


    /**
     * Get most popular tags for a event
     * @param id
     */
    @GET("tags/event/{id}")
    Call<List<Tag>> viewPopularTagsForPlace(@Path("id") long id);

    // ---------------------------------------------------------------------------------------------
    // EventsUsers

    @POST("PlacesUsers/coming/{eventId}")
    Call<UserEvent> notifyPlaceComing(@Path("eventId") long remoteId, @Body Map<String, String> conditions);

    @POST("PlacesUsers/gone/{eventId}")
    Call<UserEvent> notifyPlaceGone(@Path("eventId") long id, @Body Map<String, String> conditions);

    @POST("PlacesUsers/here/{eventId}")
    Call<UserEvent> notifyPlaceHere(@Path("eventId") long id, @Body Map<String, String> conditions);

    @POST("PlacesUsers/cancelComing/{eventId}")
    Call<RestFeedback> cancelComing(@Path("eventId") long id);

    @POST("PlacesUsers/cancelHere/{eventId}")
    Call<RestFeedback> cancelHere(@Path("eventId") long id);

    @POST("PlacesUsers/event/{id}")
    Call<PaginatedResponse<UserEvent>> viewUsersForPlace(@Path("id") int eventId, @QueryMap Map<String, String> conditions);

    @POST("PlacesUsers/event/{id}")
    Call<PaginatedResponse<UserEvent>> viewUsersForPlace(@Path("id") long eventId);

    @POST("PlacesUsers/user")
    Call<List<UserEvent>> placeStatus();

    @GET("PlacesUsers/stats/{id}")
    Call<EventPeopleStats> eventPeopleStats(@Path("id") long eventId);

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
