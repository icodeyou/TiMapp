package com.timappweb.timapp.serversync;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.mockito.asm.Type;

class JsonConfDeserializer implements JsonDeserializer<SyncConfig>
{

    private static final String TAG = "JsonConfDeserializer";

    @Override
    public SyncConfig deserialize(JsonElement jsonElement, java.lang.reflect.Type type,
                         JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        Log.d(TAG, "Parsing json element");
        SyncConfig conf = new SyncConfig();
        // Get the "content" element from the parsed JSON
        JsonElement data = jsonElement.getAsJsonObject().get("data");
        conf.data = new Gson().fromJson(data, type);
        conf.version = jsonElement.getAsJsonObject().get("version").getAsInt();

        // Deserialize it. You use a new instance of Gson to avoid infinite recursion
        // to this deserializer
        return conf;

    }
}