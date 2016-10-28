package com.timappweb.timapp.rest.callbacks;

import android.util.Log;

import com.activeandroid.Model;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;
import com.timappweb.timapp.utils.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by stephane on 6/6/2016.
 *
 * Copy data returned by the request into the model given in constructor
 * Example:
 *      Response body: {"fieldA": "valeur", "fieldB": {"fieldB":"valeur2"}}
 *      It will execute the following statements
 *          model.fieldA = "valeur"
 *          model.fieldB = new Object(fieldB = "valeur2")
 *
 *
 *  @warning: Given objects must have a default constructor
 */
public class AutoMergeCallback extends HttpCallback<JsonObject>{

    private static final String TAG = "AutoMergeCallback";
    private final Model model;

    public AutoMergeCallback(Model model) {
        this.model = model;
    }


    @Override
    public void created(JsonObject jsonElement) {

    }

    @Override
    public void successful(JsonObject jsonObject) {
        this.merge(model, jsonObject);
    }


    /**
     * Copy data of jsonObject inside current object
     */
    public void merge(Object currentObject, JsonObject jsonObject){
        if (jsonObject == null) return;
        if (currentObject == null){
            // For recursive merge, we need to create the model
            // But we don't know the object class at runtime...
            // So abort for now
            return ;
        }

        for (Map.Entry<String,JsonElement> entry: jsonObject.entrySet()){
            String fieldName = entry.getKey();
            // First we need to find the corresponding remoteField in our model
            Field field = ReflectionHelper.getFieldRecursively(currentObject.getClass(), fieldName);
            // We did not find any remoteField corresponding
            if (field == null){
                Log.v(TAG, "Cannot find corresponding remoteField: " + fieldName);
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
                    Object fieldObject = field.get(currentObject);
                    if (fieldObject == null){
                        fieldObject = field.getType().newInstance();
                        field.set(currentObject, fieldObject);
                    }
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
