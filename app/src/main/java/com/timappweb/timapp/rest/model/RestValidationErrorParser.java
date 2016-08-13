package com.timappweb.timapp.rest.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.timappweb.timapp.utils.Util;

import java.util.Map;

/**
 * Created by stephane on 5/31/2016.
 */
public class RestValidationErrorParser {

    private static final String TAG = "RestValidationErrorParser";
    private JsonObject data;

    // =============================================================================================

    public RestValidationErrorParser(JsonObject data) {
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
            if (element == null) {
                return null;
            }
            String msg = "";
            if (element.isJsonObject()){
                JsonObject messageObject = element.getAsJsonObject();
                for (Map.Entry<String, JsonElement> elem: messageObject.entrySet()){
                    msg += elem.getValue().getAsString();
                }
            }
            else{
                msg = element.getAsString();
            }
            return msg;
        }
        catch (Exception ex){
            Util.appStateError(TAG, "Server response has an invalid error format: " + ex.getMessage());
            return null;
        }
    }
}
