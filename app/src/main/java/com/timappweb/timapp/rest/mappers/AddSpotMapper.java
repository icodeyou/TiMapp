package com.timappweb.timapp.rest.mappers;

import com.google.gson.JsonObject;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.exceptions.UnknownCategoryException;

import java.util.List;

/**
 * Created by stephane on 6/8/2016.
 */
public class AddSpotMapper {

    public static JsonObject toJson(Spot spot){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", spot.getName());
        jsonObject.addProperty("latitude", spot.latitude);
        jsonObject.addProperty("longitude", spot.longitude);
        if (spot.hasCategory()){
            jsonObject.addProperty("spot_category_id",  spot.getCategory().getRemoteId());
        }
        return jsonObject;
    }


}
