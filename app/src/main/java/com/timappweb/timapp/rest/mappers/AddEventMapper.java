package com.timappweb.timapp.rest.mappers;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;
import com.timappweb.timapp.exceptions.UnknownCategoryException;
import com.timappweb.timapp.utils.ReflectionHelper;

import java.lang.reflect.Field;

/**
 * Created by stephane on 6/8/2016.
 */
public class AddEventMapper {

    public static JsonObject toJson(Event event){
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("spot", AddSpotMapper.toJson(event.getSpot()));
        jsonObject.addProperty("name", event.getName());
        jsonObject.addProperty("description", event.getDescription());
        jsonObject.addProperty("latitude", event.latitude);
        jsonObject.addProperty("longitude", event.longitude);
        try {
            if (event.hasCategory()){
                    jsonObject.addProperty("event_category_id",  event.getCategory().getRemoteId());
            }
        } catch (UnknownCategoryException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

}