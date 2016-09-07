package com.timappweb.timapp.rest.io.responses;

import com.activeandroid.util.Log;
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
        return _getNode(keys, data);
    }

    private String _getNode(String keys, JsonElement element){
        try {
            String[] parts = keys.split("\\.");
            for (String key: parts){
                element = element.getAsJsonObject().get(key);
            }
            if (element == null) {
                return null;
            }
            String msg = _elementToString(element);
            return msg;
        }
        catch (Exception ex){
            Log.e(TAG, "Server response has an invalid error format: " + ex.getMessage());
            return null;
        }
    }

    private String _elementToString(JsonElement element) {
        String msg = "";
        if (element.isJsonObject()){
            JsonObject messageObject = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> elem: messageObject.entrySet()){
                msg += _elementToString(elem.getValue()) + "\n";
            }
        }
        else{
            msg = element.getAsString();
        }
        return msg;
    }

}
