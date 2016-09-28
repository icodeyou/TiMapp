package com.timappweb.timapp.rest.io.serializers;

import com.google.gson.JsonObject;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.exceptions.UnknownCategoryException;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by stephane on 6/8/2016.
 */
public class AddEventMapper {

    public static JsonObject toJson(Event event){
        JsonObject jsonObject = new JsonObject();
        if (event.hasSpot()) {
            if (!event.getSpot().hasRemoteId()){
                jsonObject.add("spot", AddSpotMapper.toJson(event.getSpot()));
            }
            else {
                jsonObject.addProperty("spot_id", event.getSpot().getRemoteId());
            }
        }

        jsonObject.addProperty("name", event.getName());
        jsonObject.addProperty("description", event.getDescription());
        jsonObject.addProperty("latitude", event.latitude);
        jsonObject.addProperty("longitude", event.longitude);
        try {
            if (event.hasCategory()){
                jsonObject.addProperty("category_id",  event.getCategory().getRemoteId());
            }
        } catch (UnknownCategoryException e) {
            // Will not happen as event has a category
        }
        return jsonObject;
    }


}