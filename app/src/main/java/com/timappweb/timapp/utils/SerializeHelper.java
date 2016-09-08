package com.timappweb.timapp.utils;

import android.os.Bundle;
import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.query.Select;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.rest.RestClient;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Created by stephane on 6/2/2016.
 */
public class SerializeHelper {

    private static Gson serializer = new GsonBuilder()
            // @warning When using Gson with ActiveAndroid model, it will crash if you remove this line...
            //.excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    public static <T extends Model> String packModel(T obj, Class<T> type){
        if (obj.getId() == null){
            return serializer.toJson(obj, type);
        }
        else{
            return serializer.toJson(obj.getId(), Long.class);
        }
    }

    public static <T> String pack(T obj){
        return serializer.toJson(obj);
    }

    public static <T> T unpack(String data, Class<T> classOfT) {
        return serializer.fromJson(data, classOfT);
    }

    /**
     * See http://stackoverflow.com/questions/18397342/deserializing-generic-types-with-gson
     * @param data
     * @param classOfT
     * @param <T>
     * @return
     */
    public static <T extends Model> T unpackModel(String data, Class<T> classOfT) {
        try{
            return serializer.fromJson(data, classOfT);
        }
        catch (Exception ex){
            Long id = serializer.fromJson(data, Long.class);
            return new Select().from(classOfT).where("Id = ?", id).executeSingle();
        }
    }

}
