package com.timappweb.timapp.rest.io.serializers;

import com.google.gson.JsonObject;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.exceptions.UnknownCategoryException;

import java.util.List;

/**
 * Created by stephane on 6/8/2016.
 */
public class EditProfileMapper {

    public static JsonObject toJson(List<Tag> tags){
        JsonObject object = new JsonObject();
        object.addProperty("tag_string", Tag.tagsToString(tags));
        return object;
    }


}