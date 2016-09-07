package com.timappweb.timapp.rest.io.deserializers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.Spot;

import java.lang.reflect.Type;

/**
 * Created by stephane on 6/8/2016.
 */
public class SpotDeserializer implements JsonDeserializer<Spot> {

    @Override
    public Spot deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        //Event event = new Event();
        Spot spot = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                .fromJson(json, Spot.class);

        JsonObject jsonObject = json.getAsJsonObject();
        if (jsonObject.has("category_id")){
            spot.setCategory(jsonObject.get("category_id").getAsLong());
        }
        return spot;
    }
}
