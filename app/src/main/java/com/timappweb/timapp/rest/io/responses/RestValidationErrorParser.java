package com.timappweb.timapp.rest.io.responses;

import com.activeandroid.util.Log;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.timappweb.timapp.utils.JsonAccessor;
import com.timappweb.timapp.utils.Util;

import java.util.Map;

/**
 * Created by stephane on 5/31/2016.
 */
public class RestValidationErrorParser extends JsonAccessor{

    private static final String TAG = "RestValidationErrorParser";

    // =============================================================================================

    public RestValidationErrorParser(JsonObject data) {
        super(data);
    }

    // =============================================================================================

    public String getMessage(String key){
        JsonElement element = get(key);
        return _elementToString(element);
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
