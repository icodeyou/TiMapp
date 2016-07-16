package com.timappweb.timapp.utils;

import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by stephane on 6/8/2016.
 */
public class ReflectionHelper {

    private static final String TAG = "ReflectionHelper";

    public static Field getFieldRecursively(Class type, String fieldName) {

        try {
            // Check if model has the corresponding field
            Field field = type.getDeclaredField(fieldName);
            Expose annotation = field.getAnnotation(Expose.class);
            if (!annotation.deserialize()){
                Log.v(TAG, "The field '" + type.getSimpleName() + "." + fieldName + "' is not exposed to deserialization");
                return null;
            }
            return field;
        } catch (NoSuchFieldException e) {
            Log.v(TAG, "Field '" + type.getSimpleName() + "." + fieldName +"' does not exist -> searching annotations");
            // Get by annotation
            for (Field tmpField : type.getFields()) {
                if (tmpField.isAnnotationPresent(SerializedName.class)) {
                    if (tmpField.getAnnotation(SerializedName.class).value().equals(fieldName)){
                        return tmpField;
                    }
                }
            }
        }
        // Search in super class
        if (type.getSuperclass() != null) {
            return getFieldRecursively(type.getSuperclass(), fieldName);
        }
        return null;
    }

    public static List<Field> getAllFields(Class type) {
        LinkedList list = new LinkedList();
        getAllFields(list, type);
        return list;
    }

    public static void getAllFields(List<Field> list, Class type) {
        list.addAll(Arrays.asList(type.getDeclaredFields()));
        if (type.getSuperclass() != null) {
            getAllFields(list, type.getSuperclass());
        }
    }
/*
    public static List<Field> getFieldsFromNames(Class type, String[] fields) {
        LinkedList list = new LinkedList();
        getFieldsFromNames(list, type, fields);
        return list;
    }
    public static void getFieldsFromNames(List<Field> list, Class type, String[] fields) {
        list.addAll(Arrays.asList(type.getDeclaredFields()));
        if (type.getSuperclass() != null) {
            getFieldsFromNames(list, type.getSuperclass());
        }
    }*/
}
