package com.timappweb.timapp.rest.io.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.timappweb.timapp.data.models.Event;

import java.lang.reflect.Type;

/**
 * Created by stephane on 6/8/2016.
 *
 * TODO replace les mapper par les serializer ???
 */
public class AddEventSerializer implements JsonSerializer<Event>{

    @Override
    public JsonElement serialize(Event src, Type typeOfSrc, JsonSerializationContext context) {
        return null;
    }
}
