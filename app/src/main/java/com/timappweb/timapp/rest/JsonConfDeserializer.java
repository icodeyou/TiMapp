package com.timappweb.timapp.rest;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.entities.Category;
import com.timappweb.timapp.entities.SpotCategory;
import com.timappweb.timapp.serversync.SyncConfig;
import java.lang.reflect.Type;
import java.util.ArrayList;


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
        JsonElement data = object.get("data");
        Type objectClass;
        switch (objectType){
            case "rules":
                objectClass = ConfigurationProvider.Rules.class;
                break;
            case "spot_categories":
                objectClass = new TypeToken<ArrayList<SpotCategory>>() {}.getType();
                break;
            case "event_categories":
                objectClass = new TypeToken<ArrayList<Category>>() {}.getType();
                break;
            default:
                throw new JsonParseException("Invalid object type: " + objectType);
        }
        conf.data = new Gson().fromJson(data, objectClass);

        // Deserialize it. You use a new instance of Gson to avoid infinite recursion
        // to this deserializer
        Log.d(TAG, "Parsing done: " + conf);
        return conf;

    }
}