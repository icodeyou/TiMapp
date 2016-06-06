package com.timappweb.timapp.rest.services;

import com.activeandroid.Model;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.timappweb.timapp.data.entities.ApplicationRules;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.Picture;
import com.timappweb.timapp.data.models.Post;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.SpotCategory;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.UserEvent;
import com.timappweb.timapp.data.models.UserQuota;
import com.timappweb.timapp.rest.model.PaginatedResponse;
import com.timappweb.timapp.rest.model.RestFeedback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by stephane on 8/20/2015.
 */
public interface RestInterface {

    @POST("/api/{model}/add.json")
    Call<JsonObject> post(@Path("model") String model, @Body JsonObject data);

    @PUT("/api/{model}/edit.json")
    Call<JsonObject> put(@Path("model") String model, @Body JsonObject data);

    @GET("/api/{model}.json")
    <T> Call<PaginatedResponse<T>> paginate(@Path("model") String model);

    @GET("/api/{model}.json")
    <T> Call<PaginatedResponse<T>> paginate(@Path("model") String model, @QueryMap HashMap<String, String> options);

    @GET("/api/{model}.json")
    <T> Call<List<T>> list(@Path("model") String model);

    @GET("/api/{model}.json")
    <T> Call<List<T>> list(@Path("model") String model, @QueryMap HashMap<String, String> options);

    @GET("/api/{model}/view/{id}.json")
    <T> Call<List<T>> get(@Path("model") String model, @Path("id") long id);

    @GET("/api/{model}/view/{id}.json")
    <T> Call<List<T>> get(@Path("model") String model, @Path("id") long id, @QueryMap HashMap<String, String> options);

}
