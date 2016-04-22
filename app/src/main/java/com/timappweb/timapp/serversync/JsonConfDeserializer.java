package com.timappweb.timapp.serversync;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.entities.Category;
import com.timappweb.timapp.entities.SpotCategory;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;


public class JsonConfDeserializer implements JsonDeserializer<SyncConfig>
{

    private static final String TAG = "JsonConfDeserializer";

    @Override
    public SyncConfig deserialize(JsonElement jsonElement, java.lang.reflect.Type type,
                         JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        Log.d(TAG, "Parsing json element: " + type);
        JsonObject object = jsonElement.getAsJsonObject();
        SyncConfig conf = new SyncConfig();
        conf.version = object.get("version").getAsInt();

        String objectType = object.get("type").getAsString();
        switch (objectType){
            case "rules":
                conf.data = new Gson().fromJson(object.get("data"), ConfigurationProvider.Rules.class);
                break;
            case "spot_categories":
                conf.data = new Gson().fromJson(object.get("data"), SpotCategory[].class);
                break;
            case "event_categories":
                conf.data = new Gson().fromJson(object.get("data"), Category[].class);
                break;
            default:
                throw new JsonParseException("Invalid object type: " + objectType);
        }

        // Deserialize it. You use a new instance of Gson to avoid infinite recursion
        // to this deserializer
        Log.d(TAG, "Parsing done: " + conf);
        return conf;

    }
}