package com.timappweb.timapp.utils;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.Gson;

/**
 * Created by stephane on 6/2/2016.
 */
public class SerializeHelper {

    private static Gson serializer = new Gson();

    public static String pack(Object obj){
        return serializer.toJson(obj);
    }

    public static <T> T unpack(String data, Class<T> classOfT) {
        return serializer.fromJson(data, classOfT);
    }
}
