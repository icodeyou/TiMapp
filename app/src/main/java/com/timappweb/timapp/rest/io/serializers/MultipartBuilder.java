package com.timappweb.timapp.rest.io.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.timappweb.timapp.utils.Util;

import java.io.File;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by Stephane on 23/09/2016.
 */
public class MultipartBuilder {

    private final MultipartBody.Builder builder;

    public MultipartBuilder() {
        this.builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
    }

    public MultipartBuilder add(String key, File file) {
        MediaType fileMimeType = MediaType.parse(Util.getMimeType(file.getAbsolutePath()));
        this.builder
                .addFormDataPart(key, file.getName(), RequestBody.create(fileMimeType, file));
        return this;
    }

    public MultipartBuilder add(JsonElement jsonElement, String prefix) {
        if (jsonElement.isJsonPrimitive()){
            this.builder.addFormDataPart(prefix, jsonElement.getAsString());
        }
        else if (jsonElement.isJsonObject()){
            for (Map.Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().entrySet()){
                this.add(entry.getValue(), prefix != null ? prefix + "["+entry.getKey() + "]" : entry.getKey());
            }
        }
        else if (jsonElement.isJsonArray()){
            int i = 0;
            for (JsonElement subEntryValue: jsonElement.getAsJsonArray()){
                this.add(subEntryValue, prefix != null ? prefix + "[" + i + "]" : "data[" + i + "]");
                i++;
            }
        }
        return this;
    }

    public MultipartBuilder add(RequestBody body) {
        this.builder.addPart(body);
        return this;
    }

    public <T> MultipartBuilder addProperty(String key, T object) {
        this.builder.addFormDataPart(key, String.valueOf(object));
        return this;
    }

    public MultipartBody build() {
        return this.builder.build();
    }

}
