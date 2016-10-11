package com.timappweb.timapp.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Created by Stephane on 10/10/2016.
 */

public class JsonAccessor {

    private JsonObject data;

    public JsonAccessor(JsonObject data) {
        this.data = data;
    }

    public boolean has(String key){
        return get(key) != null;
    }

    public JsonElement get(String key){
        return _getNode(key, data);
    }

    public JsonElement getNotNull(String key) throws MissingKeyException {
        JsonElement tmp = _getNode(key, data);
        if (tmp == null){
            throw new MissingKeyException(key);
        }
        return tmp;
    }

    private JsonElement _getNode(String keys, JsonElement element){
        try {
            String[] parts = keys.split("\\.");
            for (String key: parts){
                if (element == null){
                    return null;
                }
                element = element.getAsJsonObject().get(key);
            }
            return element;
        }
        catch (IllegalStateException ex){
            return null;
        }
    }

    public class MissingKeyException extends Exception {
        public MissingKeyException(String key) {
            super("Missing key in JsonObject: " + key);
        }
    }
}
