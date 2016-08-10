package com.timappweb.timapp.rest.io.serializers;

import com.google.gson.JsonObject;
import com.timappweb.timapp.data.models.EventPost;
import com.timappweb.timapp.data.models.Tag;

import java.util.List;

/**
 * Created by stephane on 6/8/2016.
 */
public class AddEventPostMapper {

    public static final String MAP_KEY_EVENT_ID             = "place_id";
    public static final String MAP_KEY_LATITUDE             = "latitude";
    public static final String MAP_KEY_LONGITUDE            = "longitude";
    public static final String MAP_KEY_TAGS                 = "tag_string";

    public static JsonObject toJson(EventPost eventPost){
        JsonObject jsonObject = new JsonObject();
        if (eventPost.event != null) jsonObject.addProperty(MAP_KEY_EVENT_ID, eventPost.event.getRemoteId());
        if (eventPost.hasTags()){
            jsonObject.addProperty(MAP_KEY_TAGS, tagToString(eventPost.getTags()));
        }
        jsonObject.addProperty(MAP_KEY_LATITUDE, eventPost.latitude);
        jsonObject.addProperty(MAP_KEY_LONGITUDE, eventPost.longitude);
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