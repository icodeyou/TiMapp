package com.timappweb.timapp.rest.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Created by stephane on 5/31/2016.
 */
public class RestValidationErrors {

    private JsonObject data;

    // =============================================================================================

    public RestValidationErrors(JsonObject data) {
        this.data = data;
    }

    // =============================================================================================

    public boolean has(String key){
        return _getNode(key) != null;
    }

    public String get(String key){
        return _getNode(key);
    }

    private String _getNode(String keys){
        try {
            String[] parts = keys.split("\\.");
            JsonElement element = data;
            for (String key: parts){
                element = element.getAsJsonObject().get(key);
            }
            return element.getAsString();
        }
        catch (Exception ex){
            return null;
        }
    }
}
