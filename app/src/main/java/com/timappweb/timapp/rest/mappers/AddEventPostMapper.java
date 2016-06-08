package com.timappweb.timapp.rest.mappers;

import com.google.gson.JsonObject;
import com.timappweb.timapp.data.models.EventPost;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.exceptions.UnknownCategoryException;

import java.util.List;

/**
 * Created by stephane on 6/8/2016.
 */
public class AddEventPostMapper {

    public static JsonObject toJson(EventPost eventPost){
        JsonObject jsonObject = new JsonObject();
        if (eventPost.event != null) jsonObject.addProperty("event_id", eventPost.event.getRemoteId());
        if (eventPost.hasTags()){
            jsonObject.addProperty("tag_string", tagToString(eventPost.getTags()));
        }
        jsonObject.addProperty("latitude", eventPost.latitude);
        jsonObject.addProperty("longitude", eventPost.longitude);
        return jsonObject;
    }

    public static String tagToString(List<Tag> tags){
        String tagString = "";
        for (int i = 0; i < tags.size() - 1; i++){
            tagString += tags.get(i).name + ",";
        }
        tagString += tags.get(tags.size() -1).name;
        return tagString;
    }

}