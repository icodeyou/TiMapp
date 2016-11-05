package com.timappweb.timapp.utils;

import android.content.Intent;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Category;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.MyModel;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.SyncBaseModel;

import org.antlr.v4.codegen.model.Sync;

/**
 * Created by stephane on 6/2/2016.
 */
public class SerializeHelper {

    private static Gson serializer = new GsonBuilder()
            // @warning When using Gson with ActiveAndroid model, it will crash if you remove this line...
            //.excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    public static <T extends MyModel> String packModel(T obj){
        return serializer.toJson(obj, obj.getClass());
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
    public static <T extends MyModel> T unpackModel(String data, Class<T> classOfT) {
        return serializer.fromJson(data, classOfT);
    }
    public static void pack(Intent intent, String key, SyncBaseModel model) {
        if (intent.getExtras() == null) {
            Bundle bundle = new Bundle();
            pack(bundle, key, model);
            intent.putExtras(bundle);
        }
        else{
            pack(intent.getExtras(), key, model);
        }
    }

    public static void pack(Bundle extras, String key, SyncBaseModel model) {
        if (model instanceof Event){
            Event event = (Event) model;
            if (event.picture != null){
                event.picture.event = null; // Prevent circular reference
            }
        }
        extras.putString(key, SerializeHelper.pack(model));
    }
    public static <T extends SyncBaseModel> T unpack(Bundle extras, String key, Class<T> clazz) {
        T model = SerializeHelper.unpack(extras.getString(key), clazz);
        if (model instanceof Event){
            Event event = (Event) model;
            if (event.picture != null){
                event.picture.event = event;
            }
            if (event.event_category != null){
                event.event_category.loadIconFromLocalStorage(MyApplication.getApplicationBaseContext());
            }
        }
        else if (model instanceof Spot){
            Spot spot = (Spot) model;
            if (spot.category != null){
                spot.category.loadIconFromLocalStorage(MyApplication.getApplicationBaseContext());
            }
        }
        return model;
    }
}
