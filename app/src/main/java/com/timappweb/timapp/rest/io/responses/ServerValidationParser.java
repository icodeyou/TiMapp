package com.timappweb.timapp.rest.io.responses;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.timappweb.timapp.utils.JsonAccessor;

import java.util.Map;

/**
 * Created by stephane on 5/31/2016.
 */
public class ServerValidationParser extends JsonAccessor{

    private static final String TAG = "RestValidationErrorParser";

    // =============================================================================================

    public ServerValidationParser(JsonObject data) {
        super(data);
    }

    // =============================================================================================

    public String getMessage(String key){
        JsonElement element = get(key);
        if (element != null){
            return _elementToString(element);
        }
        return null;
    }


    private String _elementToString(JsonElement element) {
        String msg = "";
        if (element.isJsonObject()){
            JsonObject messageObject = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> elem: messageObject.entrySet()){
                msg += (msg.equals("") ? "" : "\n") + _elementToString(elem.getValue());
            }
        }
        else if (element.isJsonArray()){
            for (JsonElement subElement: element.getAsJsonArray()){
                msg += (msg.equals("") ? "" : "\n") + _elementToString(subElement);
            }
        }
        else{
            msg = element.getAsString();
        }

        return msg;
    }

}
