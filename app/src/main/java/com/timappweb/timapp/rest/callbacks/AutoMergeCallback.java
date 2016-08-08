package com.timappweb.timapp.rest.callbacks;

import android.util.Log;

import com.activeandroid.Model;
import com.google.common.reflect.Reflection;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;
import com.timappweb.timapp.utils.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by stephane on 6/6/2016.
 *
 * Merge single entry in database
 */
public class AutoMergeCallback extends HttpCallback<JsonElement>{

    private static final String TAG = "AutoMergeCallback";
    private final Model model;

    public AutoMergeCallback(Model model) {
        this.model = model;
    }


    @Override
    public void created(JsonElement jsonElement) {

    }

    @Override
    public void successful(JsonElement jsonElement) {
        if (jsonElement.isJsonArray()){
            // TODO
        }
        else if (jsonElement.isJsonObject()){
            this.merge(model, jsonElement.getAsJsonObject());
        }
        else{
            Log.e(TAG, "Server response is neither an array or an object: " + jsonElement);
        }
    }

    /**
     *
     * @param currentObject
     * @param jsonObject
    public void merge(List<Object> currentObject, JsonArray jsonArray){

        for (JsonElement elem: jsonArray){
            this.merge(currentObject);
        }
    }
     */

    /**
     * Copy data of jsonObject inside current object
     */
    public void merge(Object currentObject, JsonObject jsonObject){
        if (jsonObject == null) return;

        for (Map.Entry<String,JsonElement> entry: jsonObject.entrySet()){
            String fieldName = entry.getKey();
            // First we need to find the corresponding field in our model
            Field field = ReflectionHelper.getFieldRecursively(currentObject.getClass(), fieldName);
            // We did not find any field corresponding
            if (field == null){
                Log.v(TAG, "Cannot find corresponding field: " + fieldName);
                continue;
            }
            try {
                JsonElement value = entry.getValue();
                if (value.isJsonArray()){
                    Object fieldValue = field.get(currentObject);
                    if (!field.isAnnotationPresent(ModelAssociation.class)) continue;
                    ModelAssociation associationInfo = field.getAnnotation(ModelAssociation.class);
                    if (fieldValue instanceof List){
                        Collection fieldCollection = (List) fieldValue;
                        if (fieldValue == null){
                            fieldValue = fieldValue.getClass().newInstance();
                        }
                        else{
                            fieldCollection.clear();
                        }
                        for (JsonElement element: value.getAsJsonArray()){
                            if (element.isJsonObject()){
                                Class<?> innerType = associationInfo.targetModel();
                                Object innerObject = innerType.newInstance();
                                merge(innerObject, element.getAsJsonObject());
                                fieldCollection.add(innerObject);
                            }
                            else{
                                Log.e(TAG, fieldName + ": JsonArray does not contains JsonObject");
                                break;
                            }
                        }
                    }
                }
                // Recursive merge
                else if (value.isJsonObject()){
                    this.merge(field.get(currentObject), value.getAsJsonObject());
                }
                else if (value.isJsonNull()){
                    field.set(currentObject, null);
                }
                else if (value.isJsonPrimitive()){
                    JsonPrimitive primitive = value.getAsJsonPrimitive();
                    if (primitive.isString()){
                        field.set(currentObject, primitive.getAsString());
                    }
                    else if (primitive.isBoolean()){
                        field.set(currentObject, primitive.getAsBoolean());
                    }
                    else if (primitive.isNumber()){
                        Number number = primitive.getAsNumber();
                        Class<?> destinationFieldType = field.getType();
                        try {
                            if (destinationFieldType.isAssignableFrom(int.class)){
                                field.setInt(currentObject, number.intValue());
                            }
                            else if (destinationFieldType.isAssignableFrom(Integer.class)){
                                field.set(currentObject, new Integer(number.intValue()));
                            }
                            else if (destinationFieldType.isAssignableFrom(double.class)) {
                                field.setDouble(currentObject, number.doubleValue());
                            }
                            else if (destinationFieldType.isAssignableFrom(Double.class)) {
                                field.set(currentObject, new Double(number.doubleValue()));
                            }
                            else if (destinationFieldType.isAssignableFrom(float.class)) {
                                field.setFloat(currentObject, number.floatValue());
                            }
                            else if (destinationFieldType.isAssignableFrom(Float.class)){
                                field.set(currentObject, new Float(number.floatValue()));
                            }
                            else if (destinationFieldType.isAssignableFrom(long.class)) {
                                field.setLong(currentObject, number.longValue());
                            }
                            else if (destinationFieldType.isAssignableFrom(Long.class)){
                                field.set(currentObject, new Long(number.longValue()));
                            }
                            else {
                                Log.e(TAG, "Cannot assign type " + destinationFieldType + ". Setter not found");
                            }
                        }
                        catch (IllegalArgumentException ex){
                            Log.e(TAG, "IllegalArgumentException: " + destinationFieldType + ": " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }

                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

}
