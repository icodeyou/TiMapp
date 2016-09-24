package com.timappweb.timapp.rest.io.serializers;

import com.google.gson.JsonObject;
import com.timappweb.timapp.utils.Util;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by Stephane on 23/09/2016.
 */
public class MultipartBuilder {

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

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

    /*
    public MultipartBuilder add(JsonObject jsonObject) {
        this.builder.addFormDataPart("extra", , RequestBody.create(JSON, jsonObject.toString()));
        return this;
    }
*/
    public MultipartBuilder add(RequestBody body) {
        this.builder.addPart(body);
        return this;
    }

    public <T> MultipartBuilder addProperty(String key, T object) {
        this.builder.addFormDataPart(key, String.valueOf(object));
        return this;
    }

    public RequestBody build() {
        return this.builder.build();
    }

}
