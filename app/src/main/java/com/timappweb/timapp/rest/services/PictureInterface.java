package com.timappweb.timapp.rest.services;

import com.google.gson.JsonObject;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.Picture;
import com.timappweb.timapp.rest.model.RestFeedback;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by stephane on 6/5/2016.
 */
public interface PictureInterface {

    @POST("/api/pictures/upload/{placeId}.json")
    Call<JsonObject> upload(@Path("placeId") long placeId,
                              @Body RequestBody body);


}
