package com.timappweb.timapp.rest.io.deserializers;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.timappweb.timapp.data.models.Event;

import java.lang.reflect.Type;

/**
 * Created by stephane on 6/8/2016.
 */
public class EventDeserializer implements JsonDeserializer<Event> {

    @Override
    public Event deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        //Event event = new Event();
        Event event = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create()
                .fromJson(json, Event.class);

        JsonObject jsonObject = json.getAsJsonObject();
        if (jsonObject.has("category_id")){
            event.setCategory(jsonObject.get("category_id").getAsLong());
        }
        return event;
    }

}
