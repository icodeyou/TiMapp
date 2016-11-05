package com.timappweb.timapp.rest.io.serializers;

import com.google.gson.JsonObject;
import com.timappweb.timapp.data.models.Event;

/**
 * Created by stephane on 6/8/2016.
 */
public class AddEventMapper {

    public static JsonObject toJson(Event event){
        JsonObject jsonObject = new JsonObject();
        if (event.spot != null) {
            if (!event.spot.hasRemoteId()){
                jsonObject.add("spot", AddSpotMapper.toJson(event.spot));
            }
            else {
                jsonObject.addProperty("spot_id", event.spot.getRemoteId());
            }
        }

        jsonObject.addProperty("name", event.getName());
        jsonObject.addProperty("description", event.getDescription());
        jsonObject.addProperty("latitude", event.latitude);
        jsonObject.addProperty("longitude", event.longitude);
        if (event.event_category != null){
            jsonObject.addProperty("category_id",  event.event_category.getRemoteId());
        }
        return jsonObject;
    }


}